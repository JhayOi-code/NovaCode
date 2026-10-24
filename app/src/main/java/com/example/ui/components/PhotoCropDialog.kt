package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.model.EditorTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Interactive Profile Photo Crop & Alignment Dialog.
 * Allows panning, zooming, and rotating selected images to fit perfectly
 * in a circular profile avatar before uploading.
 */
@Composable
fun PhotoCropDialog(
    theme: EditorTheme,
    imageUri: Uri,
    onCropConfirmed: (savedFilePath: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var rotationDegrees by remember { mutableFloatStateOf(0f) }

    // Load and safely downsample bitmap from gallery URI
    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                val inputBounds = context.contentResolver.openInputStream(imageUri)
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(inputBounds, null, options)
                inputBounds?.close()

                var inSampleSize = 1
                val maxDimension = 1200
                while (options.outWidth / inSampleSize > maxDimension || options.outHeight / inSampleSize > maxDimension) {
                    inSampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val inputBitmap = context.contentResolver.openInputStream(imageUri)
                val bmp = BitmapFactory.decodeStream(inputBitmap, null, decodeOptions)
                inputBitmap?.close()

                if (bmp != null) {
                    sourceBitmap = bmp
                    isLoading = false
                } else {
                    loadError = "Unable to decode selected image"
                    isLoading = false
                }
            } catch (e: Exception) {
                loadError = e.localizedMessage ?: "Failed to open photo"
                isLoading = false
            }
        }
    }

    fun cropAndSave(bmp: Bitmap): String? {
        return try {
            // Compute cropped square bitmap with zoom, pan, and rotation
            val minSide = min(bmp.width, bmp.height).toFloat()
            val cropSize = (minSide / max(scale, 1f)).toInt().coerceIn(64, min(bmp.width, bmp.height))

            val centerX = bmp.width / 2f - (offsetX / 200f * (bmp.width / 2f))
            val centerY = bmp.height / 2f - (offsetY / 200f * (bmp.height / 2f))

            val left = (centerX - cropSize / 2f).toInt().coerceIn(0, bmp.width - cropSize)
            val top = (centerY - cropSize / 2f).toInt().coerceIn(0, bmp.height - cropSize)

            val matrix = Matrix()
            if (rotationDegrees != 0f) {
                matrix.postRotate(rotationDegrees)
            }

            val squareBmp = Bitmap.createBitmap(bmp, left, top, cropSize, cropSize, matrix, true)
            // Scale to standard 512x512 avatar
            val finalAvatar = Bitmap.createScaledBitmap(squareBmp, 512, 512, true)

            val destFile = File(context.filesDir, "custom_profile_avatar.png")
            FileOutputStream(destFile).use { out ->
                finalAvatar.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Crop, contentDescription = null, tint = theme.primaryColor)
                Text(
                    text = "Crop Profile Photo",
                    color = theme.onSurfaceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Drag to reposition, pinch or use slider to zoom into your profile avatar:",
                    color = theme.lineNumberColor,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(Color(0xFF111827), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading image...", color = theme.primaryColor, fontSize = 13.sp)
                    }
                } else if (loadError != null || sourceBitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(loadError ?: "Failed to load", color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                } else {
                    val bmp = sourceBitmap!!

                    // Interactive Cropping Box with circular overlay guide
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.5.dp, theme.borderColor, RoundedCornerShape(14.dp))
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 4f)
                                    val maxOffset = 180f * scale
                                    offsetX = (offsetX + pan.x).coerceIn(-maxOffset, maxOffset)
                                    offsetY = (offsetY + pan.y).coerceIn(-maxOffset, maxOffset)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val imageBitmap = remember(bmp) { bmp.asImageBitmap() }

                        // Canvas drawing the transformed bitmap and circular crop mask
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val radius = min(canvasWidth, canvasHeight) * 0.44f
                            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

                            // Draw image with current scale and pan
                            val drawWidth = canvasWidth * scale
                            val drawHeight = (canvasWidth * (bmp.height.toFloat() / bmp.width.toFloat())) * scale
                            val dstOffset = IntOffset(
                                x = ((canvasWidth - drawWidth) / 2f + offsetX).toInt(),
                                y = ((canvasHeight - drawHeight) / 2f + offsetY).toInt()
                            )
                            val dstSize = IntSize(drawWidth.toInt(), drawHeight.toInt())

                            drawImage(
                                image = imageBitmap,
                                dstOffset = dstOffset,
                                dstSize = dstSize
                            )

                            // Dark translucent mask outside circle
                            val circlePath = Path().apply {
                                addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
                            }

                            clipPath(circlePath, clipOp = ClipOp.Difference) {
                                drawRect(color = Color(0xAA000000))
                            }

                            // Glowing cyan circular border indicator
                            drawCircle(
                                color = Color(0xFF00E5FF),
                                radius = radius,
                                center = center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                            )
                        }
                    }

                    // Zoom and Alignment Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom", tint = theme.primaryColor, modifier = Modifier.size(18.dp))
                        Slider(
                            value = scale,
                            onValueChange = { scale = it },
                            valueRange = 1f..4f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = theme.primaryColor,
                                activeTrackColor = theme.primaryColor,
                                inactiveTrackColor = theme.borderColor
                            )
                        )
                        Text(
                            text = String.format("%.1fx", scale),
                            color = theme.onSurfaceColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Rotate button
                        IconButton(
                            onClick = { rotationDegrees = (rotationDegrees + 90f) % 360f },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = theme.onSurfaceColor, modifier = Modifier.size(18.dp))
                        }

                        // Reset button
                        IconButton(
                            onClick = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                                rotationDegrees = 0f
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = theme.lineNumberColor, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bmp = sourceBitmap
                    if (bmp != null) {
                        val path = cropAndSave(bmp)
                        if (path != null) {
                            onCropConfirmed(path)
                            onDismiss()
                        }
                    }
                },
                enabled = sourceBitmap != null,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_crop_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(6.dp))
                Text("Crop & Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = theme.onSurfaceColor)
            }
        },
        containerColor = theme.surfaceColor,
        shape = RoundedCornerShape(20.dp)
    )
}
