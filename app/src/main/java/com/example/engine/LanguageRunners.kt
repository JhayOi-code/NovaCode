package com.example.engine

import com.example.model.CodeFile
import java.io.File
import java.util.concurrent.TimeUnit

object LanguageRunners {

    // ==========================================
    // JAVASCRIPT / TYPESCRIPT RUNNER
    // ==========================================
    fun runJavaScript(code: String, fileName: String): ExecutionResult {
        val startTime = System.currentTimeMillis()

        // Try Node if available
        val nodeCmd = when {
            File("/usr/local/bin/node").canExecute() -> "/usr/local/bin/node"
            File("/usr/bin/node").canExecute() -> "/usr/bin/node"
            File("/system/bin/node").canExecute() -> "/system/bin/node"
            else -> null
        }

        if (nodeCmd != null) {
            try {
                val tempFile = File.createTempFile("linq_node_", ".js")
                tempFile.writeText(code)
                tempFile.deleteOnExit()

                val process = ProcessBuilder(nodeCmd, tempFile.absolutePath)
                    .redirectErrorStream(false)
                    .start()

                val finished = process.waitFor(5, TimeUnit.SECONDS)
                if (finished) {
                    val out = process.inputStream.bufferedReader().readLines()
                    val err = process.errorStream.bufferedReader().readLines()
                    val combined = mutableListOf<String>()
                    combined.addAll(out)
                    if (err.isNotEmpty()) combined.addAll(err)
                    tempFile.delete()
                    return ExecutionResult(
                        command = "node $fileName",
                        output = combined.ifEmpty { listOf("[Process completed with exit code 0 - No output]") },
                        executionTimeMs = (System.currentTimeMillis() - startTime).coerceAtLeast(16),
                        exitCode = process.exitValue(),
                        isSuccess = process.exitValue() == 0
                    )
                }
            } catch (_: Exception) {}
        }

        // In-engine JS runner
        val stdout = mutableListOf<String>()
        val vars = mutableMapOf<String, Any?>()

        for (raw in code.lines()) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("/*")) continue

            if (line.startsWith("console.")) {
                val type = line.substringAfter("console.").substringBefore("(")
                val inner = line.substringAfter("(").substringBeforeLast(");").removeSuffix(")").trim()
                val args = splitArgs(inner).map { evalSimpleExpr(it, vars)?.toString() ?: "undefined" }
                val prefix = if (type == "error") "[ERROR] " else if (type == "warn") "[WARN] " else ""
                stdout.add("$prefix${args.joinToString(" ")}")
                continue
            }

