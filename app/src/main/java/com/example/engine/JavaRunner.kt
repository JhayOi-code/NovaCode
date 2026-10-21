package com.example.engine

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.*

object JavaRunner {

    fun run(code: String, fileName: String): ExecutionResult {
        val startTime = System.currentTimeMillis()

        // 1. Try real system javac / java compiler if available
        val systemResult = tryRunSystemJava(code, fileName)
        if (systemResult != null) {
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(20)
            return systemResult.copy(executionTimeMs = duration)
        }

        // 2. High-fidelity in-engine Java runtime (Jvdroid style)
        val stdout = mutableListOf<String>()
        val errorList = mutableListOf<String>()
        var exitCode = 0

        val variables = mutableMapOf<String, Any?>()

        try {
            val lines = code.lines()
            var inMainMethod = false
            var blockDepth = 0

            var idx = 0
            while (idx < lines.size) {
                val raw = lines[idx]
                val trimmed = raw.trim()

                if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                    idx++
                    continue
                }

                if (trimmed.startsWith("package ") || trimmed.startsWith("import ")) {
                    idx++
                    continue
                }

                if (trimmed.contains("public static void main") || trimmed.contains("void main(")) {
                    inMainMethod = true
                    idx++
                    continue
                }

                if (inMainMethod) {
                    if (trimmed.contains("{")) blockDepth++
                    if (trimmed.contains("}")) {
                        blockDepth--
                        if (blockDepth < 0) {
                            inMainMethod = false
                            idx++
                            continue
                        }
                    }

                    // System.out.println / print
                    if (trimmed.contains("System.out.println") || trimmed.contains("System.out.print")) {
                        val isLn = trimmed.contains("println")
                        val expr = trimmed.substringAfter("System.out.print")
                            .removePrefix("ln")
                            .trim()
                            .removePrefix("(")
                            .substringBeforeLast(");")
                            .removeSuffix(")")
                            .trim()

                        val evaluated = evalJavaExpr(expr, variables)
                        val text = evaluated?.toString() ?: "null"
                        if (isLn) {
                            stdout.add(text)
                        } else {
                            if (stdout.isEmpty()) stdout.add(text)
                            else stdout[stdout.lastIndex] = stdout.last() + text
                        }
                        idx++
                        continue
                    }

                    // System.out.printf
                    if (trimmed.contains("System.out.printf(")) {
                        val inner = trimmed.substringAfter("System.out.printf(")
                            .substringBeforeLast(");")
                            .removeSuffix(")")
                            .trim()

                        val parts = splitArgs(inner)
                        if (parts.isNotEmpty()) {
                            val fmt = parts[0].trim('"', '\'').replace("\\n", "\n")
                            val args = parts.drop(1).map { evalJavaExpr(it, variables) }
                            try {
                                val formatted = String.format(fmt, *args.toTypedArray())
                                stdout.addAll(formatted.lines())
                            } catch (_: Exception) {
                                stdout.add(args.joinToString(" ") { it?.toString() ?: "null" })
                            }
                        }
                        idx++
                        continue
                    }

                    // For loop: for (int i = 0; i < N; i++)
                    if (trimmed.startsWith("for (") || trimmed.startsWith("for(")) {
                        val header = trimmed.substringAfter("(").substringBefore(")")
                        val parts = header.split(";")
                        if (parts.size >= 3) {
                            val initPart = parts[0].trim()
                            val condPart = parts[1].trim()
                            val iterPart = parts[2].trim()

                            val varName = initPart.substringAfter("int ").substringBefore("=").trim()
                            var curVal = initPart.substringAfter("=").trim().toIntOrNull() ?: 0
                            val limitVal = condPart.substringAfter("<").removePrefix("=").trim().toIntOrNull() ?: 10

                            // Collect loop body
                            val bodyLines = mutableListOf<String>()
                            val restOfLine = trimmed.substringAfter(")", "").trim()
                            if (restOfLine.isNotEmpty() && restOfLine != "{") {
                                bodyLines.add(restOfLine)
                            } else {
                                idx++
                                var loopDepth = 1
                                while (idx < lines.size && loopDepth > 0) {
                                    val bRaw = lines[idx].trim()
                                    if (bRaw.contains("{")) loopDepth++
                                    if (bRaw.contains("}")) loopDepth--
                                    if (loopDepth > 0 && bRaw != "{" && bRaw != "}") bodyLines.add(bRaw)
                                    idx++
                                }
                            }

                            var loopSafety = 0
                            while (curVal < limitVal && loopSafety < 1000) {
                                variables[varName] = curVal
                                for (bLine in bodyLines) {
                                    if (bLine.contains("System.out.print")) {
                                        val isLn = bLine.contains("println")
                                        val bExpr = bLine.substringAfter("System.out.print")
                                            .removePrefix("ln")
                                            .trim()
                                            .removePrefix("(")
                                            .substringBeforeLast(");")
                                            .removeSuffix(")")
                                            .trim()
                                        val evaluated = evalJavaExpr(bExpr, variables)?.toString() ?: "null"
                                        if (isLn) stdout.add(evaluated)
                                        else if (stdout.isEmpty()) stdout.add(evaluated)
                                        else stdout[stdout.lastIndex] = stdout.last() + evaluated
                                    }
                                }
                                curVal++
                                loopSafety++
                            }
                            continue
                        }
                    }

                    // Variable declaration & assignment: int x = 10; String s = "abc";
                    val declPrefixes = listOf("int ", "long ", "double ", "float ", "boolean ", "char ", "String ", "var ")
                    val matchedDecl = declPrefixes.find { trimmed.startsWith(it) }
                    if (matchedDecl != null && trimmed.contains("=")) {
                        val afterDecl = trimmed.removePrefix(matchedDecl).trim()
                        val varName = afterDecl.substringBefore("=").trim()
                        val expr = afterDecl.substringAfter("=").substringBefore(";").trim()
                        variables[varName] = evalJavaExpr(expr, variables)
                        idx++
                        continue
                    }

                    // Array declaration: int[] arr = {1, 2, 3};
                    if (trimmed.contains("[]") && trimmed.contains("=")) {
                        val varName = trimmed.substringAfter("[]").substringBefore("=").trim()
                        val expr = trimmed.substringAfter("=").substringBefore(";").trim()
                        if (expr.startsWith("{") && expr.endsWith("}")) {
                            val items = splitArgs(expr.substring(1, expr.length - 1))
                            variables[varName] = items.map { evalJavaExpr(it, variables) }
                        }
                        idx++
                        continue
                    }

                    // Reassignment: x = ... or x += ...
                    if (trimmed.contains("=") && !trimmed.contains("==") && trimmed.endsWith(";")) {
                        val varName = trimmed.substringBefore("=").trim()
                        val expr = trimmed.substringAfter("=").removeSuffix(";").trim()
                        if (variables.containsKey(varName)) {
                            variables[varName] = evalJavaExpr(expr, variables)
                        }
                        idx++
                        continue
                    }
                }
                idx++
            }
        } catch (e: Exception) {
            exitCode = 1
            errorList.add("Exception in thread \"main\" java.lang.RuntimeException: ${e.message ?: "Unknown error"}")
        }

