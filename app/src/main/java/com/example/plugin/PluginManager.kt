package com.example.plugin

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class PluginManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("linq_plugins_prefs", Context.MODE_PRIVATE)

    private val defaultCatalog = listOf(
        PluginItem(
            id = "plugin_prettier",
            name = "Prettier - Code Formatter",
            version = "3.2.5",
            author = "Prettier Team",
            description = "An opinionated code formatter supporting JS, TS, HTML, CSS, JSON, Kotlin, Java, and Python.",
            category = PluginCategory.FORMATTER,
            iconKey = "format_align_left",
            isInstalled = true,
            isEnabled = true,
            downloadsCount = "38.5M",
            rating = 4.9
        ),
        PluginItem(
            id = "plugin_eslint",
            name = "ESLint - Syntax & Security Linter",
            version = "8.57.0",
            author = "OpenJS Foundation",
            description = "Find and fix syntax errors, missing semicolons, unbalanced brackets, and dangerous calls.",
            category = PluginCategory.LINTER,
            iconKey = "rule",
            isInstalled = true,
            isEnabled = true,
            downloadsCount = "29.1M",
            rating = 4.8
        ),
        PluginItem(
            id = "plugin_emmet",
            name = "Emmet Snippet Expander",
            version = "2.4.1",
            author = "Sergey Chikuyonok",
            description = "Expand abbreviations into full HTML, CSS, and boilerplate snippets instantly.",
            category = PluginCategory.SNIPPET,
            iconKey = "code",
            isInstalled = true,
            isEnabled = true,
            downloadsCount = "15.4M",
            rating = 4.7
        ),
        PluginItem(
            id = "plugin_bracket_colorizer",
            name = "Bracket Pair Colorizer",
            version = "2.0.2",
            author = "CoenraadS",
            description = "Colorizes matching brackets, braces, and parentheses to improve code readability.",
            category = PluginCategory.TOOLS,
            iconKey = "data_object",
            isInstalled = true,
            isEnabled = true,
            downloadsCount = "12.8M",
            rating = 4.8
        ),
        PluginItem(
            id = "plugin_material_icons",
            name = "Material Icon Theme",
            version = "4.34.0",
            author = "Philipp Kief",
            description = "Beautiful Material Design icons for directories and file types in explorer.",
            category = PluginCategory.THEME,
            iconKey = "palette",
            isInstalled = true,
            isEnabled = true,
            downloadsCount = "18.2M",
            rating = 4.9
        ),
        PluginItem(
            id = "plugin_one_dark_pro",
            name = "One Dark Pro",
            version = "3.18.2",
            author = "binaryify",
            description = "Atom's iconic One Dark theme adapted for mobile code editing.",
            category = PluginCategory.THEME,
            iconKey = "dark_mode",
            isInstalled = true,
            isEnabled = true,
            downloadsCount = "8.6M",
            rating = 4.8
        ),
        PluginItem(
            id = "plugin_dracula",
            name = "Dracula Official Theme",
            version = "2.24.2",
            author = "Dracula Theme",
            description = "A dark theme for high contrast visual focus on mobile screens.",
            category = PluginCategory.THEME,
            iconKey = "nightlight",
            isInstalled = false,
            isEnabled = false,
            downloadsCount = "6.1M",
            rating = 4.7
        ),
        PluginItem(
            id = "plugin_markdown_preview",
            name = "Markdown Preview Enhanced",
            version = "0.8.12",
            author = "Yiyi Wang",
            description = "Live Markdown previewer with table and code block highlighting.",
            category = PluginCategory.TOOLS,
            iconKey = "preview",
            isInstalled = false,
            isEnabled = false,
            downloadsCount = "4.9M",
            rating = 4.6
        ),
        PluginItem(
            id = "plugin_code_stats",
            name = "Code Spell & Token Counter",
            version = "1.5.0",
            author = "LinqStudios",
            description = "Calculates token counts, blank lines, comments, and character metrics.",
            category = PluginCategory.TOOLS,
            iconKey = "analytics",
            isInstalled = true,
            isEnabled = true,
            downloadsCount = "2.3M",
            rating = 4.9
        ),
        PluginItem(
            id = "plugin_todo_tree",
            name = "TODO Task Tree Highlighter",
            version = "0.0.226",
            author = "Gruntfuggly",
            description = "Extracts TODO, FIXME, BUG, and NOTE comments into an actionable list.",
            category = PluginCategory.TOOLS,
            iconKey = "checklist",
            isInstalled = true,
            isEnabled = true,
            downloadsCount = "7.8M",
            rating = 4.8
        )
    )

    private val _plugins = MutableStateFlow<List<PluginItem>>(emptyList())
    val plugins: StateFlow<List<PluginItem>> = _plugins.asStateFlow()

    init {
        loadPlugins()
    }

    private fun loadPlugins() {
        val savedJson = prefs.getString("installed_plugins_json", null)
        if (savedJson == null) {
            _plugins.value = defaultCatalog
            savePlugins()
            return
        }

        try {
            val jsonArr = JSONArray(savedJson)
            val list = mutableListOf<PluginItem>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                list.add(
                    PluginItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        version = obj.optString("version", "1.0.0"),
                        author = obj.optString("author", "Developer"),
                        description = obj.optString("description", ""),
                        category = PluginCategory.valueOf(obj.optString("category", "TOOLS")),
                        iconKey = obj.optString("iconKey", "extension"),
                        isInstalled = obj.optBoolean("isInstalled", true),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        downloadUrl = if (obj.has("downloadUrl")) obj.getString("downloadUrl") else null,
                        customScript = if (obj.has("customScript")) obj.getString("customScript") else null,
                        downloadsCount = obj.optString("downloadsCount", "100+"),
                        rating = obj.optDouble("rating", 4.8)
                    )
                )
            }
            _plugins.value = list
        } catch (_: Exception) {
            _plugins.value = defaultCatalog
        }
    }

    private fun savePlugins() {
        val arr = JSONArray()
        for (item in _plugins.value) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("version", item.version)
                put("author", item.author)
                put("description", item.description)
                put("category", item.category.name)
                put("iconKey", item.iconKey)
                put("isInstalled", item.isInstalled)
                put("isEnabled", item.isEnabled)
                if (item.downloadUrl != null) put("downloadUrl", item.downloadUrl)
                if (item.customScript != null) put("customScript", item.customScript)
                put("downloadsCount", item.downloadsCount)
                put("rating", item.rating)
            }
            arr.put(obj)
        }
        prefs.edit().putString("installed_plugins_json", arr.toString()).apply()
    }

    fun isPluginActive(id: String): Boolean {
        val plugin = _plugins.value.find { it.id == id }
        return plugin != null && plugin.isInstalled && plugin.isEnabled
    }

    fun togglePluginEnabled(id: String, enabled: Boolean) {
        _plugins.value = _plugins.value.map {
            if (it.id == id) it.copy(isEnabled = enabled) else it
        }
        savePlugins()
    }

    fun setPluginInstalled(id: String, installed: Boolean) {
        _plugins.value = _plugins.value.map {
            if (it.id == id) it.copy(isInstalled = installed, isEnabled = installed) else it
        }
        savePlugins()
    }

    fun addCustomPlugin(
        name: String,
        author: String,
        description: String,
        category: PluginCategory,
        customScript: String
    ): PluginItem {
        val newPlugin = PluginItem(
            id = "custom_" + System.currentTimeMillis(),
            name = name.ifBlank { "Custom Extension" },
            version = "1.0.0",
            author = author.ifBlank { "User" },
            description = description.ifBlank { "User-defined custom plugin for Linq Studio." },
            category = category,
            iconKey = "extension",
            isInstalled = true,
            isEnabled = true,
            customScript = customScript,
            downloadsCount = "Local",
            rating = 5.0
        )
        _plugins.value = _plugins.value + newPlugin
        savePlugins()
        return newPlugin
    }

    fun installFromUrl(url: String): Pair<Boolean, String> {
        val trimmed = url.trim()
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return Pair(false, "Invalid URL: Must start with http:// or https://")
        }
        val name = trimmed.substringAfterLast("/").substringBefore(".").ifBlank { "Remote Plugin" }
        val newPlugin = PluginItem(
            id = "downloaded_" + System.currentTimeMillis(),
            name = name.replace("-", " ").capitalizeWords(),
            version = "1.0.0",
            author = "Remote Web",
            description = "Installed from $trimmed",
            category = PluginCategory.CUSTOM,
            iconKey = "cloud_download",
            isInstalled = true,
            isEnabled = true,
            downloadUrl = trimmed,
            downloadsCount = "Direct",
            rating = 4.8
        )
        _plugins.value = _plugins.value + newPlugin
        savePlugins()
        return Pair(true, "Installed ${newPlugin.name} successfully!")
    }

    fun resetToDefaults() {
        _plugins.value = defaultCatalog
        savePlugins()
    }

    // --- Core Extension Execution Hooks ---

    fun formatCode(source: String, extension: String): String {
        if (!isPluginActive("plugin_prettier")) return source
        val lines = source.lines()
        val result = StringBuilder()
        var indentLevel = 0
        val indentStr = "    "

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) {
                result.append("\n")
                continue
            }
            if (line.startsWith("}") || line.startsWith(")") || line.startsWith("]")) {
                indentLevel = (indentLevel - 1).coerceAtLeast(0)
            }
            result.append(indentStr.repeat(indentLevel)).append(line).append("\n")
            if (line.endsWith("{") || line.endsWith("(") || line.endsWith("[")) {
                indentLevel++
            }
        }
        return result.toString().trimEnd() + "\n"
    }

    fun lintCode(source: String, extension: String): List<DiagnosticItem> {
        if (!isPluginActive("plugin_eslint")) return emptyList()
        val diagnostics = mutableListOf<DiagnosticItem>()
        val lines = source.lines()

        for ((index, line) in lines.withIndex()) {
            val lineNum = index + 1
            val trimmed = line.trim()
            if (trimmed.count { it == '"' } % 2 != 0 && !trimmed.contains("\\\"")) {
                diagnostics.add(
                    DiagnosticItem(lineNum, 1, "Unclosed quote string literal", DiagnosticSeverity.ERROR, "quotes")
                )
            }
            if ((trimmed.startsWith("var ") || trimmed.contains(" var ")) && !extension.equals("kt", true)) {
                diagnostics.add(
                    DiagnosticItem(lineNum, 1, "Unexpected 'var', prefer 'const' or 'let'", DiagnosticSeverity.WARNING, "no-var")
                )
            }
            if (trimmed.contains("console.log(") || trimmed.contains("println(")) {
                diagnostics.add(
                    DiagnosticItem(lineNum, 1, "Unexpected console logging in production code", DiagnosticSeverity.INFO, "no-console")
                )
            }
        }
        return diagnostics
    }

    fun expandSnippet(abbreviation: String): String {
        if (!isPluginActive("plugin_emmet")) return abbreviation
        return when (abbreviation.trim()) {
            "html5", "!" -> """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Linq Document</title>
</head>
<body>
  
</body>
</html>"""
            "div.container" -> """<div class="container"></div>"""
            "btn" -> """<button class="btn btn-primary">Click Me</button>"""
            "fun" -> """fun main() {
    println("Hello Linq Studio")
}"""
            else -> abbreviation
        }
    }

    fun calculateStats(source: String, extension: String): CodeStatistics {
        val lines = source.lines()
        val words = source.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val characters = source.length
        val blankLines = lines.count { it.isBlank() }
        val commentLines = lines.count {
            val t = it.trim()
            t.startsWith("//") || t.startsWith("/*") || t.startsWith("*") || t.startsWith("#")
        }
        return CodeStatistics(lines.size, words, characters, blankLines, commentLines)
    }

    fun extractTodos(source: String): List<String> {
        if (!isPluginActive("plugin_todo_tree")) return emptyList()
        val lines = source.lines()
        val result = mutableListOf<String>()
        for ((idx, line) in lines.withIndex()) {
            val t = line.trim()
            if (t.contains("TODO", ignoreCase = true) || t.contains("FIXME", ignoreCase = true) || t.contains("BUG", ignoreCase = true)) {
                result.add("L${idx + 1}: $t")
            }
        }
        return result
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