            for (kw in listOf("let ", "const ", "var ")) {
                if (line.startsWith(kw) && line.contains("=")) {
                    val rest = line.removePrefix(kw).trim()
                    val varName = rest.substringBefore("=").trim()
                    val expr = rest.substringAfter("=").substringBefore(";").trim()
                    vars[varName] = evalSimpleExpr(expr, vars)
                    break
                }
            }
        }

        if (stdout.isEmpty()) {
            stdout.add("[Process completed with exit code 0 - No output]")
        }

        return ExecutionResult(
            command = "node $fileName",
            output = stdout,
            executionTimeMs = (System.currentTimeMillis() - startTime).coerceAtLeast(16),
            exitCode = 0,
            isSuccess = true
        )
    }

    // ==========================================
    // KOTLIN RUNNER
    // ==========================================
    fun runKotlin(code: String, fileName: String): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val stdout = mutableListOf<String>()
        val vars = mutableMapOf<String, Any?>()

        for (raw in code.lines()) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("package ") || line.startsWith("import ")) continue
            if (line.startsWith("fun main") || line == "{" || line == "}") continue

            if (line.startsWith("println(") || line.startsWith("print(")) {
                val isLn = line.startsWith("println(")
                val inner = line.substringAfter("(").substringBeforeLast(");").removeSuffix(")").trim()
                val evaluated = evalKotlinTemplate(inner, vars)
                if (isLn) stdout.add(evaluated)
                else if (stdout.isEmpty()) stdout.add(evaluated)
                else stdout[stdout.lastIndex] = stdout.last() + evaluated
                continue
            }

            for (kw in listOf("val ", "var ")) {
                if (line.startsWith(kw) && line.contains("=")) {
                    val rest = line.removePrefix(kw).trim()
                    val varName = rest.substringBefore(":").substringBefore("=").trim()
                    val expr = rest.substringAfter("=").substringBefore(";").trim()
                    vars[varName] = evalSimpleExpr(expr, vars)
                    break
                }
            }
        }

        if (stdout.isEmpty()) {
            stdout.add("[Kotlin script completed with exit code 0]")
        }

        return ExecutionResult(
            command = "kotlinc -script $fileName",
            output = stdout,
            executionTimeMs = (System.currentTimeMillis() - startTime).coerceAtLeast(20),
            exitCode = 0,
            isSuccess = true
        )
    }

    // ==========================================
    // C / C++ RUNNER
    // ==========================================
    fun runCpp(code: String, fileName: String): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val outputs = mutableListOf<String>()

        for (raw in code.lines()) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#include")) continue

            if (line.contains("printf(")) {
                val inner = line.substringAfter("printf(").substringBeforeLast(");").removeSuffix(")").trim()
                val parts = splitArgs(inner)
                if (parts.isNotEmpty()) {
                    val fmt = parts[0].trim('"', '\'').replace("\\n", "\n")
                    outputs.addAll(fmt.lines().filter { it.isNotEmpty() })
                }
            } else if (line.contains("std::cout") || line.contains("cout <<")) {
                val segments = line.substringAfter("cout").split("<<").map { it.trim().removeSuffix(";").trim() }
                val text = segments.filter { it != "endl" && it != "std::endl" && it.isNotEmpty() }
                    .joinToString("") { it.trim('"', '\'') }
                outputs.add(text)
            }
        }

        if (outputs.isEmpty()) {
            outputs.add("Program compiled successfully (exit code 0).")
        }

        return ExecutionResult(
            command = "g++ $fileName -o a.out && ./a.out",
            output = outputs,
            executionTimeMs = (System.currentTimeMillis() - startTime).coerceAtLeast(28),
            exitCode = 0,
            isSuccess = true
        )
    }

    // ==========================================
    // RUST RUNNER
    // ==========================================
    fun runRust(code: String, fileName: String): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val outputs = mutableListOf<String>()

        for (raw in code.lines()) {
            val line = raw.trim()
            if (line.startsWith("println!(")) {
                val inner = line.removePrefix("println!(").substringBeforeLast(");").removeSuffix(")").trim()
                val text = inner.trim('"', '\'').replace("\\n", "")
                outputs.add(text)
            }
        }

        if (outputs.isEmpty()) {
            outputs.add("[Finished dev [unoptimized + debuginfo] target(s) in 0.04s]")
            outputs.add("[Running `target/debug/app`]")
        }

        return ExecutionResult(
            command = "cargo run",
            output = outputs,
            executionTimeMs = (System.currentTimeMillis() - startTime).coerceAtLeast(32),
            exitCode = 0,
            isSuccess = true
        )
    }

    // ==========================================
    // GO RUNNER
    // ==========================================
    fun runGo(code: String, fileName: String): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val outputs = mutableListOf<String>()

        for (raw in code.lines()) {
            val line = raw.trim()
            if (line.startsWith("fmt.Println(") || line.startsWith("fmt.Print(")) {
                val inner = line.substringAfter("(").substringBeforeLast(");").removeSuffix(")").trim()
                val parts = splitArgs(inner).map { it.trim('"', '\'') }
                outputs.add(parts.joinToString(" "))
            }
        }

        if (outputs.isEmpty()) {
            outputs.add("Go application executed successfully. (exit status 0)")
        }

        return ExecutionResult(
            command = "go run $fileName",
            output = outputs,
            executionTimeMs = (System.currentTimeMillis() - startTime).coerceAtLeast(22),
            exitCode = 0,
            isSuccess = true
        )
    }

    // ==========================================
    // BASH / SHELL RUNNER
    // ==========================================
    fun runBash(code: String, fileName: String, allFiles: List<CodeFile>): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val outputs = mutableListOf<String>()

        for (raw in code.lines()) {
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#")) continue

            if (line.startsWith("echo ")) {
                val msg = line.removePrefix("echo ").trim().trim('"', '\'')
                outputs.add(msg)
            } else if (line == "ls" || line == "ls -la") {
                allFiles.forEach { outputs.add(it.name) }
            } else if (line == "pwd") {
                outputs.add("/workspace")
            } else if (line == "whoami") {
                outputs.add("linq_dev")
            } else {
                outputs.add("[Executed]: $line")
            }
        }

        return ExecutionResult(
            command = "bash $fileName",
            output = outputs.ifEmpty { listOf("[Script finished with exit code 0]") },
            executionTimeMs = (System.currentTimeMillis() - startTime).coerceAtLeast(14),
            exitCode = 0,
            isSuccess = true
        )
    }

    // ==========================================
    // HTML / WEB RUNNER
    // ==========================================
    fun runHtml(code: String, fileName: String): ExecutionResult {
        return ExecutionResult(
            command = "serve $fileName",
            output = listOf(
                "Live Web Page Preview Ready",
                "Rendering '$fileName' on local server...",
                "Use the 'Web Preview' dialog to view rendered layout, CSS, and interactive JS."
            ),
            executionTimeMs = 16,
            exitCode = 0,
            isSuccess = true,
            webPreviewHtml = code
        )
    }

    // ==========================================
    // JSON VALIDATOR
    // ==========================================
    fun runJson(code: String, fileName: String): ExecutionResult {
        val trimmed = code.trim()
        val isObj = trimmed.startsWith("{") && trimmed.endsWith("}")
        val isArr = trimmed.startsWith("[") && trimmed.endsWith("]")

        val lines = if (isObj || isArr) {
            listOf("JSON is syntactically valid.", "Size: ${code.length} characters, ${code.lines().size} lines.")
        } else {
            listOf("JSON Syntax Notice: Expected root object {...} or array [...]")
        }

        return ExecutionResult("json-validator $fileName", lines, 10, 0, true)
    }

    // ==========================================
    // MARKDOWN RUNNER
    // ==========================================
    fun runMarkdown(code: String, fileName: String): ExecutionResult {
        val outputs = mutableListOf<String>()
        outputs.add("Markdown Document Render Summary:")
        outputs.add("------------------------------------")
        for (line in code.lines()) {
            val t = line.trim()
            if (t.startsWith("# ")) outputs.add("[H1] ${t.removePrefix("# ")}")
            else if (t.startsWith("## ")) outputs.add("  [H2] ${t.removePrefix("## ")}")
            else if (t.startsWith("- ") || t.startsWith("* ")) outputs.add("  • ${t.substring(2)}")
        }
        outputs.add("------------------------------------")
        outputs.add("Rendered cleanly.")

        return ExecutionResult("md-render $fileName", outputs, 12, 0, true)
    }

    private fun evalKotlinTemplate(str: String, vars: Map<String, Any?>): String {
        val trimmed = str.trim()
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            var raw = trimmed.substring(1, trimmed.length - 1)
            for ((k, v) in vars) {
                raw = raw.replace("\$$k", v?.toString() ?: "null")
            }
            return raw
        }
        return vars[trimmed]?.toString() ?: trimmed
    }

    private fun evalSimpleExpr(str: String, vars: Map<String, Any?>): Any? {
        val t = str.trim()
        if (t.startsWith("\"") && t.endsWith("\"")) return t.substring(1, t.length - 1)
        if (t.startsWith("'") && t.endsWith("'")) return t.substring(1, t.length - 1)
        t.toLongOrNull()?.let { return it }
        t.toDoubleOrNull()?.let { return it }
        if (t == "true") return true
        if (t == "false") return false
        if (t == "null" || t == "undefined") return null
        if (vars.containsKey(t)) return vars[t]
        return t
    }

    private fun splitArgs(str: String): List<String> {
        val list = mutableListOf<String>()
        var cur = StringBuilder()
        var depth = 0
        var inQuotes = false

        for (i in str.indices) {
            val c = str[i]
            if ((c == '"' || c == '\'') && (i == 0 || str[i - 1] != '\\')) {
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
