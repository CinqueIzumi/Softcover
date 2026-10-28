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

class Migration63To64Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 63 && it.endVersion == 64 }

    private val schema63 = ExportedSchema.load(version = 63)

    private val exported = ExportedSchema.load(version = 64)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema63.createIn(connection)
        schema63.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v63 table keeps identical rows`() {
            // ----- Arrange -----
            val rowsBefore = schema63.tables.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            schema63.tables.forEach { table ->
                connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                ) shouldBe rowsBefore.getValue(table.name)
            }
        }
    }

    @Nested
    inner class PagesReadColumn {
        @Test
        fun `existing progress history rows get zero pages read`() {
            // ----- Arrange -----
            connection.execSQL("DELETE FROM progress_history")
            connection.execSQL(
                "INSERT INTO progress_history (bookId, actionAtEpochMillis, pages, seconds, pending) " +
                    "VALUES (1, 1000, 40, NULL, 0), (1, 2000, 90, NULL, 1)",
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            connection.snapshot(
                "progress_history",
                listOf("actionAtEpochMillis", "pages", "pending", "pagesRead"),
            ) shouldBe listOf(
                listOf("1000", "40", "0", "0"),
                listOf("2000", "90", "1", "0"),
            )
        }

        @Test
        fun `resulting tables, columns, indices and views equal the v64 schema`() {
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
