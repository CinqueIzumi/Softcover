package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration61To62Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 61 && it.endVersion == 62 }

    private val schema61 = ExportedSchema.load(version = 61)

    private val exported = ExportedSchema.load(version = 62)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema61.createIn(connection)
        schema61.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v61 table keeps identical rows`() {
            // ----- Arrange -----
            val rowsBefore = schema61.tables.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            schema61.tables.forEach { table ->
                val after = connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
                after shouldBe rowsBefore.getValue(table.name)
            }
        }
    }

    @Nested
    inner class NewColumns {
        @Test
        fun `existing authors and books get null in the added columns`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.snapshot(
                "authors",
                listOf(
                    "isBipoc",
                    "isLgbtq",
                    "genderId",
                ),
            ) shouldBe listOf(listOf<String?>(null, null, null))
            db.snapshot(
                "books",
                listOf("pages"),
            ) shouldBe listOf(listOf<String?>(null))
        }
    }

    @Nested
    inner class ResultingSchema {
        @Test
        fun `resulting tables, columns, indices and views equal the v62 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.userTables() shouldBe exported.tableNames
            db.indexNames() shouldBe exported.indexNames
            db.viewNames() shouldBe exported.viewNames
            exported.tables.forEach { table ->
                val actual = db.columnNames(table.name)
                // ALTER TABLE ADD COLUMN appends, so books.pages sits last; Room matches columns by name.
                actual shouldContainExactlyInAnyOrder table.columns.map { column -> column.name }
            }
        }
    }
}
