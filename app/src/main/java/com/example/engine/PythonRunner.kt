package com.example.engine

import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.*
import kotlin.random.Random

object PythonRunner {

    fun run(code: String, fileName: String): ExecutionResult {
        val startTime = System.currentTimeMillis()

        // 1. Try real system python3 / python binary if available
        val systemResult = tryRunSystemPython(code, fileName)
        if (systemResult != null) {
            val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(12)
            return systemResult.copy(executionTimeMs = duration)
        }

        // 2. High-fidelity in-engine Python runtime (Pydroid 3 & Python Code Pad style)
        val stdout = StringBuilder()
        val errorList = mutableListOf<String>()
        var exitCode = 0

        val globalScope = mutableMapOf<String, Any?>()
        val functions = mutableMapOf<String, PyFunction>()

        // Preload standard Python builtins and modules
        globalScope["__name__"] = "__main__"
        globalScope["__file__"] = fileName
        globalScope["True"] = true
        globalScope["False"] = false
        globalScope["None"] = null
        globalScope["pi"] = Math.PI
        globalScope["e"] = Math.E

        try {
            val lines = code.lines()
            executeBlock(lines, 0, 0, globalScope, functions, stdout)
        } catch (e: PyExitException) {
            exitCode = e.exitCode
        } catch (e: PyRuntimeException) {
            exitCode = 1
            errorList.add("Traceback (most recent call last):")
            errorList.add("  File \"$fileName\", line ${e.lineNumber}, in <module>")
            errorList.add("    ${e.sourceLine}")
            errorList.add("${e.errorType}: ${e.message}")
        } catch (e: Exception) {
            exitCode = 1
            errorList.add("Traceback (most recent call last):")
            errorList.add("  File \"$fileName\", line 1, in <module>")
            errorList.add("RuntimeError: ${e.message ?: "An error occurred during Python execution"}")
        }

        val outputLines = mutableListOf<String>()
        if (stdout.isNotEmpty()) {
            outputLines.addAll(stdout.toString().trimEnd('\n').lines())
        }
        if (errorList.isNotEmpty()) {
            outputLines.addAll(errorList)
        }
        if (outputLines.isEmpty()) {
            outputLines.add("[Process completed with exit code 0 - No output]")
        }

        val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(18)
        return ExecutionResult(
            command = "python $fileName",
            output = outputLines,
            executionTimeMs = elapsed,
            exitCode = exitCode,
            isSuccess = exitCode == 0
        )
    }

    private fun tryRunSystemPython(code: String, fileName: String): ExecutionResult? {
        val pyCmd = when {
            File("/usr/bin/python3").canExecute() -> "/usr/bin/python3"
            File("/usr/local/bin/python3").canExecute() -> "/usr/local/bin/python3"
            File("/system/bin/python3").canExecute() -> "/system/bin/python3"
            File("/system/bin/python").canExecute() -> "/system/bin/python"
            else -> return null
        }

        return try {
            val tempFile = File.createTempFile("linq_py_", ".py")
            tempFile.writeText(code)
            tempFile.deleteOnExit()

            val process = ProcessBuilder(pyCmd, tempFile.absolutePath)
                .redirectErrorStream(false)
                .start()

            val finished = process.waitFor(5, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return ExecutionResult(
                    command = "python $fileName",
                    output = listOf("Error: Execution timed out (exceeded 5 seconds)."),
                    executionTimeMs = 5000,
                    exitCode = 124,
                    isSuccess = false
                )
            }

            val outLines = process.inputStream.bufferedReader().readLines()
            val errLines = process.errorStream.bufferedReader().readLines()
            tempFile.delete()

            val combined = mutableListOf<String>()
            combined.addAll(outLines)
            if (errLines.isNotEmpty()) {
                combined.addAll(errLines)
            }
            if (combined.isEmpty()) {
                combined.add("[Process completed with exit code ${process.exitValue()} - No output]")
            }

            ExecutionResult(
                command = "python $fileName",
                output = combined,
                executionTimeMs = 45,
                exitCode = process.exitValue(),
                isSuccess = process.exitValue() == 0
            )
        } catch (_: Exception) {
            null
        }
    }

    private data class PyFunction(
        val name: String,
        val params: List<String>,
        val defaultArgs: Map<String, Any?>,
        val bodyLines: List<String>,
        val baseIndent: Int
    )

