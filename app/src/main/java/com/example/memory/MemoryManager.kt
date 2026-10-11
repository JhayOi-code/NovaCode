package com.example.memory

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * MemoryManager monitors mobile system memory, detects low-memory conditions,
 * and coordinates proactive garbage collection across syntax highlighters, file trees,
 * and editor caches to keep memory footprint minimal on low-end devices.
 */
object MemoryManager : ComponentCallbacks2 {

    private const val TAG = "MemoryManager"

    interface MemoryTrimListener {
        fun onTrimMemory(level: Int)
        fun onLowMemoryPressure()
    }

    private val listeners = CopyOnWriteArrayList<WeakReference<MemoryTrimListener>>()
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        val appContext = context.applicationContext
        appContext.registerComponentCallbacks(this)
        isInitialized = true
        Log.i(TAG, "MemoryManager registered with Application ComponentCallbacks2")
    }

    fun registerListener(listener: MemoryTrimListener) {
        listeners.add(WeakReference(listener))
    }

    fun unregisterListener(listener: MemoryTrimListener) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val item = ref.get()
            if (item == null || item === listener) {
                listeners.remove(ref)
            }
        }
    }

    /**
     * Checks if current available device RAM is approaching threshold.
     */
    fun isLowMemoryCondition(context: Context): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            activityManager?.getMemoryInfo(memInfo)
            memInfo.lowMemory || (memInfo.availMem < memInfo.threshold * 1.5)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Forces immediate cache cleanup & triggers explicit garbage collection.
     */
    fun performGarbageCollection(context: Context? = null) {
        Log.i(TAG, "Triggering proactive garbage collection and cache evictions")
        notifyListeners(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        System.runFinalization()
        System.gc()
    }

    override fun onTrimMemory(level: Int) {
        Log.w(TAG, "System onTrimMemory received level: $level")
        notifyListeners(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            System.gc()
        }
    }

    override fun onLowMemory() {
        Log.w(TAG, "System onLowMemory received! Urgently clearing caches and requesting GC.")
        notifyListeners(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
        System.gc()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // No-op
    }

    private fun notifyListeners(level: Int) {
        val iterator = listeners.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            val listener = ref.get()
            if (listener == null) {
                listeners.remove(ref)
            } else {
                try {
                    listener.onTrimMemory(level)
                    if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
                        listener.onLowMemoryPressure()
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "Error notifying memory listener", e)
                }
            }
        }
    }
}
