package com.example

import com.example.model.SidebarTab
import com.example.model.TreeNode
import com.example.model.TreeNodeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SidebarDrawerTreeTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testTreeNodeProperties() {
        val fileNode = TreeNode(
            id = "/workspace/Main.kt",
            name = "Main.kt",
            type = TreeNodeType.FILE,
            path = "/workspace/Main.kt",
            size = 1200L
        )

        assertFalse(fileNode.isDirectory)
        assertEquals("kt", fileNode.extension)
        assertEquals("Kotlin", fileNode.language.displayName)

        val folderNode = TreeNode(
            id = "/workspace/src",
            name = "src",
            type = TreeNodeType.FOLDER,
            path = "/workspace/src",
            isExpanded = true,
            children = listOf(fileNode)
        )

        assertTrue(folderNode.isDirectory)
        assertEquals("", folderNode.extension)
        assertEquals(1, folderNode.children.size)
        assertTrue(folderNode.isExpanded)
    }

    @Test
    fun testSidebarTabs() {
        assertEquals("Files", SidebarTab.FILES.title)
        assertEquals("Run", SidebarTab.RUN.title)
        assertEquals("Search", SidebarTab.SEARCH.title)
        assertEquals("Source Control", SidebarTab.GIT.title)
        assertEquals("Account", SidebarTab.ACCOUNT.title)
    }

    @Test
    fun testDuplicateFileLocalLogic() {
        val rootDir = tempFolder.newFolder("workspace_dup")
        val originalFile = File(rootDir, "App.java").apply {
            writeText("public class App {}")
        }
        assertTrue(originalFile.exists())

        // Simulate duplicate naming
        val baseName = originalFile.name.substringBeforeLast('.')
        val ext = originalFile.name.substringAfterLast('.')
        val copyFile = File(rootDir, "$baseName copy.$ext")

        originalFile.copyTo(copyFile, overwrite = false)

        assertTrue(copyFile.exists())
        assertEquals("App copy.java", copyFile.name)
        assertEquals("public class App {}", copyFile.readText())
    }
}
