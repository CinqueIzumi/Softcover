package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/** Read-only helpers for inspecting a raw SQLite connection in migration tests. */
object SqliteInspection {
    fun names(
        connection: SQLiteConnection,
        type: String,
    ): List<String> = column(
        connection = connection,
        sql = "SELECT name FROM sqlite_master WHERE type = '$type' AND name NOT LIKE 'sqlite_%' " +
            "AND name <> 'room_master_table'",
    )

    fun columnNames(
        connection: SQLiteConnection,
        table: String,
    ): List<String> = column(
        connection = connection,
        sql = "SELECT name FROM pragma_table_info('$table')",
    )

    /** Every row of [table], each cell rendered as text (null stays null). */
    fun rows(
        connection: SQLiteConnection,
        table: String,
    ): List<List<String?>> {
        val stmt = connection.prepare("SELECT * FROM `$table` ORDER BY 1")

        try {
            val rows = mutableListOf<List<String?>>()
            val width = stmt.getColumnCount()

            while (stmt.step()) {
                rows.add(List(width) { if (stmt.isNull(it)) null else stmt.getText(it) })
            }

            return rows
        } finally {
            stmt.close()
        }
    }

    fun userVersion(connection: SQLiteConnection): Long {
        val stmt = connection.prepare("PRAGMA user_version")

        try {
            stmt.step()

            return stmt.getLong(0)
        } finally {
            stmt.close()
        }
    }

    fun execAll(
        connection: SQLiteConnection,
        statements: List<String>,
    ) {
        statements.forEach { connection.execSQL(it) }
    }

    private fun column(
        connection: SQLiteConnection,
        sql: String,
    ): List<String> {
        val stmt = connection.prepare(sql)

        try {
            val values = mutableListOf<String>()

            while (stmt.step()) {
                values.add(stmt.getText(0))
            }

            return values
        } finally {
            stmt.close()
        }
    }
}
