package com.example.templates

object NewFileTemplates {

    fun getTemplateForFile(fileName: String): Pair<String, Int> {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val baseName = fileName.substringBeforeLast('.')
            .replace(Regex("[^a-zA-Z0-9_]"), "")
            .ifEmpty { "Main" }

        val className = baseName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        return when (extension) {
            "java" -> {
                // PART 1 exact specification:
                // public class Example {
                //     public static void main(String[] args) {
                //         
                //     }
                // }
                // Cursor on empty line inside main
                val content = "public class $className {\n    public static void main(String[] args) {\n        \n    }\n}\n"
                val cursorOffset = content.indexOf("        \n") + 8
                content to cursorOffset
            }
            "kt", "kts" -> {
                val content = "fun main() {\n    println(\"Hello from $baseName!\")\n}\n"
                val cursorOffset = content.indexOf("println")
                content to cursorOffset
            }
            "py" -> {
                val content = "def main():\n    print(\"Hello from $baseName!\")\n\nif __name__ == \"__main__\":\n    main()\n"
                val cursorOffset = content.indexOf("print")
                content to cursorOffset
            }
            "js" -> {
                val content = "// $fileName\nfunction main() {\n    console.log(\"Hello from $baseName!\");\n}\n\nmain();\n"
                val cursorOffset = content.indexOf("console.log")
                content to cursorOffset
            }
            "ts" -> {
                val content = "// $fileName\nfunction main(): void {\n    console.log(\"Hello from $baseName!\");\n}\n\nmain();\n"
                val cursorOffset = content.indexOf("console.log")
                content to cursorOffset
            }
            "html", "htm" -> {
                val content = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>$className</title>\n</head>\n<body>\n    <h1>Hello from $className</h1>\n</body>\n</html>\n"
                val cursorOffset = content.indexOf("<h1>")
                content to cursorOffset
            }
            "css" -> {
                val content = "/* $fileName */\n* {\n    box-sizing: border-box;\n    margin: 0;\n    padding: 0;\n}\n\nbody {\n    font-family: sans-serif;\n}\n"
                val cursorOffset = content.length
                content to cursorOffset
            }
            "sh", "bash" -> {
                val content = "#!/bin/bash\n\necho \"Running $baseName script...\"\n"
                val cursorOffset = content.length
                content to cursorOffset
            }
            "json" -> {
                val content = "{\n  \"name\": \"$baseName\",\n  \"version\": \"1.0.0\"\n}\n"
                val cursorOffset = content.indexOf("\"version\"")
                content to cursorOffset
            }
            "md" -> {
                val content = "# $className\n\nProject notes and documentation.\n"
                val cursorOffset = content.length
                content to cursorOffset
            }
            "c", "cpp" -> {
                val content = "#include <stdio.h>\n\nint main() {\n    printf(\"Hello, World!\\n\");\n    return 0;\n}\n"
                val cursorOffset = content.indexOf("printf")
                content to cursorOffset
            }
            else -> {
                "" to 0
            }
        }
    }
}
