package com.example.editor

/**
 * Lightweight, zero-dependency JSON parser for themes and token definitions.
 * Runs seamlessly on both Android runtime and pure JVM unit tests without android.jar stub limitations.
 */
object MiniJson {

    fun parse(jsonStr: String): Map<String, Any> {
        val trimmed = jsonStr.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return emptyMap()

        val result = mutableMapOf<String, Any>()
        val content = trimmed.substring(1, trimmed.length - 1).trim()
        var i = 0

        while (i < content.length) {
            // Find key
            while (i < content.length && (content[i].isWhitespace() || content[i] == ',')) i++
            if (i >= content.length) break

            if (content[i] != '"') {
                i++
                continue
            }

            val keyStart = i + 1
            val keyEnd = content.indexOf('"', keyStart)
            if (keyEnd == -1) break
            val key = content.substring(keyStart, keyEnd)
            i = keyEnd + 1

            // Find colon
            while (i < content.length && (content[i].isWhitespace() || content[i] == ':')) i++
            if (i >= content.length) break

            // Parse value
            when (content[i]) {
                '"' -> {
                    val valStart = i + 1
                    var valEnd = valStart
                    while (valEnd < content.length) {
                        if (content[valEnd] == '"' && content[valEnd - 1] != '\\') break
                        valEnd++
                    }
                    val value = if (valEnd < content.length) content.substring(valStart, valEnd) else ""
                    result[key] = value
                    i = valEnd + 1
                }
                '{' -> {
                    val objStart = i
                    var depth = 1
                    i++
                    while (i < content.length && depth > 0) {
                        if (content[i] == '{') depth++
                        else if (content[i] == '}') depth--
                        i++
                    }
                    val nestedStr = content.substring(objStart, i)
                    result[key] = parse(nestedStr)
                }
                else -> {
                    val start = i
                    while (i < content.length && content[i] != ',' && content[i] != '}') i++
                    val rawVal = content.substring(start, i).trim()
                    result[key] = rawVal
                }
            }
        }

        return result
    }
}
