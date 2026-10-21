package com.example.autocomplete

data class AutocompleteSuggestion(
    val displayText: String,
    val insertText: String,
    val type: String,
    val documentation: String = ""
)
