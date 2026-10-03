package com.example.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Remember a lifecycle-aware instance of PermissionManager in Jetpack Compose.
 * Automatically attaches to the current LifecycleOwner and detaches when leaving composition.
 */
@Composable
fun rememberPermissionManager(): PermissionManager {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    val manager = remember {
        PermissionManagerImpl(context)
    }

    DisposableEffect(lifecycleOwner) {
        manager.attachLifecycle(lifecycleOwner)
        onDispose {
            manager.detachLifecycle(lifecycleOwner)
        }
    }

    return manager
}
