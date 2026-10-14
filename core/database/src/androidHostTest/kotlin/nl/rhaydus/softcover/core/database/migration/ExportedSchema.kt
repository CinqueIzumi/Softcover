package nl.rhaydus.softcover.core.database.migration

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Reads Room's exported schema JSON for one database version. */
object ExportedSchema {
    private const val DIRECTORY = "schemas/nl.rhaydus.softcover.core.database.SoftcoverDatabase"

    private fun database(version: Int): JsonObject {
        val text = File("$DIRECTORY/$version.json").readText()

        return Json.parseToJsonElement(text)
            .jsonObject
            .getValue("database")
            .jsonObject
    }

    private fun entities(version: Int): JsonArray = database(version).getValue("entities").jsonArray

    private fun views(version: Int): JsonArray = database(version).getValue("views").jsonArray

    private fun indices(entity: JsonElement): JsonArray = entity.jsonObject["indices"]?.jsonArray ?: JsonArray(emptyList())

    private fun JsonElement.text(key: String): String = jsonObject.getValue(key).jsonPrimitive.content

    private fun String.forName(
        placeholder: String,
        name: String,
    ): String = replace(
        "\${$placeholder}",
        name,
    )

    fun tableNames(version: Int): List<String> {
        val entities = entities(version)

        return entities.map { it.text("tableName") }
    }

    fun indexNames(version: Int): List<String> {
        val indexElements = entities(version).flatMap { indices(it) }

        return indexElements.map { it.text("name") }
    }

    fun viewNames(version: Int): List<String> = views(version).map { it.text("viewName") }

    /** Tables, then indices, then views, each as an executable statement. */
    fun createStatements(version: Int): List<String> {
        val tables = entities(version).map {
            it.text("createSql").forName(
                placeholder = "TABLE_NAME",
                name = it.text("tableName"),
            )
        }
        val indexStatements = entities(version).flatMap { entity ->
            val table = entity.text("tableName")
            val indexElements = indices(entity)

            indexElements.map {
                it.text("createSql").forName(
                    placeholder = "TABLE_NAME",
                    name = table,
                )
            }
        }
        val viewStatements = views(version).map {
            it.text("createSql").forName(
                placeholder = "VIEW_NAME",
                name = it.text("viewName"),
            )
        }

        return tables + indexStatements + viewStatements
    }

    fun createSql(
        version: Int,
        table: String,
    ): String = entities(version)
        .single { it.text("tableName") == table }
        .text("createSql")
        .forName(
            placeholder = "TABLE_NAME",
            name = table,
        )

    fun columnNames(
        version: Int,
        table: String,
    ): List<String> = entities(version)
        .single { it.jsonObject.getValue("tableName").jsonPrimitive.content == table }
        .jsonObject.getValue("fields").jsonArray
        .map { it.jsonObject.getValue("columnName").jsonPrimitive.content }

    /**
     * One-row insert filling every column by affinity (1 / 1.5 / 'x'). Integer `1` everywhere
     * satisfies every foreign key once each table holds its single row.
     */
    fun seedInsert(
        version: Int,
        table: String,
    ): String {
        val fields = entities(version)
            .single { it.jsonObject.getValue("tableName").jsonPrimitive.content == table }
            .jsonObject.getValue("fields").jsonArray
        val columns = fields.joinToString { "`${it.jsonObject.getValue("columnName").jsonPrimitive.content}`" }
        val values = fields.joinToString {
            when (it.jsonObject.getValue("affinity").jsonPrimitive.content) {
                "INTEGER" -> "1"
                "REAL" -> "1.5"
                else -> "'x'"
            }
        }

        return "INSERT INTO `$table` ($columns) VALUES ($values)"
    }

    fun setupQueries(version: Int): List<String> = database(version).getValue("setupQueries").jsonArray
        .map { it.jsonPrimitive.content }
}
