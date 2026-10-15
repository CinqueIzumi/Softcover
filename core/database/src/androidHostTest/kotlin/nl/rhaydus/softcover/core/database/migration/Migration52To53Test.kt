package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration52To53Test {
    private lateinit var connection: SQLiteConnection

    private val schema52 = ExportedSchema.load(version = 52)
    private val schema53 = ExportedSchema.load(version = 53)

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 52 && it.endVersion == 53 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema52.createIn(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Test
    fun `migration runs cleanly from a real v52 database`() {
        // ----- Arrange -----
        val db = connection

        // ----- Act -----
        val result = runCatching { migration.migrate(db) }

        // ----- Assert -----
        result.isSuccess.shouldBeTrue()
    }

    @Nested
    inner class ReadingLogRemoval {
        @Test
        fun `reading_log_entries and its index are dropped`() {
            // ----- Arrange -----
            connection.userTables() shouldContain "reading_log_entries"
            connection.indexNames() shouldContain "index_reading_log_entries_bookId"

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            connection.userTables() shouldNotContain "reading_log_entries"
            connection.indexNames() shouldNotContain "index_reading_log_entries_bookId"
        }

        @Test
        fun `dropping succeeds when the table still holds rows`() {
            // ----- Arrange -----
            connection.execSQL(
                "INSERT INTO reading_log_entries (bookId, createdAt) VALUES (1, '2024-01-01T00:00:00')",
            )

            // ----- Act -----
            val result = runCatching { migration.migrate(connection) }

            // ----- Assert -----
            result.isSuccess.shouldBeTrue()
            connection.userTables() shouldNotContain "reading_log_entries"
        }

        @Test
        fun `running twice is harmless`() {
            // ----- Arrange -----
            migration.migrate(connection)

            // ----- Act -----
            val result = runCatching { migration.migrate(connection) }

            // ----- Assert -----
            result.isSuccess.shouldBeTrue()
        }
    }

    @Nested
    inner class EverythingElse {
        @Test
        fun `every other v52 table survives with identical columns and rows`() {
            // ----- Arrange -----
            connection.execSQL("PRAGMA foreign_keys = OFF")
            schema52.seedOneRowPerTable(connection)

            val survivors = schema52.tableNames - "reading_log_entries"
            val columnsBefore = survivors.associateWith { connection.columnNames(it) }
            val rowsBefore = survivors.associateWith { table ->
                connection.snapshot(
                    table,
                    columnsBefore.getValue(table),
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            connection.userTables() shouldBe schema53.tableNames
            survivors.forEach { table ->
                connection.columnNames(table) shouldBe columnsBefore.getValue(table)
                connection.snapshot(
                    table,
                    columnsBefore.getValue(table),
                ) shouldBe rowsBefore.getValue(table)
            }
        }

        @Test
        fun `the result has exactly the v53 tables, views and indices`() {
            // ----- Arrange -----
            val expectedIndices = schema53.tables.flatMap { it.indexNames }.toSet()

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            connection.userTables() shouldBe schema53.tableNames
            connection.viewNames() shouldBe setOf("book_edition_view")
            connection.indexNames() shouldBe expectedIndices
        }
    }
}
