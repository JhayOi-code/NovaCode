package com.example.runner

import android.content.Context
import com.example.model.CodeFile
import com.example.runner.embedded.EmbeddedEngineRunner
import com.example.runner.embedded.EmbeddedFormatRunner
import com.example.runner.embedded.EmbeddedHtmlRunner
import com.example.runner.embedded.EmbeddedJsRunner
import com.example.runner.embedded.EmbeddedSqlRunner
import com.example.runner.remote.RemotePistonRunner
import com.example.runner.termux.TermuxRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Strategy for prioritizing which runner tier to attempt first.
 */
enum class RunnerTierPreference {
    EMBEDDED_FIRST,
    TERMUX_FIRST,
    REMOTE_FIRST
}

/**
 * Result returned when querying [RunnerRegistry] for an appropriate runner.
 */
sealed class RunnerResolution {
    data class Found(val runner: CodeRunner, val language: String) : RunnerResolution()
    data class Unavailable(val language: String, val reason: String, val setupAction: String) : RunnerResolution()
}

/**
 * Registry mapping file extensions and languages to an ordered list of CodeRunners.
 * Automatically selects the highest-priority runner currently available on the device.
 */
class RunnerRegistry(
    private val context: Context,
    var openFilesProvider: () -> List<CodeFile> = { emptyList() }
) {

    // Registered runner instances
    val embeddedEngineRunner = EmbeddedEngineRunner(context.applicationContext, { openFilesProvider() })
    val embeddedSqlRunner = EmbeddedSqlRunner()
    val embeddedJsRunner = EmbeddedJsRunner(context.applicationContext)
    val embeddedHtmlRunner = EmbeddedHtmlRunner(context.applicationContext)
    val embeddedFormatRunner = EmbeddedFormatRunner()
    val termuxRunner = TermuxRunner(context.applicationContext)
    val remoteRunner = RemotePistonRunner()

    var tierPreference: RunnerTierPreference = RunnerTierPreference.EMBEDDED_FIRST

    fun setRemoteConfig(baseUrl: String, apiKey: String) {
        remoteRunner.updateConfig(baseUrl, apiKey)
    }

    /**
     * Resolves the best available runner for the given file and code snippet.
     */
    suspend fun resolveRunner(
        fileName: String,
        codeSnippet: String = "",
        explicitLanguage: String? = null
    ): RunnerResolution = withContext(Dispatchers.IO) {
        val language = explicitLanguage ?: RemoteLanguageConfig.normalizeLanguage(fileName, codeSnippet)
        val ext = fileName.substringAfterLast('.', "").lowercase()

        // 1. Build candidates list according to tier priority and language support
        val candidates = getCandidatesForLanguage(language, ext)

        // 2. Pick the first available runner
        for (runner in candidates) {
            try {
                if (runner.isAvailable()) {
                    return@withContext RunnerResolution.Found(runner, language)
                }
            } catch (_: Exception) {}
        }

        // 3. Fallback to universal embedded engine
        if (embeddedEngineRunner.isAvailable()) {
            return@withContext RunnerResolution.Found(embeddedEngineRunner, language)
        }

        val (reason, setupAction) = getMissingSetupGuidance(language)
        return@withContext RunnerResolution.Unavailable(language, reason, setupAction)
    }

    private fun getCandidatesForLanguage(language: String, ext: String): List<CodeRunner> {
        val list = mutableListOf<CodeRunner>()

        when (language.lowercase()) {
            "sql" -> {
                when (tierPreference) {
                    RunnerTierPreference.EMBEDDED_FIRST -> {
                        list.add(embeddedSqlRunner)
                        list.add(embeddedEngineRunner)
                        list.add(termuxRunner)
                        list.add(remoteRunner)
                    }
                    RunnerTierPreference.TERMUX_FIRST -> {
                        list.add(termuxRunner)
                        list.add(embeddedSqlRunner)
                        list.add(embeddedEngineRunner)
                        list.add(remoteRunner)
                    }
                    RunnerTierPreference.REMOTE_FIRST -> {
                        list.add(remoteRunner)
                        list.add(embeddedSqlRunner)
                        list.add(embeddedEngineRunner)
                        list.add(termuxRunner)
                    }
                }
            }
            "html", "css" -> {
                list.add(embeddedHtmlRunner)
                list.add(embeddedEngineRunner)
            }
            "json", "markdown", "xml", "csv", "text", "plain text" -> {
                list.add(embeddedFormatRunner)
                list.add(embeddedEngineRunner)
            }
            "javascript", "typescript", "js", "ts" -> {
                when (tierPreference) {
                    RunnerTierPreference.EMBEDDED_FIRST -> {
                        list.add(embeddedJsRunner)
                        list.add(embeddedEngineRunner)
                        list.add(termuxRunner)
                        list.add(remoteRunner)
                    }
                    RunnerTierPreference.TERMUX_FIRST -> {
                        list.add(termuxRunner)
                        list.add(embeddedJsRunner)
                        list.add(embeddedEngineRunner)
                        list.add(remoteRunner)
                    }
                    RunnerTierPreference.REMOTE_FIRST -> {
                        list.add(remoteRunner)
                        list.add(embeddedJsRunner)
                        list.add(embeddedEngineRunner)
                        list.add(termuxRunner)
                    }
                }
            }
            else -> {
                // Python, Java, Kotlin, C, C++, Rust, Go, PHP, Ruby, Lua, Bash, etc.
                when (tierPreference) {
                    RunnerTierPreference.EMBEDDED_FIRST -> {
                        list.add(embeddedEngineRunner)
                        list.add(termuxRunner)
                        list.add(remoteRunner)
                    }
                    RunnerTierPreference.TERMUX_FIRST -> {
                        list.add(termuxRunner)
                        list.add(embeddedEngineRunner)
                        list.add(remoteRunner)
                    }
                    RunnerTierPreference.REMOTE_FIRST -> {
                        list.add(remoteRunner)
                        list.add(embeddedEngineRunner)
                        list.add(termuxRunner)
                    }
                }
            }
        }
        return list
    }

    private fun getMissingSetupGuidance(language: String): Pair<String, String> {
        return when (language.lowercase()) {
            "c", "c++" -> Pair(
                "Neither Termux with 'clang' nor remote cloud execution is available.",
                "Connect to the internet for remote execution or install Termux and run 'pkg install clang'."
            )
            "java" -> Pair(
                "Neither Termux with OpenJDK nor remote execution is available.",
                "Connect to the internet or install Termux and run 'pkg install openjdk-21'."
            )
            "python" -> Pair(
                "No Python runner available.",
                "Connect to the internet or install Termux and run 'pkg install python'."
            )
            "rust" -> Pair(
                "No Rust compiler available.",
                "Connect to the internet or install Termux and run 'pkg install rust'."
            )
            "go" -> Pair(
                "No Go compiler available.",
                "Connect to the internet or install Termux and run 'pkg install golang'."
            )
            else -> Pair(
                "Language '$language' requires an active internet connection or Termux environment.",
                "Connect to the internet for remote execution or install the matching package in Termux."
            )
        }
    }
}
