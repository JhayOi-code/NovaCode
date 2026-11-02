package com.example.runner.termux

import android.content.Context

data class TermuxToolStatus(
    val name: String,
    val command: String,
    val packageName: String,
    val installCommand: String,
    val isInstalled: Boolean,
    val versionInfo: String? = null
)

object TermuxToolChecker {

    val supportedTools = listOf(
        TermuxToolStatus("Python 3", "python", "python", "pkg install -y python", false),
        TermuxToolStatus("Node.js / npm", "node", "nodejs", "pkg install -y nodejs", false),
        TermuxToolStatus("TypeScript compiler", "tsc", "nodejs-lts", "npm install -g typescript tsx", false),
        TermuxToolStatus("C / C++ (Clang)", "clang", "clang", "pkg install -y clang", false),
        TermuxToolStatus("Java (OpenJDK)", "javac", "openjdk-21", "pkg install -y openjdk-21", false),
        TermuxToolStatus("Kotlin Compiler", "kotlinc", "kotlin", "pkg install -y kotlin", false),
        TermuxToolStatus("Rust (rustc/cargo)", "rustc", "rust", "pkg install -y rust", false),
        TermuxToolStatus("Go Language", "go", "golang", "pkg install -y golang", false),
        TermuxToolStatus("PHP Runtime", "php", "php", "pkg install -y php", false),
        TermuxToolStatus("Ruby Runtime", "ruby", "ruby", "pkg install -y ruby", false),
        TermuxToolStatus("Lua Engine", "lua", "lua54", "pkg install -y lua54", false),
        TermuxToolStatus("R Programming", "Rscript", "r-base", "pkg install -y r-base", false),
        TermuxToolStatus("Dart SDK", "dart", "dart", "pkg install -y dart", false),
        TermuxToolStatus("Perl Runtime", "perl", "perl", "pkg install -y perl", false)
    )

    fun checkInstalledTools(context: Context): List<TermuxToolStatus> {
        val isInstalled = TermuxRunner.isTermuxInstalled(context)
        val hasPermission = TermuxRunner.hasRunCommandPermission(context)

        return supportedTools.map { tool ->
            // Check if tool binary exists in standard Termux paths if accessible,
            // or return estimated state
            val binaryPath = "/data/data/com.termux/files/usr/bin/${tool.command}"
            val exists = java.io.File(binaryPath).exists()
            tool.copy(isInstalled = exists && isInstalled && hasPermission)
        }
    }
}
