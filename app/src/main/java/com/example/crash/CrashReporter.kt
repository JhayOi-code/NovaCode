package com.example.crash

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Lightweight crash event data model similar to Sentry/Bugsnag issues.
 */
data class CrashReport(
    val id: String,
    val timestamp: Long,
    val threadName: String,
    val exceptionClass: String,
    val message: String,
    val stackTrace: String,
    val deviceModel: String,
    val osVersion: String,
    val memoryUsageMb: Long
)

/**
 * Lightweight Sentry-like crash reporting and self-healing hook.
 * Catches runtime crashes silently in the background, logs structured diagnostics,
 * notifies registered observers, and auto-recovers the editor service / activity
 * to prevent full app freezes and ANRs on low-end devices.
 */
object CrashReporter {

    private const val TAG = "CrashReporter"
    private const val CRASH_LOG_FILE = "linq_crash_reports.log"

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var isHooked = false
    private var appContext: Context? = null

    private val crashListeners = CopyOnWriteArrayList<(CrashReport) -> Unit>()
    private val inMemoryReports = mutableListOf<CrashReport>()

    fun init(context: Context, autoRestartOnFatal: Boolean = true) {
        if (isHooked) return
        appContext = context.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleUncaughtException(thread, throwable, autoRestartOnFatal)
        }
        isHooked = true
        Log.i(TAG, "CrashReporter successfully installed uncaught exception handler")
    }

    fun addCrashListener(listener: (CrashReport) -> Unit) {
        crashListeners.add(listener)
    }

    fun removeCrashListener(listener: (CrashReport) -> Unit) {
        crashListeners.remove(listener)
    }

    fun getRecentReports(): List<CrashReport> {
        synchronized(inMemoryReports) {
            return inMemoryReports.toList()
        }
    }

    /**
     * Silently captures a non-fatal or caught exception and stores diagnostic metadata.
     */
    fun captureException(throwable: Throwable, tag: String = "NonFatal") {
        try {
            val report = buildReport(Thread.currentThread(), throwable)
            recordReport(report)
            Log.e(TAG, "Captured non-fatal exception [$tag]: ${throwable.message}", throwable)
        } catch (e: Exception) {
            Log.e(TAG, "Failed capturing non-fatal exception", e)
        }
    }

    private fun handleUncaughtException(thread: Thread, throwable: Throwable, autoRestart: Boolean) {
        try {
            val report = buildReport(thread, throwable)
            recordReport(report)
            Log.e(TAG, "CRITICAL: Caught unhandled crash silently in background: ${report.exceptionClass} - ${report.message}")

            // Trigger recovery if enabled
            val ctx = appContext
            if (autoRestart && ctx != null) {
                attemptAutoRestart(ctx, report)
            } else {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in crash handler fallback", e)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun buildReport(thread: Thread, throwable: Throwable): CrashReport {
        val runtime = Runtime.getRuntime()
        val usedMemoryMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)

        val sw = java.io.StringWriter()
        throwable.printStackTrace(PrintWriter(sw))

        return CrashReport(
            id = "crash_${System.currentTimeMillis()}_${(1000..9999).random()}",
            timestamp = System.currentTimeMillis(),
            threadName = thread.name,
            exceptionClass = throwable.javaClass.name,
            message = throwable.message ?: "No error message provided",
            stackTrace = sw.toString(),
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            memoryUsageMb = usedMemoryMb
        )
    }

    private fun recordReport(report: CrashReport) {
        synchronized(inMemoryReports) {
            inMemoryReports.add(0, report)
            if (inMemoryReports.size > 20) {
                inMemoryReports.removeAt(inMemoryReports.lastIndex)
            }
        }

        // Persist to local crash log
        appContext?.let { ctx ->
            try {
                val logFile = File(ctx.filesDir, CRASH_LOG_FILE)
                FileWriter(logFile, true).use { fw ->
                    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(report.timestamp))
                    fw.append("[$dateStr] [${report.threadName}] ${report.exceptionClass}: ${report.message}\n")
                    fw.append("Device: ${report.deviceModel} | OS: ${report.osVersion} | Memory: ${report.memoryUsageMb}MB\n")
                    fw.append("${report.stackTrace}\n")
                    fw.append("----------------------------------------------------------------\n")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed writing crash to file", e)
            }
        }

        crashListeners.forEach { listener ->
            try {
                listener(report)
            } catch (e: Exception) {
                Log.e(TAG, "Crash listener threw error", e)
            }
        }
    }

    private fun attemptAutoRestart(context: Context, report: CrashReport) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                launchIntent.putExtra("EXTRA_RESTARTED_FROM_CRASH", true)
                launchIntent.putExtra("EXTRA_CRASH_REASON", "${report.exceptionClass}: ${report.message}")
                context.startActivity(launchIntent)
            }
            // Terminate previous crashed VM instance cleanly
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        } catch (e: Exception) {
            Log.e(TAG, "Failed auto-restarting app", e)
        }
    }
}
