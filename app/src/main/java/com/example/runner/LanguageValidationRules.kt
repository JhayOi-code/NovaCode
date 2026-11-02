package com.example.runner

/**
 * Structural language validation rules for compiled and structured languages.
 * Detects mismatched entrypoints or class names and provides clear, actionable compiler guidance.
 */
object LanguageValidationRules {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateCode(language: String, fileName: String, code: String): ValidationResult {
        val trimmed = code.trim()
        val lang = language.lowercase()

        when (lang) {
            "java" -> {
                if (trimmed.isEmpty()) {
                    return ValidationResult(isValid = false, errorMessage = "Java source file is empty.")
                }
                // Check for main method entry point
                val hasMain = code.contains("public static void main") ||
                        code.contains("void main(String[]") ||
                        code.contains("void main(String...") ||
                        code.contains("static void main(")
                if (!hasMain) {
                    val baseName = fileName.substringBeforeLast('.')
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Java Error: Main method not found in class '$baseName'.\nPlease define the entry point:\n  public static void main(String[] args) {\n      // your code\n  }"
                    )
                }
            }
            "c", "c++" -> {
                val hasMain = code.contains("int main(") || code.contains("int main ()") || code.contains("void main(")
                if (!hasMain) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Undefined reference to 'main': In C/C++, a program must contain an 'int main()' entry point.\nExample:\nint main() {\n    // your code\n    return 0;\n}"
                    )
                }
            }
            "kotlin" -> {
                val hasMain = code.contains("fun main(") || code.contains("fun main()")
                if (!hasMain && !fileName.endsWith(".kts")) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "Kotlin Error: Entry point 'fun main()' not found in $fileName.\nExample:\nfun main() {\n    println(\"Hello World\")\n}"
                    )
                }
            }
            "go" -> {
                val hasPackageMain = code.contains("package main")
                val hasFuncMain = code.contains("func main()")
                if (!hasPackageMain) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "go run: cannot run non-main package (missing 'package main' at line 1)"
                    )
                }
                if (!hasFuncMain) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "go run: function main is undeclared in the main package"
                    )
                }
            }
            "rust" -> {
                val hasMain = code.contains("fn main()")
                if (!hasMain) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "error[E0601]: `main` function not found in crate `$fileName`\nConsider adding `fn main() { ... }`"
                    )
                }
            }
            "csharp", "c#" -> {
                val hasMain = code.contains("Main(") || code.contains("Main (")
                if (!hasMain) {
                    return ValidationResult(
                        isValid = false,
                        errorMessage = "CS5001: Program does not contain a static 'Main' method suitable for an entry point"
                    )
                }
            }
        }

        return ValidationResult(isValid = true)
    }
}
