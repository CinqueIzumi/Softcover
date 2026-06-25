package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration35To36Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 35 && it.endVersion == 36 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV35Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }
    // region Helpers
    private fun buildV35Schema(conn: SQLiteConnection) {
        // books is a parent for the user_books FK — minimal schema is enough
        conn.execSQL(
            """
            CREATE TABLE books (
                id INTEGER NOT NULL PRIMARY KEY,
                title TEXT NOT NULL
            )
            """.trimIndent(),
        )

        // v35 user_books — includes `review TEXT` (added in 33→34, dropped here)
        // and `createdAt` (added in 17→18)
        conn.execSQL(
            """
            CREATE TABLE user_books (
                id INTEGER NOT NULL PRIMARY KEY,
                bookId INTEGER NOT NULL,
                statusCode INTEGER NOT NULL,
                dateAdded TEXT NOT NULL,
                privacySettingId INTEGER NOT NULL,
                reviewHasSpoilers INTEGER NOT NULL,
                editionId INTEGER,
                lastReadDate TEXT,
                rating REAL,
                referrerUserId INTEGER,
                review TEXT,
                reviewedAt TEXT,
                updatedAt TEXT,
                createdAt TEXT,
                FOREIGN KEY(bookId) REFERENCES books(id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        conn.execSQL("CREATE INDEX index_user_books_bookId ON user_books(bookId)")
        conn.execSQL("CREATE INDEX index_user_books_statusCode ON user_books(statusCode)")

        // v35 pending_user_book_writes — includes `reviewBody TEXT` (added in 32→33, dropped here)
        conn.execSQL(
            """
            CREATE TABLE pending_user_book_writes (
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
                reviewBody TEXT,
                reviewHasSpoilers INTEGER,
                enqueuedAt TEXT NOT NULL,
                attempts INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        conn.execSQL(
            "CREATE UNIQUE INDEX index_pending_user_book_writes_userBookId_kind " +
                    "ON pending_user_book_writes(userBookId, kind)",
        )
    }

    private fun columnNames(table: String): List<String> {
        val cols = mutableListOf<String>()
        val stmt = connection.prepare("PRAGMA table_info($table)")

        try {
            while (stmt.step()) {
                cols.add(stmt.getText(1))
            }
        } finally {
            stmt.close()
        }

        return cols
    }

    private fun indexExists(name: String): Boolean {
        val stmt = connection.prepare(
            "SELECT name FROM sqlite_master WHERE type='index' AND name=?",
        )

        try {
            stmt.bindText(
                1,
                name,
            )
            return stmt.step()
        } finally {
            stmt.close()
        }
    }
    // endregion
    @Nested
    inner class UserBooksTableRebuild {
        @Test
        fun `user_books has reviewSlateJson column after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("user_books").contains("reviewSlateJson").shouldBeTrue()
        }

        @Test
        fun `user_books does not have review column after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("user_books").contains("review").shouldBeFalse()
        }

        @Test
        fun `user_books has all expected columns after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("user_books").shouldContainExactlyInAnyOrder(
                "id", "bookId", "statusCode", "dateAdded",
                "privacySettingId", "reviewHasSpoilers", "editionId",
                "lastReadDate", "rating", "referrerUserId",
                "reviewSlateJson", "reviewedAt", "updatedAt", "createdAt",
            )
        }

        @Test
        fun `index_user_books_bookId exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            indexExists("index_user_books_bookId").shouldBeTrue()
        }

        @Test
        fun `index_user_books_statusCode exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            indexExists("index_user_books_statusCode").shouldBeTrue()
        }
    }

    @Nested
    inner class PendingUserBookWritesTableRebuild {
        @Test
        fun `pending_user_book_writes has reviewSlateJson column after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("pending_user_book_writes").contains("reviewSlateJson").shouldBeTrue()
        }

        @Test
        fun `pending_user_book_writes does not have reviewBody column after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("pending_user_book_writes").contains("reviewBody").shouldBeFalse()
        }

        @Test
        fun `pending_user_book_writes has all expected columns after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("pending_user_book_writes").shouldContainExactlyInAnyOrder(
                "localId", "kind", "userBookId", "userBookReadId", "bookId",
                "editionId", "progressPages", "progressSeconds", "startedAt", "finishedAt",
                "rating", "reviewSlateJson", "reviewHasSpoilers", "enqueuedAt", "attempts",
            )
        }

        @Test
        fun `unique index on pending_user_book_writes exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            indexExists("index_pending_user_book_writes_userBookId_kind").shouldBeTrue()
        }
    }

    @Nested
    inner class DataMigration {
        @Test
        fun `user_books row reviewSlateJson is NULL for migrated rows`() {
            // ----- Arrange -----
            connection.execSQL("INSERT INTO books (id, title) VALUES (1, 'Dune')")
            connection.execSQL(
                """
                INSERT INTO user_books (
                    id, bookId, statusCode, dateAdded, privacySettingId, reviewHasSpoilers,
                    editionId, lastReadDate, rating, referrerUserId, review, reviewedAt, updatedAt, createdAt
                ) VALUES (
                    10, 1, 2, '2024-01-01', 1, 0,
                    100, NULL, 4.5, NULL, 'A great book', '2024-06-01', '2024-06-01', '2023-12-01'
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare("SELECT reviewSlateJson FROM user_books WHERE id = 10")

            try {
                stmt.step().shouldBeTrue()
                stmt.isNull(0).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `user_books non-review fields are preserved after migration`() {
            // ----- Arrange -----
            connection.execSQL("INSERT INTO books (id, title) VALUES (1, 'Dune')")
            connection.execSQL(
                """
                INSERT INTO user_books (
                    id, bookId, statusCode, dateAdded, privacySettingId, reviewHasSpoilers,
                    editionId, lastReadDate, rating, referrerUserId, review, reviewedAt, updatedAt, createdAt
                ) VALUES (
                    10, 1, 2, '2024-01-01', 1, 0,
                    100, '2024-05-01', 4.5, NULL, 'A great book', '2024-06-01', '2024-06-02', '2023-12-01'
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT id, bookId, statusCode, dateAdded, privacySettingId, reviewHasSpoilers, editionId, lastReadDate, rating, createdAt FROM user_books",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 10L
                stmt.getLong(1) shouldBe 1L
                stmt.getLong(2) shouldBe 2L
                stmt.getText(3) shouldBe "2024-01-01"
                stmt.getLong(4) shouldBe 1L
                stmt.getLong(5) shouldBe 0L
                stmt.getLong(6) shouldBe 100L
                stmt.getText(7) shouldBe "2024-05-01"
                stmt.getDouble(8) shouldBe 4.5
                stmt.getText(9) shouldBe "2023-12-01"
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `pending_user_book_writes row reviewSlateJson is NULL for migrated rows`() {
            // ----- Arrange -----
            connection.execSQL(
                """
                INSERT INTO pending_user_book_writes (
                    kind, userBookId, userBookReadId, bookId, editionId,
                    progressPages, progressSeconds, startedAt, finishedAt,
                    rating, reviewBody, reviewHasSpoilers, enqueuedAt, attempts
                ) VALUES (
                    'REVIEW', 10, 20, 30, NULL,
                    NULL, NULL, NULL, NULL,
                    NULL, 'Old review text', 0, '2024-01-01T00:00:00', 0
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT reviewSlateJson FROM pending_user_book_writes",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.isNull(0).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `pending_user_book_writes non-review fields are preserved after migration`() {
            // ----- Arrange -----
            connection.execSQL(
                """
                INSERT INTO pending_user_book_writes (
                    kind, userBookId, userBookReadId, bookId, editionId,
                    progressPages, progressSeconds, startedAt, finishedAt,
                    rating, reviewBody, reviewHasSpoilers, enqueuedAt, attempts
                ) VALUES (
                    'PROGRESS', 10, 20, 30, 100,
                    150, NULL, '2024-01-01', NULL,
                    4.0, NULL, NULL, '2024-01-01T00:00:00', 2
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT kind, userBookId, userBookReadId, bookId, editionId, progressPages, rating, enqueuedAt, attempts FROM pending_user_book_writes",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getText(0) shouldBe "PROGRESS"
                stmt.getLong(1) shouldBe 10L
                stmt.getLong(2) shouldBe 20L
                stmt.getLong(3) shouldBe 30L
                stmt.getLong(4) shouldBe 100L
                stmt.getLong(5) shouldBe 150L
                stmt.getDouble(6) shouldBe 4.0
                stmt.getText(7) shouldBe "2024-01-01T00:00:00"
                stmt.getLong(8) shouldBe 2L
            } finally {
                stmt.close()
            }
        }
    }
}
