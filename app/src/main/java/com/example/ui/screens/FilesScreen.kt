package com.example.ui.screens

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.model.FileItem
import com.example.ui.components.FileExplorerPane
import java.io.File

@Composable
fun FilesScreen(
    theme: EditorTheme,
    workspaceDir: File,
    currentDirectory: File,
    currentPathOrUri: String = currentDirectory.absolutePath,
    storageRoots: List<FileItem> = emptyList(),
    files: List<FileItem>,
    onOpenFile: (FileItem) -> Unit,
    onNavigateDir: (File) -> Unit,
    onNavigatePathOrUri: (String) -> Unit = { onNavigateDir(File(it)) },
    onNavigateUp: () -> Unit,
    onNewFile: (String) -> Unit,
    onNewFolder: (String) -> Unit,
    onDelete: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onMove: (String, String) -> Unit = { _, _ -> },
    onShare: (FileItem) -> Unit = {},
    onClone: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onExportZip: (File) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("files_screen"),
        color = theme.editorBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Storage Location Quick Switcher Row
            if (storageRoots.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(theme.surfaceColor)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    storageRoots.forEach { root ->
                        val isSelected = currentPathOrUri == root.path ||
                                currentDirectory.absolutePath == root.path

                        FilterChip(
                            selected = isSelected,
                            onClick = { onNavigatePathOrUri(root.path) },
                            label = { Text(root.name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primaryColor,
                                selectedLabelColor = theme.onPrimaryColor,
                                containerColor = theme.cardBackgroundColor,
                                labelColor = theme.onSurfaceColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("chip_root_${root.name}")
                        )
                    }
                }
            }

            FileExplorerPane(
                theme = theme,
                currentDirectory = currentDirectory,
                files = files,
                onOpenFile = onOpenFile,
                onNavigateDir = onNavigateDir,
                onNavigateUp = onNavigateUp,
                onNewFile = onNewFile,
                onNewFolder = onNewFolder,
                onDelete = onDelete,
                onRename = onRename,
                onMove = onMove,
                onShare = onShare,
                onClone = onClone,
                onExportZip = onExportZip,
                onRefresh = onRefresh,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
