package com.example.model

import java.util.concurrent.ConcurrentHashMap

/**
 * TreeCacheManager retains cached TreeNode hierarchies to prevent recalculating
 * disk operations repeatedly, and enables instant eviction and garbage collection
 * when low-memory thresholds are approached.
 */
object TreeCacheManager {

    private val treeCache = ConcurrentHashMap<String, List<TreeNode>>()

    fun getCachedNodes(key: String): List<TreeNode>? {
        return treeCache[key]
    }

    fun putCachedNodes(key: String, nodes: List<TreeNode>) {
        // Guard against memory explosion on low-end devices
        if (treeCache.size > 20) {
            treeCache.clear()
        }
        treeCache[key] = nodes
    }

    fun clearCache() {
        treeCache.clear()
    }

    fun cacheSize(): Int = treeCache.size
}
