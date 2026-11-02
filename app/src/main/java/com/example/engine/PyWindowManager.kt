package com.example.engine

/**
 * Interface bridging Python GUI frameworks (such as Tkinter or Turtle)
 * to Linq Studio's floating multi-window manager.
 */
interface PyWindowManager {
    /**
     * Spawns a new floating window and returns its unique ID.
     */
    fun spawnWindow(
        title: String,
        x: Float = 60f,
        y: Float = 140f,
        width: Float = 260f,
        height: Float = 160f,
        text: String = "",
        bgColorHex: String = "#1E1E2E",
        textColorHex: String = "#CDD6F4"
    ): String

    /**
     * Updates the text / label displayed inside a floating window.
     */
    fun updateWindowText(id: String, text: String)

    /**
     * Closes a specific floating window.
     */
    fun closeWindow(id: String)

    /**
     * Closes all open floating windows.
     */
    fun clearAllWindows()

    /**
     * Checks if execution was cancelled by the user.
     */
    fun isCancelled(): Boolean = false
}
