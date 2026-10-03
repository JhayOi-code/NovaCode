package com.example.model

import java.util.UUID

data class CodeFile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val path: String,
    val content: String = "",
    val language: SupportedLanguage = SupportedLanguage.fromFilename(name),
    val isDirty: Boolean = false,
    val cursorPosition: Int = 0,
    val scrollOffset: Int = 0,
    val undoStack: List<String> = emptyList(),
    val redoStack: List<String> = emptyList()
) {
    val lineCount: Int
        get() = content.count { it == '\n' } + 1

    val charCount: Int
        get() = content.length

    val byteSize: Long
        get() = content.toByteArray().size.toLong()

    fun withUpdatedContent(newContent: String): CodeFile {
        if (newContent == content) return this
        val newUndo = (undoStack + content).takeLast(50)
        return copy(
            content = newContent,
            isDirty = true,
            undoStack = newUndo,
            redoStack = emptyList()
        )
    }

    fun undo(): CodeFile {
        if (undoStack.isEmpty()) return this
        val previousContent = undoStack.last()
        val newUndo = undoStack.dropLast(1)
        val newRedo = (redoStack + content).takeLast(50)
        return copy(
            content = previousContent,
            isDirty = true,
            undoStack = newUndo,
            redoStack = newRedo
        )
    }

    fun redo(): CodeFile {
        if (redoStack.isEmpty()) return this
        val nextContent = redoStack.last()
        val newRedo = redoStack.dropLast(1)
        val newUndo = (undoStack + content).takeLast(50)
        return copy(
            content = nextContent,
            isDirty = true,
            undoStack = newUndo,
            redoStack = newRedo
        )
    }

    fun markSaved(): CodeFile = copy(isDirty = false)
}
