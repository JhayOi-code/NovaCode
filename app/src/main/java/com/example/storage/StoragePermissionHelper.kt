package com.example.storage

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.model.PermissionStatus
import java.io.File

object StoragePermissionHelper {

    fun checkStatus(context: Context): PermissionStatus {
        val tier1Granted = true // App-private sandbox is always granted

        val persistedUris = context.contentResolver.persistedUriPermissions
        val tier2Granted = persistedUris.isNotEmpty() && persistedUris.any { it.isReadPermission && it.isWritePermission }
        val tier2Uri = persistedUris.firstOrNull()?.uri?.toString()

        val tier3Granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        val activePath = when {
            tier3Granted -> Environment.getExternalStorageDirectory().absolutePath
            tier2Granted -> tier2Uri ?: context.filesDir.absolutePath
            else -> context.filesDir.absolutePath
        }

        return PermissionStatus(
            tier1Granted = tier1Granted,
            tier2Granted = tier2Granted,
            tier2Uri = tier2Uri,
            tier3Granted = tier3Granted,
            tier4Active = false,
            activeStoragePath = activePath
        )
    }

    /**
     * Intent to open Android 11+ All Files Access settings directly for this app
     */
    fun createManageAllFilesIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } catch (e: Exception) {
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    /**
     * Intent to launch SAF Folder Picker (ACTION_OPEN_DOCUMENT_TREE)
     */
    fun createOpenDocumentTreeIntent(): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }
    }
}
