package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * A Room schema as exported to `core/database/schemas`, loaded so tests can recreate the exact
 * database a shipped release would have on disk.
 */
internal class ExportedSchema private constructor(
    val version: Int,
    val identityHash: String,
    val tables: List<Table>,
    private val views: List<Pair<String, String>>,
    private val setupQueries: List<String>,
) {
    class Column(
        val name: String,
        val affinity: String,
    )

    class Table(
        val name: String,
        val columns: List<Column>,
        val parents: Set<String>,
        val createSql: String,
        val indexSql: List<String>,
        val indexNames: List<String>,
    )

    val tableNames: Set<String> get() = tables.map { it.name }.toSet()

    val indexNames: Set<String> get() = tables.flatMap { it.indexNames }.toSet()

    val viewNames: Set<String> get() = views.map { it.first }.toSet()

    fun table(name: String): Table = tables.single { it.name == name }

    /** Creates every table, index and view, writes the identity hash and sets `user_version`. */
    fun createIn(connection: SQLiteConnection) {
        tables.forEach { table ->
            connection.execSQL(table.createSql)
            table.indexSql.forEach { connection.execSQL(it) }
        }

        views.forEach { (name, sql) ->
            connection.execSQL(sql.replace(
                "\${VIEW_NAME}",
                name,
            ),)
        }

        setupQueries.forEach { connection.execSQL(it) }
        connection.execSQL("PRAGMA user_version = $version")
    }

    /**
     * Inserts one row into every table (parents before children, so foreign keys hold). Every
     * integer column is 1, so each foreign key finds its parent row.
     */
    fun seedOneRowPerTable(connection: SQLiteConnection) {
        val remaining = tables.toMutableList()
        val done = mutableSetOf<String>()

        while (remaining.isNotEmpty()) {
            val next = remaining.first { table -> (table.parents - table.name).all { it in done } }

            remaining.remove(next)
            done.add(next.name)

            val columns = next.columns.joinToString(",") { "`${it.name}`" }
            val values = next.columns.joinToString(",") { seedLiteral(it.affinity) }

            connection.execSQL("INSERT INTO `${next.name}` ($columns) VALUES ($values)")
        }
    }

    private fun seedLiteral(affinity: String): String = when (affinity) {
        "INTEGER" -> "1"
        "REAL" -> "1.5"
        "BLOB" -> "x'01'"
        else -> "'x'"
    }

    companion object {
        fun load(version: Int): ExportedSchema {
            val file = File(
                "schemas/nl.rhaydus.softcover.core.database.SoftcoverDatabase/$version.json",
            )
            val database = Json.parseToJsonElement(file.readText())
                .jsonObject.getValue("database").jsonObject

            val tables = database.getValue("entities").jsonArray.map { entity ->
                val obj = entity.jsonObject
                val name = obj.getValue("tableName").jsonPrimitive.content
                val indices = obj.optionalArray("indices")

                Table(
                    name = name,
                    columns = obj.getValue("fields").jsonArray.map { field ->
                        Column(
                            name = field.jsonObject.getValue("columnName").jsonPrimitive.content,
                            affinity = field.jsonObject.getValue("affinity").jsonPrimitive.content,
                        )
                    },
                    parents = obj.optionalArray("foreignKeys")
                        .map { it.jsonObject.getValue("table").jsonPrimitive.content }
                        .toSet(),
                    createSql = obj.getValue("createSql").jsonPrimitive.content
                        .replace(
                            "\${TABLE_NAME}",
                            name,
                        ),
                    indexSql = indices.map {
                        it.jsonObject.getValue("createSql").jsonPrimitive.content
                            .replace(
                                "\${TABLE_NAME}",
                                name,
                            )
                    },
                    indexNames = indices.map {
                        it.jsonObject.getValue("name").jsonPrimitive.content
                    },
                )
            }

            return ExportedSchema(
                version = version,
                identityHash = database.getValue("identityHash").jsonPrimitive.content,
                tables = tables,
                views = database.optionalArray("views").map {
                    it.jsonObject.getValue("viewName").jsonPrimitive.content to
                        it.jsonObject.getValue("createSql").jsonPrimitive.content
                },
                setupQueries = database.getValue("setupQueries").jsonArray
                    .map { it.jsonPrimitive.content },
            )
        }

        private fun JsonObject.optionalArray(key: String): JsonArray =
            this[key]?.jsonArray ?: JsonArray(emptyList())
    }
}
