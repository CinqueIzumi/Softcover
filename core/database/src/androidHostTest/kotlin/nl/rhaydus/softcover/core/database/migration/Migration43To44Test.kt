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

class Migration43To44Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 43 && it.endVersion == 44 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV43Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    // region Helpers
    private fun buildV43Schema(conn: SQLiteConnection) {
        conn.execSQL(
            "CREATE TABLE dismissed_continue_series_books (bookId INTEGER NOT NULL, PRIMARY KEY(bookId))",
        )

        conn.execSQL(
            "CREATE TABLE dismissed_continue_series (seriesId INTEGER NOT NULL, PRIMARY KEY(seriesId))",
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
    // endregion
    @Nested
    inner class DismissedContinueSeriesBooksColumns {
        @Test
        fun `bookTitle, coverUrl, authorText and seriesName columns exist after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val columns = columnNames("dismissed_continue_series_books")
            columns.contains("bookTitle").shouldBeTrue()
            columns.contains("coverUrl").shouldBeTrue()
            columns.contains("authorText").shouldBeTrue()
            columns.contains("seriesName").shouldBeTrue()
        }

        @Test
        fun `existing row is preserved with the new columns defaulting to NULL`() {
            // ----- Arrange -----
            connection.execSQL("INSERT INTO dismissed_continue_series_books (bookId) VALUES (1)")

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT bookId, bookTitle, coverUrl, authorText, seriesName " +
                    "FROM dismissed_continue_series_books WHERE bookId = 1",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 1L
                stmt.isNull(1).shouldBeTrue()
                stmt.isNull(2).shouldBeTrue()
                stmt.isNull(3).shouldBeTrue()
                stmt.isNull(4).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }
    }

    @Nested
    inner class DismissedContinueSeriesColumns {
        @Test
        fun `seriesName and coverUrl columns exist after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val columns = columnNames("dismissed_continue_series")
            columns.contains("seriesName").shouldBeTrue()
            columns.contains("coverUrl").shouldBeTrue()
        }

        @Test
        fun `existing row is preserved with the new columns defaulting to NULL`() {
            // ----- Arrange -----
            connection.execSQL("INSERT INTO dismissed_continue_series (seriesId) VALUES (10)")

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT seriesId, seriesName, coverUrl FROM dismissed_continue_series WHERE seriesId = 10",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 10L
                stmt.isNull(1).shouldBeTrue()
                stmt.isNull(2).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }
    }
}