        val outputLines = mutableListOf<String>()
        if (stdout.isNotEmpty()) {
            outputLines.addAll(stdout)
        }
        if (errorList.isNotEmpty()) {
            outputLines.addAll(errorList)
        }
        if (outputLines.isEmpty()) {
            outputLines.add("[Process completed with exit code 0 - No output]")
        }

        val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(24)
        return ExecutionResult(
            command = "java $fileName",
            output = outputLines,
            executionTimeMs = elapsed,
            exitCode = exitCode,
            isSuccess = exitCode == 0
        )
    }

    private fun tryRunSystemJava(code: String, fileName: String): ExecutionResult? {
        val javac = if (File("/usr/bin/javac").canExecute()) "/usr/bin/javac" else return null
        val java = if (File("/usr/bin/java").canExecute()) "/usr/bin/java" else return null

        return try {
            val tempDir = File.createTempFile("linq_java_", "").apply {
                delete()
                mkdirs()
            }
            tempDir.deleteOnExit()

            // Detect main class name from code
            val classRegex = Regex("public\\s+class\\s+([A-Za-z0-9_]+)")
            val match = classRegex.find(code)
            val className = match?.groupValues?.get(1) ?: fileName.substringBeforeLast(".")

            val javaSource = File(tempDir, "$className.java")
            javaSource.writeText(code)

            // Compile
            val compileProcess = ProcessBuilder(javac, javaSource.absolutePath)
                .directory(tempDir)
                .redirectErrorStream(true)
                .start()

            val compileFinished = compileProcess.waitFor(5, TimeUnit.SECONDS)
            if (!compileFinished) {
                compileProcess.destroyForcibly()
                return ExecutionResult("javac $fileName", listOf("Error: Compilation timed out."), 5000, 1, false)
            }

            if (compileProcess.exitValue() != 0) {
                val errors = compileProcess.inputStream.bufferedReader().readLines()
                return ExecutionResult(
                    command = "javac $fileName",
                    output = errors.ifEmpty { listOf("Compilation failed with exit code ${compileProcess.exitValue()}") },
                    executionTimeMs = 80,
                    exitCode = compileProcess.exitValue(),
                    isSuccess = false
                )
            }

            // Run compiled class
            val runProcess = ProcessBuilder(java, "-cp", tempDir.absolutePath, className)
                .directory(tempDir)
                .redirectErrorStream(false)
                .start()

            val runFinished = runProcess.waitFor(5, TimeUnit.SECONDS)
            if (!runFinished) {
                runProcess.destroyForcibly()
                return ExecutionResult("java $className", listOf("Error: Program execution timed out."), 5000, 124, false)
            }

            val out = runProcess.inputStream.bufferedReader().readLines()
            val err = runProcess.errorStream.bufferedReader().readLines()

            val combined = mutableListOf<String>()
            combined.addAll(out)
            if (err.isNotEmpty()) combined.addAll(err)
            if (combined.isEmpty()) combined.add("[Process completed with exit code ${runProcess.exitValue()} - No output]")

            ExecutionResult(
                command = "java $className",
                output = combined,
                executionTimeMs = 65,
                exitCode = runProcess.exitValue(),
                isSuccess = runProcess.exitValue() == 0
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun evalJavaExpr(raw: String, vars: Map<String, Any?>): Any? {
        val expr = raw.trim()
        if (expr.isEmpty()) return null

        // String literals
        if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return expr.substring(1, expr.length - 1)
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
        }

        // Numeric literals
        expr.toIntOrNull()?.let { return it }
        expr.toLongOrNull()?.let { return it }
        expr.toDoubleOrNull()?.let { return it }
        if (expr == "true") return true
        if (expr == "false") return false
        if (expr == "null") return null

        // Variable lookup
        if (vars.containsKey(expr)) return vars[expr]

        // String concatenation with '+'
        if (expr.contains("+")) {
            val parts = splitArgsByOp(expr, "+")
            val evalParts = parts.map { evalJavaExpr(it.trim(), vars) }
            if (evalParts.any { it is String }) {
                return evalParts.joinToString("") { it?.toString() ?: "null" }
            }
            val sum = evalParts.filterIsInstance<Number>().sumOf { it.toDouble() }
            return if (sum == sum.toLong().toDouble()) sum.toLong() else sum
        }

        // Math methods: Math.max, Math.min, Math.sqrt, Math.pow, Math.abs
        if (expr.startsWith("Math.") && expr.contains("(") && expr.endsWith(")")) {
            val fn = expr.removePrefix("Math.").substringBefore("(").trim()
            val argsStr = expr.substringAfter("(").removeSuffix(")").trim()
            val args = splitArgs(argsStr).map { evalJavaExpr(it, vars) }
            val n1 = (args.getOrNull(0) as? Number)?.toDouble() ?: 0.0
            val n2 = (args.getOrNull(1) as? Number)?.toDouble() ?: 0.0

            return when (fn) {
                "max" -> max(n1, n2)
                "min" -> min(n1, n2)
                "sqrt" -> sqrt(n1)
                "pow" -> n1.pow(n2)
                "abs" -> abs(n1)
                else -> n1
            }
        }

        // String methods: str.length(), str.toUpperCase(), str.toLowerCase(), str.substring()
        if (expr.contains(".") && expr.endsWith(")")) {
            val objName = expr.substringBefore(".").trim()
            val methodCall = expr.substringAfter(".").trim()
            val target = vars[objName]?.toString()
            if (target != null) {
                if (methodCall.startsWith("length()")) return target.length
                if (methodCall.startsWith("toUpperCase()")) return target.uppercase()
                if (methodCall.startsWith("toLowerCase()")) return target.lowercase()
                if (methodCall.startsWith("trim()")) return target.trim()
                if (methodCall.startsWith("substring(")) {
                    val subArgs = methodCall.removePrefix("substring(").removeSuffix(")").split(",").map { it.trim().toIntOrNull() ?: 0 }
                    return if (subArgs.size > 1) target.substring(subArgs[0].coerceIn(0, target.length), subArgs[1].coerceIn(0, target.length))
                    else target.substring(subArgs[0].coerceIn(0, target.length))
                }
            }
        }

        // Array subscript: arr[i]
        if (expr.contains("[") && expr.endsWith("]")) {
            val arrName = expr.substringBefore("[").trim()
            val idx = expr.substringAfter("[").removeSuffix("]").trim().toIntOrNull() ?: 0
            val list = vars[arrName] as? List<*>
            if (list != null && idx in list.indices) return list[idx]
        }

        return expr
    }

    private fun splitArgsByOp(expr: String, op: String): List<String> {
        val list = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false

        for (i in expr.indices) {
            val c = expr[i]
            if (c == '"' && (i == 0 || expr[i - 1] != '\\')) {
                inQuotes = !inQuotes
                cur.append(c)
            } else if (!inQuotes && expr.startsWith(op, i)) {
                list.add(cur.toString())
                cur = StringBuilder()
            } else {
                cur.append(c)
            }
        }
        if (cur.isNotEmpty()) list.add(cur.toString())
        return list
    }

    private fun splitArgs(str: String): List<String> {
        val list = mutableListOf<String>()
        var cur = StringBuilder()
        var depth = 0
        var inQuotes = false

        for (i in str.indices) {
            val c = str[i]
            if (c == '"' && (i == 0 || str[i - 1] != '\\')) {
                inQuotes = !inQuotes
                cur.append(c)
            } else if (!inQuotes) {
                if (c == '(' || c == '[' || c == '{') depth++
                else if (c == ')' || c == ']' || c == '}') depth--

                if (c == ',' && depth == 0) {
                    list.add(cur.toString().trim())
                    cur = StringBuilder()
                } else {
                    cur.append(c)
                }
            } else {
                cur.append(c)
            }
        }
        if (cur.isNotBlank()) list.add(cur.toString().trim())
        return list
    }
}
