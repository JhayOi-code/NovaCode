package com.example.model

/**
 * Recursive Tree Node representing either a file or a folder
 * in the editor sidebar drawer file tree.
 */
data class TreeNode(
    val id: String,
    val name: String,
    val type: TreeNodeType,
    val path: String,
    val size: Long = 0L,
    val lastModified: Long = 0L,
    val isExpanded: Boolean = false,
    val children: List<TreeNode> = emptyList(),
    val level: Int = 0
) {
    val isDirectory: Boolean
        get() = type == TreeNodeType.FOLDER

    val extension: String
        get() = if (isDirectory) "" else name.substringAfterLast('.', "")

    val language: SupportedLanguage
        get() = SupportedLanguage.fromFilename(name)
}

enum class TreeNodeType {
    FILE,
    FOLDER
}

/**
 * Sidebar Drawer Tabs:
 * 0: Run / Demo execution
 * 1: Files Explorer (Default)
 * 2: Code Search
 * 3: Source Control (Git / VCS)
 * 4: Account / Profile
 */
enum class SidebarTab(val title: String) {
    RUN("Run"),
    FILES("Files"),
    SEARCH("Search"),
    GIT("Source Control"),
    ACCOUNT("Account")
}
