package com.example.model

enum class LintSeverity {
    ERROR, WARNING, INFO
}

data class LintIssue(
    val line: Int,
    val message: String,
    val severity: LintSeverity,
    val startIndex: Int = 0,
    val length: Int = 1,
    val ruleId: String = ""
)
