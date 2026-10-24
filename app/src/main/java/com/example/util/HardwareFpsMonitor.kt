package com.example.util

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.platform.LocalContext

/**
 * Real-time Hardware FPS Monitor for Android.
 * Dynamically queries the physical device display refresh rate (e.g., 60Hz, 90Hz, 120Hz, 144Hz)
 * and accurately measures the live Compose frame-rendering loop using high-precision timestamps.
 */
data class FpsState(
    val currentFps: Int,
    val targetRefreshRate: Int,
    val frameTimeMs: Float,
    val isRunningSmooth: Boolean
) {
    val displayLabel: String
        get() = "$currentFps FPS • ${targetRefreshRate}Hz"
}

@Composable
fun rememberHardwareFps(): FpsState {
    val context = LocalContext.current
    
    // Detect physical display refresh rate
    val targetRefreshRate = remember(context) {
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    context.display
                } catch (e: Exception) {
                    @Suppress("DEPRECATION")
                    windowManager?.defaultDisplay
                }
            } else {
                @Suppress("DEPRECATION")
                windowManager?.defaultDisplay
            }
            val rate = display?.refreshRate ?: 60f
            if (rate > 20f && rate < 240f) rate.toInt() else 60
        } catch (e: Exception) {
            60
        }
    }

    var measuredFps by remember { mutableIntStateOf(targetRefreshRate) }
    var frameTimeMs by remember { mutableFloatStateOf(1000f / targetRefreshRate) }

    LaunchedEffect(targetRefreshRate) {
        var lastNanos = 0L
        var frameCount = 0
        var accumulatedNanos = 0L

        while (true) {
            withFrameNanos { nowNanos ->
                if (lastNanos != 0L) {
                    val delta = nowNanos - lastNanos
                    if (delta > 0) {
                        accumulatedNanos += delta
                        frameCount++

                        // Update FPS reading every 20 frames (approx. 3-4 times per second for stability)
                        if (frameCount >= 20 || accumulatedNanos >= 300_000_000L) {
                            val averageDeltaSeconds = (accumulatedNanos.toDouble() / frameCount) / 1_000_000_000.0
                            if (averageDeltaSeconds > 0) {
                                val instantFps = (1.0 / averageDeltaSeconds).toInt()
                                // Clamp between 1 and targetRefreshRate + 2 to avoid anomalous spikes
                                val boundedFps = instantFps.coerceIn(1, targetRefreshRate + 2)
                                measuredFps = boundedFps
                                frameTimeMs = (accumulatedNanos / frameCount / 1_000_000f).coerceAtLeast(1f)
                            }
                            frameCount = 0
                            accumulatedNanos = 0L
                        }
                    }
                }
                lastNanos = nowNanos
            }
        }
    }

    val isRunningSmooth = measuredFps >= (targetRefreshRate * 0.85f).toInt()

    return FpsState(
        currentFps = measuredFps,
        targetRefreshRate = targetRefreshRate,
        frameTimeMs = frameTimeMs,
        isRunningSmooth = isRunningSmooth
    )
}
