package com.example.runner

import com.example.engine.PyWindowManager
import com.example.engine.PythonRunner
import org.junit.Assert.*
import org.junit.Test

class PythonStreamingAndTkinterTest {

    @Test
    fun testTypewriterOutputLiveStreaming() {
        val script = """
            import sys, time
            def typewriter(text, delay=0.001):
                for char in text:
                    sys.stdout.write(char)
                    sys.stdout.flush()
                    time.sleep(delay)
                sys.stdout.write("\n")
            print("=== Typewriter Engine ===")
            typewriter("Welcome to Linq Studio!")
            typewriter("Running python scripts directly on Android.")
        """.trimIndent()

        val chunks = mutableListOf<String>()
        val result = PythonRunner.run(
            code = script,
            fileName = "typewriter_test.py",
            onChunk = { chunks.add(it) }
        )

        assertTrue("Execution should succeed", result.isSuccess)
        assertEquals(0, result.exitCode)
        assertTrue("Live chunks should be received", chunks.isNotEmpty())

        val joinedChunks = chunks.joinToString("")
        assertTrue("Output should contain header", joinedChunks.contains("=== Typewriter Engine ==="))
        assertTrue("Output should contain typewriter line 1", joinedChunks.contains("Welcome to Linq Studio!"))
        assertTrue("Output should contain typewriter line 2", joinedChunks.contains("Running python scripts directly on Android."))
    }

    @Test
    fun testTkinterMultiWindowPopupSpawning() {
        val script = """
            import tkinter as tk
            import random

            root = tk.Tk()
            root.withdraw()

            windows = []
            for i in range(5):
                w = tk.Toplevel(root)
                w.title(f"Window {i+1}")
                lbl = tk.Label(w, text="Testing...")
                lbl.pack()
                lbl.config(text=f"Active Window {i+1}")
                windows.append(w)
        """.trimIndent()

        val spawned = mutableListOf<String>()
        val updatedTexts = mutableMapOf<String, String>()

        val mockWm = object : PyWindowManager {
            override fun spawnWindow(
                title: String,
                x: Float,
                y: Float,
                width: Float,
                height: Float,
                text: String,
                bgColorHex: String,
                textColorHex: String
            ): String {
                val id = "win_${spawned.size + 1}"
                spawned.add(id)
                return id
            }

            override fun updateWindowText(id: String, text: String) {
                updatedTexts[id] = text
            }

            override fun closeWindow(id: String) {
                spawned.remove(id)
            }

            override fun clearAllWindows() {
                spawned.clear()
            }
        }

        val result = PythonRunner.run(
            code = script,
            fileName = "gui_test.py",
            windowManager = mockWm
        )

        assertTrue(result.isSuccess)
        assertEquals("5 floating windows should have been spawned", 5, spawned.size)
        assertEquals(5, updatedTexts.size)
        assertTrue(updatedTexts.values.any { it.contains("Active Window 1") })
    }
}
