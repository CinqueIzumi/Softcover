package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.database.SoftcoverDatabase

class Migration44To45Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 44 && it.endVersion == 45 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV44Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun buildV44Schema(conn: SQLiteConnection) {
        conn.execSQL(
            """
            CREATE TABLE dismissed_continue_series_books (
                bookId INTEGER NOT NULL,
                bookTitle TEXT,
                coverUrl TEXT,
                authorText TEXT,
                seriesName TEXT,
                PRIMARY KEY(bookId)
            )
            """.trimIndent(),
        )
    }

    private fun columnNames(table: String): List<String> {
        val stmt = connection.prepare("PRAGMA table_info($table)")

        try {
            val names = mutableListOf<String>()

            while (stmt.step()) {
                // PRAGMA table_info columns: cid, name, type, notnull, dflt_value, pk
                names.add(stmt.getText(1))
            }

            return names
        } finally {
            stmt.close()
        }
    }

    @Nested
    inner class DismissedContinueSeriesBooksColumns {
        @Test
        fun `seriesId and seriesPosition columns exist after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val columns = columnNames("dismissed_continue_series_books")
            columns.contains("seriesId").shouldBeTrue()
            columns.contains("seriesPosition").shouldBeTrue()
        }

        @Test
        fun `existing row is preserved with the new columns defaulting to NULL`() {
            // ----- Arrange -----
            connection.execSQL("INSERT INTO dismissed_continue_series_books (bookId) VALUES (1)")

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT bookId, seriesId, seriesPosition " +
                    "FROM dismissed_continue_series_books WHERE bookId = 1",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 1L
                stmt.isNull(1).shouldBeTrue()
                stmt.isNull(2).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }
    }
}
