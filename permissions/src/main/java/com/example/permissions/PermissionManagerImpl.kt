package com.example.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

class PermissionManagerImpl(
    private val context: Context
) : PermissionManager {

    private val _permissionState = MutableStateFlow(calculateCurrentState())
    override val permissionState: StateFlow<StoragePermissionState> = _permissionState.asStateFlow()

    init {
        refreshState()
    }

    override fun onResume(owner: LifecycleOwner) {
        // Lifecycle-aware: automatically poll permission state when returning from System Settings
        refreshState()
    }

    override fun attachLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
        refreshState()
    }

    override fun detachLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.removeObserver(this)
    }

    override fun refreshState() {
        _permissionState.update { calculateCurrentState() }
    }

    private fun calculateCurrentState(): StoragePermissionState {
        val isAndroid11OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

        // 1. Check Tier 3: All Files Access (Android 11+)
        val tier3Granted = if (isAndroid11OrAbove) {
            Environment.isExternalStorageManager()
        } else {
            false
        }

        // 2. Check Legacy Storage Access (Android 8 - 10)
        val legacyStorageGranted = if (!isAndroid11OrAbove) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }

        // 3. Check Tier 2: Storage Access Framework Persisted URIs
        val persistedList = try {
            context.contentResolver.persistedUriPermissions
        } catch (_: Exception) {
            emptyList()
        }

        val grantedUris = persistedList
            .filter { it.isReadPermission && it.isWritePermission }
            .map { it.uri }

        val tier2SafGranted = grantedUris.isNotEmpty()

        // 4. Calculate default active path
        val activePath = when {
            tier3Granted || legacyStorageGranted -> Environment.getExternalStorageDirectory().absolutePath
            tier2SafGranted -> grantedUris.first().toString()
            else -> context.filesDir.absolutePath
        }

        return StoragePermissionState(
            tier1SandboxGranted = true, // Always available
            tier2SafGranted = tier2SafGranted,
            grantedSafUris = grantedUris,
            tier3AllFilesGranted = tier3Granted,
            legacyStorageGranted = legacyStorageGranted,
            isAndroid11OrAbove = isAndroid11OrAbove,
            activeStoragePath = activePath
        )
    }

    override fun createSafFolderIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }
    }

    override fun handleSafFolderResult(treeUri: Uri?): Boolean {
        if (treeUri == null) return false
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        return try {
            context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
            refreshState()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun createManageAllFilesIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } catch (_: Exception) {
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        } else {
            // Android 8-10 fallback to App Details Settings
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    override fun releaseSafPermission(uri: Uri): Boolean {
        val releaseFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        return try {
            context.contentResolver.releasePersistableUriPermission(uri, releaseFlags)
            refreshState()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
