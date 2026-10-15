package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection

internal fun SQLiteConnection.userTables(): Set<String> =
    queryStrings(
        "SELECT name FROM sqlite_master WHERE type = 'table' " +
            "AND name NOT LIKE 'sqlite_%' AND name NOT IN ('room_master_table', 'android_metadata')",
    ).toSet()

internal fun SQLiteConnection.indexNames(): Set<String> =
    queryStrings("SELECT name FROM sqlite_master WHERE type = 'index' AND name NOT LIKE 'sqlite_%'")
        .toSet()

internal fun SQLiteConnection.viewNames(): Set<String> =
    queryStrings("SELECT name FROM sqlite_master WHERE type = 'view'").toSet()

internal fun SQLiteConnection.columnNames(table: String): List<String> =
    queryRows("PRAGMA table_info(`$table`)").map { requireNotNull(it[1]) }

internal fun SQLiteConnection.userVersion(): Long =
    requireNotNull(queryRows("PRAGMA user_version").single().single()).toLong()

internal fun SQLiteConnection.identityHash(): String =
    requireNotNull(queryRows("SELECT identity_hash FROM room_master_table").single().single())

internal fun SQLiteConnection.countRows(table: String): Int =
    requireNotNull(queryRows("SELECT COUNT(*) FROM `$table`").single().single()).toInt()

/** Every row of [table] ordered by rowid, restricted to [columns], each value rendered as text. */
internal fun SQLiteConnection.snapshot(
    table: String,
    columns: List<String>,
): List<List<String?>> {
    val select = columns.joinToString(",") { "`$it`" }

    return queryRows("SELECT $select FROM `$table` ORDER BY rowid")
}

private fun SQLiteConnection.queryStrings(sql: String): List<String> =
    queryRows(sql).map { requireNotNull(it.single()) }

private fun SQLiteConnection.queryRows(sql: String): List<List<String?>> {
    val statement = prepare(sql)

    try {
        val rows = mutableListOf<List<String?>>()

        while (statement.step()) {
            rows.add(
                (0 until statement.getColumnCount()).map { index ->
                    if (statement.isNull(index)) null else statement.getText(index)
                },
            )
        }

        return rows
    } finally {
        statement.close()
    }
}
