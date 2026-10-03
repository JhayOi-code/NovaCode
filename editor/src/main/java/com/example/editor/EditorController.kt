package com.example.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class EditorController(
    initialText: String = "",
    val onContentChanged: (String) -> Unit = {}
) {
    val documentBuffer = DocumentBuffer(initialText)

    var currentLineIndex by mutableIntStateOf(0)
    var fontSizeSp by mutableIntStateOf(13)
    var isWordWrap by mutableStateOf(false)
    var searchQuery by mutableStateOf("")

    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    fun setText(newText: String, pushUndo: Boolean = true) {
        val current = documentBuffer.getAllText()
        if (current == newText) return

        if (pushUndo) {
            if (undoStack.size > 50) undoStack.removeAt(0)
            undoStack.add(current)
            redoStack.clear()
        }

        documentBuffer.setText(newText)
        onContentChanged(newText)
    }

    fun undo() {
        if (!canUndo) return
        val previous = undoStack.removeAt(undoStack.lastIndex)
        val current = documentBuffer.getAllText()
        redoStack.add(current)
        documentBuffer.setText(previous)
        onContentChanged(previous)
    }

    fun redo() {
        if (!canRedo) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        val current = documentBuffer.getAllText()
        undoStack.add(current)
        documentBuffer.setText(next)
        onContentChanged(next)
    }
}
