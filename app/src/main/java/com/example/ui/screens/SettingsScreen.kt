package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.EditorTheme
import com.example.theme.JsonThemeLoader

@Composable
fun SettingsScreen(
    currentTheme: EditorTheme,
    onSelectTheme: (EditorTheme) -> Unit,
    fontSizeSp: Int,
    onFontSizeChange: (Int) -> Unit,
    isWordWrapEnabled: Boolean,
    onToggleWordWrap: (Boolean) -> Unit,
    reduceTransparency: Boolean,
    onToggleReduceTransparency: (Boolean) -> Unit,
    onOpenStorageWizard: () -> Unit,
    onImportJsonTheme: (String) -> Boolean = { false },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val companyName = stringResource(R.string.company_name)
    val footerText = stringResource(R.string.footer_credit, companyName)

    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf(JsonThemeLoader.ONE_DARK_JSON) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.editorBackground)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Preferences & Themes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = currentTheme.onSurfaceColor
            )
            Text(
                text = "Customize the editor theme, typography, and JSON syntax token engine",
                color = currentTheme.lineNumberColor,
                fontSize = 12.sp
            )
        }

        // Theme Engine Presets Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = currentTheme.primaryColor
                        )
                        Text(
                            text = "Color Theme Presets",
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.onSurfaceColor,
                            fontSize = 15.sp
                        )
                    }

                    // Themes horizontal row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        EditorTheme.AllThemes.forEach { preset ->
                            val isSelected = preset.id == currentTheme.id
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(preset.cardBackgroundColor)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) currentTheme.primaryColor else currentTheme.borderColor,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onSelectTheme(preset) }
                                    .padding(10.dp)
                                    .width(100.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(preset.primaryColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(preset.keywordColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(preset.stringColor)
                                    )
                                }

                                Text(
                                    text = preset.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = preset.onSurfaceColor,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // JSON Theme Engine & Token Colors Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_json_theme_engine"),
                colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DataObject,
                                contentDescription = null,
                                tint = currentTheme.primaryColor
                            )
                            Text(
                                text = "JSON Token Engine",
                                fontWeight = FontWeight.Bold,
                                color = currentTheme.onSurfaceColor,
                                fontSize = 15.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { showImportDialog = true }) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Import", fontSize = 12.sp)
                            }
                            TextButton(onClick = { showExportDialog = true }) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Export", fontSize = 12.sp)
                            }
                        }
                    }

                    Text(
                        text = "Syntax highlighting token color definitions in standard JSON format with One Dark and Dracula schemes.",
                        color = currentTheme.lineNumberColor,
                        fontSize = 12.sp
                    )

                    // One Dark JSON Card
                    JsonThemePresetCard(
                        title = "One Dark (JSON)",
                        subtitle = "Classic Atom / VS Code dark syntax",
                        theme = currentTheme,
                        isActive = currentTheme.id == "one_dark",
                        tokenSamples = listOf(
                            "keyword" to Color(0xFFC678DD),
                            "string" to Color(0xFF98C379),
                            "comment" to Color(0xFF5C6370),
                            "number" to Color(0xFFD19A66),
                            "type" to Color(0xFFE5C07B),
                            "function" to Color(0xFF61AFEF)
                        ),
                        onApply = {
                            val oneDark = JsonThemeLoader.loadOneDark()
                            onSelectTheme(oneDark)
                            Toast.makeText(context, "Applied One Dark (JSON)", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Dracula JSON Card
                    JsonThemePresetCard(
                        title = "Dracula (JSON)",
                        subtitle = "Vibrant dark theme for modern developers",
                        theme = currentTheme,
                        isActive = currentTheme.id == "dracula",
                        tokenSamples = listOf(
                            "keyword" to Color(0xFFFF79C6),
                            "string" to Color(0xFFF1FA8C),
                            "comment" to Color(0xFF6272A4),
                            "number" to Color(0xFFBD93F9),
                            "type" to Color(0xFF8BE9FD),
                            "function" to Color(0xFF50FA7B)
                        ),
                        onApply = {
                            val dracula = JsonThemeLoader.loadDracula()
                            onSelectTheme(dracula)
                            Toast.makeText(context, "Applied Dracula (JSON)", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // Editor Typography & Display
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = null,
                            tint = currentTheme.primaryColor
                        )
                        Text(
                            text = "Editor Typography & Wrap",
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.onSurfaceColor,
                            fontSize = 15.sp
                        )
                    }

                    // Font Size Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Font Size", color = currentTheme.onSurfaceColor, fontSize = 13.sp)
                            Text(
                                text = "${fontSizeSp}sp",
                                color = currentTheme.primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }

                        Slider(
                            value = fontSizeSp.toFloat(),
                            onValueChange = { onFontSizeChange(it.toInt()) },
                            valueRange = 10f..24f,
                            steps = 13,
                            colors = SliderDefaults.colors(
                                thumbColor = currentTheme.primaryColor,
                                activeTrackColor = currentTheme.primaryColor,
                                inactiveTrackColor = currentTheme.borderColor
                            ),
                            modifier = Modifier.testTag("slider_font_size")
                        )
                    }

                    // Soft Line Wrap Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Soft Line Wrap", fontWeight = FontWeight.Medium, color = currentTheme.onSurfaceColor, fontSize = 13.sp)
                            Text(text = "Wrap long code lines without horizontal scrolling", color = currentTheme.lineNumberColor, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isWordWrapEnabled,
                            onCheckedChange = onToggleWordWrap,
                            colors = SwitchDefaults.colors(checkedThumbColor = currentTheme.primaryColor),
                            modifier = Modifier.testTag("switch_word_wrap")
                        )
                    }
                }
            }
        }

        // Storage Access Wizard Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenStorageWizard)
                    .testTag("card_open_storage_wizard"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = currentTheme.surfaceColor),
                border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = currentTheme.primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(text = "Storage Access Permissions", fontWeight = FontWeight.Bold, color = currentTheme.onSurfaceColor, fontSize = 14.sp)
                            Text(text = "Configure SAF directory tree and All Files access", color = currentTheme.lineNumberColor, fontSize = 12.sp)
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Wizard",
                        tint = currentTheme.lineNumberColor
                    )
                }
            }
        }

        // Footer Credit
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_footer_credit"),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = footerText,
                        color = currentTheme.lineNumberColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "NovaCode v1.0.0",
                        color = currentTheme.lineNumberColor.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Import Custom JSON Theme Dialog
    if (showImportDialog) {
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = currentTheme.surfaceColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Import JSON Theme",
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.onSurfaceColor,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Paste syntax token color definitions in JSON format:",
                        color = currentTheme.lineNumberColor,
                        fontSize = 12.sp
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF080C14),
                        border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor)
                    ) {
                        BasicTextField(
                            value = importJsonText,
                            onValueChange = { importJsonText = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .testTag("import_theme_json_input"),
                            textStyle = TextStyle(
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            cursorBrush = SolidColor(currentTheme.primaryColor)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showImportDialog = false }) {
                            Text("Cancel", color = currentTheme.lineNumberColor)
                        }
                        Button(
                            onClick = {
                                val success = onImportJsonTheme(importJsonText)
                                if (success) {
                                    showImportDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTheme.primaryColor,
                                contentColor = currentTheme.onPrimaryColor
                            )
                        ) {
                            Text("Apply JSON Theme")
                        }
                    }
                }
            }
        }
    }

    // Export Current Theme JSON Dialog
    if (showExportDialog) {
        val jsonOutput = currentTheme.toJson()
        Dialog(onDismissRequest = { showExportDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = currentTheme.surfaceColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Export Theme JSON",
                        fontWeight = FontWeight.Bold,
                        color = currentTheme.onSurfaceColor,
                        fontSize = 16.sp
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF080C14),
                        border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.borderColor)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            item {
                                Text(
                                    text = jsonOutput,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("theme_json", jsonOutput)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied theme JSON to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTheme.primaryColor,
                                contentColor = currentTheme.onPrimaryColor
                            )
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Copy JSON")
                        }

                        TextButton(onClick = { showExportDialog = false }) {
                            Text("Close", color = currentTheme.lineNumberColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JsonThemePresetCard(
    title: String,
    subtitle: String,
    theme: EditorTheme,
    isActive: Boolean,
    tokenSamples: List<Pair<String, Color>>,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = theme.cardBackgroundColor),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) theme.primaryColor else theme.borderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor,
                        fontSize = 13.sp
                    )
                    Text(
                        text = subtitle,
                        color = theme.lineNumberColor,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) Color(0xFF22C55E) else theme.primaryColor,
                        contentColor = theme.onPrimaryColor
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = if (isActive) "Active" else "Apply",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Token Color Chips Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tokenSamples.forEach { (tokenName, tokenColor) ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0F141C))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(tokenColor)
                        )
                        Text(
                            text = tokenName,
                            color = tokenColor,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
