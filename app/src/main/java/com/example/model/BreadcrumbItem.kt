package com.example.model

import java.io.File

data class BreadcrumbItem(
    val label: String,
    val path: String,
    val isDirectory: Boolean
)

object BreadcrumbHelper {
    /**
     * Extracts clean, dynamic breadcrumb segments for a file path based on the file tree workspace.
     * E.g.
     *   /data/user/0/.../files/workspace/WebStarter/index.html ->
     *   [workspace, WebStarter, index.html]
     */
    fun getSegments(
        filePath: String,
        fileName: String,
        workspaceDir: File? = null
    ): List<BreadcrumbItem> {
        if (filePath.isBlank()) {
            return listOf(BreadcrumbItem(fileName.ifEmpty { "Editor" }, "", false))
        }

        val wsPath = workspaceDir?.absolutePath ?: ""
        val cleanPath = filePath.replace('\\', '/')

        // Case 1: Path is inside workspaceDir
        if (wsPath.isNotEmpty() && cleanPath.startsWith(wsPath)) {
            val relative = cleanPath.removePrefix(wsPath).trimStart('/')
            val parts = relative.split('/').filter { it.isNotEmpty() }
            val result = mutableListOf<BreadcrumbItem>()
            result.add(BreadcrumbItem(workspaceDir?.name ?: "workspace", wsPath, true))

            var current = wsPath
            parts.forEachIndexed { index, part ->
                current = "$current/$part"
                val isLast = index == parts.lastIndex
                result.add(BreadcrumbItem(if (isLast) fileName else part, current, !isLast))
            }
            return result
        }

        // Case 2: Path contains "/workspace" or starts with "workspace/"
        val wsIndex = cleanPath.indexOf("/workspace")
        if (wsIndex != -1) {
            val sub = cleanPath.substring(wsIndex + 1) // "workspace/..."
            val parts = sub.split('/').filter { it.isNotEmpty() }
            val rootPath = cleanPath.substring(0, wsIndex + "/workspace".length)
            val result = mutableListOf<BreadcrumbItem>()
            var current = rootPath
            parts.forEachIndexed { index, part ->
                if (index > 0) current = "$current/$part"
                val isLast = index == parts.lastIndex
                result.add(BreadcrumbItem(if (isLast) fileName else part, current, !isLast))
            }
            return result
        }

        // Case 3: Relative or simple path
        val parts = cleanPath.split('/').filter { it.isNotEmpty() }
        if (parts.size <= 1) {
            return listOf(
                BreadcrumbItem("workspace", wsPath.ifEmpty { "/workspace" }, true),
                BreadcrumbItem(fileName.ifEmpty { parts.firstOrNull() ?: "untitled" }, cleanPath, false)
            )
        }

        // Trim deeply nested internal app directories if present (e.g. data/user/0/com.../files)
        val filteredParts = if (parts.contains("files")) {
            val afterFiles = parts.dropWhile { it != "files" }.drop(1)
            if (afterFiles.isNotEmpty()) afterFiles else parts
        } else {
            parts
        }

        val result = mutableListOf<BreadcrumbItem>()
        var acc = ""
        filteredParts.forEachIndexed { index, part ->
            acc = if (acc.isEmpty()) "/$part" else "$acc/$part"
            val isLast = index == filteredParts.lastIndex
            result.add(BreadcrumbItem(if (isLast) fileName else part, acc, !isLast))
        }

        return if (result.isNotEmpty()) result else listOf(BreadcrumbItem(fileName, cleanPath, false))
    }
}
