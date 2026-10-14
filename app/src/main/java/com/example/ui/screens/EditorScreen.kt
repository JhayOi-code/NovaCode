package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CodeFile
import com.example.model.EditorTheme

@Composable
fun EditorScreen(
    theme: EditorTheme,
    openFiles: List<CodeFile>,
    activeFileIndex: Int,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onNewTab: () -> Unit,
    onContentChange: (String) -> Unit,
    onSaveCurrent: () -> Unit,
    onOpenWebPreview: () -> Unit,
    onOpenPlugins: () -> Unit
) {
    val activeFile = openFiles.getOrNull(activeFileIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground)
    ) {
        // Top File Tabs Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surfaceColor)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            openFiles.forEachIndexed { index, file ->
                val isSelected = index == activeFileIndex
                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    color = if (isSelected) theme.editorBackground else Color.Transparent,
                    modifier = Modifier.testTag("editor_tab_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = file.name + (if (file.isModified) " •" else ""),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) theme.onSurfaceColor else theme.lineNumberColor
                        )
                        IconButton(
                            onClick = { onCloseTab(index) },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            IconButton(onClick = onNewTab, modifier = Modifier.size(28.dp).testTag("btn_editor_new_tab")) {
                Icon(Icons.Default.Add, contentDescription = "New Tab", tint = theme.lineNumberColor, modifier = Modifier.size(16.dp))
            }
        }

        HorizontalDivider(color = theme.borderColor)

        // Quick Editor Action Bar (Save, Plugins, Preview, Format)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surfaceColor.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onSaveCurrent, modifier = Modifier.size(32.dp).testTag("btn_save_file")) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onOpenPlugins, modifier = Modifier.size(32.dp).testTag("btn_editor_plugins")) {
                    Icon(Icons.Default.Extension, contentDescription = "VS Code Plugins", tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                }
            }

            Button(
                onClick = onOpenWebPreview,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp).testTag("btn_editor_web_preview")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Run Result", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(color = theme.borderColor)

        // Code Editor Body
        if (activeFile != null) {
            OutlinedTextField(
                value = activeFile.content,
                onValueChange = onContentChange,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = theme.onSurfaceColor,
                    lineHeight = 18.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = theme.editorBackground,
                    unfocusedContainerColor = theme.editorBackground
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("code_editor_text_field")
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No file open. Click + to create a new file.", color = theme.lineNumberColor)
            }
        }
    }
}
