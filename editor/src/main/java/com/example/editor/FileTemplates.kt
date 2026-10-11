package com.example.editor

data class TemplateResult(
    val content: String,
    val cursorPosition: Int
)

/**
 * Registry of starter templates for code files.
 * Keyed by file extension (without dot).
 */
object FileTemplates {

    val TEMPLATES: Map<String, String> = mapOf(
        "java" to """public class {Name} {
    public static void main(String[] args) {
        |
    }
}""",
        "kt" to """fun main() {
    |
}""",
        "py" to """def main():
    |

if __name__ == "__main__":
    main()""",
        "js" to """function main() {
  |
}

main();""",
        "html" to """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>{Name}</title>
  <link rel="stylesheet" href="{Name}.css">
</head>
<body>
  |
</body>
</html>""",
        "css" to """/* {Name}.css */

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: sans-serif;
  |
}""",
        "c" to """#include <stdio.h>

int main(void) {
    |
    return 0;
}""",
        "cpp" to """#include <iostream>

int main() {
    |
    return 0;
}""",
        "cs" to """using System;

class {Name}
{
    static void Main(string[] args)
    {
        |
    }
}""",
        "go" to "package main\n\nimport \"fmt\"\n\nfunc main() {\n\t|\n}",
        "rs" to """fn main() {
    |
}""",
        "php" to """<?php

|""",
        "swift" to """import Foundation

|"""
    )

    /**
     * Resolves starter code and landing cursor position for a newly created file.
     * Returns null if extension is unknown or not supported.
     */
    fun getTemplateForFile(fileName: String, tabSize: Int = 4): TemplateResult? {
        val dotIdx = fileName.lastIndexOf('.')
        if (dotIdx == -1 || dotIdx == fileName.length - 1) return null
        val ext = fileName.substring(dotIdx + 1).lowercase()
        val rawTemplate = TEMPLATES[ext] ?: return null

        val baseName = fileName.substring(0, dotIdx)
        var populated = rawTemplate.replace("{Name}", baseName)

        // Adjust tabSize for space-indented templates if user configured custom tab size
        if (ext != "go" && tabSize != 4 && tabSize > 0) {
            val customIndent = " ".repeat(tabSize)
            populated = populated.replace("    ", customIndent)
        }

        val cursorOffset = populated.indexOf('|')
        val cleanContent = populated.replace("|", "")
        val cursorPos = if (cursorOffset >= 0) cursorOffset else cleanContent.length

        return TemplateResult(
            content = cleanContent,
            cursorPosition = cursorPos
        )
    }
}
