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

class Migration46To47Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 46 && it.endVersion == 47 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV46Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun buildV46Schema(conn: SQLiteConnection) {
        conn.execSQL(
            """
            CREATE TABLE dismissed_continue_series (
                seriesId INTEGER NOT NULL,
                seriesName TEXT,
                coverUrl TEXT,
                PRIMARY KEY(seriesId)
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

    @Test
    fun `migration runs cleanly from a real v46 database`() {
        // ----- Act / Assert -----
        migration.migrate(connection)
    }

    @Nested
    inner class DismissedContinueSeriesColumns {
        @Test
        fun `authorText and bookCount columns exist after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val columns = columnNames("dismissed_continue_series")
            columns.contains("authorText").shouldBeTrue()
            columns.contains("bookCount").shouldBeTrue()
        }

        @Test
        fun `existing row is preserved with authorText and bookCount defaulting to NULL`() {
            // ----- Arrange -----
            connection.execSQL(
                "INSERT INTO dismissed_continue_series (seriesId, seriesName, coverUrl) " +
                    "VALUES (10, 'Foundation', 'https://example.com/foundation.jpg')",
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT seriesId, seriesName, coverUrl, authorText, bookCount " +
                    "FROM dismissed_continue_series WHERE seriesId = 10",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 10L
                stmt.getText(1) shouldBe "Foundation"
                stmt.getText(2) shouldBe "https://example.com/foundation.jpg"
                stmt.isNull(3).shouldBeTrue()
                stmt.isNull(4).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `authorText and bookCount can be written to and read back after migration`() {
            // ----- Arrange -----
            connection.execSQL(
                "INSERT INTO dismissed_continue_series (seriesId, seriesName, coverUrl) " +
                    "VALUES (20, 'Culture Series', 'https://example.com/culture.jpg')",
            )

            migration.migrate(connection)

            // ----- Act -----
            connection.execSQL(
                "UPDATE dismissed_continue_series SET authorText = 'Iain M. Banks', bookCount = 9 " +
                    "WHERE seriesId = 20",
            )

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT authorText, bookCount FROM dismissed_continue_series WHERE seriesId = 20",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getText(0) shouldBe "Iain M. Banks"
                stmt.getLong(1) shouldBe 9L
            } finally {
                stmt.close()
            }
        }
    }
}
