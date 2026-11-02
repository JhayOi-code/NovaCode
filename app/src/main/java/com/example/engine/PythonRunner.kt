package com.example.engine

import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.*
import kotlin.random.Random

object PythonRunner {

    fun run(
        code: String,
        fileName: String,
        onChunk: (String) -> Unit = {},
        windowManager: PyWindowManager? = null,
        isCancelled: () -> Boolean = { false }
    ): ExecutionResult {
        val startTime = System.currentTimeMillis()

        // 1. If script uses external system packages or binary without Tkinter/typewriter, try system python
        val needsInEngine = code.contains("tkinter") ||
                code.contains("time.sleep") ||
                code.contains("sys.stdout") ||
                code.contains("turtle")

        if (!needsInEngine) {
            val systemResult = tryRunSystemPython(code, fileName)
            if (systemResult != null) {
                val duration = (System.currentTimeMillis() - startTime).coerceAtLeast(12)
                return systemResult.copy(executionTimeMs = duration)
            }
        }

        // 2. High-fidelity in-engine Python runtime with real-time live streaming & Tkinter support
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

        val execContext = PyContext(
            stdout = stdout,
            onChunk = onChunk,
            windowManager = windowManager,
            isCancelled = isCancelled
        )

        try {
            val lines = code.lines()
            executeBlock(lines, 0, 0, globalScope, functions, execContext)
        } catch (e: InterruptedException) {
            exitCode = 130
            errorList.add("\n[Process terminated by user]")
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

    private class PyContext(
        val stdout: StringBuilder,
        val onChunk: (String) -> Unit,
        val windowManager: PyWindowManager?,
        val isCancelled: () -> Boolean
    )

    // Tkinter runtime classes
    class PyTkRoot(val wm: PyWindowManager?) {
        var isWithdrawn: Boolean = false
        fun withdraw() { isWithdrawn = true }
        fun mainloop(isCancelled: () -> Boolean) {
            val start = System.currentTimeMillis()
            while (!isCancelled() && (System.currentTimeMillis() - start < 30_000L)) {
                Thread.sleep(30)
                if (isCancelled()) break
            }
        }
        fun destroy() {
            wm?.clearAllWindows()
        }
    }

    class PyTkWindow(val id: String, val wm: PyWindowManager?) {
        var title: String = "Window"
        var x: Float = 60f
        var y: Float = 140f
        var width: Float = 260f
        var height: Float = 160f
        var text: String = ""

        fun updateTitle(newTitle: String) {
            title = newTitle
        }

        fun setGeometry(geo: String) {
            try {
                val sizePart = geo.substringBefore("+")
                if (sizePart.contains("x")) {
                    width = sizePart.substringBefore("x").toFloatOrNull() ?: width
                    height = sizePart.substringAfter("x").toFloatOrNull() ?: height
                }
                if (geo.contains("+")) {
                    val afterPlus = geo.substringAfter("+")
                    val posParts = afterPlus.split("+")
                    if (posParts.isNotEmpty()) x = posParts[0].toFloatOrNull() ?: x
                    if (posParts.size > 1) y = posParts[1].toFloatOrNull() ?: y
                }
            } catch (_: Exception) {}
        }
    }

    class PyTkLabel(val window: PyTkWindow, val wm: PyWindowManager?) {
        var text: String = ""
        fun config(newText: String) {
            text = newText
            wm?.updateWindowText(window.id, newText)
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
    private class PyRuntimeException(val lineNumber: Int, val sourceLine: String, val errorType: String, message: String) : Exception(message)

    private fun executeBlock(
        lines: List<String>,
        startIdx: Int,
        expectedIndent: Int,
        scope: MutableMap<String, Any?>,
        functions: MutableMap<String, PyFunction>,
        ctx: PyContext
    ): Int {
        var idx = startIdx
        while (idx < lines.size) {
            if (ctx.isCancelled()) throw InterruptedException("Execution cancelled")

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
                            val defVal = evalExpr(p.substringAfter("=").trim(), scope, functions, ctx)
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
                val (newIdx, _) = handleIfElifElse(lines, idx, indent, cond, scope, functions, ctx)
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

                val iterable = evalIterable(iterExpr, scope, functions, ctx)
                for (item in iterable) {
                    if (ctx.isCancelled()) throw InterruptedException("Execution cancelled")

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
                        executeBlock(bodyLines, 0, nextIndent, scope, functions, ctx)
                    } catch (b: PyBreakException) {
                        break
                    } catch (c: PyContinueException) {
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

                var safetyCount = 0
                while (isTruthy(evalExpr(cond, scope, functions, ctx))) {
                    if (ctx.isCancelled()) throw InterruptedException("Execution cancelled")
                    safetyCount++
                    if (safetyCount > 500_000) {
                        throw PyRuntimeException(idx + 1, trimmed, "RuntimeError", "Maximum loop iterations exceeded.")
                    }
                    try {
                        executeBlock(bodyLines, 0, nextIndent, scope, functions, ctx)
                    } catch (b: PyBreakException) {
                        break
                    } catch (c: PyContinueException) {
                        continue
                    }
                }
                continue
            }

            // Single line statement
            executeStatement(trimmed, idx + 1, scope, functions, ctx)
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
        ctx: PyContext
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

            if (!handled && isTruthy(evalExpr(cond, scope, functions, ctx))) {
                executeBlock(bodyLines, 0, nextIndent, scope, functions, ctx)
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
        ctx: PyContext
    ) {
        val trimmed = stmt.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return

        if (ctx.isCancelled()) throw InterruptedException("Execution cancelled")

        // Return
        if (trimmed.startsWith("return")) {
            val retExpr = trimmed.removePrefix("return").trim()
            val retVal = if (retExpr.isNotEmpty()) evalExpr(retExpr, scope, functions, ctx) else null
            throw PyReturnException(retVal)
        }

        // Break & Continue
        if (trimmed == "break") throw PyBreakException()
        if (trimmed == "continue") throw PyContinueException()

        // Imports
        if (trimmed.startsWith("import ") || trimmed.startsWith("from ")) {
            return
        }

        // sys.stdout.flush()
        if (trimmed == "sys.stdout.flush()" || trimmed.startsWith("sys.stdout.flush(")) {
            return
        }

        // sys.stdout.write(expr) - Stream character by character immediately!
        if (trimmed.startsWith("sys.stdout.write(") && trimmed.endsWith(")")) {
            val inner = trimmed.removePrefix("sys.stdout.write(").removeSuffix(")").trim()
            val evaluated = evalExpr(inner, scope, functions, ctx)
            val str = evaluated?.toString() ?: "None"
            ctx.stdout.append(str)
            ctx.onChunk(str)
            return
        }

        // time.sleep(delay) - Non-blocking sleep with cancellation checks!
        if (trimmed.startsWith("time.sleep(") && trimmed.endsWith(")")) {
            val inner = trimmed.removePrefix("time.sleep(").removeSuffix(")").trim()
            val delayVal = evalExpr(inner, scope, functions, ctx)
            val sec = when (delayVal) {
                is Number -> delayVal.toDouble()
                is String -> delayVal.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
            val ms = (sec * 1000).toLong().coerceAtLeast(0)
            val step = 15L
            var elapsed = 0L
            while (elapsed < ms) {
                if (ctx.isCancelled()) throw InterruptedException("Execution stopped by user")
                val sleepTime = minOf(step, ms - elapsed)
                Thread.sleep(sleepTime)
                elapsed += sleepTime
            }
            return
        }

        // print(*args, sep=' ', end='\n') - Stream immediately!
        if (trimmed.startsWith("print(") && trimmed.endsWith(")")) {
            val inner = trimmed.removePrefix("print(").removeSuffix(")").trim()
            if (inner.isEmpty()) {
                ctx.stdout.append("\n")
                ctx.onChunk("\n")
                return
            }

            val args = splitArgs(inner)
            var sep = " "
            var end = "\n"
            val normalArgs = mutableListOf<String>()

            for (a in args) {
                val aTrim = a.trim()
                if (aTrim.startsWith("sep=")) {
                    sep = evalExpr(aTrim.removePrefix("sep=").trim(), scope, functions, ctx)?.toString() ?: " "
                } else if (aTrim.startsWith("end=")) {
                    end = evalExpr(aTrim.removePrefix("end=").trim(), scope, functions, ctx)?.toString() ?: "\n"
                } else if (!aTrim.startsWith("flush=")) {
                    normalArgs.add(aTrim)
                }
            }

            val text = normalArgs.joinToString(sep) { evalExpr(it, scope, functions, ctx)?.toString() ?: "None" }
            ctx.stdout.append(text).append(end)
            ctx.onChunk(text + end)
            return
        }

        // Tkinter: root.withdraw()
        if (trimmed.endsWith(".withdraw()")) {
            val obj = trimmed.removeSuffix(".withdraw()").trim()
            (scope[obj] as? PyTkRoot)?.withdraw()
            return
        }

        // Tkinter: root.mainloop()
        if (trimmed.endsWith(".mainloop()")) {
            val obj = trimmed.removeSuffix(".mainloop()").trim()
            val root = scope[obj] as? PyTkRoot ?: scope.values.firstOrNull { it is PyTkRoot } as? PyTkRoot
            root?.mainloop { ctx.isCancelled() }
            return
        }

        // Tkinter: window.geometry("...")
        if (trimmed.contains(".geometry(") && trimmed.endsWith(")")) {
            val objName = trimmed.substringBefore(".geometry(").trim()
            val geoArg = trimmed.substringAfter(".geometry(").removeSuffix(")").trim()
            val geoStr = evalExpr(geoArg, scope, functions, ctx)?.toString() ?: ""
            val win = scope[objName] as? PyTkWindow
            if (win != null) {
                win.setGeometry(geoStr)
                ctx.windowManager?.let {
                    // Update bounds
                }
            }
            return
        }

        // Tkinter: window.title("...")
        if (trimmed.contains(".title(") && trimmed.endsWith(")")) {
            val objName = trimmed.substringBefore(".title(").trim()
            val titleArg = trimmed.substringAfter(".title(").removeSuffix(")").trim()
            val titleStr = evalExpr(titleArg, scope, functions, ctx)?.toString() ?: "Window"
            val win = scope[objName] as? PyTkWindow
            win?.title = titleStr
            return
        }

        // Tkinter: label.pack(...)
        if (trimmed.contains(".pack(") && trimmed.endsWith(")")) {
            val objName = trimmed.substringBefore(".pack(").trim()
            val lbl = scope[objName] as? PyTkLabel
            if (lbl != null) {
                ctx.windowManager?.updateWindowText(lbl.window.id, lbl.text)
            }
            return
        }

        // Tkinter: label.config(text=...) or label.configure(text=...)
        if ((trimmed.contains(".config(") || trimmed.contains(".configure(")) && trimmed.endsWith(")")) {
            val prefix = if (trimmed.contains(".config(")) ".config(" else ".configure("
            val objName = trimmed.substringBefore(prefix).trim()
            val arg = trimmed.substringAfter(prefix).removeSuffix(")").trim()
            val newText = if (arg.startsWith("text=")) {
                evalExpr(arg.removePrefix("text=").trim(), scope, functions, ctx)?.toString() ?: ""
            } else {
                evalExpr(arg, scope, functions, ctx)?.toString() ?: ""
            }
            val lbl = scope[objName] as? PyTkLabel
            lbl?.config(newText)
            return
        }

        // List method: list.append(x)
        if (trimmed.contains(".append(") && trimmed.endsWith(")")) {
            val varName = trimmed.substringBefore(".append(").trim()
            val arg = trimmed.substringAfter(".append(").removeSuffix(")").trim()
            val targetList = scope[varName] as? MutableList<Any?>
            if (targetList != null) {
                targetList.add(evalExpr(arg, scope, functions, ctx))
                return
            }
        }

        // Assignment: a = b, a += b, a -= b
        if (trimmed.contains(" = ") || trimmed.contains(" += ") || trimmed.contains(" -= ")) {
            val isPlusEq = trimmed.contains(" += ")
            val isMinusEq = trimmed.contains(" -= ")
            val op = when {
                isPlusEq -> " += "
                isMinusEq -> " -= "
                else -> " = "
            }

            val lhs = trimmed.substringBefore(op).trim()
            val rhs = trimmed.substringAfter(op).trim()

            // Index / Map assignment: d[k] = v
            if (lhs.contains("[") && lhs.endsWith("]")) {
                val objName = lhs.substringBefore("[").trim()
                val keyExpr = lhs.substringAfter("[").removeSuffix("]").trim()
                val keyVal = evalExpr(keyExpr, scope, functions, ctx)
                val targetObj = scope[objName]
                val assignedVal = evalExpr(rhs, scope, functions, ctx)
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

            if (isPlusEq) {
                val curVal = scope[lhs]
                val addVal = evalExpr(rhs, scope, functions, ctx)
                scope[lhs] = when {
                    curVal is Long && addVal is Long -> curVal + addVal
                    curVal is Number && addVal is Number -> curVal.toDouble() + addVal.toDouble()
                    curVal is String -> curVal + addVal.toString()
                    curVal is List<*> && addVal is List<*> -> (curVal + addVal).toMutableList()
                    else -> addVal
                }
                return
            } else if (isMinusEq) {
                val curVal = scope[lhs]
                val subVal = evalExpr(rhs, scope, functions, ctx)
                scope[lhs] = when {
                    curVal is Long && subVal is Long -> curVal - subVal
                    curVal is Number && subVal is Number -> curVal.toDouble() - subVal.toDouble()
                    else -> 0L
                }
                return
            }

            // Normal assignment: handle Tkinter constructors
            if (rhs == "tk.Tk()" || rhs == "Tk()") {
                scope[lhs] = PyTkRoot(ctx.windowManager)
                return
            }

            if (rhs.startsWith("tk.Toplevel(") || rhs.startsWith("Toplevel(") || rhs == "tk.Toplevel()" || rhs == "Toplevel()") {
                val winCount = scope.values.count { it is PyTkWindow } + 1
                val posX = (40f + (winCount * 25f) % 200f)
                val posY = (120f + (winCount * 30f) % 300f)
                val winId = ctx.windowManager?.spawnWindow(
                    title = "Window $winCount",
                    x = posX,
                    y = posY,
                    width = 260f,
                    height = 160f,
                    text = "Window $winCount"
                ) ?: UUID.randomUUID().toString().take(6)
                scope[lhs] = PyTkWindow(winId, ctx.windowManager)
                return
            }

            if (rhs.startsWith("tk.Label(") || rhs.startsWith("Label(")) {
                val inner = rhs.substringAfter("(").removeSuffix(")").trim()
                val args = splitArgs(inner)
                val winObj = if (args.isNotEmpty()) scope[args[0].trim()] as? PyTkWindow else null
                val targetWin = winObj ?: scope.values.firstOrNull { it is PyTkWindow } as? PyTkWindow
                ?: PyTkWindow(UUID.randomUUID().toString().take(6), ctx.windowManager)

                var textVal = "Label"
                for (arg in args) {
                    val aTrim = arg.trim()
                    if (aTrim.startsWith("text=")) {
                        textVal = evalExpr(aTrim.removePrefix("text=").trim(), scope, functions, ctx)?.toString() ?: ""
                    }
                }

                val lbl = PyTkLabel(targetWin, ctx.windowManager).apply { text = textVal }
                scope[lhs] = lbl
                return
            }

            scope[lhs] = evalExpr(rhs, scope, functions, ctx)
            return
        }

        // Bare expression or function call
        evalExpr(trimmed, scope, functions, ctx)
    }

    private fun evalIterable(
        expr: String,
        scope: MutableMap<String, Any?>,
        functions: MutableMap<String, PyFunction>,
        ctx: PyContext
    ): List<Any?> {
        val trimmed = expr.trim()
        if (trimmed.startsWith("range(") && trimmed.endsWith(")")) {
            val inner = trimmed.removePrefix("range(").removeSuffix(")").trim()
            val args = splitArgs(inner).map { evalExpr(it, scope, functions, ctx) }
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

        val evaluated = evalExpr(trimmed, scope, functions, ctx)
        return when (evaluated) {
            is List<*> -> evaluated
            is String -> evaluated.map { it.toString() }
            is Map<*, *> -> evaluated.keys.toList()
            else -> emptyList()
        }
    }

    private fun evalExpr(
        raw: String,
        scope: MutableMap<String, Any?>,
        functions: MutableMap<String, PyFunction>,
        ctx: PyContext
    ): Any? {
        val expr = raw.trim()
        if (expr.isEmpty()) return null

        // F-strings: f"..." or f'...'
        if ((expr.startsWith("f\"") && expr.endsWith("\"")) || (expr.startsWith("f'") && expr.endsWith("'"))) {
            val template = expr.substring(2, expr.length - 1)
            val regex = Regex("\\{([^}]+)\\}")
            return regex.replace(template) { match ->
                val innerExpr = match.groupValues[1]
                evalExpr(innerExpr, scope, functions, ctx)?.toString() ?: "None"
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
            val inner = expr.removePrefix("[").removeSuffix("]").trim()
            if (inner.isEmpty()) return mutableListOf<Any?>()
            return splitArgs(inner).map { evalExpr(it, scope, functions, ctx) }.toMutableList()
        }

        // Dict literal: {"a": 1, "b": 2}
        if (expr.startsWith("{") && expr.endsWith("}")) {
            val inner = expr.removePrefix("{").removeSuffix("}").trim()
            if (inner.isEmpty()) return mutableMapOf<Any?, Any?>()
            val map = mutableMapOf<Any?, Any?>()
            splitArgs(inner).forEach { pair ->
                if (pair.contains(":")) {
                    val k = evalExpr(pair.substringBefore(":").trim(), scope, functions, ctx)
                    val v = evalExpr(pair.substringAfter(":").trim(), scope, functions, ctx)
                    map[k] = v
                }
            }
            return map
        }

        // Tuple literal: (a, b)
        if (expr.startsWith("(") && expr.endsWith(")") && expr.contains(",")) {
            val inner = expr.removePrefix("(").removeSuffix(")").trim()
            return splitArgs(inner).map { evalExpr(it, scope, functions, ctx) }
        }

        // Binary operations: +, -, *, /, %, // (with or without spaces)
        val binaryOps = listOf(" == ", " != ", " <= ", " >= ", " < ", " > ", " + ", " - ", " * ", " // ", " / ", " % ")
        for (op in binaryOps) {
            if (expr.contains(op)) {
                val left = evalExpr(expr.substringBefore(op).trim(), scope, functions, ctx)
                val right = evalExpr(expr.substringAfter(op).trim(), scope, functions, ctx)
                return evalBinaryOp(op.trim(), left, right)
            }
        }
        if (!expr.startsWith("\"") && !expr.startsWith("'") && !expr.contains("(") && !expr.contains("[")) {
            val compactOps = listOf("+", "-", "*", "/", "%")
            for (op in compactOps) {
                if (expr.contains(op) && !expr.startsWith(op)) {
                    val left = evalExpr(expr.substringBefore(op).trim(), scope, functions, ctx)
                    val right = evalExpr(expr.substringAfter(op).trim(), scope, functions, ctx)
                    return evalBinaryOp(op, left, right)
                }
            }
        }

        // Builtin functions: len(), str(), int(), float(), sum(), min(), max(), abs()
        if (expr.startsWith("len(") && expr.endsWith(")")) {
            val inner = evalExpr(expr.removePrefix("len(").removeSuffix(")").trim(), scope, functions, ctx)
            return when (inner) {
                is String -> inner.length.toLong()
                is List<*> -> inner.size.toLong()
                is Map<*, *> -> inner.size.toLong()
                else -> 0L
            }
        }
        if (expr.startsWith("str(") && expr.endsWith(")")) {
            return evalExpr(expr.removePrefix("str(").removeSuffix(")").trim(), scope, functions, ctx)?.toString() ?: "None"
        }
        if (expr.startsWith("int(") && expr.endsWith(")")) {
            val inner = evalExpr(expr.removePrefix("int(").removeSuffix(")").trim(), scope, functions, ctx)
            return (inner as? Number)?.toLong() ?: inner?.toString()?.toLongOrNull() ?: 0L
        }
        if (expr.startsWith("float(") && expr.endsWith(")")) {
            val inner = evalExpr(expr.removePrefix("float(").removeSuffix(")").trim(), scope, functions, ctx)
            return (inner as? Number)?.toDouble() ?: inner?.toString()?.toDoubleOrNull() ?: 0.0
        }
        if (expr.startsWith("abs(") && expr.endsWith(")")) {
            val inner = evalExpr(expr.removePrefix("abs(").removeSuffix(")").trim(), scope, functions, ctx)
            return (inner as? Number)?.toDouble()?.let { kotlin.math.abs(it) } ?: 0.0
        }

        // Random module calls: random.randint(a, b), random.random()
        if (expr.startsWith("random.") && expr.contains("(") && expr.endsWith(")")) {
            val methodName = expr.substringBefore("(").removePrefix("random.").trim()
            val argStr = expr.substringAfter("(").removeSuffix(")").trim()
            val args = if (argStr.isNotEmpty()) splitArgs(argStr).map { evalExpr(it, scope, functions, ctx) } else emptyList()
            return when (methodName) {
                "randint" -> {
                    val low = (args.getOrNull(0) as? Number)?.toInt() ?: 1
                    val high = (args.getOrNull(1) as? Number)?.toInt() ?: 100
                    Random.nextInt(low, high + 1)
                }
                "random" -> Random.nextDouble()
                "choice" -> (args.getOrNull(0) as? List<*>)?.let { if (it.isNotEmpty()) it.random() else null }
                else -> Random.nextInt(1, 100)
            }
        }

        // Math module calls: math.sqrt, sin, cos, etc.
        if (expr.startsWith("math.") && expr.contains("(") && expr.endsWith(")")) {
            val methodName = expr.substringBefore("(").removePrefix("math.").trim()
            val argStr = expr.substringAfter("(").removeSuffix(")").trim()
            val argVal = evalExpr(argStr, scope, functions, ctx)
            val num = (argVal as? Number)?.toDouble() ?: 0.0
            return when (methodName) {
                "sqrt" -> kotlin.math.sqrt(num)
                "sin" -> kotlin.math.sin(num)
                "cos" -> kotlin.math.cos(num)
                "floor" -> kotlin.math.floor(num).toLong()
                "ceil" -> kotlin.math.ceil(num).toLong()
                else -> num
            }
        }

        // User defined function call: func(args) - Writes directly to the REAL stdout and streams via onChunk!
        if (expr.contains("(") && expr.endsWith(")")) {
            val funcName = expr.substringBefore("(").trim()
            val argStr = expr.substringAfter("(").removeSuffix(")").trim()
            val pyFunc = functions[funcName]
            if (pyFunc != null) {
                val localScope = scope.toMutableMap()
                val passedArgs = if (argStr.isNotEmpty()) splitArgs(argStr).map { evalExpr(it, scope, functions, ctx) } else emptyList()
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
                    executeBlock(pyFunc.bodyLines, 0, pyFunc.baseIndent, localScope, functions, ctx)
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

    private fun evalBinaryOp(op: String, left: Any?, right: Any?): Any? {
        return when (op) {
            "==" -> left == right
            "!=" -> left != right
            "<=" -> if (left is Number && right is Number) left.toDouble() <= right.toDouble() else false
            ">=" -> if (left is Number && right is Number) left.toDouble() >= right.toDouble() else false
            "<" -> if (left is Number && right is Number) left.toDouble() < right.toDouble() else false
            ">" -> if (left is Number && right is Number) left.toDouble() > right.toDouble() else false
            "+" -> {
                when {
                    left is Number && right is Number -> {
                        val sum = left.toDouble() + right.toDouble()
                        if (sum % 1.0 == 0.0) sum.toLong() else sum
                    }
                    left is String || right is String -> left.toString() + right.toString()
                    left is List<*> && right is List<*> -> left + right
                    else -> left.toString() + right.toString()
                }
            }
            "-" -> if (left is Number && right is Number) {
                val diff = left.toDouble() - right.toDouble()
                if (diff % 1.0 == 0.0) diff.toLong() else diff
            } else 0.0
            "*" -> if (left is Number && right is Number) {
                val prod = left.toDouble() * right.toDouble()
                if (prod % 1.0 == 0.0) prod.toLong() else prod
            } else 0.0
            "/" -> if (left is Number && right is Number && right.toDouble() != 0.0) left.toDouble() / right.toDouble() else 0.0
            "//" -> if (left is Number && right is Number && right.toDouble() != 0.0) (left.toDouble() / right.toDouble()).toLong() else 0L
            "%" -> if (left is Number && right is Number) left.toDouble() % right.toDouble() else 0.0
            else -> null
        }
    }

    private fun isTruthy(v: Any?): Boolean {
        return when (v) {
            null -> false
            is Boolean -> v
            is Number -> v.toDouble() != 0.0
            is String -> v.isNotEmpty()
            is List<*> -> v.isNotEmpty()
            is Map<*, *> -> v.isNotEmpty()
            else -> true
        }
    }

    private fun splitArgs(s: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        val sb = StringBuilder()
        for (c in s) {
            when (c) {
                '(', '[', '{' -> depth++
                ')', ']', '}' -> depth--
                ',' -> {
                    if (depth == 0) {
                        result.add(sb.toString().trim())
                        sb.clear()
                        continue
                    }
                }
            }
            sb.append(c)
        }
        if (sb.isNotEmpty()) {
            result.add(sb.toString().trim())
        }
        return result
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
}
