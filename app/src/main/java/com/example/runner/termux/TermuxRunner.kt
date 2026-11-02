package com.example.runner.termux

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.runner.CodeRunner
import com.example.runner.RunEvent
import com.example.runner.RunRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Runner utilizing the Termux app via the RUN_COMMAND Intent API.
 * Provides real offline compilers and interpreters (Clang, GCC, OpenJDK, Rust, Go, Python, Node, etc.).
 */
class TermuxRunner(private val context: Context) : CodeRunner {

    override val id: String = "termux"
    override val displayName: String = "Termux Native Compiler (Local Device)"

    override val supportedLanguages: Set<String> = setOf(
        "Python", "JavaScript", "TypeScript", "Java", "Kotlin", "C", "C++",
        "Go", "Rust", "PHP", "Ruby", "Perl", "Lua", "R", "Bash", "Dart",
        "Swift", "Scala", "Haskell", "Elixir", "Julia", "Fortran", "Pascal", "Groovy"
    )

    private val isStopped = AtomicBoolean(false)

    companion object {
        const val TERMUX_PACKAGE = "com.termux"
        const val TERMUX_SERVICE = "com.termux.app.RunCommandService"
        const val PERMISSION_RUN_COMMAND = "com.termux.permission.RUN_COMMAND"

        // Termux RunCommandService intent extra keys
        const val EXTRA_RUN_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
        const val EXTRA_RUN_COMMAND_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
        const val EXTRA_RUN_COMMAND_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
        const val EXTRA_RUN_COMMAND_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
        const val EXTRA_RUN_COMMAND_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"
        const val EXTRA_RUN_COMMAND_PENDING_INTENT = "com.termux.RUN_COMMAND_PENDING_INTENT"

        fun isTermuxInstalled(context: Context): Boolean {
            return try {
                context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }

        fun hasRunCommandPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                PERMISSION_RUN_COMMAND
            ) == PackageManager.PERMISSION_GRANTED
        }

