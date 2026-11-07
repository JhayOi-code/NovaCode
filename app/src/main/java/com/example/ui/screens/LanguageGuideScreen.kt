package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme

@Composable
fun LanguageGuideScreen(
    theme: EditorTheme,
    guide: LanguageGuideData,
    onBack: () -> Unit,
    onOpenStarterCode: (String, String) -> Unit = { _, _ -> },
    onRunSnippet: ((String, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val fullGuide = remember(guide.language) {
        LanguageGuidesRepository.getGuideById(guide.language) ?: ComprehensiveLanguageGuide(
            id = guide.language.lowercase(),
            language = guide.language,
            iconVector = guide.iconVector,
            description = guide.description,
            badgeColor = guide.badgeColor,
            fileExtension = guide.fileExtension,
            version = "Stable",
            overview = guide.overview,
            quickRef = emptyList(),
            sections = emptyList(),
            starterCode = guide.starterCode,
            tips = guide.tips
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Categories derived dynamically from sections
    val categories = remember(fullGuide.sections) {
        listOf("All", "Quick Ref") + fullGuide.sections.map { it.category }.distinct()
    }

    // Filter sections based on search query and category filter
    val filteredSections = remember(searchQuery, selectedCategory, fullGuide.sections) {
        val q = searchQuery.trim().lowercase()
        fullGuide.sections.filter { sec ->
            val matchesCategory = selectedCategory == "All" || selectedCategory == "Quick Ref" || sec.category.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = q.isEmpty() ||
                    sec.title.lowercase().contains(q) ||
                    sec.explanation.lowercase().contains(q) ||
                    sec.codeSnippet.lowercase().contains(q) ||
                    sec.tags.any { it.lowercase().contains(q) } ||
                    (q == "looping" && (sec.tags.contains("loop") || sec.title.lowercase().contains("loop")))
            matchesCategory && matchesQuery
        }
    }

    val filteredQuickRef = remember(searchQuery, selectedCategory, fullGuide.quickRef) {
        val q = searchQuery.trim().lowercase()
        if (selectedCategory != "All" && selectedCategory != "Quick Ref") emptyList()
        else if (q.isEmpty()) fullGuide.quickRef
        else fullGuide.quickRef.filter {
            it.topic.lowercase().contains(q) || it.syntax.lowercase().contains(q) || it.description.lowercase().contains(q)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.editorBackground)
            .testTag("language_guide_screen")
    ) {
        // Sticky Top Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = theme.surfaceColor,
            tonalElevation = 4.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp).testTag("guide_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Guides",
                        tint = theme.onSurfaceColor
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${fullGuide.language} Guide & Cheat Sheet",
                            color = theme.onSurfaceColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = fullGuide.badgeColor.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, fullGuide.badgeColor.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = fullGuide.fileExtension,
                                color = fullGuide.badgeColor,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${fullGuide.version} • Comprehensive Code Guide",
                        color = theme.lineNumberColor,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Live In-Guide Interactive Search Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = theme.surfaceColor.copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search in ${fullGuide.language} (e.g. looping, array, async)...",
                            fontSize = 12.sp,
                            color = theme.lineNumberColor
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (searchQuery.isNotEmpty()) theme.primaryColor else theme.lineNumberColor,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp).testTag("guide_search_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = theme.lineNumberColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primaryColor,
                        unfocusedBorderColor = theme.borderColor,
                        focusedTextColor = theme.onSurfaceColor,
                        unfocusedTextColor = theme.onSurfaceColor,
                        cursorColor = theme.primaryColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 46.dp)
                        .testTag("guide_search_input")
                )

                // Horizontally Scrollable Topic TOC Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) fullGuide.badgeColor else theme.editorBackground,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) fullGuide.badgeColor else theme.borderColor
                            ),
                            modifier = Modifier
                                .clickable { selectedCategory = cat }
                                .testTag("guide_filter_chip_$cat")
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else theme.onSurfaceColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Match count feedback indicator
                if (searchQuery.isNotEmpty()) {
                    val totalFound = filteredSections.size + filteredQuickRef.size
                    Text(
                        text = if (totalFound > 0) "Found $totalFound matching guides for '$searchQuery'" else "No matches found for '$searchQuery'",
                        color = if (totalFound > 0) fullGuide.badgeColor else Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header card
            if (searchQuery.isEmpty() && selectedCategory == "All") {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = fullGuide.badgeColor.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, fullGuide.badgeColor),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = fullGuide.iconVector,
                                        contentDescription = fullGuide.language,
                                        tint = fullGuide.badgeColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = fullGuide.language,
                                        color = theme.onSurfaceColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = fullGuide.version,
                                        color = theme.lineNumberColor,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = fullGuide.description,
                                    color = theme.lineNumberColor,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                // Overview Block
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Overview & Linq Studio Execution",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = fullGuide.overview,
                                color = theme.onSurfaceColor.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Quick Reference Cheat Sheet Table Block
            if (filteredQuickRef.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "⚡", fontSize = 14.sp)
                            Text(
                                text = "Quick Reference Cheat Sheet",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                filteredQuickRef.forEachIndexed { idx, row ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (idx % 2 == 0) Color.Transparent else theme.editorBackground.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                            Text(text = row.topic, color = theme.onSurfaceColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text(text = row.syntax, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                            Text(text = row.description, color = theme.lineNumberColor, fontSize = 10.sp)
                                        }

                                        IconButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Syntax", row.syntax))
                                                Toast.makeText(context, "Copied syntax: ${row.syntax}", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy syntax", tint = theme.lineNumberColor, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Filtered Sections
            items(filteredSections, key = { it.id }) { sec ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                    modifier = Modifier.fillMaxWidth().testTag("guide_section_${sec.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = fullGuide.badgeColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = sec.category.uppercase(),
                                            color = fullGuide.badgeColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = sec.title,
                                        color = theme.onSurfaceColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = sec.explanation,
                            color = theme.onSurfaceColor.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )

                        // Code Card
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                // Code card header with action buttons
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF1E293B).copy(alpha = 0.4f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = fullGuide.fileExtension,
                                        color = Color(0xFF94A3B8),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Copy Button
                                        Button(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText(sec.title, sec.codeSnippet))
                                                Toast.makeText(context, "✓ Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color.White),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.heightIn(min = 32.dp).testTag("guide_copy_snippet_button")
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copy", fontSize = 11.sp)
                                        }

                                        // Open in Editor Button
                                        Button(
                                            onClick = {
                                                val fileName = "Example_${sec.id.replace("[^a-zA-Z0-9]".toRegex(), "")}${fullGuide.fileExtension}"
                                                onOpenStarterCode(fileName, sec.codeSnippet)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = fullGuide.badgeColor, contentColor = Color.Black),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.heightIn(min = 32.dp).testTag("guide_open_starter_button")
                                        ) {
                                            Icon(Icons.Default.Code, contentDescription = "Open", modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Text(
                                    text = sec.codeSnippet,
                                    color = Color(0xFFE2E8F0),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Empty Search State
            if (filteredSections.isEmpty() && filteredQuickRef.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = theme.lineNumberColor, modifier = Modifier.size(36.dp))
                            Text(
                                text = "No topics match '$searchQuery'",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Try searching for terms like 'looping', 'array', 'function', 'class', or 'variables'.",
                                color = theme.lineNumberColor,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = "All"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.heightIn(min = 44.dp)
                            ) {
                                Text("Clear Search & Show All Topics", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Tips Section at bottom
            if (searchQuery.isEmpty() && fullGuide.tips.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(18.dp))
                            Text(
                                text = "Best Practices & Pro Tips",
                                color = theme.onSurfaceColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        fullGuide.tips.forEach { tip ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.surfaceColor,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = fullGuide.badgeColor,
                                        modifier = Modifier.size(5.dp)
                                    ) {}
                                    Text(
                                        text = tip,
                                        color = theme.onSurfaceColor.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
