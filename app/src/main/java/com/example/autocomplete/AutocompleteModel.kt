package com.example.autocomplete

enum class SuggestionKind {
    KEYWORD,
    FUNCTION,
    METHOD,
    PROPERTY,
    SNIPPET,
    TAG,
    AI_COMPLETION
}

data class AutocompleteSuggestion(
    val label: String,
    val insertText: String,
    val kind: SuggestionKind,
    val signature: String? = null,
    val detail: String? = null,
    val isAi: Boolean = false
)
