package com.example.runner

/**
 * Intelligent Java source code inspector.
 * Accurately extracts the target compilation file name and class name for arbitrary Java files,
 * avoiding javac "class X is public, should be declared in a file named X.java" mismatch errors.
 */
object JavaSourceParser {

    data class JavaFileInfo(
        val targetFileName: String, // e.g. "MyProgram.java"
        val className: String       // e.g. "MyProgram"
    )

    /**
     * Resolves the target Java file name and class name:
     * 1. Parse for "public (class|interface|enum|record) X"
     * 2. If no public class, find the class containing "public static void main"
     * 3. If neither exists, default to Main.java
     */
    fun resolveJavaFileInfo(code: String, fallbackFileName: String = "Main.java"): JavaFileInfo {
        // Strip block and line comments to avoid false matches in comments
        val cleanCode = code
            .replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), " ")
            .replace(Regex("""//.*"""), " ")

        // 1. Check for public (class|interface|enum|record) X
        val publicPattern = Regex("""\bpublic\s+(?:final\s+|abstract\s+|sealed\s+|non-sealed\s+|static\s+)*(?:class|interface|enum|record)\s+([A-Za-z0-9_$]+)""")
        publicPattern.find(cleanCode)?.let { match ->
            val cls = match.groupValues[1].trim()
            if (cls.isNotEmpty()) {
                return JavaFileInfo(targetFileName = "$cls.java", className = cls)
            }
        }

        // 2. Class containing public static void main
        val classWithMainPattern = Regex("""\b(?:class|record)\s+([A-Za-z0-9_$]+)\s*\{[^}]*?\bpublic\s+static\s+void\s+main\b""", setOf(RegexOption.DOT_MATCHES_ALL))
        classWithMainPattern.find(cleanCode)?.let { match ->
            val cls = match.groupValues[1].trim()
            if (cls.isNotEmpty()) {
                return JavaFileInfo(targetFileName = "$cls.java", className = cls)
            }
        }

        // 3. Any class declaration if main method is present anywhere
        if (cleanCode.contains("public static void main")) {
            val anyClass = Regex("""\b(?:class|record)\s+([A-Za-z0-9_$]+)""").find(cleanCode)
            if (anyClass != null) {
                val cls = anyClass.groupValues[1].trim()
                if (cls.isNotEmpty()) {
                    return JavaFileInfo(targetFileName = "$cls.java", className = cls)
                }
            }
        }

        // 4. Default fallback: sanitize fallbackFileName or return Main.java
        val fallbackBase = fallbackFileName.substringBeforeLast('.').trim()
        val sanitized = fallbackBase.replace(Regex("[^A-Za-z0-9_$]"), "_").ifEmpty { "Main" }
        return JavaFileInfo(targetFileName = "$sanitized.java", className = sanitized)
    }

    /**
     * Sanitizes file names for remote execution environments across any language,
     * preserving extension while cleaning unsafe characters for sandbox shells.
     */
    fun sanitizeFileNameForSandbox(fileName: String, defaultName: String): String {
        val clean = fileName.trim().ifEmpty { defaultName }
        val ext = clean.substringAfterLast('.', "")
        val base = clean.substringBeforeLast('.', clean)
        val safeBase = base.replace(' ', '_').replace(Regex("""[^a-zA-Z0-9_\-]"""), "_").ifEmpty { "file" }
        return if (ext.isNotEmpty()) "$safeBase.$ext" else safeBase
    }
}
