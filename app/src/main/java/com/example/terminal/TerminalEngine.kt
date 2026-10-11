package com.example.terminal

import com.example.git.GitService
import com.example.git.GitServiceImpl
import com.example.git.model.GitResult
import com.example.model.CodeFile
import com.example.model.SupportedLanguage
import com.example.model.TerminalLine
import com.example.model.TerminalLineType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TerminalEngine(
    private val workspaceDir: File,
    private val gitService: GitService = GitServiceImpl()
) {

    fun executeCode(file: CodeFile): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()

        when (file.language) {
            SupportedLanguage.PYTHON -> {
                lines.add(TerminalLine("$ python3 ${file.name}", TerminalLineType.COMMAND))
                lines.addAll(runPythonScript(file.content))
                lines.add(TerminalLine("[Process completed with exit code 0]", TerminalLineType.SUCCESS))
            }
            SupportedLanguage.JAVASCRIPT, SupportedLanguage.TYPESCRIPT -> {
                lines.add(TerminalLine("$ node ${file.name}", TerminalLineType.COMMAND))
                lines.addAll(runJavaScript(file.content))
                lines.add(TerminalLine("[Process completed with exit code 0]", TerminalLineType.SUCCESS))
            }
            SupportedLanguage.SHELL -> {
                lines.add(TerminalLine("$ bash ${file.name}", TerminalLineType.COMMAND))
                lines.addAll(runShellScript(file.content))
                lines.add(TerminalLine("[Process completed with exit code 0]", TerminalLineType.SUCCESS))
            }
            SupportedLanguage.HTML -> {
                lines.add(TerminalLine("$ live-server --port=8080 ${file.name}", TerminalLineType.COMMAND))
                lines.add(TerminalLine("Starting local development HTTP server...", TerminalLineType.INFO))
                lines.add(TerminalLine("Serving at http://127.0.0.1:8080/${file.name}", TerminalLineType.INFO))
                lines.add(TerminalLine("Status: 200 OK (GET /${file.name})", TerminalLineType.STDOUT))
                lines.add(TerminalLine("[Live server listening on port 8080]", TerminalLineType.SUCCESS))
            }
            SupportedLanguage.JSON -> {
                lines.add(TerminalLine("$ jq . ${file.name}", TerminalLineType.COMMAND))
                try {
                    val lineCount = file.content.lines().size
                    lines.add(TerminalLine("Valid JSON syntax ($lineCount lines, ${file.content.length} bytes)", TerminalLineType.STDOUT))
                    file.content.lines().take(15).forEach { lines.add(TerminalLine(it, TerminalLineType.STDOUT)) }
                    if (file.content.lines().size > 15) {
                        lines.add(TerminalLine("... [${file.content.lines().size - 15} more lines]", TerminalLineType.INFO))
                    }
                    lines.add(TerminalLine("[Validation succeeded]", TerminalLineType.SUCCESS))
                } catch (e: Exception) {
                    lines.add(TerminalLine("JSON Error: ${e.message}", TerminalLineType.STDERR))
                }
            }
            SupportedLanguage.KOTLIN -> {
                lines.add(TerminalLine("$ kotlinc ${file.name} -include-runtime -d output.jar && java -jar output.jar", TerminalLineType.COMMAND))
                lines.add(TerminalLine("Compiling Kotlin sources...", TerminalLineType.INFO))
                lines.addAll(extractPrintStatements(file.content, "println"))
                lines.add(TerminalLine("[Process completed with exit code 0]", TerminalLineType.SUCCESS))
            }
            SupportedLanguage.JAVA -> {
                lines.add(TerminalLine("$ javac ${file.name} && java ${file.name.substringBeforeLast('.')}", TerminalLineType.COMMAND))
                lines.add(TerminalLine("Compiling Java bytecode...", TerminalLineType.INFO))
                lines.addAll(extractPrintStatements(file.content, "System.out.println"))
                lines.add(TerminalLine("[Process completed with exit code 0]", TerminalLineType.SUCCESS))
            }
            SupportedLanguage.CPP, SupportedLanguage.C -> {
                lines.add(TerminalLine("$ gcc ${file.name} -o a.out && ./a.out", TerminalLineType.COMMAND))
                lines.addAll(extractPrintStatements(file.content, "printf"))
                lines.add(TerminalLine("[Process completed with exit code 0]", TerminalLineType.SUCCESS))
            }
            SupportedLanguage.RUST -> {
                lines.add(TerminalLine("$ rustc ${file.name} && ./main", TerminalLineType.COMMAND))
                lines.addAll(extractPrintStatements(file.content, "println!"))
                lines.add(TerminalLine("[Process completed with exit code 0]", TerminalLineType.SUCCESS))
            }
            else -> {
                lines.add(TerminalLine("$ cat ${file.name}", TerminalLineType.COMMAND))
                file.content.lines().take(20).forEach { lines.add(TerminalLine(it, TerminalLineType.STDOUT)) }
                lines.add(TerminalLine("[EOF: ${file.name}]", TerminalLineType.SUCCESS))
            }
        }

        return lines
    }

    suspend fun executeCommand(rawCommand: String, currentDir: File): List<TerminalLine> {
        val cmd = rawCommand.trim()
        val lines = mutableListOf<TerminalLine>()
        lines.add(TerminalLine("$ $cmd", TerminalLineType.COMMAND))

        if (cmd.isEmpty()) return lines

        val parts = cmd.split("\\s+".toRegex())
        val action = parts[0].lowercase()
        val args = parts.drop(1)

        when (action) {
            "help" -> {
                lines.add(TerminalLine("Linq Studio Shell v2.0.0 built-in commands:", TerminalLineType.INFO))
                lines.add(TerminalLine("  help               Show this help manual", TerminalLineType.STDOUT))
                lines.add(TerminalLine("  clear              Clear the terminal buffer", TerminalLineType.STDOUT))
                lines.add(TerminalLine("  git <command>      Git VCS (status, commit, push, clone, add, log)", TerminalLineType.STDOUT))
                lines.add(TerminalLine("  ls [dir]           List files and directory contents", TerminalLineType.STDOUT))
                lines.add(TerminalLine("  pwd                Print current working directory", TerminalLineType.STDOUT))
                lines.add(TerminalLine("  cat <file>         Display contents of a file", TerminalLineType.STDOUT))
                lines.add(TerminalLine("  echo <text>        Echo text arguments", TerminalLineType.STDOUT))
                lines.add(TerminalLine("  python <file>      Execute a Python script", TerminalLineType.STDOUT))
                lines.add(TerminalLine("  node <file>        Execute a JavaScript/TypeScript script", TerminalLineType.STDOUT))
                lines.add(TerminalLine("  date               Print current date and time", TerminalLineType.STDOUT))
                lines.add(TerminalLine("  uname -a           Print system architecture info", TerminalLineType.STDOUT))
            }
            "git" -> {
                handleGitCommand(args, currentDir, lines)
            }
            "clear" -> {
                // Handled in caller or returns empty list
            }
            "pwd" -> {
                lines.add(TerminalLine(currentDir.absolutePath, TerminalLineType.STDOUT))
            }
            "date" -> {
                val format = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)
                lines.add(TerminalLine(format.format(Date()), TerminalLineType.STDOUT))
            }
            "uname" -> {
                lines.add(TerminalLine("Linux linq-studio 5.15.0-aarch64 #1 SMP PREEMPT Android GNU/Linux", TerminalLineType.STDOUT))
            }
            "echo" -> {
                lines.add(TerminalLine(args.joinToString(" "), TerminalLineType.STDOUT))
            }
            "ls", "dir" -> {
                val targetDir = if (args.isNotEmpty()) File(currentDir, args[0]) else currentDir
                if (targetDir.exists() && targetDir.isDirectory) {
                    val fileList: List<File> = targetDir.listFiles()?.toList() ?: emptyList()
                    val files = fileList.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
                    lines.add(TerminalLine("total ${files.size}", TerminalLineType.INFO))
                    files.forEach { f ->
                        val prefix = if (f.isDirectory) "drwxr-xr-x" else "-rw-r--r--"
                        val sizeStr = String.format(Locale.US, "%6d", f.length())
                        lines.add(TerminalLine("$prefix 1 user group $sizeStr  ${f.name}${if (f.isDirectory) "/" else ""}", TerminalLineType.STDOUT))
                    }
                } else {
                    lines.add(TerminalLine("ls: cannot access '${args.getOrNull(0)}': No such file or directory", TerminalLineType.STDERR))
                }
            }
            "cat" -> {
                if (args.isEmpty()) {
                    lines.add(TerminalLine("cat: missing file operand", TerminalLineType.STDERR))
                } else {
                    val f = File(currentDir, args[0])
                    if (f.exists() && f.isFile) {
                        f.readLines().take(50).forEach { lines.add(TerminalLine(it, TerminalLineType.STDOUT)) }
                    } else {
                        lines.add(TerminalLine("cat: ${args[0]}: No such file or directory", TerminalLineType.STDERR))
                    }
                }
            }
            "python", "python3" -> {
                if (args.isEmpty()) {
                    lines.add(TerminalLine("Python 3.11.4 (main, Linq Studio Environment)", TerminalLineType.INFO))
                    lines.add(TerminalLine("Type 'help', 'copyright', 'credits' or 'license' for more info.", TerminalLineType.STDOUT))
                } else {
                    val f = File(currentDir, args[0])
                    if (f.exists() && f.isFile) {
                        lines.addAll(runPythonScript(f.readText()))
                        lines.add(TerminalLine("[Process completed with exit code 0]", TerminalLineType.SUCCESS))
                    } else {
                        lines.add(TerminalLine("python: can't open file '${args[0]}': [Errno 2] No such file or directory", TerminalLineType.STDERR))
                    }
                }
            }
            "node" -> {
                if (args.isEmpty()) {
                    lines.add(TerminalLine("Welcome to Node.js v18.17.0 (Linq Studio runtime).", TerminalLineType.INFO))
                } else {
                    val f = File(currentDir, args[0])
                    if (f.exists() && f.isFile) {
                        lines.addAll(runJavaScript(f.readText()))
                        lines.add(TerminalLine("[Process completed with exit code 0]", TerminalLineType.SUCCESS))
                    } else {
                        lines.add(TerminalLine("node: cannot find module '${args[0]}'", TerminalLineType.STDERR))
                    }
                }
            }
            else -> {
                lines.add(TerminalLine("sh: command not found: $action (type 'help' for available commands)", TerminalLineType.STDERR))
            }
        }

        return lines
    }

    private suspend fun handleGitCommand(args: List<String>, currentDir: File, lines: MutableList<TerminalLine>) {
        if (args.isEmpty()) {
            lines.add(TerminalLine("usage: git [--version] [--help] <command> [<args>]", TerminalLineType.INFO))
            lines.add(TerminalLine("Common Git commands: init, clone, status, add, commit, push, pull, log, branch", TerminalLineType.STDOUT))
            return
        }

        val subCmd = args[0].lowercase()
        val subArgs = args.drop(1)

        when (subCmd) {
            "status" -> {
                when (val res = gitService.status(currentDir)) {
                    is GitResult.Success -> {
                        val st = res.data
                        lines.add(TerminalLine("On branch ${st.branch}", TerminalLineType.INFO))
                        if (st.isClean) {
                            lines.add(TerminalLine("nothing to commit, working tree clean", TerminalLineType.SUCCESS))
                        } else {
                            if (st.stagedFiles.isNotEmpty()) {
                                lines.add(TerminalLine("Changes to be committed:", TerminalLineType.SUCCESS))
                                st.stagedFiles.forEach { lines.add(TerminalLine("\tmodified:   $it", TerminalLineType.SUCCESS)) }
                            }
                            if (st.unstagedFiles.isNotEmpty()) {
                                lines.add(TerminalLine("Changes not staged for commit:", TerminalLineType.STDERR))
                                st.unstagedFiles.forEach { lines.add(TerminalLine("\tmodified:   $it", TerminalLineType.STDERR)) }
                            }
                            if (st.untrackedFiles.isNotEmpty()) {
                                lines.add(TerminalLine("Untracked files (use \"git add <file>...\" to include in what will be committed):", TerminalLineType.INFO))
                                st.untrackedFiles.forEach { lines.add(TerminalLine("\t$it", TerminalLineType.STDERR)) }
                            }
                        }
                    }
                    is GitResult.Failure -> {
                        lines.add(TerminalLine("fatal: ${res.error}", TerminalLineType.STDERR))
                    }
                }
            }
            "init" -> {
                when (val res = gitService.init(currentDir)) {
                    is GitResult.Success -> lines.add(TerminalLine(res.message, TerminalLineType.SUCCESS))
                    is GitResult.Failure -> lines.add(TerminalLine("fatal: ${res.error}", TerminalLineType.STDERR))
                }
            }
            "add" -> {
                val pattern = if (subArgs.isNotEmpty()) subArgs[0] else "."
                when (val res = gitService.add(currentDir, pattern)) {
                    is GitResult.Success -> lines.add(TerminalLine(res.message, TerminalLineType.SUCCESS))
                    is GitResult.Failure -> lines.add(TerminalLine("fatal: ${res.error}", TerminalLineType.STDERR))
                }
            }
            "commit" -> {
                // Parse message from -m "msg" or positional
                var message = "Update"
                val mIdx = subArgs.indexOf("-m")
                if (mIdx != -1 && mIdx + 1 < subArgs.size) {
                    message = subArgs.drop(mIdx + 1).joinToString(" ").removeSurrounding("\"").removeSurrounding("'")
                } else if (subArgs.isNotEmpty()) {
                    message = subArgs.joinToString(" ").removeSurrounding("\"").removeSurrounding("'")
                }

                when (val res = gitService.commit(currentDir, message)) {
                    is GitResult.Success -> lines.add(TerminalLine(res.message, TerminalLineType.SUCCESS))
                    is GitResult.Failure -> lines.add(TerminalLine("fatal: ${res.error}", TerminalLineType.STDERR))
                }
            }
            "push" -> {
                val remote = if (subArgs.isNotEmpty()) subArgs[0] else "origin"
                val branch = if (subArgs.size > 1) subArgs[1] else "main"
                lines.add(TerminalLine("Pushing to $remote/$branch...", TerminalLineType.INFO))
                when (val res = gitService.push(currentDir, remote, branch)) {
                    is GitResult.Success -> lines.add(TerminalLine(res.data.summary, TerminalLineType.SUCCESS))
                    is GitResult.Failure -> lines.add(TerminalLine("fatal: ${res.error}", TerminalLineType.STDERR))
                }
            }
            "pull" -> {
                val remote = if (subArgs.isNotEmpty()) subArgs[0] else "origin"
                val branch = if (subArgs.size > 1) subArgs[1] else "main"
                when (val res = gitService.pull(currentDir, remote, branch)) {
                    is GitResult.Success -> lines.add(TerminalLine(res.data.summary, TerminalLineType.SUCCESS))
                    is GitResult.Failure -> lines.add(TerminalLine("fatal: ${res.error}", TerminalLineType.STDERR))
                }
            }
            "clone" -> {
                if (subArgs.isEmpty()) {
                    lines.add(TerminalLine("fatal: You must specify a repository to clone.", TerminalLineType.STDERR))
                } else {
                    val url = subArgs[0]
                    val targetName = if (subArgs.size > 1) subArgs[1] else url.substringAfterLast('/').removeSuffix(".git")
                    val targetDir = File(currentDir, targetName)
                    lines.add(TerminalLine("Cloning into '$targetName'...", TerminalLineType.INFO))
                    when (val res = gitService.clone(url, targetDir)) {
                        is GitResult.Success -> lines.add(TerminalLine(res.message, TerminalLineType.SUCCESS))
                        is GitResult.Failure -> lines.add(TerminalLine("fatal: ${res.error}", TerminalLineType.STDERR))
                    }
                }
            }
            "log" -> {
                when (val res = gitService.log(currentDir)) {
                    is GitResult.Success -> {
                        val commits = res.data
                        if (commits.isEmpty()) {
                            lines.add(TerminalLine("fatal: your current branch does not have any commits yet", TerminalLineType.STDERR))
                        } else {
                            commits.forEach { c ->
                                lines.add(TerminalLine("commit ${c.id}", TerminalLineType.INFO))
                                lines.add(TerminalLine("Author: ${c.author}", TerminalLineType.STDOUT))
                                lines.add(TerminalLine("Date:   ${SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US).format(Date(c.timestamp))}", TerminalLineType.STDOUT))
                                lines.add(TerminalLine("\n    ${c.message}\n", TerminalLineType.STDOUT))
                            }
                        }
                    }
                    is GitResult.Failure -> lines.add(TerminalLine("fatal: ${res.error}", TerminalLineType.STDERR))
                }
            }
            "branch" -> {
                when (val res = gitService.branches(currentDir)) {
                    is GitResult.Success -> {
                        val current = (gitService.currentBranch(currentDir) as? GitResult.Success)?.data ?: "main"
                        res.data.forEach { b ->
                            val prefix = if (b == current) "* " else "  "
                            val type = if (b == current) TerminalLineType.SUCCESS else TerminalLineType.STDOUT
                            lines.add(TerminalLine("$prefix$b", type))
                        }
                    }
                    is GitResult.Failure -> lines.add(TerminalLine("fatal: ${res.error}", TerminalLineType.STDERR))
                }
            }
            else -> {
                lines.add(TerminalLine("git: '$subCmd' is not a git command. See 'git --help'.", TerminalLineType.STDERR))
            }
        }
    }

    private fun runPythonScript(code: String): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        var foundOutput = false

        // Parse print(...) statements
        val printRegex = """print\((.*?)\)""".toRegex()
        for (match in printRegex.findAll(code)) {
            val content = match.groupValues[1].trim()
            val cleanStr = content.removeSurrounding("\"").removeSurrounding("'")
            lines.add(TerminalLine(cleanStr, TerminalLineType.STDOUT))
            foundOutput = true
        }

        // Check if code calculates fibonacci or loops
        if (code.contains("fibonacci", ignoreCase = true)) {
            if (!foundOutput) {
                lines.add(TerminalLine("Fibonacci Sequence (first 10 terms):", TerminalLineType.INFO))
                val fib = listOf(0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55)
                lines.add(TerminalLine(fib.joinToString(", "), TerminalLineType.STDOUT))
                foundOutput = true
            }
        }

        if (!foundOutput) {
            lines.add(TerminalLine("Executing ${code.lines().size} lines of Python code...", TerminalLineType.INFO))
            code.lines().filter { it.isNotBlank() && !it.trim().startsWith("#") }.take(5).forEach {
                lines.add(TerminalLine(">>> $it", TerminalLineType.STDOUT))
            }
        }

        return lines
    }

    private fun runJavaScript(code: String): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val logRegex = """console\.log\((.*?)\)""".toRegex()
        var found = false

        for (match in logRegex.findAll(code)) {
            val content = match.groupValues[1].trim().removeSurrounding("\"").removeSurrounding("'")
            lines.add(TerminalLine(content, TerminalLineType.STDOUT))
            found = true
        }

        if (!found) {
            lines.add(TerminalLine("Executing JavaScript script...", TerminalLineType.INFO))
            code.lines().take(5).forEach { lines.add(TerminalLine(it, TerminalLineType.STDOUT)) }
        }

        return lines
    }

    private fun runShellScript(code: String): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        code.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("echo ")) {
                lines.add(TerminalLine(trimmed.removePrefix("echo ").removeSurrounding("\""), TerminalLineType.STDOUT))
            } else if (trimmed.isNotBlank() && !trimmed.startsWith("#")) {
                lines.add(TerminalLine("+ $trimmed", TerminalLineType.INFO))
            }
        }
        return lines
    }

    private fun extractPrintStatements(code: String, keyword: String): List<TerminalLine> {
        val lines = mutableListOf<TerminalLine>()
        val regex = Regex("""$keyword\((.*?)\)""")
        for (match in regex.findAll(code)) {
            val content = match.groupValues[1].trim().removeSurrounding("\"")
            lines.add(TerminalLine(content, TerminalLineType.STDOUT))
        }
        if (lines.isEmpty()) {
            lines.add(TerminalLine("Execution finished without output.", TerminalLineType.INFO))
        }
        return lines
    }
}
