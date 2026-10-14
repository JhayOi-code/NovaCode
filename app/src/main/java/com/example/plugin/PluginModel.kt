package com.example.plugin

enum class PluginCategory(val displayName: String, val badgeColor: Long) {
    ALL("All", 0xFF6366F1),
    FORMATTER("Formatters", 0xFF10B981),
    LINTER("Linters", 0xFFEF4444),
    SNIPPET("Snippets", 0xFFF59E0B),
    THEME("Themes", 0xFFA855F7),
    TOOLS("Tools", 0xFF06B6D4),
    CUSTOM("Custom", 0xFFEC4899)
}

enum class DiagnosticSeverity {
    INFO,
    WARNING,
    ERROR
}

data class DiagnosticItem(
    val line: Int,
    val column: Int = 1,
    val message: String,
    val severity: DiagnosticSeverity = DiagnosticSeverity.WARNING,
    val ruleId: String = "lint"
)

data class CodeStatistics(
    val lines: Int,
    val words: Int,
    val characters: Int,
    val blankLines: Int,
    val commentLines: Int
)

data class PluginItem(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val category: PluginCategory,
    val iconKey: String = "extension",
    val isInstalled: Boolean = true,
    val isEnabled: Boolean = true,
    val downloadUrl: String? = null,
    val customScript: String? = null,
    val downloadsCount: String = "1.2M",
    val rating: Double = 4.8
)
