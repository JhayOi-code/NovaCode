package com.example.permissions

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.StateFlow

/**
 * Robust, lifecycle-aware manager that handles Storage Access Framework (SAF) folder requests,
 * MANAGE_EXTERNAL_STORAGE settings intents, legacy runtime storage permissions, and persistable URI tracking.
 */
interface PermissionManager : DefaultLifecycleObserver {

    /**
     * Hot reactive state flow emitting real-time storage permission status.
     */
    val permissionState: StateFlow<StoragePermissionState>

    /**
     * Re-evaluates all permission states (Environment.isExternalStorageManager(), ContentResolver persisted URIs, ContextCompat checks).
     */
    fun refreshState()

    /**
     * Attaches to an Android LifecycleOwner (e.g. ComponentActivity) to automatically refresh permissions on ON_RESUME.
     */
    fun attachLifecycle(lifecycleOwner: LifecycleOwner)

    /**
     * Detaches observer from LifecycleOwner.
     */
    fun detachLifecycle(lifecycleOwner: LifecycleOwner)

    /**
     * Creates an Intent to launch the Storage Access Framework (SAF) Folder Picker.
     */
    fun createSafFolderIntent(): Intent

    /**
     * Takes persistable read/write URI permission and updates the active state.
     * @return true if permission was successfully taken.
     */
    fun handleSafFolderResult(treeUri: Uri?): Boolean

    /**
     * Creates an Intent to deep-link the user directly to the All Files Access settings page.
     */
    fun createManageAllFilesIntent(): Intent

    /**
     * Revokes a previously granted persistable SAF folder permission.
     */
    fun releaseSafPermission(uri: Uri): Boolean
}
