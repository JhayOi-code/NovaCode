package com.example.engine

import android.database.sqlite.SQLiteDatabase

object SqlRunner {

    fun run(code: String, fileName: String): ExecutionResult {
        val startTime = System.currentTimeMillis()
        val outputs = mutableListOf<String>()
        var exitCode = 0

        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.create(null)

            // Split into individual SQL statements by semicolon
            val statements = code.split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("--") }

            for (stmt in statements) {
                val clean = stmt.replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "").trim()
                if (clean.isEmpty()) continue

                val isSelect = clean.startsWith("SELECT", ignoreCase = true) || clean.startsWith("PRAGMA", ignoreCase = true)
                if (isSelect) {
                    val cursor = db.rawQuery(clean, null)
                    cursor.use { c ->
                        val colNames = c.columnNames
                        val rows = mutableListOf<List<String>>()

                        while (c.moveToNext()) {
                            val row = mutableListOf<String>()
                            for (i in 0 until c.columnCount) {
                                row.add(c.getString(i) ?: "NULL")
                            }
                            rows.add(row)
                        }

                        // Format as ASCII grid table
                        outputs.addAll(formatAsciiTable(colNames.toList(), rows))
                        outputs.add("(${rows.size} ${if (rows.size == 1) "row" else "rows"} in set)")
                        outputs.add("")
                    }
                } else {
                    db.execSQL(clean)
                    val action = clean.substringBefore(" ").uppercase()
                    outputs.add("Query OK. Statement executed successfully: [$action]")
                }
            }
        } catch (e: Exception) {
            exitCode = 1
            outputs.add("SQLite Error: ${e.message ?: "Syntax or constraint error in SQL script"}")
        } finally {
            try { db?.close() } catch (_: Exception) {}
        }

        if (outputs.isEmpty()) {
            outputs.add("Database script executed successfully. (0 errors)")
        }

        val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(16)
        return ExecutionResult(
            command = "sqlite3 $fileName",
            output = outputs,
            executionTimeMs = elapsed,
            exitCode = exitCode,
            isSuccess = exitCode == 0
        )
    }

    private fun formatAsciiTable(headers: List<String>, rows: List<List<String>>): List<String> {
        if (headers.isEmpty()) return listOf("(Empty result set)")

        val colWidths = headers.map { it.length }.toMutableList()
        for (row in rows) {
            for (i in row.indices) {
                if (i < colWidths.size) {
                    colWidths[i] = maxOf(colWidths[i], row[i].length)
                }
            }
        }

        val sep = buildString {
            append("+")
            colWidths.forEach { w -> append("-".repeat(w + 2)).append("+") }
        }

        val headerLine = buildString {
            append("|")
            headers.forEachIndexed { i, h ->
                append(" ").append(h.padEnd(colWidths[i])).append(" |")
            }
        }

        val result = mutableListOf<String>()
        result.add(sep)
        result.add(headerLine)
        result.add(sep)

        for (row in rows) {
            val rowLine = buildString {
                append("|")
                for (i in headers.indices) {
                    val cell = if (i < row.size) row[i] else "NULL"
                    append(" ").append(cell.padEnd(colWidths[i])).append(" |")
                }
            }
            result.add(rowLine)
        }
        result.add(sep)
        return result
    }
}