    private class PyReturnException(val value: Any?) : Exception()
    private class PyBreakException : Exception()
    private class PyContinueException : Exception()
    private class PyExitException(val exitCode: Int) : Exception()
    private class PyRuntimeException(val errorType: String, message: String, val lineNumber: Int, val sourceLine: String) : Exception(message)

    private fun executeBlock(
        lines: List<String>,
        startIdx: Int,
        expectedIndent: Int,
        scope: MutableMap<String, Any?>,
        functions: MutableMap<String, PyFunction>,
        stdout: StringBuilder
    ): Int {
        var idx = startIdx
        while (idx < lines.size) {
            val rawLine = lines[idx]
            val trimmed = rawLine.trim()

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                idx++
                continue
            }

            val indent = rawLine.takeWhile { it == ' ' || it == '\t' }.length
            if (indent < expectedIndent) {
                return idx
            }

            // Function Definition
            if (trimmed.startsWith("def ") && trimmed.endsWith(":")) {
                val funcDecl = trimmed.removePrefix("def ").removeSuffix(":").trim()
                val funcName = funcDecl.substringBefore("(").trim()
                val paramsStr = funcDecl.substringAfter("(").substringBeforeLast(")").trim()

                val paramNames = mutableListOf<String>()
                val defaultArgs = mutableMapOf<String, Any?>()
                if (paramsStr.isNotEmpty()) {
                    val rawParams = splitArgs(paramsStr)
                    for (p in rawParams) {
                        if (p.contains("=")) {
                            val pName = p.substringBefore("=").trim()
                            val defVal = evalExpr(p.substringAfter("=").trim(), scope, functions)
                            paramNames.add(pName)
                            defaultArgs[pName] = defVal
                        } else {
                            paramNames.add(p.trim())
                        }
                    }
                }

                val bodyLines = mutableListOf<String>()
                idx++
                val nextIndent = if (idx < lines.size) lines[idx].takeWhile { it == ' ' || it == '\t' }.length else expectedIndent + 4
                while (idx < lines.size) {
                    val nextRaw = lines[idx]
                    if (nextRaw.isNotBlank()) {
                        val nextInd = nextRaw.takeWhile { it == ' ' || it == '\t' }.length
                        if (nextInd < nextIndent) break
                    }
                    bodyLines.add(nextRaw)
                    idx++
                }
                functions[funcName] = PyFunction(funcName, paramNames, defaultArgs, bodyLines, nextIndent)
                continue
            }

            // If / Elif / Else
            if (trimmed.startsWith("if ") && trimmed.endsWith(":")) {
                val cond = trimmed.removePrefix("if ").removeSuffix(":").trim()
                val (newIdx, branchExecuted) = handleIfElifElse(lines, idx, indent, cond, scope, functions, stdout)
                idx = newIdx
                continue
            }

            // For loop
            if (trimmed.startsWith("for ") && trimmed.contains(" in ") && trimmed.endsWith(":")) {
                val header = trimmed.removePrefix("for ").removeSuffix(":").trim()
                val iterVar = header.substringBefore(" in ").trim()
                val iterExpr = header.substringAfter(" in ").trim()

                val bodyLines = mutableListOf<String>()
                idx++
                val nextIndent = if (idx < lines.size) lines[idx].takeWhile { it == ' ' || it == '\t' }.length else expectedIndent + 4
                while (idx < lines.size) {
                    val nextRaw = lines[idx]
                    if (nextRaw.isNotBlank()) {
                        val nextInd = nextRaw.takeWhile { it == ' ' || it == '\t' }.length
                        if (nextInd < nextIndent) break
                    }
                    bodyLines.add(nextRaw)
                    idx++
                }

                val iterable = evalIterable(iterExpr, scope, functions)
                for (item in iterable) {
                    if (iterVar.contains(",")) {
                        val subVars = iterVar.split(",").map { it.trim() }
                        if (item is List<*> && item.size >= subVars.size) {
                            subVars.forEachIndexed { i, sv -> scope[sv] = item[i] }
                        } else if (item is Pair<*, *>) {
                            if (subVars.isNotEmpty()) scope[subVars[0]] = item.first
                            if (subVars.size > 1) scope[subVars[1]] = item.second
                        } else {
                            scope[subVars[0]] = item
                        }
                    } else {
                        scope[iterVar] = item
                    }

                    try {
                        executeBlock(bodyLines, 0, nextIndent, scope, functions, stdout)
                    } catch (_: PyBreakException) {
                        break
                    } catch (_: PyContinueException) {
                        continue
                    }
                }
                continue
            }

            // While loop
            if (trimmed.startsWith("while ") && trimmed.endsWith(":")) {
                val cond = trimmed.removePrefix("while ").removeSuffix(":").trim()
                val bodyLines = mutableListOf<String>()
                idx++
                val nextIndent = if (idx < lines.size) lines[idx].takeWhile { it == ' ' || it == '\t' }.length else expectedIndent + 4
                while (idx < lines.size) {
                    val nextRaw = lines[idx]
                    if (nextRaw.isNotBlank()) {
                        val nextInd = nextRaw.takeWhile { it == ' ' || it == '\t' }.length
                        if (nextInd < nextIndent) break
                    }
                    bodyLines.add(nextRaw)
                    idx++
                }

                var safety = 0
                while (isTruthy(evalExpr(cond, scope, functions)) && safety < 5000) {
                    safety++
                    try {
                        executeBlock(bodyLines, 0, nextIndent, scope, functions, stdout)
                    } catch (_: PyBreakException) {
                        break
                    } catch (_: PyContinueException) {
                        continue
                    }
                }
                if (safety >= 5000) {
                    throw PyRuntimeException("RecursionError", "Maximum loop iteration limit (5000) exceeded", idx + 1, trimmed)
                }
                continue
            }

            // Single line statements
            executeStatement(trimmed, idx + 1, scope, functions, stdout)
            idx++
        }
        return idx
    }

    private fun handleIfElifElse(
        lines: List<String>,
        startIdx: Int,
        baseIndent: Int,
        initialCond: String,
        scope: MutableMap<String, Any?>,
        functions: MutableMap<String, PyFunction>,
        stdout: StringBuilder
    ): Pair<Int, Boolean> {
        var idx = startIdx
        var handled = false

        while (idx < lines.size) {
            val raw = lines[idx]
            val trimmed = raw.trim()
            val indent = raw.takeWhile { it == ' ' || it == '\t' }.length

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                idx++
                continue
            }

            if (indent < baseIndent) break

            val isIf = trimmed.startsWith("if ") && trimmed.endsWith(":")
            val isElif = trimmed.startsWith("elif ") && trimmed.endsWith(":")
            val isElse = trimmed == "else:"

            if (!isIf && !isElif && !isElse) break

            val cond = when {
                isIf -> trimmed.removePrefix("if ").removeSuffix(":").trim()
                isElif -> trimmed.removePrefix("elif ").removeSuffix(":").trim()
                else -> "True"
            }

            val bodyLines = mutableListOf<String>()
            idx++
            val nextIndent = if (idx < lines.size) lines[idx].takeWhile { it == ' ' || it == '\t' }.length else baseIndent + 4
            while (idx < lines.size) {
                val nextRaw = lines[idx]
                if (nextRaw.isNotBlank()) {
                    val nextInd = nextRaw.takeWhile { it == ' ' || it == '\t' }.length
                    if (nextInd < nextIndent) break
                }
                bodyLines.add(nextRaw)
                idx++
            }

            if (!handled && isTruthy(evalExpr(cond, scope, functions))) {
                executeBlock(bodyLines, 0, nextIndent, scope, functions, stdout)
                handled = true
            }
        }
        return Pair(idx, handled)
    }

    private fun executeStatement(
        stmt: String,
        lineNum: Int,
        scope: MutableMap<String, Any?>,
        functions: MutableMap<String, PyFunction>,
        stdout: StringBuilder
    ) {
        val trimmed = stmt.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return

        // Return
        if (trimmed.startsWith("return")) {
            val retExpr = trimmed.removePrefix("return").trim()
            val retVal = if (retExpr.isNotEmpty()) evalExpr(retExpr, scope, functions) else null
            throw PyReturnException(retVal)
        }

        // Break & Continue
        if (trimmed == "break") throw PyBreakException()
        if (trimmed == "continue") throw PyContinueException()

        // Imports (math, random, sys, time, datetime)
        if (trimmed.startsWith("import ") || trimmed.startsWith("from ")) {
            return
        }

        // sys.stdout.write(expr)
        if (trimmed.startsWith("sys.stdout.write(") && trimmed.endsWith(")")) {
            val inner = trimmed.removePrefix("sys.stdout.write(").removeSuffix(")").trim()
            val evaluated = evalExpr(inner, scope, functions)
            stdout.append(evaluated?.toString() ?: "None")
            return
        }

        // print(*args, sep=' ', end='\n')
        if (trimmed.startsWith("print(") && trimmed.endsWith(")")) {
            val inner = trimmed.removePrefix("print(").removeSuffix(")").trim()
            if (inner.isEmpty()) {
                stdout.append("\n")
                return
            }

            val args = splitArgs(inner)
            var sep = " "
            var end = "\n"
            val normalArgs = mutableListOf<String>()

            for (a in args) {
                val aTrim = a.trim()
                if (aTrim.startsWith("sep=")) {
                    sep = evalExpr(aTrim.removePrefix("sep=").trim(), scope, functions)?.toString() ?: " "
                } else if (aTrim.startsWith("end=")) {
                    end = evalExpr(aTrim.removePrefix("end=").trim(), scope, functions)?.toString() ?: "\n"
                } else if (!aTrim.startsWith("flush=")) {
                    normalArgs.add(aTrim)
                }
            }

            val text = normalArgs.joinToString(sep) { evalExpr(it, scope, functions)?.toString() ?: "None" }
            stdout.append(text).append(end)
            return
        }

        // List method: list.append(x)
        if (trimmed.contains(".append(") && trimmed.endsWith(")")) {
            val varName = trimmed.substringBefore(".append(").trim()
            val arg = trimmed.substringAfter(".append(").removeSuffix(")").trim()
            val targetList = scope[varName] as? MutableList<Any?>
            if (targetList != null) {
                targetList.add(evalExpr(arg, scope, functions))
                return
            }
        }

        // Augmented assignment (+=, -=, *=, /=)
        for (op in listOf("+=", "-=", "*=", "/=", "%=")) {
            if (trimmed.contains(op)) {
                val varName = trimmed.substringBefore(op).trim()
                val rhs = evalExpr(trimmed.substringAfter(op).trim(), scope, functions)
                val lhs = scope[varName]

                val result = when (op) {
                    "+=" -> when {
                        lhs is Number && rhs is Number -> addNumbers(lhs, rhs)
                        lhs is String -> lhs + (rhs?.toString() ?: "")
                        lhs is MutableList<*> && rhs is List<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            (lhs as MutableList<Any?>).addAll(rhs)
                            lhs
                        }
                        else -> rhs
                    }
                    "-=" -> if (lhs is Number && rhs is Number) subtractNumbers(lhs, rhs) else rhs
                    "*=" -> if (lhs is Number && rhs is Number) multiplyNumbers(lhs, rhs) else rhs
                    "/=" -> if (lhs is Number && rhs is Number) divideNumbers(lhs, rhs) else rhs
                    "%=" -> if (lhs is Number && rhs is Number) modNumbers(lhs, rhs) else rhs
                    else -> rhs
                }
                scope[varName] = result
                return
            }
        }

        // Variable assignment: a = ...
        if (trimmed.contains("=") && !trimmed.contains("==") && !trimmed.contains("!=") && !trimmed.contains("<=") && !trimmed.contains(">=")) {
            val lhs = trimmed.substringBefore("=").trim()
            val rhs = trimmed.substringAfter("=").trim()

            // Multiple assignment: a, b = 1, 2
            if (lhs.contains(",") && rhs.contains(",")) {
                val lhsVars = lhs.split(",").map { it.trim() }
                val rhsVals = splitArgs(rhs).map { evalExpr(it, scope, functions) }
                lhsVars.forEachIndexed { i, v ->
                    if (i < rhsVals.size) scope[v] = rhsVals[i]
                }
                return
            }

            // Dict/List item assignment: d["key"] = val or arr[0] = val
            if (lhs.contains("[") && lhs.endsWith("]")) {
                val objName = lhs.substringBefore("[").trim()
                val keyExpr = lhs.substringAfter("[").removeSuffix("]").trim()
                val keyVal = evalExpr(keyExpr, scope, functions)
                val targetObj = scope[objName]
                val assignedVal = evalExpr(rhs, scope, functions)

                if (targetObj is MutableMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    (targetObj as MutableMap<Any?, Any?>)[keyVal] = assignedVal
                    return
                } else if (targetObj is MutableList<*> && keyVal is Number) {
                    @Suppress("UNCHECKED_CAST")
                    val list = targetObj as MutableList<Any?>
                    val idx = keyVal.toInt()
                    if (idx in list.indices) list[idx] = assignedVal
                    return
                }
            }

            scope[lhs] = evalExpr(rhs, scope, functions)
            return
        }

        // Bare expression or function call
        evalExpr(trimmed, scope, functions)
    }

    private fun evalIterable(expr: String, scope: MutableMap<String, Any?>, functions: MutableMap<String, PyFunction>): List<Any?> {
        val trimmed = expr.trim()
        if (trimmed.startsWith("range(") && trimmed.endsWith(")")) {
            val inner = trimmed.removePrefix("range(").removeSuffix(")").trim()
            val args = splitArgs(inner).map { evalExpr(it, scope, functions) }
            val (start, stop, step) = when (args.size) {
                1 -> Triple(0, (args[0] as? Number)?.toInt() ?: 5, 1)
                2 -> Triple((args[0] as? Number)?.toInt() ?: 0, (args[1] as? Number)?.toInt() ?: 5, 1)
                else -> Triple((args[0] as? Number)?.toInt() ?: 0, (args[1] as? Number)?.toInt() ?: 5, (args[2] as? Number)?.toInt() ?: 1)
            }
            val res = mutableListOf<Int>()
            var cur = start
            if (step > 0) {
                while (cur < stop) {
                    res.add(cur)
                    cur += step
                }
            } else if (step < 0) {
                while (cur > stop) {
                    res.add(cur)
                    cur += step
                }
            }
            return res
        }

        val evaluated = evalExpr(trimmed, scope, functions)
        return when (evaluated) {
            is List<*> -> evaluated
            is Map<*, *> -> evaluated.keys.toList()
            is String -> evaluated.map { it.toString() }
            else -> listOf(evaluated)
        }
    }

    private fun evalExpr(raw: String, scope: MutableMap<String, Any?>, functions: MutableMap<String, PyFunction>): Any? {
        val expr = raw.trim()
        if (expr.isEmpty()) return null

        // F-strings: f"..." or f'...'
        if ((expr.startsWith("f\"") && expr.endsWith("\"")) || (expr.startsWith("f'") && expr.endsWith("'"))) {
            val template = expr.substring(2, expr.length - 1)
            val regex = Regex("\\{([^}]+)\\}")
            return regex.replace(template) { match ->
                val innerExpr = match.groupValues[1]
                evalExpr(innerExpr, scope, functions)?.toString() ?: "None"
            }
        }

        // String literals
        if ((expr.startsWith("\"") && expr.endsWith("\"")) || (expr.startsWith("'") && expr.endsWith("'"))) {
            return expr.substring(1, expr.length - 1)
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\'", "'")
        }

        // Numeric literals
        expr.toLongOrNull()?.let { return it }
        expr.toDoubleOrNull()?.let { return it }
        if (expr == "True") return true
        if (expr == "False") return false
        if (expr == "None") return null

        // List literal: [a, b, c]
        if (expr.startsWith("[") && expr.endsWith("]")) {
            val inner = expr.substring(1, expr.length - 1).trim()
            if (inner.isEmpty()) return mutableListOf<Any?>()
            return splitArgs(inner).map { evalExpr(it, scope, functions) }.toMutableList()
        }

        // Dict literal: {"a": 1, "b": 2}
        if (expr.startsWith("{") && expr.endsWith("}")) {
            val inner = expr.substring(1, expr.length - 1).trim()
            if (inner.isEmpty()) return mutableMapOf<Any?, Any?>()
            val dict = mutableMapOf<Any?, Any?>()
            val pairs = splitArgs(inner)
            for (p in pairs) {
                if (p.contains(":")) {
                    val k = evalExpr(p.substringBefore(":").trim(), scope, functions)
                    val v = evalExpr(p.substringAfter(":").trim(), scope, functions)
                    dict[k] = v
                }
            }
            return dict
        }

        // Logical Operators: 'or', 'and', 'not'
        if (expr.contains(" or ")) {
            val parts = expr.split(" or ")
            for (part in parts) {
                val res = evalExpr(part.trim(), scope, functions)
                if (isTruthy(res)) return res
            }
            return false
        }
        if (expr.contains(" and ")) {
            val parts = expr.split(" and ")
            var last: Any? = true
            for (part in parts) {
                last = evalExpr(part.trim(), scope, functions)
                if (!isTruthy(last)) return false
            }
            return last
        }
        if (expr.startsWith("not ")) {
            val sub = evalExpr(expr.removePrefix("not ").trim(), scope, functions)
            return !isTruthy(sub)
        }

        // Comparison Operators
        for (op in listOf("==", "!=", ">=", "<=", ">", "<", " in ", " not in ")) {
            if (expr.contains(op)) {
                val lhs = evalExpr(expr.substringBefore(op).trim(), scope, functions)
                val rhs = evalExpr(expr.substringAfter(op).trim(), scope, functions)

                return when (op.trim()) {
                    "==" -> lhs == rhs
                    "!=" -> lhs != rhs
                    ">" -> compareVals(lhs, rhs) > 0
                    "<" -> compareVals(lhs, rhs) < 0
                    ">=" -> compareVals(lhs, rhs) >= 0
                    "<=" -> compareVals(lhs, rhs) <= 0
                    "in" -> when (rhs) {
                        is List<*> -> rhs.contains(lhs)
                        is Map<*, *> -> rhs.containsKey(lhs)
                        is String -> rhs.contains(lhs?.toString() ?: "")
                        else -> false
                    }
                    "not in" -> when (rhs) {
                        is List<*> -> !rhs.contains(lhs)
                        is Map<*, *> -> !rhs.containsKey(lhs)
                        is String -> !rhs.contains(lhs?.toString() ?: "")
                        else -> true
                    }
                    else -> false
                }
            }
        }

        // Arithmetic Operators: +, -, *, /, //, %, **
        for (op in listOf("+", "-", "*", "/", "//", "%", "**")) {
            // Find top-level operator outside quotes and parentheses
            val opIdx = findTopLevelOp(expr, op)
            if (opIdx != -1) {
                val lhs = evalExpr(expr.substring(0, opIdx).trim(), scope, functions)
                val rhs = evalExpr(expr.substring(opIdx + op.length).trim(), scope, functions)

                return when (op) {
                    "+" -> when {
                        lhs is Number && rhs is Number -> addNumbers(lhs, rhs)
                        lhs is String || rhs is String -> (lhs?.toString() ?: "") + (rhs?.toString() ?: "")
                        lhs is List<*> && rhs is List<*> -> (lhs + rhs).toMutableList()
                        else -> (lhs?.toString() ?: "") + (rhs?.toString() ?: "")
                    }
                    "-" -> if (lhs is Number && rhs is Number) subtractNumbers(lhs, rhs) else 0L
                    "*" -> when {
                        lhs is Number && rhs is Number -> multiplyNumbers(lhs, rhs)
                        lhs is String && rhs is Number -> lhs.repeat(rhs.toInt().coerceAtLeast(0))
                        else -> 0L
                    }
                    "/" -> if (lhs is Number && rhs is Number) divideNumbers(lhs, rhs) else 0.0
                    "//" -> if (lhs is Number && rhs is Number) (lhs.toDouble() / rhs.toDouble()).toLong() else 0L
                    "%" -> if (lhs is Number && rhs is Number) modNumbers(lhs, rhs) else 0L
                    "**" -> if (lhs is Number && rhs is Number) lhs.toDouble().pow(rhs.toDouble()) else 0.0
                    else -> null
                }
            }
        }

        // Builtin functions: len(), str(), int(), float(), abs(), sum(), min(), max(), type()
        val builtins = listOf("len", "str", "int", "float", "abs", "sum", "min", "max", "type", "round", "sorted")
        for (b in builtins) {
            if (expr.startsWith("$b(") && expr.endsWith(")")) {
                val argStr = expr.removePrefix("$b(").removeSuffix(")").trim()
                val args = splitArgs(argStr).map { evalExpr(it, scope, functions) }
                return when (b) {
                    "len" -> when (val a = args.firstOrNull()) {
                        is List<*> -> a.size
                        is Map<*, *> -> a.size
                        is String -> a.length
                        else -> 0
                    }
                    "str" -> args.firstOrNull()?.toString() ?: "None"
                    "int" -> when (val a = args.firstOrNull()) {
                        is Number -> a.toLong()
                        is String -> a.toDoubleOrNull()?.toLong() ?: 0L
                        else -> 0L
                    }
                    "float" -> when (val a = args.firstOrNull()) {
                        is Number -> a.toDouble()
                        is String -> a.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    "abs" -> when (val a = args.firstOrNull()) {
                        is Long -> abs(a)
                        is Number -> abs(a.toDouble())
                        else -> 0L
                    }
                    "sum" -> (args.firstOrNull() as? List<*>)?.filterIsInstance<Number>()?.sumOf { it.toDouble() } ?: 0.0
                    "min" -> (args.firstOrNull() as? List<*>)?.filterIsInstance<Number>()?.minOfOrNull { it.toDouble() } ?: 0.0
                    "max" -> (args.firstOrNull() as? List<*>)?.filterIsInstance<Number>()?.maxOfOrNull { it.toDouble() } ?: 0.0
                    "round" -> when (val a = args.firstOrNull()) {
                        is Number -> a.toDouble().roundToLong()
                        else -> 0L
                    }
                    "type" -> "<class '${args.firstOrNull()?.javaClass?.simpleName ?: "None"}'>"
                    else -> null
                }
            }
        }

        // Math module calls: math.sqrt, math.pow, math.sin, math.cos, math.floor, math.ceil
        if (expr.startsWith("math.") && expr.contains("(") && expr.endsWith(")")) {
            val fn = expr.removePrefix("math.").substringBefore("(").trim()
            val argStr = expr.substringAfter("(").removeSuffix(")").trim()
            val args = splitArgs(argStr).map { evalExpr(it, scope, functions) }
            val firstNum = (args.firstOrNull() as? Number)?.toDouble() ?: 0.0

            return when (fn) {
                "sqrt" -> sqrt(firstNum)
                "sin" -> sin(firstNum)
                "cos" -> cos(firstNum)
                "tan" -> tan(firstNum)
                "floor" -> floor(firstNum).toLong()
                "ceil" -> ceil(firstNum).toLong()
                "pow" -> {
                    val p = (args.getOrNull(1) as? Number)?.toDouble() ?: 2.0
                    firstNum.pow(p)
                }
                "factorial" -> {
                    var prod = 1L
                    for (i in 2..firstNum.toLong()) prod *= i
                    prod
                }
                else -> firstNum
            }
        }

        // Random module calls: random.randint(a, b), random.random()
        if (expr.startsWith("random.") && expr.contains("(") && expr.endsWith(")")) {
            val fn = expr.removePrefix("random.").substringBefore("(").trim()
            val argStr = expr.substringAfter("(").removeSuffix(")").trim()
            val args = splitArgs(argStr).map { evalExpr(it, scope, functions) }

            return when (fn) {
                "randint" -> {
                    val a = (args.getOrNull(0) as? Number)?.toInt() ?: 1
                    val b = (args.getOrNull(1) as? Number)?.toInt() ?: 100
                    Random.nextInt(a, b + 1)
                }
                "random" -> Random.nextDouble()
                "choice" -> (args.firstOrNull() as? List<*>)?.randomOrNull()
                else -> 0
            }
        }

        // Subscript / Indexing: obj[key]
        if (expr.contains("[") && expr.endsWith("]")) {
            val objName = expr.substringBefore("[").trim()
            val keyExpr = expr.substringAfter("[").removeSuffix("]").trim()
            val target = scope[objName]
            val key = evalExpr(keyExpr, scope, functions)

            if (target is Map<*, *>) return target[key]
            if (target is List<*> && key is Number) {
                val idx = key.toInt()
                val realIdx = if (idx < 0) target.size + idx else idx
                if (realIdx in target.indices) return target[realIdx]
            }
            if (target is String && key is Number) {
                val idx = key.toInt()
                val realIdx = if (idx < 0) target.length + idx else idx
                if (realIdx in target.indices) return target[realIdx].toString()
            }
        }

        // User defined function call: func(args)
        if (expr.contains("(") && expr.endsWith(")")) {
            val funcName = expr.substringBefore("(").trim()
            val argStr = expr.substringAfter("(").removeSuffix(")").trim()
            val pyFunc = functions[funcName]
            if (pyFunc != null) {
                val localScope = scope.toMutableMap()
                val passedArgs = if (argStr.isNotEmpty()) splitArgs(argStr).map { evalExpr(it, scope, functions) } else emptyList()

                pyFunc.params.forEachIndexed { i, p ->
                    if (i < passedArgs.size) {
                        localScope[p] = passedArgs[i]
                    } else if (pyFunc.defaultArgs.containsKey(p)) {
                        localScope[p] = pyFunc.defaultArgs[p]
                    } else {
                        localScope[p] = null
                    }
                }

                return try {
                    val dummyStdout = StringBuilder()
                    executeBlock(pyFunc.bodyLines, 0, pyFunc.baseIndent, localScope, functions, dummyStdout)
                    null
                } catch (e: PyReturnException) {
                    e.value
                }
            }
        }

        // Variable lookup in scope
        if (scope.containsKey(expr)) {
            return scope[expr]
        }

        return expr
    }

    private fun isTruthy(v: Any?): Boolean {
        return when (v) {
            null -> false
            is Boolean -> v
            is Number -> v.toDouble() != 0.0
            is String -> v.isNotEmpty() && v != "False" && v != "0"
            is Collection<*> -> v.isNotEmpty()
            is Map<*, *> -> v.isNotEmpty()
            else -> true
        }
    }

    private fun compareVals(a: Any?, b: Any?): Int {
        if (a is Number && b is Number) return a.toDouble().compareTo(b.toDouble())
        if (a is String && b is String) return a.compareTo(b)
        return (a?.toString() ?: "").compareTo(b?.toString() ?: "")
    }

    private fun addNumbers(a: Number, b: Number): Any {
        if (a is Double || b is Double || a is Float || b is Float) return a.toDouble() + b.toDouble()
        return a.toLong() + b.toLong()
    }

    private fun subtractNumbers(a: Number, b: Number): Any {
        if (a is Double || b is Double || a is Float || b is Float) return a.toDouble() - b.toDouble()
        return a.toLong() - b.toLong()
    }

    private fun multiplyNumbers(a: Number, b: Number): Any {
        if (a is Double || b is Double || a is Float || b is Float) return a.toDouble() * b.toDouble()
        return a.toLong() * b.toLong()
    }

    private fun divideNumbers(a: Number, b: Number): Double {
        if (b.toDouble() == 0.0) throw ArithmeticException("division by zero")
        return a.toDouble() / b.toDouble()
    }

    private fun modNumbers(a: Number, b: Number): Any {
        if (b.toDouble() == 0.0) throw ArithmeticException("division by zero")
        if (a is Double || b is Double) return a.toDouble() % b.toDouble()
        return a.toLong() % b.toLong()
    }

    private fun findTopLevelOp(expr: String, op: String): Int {
        var inQuotes = false
        var quoteChar = ' '
        var depth = 0
        var i = 0
        while (i <= expr.length - op.length) {
            val c = expr[i]
            if ((c == '"' || c == '\'') && (i == 0 || expr[i - 1] != '\\')) {
                if (!inQuotes) { inQuotes = true; quoteChar = c }
                else if (quoteChar == c) inQuotes = false
            } else if (!inQuotes) {
                if (c == '(' || c == '[' || c == '{') depth++
                else if (c == ')' || c == ']' || c == '}') depth--
                else if (depth == 0 && expr.startsWith(op, i)) {
                    if (op == "+" || op == "-") {
                        if (i == 0 || expr[i - 1] in "=+-*/%<>&|^") {
                            i++
                            continue
                        }
                    }
                    return i
                }
            }
            i++
        }
        return -1
    }

    private fun splitArgs(str: String): List<String> {
        val list = mutableListOf<String>()
        var cur = StringBuilder()
        var depth = 0
        var inQuotes = false
        var quoteChar = ' '

        for (i in str.indices) {
            val c = str[i]
            if ((c == '"' || c == '\'') && (i == 0 || str[i - 1] != '\\')) {
                if (!inQuotes) { inQuotes = true; quoteChar = c }
                else if (quoteChar == c) inQuotes = false
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
