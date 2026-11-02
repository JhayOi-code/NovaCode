package com.example.runner

/**
 * Metadata descriptor for a language runtime in remote execution services (Piston / Judge0).
 */
data class RemoteLanguageRuntime(
    val language: String,
    val aliases: List<String>,
    val defaultVersion: String = "*",
    val defaultFileName: String,
    val runCommandHint: String
)

/**
 * Centralized mapping of file extensions, shebangs, and language identifiers to remote execution runtimes.
 * Allows adding new languages in a single place without modifying runner execution logic.
 */
object RemoteLanguageConfig {

    private val runtimeDefinitions = listOf(
        RemoteLanguageRuntime(
            language = "python",
            aliases = listOf("py", "python3", "pyw"),
            defaultVersion = "3.10.0",
            defaultFileName = "main.py",
            runCommandHint = "python main.py"
        ),
        RemoteLanguageRuntime(
            language = "javascript",
            aliases = listOf("js", "node", "mjs", "cjs"),
            defaultVersion = "18.15.0",
            defaultFileName = "index.js",
            runCommandHint = "node index.js"
        ),
        RemoteLanguageRuntime(
            language = "typescript",
            aliases = listOf("ts", "tsx"),
            defaultVersion = "5.0.3",
            defaultFileName = "index.ts",
            runCommandHint = "npx tsx index.ts"
        ),
        RemoteLanguageRuntime(
            language = "java",
            aliases = listOf("java"),
            defaultVersion = "15.0.2",
            defaultFileName = "Main.java",
            runCommandHint = "javac Main.java && java Main"
        ),
        RemoteLanguageRuntime(
            language = "kotlin",
            aliases = listOf("kt", "kts"),
            defaultVersion = "1.8.20",
            defaultFileName = "main.kt",
            runCommandHint = "kotlinc main.kt -include-runtime -d out.jar && java -jar out.jar"
        ),
        RemoteLanguageRuntime(
            language = "c",
            aliases = listOf("c", "h"),
            defaultVersion = "10.2.0",
            defaultFileName = "main.c",
            runCommandHint = "gcc main.c -o main && ./main"
        ),
        RemoteLanguageRuntime(
            language = "c++",
            aliases = listOf("cpp", "cc", "cxx", "hpp"),
            defaultVersion = "10.2.0",
            defaultFileName = "main.cpp",
            runCommandHint = "g++ main.cpp -o main && ./main"
        ),
        RemoteLanguageRuntime(
            language = "csharp",
            aliases = listOf("cs", "c#", "dotnet"),
            defaultVersion = "6.12.0",
            defaultFileName = "Program.cs",
            runCommandHint = "dotnet run"
        ),
        RemoteLanguageRuntime(
            language = "go",
            aliases = listOf("go"),
            defaultVersion = "1.16.2",
            defaultFileName = "main.go",
            runCommandHint = "go run main.go"
        ),
        RemoteLanguageRuntime(
            language = "rust",
            aliases = listOf("rs"),
            defaultVersion = "1.68.2",
            defaultFileName = "main.rs",
            runCommandHint = "rustc main.rs -o main && ./main"
        ),
        RemoteLanguageRuntime(
            language = "php",
            aliases = listOf("php"),
            defaultVersion = "8.2.3",
            defaultFileName = "index.php",
            runCommandHint = "php index.php"
        ),
        RemoteLanguageRuntime(
            language = "ruby",
            aliases = listOf("rb", "ruby"),
            defaultVersion = "3.0.1",
            defaultFileName = "main.rb",
            runCommandHint = "ruby main.rb"
        ),
        RemoteLanguageRuntime(
            language = "perl",
            aliases = listOf("pl"),
            defaultVersion = "5.36.0",
            defaultFileName = "main.pl",
            runCommandHint = "perl main.pl"
        ),
        RemoteLanguageRuntime(
            language = "lua",
            aliases = listOf("lua"),
            defaultVersion = "5.4.4",
            defaultFileName = "main.lua",
            runCommandHint = "lua main.lua"
        ),
        RemoteLanguageRuntime(
            language = "bash",
            aliases = listOf("sh", "bash", "shell"),
            defaultVersion = "5.2.0",
            defaultFileName = "script.sh",
            runCommandHint = "bash script.sh"
        ),
        RemoteLanguageRuntime(
            language = "dart",
            aliases = listOf("dart"),
            defaultVersion = "2.19.6",
            defaultFileName = "main.dart",
            runCommandHint = "dart run main.dart"
        ),
        RemoteLanguageRuntime(
            language = "swift",
            aliases = listOf("swift"),
            defaultVersion = "5.3.3",
            defaultFileName = "main.swift",
            runCommandHint = "swift main.swift"
        ),
        RemoteLanguageRuntime(
            language = "r",
            aliases = listOf("r"),
            defaultVersion = "4.1.1",
            defaultFileName = "main.R",
            runCommandHint = "Rscript main.R"
        ),
        RemoteLanguageRuntime(
            language = "scala",
            aliases = listOf("scala"),
            defaultVersion = "3.2.2",
            defaultFileName = "Main.scala",
            runCommandHint = "scala Main.scala"
        ),
        RemoteLanguageRuntime(
            language = "haskell",
            aliases = listOf("hs", "lhs"),
            defaultVersion = "9.0.1",
            defaultFileName = "main.hs",
            runCommandHint = "runghc main.hs"
        ),
        RemoteLanguageRuntime(
            language = "elixir",
            aliases = listOf("ex", "exs"),
            defaultVersion = "1.11.3",
            defaultFileName = "main.exs",
            runCommandHint = "elixir main.exs"
        ),
        RemoteLanguageRuntime(
            language = "julia",
            aliases = listOf("jl"),
            defaultVersion = "1.8.5",
            defaultFileName = "main.jl",
            runCommandHint = "julia main.jl"
        ),
        RemoteLanguageRuntime(
            language = "fortran",
            aliases = listOf("f90", "f95", "f03", "f"),
            defaultVersion = "10.2.0",
            defaultFileName = "main.f90",
            runCommandHint = "gfortran main.f90 -o main && ./main"
        ),
        RemoteLanguageRuntime(
            language = "pascal",
            aliases = listOf("pas", "pp"),
            defaultVersion = "3.2.2",
            defaultFileName = "main.pas",
            runCommandHint = "fpc main.pas && ./main"
        ),
        RemoteLanguageRuntime(
            language = "groovy",
            aliases = listOf("groovy", "gvy"),
            defaultVersion = "3.0.7",
            defaultFileName = "main.groovy",
            runCommandHint = "groovy main.groovy"
        ),
        RemoteLanguageRuntime(
            language = "sqlite3",
            aliases = listOf("sql", "sqlite"),
            defaultVersion = "3.36.0",
            defaultFileName = "query.sql",
            runCommandHint = "sqlite3 < query.sql"
        )
    )

