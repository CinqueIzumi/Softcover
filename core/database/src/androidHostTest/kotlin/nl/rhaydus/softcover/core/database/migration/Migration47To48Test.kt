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

class Migration47To48Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 47 && it.endVersion == 48 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV47Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun buildV47Schema(conn: SQLiteConnection) {
        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS dismissed_continue_series (
                seriesId INTEGER NOT NULL,
                seriesName TEXT,
                coverUrl TEXT,
                authorText TEXT,
                bookCount INTEGER,
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
    fun `migration runs cleanly from a real v47 database`() {
        // ----- Act / Assert -----
        migration.migrate(connection)
    }

    @Nested
    inner class UserTagVocabularyColumns {
        @Test
        fun `user_tag_vocabulary table exists with expected columns after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val columns = columnNames("user_tag_vocabulary")
            columns.contains("userId").shouldBeTrue()
            columns.contains("category").shouldBeTrue()
            columns.contains("name").shouldBeTrue()
            columns.contains("usageCount").shouldBeTrue()
        }

        @Test
        fun `userId, category and name are non-null composite key columns`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare("PRAGMA table_info(user_tag_vocabulary)")

            try {
                val notNullByName = mutableMapOf<String, Boolean>()
                val pkIndexByName = mutableMapOf<String, Long>()

                while (stmt.step()) {
                    // PRAGMA table_info columns: cid, name, type, notnull, dflt_value, pk
                    val name = stmt.getText(1)
                    notNullByName[name] = stmt.getLong(3) == 1L
                    pkIndexByName[name] = stmt.getLong(5)
                }

                notNullByName["userId"] shouldBe true
                notNullByName["category"] shouldBe true
                notNullByName["name"] shouldBe true
                (pkIndexByName.getValue("userId") >= 1L).shouldBeTrue()
                (pkIndexByName.getValue("category") >= 1L).shouldBeTrue()
                (pkIndexByName.getValue("name") >= 1L).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `usageCount is non-null with a default of 0`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare("PRAGMA table_info(user_tag_vocabulary)")

            try {
                while (stmt.step()) {
                    // PRAGMA table_info columns: cid, name, type, notnull, dflt_value, pk
                    if (stmt.getText(1) == "usageCount") {
                        stmt.getLong(3) shouldBe 1L
                        stmt.getText(4) shouldBe "0"
                    }
                }
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `usageCount can be written to and read back after migration`() {
            // ----- Arrange -----
            migration.migrate(connection)

            connection.execSQL(
                "INSERT INTO user_tag_vocabulary (userId, category, name) " +
                    "VALUES (1, 'genre', 'sci-fi')",
            )

            // ----- Act -----
            connection.execSQL(
                "UPDATE user_tag_vocabulary SET usageCount = 4 " +
                    "WHERE userId = 1 AND category = 'genre' AND name = 'sci-fi'",
            )

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT usageCount FROM user_tag_vocabulary " +
                    "WHERE userId = 1 AND category = 'genre' AND name = 'sci-fi'",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 4L
            } finally {
                stmt.close()
            }
        }
    }

    @Nested
    inner class DismissedContinueSeriesSurvival {
        @Test
        fun `existing dismissed_continue_series row is preserved untouched by the migration`() {
            // ----- Arrange -----
            connection.execSQL(
                "INSERT INTO dismissed_continue_series " +
                    "(seriesId, seriesName, coverUrl, authorText, bookCount) VALUES " +
                    "(10, 'Foundation', 'https://example.com/foundation.jpg', 'Isaac Asimov', 7)",
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
                stmt.getText(3) shouldBe "Isaac Asimov"
                stmt.getLong(4) shouldBe 7L
            } finally {
                stmt.close()
            }
        }
    }
}