        fun openInTermuxTerminal(context: Context, command: String, workDir: String = "/data/data/com.termux/files/home") {
            try {
                val intent = Intent().apply {
                    component = ComponentName(TERMUX_PACKAGE, TERMUX_SERVICE)
                    action = "com.termux.RUN_COMMAND"
                    putExtra(EXTRA_RUN_COMMAND_PATH, "/data/data/com.termux/files/usr/bin/bash")
                    putExtra(EXTRA_RUN_COMMAND_ARGUMENTS, arrayOf("-c", command))
                    putExtra(EXTRA_RUN_COMMAND_WORKDIR, workDir)
                    putExtra(EXTRA_RUN_COMMAND_BACKGROUND, false)
                    putExtra(EXTRA_RUN_COMMAND_SESSION_ACTION, "0")
                }
                context.startService(intent)
            } catch (_: Exception) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE)
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                }
            }
        }
    }

    override suspend fun isAvailable(): Boolean {
        return isTermuxInstalled(context) && hasRunCommandPermission(context)
    }

    override fun run(request: RunRequest): Flow<RunEvent> = flow {
        isStopped.set(false)
        val startTime = System.currentTimeMillis()
        emit(RunEvent.Started(language = request.language, runner = displayName))

        if (!isTermuxInstalled(context)) {
            emit(RunEvent.Failure("Termux is not installed. Install Termux to run real local compilers/interpreters offline."))
            return@flow
        }

        if (!hasRunCommandPermission(context)) {
            emit(RunEvent.Failure("Permission 'com.termux.permission.RUN_COMMAND' not granted. Enable Termux integration in Settings."))
            return@flow
        }

        val executionId = UUID.randomUUID().toString()

        // Prepare working workspace directory inside app internal files which Termux can read if allowed,
        // or inside Termux accessible shared directory
        val workDir = File(context.filesDir, "termux_work").apply { mkdirs() }
        val targetFile = File(workDir, request.fileName)
        targetFile.writeText(request.code)

        // Build exact execution command
        val shellScript = buildExecutionScript(request, targetFile.name)

        emit(RunEvent.Stdout("[Termux] $shellScript\n"))

        // Create PendingIntent for callback
        val callbackIntent = Intent(context, TermuxResultReceiver::class.java).apply {
            action = TermuxResultReceiver.ACTION_TERMUX_RESULT
            putExtra(TermuxResultReceiver.EXTRA_EXECUTION_ID, executionId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            executionId.hashCode(),
            callbackIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        // Dispatch RUN_COMMAND intent to Termux service
        val intent = Intent().apply {
            component = ComponentName(TERMUX_PACKAGE, TERMUX_SERVICE)
            action = "com.termux.RUN_COMMAND"
            putExtra(EXTRA_RUN_COMMAND_PATH, "/data/data/com.termux/files/usr/bin/bash")
            putExtra(EXTRA_RUN_COMMAND_ARGUMENTS, arrayOf("-c", shellScript))
            putExtra(EXTRA_RUN_COMMAND_WORKDIR, workDir.absolutePath)
            putExtra(EXTRA_RUN_COMMAND_BACKGROUND, true)
            putExtra(EXTRA_RUN_COMMAND_PENDING_INTENT, pendingIntent)
        }

        try {
            context.startService(intent)
        } catch (e: Exception) {
            emit(RunEvent.Failure("Failed to dispatch command to Termux: ${e.message}"))
            return@flow
        }

        // Wait for broadcast result with timeout
        val timeoutMs = request.timeoutMs.coerceIn(5_000L, 120_000L)
        val termuxResult = withTimeoutOrNull(timeoutMs) {
            var matchingResult: TermuxExecutionResult? = null
            TermuxResultReceiver.resultsFlow.collect { res ->
                if (res.executionId == executionId) {
                    matchingResult = res
                    return@collect
                }
            }
            matchingResult
        }

        val elapsed = System.currentTimeMillis() - startTime

        if (termuxResult == null) {
            if (isStopped.get()) {
                emit(RunEvent.Stderr("Termux execution stopped by user."))
                emit(RunEvent.Exit(code = 130, durationMs = elapsed))
            } else {
                emit(RunEvent.Stderr("Termux execution timed out after ${timeoutMs / 1000}s."))
                emit(RunEvent.Exit(code = 124, durationMs = elapsed))
            }
        } else {
            if (termuxResult.stdout.isNotEmpty()) {
                emit(RunEvent.Stdout(termuxResult.stdout))
            }
            if (termuxResult.stderr.isNotEmpty()) {
                emit(RunEvent.Stderr(termuxResult.stderr))
            }
            if (termuxResult.errmsg != null && termuxResult.errCode != 0) {
                emit(RunEvent.Stderr("Termux Error (${termuxResult.errCode}): ${termuxResult.errmsg}"))
            }
            emit(RunEvent.Exit(code = termuxResult.exitCode, durationMs = elapsed))
        }

    }.flowOn(Dispatchers.IO)

    override fun stop() {
        isStopped.set(true)
    }

    private fun buildExecutionScript(request: RunRequest, fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        val stdinPipe = if (!request.stdin.isNullOrEmpty()) {
            val escapedStdin = request.stdin.replace("'", "'\\''")
            "printf '%s' '$escapedStdin' | "
        } else ""

        val cmd = when (ext) {
            "py", "pyw" -> "python3 $fileName"
            "js", "mjs" -> "node $fileName"
            "ts", "tsx" -> "npx tsx $fileName || (tsc $fileName && node ${fileName.removeSuffix(".ts")}.js)"
            "java" -> {
                val className = fileName.removeSuffix(".java")
                "javac $fileName && java $className"
            }
            "kt", "kts" -> "kotlinc $fileName -include-runtime -d out.jar && java -jar out.jar"
            "c" -> "clang $fileName -o a.out && ./a.out"
            "cpp", "cc", "cxx" -> "clang++ $fileName -o a.out && ./a.out"
            "go" -> "go run $fileName"
            "rs" -> "rustc $fileName -o a.out && ./a.out"
            "php" -> "php $fileName"
            "rb" -> "ruby $fileName"
            "pl" -> "perl $fileName"
            "lua" -> "lua $fileName"
            "r" -> "Rscript $fileName"
            "sh", "bash" -> "bash $fileName"
            "dart" -> "dart run $fileName"
            "swift" -> "swift $fileName"
            "scala" -> "scala $fileName"
            "hs" -> "runghc $fileName"
            "ex", "exs" -> "elixir $fileName"
            "jl" -> "julia $fileName"
            "f90", "f95" -> "gfortran $fileName -o a.out && ./a.out"
            "pas" -> "fpc $fileName && ./a.out"
            "groovy" -> "groovy $fileName"
            else -> "cat $fileName"
        }

        return "$stdinPipe$cmd"
    }
}
