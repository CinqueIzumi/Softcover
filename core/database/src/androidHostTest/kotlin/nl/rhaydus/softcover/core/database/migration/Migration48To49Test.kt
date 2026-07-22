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

class Migration48To49Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 48 && it.endVersion == 49 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV48Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun buildV48Schema(conn: SQLiteConnection) {
        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pending_user_book_writes (
                localId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                kind TEXT NOT NULL,
                userBookId INTEGER NOT NULL,
                userBookReadId INTEGER NOT NULL,
                bookId INTEGER NOT NULL,
                editionId INTEGER,
                progressPages INTEGER,
                progressSeconds INTEGER,
                startedAt TEXT,
                finishedAt TEXT,
                rating REAL,
                reviewSlateJson TEXT,
                reviewHasSpoilers INTEGER,
                enqueuedAt TEXT NOT NULL,
                attempts INTEGER NOT NULL DEFAULT 0
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
    fun `migration runs cleanly from a real v48 database`() {
        // ----- Act / Assert -----
        migration.migrate(connection)
    }

    @Nested
    inner class ActionAtColumn {
        @Test
        fun `pending_user_book_writes has actionAt column after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("pending_user_book_writes").contains("actionAt").shouldBeTrue()
        }

        @Test
        fun `existing row survives migration with actionAt reading NULL`() {
            // ----- Arrange -----
            connection.execSQL(
                """
                INSERT INTO pending_user_book_writes (
                    kind, userBookId, userBookReadId, bookId, editionId,
                    progressPages, progressSeconds, startedAt, finishedAt,
                    rating, reviewSlateJson, reviewHasSpoilers, enqueuedAt, attempts
                ) VALUES (
                    'PROGRESS', 10, 20, 30, NULL,
                    150, NULL, NULL, NULL,
                    NULL, NULL, 0, '2024-01-01T00:00:00', 0
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT kind, userBookId, actionAt FROM pending_user_book_writes WHERE userBookId = 10",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getText(0) shouldBe "PROGRESS"
                stmt.getLong(1) shouldBe 10L
                stmt.isNull(2).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `a row can be inserted and read back with a non-null actionAt after migration`() {
            // ----- Arrange -----
            migration.migrate(connection)

            // ----- Act -----
            connection.execSQL(
                """
                INSERT INTO pending_user_book_writes (
                    kind, userBookId, userBookReadId, bookId, editionId,
                    progressPages, progressSeconds, startedAt, finishedAt,
                    rating, reviewSlateJson, reviewHasSpoilers, enqueuedAt, attempts, actionAt
                ) VALUES (
                    'PROGRESS', 11, 21, 31, NULL,
                    200, NULL, NULL, NULL,
                    NULL, NULL, 0, '2024-02-01T00:00:00', 0, '2024-01-15T09:30:00'
                )
                """.trimIndent(),
            )

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT actionAt FROM pending_user_book_writes WHERE userBookId = 11",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getText(0) shouldBe "2024-01-15T09:30:00"
            } finally {
                stmt.close()
            }
        }
    }
}
