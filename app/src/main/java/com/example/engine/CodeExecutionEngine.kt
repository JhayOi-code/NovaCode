package com.example.engine

import com.example.model.CodeFile
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ExecutionResult(
    val command: String,
    val output: List<String>,
    val executionTimeMs: Long,
    val exitCode: Int,
    val isSuccess: Boolean = exitCode == 0,
    val webPreviewHtml: String? = null
)

object CodeExecutionEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Executes the code of a given CodeFile based on its language extension.
     */
    fun execute(file: CodeFile, allFiles: List<CodeFile>): ExecutionResult {
        val extension = file.name.substringAfterLast('.', "").lowercase()

        return when (extension) {
            "py", "pyw" -> PythonRunner.run(file.content, file.name)
            "java" -> JavaRunner.run(file.content, file.name)
            "sql" -> SqlRunner.run(file.content, file.name)
            "kt", "kts" -> LanguageRunners.runKotlin(file.content, file.name)
            "js", "ts", "mjs" -> LanguageRunners.runJavaScript(file.content, file.name)
            "c", "cpp", "cc", "h", "hpp" -> LanguageRunners.runCpp(file.content, file.name)
            "rs" -> LanguageRunners.runRust(file.content, file.name)
            "go" -> LanguageRunners.runGo(file.content, file.name)
            "sh", "bash" -> LanguageRunners.runBash(file.content, file.name, allFiles)
            "html", "htm", "svg" -> LanguageRunners.runHtml(file.content, file.name)
            "json" -> LanguageRunners.runJson(file.content, file.name)
            "md", "markdown" -> LanguageRunners.runMarkdown(file.content, file.name)
            else -> ExecutionResult(
                command = "cat ${file.name}",
                output = file.content.lines().take(50),
                executionTimeMs = 12,
                exitCode = 0,
                isSuccess = true
            )
        }
    }

    /**
     * Executes an interactive shell command in the context of the workspace (Termux style).
     */
    fun executeCommand(
        rawCommand: String,
        openFiles: List<CodeFile>,
        onNewFile: (String, String) -> Unit,
        onDeleteFile: (String) -> Unit
    ): List<String> {
        val trimmed = rawCommand.trim()
        if (trimmed.isEmpty()) return emptyList()

        val parts = trimmed.split(Regex("\\s+"))
        val cmd = parts[0].lowercase()
        val args = if (parts.size > 1) parts.subList(1, parts.size) else emptyList()

        return when (cmd) {
            "help", "?" -> listOf(
                "Linq Studios Shell v2.4.0 (Linux ARM64/x86_64)",
                "Built-in Commands & Multi-Language Runtimes:",
                "  python, py <file>     Run Python script (typewriter, loops, functions, math, sys)",
                "  java <file>           Compile & execute Java application (classes, methods, loops)",
                "  sql <file>            Execute SQLite statements on real in-memory database",
                "  kotlinc, kt <file>    Execute Kotlin script or class",
                "  node, js, ts <file>   Run JavaScript / TypeScript engine",
                "  gcc, clang, c++ <file> Compile & run C/C++ program",
                "  rustc, rs <file>      Compile & run Rust source",
                "  go <file>             Execute Go application",
                "  run [file]            Execute active file or specified file",
                "  ls [-la]              List directory files with metadata",
                "  cat <file>            Display file contents in terminal",
                "  touch <file>          Create a new file",
                "  rm <file>             Remove a file",
                "  echo <text>           Output text",
                "  pwd, whoami, date     System environmental inspection",
                "  curl <url>            Send real HTTP request and inspect headers/body",
                "  neofetch              Display system specs",
                "  clear                 Clear terminal buffer"
            )

            "pwd" -> listOf("/data/data/com.aistudio.linq/workspace")
            "whoami" -> listOf("linq_dev (uid=10243 gid=10243)")
            "date" -> listOf(SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US).format(Date()))

            "uname" -> {
                if (args.contains("-a") || args.contains("-m")) {
                    listOf("Linux linq-android 5.15.0-termux-aarch64 #1 SMP PREEMPT Android")
                } else {
                    listOf("Linux")
                }
            }

            "neofetch" -> listOf(
                "       /\\         linq_dev@android-linq",
                "      /  \\        ---------------------",
                "     / /\\ \\       OS: Linq Studios Linux 5.15.0-aarch64",
                "    / /  \\ \\      Host: Android Mobile Device",
                "   / / /\\ \\ \\     Kernel: 5.15.0-linq",
                "  / / /  \\ \\ \\    Uptime: 4 days, 12 hours",
                " /_/_/    \\_\\_\\   Shell: Linq Studio Bash v2.4.0",
                "                  Editor: Linq Studios Compose IDE",
                "                  CPU: ARM Cortex-A78 (8 cores)",
                "                  Memory: 3120MiB / 7840MiB"
            )

            "ls" -> {
                val isDetailed = args.contains("-la") || args.contains("-l")
                if (openFiles.isEmpty()) {
                    listOf("total 0")
                } else {
                    val result = mutableListOf<String>()
                    if (isDetailed) {
                        result.add("total ${openFiles.size * 4}")
                        openFiles.forEach { f ->
                            val size = f.content.toByteArray().size
                            result.add("-rw-r--r-- 1 linq_dev linq_dev %5d Oct 21 12:00 %s".format(size, f.name))
                        }
                    } else {
                        result.add(openFiles.joinToString("  ") { it.name })
                    }
                    result
                }
            }

            "cat" -> {
                if (args.isEmpty()) {
                    listOf("cat: missing file operand")
                } else {
                    val file = findFileMatching(args[0], openFiles)
                    file?.content?.lines() ?: listOf("cat: ${args[0]}: No such file or directory")
                }
            }

            "touch" -> {
                if (args.isEmpty()) {
                    listOf("touch: missing file operand")
                } else {
                    val name = args[0]
                    onNewFile(name, "")
                    listOf("Created empty file: $name")
                }
            }

            "rm" -> {
                if (args.isEmpty()) {
                    listOf("rm: missing operand")
                } else {
                    val name = args[0]
                    val file = findFileMatching(name, openFiles)
                    if (file != null) {
                        onDeleteFile(file.name)
                        listOf("Removed file: ${file.name}")
                    } else {
                        listOf("rm: cannot remove '$name': No such file or directory")
                    }
                }
            }

            "echo" -> {
                listOf(args.joinToString(" ").trim('"', '\''))
            }

            "python", "python3", "py" -> {
                if (args.isEmpty()) {
                    listOf(
                        "Python 3.11.8 (main, Oct 21 2026, 12:00:00) [Clang 17.0.6]",
                        "Type \"help\", \"copyright\", \"credits\" or \"license\" for more information.",
                        "Usage: python <filename.py> or pass script as argument"
                    )
                } else {
                    val file = findFileMatching(args[0], openFiles)
                    if (file != null) {
                        PythonRunner.run(file.content, file.name).output
                    } else {
                        val inlineCode = args.joinToString(" ")
                        PythonRunner.run(inlineCode, "stdin.py").output
                    }
                }
            }

            "java" -> {
                if (args.isEmpty()) {
                    listOf("openjdk version \"21.0.2\" 2026-10-21", "Usage: java <File.java>")
                } else {
                    val file = findFileMatching(args[0], openFiles)
                    if (file != null) {
                        JavaRunner.run(file.content, file.name).output
                    } else {
                        listOf("Error: Could not find or load main class ${args[0]}")
                    }
                }
            }

            "javac" -> {
                if (args.isEmpty()) {
                    listOf("javac 21.0.2", "Usage: javac <source files>")
                } else {
                    val file = findFileMatching(args[0], openFiles)
                    if (file != null) {
                        listOf("Compiled ${file.name} successfully into ${file.name.substringBeforeLast(".")}.class")
                    } else {
                        listOf("error: Class names, '${args[0]}', are only accepted if annotation processing is explicitly requested")
                    }
                }
            }

            "node", "js", "ts" -> {
                if (args.isEmpty()) {
                    listOf("v20.11.1", "Usage: node <file.js>")
                } else {
                    val file = findFileMatching(args[0], openFiles)
                    if (file != null) {
                        LanguageRunners.runJavaScript(file.content, file.name).output
                    } else {
                        listOf("node: error: Cannot find module '${args[0]}'")
                    }
                }
            }

            "kt", "kotlinc" -> {
                if (args.isEmpty()) {
                    listOf("info: kotlinc-jvm 1.9.22 (JRE 21.0.2)", "Usage: kotlinc <file.kt>")
                } else {
                    val file = findFileMatching(args[0], openFiles)
                    if (file != null) {
                        LanguageRunners.runKotlin(file.content, file.name).output
                    } else {
                        listOf("error: no source files were specified")
                    }
                }
            }

            "gcc", "clang", "c", "c++", "g++" -> {
                if (args.isEmpty()) {
                    listOf("clang version 17.0.6", "Usage: clang <file.c> -o <output>")
                } else {
                    val file = findFileMatching(args[0], openFiles)
                    if (file != null) {
                        LanguageRunners.runCpp(file.content, file.name).output
                    } else {
                        listOf("clang: error: no such file or directory: '${args[0]}'")
                    }
                }
            }

            "rust", "rustc", "rs" -> {
                if (args.isEmpty()) {
                    listOf("rustc 1.76.0 (LLVM 17.0.6)", "Usage: rustc <file.rs>")
                } else {
                    val file = findFileMatching(args[0], openFiles)
                    if (file != null) {
                        LanguageRunners.runRust(file.content, file.name).output
                    } else {
                        listOf("error: could not find '${args[0]}'")
                    }
                }
            }

            "go" -> {
                val target = if (args.firstOrNull() == "run" && args.size > 1) args[1] else args.firstOrNull() ?: ""
                val file = findFileMatching(target, openFiles)
                if (file != null) {
                    LanguageRunners.runGo(file.content, file.name).output
                } else {
                    listOf("go run: no such file: $target")
                }
            }

            "sql", "sqlite3" -> {
                val file = findFileMatching(args.firstOrNull() ?: "", openFiles)
                if (file != null) {
                    SqlRunner.run(file.content, file.name).output
                } else {
                    val inlineSql = args.joinToString(" ")
                    SqlRunner.run(inlineSql, "query.sql").output
                }
            }

            "sh", "bash" -> {
                if (args.isEmpty()) {
                    listOf("GNU bash, version 5.2.15(1)-release (aarch64-unknown-linux-android)")
                } else {
                    val file = findFileMatching(args[0], openFiles)
                    if (file != null) {
                        LanguageRunners.runBash(file.content, file.name, openFiles).output
                    } else {
                        listOf("bash: ${args[0]}: No such file or directory")
                    }
                }
            }

            "run" -> {
                val target = if (args.isNotEmpty()) {
                    findFileMatching(args[0], openFiles)
                } else {
                    openFiles.firstOrNull()
                }

                if (target != null) {
                    execute(target, openFiles).output
                } else {
                    listOf("run: target file '${args.firstOrNull() ?: ""}' not found in workspace")
                }
            }

            "curl" -> {
                if (args.isEmpty()) {
                    listOf("curl: try 'curl --help' for more information")
                } else {
                    val url = if (args[0].startsWith("http")) args[0] else "https://${args[0]}"
                    try {
                        val req = Request.Builder().url(url).build()
                        val res = httpClient.newCall(req).execute()
                        val body = res.body?.string() ?: ""
                        listOf(
                            "< HTTP/1.1 ${res.code} ${res.message}",
                            "< Content-Type: ${res.header("Content-Type") ?: "unknown"}",
                            ""
                        ) + body.take(500).lines()
                    } catch (e: Exception) {
                        listOf("curl: (6) Could not resolve host: $url (${e.message})")
                    }
                }
            }

            else -> {
                val cleanCmd = cmd.removePrefix("./")
                val directFile = findFileMatching(cleanCmd, openFiles)
                if (directFile != null) {
                    execute(directFile, openFiles).output
                } else if (trimmed.startsWith("print(") || trimmed.contains("=")) {
                    PythonRunner.run(trimmed, "terminal_eval.py").output
                } else {
                    listOf("bash: $cmd: command not found. Type 'help' for available commands.")
                }
            }
        }
    }

    private fun findFileMatching(query: String, files: List<CodeFile>): CodeFile? {
        val clean = query.trim().removePrefix("./")
        return files.find { it.name.equals(clean, ignoreCase = true) }
            ?: files.find { it.name.substringBeforeLast(".").equals(clean, ignoreCase = true) }
            ?: files.find { it.name.equals(clean + ".py", ignoreCase = true) }
            ?: files.find { it.name.equals(clean + ".java", ignoreCase = true) }
            ?: files.find { it.name.equals(clean + ".sql", ignoreCase = true) }
            ?: files.find { it.name.equals(clean + ".js", ignoreCase = true) }
            ?: files.find { it.name.equals(clean + ".kt", ignoreCase = true) }
            ?: files.find { it.name.equals(clean + ".cpp", ignoreCase = true) }
    }
}
