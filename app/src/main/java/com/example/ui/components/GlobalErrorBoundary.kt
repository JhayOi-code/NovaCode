package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crash.CrashReporter
import com.example.model.EditorTheme

/**
 * CompositionLocal to trigger recovery or catch from any child screen.
 */
val LocalErrorBoundaryHandler = staticCompositionLocalOf<(Throwable) -> Unit> {
    { _ -> }
}

/**
 * Global Compose Error Boundary.
 * Catches runtime UI errors and composable render failures silently in the background,
 * reports diagnostics via CrashReporter, and presents a graceful recovery UI
 * with auto-restart / reset state actions rather than hard crashes or app freezes.
 */
@Composable
fun GlobalErrorBoundary(
    theme: EditorTheme,
    onRestartApp: () -> Unit,
    onResetEditor: () -> Unit,
    content: @Composable () -> Unit
) {
    var caughtError by remember { mutableStateOf<Throwable?>(null) }

    val errorHandler: (Throwable) -> Unit = remember {
        { throwable ->
            caughtError = throwable
            CrashReporter.captureException(throwable, tag = "GlobalErrorBoundary")
        }
    }

    CompositionLocalProvider(LocalErrorBoundaryHandler provides errorHandler) {
        if (caughtError == null) {
            content()
        } else {
            val error = caughtError!!
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("error_boundary_surface"),
                color = theme.editorBackground
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = theme.surfaceColor
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(28.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = "Error Boundary Shield",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Text(
                                text = "Something went wrong",
                                color = theme.onSurfaceColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Linq Studio's Error Boundary caught a runtime issue silently to prevent a system freeze.",
                                color = theme.lineNumberColor,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                color = theme.gutterBackground,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${error.javaClass.simpleName}: ${error.message ?: "Unknown runtime error"}",
                                    color = Color(0xFFF87171),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(12.dp),
                                    maxLines = 4
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        caughtError = null
                                        onResetEditor()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_recover_editor"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Reset Editor", fontSize = 13.sp)
                                }

                                Button(
                                    onClick = {
                                        caughtError = null
                                        onRestartApp()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_restart_app"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = theme.primaryColor,
                                        contentColor = theme.onPrimaryColor
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restart Service", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
