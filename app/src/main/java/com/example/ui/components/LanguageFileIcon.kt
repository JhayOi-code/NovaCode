package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SupportedLanguage

/**
 * Renders dedicated language and file-type icons with color badges
 * for fast visual scanning in file trees and lists.
 */
@Composable
fun LanguageFileIcon(
    filename: String,
    isDirectory: Boolean,
    isExpanded: Boolean = false,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    if (isDirectory) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
            contentDescription = if (isExpanded) "Expanded Folder" else "Folder",
            tint = Color(0xFFF59E0B), // Warm amber folder color
            modifier = modifier.size(size)
        )
        return
    }

    val ext = filename.substringAfterLast('.', "").lowercase()
    val lang = SupportedLanguage.fromFilename(filename)

    when {
        ext in listOf("kt", "kts") -> {
            // Kotlin badge: Purple/Violet
            FileBadgeIcon("KT", Color(0xFF7F52FF), Color.White, size, modifier)
        }
        ext == "java" -> {
            // Java badge: Amber/Red
            FileBadgeIcon("J", Color(0xFFEA580C), Color.White, size, modifier)
        }
        ext in listOf("py", "pyw") -> {
            // Python badge: Blue & Gold
            FileBadgeIcon("PY", Color(0xFF38BDF8), Color(0xFF0F172A), size, modifier)
        }
        ext in listOf("js", "mjs", "cjs") -> {
            // JavaScript: Bright Yellow
            Box(
                modifier = modifier
                    .size(size)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFFACC15)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "JS",
                    color = Color(0xFF1E293B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        ext in listOf("ts", "tsx") -> {
            // TypeScript: Blue
            FileBadgeIcon("TS", Color(0xFF3178C6), Color.White, size, modifier)
        }
        ext in listOf("html", "htm") -> {
            // HTML: Vibrant Orange
            Icon(
                imageVector = Icons.Default.Html,
                contentDescription = "HTML",
                tint = Color(0xFFE34F26),
                modifier = modifier.size(size)
            )
        }
        ext in listOf("css", "scss", "sass") -> {
            // CSS: Cyan / Blue
            FileBadgeIcon("#", Color(0xFF0284C7), Color.White, size, modifier)
        }
        ext in listOf("json") -> {
            // JSON: Amber curly
            FileBadgeIcon("{ }", Color(0xFFF59E0B), Color(0xFF0F172A), size, modifier)
        }
        ext in listOf("xml") -> {
            FileBadgeIcon("< >", Color(0xFF10B981), Color.White, size, modifier)
        }
        ext in listOf("c", "h") -> {
            FileBadgeIcon("C", Color(0xFF64748B), Color.White, size, modifier)
        }
        ext in listOf("cpp", "hpp", "cc", "cxx") -> {
            FileBadgeIcon("C++", Color(0xFF0284C7), Color.White, size, modifier)
        }
        ext == "rs" -> {
            FileBadgeIcon("RS", Color(0xFFF97316), Color.White, size, modifier)
        }
        ext == "go" -> {
            FileBadgeIcon("GO", Color(0xFF06B6D4), Color.White, size, modifier)
        }
        ext in listOf("sh", "bash", "zsh") -> {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Shell script",
                tint = Color(0xFF10B981),
                modifier = modifier.size(size)
            )
        }
        ext in listOf("md", "markdown") -> {
            FileBadgeIcon("MD", Color(0xFF94A3B8), Color(0xFF0F172A), size, modifier)
        }
        ext == "sql" -> {
            FileBadgeIcon("SQL", Color(0xFF38BDF8), Color(0xFF0F172A), size, modifier)
        }
        ext in listOf("png", "jpg", "jpeg", "svg", "gif", "webp") -> {
            FileBadgeIcon("IMG", Color(0xFFEC4899), Color.White, size, modifier)
        }
        else -> {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "File",
                tint = Color(0xFF94A3B8),
                modifier = modifier.size(size)
            )
        }
    }
}

@Composable
private fun FileBadgeIcon(
    text: String,
    bg: Color,
    textColor: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(3.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = if (text.length > 2) 7.sp else 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            lineHeight = 9.sp
        )
    }
}
