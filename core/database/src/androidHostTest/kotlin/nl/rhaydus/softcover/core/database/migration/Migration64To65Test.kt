package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration64To65Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 64 && it.endVersion == 65 }

    private val schema64 = ExportedSchema.load(version = 64)

    private val exported = ExportedSchema.load(version = 65)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema64.createIn(connection)
        schema64.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v64 table keeps identical rows`() {
            // ----- Arrange -----
            val rowsBefore = schema64.tables.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            schema64.tables.forEach { table ->
                connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                ) shouldBe rowsBefore.getValue(table.name)
            }
        }
    }

    @Nested
    inner class IsPrimaryAuthorColumn {
        @Test
        fun `existing author links are marked as primary authors`() {
            // ----- Arrange -----
            connection.execSQL("DELETE FROM book_author_cross_ref")
            connection.execSQL("INSERT INTO book_author_cross_ref (bookId, authorId) VALUES (1, 10), (1, 11)")

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            connection.snapshot(
                "book_author_cross_ref",
                listOf("bookId", "authorId", "isPrimaryAuthor"),
            ) shouldBe listOf(
                listOf("1", "10", "1"),
                listOf("1", "11", "1"),
            )
        }

        @Test
        fun `resulting tables, columns, indices and views equal the v65 schema`() {
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
