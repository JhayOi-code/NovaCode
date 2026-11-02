package com.example.runner.remote

import com.example.runner.CodeRunner
import com.example.runner.JavaSourceParser
import com.example.runner.RemoteLanguageConfig
import com.example.runner.RunEvent
import com.example.runner.RunRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * High-performance Remote Code Execution Runner supporting 40+ languages via Piston and Judge0 CE API.
 * Accurately processes arbitrary filenames, auto-corrects Java class entrypoints silently,
 * trims and validates API keys, and seamlessly falls back to free keyless sandboxes.
 */
class RemotePistonRunner(
    private var baseUrl: String = DEFAULT_BASE_URL,
    private var apiKey: String = ""
) : CodeRunner {

    override val id: String = "remote_piston"
    override val displayName: String = "Cloud Sandbox"

    override val supportedLanguages: Set<String> by lazy {
        RemoteLanguageConfig.getAllRuntimes().flatMap { listOf(it.language) + it.aliases }.map { it.lowercase() }.toSet()
    }

    private val isStopped = AtomicBoolean(false)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        const val DEFAULT_BASE_URL = "https://emkc.org/api/v2/piston"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    fun updateConfig(url: String, key: String) {
        baseUrl = if (url.trim().isNotEmpty()) url.trim().removeSuffix("/") else DEFAULT_BASE_URL
        apiKey = key.trim()
    }

    override suspend fun isAvailable(): Boolean {
        return try {
            val endpoint = if (baseUrl.endsWith("/execute")) baseUrl.removeSuffix("/execute") else baseUrl
            val runtimesUrl = if (endpoint.endsWith("/api/v2/piston")) "$endpoint/runtimes" else "$endpoint/api/v2/piston/runtimes"
            val reqBuilder = Request.Builder().url(runtimesUrl).get()
            val trimmedKey = apiKey.trim()
            if (trimmedKey.isNotEmpty()) {
                reqBuilder.addHeader("Authorization", trimmedKey)
                reqBuilder.addHeader("X-API-KEY", trimmedKey)
            }
            httpClient.newCall(reqBuilder.build()).execute().use { response ->
                response.isSuccessful || response.code == 401 || response.code == 429
            }
        } catch (_: Exception) {
            true
        }
    }

    override fun run(request: RunRequest): Flow<RunEvent> = flow {
        isStopped.set(false)
        val startTime = System.currentTimeMillis()

        val runtime = RemoteLanguageConfig.resolveRuntime(request.fileName, request.language)
        if (runtime == null) {
            emit(RunEvent.Failure("Language '${request.language}' is not supported yet for remote execution."))
            return@flow
        }

        emit(RunEvent.Started(language = runtime.language.replaceFirstChar { it.uppercase() }, runner = displayName))

        val userFileName = request.fileName.ifEmpty { runtime.defaultFileName }

        // 1. Silent, name-independent Java resolution & filename sanitization for any language
        val sandboxFileName: String
        val sandboxCode: String

        if (runtime.language.lowercase() == "java") {
            val javaInfo = JavaSourceParser.resolveJavaFileInfo(request.code, userFileName)
            sandboxFileName = javaInfo.targetFileName
            sandboxCode = request.code
        } else {
            sandboxFileName = JavaSourceParser.sanitizeFileNameForSandbox(userFileName, runtime.defaultFileName)
            sandboxCode = request.code
        }

        val trimmedKey = apiKey.trim()

        // 2. Handle missing API key on authenticated services: seamlessly execute via local on-device engine
        if (trimmedKey.isEmpty() && baseUrl.contains("emkc.org")) {
            val fallbackSuccess = try {
                executeWithJudge0(request, startTime, runtime, sandboxFileName, userFileName)
            } catch (_: Exception) {
                false
            }
            if (fallbackSuccess) {
                return@flow
            }
            val localResult = com.example.engine.CodeExecutionEngine.execute(
                com.example.model.CodeFile(name = userFileName, path = "/workspace/$userFileName", content = request.code, language = request.language),
                emptyList()
            )
            for (line in localResult.output) {
                if (localResult.isSuccess) emit(RunEvent.Stdout(line)) else emit(RunEvent.Stderr(line))
            }
            emit(RunEvent.Exit(code = localResult.exitCode, durationMs = localResult.executionTimeMs))
            return@flow
        }

        // 3. Build Piston request payload with real filename
        val payload = JSONObject().apply {
            put("language", runtime.language)
            put("version", runtime.defaultVersion)

            val filesArray = JSONArray()
            filesArray.put(JSONObject().apply {
                put("name", sandboxFileName)
                put("content", sandboxCode)
            })
            put("files", filesArray)

            if (!request.stdin.isNullOrEmpty()) {
                put("stdin", request.stdin)
            }
            if (request.args.isNotEmpty()) {
                put("args", JSONArray(request.args))
            }
            put("compile_timeout", 15000)
            put("run_timeout", request.timeoutMs.coerceIn(5000L, 60000L))
        }

        val executeUrl = if (baseUrl.endsWith("/execute")) baseUrl else "$baseUrl/execute"
        val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)

        val reqBuilder = Request.Builder()
            .url(executeUrl)
            .post(requestBody)

        if (trimmedKey.isNotEmpty()) {
            reqBuilder.addHeader("Authorization", trimmedKey)
            reqBuilder.addHeader("X-API-KEY", trimmedKey)
        }

        try {
            val call = httpClient.newCall(reqBuilder.build())
            val response = call.execute()

            if (isStopped.get()) {
                val elapsed = System.currentTimeMillis() - startTime
                emit(RunEvent.Stderr("Execution stopped by user."))
                emit(RunEvent.Exit(code = 130, durationMs = elapsed))
                return@flow
            }

            val responseBody = response.body?.string() ?: ""
            val statusCode = response.code

            when (statusCode) {
                429 -> {
                    emit(RunEvent.Stderr("Remote Rate Limit (HTTP 429): Rate limit reached. Trying keyless backup sandbox..."))
                    val fallbackOk = try {
                        executeWithJudge0(request, startTime, runtime, sandboxFileName, userFileName)
                    } catch (_: Exception) { false }
                    if (fallbackOk) return@flow

                    emit(RunEvent.Stderr("Public rate limit exceeded. Please wait a few seconds or run locally in Termux."))
                    emit(RunEvent.Exit(code = 429, durationMs = System.currentTimeMillis() - startTime))
                    return@flow
                }
                401, 403 -> {
                    // Try keyless Judge0 CE fallback automatically
                    val fallbackSuccess = try {
                        executeWithJudge0(request, startTime, runtime, sandboxFileName, userFileName)
                    } catch (_: Exception) {
                        false
                    }
                    if (fallbackSuccess) {
                        return@flow
                    }
                    val localResult = com.example.engine.CodeExecutionEngine.execute(
                        com.example.model.CodeFile(name = userFileName, path = "/workspace/$userFileName", content = request.code, language = request.language),
                        emptyList()
                    )
                    for (line in localResult.output) {
                        if (localResult.isSuccess) emit(RunEvent.Stdout(line)) else emit(RunEvent.Stderr(line))
                    }
                    emit(RunEvent.Exit(code = localResult.exitCode, durationMs = localResult.executionTimeMs))
                    return@flow
                }
                in 400..499 -> {
                    emit(RunEvent.Stderr("Remote Request Error (HTTP $statusCode): $responseBody"))
                    emit(RunEvent.Exit(code = statusCode, durationMs = System.currentTimeMillis() - startTime))
                    return@flow
                }
                in 500..599 -> {
                    // Try keyless fallback
                    val fallbackOk = try {
                        executeWithJudge0(request, startTime, runtime, sandboxFileName, userFileName)
                    } catch (_: Exception) { false }
                    if (fallbackOk) return@flow

                    emit(RunEvent.Stderr("Remote Server Error (HTTP $statusCode): Service unavailable. Try local Termux."))
                    emit(RunEvent.Exit(code = statusCode, durationMs = System.currentTimeMillis() - startTime))
                    return@flow
                }
            }

            // Parse Piston response JSON
            val json = JSONObject(responseBody)

            // 1. Compile stage output
            if (json.has("compile")) {
                val compileObj = json.getJSONObject("compile")
                val compileStdout = compileObj.optString("stdout", "")
                val compileStderr = compileObj.optString("stderr", "")
                val compileCode = compileObj.optInt("code", 0)

                if (compileStdout.isNotEmpty()) {
                    emit(RunEvent.Stdout(remapOutput(compileStdout, sandboxFileName, userFileName)))
                }
                if (compileStderr.isNotEmpty()) {
                    emit(RunEvent.Stderr(remapOutput(compileStderr, sandboxFileName, userFileName)))
                }

                if (compileCode != 0) {
                    val elapsed = System.currentTimeMillis() - startTime
                    emit(RunEvent.Exit(code = compileCode, durationMs = elapsed))
                    return@flow
                }
            }

            // 2. Run stage output
            if (json.has("run")) {
                val runObj = json.getJSONObject("run")
                val runStdout = runObj.optString("stdout", "")
                val runStderr = runObj.optString("stderr", "")
                val exitCode = runObj.optInt("code", 0)
                val signal = runObj.optString("signal", "")

                if (runStdout.isNotEmpty()) {
                    emit(RunEvent.Stdout(remapOutput(runStdout, sandboxFileName, userFileName)))
                }
                if (runStderr.isNotEmpty()) {
                    emit(RunEvent.Stderr(remapOutput(runStderr, sandboxFileName, userFileName)))
                }
                if (signal.isNotEmpty() && signal != "null") {
                    emit(RunEvent.Stderr("Process terminated by signal: $signal"))
                }

                val elapsed = System.currentTimeMillis() - startTime
                emit(RunEvent.Exit(code = exitCode, durationMs = elapsed))
            } else {
                emit(RunEvent.Stdout(remapOutput(responseBody, sandboxFileName, userFileName)))
                emit(RunEvent.Exit(code = 0, durationMs = System.currentTimeMillis() - startTime))
            }

        } catch (e: UnknownHostException) {
            emit(RunEvent.Failure("No internet connection. Connect to Wi-Fi/data for remote execution, or install Termux for offline compiling."))
        } catch (e: IOException) {
            if (isStopped.get()) {
                emit(RunEvent.Stderr("Execution cancelled."))
                emit(RunEvent.Exit(code = 130, durationMs = System.currentTimeMillis() - startTime))
            } else {
                // Try keyless fallback before giving up
                val fallbackOk = try {
                    executeWithJudge0(request, startTime, runtime, sandboxFileName, userFileName)
                } catch (_: Exception) { false }
                if (!fallbackOk) {
                    emit(RunEvent.Stderr("Connection Error: ${e.message ?: "Network timeout or connection refused."}"))
                    emit(RunEvent.Exit(code = 1, durationMs = System.currentTimeMillis() - startTime))
                }
            }
        } catch (e: Exception) {
            emit(RunEvent.Failure("Remote execution error: ${e.message ?: e.javaClass.simpleName}"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Open, keyless execution service (Judge0 CE public cluster).
     * Automatically adapts Java class names to prevent class mismatch errors.
     */
    private suspend fun kotlinx.coroutines.flow.FlowCollector<RunEvent>.executeWithJudge0(
        request: RunRequest,
        startTime: Long,
        runtime: com.example.runner.RemoteLanguageRuntime,
        sandboxFileName: String,
        userFileName: String
    ): Boolean {
        val langId = getJudge0LanguageId(runtime.language) ?: return false

        val preparedSource = if (runtime.language.lowercase() == "java") {
            val javaInfo = JavaSourceParser.resolveJavaFileInfo(request.code, userFileName)
            if (javaInfo.className != "Main") {
                // Demote public class to package-private and provide wrapper Main entrypoint
                val demoted = request.code.replace(
                    Regex("""\bpublic\s+(?:class|record)\s+""" + javaInfo.className),
                    "class ${javaInfo.className}"
                )
                """
                $demoted
                
                public class Main {
                    public static void main(String[] args) throws Throwable {
                        ${javaInfo.className}.main(args);
                    }
                }
                """.trimIndent()
            } else {
                request.code
            }
        } else {
            request.code
        }

        val payload = JSONObject().apply {
            put("source_code", preparedSource)
            put("language_id", langId)
            if (!request.stdin.isNullOrEmpty()) {
                put("stdin", request.stdin)
            }
        }

        val req = Request.Builder()
            .url("https://ce.judge0.com/submissions?base64_encoded=false&wait=true")
            .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        val response = httpClient.newCall(req).execute()
        val responseBody = response.body?.string() ?: ""
        if (!response.isSuccessful) return false

        val json = JSONObject(responseBody)
        val compileOutput = json.optString("compile_output", "")
        val stdout = json.optString("stdout", "")
        val stderr = json.optString("stderr", "")
        val message = json.optString("message", "")
        val status = json.optJSONObject("status")
        val statusId = status?.optInt("id", 3) ?: 3

        if (compileOutput.isNotEmpty() && compileOutput != "null") {
            emit(RunEvent.Stderr(remapOutput(compileOutput, sandboxFileName, userFileName)))
        }
        if (stdout.isNotEmpty() && stdout != "null") {
            emit(RunEvent.Stdout(remapOutput(stdout, sandboxFileName, userFileName)))
        }
        if (stderr.isNotEmpty() && stderr != "null") {
            emit(RunEvent.Stderr(remapOutput(stderr, sandboxFileName, userFileName)))
        }
        if (message.isNotEmpty() && message != "null") {
            emit(RunEvent.Stderr(remapOutput(message, sandboxFileName, userFileName)))
        }

        val elapsed = System.currentTimeMillis() - startTime
        val exitCode = if (statusId == 3) 0 else if (statusId == 6) 1 else statusId
        emit(RunEvent.Exit(code = exitCode, durationMs = elapsed))
        return true
    }

    /**
     * Remaps compiler and interpreter error locations from internal sandbox names (e.g. Main.java, generated names)
     * back to the user's active file name so error line badges and navigation work seamlessly.
     */
    private fun remapOutput(output: String, sandboxFileName: String, userFileName: String): String {
        var text = output
        if (sandboxFileName.isNotEmpty() && !sandboxFileName.equals(userFileName, ignoreCase = true)) {
            text = text.replace(sandboxFileName, userFileName)
        }
        if (userFileName.endsWith(".java", ignoreCase = true) && !userFileName.equals("Main.java", ignoreCase = true)) {
            text = text.replace("Main.java", userFileName)
        }
        return text
    }

    private fun getJudge0LanguageId(language: String): Int? {
        return when (language.lowercase()) {
            "python", "py", "python3" -> 71
            "javascript", "js", "node" -> 63
            "typescript", "ts" -> 74
            "java" -> 62
            "c" -> 50
            "cpp", "c++" -> 54
            "csharp", "c#" -> 51
            "go" -> 60
            "rust", "rs" -> 73
            "php" -> 68
            "ruby", "rb" -> 72
            "bash", "sh" -> 46
            "kotlin", "kt" -> 78
            "lua" -> 64
            "sql", "sqlite" -> 82
            "swift" -> 83
            "r" -> 80
            "perl" -> 85
            "scala" -> 81
            "haskell" -> 61
            "fortran" -> 59
            "pascal" -> 67
            "elixir" -> 57
            "julia" -> 65
            else -> null
        }
    }

    override fun stop() {
        isStopped.set(true)
    }
}
