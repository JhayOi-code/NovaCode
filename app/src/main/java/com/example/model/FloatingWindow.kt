package com.example.model

/**
 * Model representing an on-screen floating window spawned by GUI frameworks
 * (such as Python Tkinter, Turtle, or multi-window preview widgets).
 */
data class FloatingWindow(
    val id: String,
    val title: String,
    val x: Float = 60f,
    val y: Float = 140f,
    val width: Float = 260f,
    val height: Float = 160f,
    val text: String = "",
    val zIndex: Float = 1f,
    val isVisible: Boolean = true,
    val bgColorHex: String = "#1E1E2E",
    val textColorHex: String = "#CDD6F4"
)
