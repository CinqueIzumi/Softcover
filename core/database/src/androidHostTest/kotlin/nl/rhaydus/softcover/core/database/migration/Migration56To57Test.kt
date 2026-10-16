package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration56To57Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 56 && it.endVersion == 57 }

    private val schema56 = ExportedSchema.load(version = 56)

    private val exported = ExportedSchema.load(version = 57)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema56.createIn(connection)
        schema56.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class TableShape {
        @Test
        fun `book_quotes has the exported columns`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.columnNames("book_quotes") shouldBe exported.table("book_quotes").columns.map { it.name }
        }
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `an existing quote keeps its columns and gets no book title`() {
            // ----- Arrange -----
            val columns = schema56.table("book_quotes").columns.map { it.name }
            val before = connection.snapshot(
                "book_quotes",
                columns,
            )
            before.size shouldBe 1

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            connection.snapshot(
                "book_quotes",
                columns,
            ) shouldBe before
            connection.snapshot(
                "book_quotes",
                listOf("bookTitle"),
            ) shouldBe listOf(listOf<String?>(null))
        }

        @Test
        fun `every other v56 table keeps identical rows`() {
            // ----- Arrange -----
            val kept = schema56.tables.filter { it.name != "book_quotes" }
            val rowsBefore = kept.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            kept.forEach { table ->
                connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                ) shouldBe rowsBefore.getValue(table.name)
            }
        }

        @Test
        fun `resulting tables, indices and views equal the v57 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.userTables() shouldBe exported.tableNames
            db.indexNames() shouldBe exported.indexNames
            db.viewNames() shouldBe exported.viewNames
        }
    }
}
