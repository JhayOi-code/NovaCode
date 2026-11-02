package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.FloatingWindow
import kotlin.math.roundToInt

@Composable
fun FloatingWindowHost(
    windows: List<FloatingWindow>,
    onCloseWindow: (String) -> Unit,
    onMoveWindow: (String, Float, Float) -> Unit,
    onResizeWindow: (String, Float, Float) -> Unit,
    onFocusWindow: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (windows.isEmpty()) return

    Box(modifier = modifier.fillMaxSize()) {
        windows.filter { it.isVisible }.forEach { window ->
            key(window.id) {
                FloatingWindowItem(
                    window = window,
                    onClose = { onCloseWindow(window.id) },
                    onMove = { dx, dy -> onMoveWindow(window.id, window.x + dx, window.y + dy) },
                    onResize = { dw, dh ->
                        val newW = (window.width + dw).coerceIn(160f, 600f)
                        val newH = (window.height + dh).coerceIn(100f, 600f)
                        onResizeWindow(window.id, newW, newH)
                    },
                    onFocus = { onFocusWindow(window.id) }
                )
            }
        }
    }
}

@Composable
private fun FloatingWindowItem(
    window: FloatingWindow,
    onClose: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onResize: (Float, Float) -> Unit,
    onFocus: () -> Unit
) {
    val density = LocalDensity.current
    val shape = RoundedCornerShape(10.dp)

    val bgColor = remember(window.bgColorHex) {
        try {
            Color(android.graphics.Color.parseColor(window.bgColorHex))
        } catch (_: Exception) {
            Color(0xFF1E1E2E)
        }
    }

    val textColor = remember(window.textColorHex) {
        try {
            Color(android.graphics.Color.parseColor(window.textColorHex))
        } catch (_: Exception) {
            Color(0xFFCDD6F4)
        }
    }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (window.x * density.density).roundToInt(),
                    (window.y * density.density).roundToInt()
                )
            }
            .size(window.width.dp, window.height.dp)
            .zIndex(window.zIndex)
            .shadow(12.dp, shape)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f), shape)
            .testTag("floating_window_${window.id}")
            .clickable { onFocus() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color(0xFF282A36))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dxDp = dragAmount.x / density.density
                            val dyDp = dragAmount.y / density.density
                            onMove(dxDp, dyDp)
                        }
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8B5CF6))
                    )
                    Text(
                        text = window.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close window",
                        tint = Color(0xFFA6ADC8),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Window Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(10.dp)
            ) {
                Text(
                    text = window.text,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxSize()
                )

                // Resize handle at bottom right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val dxDp = dragAmount.x / density.density
                                val dyDp = dragAmount.y / density.density
                                onResize(dxDp, dyDp)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(10.dp)) {
                    val stroke = 1.5f
                    drawLine(Color(0xFF6C7086), androidx.compose.ui.geometry.Offset(size.width, size.height * 0.4f), androidx.compose.ui.geometry.Offset(size.width * 0.4f, size.height), strokeWidth = stroke)
                    drawLine(Color(0xFF6C7086), androidx.compose.ui.geometry.Offset(size.width, size.height * 0.7f), androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height), strokeWidth = stroke)
                }
                }
            }
        }
    }
}
