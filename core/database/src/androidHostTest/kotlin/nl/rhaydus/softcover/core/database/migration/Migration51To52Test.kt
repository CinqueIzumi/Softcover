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

class Migration51To52Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 51 && it.endVersion == 52 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV51Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun buildV51Schema(conn: SQLiteConnection) {
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
                actionAt TEXT,
                progressHistoryId INTEGER
            )
            """.trimIndent(),
        )
    }

    private fun columnNames(table: String): List<String> {
        val stmt = connection.prepare("PRAGMA table_info($table)")

        try {
            val names = mutableListOf<String>()

            while (stmt.step()) {
                names.add(stmt.getText(1))
            }

            return names
        } finally {
            stmt.close()
        }
    }

    @Test
    fun `migration runs cleanly from a real v51 database`() {
        // ----- Arrange -----
        val db = connection

        // ----- Act -----
        val result = runCatching { migration.migrate(db) }

        // ----- Assert -----
        result.isSuccess.shouldBeTrue()
    }

    @Nested
    inner class StatusCodeColumn {
        @Test
        fun `pending_user_book_writes gains a statusCode column`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            columnNames("pending_user_book_writes") shouldContain "statusCode"
        }

        @Test
        fun `existing pending write survives migration with statusCode reading NULL`() {
            // ----- Arrange -----
            connection.execSQL(
                """
                INSERT INTO pending_user_book_writes (
                    kind, userBookId, userBookReadId, bookId, editionId,
                    progressPages, enqueuedAt, attempts, actionAt, progressHistoryId
                ) VALUES (
                    'UPDATE_PROGRESS', 10, 20, 30, NULL,
                    150, '2024-01-01T00:00:00', 2, '2024-01-01T00:00:00', 9
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT kind, progressPages, attempts, progressHistoryId, statusCode FROM pending_user_book_writes WHERE userBookId = 10",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getText(0) shouldBe "UPDATE_PROGRESS"
                stmt.getLong(1) shouldBe 150L
                stmt.getLong(2) shouldBe 2L
                stmt.getLong(3) shouldBe 9L
                stmt.isNull(4).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }
    }
}