    /** Map from extension/alias to RemoteLanguageRuntime */
    private val extensionMap: Map<String, RemoteLanguageRuntime> = buildMap {
        for (runtime in runtimeDefinitions) {
            put(runtime.language.lowercase(), runtime)
            for (alias in runtime.aliases) {
                put(alias.lowercase(), runtime)
            }
        }
    }

    /**
     * Resolves the remote service runtime for a given file name or explicit language identifier.
     */
    fun resolveRuntime(fileName: String, explicitLanguage: String? = null): RemoteLanguageRuntime? {
        if (!explicitLanguage.isNullOrBlank()) {
            extensionMap[explicitLanguage.trim().lowercase()]?.let { return it }
        }
        val ext = fileName.substringAfterLast('.', "").lowercase()
        if (ext.isNotEmpty()) {
            extensionMap[ext]?.let { return it }
        }
        return null
    }

    /**
     * Returns all configured runtimes.
     */
    fun getAllRuntimes(): List<RemoteLanguageRuntime> = runtimeDefinitions

    /**
     * Resolves the canonical language identifier for a file name and optional code snippet.
     */
    fun resolveLanguageForFile(fileName: String, codeSnippet: String = ""): String {
        return normalizeLanguage(fileName, codeSnippet).lowercase()
    }

    /**
     * Normalizes a file extension to a display-ready language name.
     */
    fun normalizeLanguage(fileName: String, codeSnippet: String = ""): String {
        // 1. Check shebang if present
        if (codeSnippet.startsWith("#!")) {
            val firstLine = codeSnippet.substringBefore('\n').lowercase()
            when {
                firstLine.contains("python") -> return "Python"
                firstLine.contains("node") || firstLine.contains("javascript") -> return "JavaScript"
                firstLine.contains("bash") || firstLine.contains("sh") -> return "Bash"
                firstLine.contains("ruby") -> return "Ruby"
                firstLine.contains("perl") -> return "Perl"
                firstLine.contains("php") -> return "PHP"
            }
        }

        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "py", "pyw" -> "Python"
            "js", "mjs", "cjs" -> "JavaScript"
            "ts", "tsx" -> "TypeScript"
            "java" -> "Java"
            "kt", "kts" -> "Kotlin"
            "c", "h" -> "C"
            "cpp", "cc", "cxx", "hpp" -> "C++"
            "cs" -> "C#"
            "go" -> "Go"
            "rs" -> "Rust"
            "php" -> "PHP"
            "rb" -> "Ruby"
            "pl" -> "Perl"
            "lua" -> "Lua"
            "sh", "bash" -> "Bash"
            "sql" -> "SQL"
            "html", "htm" -> "HTML"
            "css" -> "CSS"
            "json" -> "JSON"
            "md", "markdown" -> "Markdown"
            "dart" -> "Dart"
            "swift" -> "Swift"
            "r" -> "R"
            "scala" -> "Scala"
            "hs" -> "Haskell"
            "ex", "exs" -> "Elixir"
            "jl" -> "Julia"
            "f90", "f95" -> "Fortran"
            "pas" -> "Pascal"
            "groovy" -> "Groovy"
            "xml" -> "XML"
            "csv" -> "CSV"
            "txt" -> "Text"
            else -> "Plain Text"
        }
    }
}
