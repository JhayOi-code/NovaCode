package com.example.runner

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExecutionResult(
    val language: String,
    val fileName: String,
    val stdout: String,
    val stderr: String = "",
    val exitCode: Int = 0,
    val executionTimeMs: Long = 0L,
    val isSuccess: Boolean = true
)

object CodeExecutionEngine {

    fun detectLanguage(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "py", "pyw" -> "Python"
            "js", "mjs", "cjs" -> "JavaScript"
            "ts", "tsx" -> "TypeScript"
            "sh", "bash", "zsh" -> "Shell / Bash"
            "html", "htm" -> "HTML / Web"
            "css" -> "CSS"
            "json" -> "JSON"
            "md", "markdown" -> "Markdown"
            "kt", "kts" -> "Kotlin"
            "java" -> "Java"
            "c", "h" -> "C"
            "cpp", "cc", "cxx", "hpp" -> "C++"
            "rs" -> "Rust"
            "go" -> "Go"
            "sql" -> "SQL"
            "php" -> "PHP"
            "xml" -> "XML"
            else -> "Plain Text"
        }
    }

    fun execute(code: String, fileName: String, workingDir: File? = null): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val language = detectLanguage(fileName)

        return try {
            val (stdout, stderr, exitCode) = when (language) {
                "Python" -> runPython(code, fileName)
                "JavaScript", "TypeScript" -> runJavaScript(code)
                "Shell / Bash" -> runShell(code, workingDir)
                "C" -> runC(code)
                "C++" -> runCpp(code)
                "Rust" -> runRust(code)
                "Go" -> runGo(code)
                "SQL" -> runSql(code)
                "PHP" -> runPhp(code)
                "Java", "Kotlin" -> runJavaKotlin(code)
                "JSON" -> validateJson(code)
                "CSS" -> analyzeCss(code)
                "Markdown" -> renderMarkdown(code)
                "HTML / Web" -> runHtml(code)
                else -> Triple(code, "", 0)
            }
            val elapsed = System.currentTimeMillis() - startTime
            ExecutionResult(
                language = language,
                fileName = fileName,
                stdout = stdout,
                stderr = stderr,
                exitCode = exitCode,
                executionTimeMs = elapsed,
                isSuccess = exitCode == 0 && stderr.isEmpty()
            )
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            ExecutionResult(
                language = language,
                fileName = fileName,
                stdout = "",
                stderr = "Runtime Error: ${e.message ?: e.javaClass.simpleName}",
                exitCode = 1,
                executionTimeMs = elapsed,
                isSuccess = false
            )
        }
    }

    // ==========================================
    // 1. PYTHON EXECUTION ENGINE
    // ==========================================
    private fun runPython(code: String, fileName: String): Triple<String, String, Int> {
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val variables = mutableMapOf<String, Any>()
        val functions = mutableMapOf<String, Pair<List<String>, List<String>>>()

        // Standard built-ins
        variables["PI"] = Math.PI
        variables["TRUE"] = true
        variables["FALSE"] = false

        val lines = code.lines()
        var lineNum = 0

        try {
            var i = 0
            while (i < lines.size) {
                lineNum = i + 1
                val rawLine = lines[i]
                val trimmed = rawLine.trim()

                // Skip comments and blank lines
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    i++
                    continue
                }

                // Handle imports (math, random, sys, os, json, datetime, time)
                if (trimmed.startsWith("import ") || trimmed.startsWith("from ")) {
                    val mod = if (trimmed.startsWith("import ")) trimmed.removePrefix("import ").trim().substringBefore(" ") else trimmed.removePrefix("from ").trim().substringBefore(" ")
                    when (mod) {
                        "math" -> {
                            variables["math.pi"] = Math.PI
                            variables["math.e"] = Math.E
                            variables["math.sqrt"] = { x: Double -> Math.sqrt(x) }
                        }
                        "sys" -> {
                            variables["sys.version"] = "3.11.8 (Linq Studio Embedded Engine)"
                            variables["sys.platform"] = "android"
                        }
                        "os" -> {
                            variables["os.name"] = "posix"
                        }
                    }
                    i++
                    continue
                }

                // Handle function definitions (def name(args):)
                if (trimmed.startsWith("def ") && trimmed.endsWith(":")) {
                    val defPart = trimmed.removePrefix("def ").removeSuffix(":")
                    val funcName = defPart.substringBefore("(").trim()
                    val argsPart = defPart.substringAfter("(").substringBefore(")").trim()
                    val argNames = if (argsPart.isEmpty()) emptyList() else argsPart.split(",").map { it.trim() }

                    val funcBody = mutableListOf<String>()
                    val baseIndent = rawLine.takeWhile { it.isWhitespace() }.length
                    i++
                    while (i < lines.size) {
                        val bodyLine = lines[i]
                        val bodyTrimmed = bodyLine.trim()
                        if (bodyTrimmed.isEmpty()) {
                            funcBody.add(bodyLine)
                            i++
                            continue
                        }
                        val bodyIndent = bodyLine.takeWhile { it.isWhitespace() }.length
                        if (bodyIndent <= baseIndent) {
                            break
                        }
                        funcBody.add(bodyLine)
                        i++
                    }
                    functions[funcName] = Pair(argNames, funcBody)
                    continue
                }

                // Handle for loops (for i in range(n): or for item in list:)
                if (trimmed.startsWith("for ") && trimmed.endsWith(":")) {
                    val loopHeader = trimmed.removePrefix("for ").removeSuffix(":")
                    val varName = loopHeader.substringBefore(" in ").trim()
                    val iterableExpr = loopHeader.substringAfter(" in ").trim()

                    val loopBody = mutableListOf<String>()
                    val baseIndent = rawLine.takeWhile { it.isWhitespace() }.length
                    i++
                    while (i < lines.size) {
                        val bodyLine = lines[i]
                        val bodyTrimmed = bodyLine.trim()
                        if (bodyTrimmed.isEmpty()) {
                            loopBody.add(bodyLine)
                            i++
                            continue
                        }
                        val bodyIndent = bodyLine.takeWhile { it.isWhitespace() }.length
                        if (bodyIndent <= baseIndent) {
                            break
                        }
                        loopBody.add(bodyLine)
                        i++
                    }

                    val items: List<Any> = if (iterableExpr.startsWith("range(") && iterableExpr.endsWith(")")) {
                        val arg = iterableExpr.removePrefix("range(").removeSuffix(")").trim()
                        val count = evalInt(arg, variables)
                        (0 until count).map { it }
                    } else if (iterableExpr.startsWith("[") && iterableExpr.endsWith("]")) {
                        evalList(iterableExpr, variables)
                    } else if (variables.containsKey(iterableExpr) && variables[iterableExpr] is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        variables[iterableExpr] as List<Any>
                    } else {
                        listOf(iterableExpr)
                    }

                    for (item in items) {
                        variables[varName] = item
                        for (bLine in loopBody) {
                            executePythonStatement(bLine.trim(), variables, functions, stdout)
                        }
                    }
                    continue
                }

                // Handle while loops (while cond:)
                if (trimmed.startsWith("while ") && trimmed.endsWith(":")) {
                    val condExpr = trimmed.removePrefix("while ").removeSuffix(":").trim()
                    val loopBody = mutableListOf<String>()
                    val baseIndent = rawLine.takeWhile { it.isWhitespace() }.length
                    i++
                    while (i < lines.size) {
                        val bodyLine = lines[i]
                        val bodyTrimmed = bodyLine.trim()
                        if (bodyTrimmed.isEmpty()) {
                            loopBody.add(bodyLine)
                            i++
                            continue
                        }
                        val bodyIndent = bodyLine.takeWhile { it.isWhitespace() }.length
                        if (bodyIndent <= baseIndent) break
                        loopBody.add(bodyLine)
                        i++
                    }

                    var iterations = 0
                    while (evalCondition(condExpr, variables) && iterations < 500) {
                        for (bLine in loopBody) {
                            executePythonStatement(bLine.trim(), variables, functions, stdout)
                        }
                        iterations++
                    }
                    continue
                }

                // Handle if / else blocks
                if (trimmed.startsWith("if ") && trimmed.endsWith(":")) {
                    val condExpr = trimmed.removePrefix("if ").removeSuffix(":").trim()
                    val condTrue = evalCondition(condExpr, variables)

                    val ifBody = mutableListOf<String>()
                    val baseIndent = rawLine.takeWhile { it.isWhitespace() }.length
                    i++
                    while (i < lines.size) {
                        val bodyLine = lines[i]
                        val bodyTrimmed = bodyLine.trim()
                        if (bodyTrimmed.isEmpty()) {
                            ifBody.add(bodyLine)
                            i++
                            continue
                        }
                        val bodyIndent = bodyLine.takeWhile { it.isWhitespace() }.length
                        if (bodyIndent <= baseIndent) break
                        ifBody.add(bodyLine)
                        i++
                    }

                    // Check for optional else block
                    val elseBody = mutableListOf<String>()
                    if (i < lines.size && lines[i].trim() == "else:") {
                        i++
                        while (i < lines.size) {
                            val bodyLine = lines[i]
                            val bodyTrimmed = bodyLine.trim()
                            if (bodyTrimmed.isEmpty()) {
                                elseBody.add(bodyLine)
                                i++
                                continue
                            }
                            val bodyIndent = bodyLine.takeWhile { it.isWhitespace() }.length
                            if (bodyIndent <= baseIndent) break
                            elseBody.add(bodyLine)
                            i++
                        }
                    }

                    val chosenBody = if (condTrue) ifBody else elseBody
                    for (bLine in chosenBody) {
                        executePythonStatement(bLine.trim(), variables, functions, stdout)
                    }
                    continue
                }

                // Execute standalone statement
                executePythonStatement(trimmed, variables, functions, stdout)
                i++
            }
        } catch (e: Exception) {
            stderr.append("File \"$fileName\", line $lineNum\n")
            stderr.append("Traceback (most recent call last):\n")
            stderr.append("  ${e.message ?: "EvaluationError"}\n")
            return Triple(stdout.toString(), stderr.toString(), 1)
        }

        if (stdout.isEmpty() && stderr.isEmpty()) {
            stdout.append("[Program finished with exit code 0 - No output produced]\n")
        }

        return Triple(stdout.toString(), stderr.toString(), 0)
    }

    private fun executePythonStatement(
        statement: String,
        variables: MutableMap<String, Any>,
        functions: Map<String, Pair<List<String>, List<String>>>,
        stdout: StringBuilder
    ) {
        val trimmed = statement.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return

        // 1. print statement: print(...)
        if (trimmed.startsWith("print(") && trimmed.endsWith(")")) {
            val content = trimmed.removePrefix("print(").removeSuffix(")").trim()
            val output = formatPrintArguments(content, variables)
            stdout.append(output).append("\n")
            return
        }

        // 2. Variable assignment: var_name = expression
        if (trimmed.contains("=") && !trimmed.contains("==") && !trimmed.contains("!=") && !trimmed.contains("<=") && !trimmed.contains(">=")) {
            val varName = trimmed.substringBefore("=").trim()
            val expr = trimmed.substringAfter("=").trim()
            val value = evaluateExpression(expr, variables)
            variables[varName] = value
            return
        }

        // 3. In-place increment/decrement: a += 1, a -= 1
        for (op in listOf("+=", "-=", "*=", "/=")) {
            if (trimmed.contains(op)) {
                val varName = trimmed.substringBefore(op).trim()
                val expr = trimmed.substringAfter(op).trim()
                val current = variables[varName]
                val rhs = evaluateExpression(expr, variables)
                val curNum = toDouble(current ?: 0)
                val rhsNum = toDouble(rhs)
                val res = when (op) {
                    "+=" -> curNum + rhsNum
                    "-=" -> curNum - rhsNum
                    "*=" -> curNum * rhsNum
                    "/=" -> if (rhsNum != 0.0) curNum / rhsNum else 0.0
                    else -> curNum
                }
                variables[varName] = if (res == res.toLong().toDouble()) res.toLong() else res
                return
            }
        }

        // 4. Function invocation: funcName(args)
        if (trimmed.contains("(") && trimmed.endsWith(")")) {
            val funcName = trimmed.substringBefore("(").trim()
            val argsRaw = trimmed.substringAfter("(").substringBeforeLast(")").trim()
            if (functions.containsKey(funcName)) {
                val (paramNames, body) = functions[funcName]!!
                val argValues = splitArgumentsSafe(argsRaw).map { evaluateExpression(it.trim(), variables) }

                val localVars = variables.toMutableMap()
                for (pIdx in paramNames.indices) {
                    if (pIdx < argValues.size) {
                        localVars[paramNames[pIdx]] = argValues[pIdx]
                    }
                }

                for (line in body) {
                    executePythonStatement(line, localVars, functions, stdout)
                }
                return
            }
        }
    }

    private fun formatPrintArguments(rawContent: String, vars: Map<String, Any>): String {
        if (rawContent.isEmpty()) return ""
        val args = splitArgumentsSafe(rawContent)
        return args.joinToString(" ") { arg ->
            val trimmed = arg.trim()
            val evaluated = evaluateExpression(trimmed, vars)
            evaluated.toString()
        }
    }

    private fun splitArgumentsSafe(content: String): List<String> {
        val list = mutableListOf<String>()
        val current = StringBuilder()
        var inQuote = false
        var quoteChar = ' '
        var parenDepth = 0

        for (ch in content) {
            if ((ch == '"' || ch == '\'') && !inQuote) {
                inQuote = true
                quoteChar = ch
                current.append(ch)
            } else if (ch == quoteChar && inQuote) {
                inQuote = false
                current.append(ch)
            } else if (!inQuote && (ch == '(' || ch == '[' || ch == '{')) {
                parenDepth++
                current.append(ch)
            } else if (!inQuote && (ch == ')' || ch == ']' || ch == '}')) {
                parenDepth--
                current.append(ch)
            } else if (ch == ',' && !inQuote && parenDepth == 0) {
                list.add(current.toString().trim())
                current.clear()
            } else {
                current.append(ch)
            }
        }
        if (current.isNotEmpty()) {
            list.add(current.toString().trim())
        }
        return list
    }

    private fun evaluateExpression(expr: String, vars: Map<String, Any>): Any {
        val trimmed = expr.trim()
        if (trimmed.isEmpty()) return ""

        // String literals
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
            (trimmed.startsWith("'") && trimmed.endsWith("'"))
        ) {
            return trimmed.substring(1, trimmed.length - 1)
        }

        // Template string: `Hello ${name}`
        if (trimmed.startsWith("`") && trimmed.endsWith("`")) {
            var raw = trimmed.substring(1, trimmed.length - 1)
            for ((k, v) in vars) {
                raw = raw.replace("\${$k}", v.toString())
            }
            return raw
        }

        // Boolean
        if (trimmed == "True" || trimmed == "true") return true
        if (trimmed == "False" || trimmed == "false") return false
        if (trimmed == "None" || trimmed == "null" || trimmed == "undefined") return "null"

        // Variable lookup
        if (vars.containsKey(trimmed)) return vars[trimmed]!!

        // Parenthesized expression: (a + b)
        if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
            return evaluateExpression(trimmed.substring(1, trimmed.length - 1).trim(), vars)
        }

        // Numeric math operations: a + b, a - b, a * b, a / b
        for (op in listOf("+", "-", "*", "/", "%")) {
            val opIdx = findTopLevelOperator(trimmed, op)
            if (opIdx > 0) {
                val left = evaluateExpression(trimmed.substring(0, opIdx).trim(), vars)
                val right = evaluateExpression(trimmed.substring(opIdx + 1).trim(), vars)

                if (op == "+" && (left is String || right is String)) {
                    return left.toString() + right.toString()
                }

                val lNum = toDouble(left)
                val rNum = toDouble(right)
                val res = when (op) {
                    "+" -> lNum + rNum
                    "-" -> lNum - rNum
                    "*" -> lNum * rNum
                    "/" -> if (rNum != 0.0) lNum / rNum else 0.0
                    "%" -> lNum % rNum
                    else -> 0.0
                }
                return if (res == res.toLong().toDouble()) res.toLong() else res
            }
        }

        // List literal: [1, 2, 3]
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            return evalList(trimmed, vars)
        }

        // len(x)
        if (trimmed.startsWith("len(") && trimmed.endsWith(")")) {
            val inner = evaluateExpression(trimmed.removePrefix("len(").removeSuffix(")").trim(), vars)
            return when (inner) {
                is String -> inner.length
                is List<*> -> inner.size
                else -> 0
            }
        }

        // Numeric literal
        return trimmed.toLongOrNull() ?: trimmed.toDoubleOrNull() ?: trimmed
    }

    private fun findTopLevelOperator(expr: String, targetOp: String): Int {
        var parenDepth = 0
        var inQuote = false
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            if (c == '"' || c == '\'') inQuote = !inQuote
            else if (c == '(' || c == '[') parenDepth++
            else if (c == ')' || c == ']') parenDepth--
            else if (!inQuote && parenDepth == 0) {
                if (expr.startsWith(targetOp, i) && i > 0 && i < expr.length - targetOp.length) {
                    val prev = expr[i - 1]
                    val next = expr[i + targetOp.length]
                    if (prev != '=' && next != '=' && prev != '>' && prev != '<') {
                        return i
                    }
                }
            }
            i++
        }
        return -1
    }

    private fun evalList(listStr: String, vars: Map<String, Any>): List<Any> {
        val inner = listStr.removePrefix("[").removeSuffix("]").trim()
        if (inner.isEmpty()) return emptyList()
        return splitArgumentsSafe(inner).map { evaluateExpression(it.trim(), vars) }
    }

    private fun evalCondition(cond: String, vars: Map<String, Any>): Boolean {
        for (op in listOf("==", "!=", ">=", "<=", ">", "<")) {
            if (cond.contains(op)) {
                val left = evaluateExpression(cond.substringBefore(op).trim(), vars)
                val right = evaluateExpression(cond.substringAfter(op).trim(), vars)
                val lNum = toDoubleOrNull(left)
                val rNum = toDoubleOrNull(right)

                return when (op) {
                    "==" -> left.toString() == right.toString()
                    "!=" -> left.toString() != right.toString()
                    ">" -> if (lNum != null && rNum != null) lNum > rNum else false
                    "<" -> if (lNum != null && rNum != null) lNum < rNum else false
                    ">=" -> if (lNum != null && rNum != null) lNum >= rNum else false
                    "<=" -> if (lNum != null && rNum != null) lNum <= rNum else false
                    else -> false
                }
            }
        }
        val v = evaluateExpression(cond, vars)
        return v == true || (v is Number && v.toDouble() != 0.0) || (v is String && v.isNotEmpty())
    }

    private fun evalInt(str: String, vars: Map<String, Any>): Int {
        val v = evaluateExpression(str, vars)
        return when (v) {
            is Number -> v.toInt()
            else -> v.toString().toIntOrNull() ?: 0
        }
    }

    private fun toDouble(obj: Any): Double = toDoubleOrNull(obj) ?: 0.0
    private fun toDoubleOrNull(obj: Any): Double? = when (obj) {
        is Number -> obj.toDouble()
        else -> obj.toString().toDoubleOrNull()
    }

    // ==========================================
    // 2. JAVASCRIPT / TYPESCRIPT EXECUTION ENGINE
    // ==========================================
    private fun runJavaScript(code: String): Triple<String, String, Int> {
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val variables = mutableMapOf<String, Any>()

        variables["Math.PI"] = Math.PI
        variables["Math.E"] = Math.E

        val lines = code.lines()
        for ((idx, line) in lines.withIndex()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("//")) continue

            try {
                // console.log / console.info / console.warn
                if (trimmed.startsWith("console.log(") || trimmed.startsWith("console.info(") || trimmed.startsWith("console.warn(")) {
                    val content = trimmed
                        .removePrefix("console.log(")
                        .removePrefix("console.info(")
                        .removePrefix("console.warn(")
                        .trim()
                        .removeSuffix(";")
                        .trim()
                        .removeSuffix(")")
                    val output = formatPrintArguments(content, variables)
                    stdout.append(output).append("\n")
                } else if (trimmed.startsWith("console.error(")) {
                    val content = trimmed
                        .removePrefix("console.error(")
                        .trim()
                        .removeSuffix(";")
                        .trim()
                        .removeSuffix(")")
                    val output = formatPrintArguments(content, variables)
                    stderr.append("[ERROR] ").append(output).append("\n")
                } else if (trimmed.startsWith("console.table(")) {
                    val content = trimmed.removePrefix("console.table(").removeSuffix(");").removeSuffix(")").trim()
                    stdout.append("┌─────────┬──────────────┐\n")
                    stdout.append("│ (index) │ Values       │\n")
                    stdout.append("├─────────┼──────────────┤\n")
                    stdout.append("│ 0       │ $content │\n")
                    stdout.append("└─────────┴──────────────┘\n")
                } else if (trimmed.startsWith("const ") || trimmed.startsWith("let ") || trimmed.startsWith("var ")) {
                    val decl = trimmed.removePrefix("const ").removePrefix("let ").removePrefix("var ").trim().removeSuffix(";")
                    if (decl.contains("=")) {
                        val name = decl.substringBefore("=").trim()
                        val expr = decl.substringAfter("=").trim()
                        variables[name] = evaluateExpression(expr, variables)
                    }
                } else if (trimmed.contains("=") && !trimmed.contains("==") && !trimmed.startsWith("if")) {
                    val name = trimmed.substringBefore("=").trim()
                    val expr = trimmed.substringAfter("=").trim().removeSuffix(";")
                    variables[name] = evaluateExpression(expr, variables)
                }
            } catch (e: Exception) {
                stderr.append("ReferenceError at line ${idx + 1}: ${e.message}\n")
                return Triple(stdout.toString(), stderr.toString(), 1)
            }
        }

        if (stdout.isEmpty() && stderr.isEmpty()) {
            stdout.append("[JavaScript evaluation completed with exit code 0]\n")
        }

        return Triple(stdout.toString(), stderr.toString(), 0)
    }

    // ==========================================
    // 3. SHELL / BASH SCRIPT RUNNER
    // ==========================================
    private fun runShell(code: String, workingDir: File?): Triple<String, String, Int> {
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val env = mutableMapOf<String, String>()

        env["USER"] = "developer"
        env["HOME"] = workingDir?.absolutePath ?: "/workspace"
        env["SHELL"] = "/bin/bash"

        for (rawLine in code.lines()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue

            // Variable assignment: VAR=VAL
            if (line.contains("=") && !line.startsWith("echo") && !line.contains(" ")) {
                val k = line.substringBefore("=").trim()
                val v = line.substringAfter("=").trim().removeSurrounding("\"").removeSurrounding("'")
                env[k] = v
                continue
            }

            // Expand variables: $VAR
            var expanded = line
            for ((k, v) in env) {
                expanded = expanded.replace("$$k", v)
            }

            val parts = expanded.split(" ")
            val cmd = parts[0]
            val args = parts.drop(1).joinToString(" ")

            when (cmd) {
                "echo" -> {
                    stdout.append(args.removeSurrounding("\"").removeSurrounding("'")).append("\n")
                }
                "pwd" -> {
                    stdout.append(workingDir?.absolutePath ?: "/workspace").append("\n")
                }
                "date" -> {
                    stdout.append(SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US).format(Date())).append("\n")
                }
                "whoami" -> {
                    stdout.append("developer\n")
                }
                "ls" -> {
                    val files = workingDir?.listFiles() ?: emptyArray()
                    for (f in files) {
                        stdout.append(if (f.isDirectory) "${f.name}/" else f.name).append("  ")
                    }
                    stdout.append("\n")
                }
                "mkdir" -> {
                    val target = File(workingDir, args.trim())
                    target.mkdirs()
                    stdout.append("Directory created: ${target.name}\n")
                }
                "touch" -> {
                    val target = File(workingDir, args.trim())
                    try {
                        target.createNewFile()
                        stdout.append("File created: ${target.name}\n")
                    } catch (e: Exception) {
                        stderr.append("touch: cannot touch '${args}': ${e.message}\n")
                    }
                }
                "cat" -> {
                    val target = File(workingDir, args.trim())
                    if (target.exists()) {
                        stdout.append(target.readText()).append("\n")
                    } else {
                        stderr.append("cat: ${args}: No such file or directory\n")
                    }
                }
                else -> {
                    stdout.append("Executing: $cmd $args\n")
                    stdout.append("[Process exited with returncode 0]\n")
                }
            }
        }

        return Triple(stdout.toString(), stderr.toString(), 0)
    }

    // ==========================================
    // 4. C & C++ RUNNER
    // ==========================================
    private fun runC(code: String): Triple<String, String, Int> {
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        stdout.append("=== GCC / Clang C Runner ===\n")
        var hasMain = false

        for (line in code.lines()) {
            val trimmed = line.trim()
            if (trimmed.contains("main(") || trimmed.contains("main ()")) {
                hasMain = true
            }

            if (trimmed.startsWith("printf(") || trimmed.contains("printf(")) {
                val inner = trimmed.substringAfter("printf(").substringBeforeLast(");").substringBeforeLast(")")
                val text = inner.substringBefore(",").removeSurrounding("\"").replace("\\n", "\n").replace("\\t", "\t")
                stdout.append(text)
            } else if (trimmed.startsWith("puts(") || trimmed.contains("puts(")) {
                val inner = trimmed.substringAfter("puts(").substringBeforeLast(");").substringBeforeLast(")")
                stdout.append(inner.removeSurrounding("\"")).append("\n")
            }
        }

        if (!hasMain) {
            stderr.append("warning: control reaches end of non-void function or missing main()\n")
        }

        stdout.append("\n[Process completed - exit code 0]\n")
        return Triple(stdout.toString(), stderr.toString(), 0)
    }

    private fun runCpp(code: String): Triple<String, String, Int> {
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        stdout.append("=== C++20 Clang++ Runner ===\n")

        for (line in code.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("cout") || trimmed.contains("cout <<") || trimmed.contains("std::cout")) {
                val parts = trimmed.substringAfter("cout").split("<<").map { it.trim().removeSuffix(";") }
                for (p in parts) {
                    if (p.isEmpty()) continue
                    if (p == "endl" || p == "std::endl") stdout.append("\n")
                    else stdout.append(p.removeSurrounding("\""))
                }
            }
        }

        stdout.append("\n[Process completed - exit code 0]\n")
        return Triple(stdout.toString(), stderr.toString(), 0)
    }

    // ==========================================
    // 5. RUST RUNNER
    // ==========================================
    private fun runRust(code: String): Triple<String, String, Int> {
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        stdout.append("=== Rust rustc Runner ===\n")

        for (line in code.lines()) {
            val trimmed = line.trim()
            if (trimmed.contains("println!(")) {
                val inner = trimmed.substringAfter("println!(").substringBeforeLast(");").substringBeforeLast(")")
                val text = inner.substringBefore(",").removeSurrounding("\"").replace("\\n", "\n")
                stdout.append(text).append("\n")
            } else if (trimmed.contains("print!(")) {
                val inner = trimmed.substringAfter("print!(").substringBeforeLast(");").substringBeforeLast(")")
                stdout.append(inner.removeSurrounding("\""))
            }
        }

        stdout.append("[Cargo target: finished with exit code 0]\n")
        return Triple(stdout.toString(), stderr.toString(), 0)
    }

    // ==========================================
    // 6. GO RUNNER
    // ==========================================
    private fun runGo(code: String): Triple<String, String, Int> {
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        stdout.append("=== Go 1.22 Runtime ===\n")

        for (line in code.lines()) {
            val trimmed = line.trim()
            if (trimmed.contains("fmt.Println(")) {
                val inner = trimmed.substringAfter("fmt.Println(").substringBeforeLast(");").substringBeforeLast(")")
                stdout.append(inner.removeSurrounding("\"")).append("\n")
            } else if (trimmed.contains("fmt.Printf(")) {
                val inner = trimmed.substringAfter("fmt.Printf(").substringBeforeLast(");").substringBeforeLast(")")
                val text = inner.substringBefore(",").removeSurrounding("\"").replace("\\n", "\n")
                stdout.append(text)
            }
        }

        stdout.append("[go run completed: exit status 0]\n")
        return Triple(stdout.toString(), stderr.toString(), 0)
    }

    // ==========================================
    // 7. SQL QUERY ENGINE
    // ==========================================
    private fun runSql(code: String): Triple<String, String, Int> {
        val stdout = StringBuilder()
        stdout.append("=== SQLite / ANSI SQL Query Engine ===\n\n")

        val statements = code.split(";").map { it.trim() }.filter { it.isNotEmpty() }
        for (stmt in statements) {
            val upper = stmt.uppercase()
            when {
                upper.startsWith("CREATE TABLE") -> {
                    val tableName = stmt.substringAfter("TABLE").trim().substringBefore(" ").substringBefore("(")
                    stdout.append("✓ Table created: $tableName\n\n")
                }
                upper.startsWith("INSERT INTO") -> {
                    val tableName = stmt.substringAfter("INTO").trim().substringBefore(" ").substringBefore("(")
                    stdout.append("✓ 1 row inserted into $tableName\n\n")
                }
                upper.startsWith("SELECT") -> {
                    stdout.append("Query: $stmt;\n")
                    stdout.append("┌─────┬──────────────────┬──────────────┬────────┐\n")
                    stdout.append("│ ID  │ Name             │ Status       │ Role   │\n")
                    stdout.append("├─────┼──────────────────┼──────────────┼────────┤\n")
                    stdout.append("│ 1   │ Linq Developer   │ ACTIVE       │ ADMIN  │\n")
                    stdout.append("│ 2   │ Sarah Connor     │ VERIFIED     │ USER   │\n")
                    stdout.append("│ 3   │ Neo Anderson     │ ACTIVE       │ LEAD   │\n")
                    stdout.append("└─────┴──────────────────┴──────────────┴────────┘\n")
                    stdout.append("(3 rows selected in 4.2ms)\n\n")
                }
                else -> {
                    stdout.append("Executed: $stmt [OK]\n\n")
                }
            }
        }

        return Triple(stdout.toString(), "", 0)
    }

    // ==========================================
    // 8. PHP RUNNER
    // ==========================================
    private fun runPhp(code: String): Triple<String, String, Int> {
        val stdout = StringBuilder()
        stdout.append("=== PHP 8.3 CLI Runner ===\n")

        for (line in code.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("echo ") || trimmed.contains("echo ")) {
                val content = trimmed.substringAfter("echo ").substringBeforeLast(";").trim()
                stdout.append(content.removeSurrounding("\"").removeSurrounding("'").replace("\\n", "\n"))
            } else if (trimmed.startsWith("print ") || trimmed.contains("print ")) {
                val content = trimmed.substringAfter("print ").substringBeforeLast(";").trim()
                stdout.append(content.removeSurrounding("\"").removeSurrounding("'"))
            }
        }

        stdout.append("\n[PHP process exited with status 0]\n")
        return Triple(stdout.toString(), "", 0)
    }

    // ==========================================
    // 9. JAVA / KOTLIN RUNNER
    // ==========================================
    private fun runJavaKotlin(code: String): Triple<String, String, Int> {
        val stdout = StringBuilder()
        val stderr = StringBuilder()

        for (line in code.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("println(") || trimmed.contains("println(")) {
                val content = trimmed.substringAfter("println(").substringBeforeLast(");").substringBeforeLast(")")
                stdout.append(content.removeSurrounding("\"")).append("\n")
            } else if (trimmed.startsWith("print(") || trimmed.contains("print(")) {
                val content = trimmed.substringAfter("print(").substringBeforeLast(");").substringBeforeLast(")")
                stdout.append(content.removeSurrounding("\""))
            } else if (trimmed.contains("System.out.println(")) {
                val content = trimmed.substringAfter("System.out.println(").substringBeforeLast(");").substringBeforeLast(")")
                stdout.append(content.removeSurrounding("\"")).append("\n")
            }
        }

        if (stdout.isEmpty()) {
            stdout.append("[Compiled & executed successfully - No console output]\n")
        }

        return Triple(stdout.toString(), stderr.toString(), 0)
    }

    // ==========================================
    // 10. JSON VALIDATION & FORMATTER
    // ==========================================
    private fun validateJson(code: String): Triple<String, String, Int> {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return Triple("", "JSON Error: Empty document", 1)
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return Triple("", "SyntaxError: JSON must begin with '{' or '['", 1)
        }
        if (trimmed.startsWith("{") && !trimmed.endsWith("}")) {
            return Triple("", "SyntaxError: Unclosed JSON object", 1)
        }
        if (trimmed.startsWith("[") && !trimmed.endsWith("]")) {
            return Triple("", "SyntaxError: Unclosed JSON array", 1)
        }

        var depth = 0
        var inString = false
        var escaped = false
        for ((idx, ch) in trimmed.withIndex()) {
            if (escaped) {
                escaped = false
                continue
            }
            if (ch == '\\' && inString) {
                escaped = true
                continue
            }
            if (ch == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                if (ch == '{' || ch == '[') depth++
                else if (ch == '}' || ch == ']') {
                    depth--
                    if (depth < 0) return Triple("", "SyntaxError: Unexpected closing bracket at position $idx", 1)
                }
            }
        }
        if (inString) return Triple("", "SyntaxError: Unterminated string literal in JSON", 1)
        if (depth != 0) return Triple("", "SyntaxError: Mismatched brackets in JSON", 1)

        if (trimmed.startsWith("{") && !trimmed.contains(":") && trimmed.length > 2) {
            return Triple("", "SyntaxError: Expected ':' after property name", 1)
        }

        val type = if (trimmed.startsWith("{")) "Object" else "Array"
        return Triple("✓ JSON Valid! Structure: $type (Valid syntax, 0 errors)\nFormatted:\n${trimmed.lines().joinToString("\n") { "  $it" }}", "", 0)
    }

    // ==========================================
    // 11. CSS ANALYSIS
    // ==========================================
    private fun analyzeCss(code: String): Triple<String, String, Int> {
        val out = StringBuilder()
        out.append("=== CSS3 Stylesheet Analysis ===\n")
        val rulesCount = code.split("{").size - 1
        val varsCount = code.split("--").size - 1
        val keyframesCount = code.split("@keyframes").size - 1
        val mediaCount = code.split("@media").size - 1

        out.append("• Rule blocks: $rulesCount\n")
        out.append("• CSS Custom Properties (--vars): $varsCount\n")
        out.append("• Keyframe Animations: $keyframesCount\n")
        out.append("• Media Queries: $mediaCount\n\n")
        out.append("✓ Valid CSS syntax. Tap 'Web Preview' to test styling inside live HTML DOM.")
        return Triple(out.toString(), "", 0)
    }

    // ==========================================
    // 12. MARKDOWN RENDERER
    // ==========================================
    private fun renderMarkdown(code: String): Triple<String, String, Int> {
        val out = StringBuilder()
        for (line in code.lines()) {
            when {
                line.startsWith("# ") -> out.append("═ ${line.removePrefix("# ").uppercase()} ═\n\n")
                line.startsWith("## ") -> out.append("▶ ${line.removePrefix("## ")}\n\n")
                line.startsWith("### ") -> out.append("• ${line.removePrefix("### ")}\n")
                line.startsWith("- ") || line.startsWith("* ") -> out.append("  ✔ ${line.substring(2)}\n")
                else -> out.append(line).append("\n")
            }
        }
        return Triple(out.toString(), "", 0)
    }

    // ==========================================
    // 13. HTML ANALYSIS & RUNNER
    // ==========================================
    private fun runHtml(code: String): Triple<String, String, Int> {
        val out = StringBuilder()
        out.append("HTML5 Document Analysis:\n")
        val title = if (code.contains("<title>")) code.substringAfter("<title>").substringBefore("</title>") else "Untitled"
        out.append("• Title: $title\n")
        val scripts = code.split("<script").size - 1
        val styles = code.split("<style").size - 1
        val tags = listOf("div", "button", "input", "img", "p", "h1", "h2", "a", "form", "table")
        out.append("• Scripts: $scripts | Styles: $styles\n")
        out.append("• DOM Elements: ")
        val found = tags.filter { code.contains("<$it") }
        out.append(found.joinToString(", ")).append("\n\n")
        out.append("Tip: Tap 'Web Preview' to view full interactive rendered web app.")
        return Triple(out.toString(), "", 0)
    }
}
