package com.example.ui.components

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTheme
import com.example.templates.CodeSnippetTemplate
import com.example.templates.CodeTemplates

@Composable
fun TemplatesSidebar(
    theme: EditorTheme,
    onInsertSnippet: (String) -> Unit,
    onCreateFileWithSnippet: (fileName: String, content: String) -> Unit,
    onClose: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTemplates = remember(selectedCategory, searchQuery) {
        val baseList = CodeTemplates.getTemplatesForCategory(selectedCategory)
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true) ||
                        it.language.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(theme.surfaceColor)
            .border(width = 1.dp, color = theme.borderColor)
            .testTag("templates_sidebar")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DashboardCustomize,
                    contentDescription = null,
                    tint = theme.primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Boilerplate Templates",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor
                    )
                    Text(
                        text = "HTML5, React, Tailwind & More",
                        fontSize = 10.sp,
                        color = theme.lineNumberColor
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp).testTag("btn_close_templates")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.lineNumberColor, modifier = Modifier.size(16.dp))
            }
        }

        HorizontalDivider(color = theme.borderColor.copy(alpha = 0.5f))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search templates...", fontSize = 12.sp, color = theme.lineNumberColor) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.lineNumberColor, modifier = Modifier.size(16.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = theme.lineNumberColor, modifier = Modifier.size(14.dp))
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = theme.editorBackground,
                unfocusedContainerColor = theme.editorBackground,
                focusedBorderColor = theme.primaryColor,
                unfocusedBorderColor = theme.borderColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .height(48.dp)
                .testTag("input_search_templates")
        )

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CodeTemplates.categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) theme.primaryColor else theme.editorBackground,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) theme.primaryColor else theme.borderColor
                    ),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else theme.onSurfaceColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = theme.borderColor.copy(alpha = 0.3f))

        // Templates List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredTemplates, key = { it.id }) { template ->
                TemplateCard(
                    theme = theme,
                    template = template,
                    onInsert = { onInsertSnippet(template.snippet) },
                    onCreateNew = { onCreateFileWithSnippet(template.defaultFileName, template.snippet) }
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    theme: EditorTheme,
    template: CodeSnippetTemplate,
    onInsert: () -> Unit,
    onCreateNew: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = theme.editorBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderColor),
        modifier = Modifier.fillMaxWidth().testTag("template_card_${template.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = template.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.onSurfaceColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = theme.primaryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = template.language,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.primaryColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = template.description,
                fontSize = 11.sp,
                color = theme.lineNumberColor,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Code Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.surfaceColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = template.snippet.lines().take(4).joinToString("\n"),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = theme.onSurfaceColor.copy(alpha = 0.8f),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Insert Button
                OutlinedButton(
                    onClick = onInsert,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .testTag("btn_insert_${template.id}")
                ) {
                    Icon(Icons.Default.Input, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Insert", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // New File Button
                Button(
                    onClick = onCreateNew,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(30.dp)
                        .testTag("btn_newfile_${template.id}")
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
