package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.plugin.PluginCategory
import com.example.plugin.PluginItem
import com.example.plugin.PluginManager

@Composable
fun PluginsScreen(
    theme: EditorTheme,
    pluginManager: PluginManager,
    plugins: List<PluginItem>,
    onOpenEditorWithPlugin: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PluginCategory.ALL) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Store / Catalog, 1: Installed

    var showDownloadUrlDialog by remember { mutableStateOf(false) }
    var downloadUrlInput by remember { mutableStateOf("") }
    var downloadStatusMsg by remember { mutableStateOf<String?>(null) }

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }
    var customAuthor by remember { mutableStateOf("") }
    var customDesc by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf(PluginCategory.TOOLS) }
    var customScript by remember { mutableStateOf("") }

    val filteredPlugins = remember(plugins, searchQuery, selectedCategory, selectedTab) {
        plugins.filter { plugin ->
            val matchesTab = if (selectedTab == 1) plugin.isInstalled else true
            val matchesCategory = if (selectedCategory == PluginCategory.ALL) true else plugin.category == selectedCategory
            val matchesSearch = if (searchQuery.isBlank()) true else {
                plugin.name.contains(searchQuery, ignoreCase = true) ||
                        plugin.description.contains(searchQuery, ignoreCase = true) ||
                        plugin.author.contains(searchQuery, ignoreCase = true)
            }
            matchesTab && matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .padding(16.dp)
    ) {
        // Top Marketplace Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = theme.primaryColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Extension, contentDescription = null, tint = theme.primaryColor)
                    }
                }
                Column {
                    Text(
                        text = "Extensions Marketplace",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor
                    )
                    Text(
                        text = "VS Code-compatible plugin engine",
                        fontSize = 12.sp,
                        color = theme.lineNumberColor
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { showDownloadUrlDialog = true },
                    modifier = Modifier.testTag("btn_plugin_download_url")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = "Download URL", tint = Color(0xFF06B6D4))
                }
                IconButton(
                    onClick = { showAddCustomDialog = true },
                    modifier = Modifier.testTag("btn_plugin_add_custom")
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Plugin", tint = theme.primaryColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search extensions, linters, themes...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.lineNumberColor) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = theme.onSurfaceColor,
                unfocusedTextColor = theme.onSurfaceColor,
                focusedBorderColor = theme.primaryColor,
                unfocusedBorderColor = theme.borderColor,
                focusedContainerColor = theme.surfaceColor,
                unfocusedContainerColor = theme.surfaceColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_plugin_search")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Store vs Installed Switcher
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = theme.surfaceColor,
            contentColor = theme.primaryColor,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Marketplace (${plugins.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("tab_marketplace")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Installed (${plugins.count { it.isInstalled }})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                modifier = Modifier.testTag("tab_installed")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PluginCategory.entries.forEach { category ->
                val isSelected = selectedCategory == category
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) theme.primaryColor else theme.surfaceColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) theme.primaryColor else theme.borderColor),
                    modifier = Modifier.clickable { selectedCategory = category }
                ) {
                    Text(
                        text = category.displayName,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else theme.onSurfaceColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Plugins List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredPlugins, key = { it.id }) { plugin ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                    modifier = Modifier.fillMaxWidth().testTag("plugin_card_${plugin.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(plugin.category.badgeColor).copy(alpha = 0.2f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Extension,
                                            contentDescription = null,
                                            tint = Color(plugin.category.badgeColor),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = plugin.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.onSurfaceColor
                                    )
                                    Text(
                                        text = "${plugin.author} • v${plugin.version} • ★ ${plugin.rating}",
                                        fontSize = 11.sp,
                                        color = theme.lineNumberColor
                                    )
                                }
                            }

                            // Enable / Disable Switch if installed
                            if (plugin.isInstalled) {
                                Switch(
                                    checked = plugin.isEnabled,
                                    onCheckedChange = { checked ->
                                        pluginManager.togglePluginEnabled(plugin.id, checked)
                                    },
                                    modifier = Modifier.testTag("switch_${plugin.id}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = plugin.description,
                            fontSize = 12.sp,
                            color = theme.lineNumberColor,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(plugin.category.badgeColor).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = plugin.category.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(plugin.category.badgeColor),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!plugin.isInstalled) {
                                    Button(
                                        onClick = { pluginManager.setPluginInstalled(plugin.id, true) },
                                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp).testTag("btn_install_${plugin.id}")
                                    ) {
                                        Text("Install", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { pluginManager.setPluginInstalled(plugin.id, false) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp).testTag("btn_uninstall_${plugin.id}")
                                    ) {
                                        Text("Uninstall", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DOWNLOAD FROM URL DIALOG
    if (showDownloadUrlDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadUrlDialog = false },
            title = { Text("Download VS Code Plugin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter extension URL, npm repository, or Open VSX URL:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = downloadUrlInput,
                        onValueChange = { downloadUrlInput = it },
                        placeholder = { Text("https://open-vsx.org/api/...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_download_url")
                    )
                    downloadStatusMsg?.let {
                        Text(it, color = if (it.startsWith("✓")) Color(0xFF10B981) else Color(0xFFEF4444), fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (success, msg) = pluginManager.installFromUrl(downloadUrlInput)
                        downloadStatusMsg = if (success) "✓ $msg" else msg
                        if (success) {
                            downloadUrlInput = ""
                            showDownloadUrlDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_download_url")
                ) {
                    Text("Download & Install")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ADD CUSTOM PLUGIN DIALOG
    if (showAddCustomDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("Create Custom Extension") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Plugin Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customAuthor,
                        onValueChange = { customAuthor = it },
                        label = { Text("Author") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customDesc,
                        onValueChange = { customDesc = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customScript,
                        onValueChange = { customScript = it },
                        label = { Text("Extension Logic (JS / Rules)") },
                        placeholder = { Text("// custom code formatter or rules") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pluginManager.addCustomPlugin(
                            name = customName,
                            author = customAuthor,
                            description = customDesc,
                            category = customCategory,
                            customScript = customScript
                        )
                        customName = ""
                        customAuthor = ""
                        customDesc = ""
                        customScript = ""
                        showAddCustomDialog = false
                    }
                ) {
                    Text("Add to Linq Studio")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
