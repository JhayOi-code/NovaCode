package com.example.runner.embedded

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.runner.CodeRunner
import com.example.runner.RunEvent
import com.example.runner.RunRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Embedded SQLite code runner utilizing Android's built-in SQLite engine.
 * Executes DDL/DML schemas, INSERTs, and SELECT queries with real formatted tables.
 */
class EmbeddedSqlRunner : CodeRunner {

    override val id: String = "embedded_sql"
    override val displayName: String = "Embedded SQLite Engine (Offline)"
    override val supportedLanguages: Set<String> = setOf("SQL", "sqlite")

    private val isStopped = AtomicBoolean(false)

    override suspend fun isAvailable(): Boolean = true

    override fun run(request: RunRequest): Flow<RunEvent> = flow {
        isStopped.set(false)
        val startTime = System.currentTimeMillis()
        emit(RunEvent.Started(language = "SQL", runner = displayName))

        var db: SQLiteDatabase? = null
        try {
            // Create a fresh isolated in-memory SQLite database
            db = SQLiteDatabase.create(null)

            // Split into clean SQL statements
            val statements = splitSqlStatements(request.code)
            if (statements.isEmpty()) {
                emit(RunEvent.Stdout("[SQL Notice] No valid statements found to execute."))
                emit(RunEvent.Exit(code = 0, durationMs = System.currentTimeMillis() - startTime))
                return@flow
            }

            var executedCount = 0
            for (stmt in statements) {
                if (isStopped.get()) {
                    emit(RunEvent.Stderr("Execution stopped by user."))
                    emit(RunEvent.Exit(code = 130, durationMs = System.currentTimeMillis() - startTime))
                    return@flow
                }

                val trimmed = stmt.trim()
                if (trimmed.isEmpty()) continue

                val upper = trimmed.uppercase()
                if (upper.startsWith("SELECT") || upper.startsWith("PRAGMA") || upper.startsWith("EXPLAIN")) {
                    // Query statement returning results
                    emit(RunEvent.Stdout("sqlite> $trimmed;\n"))
                    var cursor: Cursor? = null
                    try {
                        cursor = db.rawQuery(trimmed, null)
                        val tableOutput = formatCursorTable(cursor)
                        emit(RunEvent.Stdout(tableOutput))
                        val rowCount = cursor.count
                        emit(RunEvent.Stdout("\n($rowCount ${if (rowCount == 1) "row" else "rows"} returned)\n\n"))
                    } finally {
                        cursor?.close()
                    }
                } else {
                    // DDL / DML statement (CREATE, INSERT, UPDATE, DELETE, DROP)
                    emit(RunEvent.Stdout("sqlite> $trimmed;\n"))
                    db.execSQL(trimmed)
                    val summary = when {
                        upper.startsWith("CREATE TABLE") -> "Table created successfully."
                        upper.startsWith("INSERT INTO") -> "1 row inserted."
                        upper.startsWith("UPDATE") -> "Update executed."
                        upper.startsWith("DELETE") -> "Delete executed."
                        upper.startsWith("DROP") -> "Object dropped."
                        else -> "Statement executed successfully."
                    }
                    emit(RunEvent.Stdout("$summary\n\n"))
                }
                executedCount++
            }

            val elapsed = System.currentTimeMillis() - startTime
            emit(RunEvent.Exit(code = 0, durationMs = elapsed))

        } catch (e: android.database.SQLException) {
            val elapsed = System.currentTimeMillis() - startTime
            emit(RunEvent.Stderr("SQLite Error: ${e.message ?: "Syntax or constraint error"}"))
            emit(RunEvent.Exit(code = 1, durationMs = elapsed))
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            emit(RunEvent.Stderr("Runtime Exception: ${e.message ?: e.javaClass.simpleName}"))
            emit(RunEvent.Exit(code = 1, durationMs = elapsed))
        } finally {
            try {
                db?.close()
            } catch (_: Exception) {}
        }
    }.flowOn(Dispatchers.IO)

    override fun stop() {
        isStopped.set(true)
    }

    private fun splitSqlStatements(sql: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inSingleQuote = false
        var inDoubleQuote = false

        for (ch in sql) {
            when (ch) {
                '\'' -> {
                    if (!inDoubleQuote) inSingleQuote = !inSingleQuote
                    current.append(ch)
                }
                '"' -> {
                    if (!inSingleQuote) inDoubleQuote = !inDoubleQuote
                    current.append(ch)
                }
                ';' -> {
                    if (!inSingleQuote && !inDoubleQuote) {
                        val stmt = current.toString().trim()
                        if (stmt.isNotEmpty()) result.add(stmt)
                        current.clear()
                    } else {
                        current.append(ch)
                    }
                }
                else -> current.append(ch)
            }
        }
        val remainder = current.toString().trim()
        if (remainder.isNotEmpty()) {
            result.add(remainder)
        }
        return result
    }

    private fun formatCursorTable(cursor: Cursor): String {
        val columnNames = cursor.columnNames
        if (columnNames.isEmpty()) return "(No columns)"

        val rows = mutableListOf<List<String>>()
        while (cursor.moveToNext()) {
            val row = mutableListOf<String>()
            for (i in 0 until cursor.columnCount) {
                val value = try {
                    if (cursor.isNull(i)) "NULL" else cursor.getString(i) ?: ""
                } catch (_: Exception) {
                    "[BLOB]"
                }
                row.add(value)
            }
            rows.add(row)
        }

        // Calculate max width for each column
        val colWidths = IntArray(columnNames.size)
        for (i in columnNames.indices) {
            var maxLen = columnNames[i].length
            for (row in rows) {
                if (i < row.size && row[i].length > maxLen) {
                    maxLen = row[i].length
                }
            }
            colWidths[i] = maxLen.coerceIn(4, 40)
        }

        val sb = StringBuilder()
        // Top border
        sb.append("┌").append(colWidths.joinToString("┬") { "─".repeat(it + 2) }).append("┐\n")

        // Header row
        sb.append("│ ")
        for (i in columnNames.indices) {
            sb.append(columnNames[i].padEnd(colWidths[i]))
            if (i < columnNames.size - 1) sb.append(" │ ")
        }
        sb.append(" │\n")

        // Divider
        sb.append("├").append(colWidths.joinToString("┼") { "─".repeat(it + 2) }).append("┤\n")

        // Data rows
        if (rows.isEmpty()) {
            sb.append("│ ")
            val totalWidth = colWidths.sum() + (colWidths.size - 1) * 3
            sb.append("(0 rows returned)".padEnd(totalWidth))
            sb.append(" │\n")
        } else {
            for (row in rows) {
                sb.append("│ ")
                for (i in columnNames.indices) {
                    val cell = if (i < row.size) row[i] else ""
                    val display = if (cell.length > colWidths[i]) cell.take(colWidths[i] - 3) + "..." else cell
                    sb.append(display.padEnd(colWidths[i]))
                    if (i < columnNames.size - 1) sb.append(" │ ")
                }
                sb.append(" │\n")
            }
        }

        // Bottom border
        sb.append("└").append(colWidths.joinToString("┴") { "─".repeat(it + 2) }).append("┘")
        return sb.toString()
    }
}
