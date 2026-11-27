package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration67To68Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 67 && it.endVersion == 68 }

    private val schema67 = ExportedSchema.load(version = 67)

    private val exported = ExportedSchema.load(version = 68)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema67.createIn(connection)
        schema67.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v67 table keeps identical rows`() {
            // ----- Arrange -----
            val rowsBefore = schema67.tables.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            schema67.tables.forEach { table ->
                connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                ) shouldBe rowsBefore.getValue(table.name)
            }
        }
    }

    @Nested
    inner class BookEditionsTable {
        @Test
        fun `an existing edition arrives with no language and not fetched`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.snapshot(
                "book_editions",
                listOf(
                    "id",
                    "language",
                    "languageFetched",
                ),
            ) shouldBe listOf(listOf("1", null, "0"))
        }

        @Test
        fun `book copies and book traits keep their rows`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.countRows("book_copies") shouldBe 1
            db.countRows("book_traits") shouldBe 1
        }
    }

    @Nested
    inner class ResultingSchema {
        @Test
        fun `resulting tables, columns, indices and views equal the v68 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.userTables() shouldBe exported.tableNames
            db.indexNames() shouldBe exported.indexNames
            db.viewNames() shouldBe exported.viewNames

            exported.tables.forEach {
                db.columnNames(it.name).toSet() shouldBe it.columns.map { column -> column.name }.toSet()
            }
        }
    }
}
