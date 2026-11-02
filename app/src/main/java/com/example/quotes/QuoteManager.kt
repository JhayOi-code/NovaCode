package com.example.quotes

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.InputStreamReader

data class CodeQuote(
    val quote: String,
    val author: String
)

class QuoteManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("linq_quotes_bag", Context.MODE_PRIVATE)

    companion object {
        val FALLBACK_QUOTE = CodeQuote(
            quote = "Every expert coder was once a beginner who refused to quit.",
            author = "Linq"
        )
        private const val KEY_SEEN_INDICES = "seen_indices"
        private const val KEY_LAST_SHOWN = "last_shown_index"
    }

    private var cachedQuotes: List<CodeQuote>? = null

    suspend fun loadQuotes(): List<CodeQuote> = withContext(Dispatchers.IO) {
        cachedQuotes?.let { return@withContext it }
        try {
            context.assets.open("quotes.json").use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    val jsonStr = reader.readText()
                    val jsonArray = JSONArray(jsonStr)
                    val list = ArrayList<CodeQuote>(jsonArray.length())
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val q = obj.getString("quote").trim()
                        val a = obj.getString("author").trim()
                        if (q.isNotEmpty() && a.isNotEmpty()) {
                            list.add(CodeQuote(q, a))
                        }
                    }
                    if (list.isNotEmpty()) {
                        cachedQuotes = list
                        return@withContext list
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback handled below
        }
        val defaultList = listOf(FALLBACK_QUOTE)
        cachedQuotes = defaultList
        defaultList
    }

    /**
     * Retrieves the next quote from a persistent shuffle bag.
     * Guarantees:
     * 1. Never shows the same quote twice in a row.
     * 2. Exhausts all quotes before repeating any quote.
     */
    suspend fun getNextQuote(): CodeQuote = withContext(Dispatchers.IO) {
        val quotes = loadQuotes()
        if (quotes.isEmpty()) return@withContext FALLBACK_QUOTE
        if (quotes.size == 1) return@withContext quotes[0]

        val seenStrings = prefs.getStringSet(KEY_SEEN_INDICES, emptySet()) ?: emptySet()
        val seenIndices = seenStrings.mapNotNull { it.toIntOrNull() }.filter { it in quotes.indices }.toMutableSet()
        val lastShownIndex = prefs.getInt(KEY_LAST_SHOWN, -1)

        // If all quotes have been seen, reset shuffle bag while avoiding repeating the last one
        if (seenIndices.size >= quotes.size) {
            seenIndices.clear()
            if (lastShownIndex in quotes.indices) {
                seenIndices.add(lastShownIndex)
            }
        }

        // Candidates: indices not yet seen, and not equal to lastShownIndex
        var available = quotes.indices.filter { it !in seenIndices && it != lastShownIndex }
        if (available.isEmpty()) {
            // Edge case: reset completely
            seenIndices.clear()
            available = quotes.indices.filter { it != lastShownIndex }
            if (available.isEmpty()) {
                available = quotes.indices.toList()
            }
        }

        val chosenIndex = available.random()
        seenIndices.add(chosenIndex)

        prefs.edit()
            .putStringSet(KEY_SEEN_INDICES, seenIndices.map { it.toString() }.toSet())
            .putInt(KEY_LAST_SHOWN, chosenIndex)
            .apply()

        quotes[chosenIndex]
    }
}
