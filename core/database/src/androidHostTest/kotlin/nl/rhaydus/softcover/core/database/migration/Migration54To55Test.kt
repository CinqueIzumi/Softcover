package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration54To55Test {
    private lateinit var connection: SQLiteConnection

    private val schema54 = ExportedSchema.load(version = 54)
    private val schema55 = ExportedSchema.load(version = 55)

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 54 && it.endVersion == 55 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        connection.execSQL("PRAGMA foreign_keys = ON")
        schema54.createIn(connection)
        schema54.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun createSql(table: String): String {
        val stmt = connection.prepare("SELECT sql FROM sqlite_master WHERE name = '$table'")

        try {
            stmt.step().shouldBeTrue()

            return stmt.getText(0)
        } finally {
            stmt.close()
        }
    }

    private fun normalized(sql: String): String = sql
        .replace(
            "`",
            "",
        )
        .replace(
            "\"",
            "",
        )
        .filterNot { it.isWhitespace() }
        .replace(
            "IFNOTEXISTS",
            "",
        )

    @Nested
    inner class TrendingBooksTable {
        @Test
        fun `trending_books definition matches the exported v55 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            normalized(createSql("trending_books")) shouldBe
                normalized(schema55.table("trending_books").createSql)
        }
    }

    @Nested
    inner class TrendingMetaTable {
        @Test
        fun `trending_meta definition matches the exported v55 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            normalized(createSql("trending_meta")) shouldBe
                normalized(schema55.table("trending_meta").createSql)
        }
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v54 table keeps identical rows and columns`() {
            // ----- Arrange -----
            val tables = schema54.tables
            val rowsBefore = tables.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }
            val columnsBefore = tables.associate { it.name to connection.columnNames(it.name) }
            tables.forEach { rowsBefore.getValue(it.name).size shouldBe 1 }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            tables.forEach { table ->
                connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                ) shouldBe rowsBefore.getValue(table.name)
                connection.columnNames(table.name) shouldBe columnsBefore.getValue(table.name)
            }
        }

        @Test
        fun `resulting tables, indices and views equal the v55 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.userTables() shouldBe schema55.tableNames
            db.indexNames() shouldBe schema55.indexNames
            db.viewNames() shouldBe schema55.viewNames
        }
    }
}
