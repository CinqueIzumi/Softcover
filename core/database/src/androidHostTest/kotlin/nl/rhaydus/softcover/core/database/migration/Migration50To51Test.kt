package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration50To51Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 50 && it.endVersion == 51 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV50Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun buildV50Schema(conn: SQLiteConnection) {
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
                attempts INTEGER NOT NULL DEFAULT 0,
                actionAt TEXT
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

    private fun sqliteMasterNames(type: String): List<String> {
        val stmt = connection.prepare("SELECT name FROM sqlite_master WHERE type = '$type'")

        try {
            val names = mutableListOf<String>()

            while (stmt.step()) {
                names.add(stmt.getText(0))
            }

            return names
        } finally {
            stmt.close()
        }
    }

    @Test
    fun `migration runs cleanly from a real v50 database`() {
        // ----- Arrange -----
        val db = connection

        // ----- Act -----
        val result = runCatching { migration.migrate(db) }

        // ----- Assert -----
        result.isSuccess.shouldBeTrue()
    }

    @Nested
    inner class ProgressHistoryTable {
        @Test
        fun `progress_history exists with its columns after migration`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            columnNames("progress_history") shouldBe listOf(
                "localId",
                "bookId",
                "actionAtEpochMillis",
                "pages",
                "seconds",
                "pending",
            )
        }

        @Test
        fun `progress_history has an index on bookId after migration`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            sqliteMasterNames(type = "index") shouldContain "index_progress_history_bookId"
        }
    }

    @Nested
    inner class ProgressHistoryIdColumn {
        @Test
        fun `existing pending write survives migration with progressHistoryId reading NULL`() {
            // ----- Arrange -----
            connection.execSQL(
                """
                INSERT INTO pending_user_book_writes (
                    kind, userBookId, userBookReadId, bookId, editionId,
                    progressPages, progressSeconds, startedAt, finishedAt,
                    rating, reviewSlateJson, reviewHasSpoilers, enqueuedAt, attempts, actionAt
                ) VALUES (
                    'UPDATE_PROGRESS', 10, 20, 30, NULL,
                    150, NULL, NULL, NULL,
                    NULL, NULL, 0, '2024-01-01T00:00:00', 0, '2024-01-01T00:00:00'
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("pending_user_book_writes") shouldContain "progressHistoryId"

            val stmt = connection.prepare(
                "SELECT kind, progressPages, actionAt, progressHistoryId FROM pending_user_book_writes WHERE userBookId = 10",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getText(0) shouldBe "UPDATE_PROGRESS"
                stmt.getLong(1) shouldBe 150L
                stmt.getText(2) shouldBe "2024-01-01T00:00:00"
                stmt.isNull(3).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }
    }
}
