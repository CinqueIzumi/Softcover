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

class Migration62To63Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 62 && it.endVersion == 63 }

    private val schema62 = ExportedSchema.load(version = 62)

    private val exported = ExportedSchema.load(version = 63)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema62.createIn(connection)
        schema62.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v62 table keeps identical rows`() {
            // ----- Arrange -----
            val rowsBefore = schema62.tables.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            schema62.tables.forEach { table ->
                connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                ) shouldBe rowsBefore.getValue(table.name)
            }
        }
    }

    @Nested
    inner class BookUserTagsTable {
        @Test
        fun `resulting tables, columns, indices and views equal the v63 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.userTables() shouldBe exported.tableNames
            db.indexNames() shouldBe exported.indexNames
            db.viewNames() shouldBe exported.viewNames
            exported.tables.forEach {
                db.columnNames(it.name) shouldBe it.columns.map { column -> column.name }
            }
        }

        @Test
        fun `the new table starts empty and accepts a tag for a migrated book`() {
            // ----- Arrange -----
            migration.migrate(connection)
            val emptyAfterMigration = connection.countRows("book_user_tags")

            // ----- Act -----
            connection.execSQL(
                "INSERT INTO book_user_tags (bookId, category, name, spoiler) VALUES (1, 'genre', 'sci-fi', 0)",
            )

            // ----- Assert -----
            emptyAfterMigration shouldBe 0
            connection.snapshot(
                "book_user_tags",
                listOf("bookId", "category", "name", "spoiler"),
            ) shouldBe listOf(listOf("1", "genre", "sci-fi", "0"))
        }
    }
}
