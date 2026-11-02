package com.example.quotes

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class QuotesTest {

    @Test
    fun testQuotesJsonValidity() {
        val candidates = listOf(
            File("src/main/assets/quotes.json"),
            File("app/src/main/assets/quotes.json"),
            File("../app/src/main/assets/quotes.json")
        )
        val file = candidates.find { it.exists() }
            ?: throw IllegalStateException("quotes.json not found in test search paths")

        val content = file.readText(Charsets.UTF_8)
        val jsonArray = Json.parseToJsonElement(content).jsonArray

        // 1. At least 500 unique quotes
        assertTrue("Expected at least 500 quotes, found ${jsonArray.size}", jsonArray.size >= 500)

        val seenQuotes = mutableSetOf<String>()

        for (i in 0 until jsonArray.size) {
            val obj = jsonArray[i].jsonObject

            // 2. No empty fields
            val quote = obj["quote"]?.jsonPrimitive?.content?.trim() ?: ""
            val author = obj["author"]?.jsonPrimitive?.content?.trim() ?: ""
            assertTrue("Quote at index $i must not be empty", quote.isNotEmpty())
            assertTrue("Author at index $i must not be empty", author.isNotEmpty())

            // 3. No quote over 120 characters
            assertTrue(
                "Quote at index $i exceeds 120 characters (${quote.length}): '$quote'",
                quote.length <= 120
            )

            // 4. No duplicates
            val normalized = quote.lowercase()
            assertTrue("Duplicate quote found at index $i: '$quote'", !seenQuotes.contains(normalized))
            seenQuotes.add(normalized)
        }
    }
}
