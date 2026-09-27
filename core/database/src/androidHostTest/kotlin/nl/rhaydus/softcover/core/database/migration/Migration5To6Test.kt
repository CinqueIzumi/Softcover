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

class Migration5To6Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 5 && it.endVersion == 6 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV5Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }
    // region Helpers
    private fun buildV5Schema(conn: SQLiteConnection) {
        // v5 books table: book fields + denormalized user-book fields (userBook_id doubles as the
        // FK for user_book_reads — the migration SELECTs it as userBookId in both INSERT statements)
        conn.execSQL(
            """
            CREATE TABLE books (
                id INTEGER NOT NULL PRIMARY KEY,
                title TEXT NOT NULL,
                defaultEditionId INTEGER,
                rating REAL NOT NULL,
                description TEXT NOT NULL,
                releaseYear INTEGER NOT NULL,
                coverUrl TEXT NOT NULL,
                usersCount INTEGER NOT NULL,
                userBook_id INTEGER,
                userBook_statusCode INTEGER,
                userBook_dateAdded TEXT,
                userBook_privacySettingId INTEGER,
                userBook_reviewHasSpoilers INTEGER,
                userBook_editionId INTEGER,
                userBook_lastReadDate TEXT,
                userBook_rating REAL,
                userBook_referrerUserId INTEGER,
                userBook_reviewedAt TEXT,
                userBook_updatedAt TEXT,
                userBookRead_id INTEGER,
                userBookRead_currentPage INTEGER,
                userBookRead_progress REAL,
                userBookRead_startedAt TEXT,
                userBookRead_finishedAt TEXT
            )
            """.trimIndent(),
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

    private fun tableExists(name: String): Boolean {
        val stmt = connection.prepare(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
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
    inner class UserBooksTableCreation {
        @Test
        fun `user_books table exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            tableExists("user_books").shouldBeTrue()
        }

        @Test
        fun `user_books has expected columns`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("user_books").shouldContainExactlyInAnyOrder(
                "id", "bookId", "statusCode", "dateAdded",
                "privacySettingId", "reviewHasSpoilers", "editionId",
                "lastReadDate", "rating", "referrerUserId", "reviewedAt", "updatedAt",
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
    inner class UserBookReadsTableCreation {
        @Test
        fun `user_book_reads table exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            tableExists("user_book_reads").shouldBeTrue()
        }

        @Test
        fun `user_book_reads has expected columns`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("user_book_reads").shouldContainExactlyInAnyOrder(
                "id", "userBookId", "currentPage", "progress", "startedAt", "finishedAt",
            )
        }

        @Test
        fun `index_user_book_reads_userBookId exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            indexExists("index_user_book_reads_userBookId").shouldBeTrue()
        }
    }

    @Nested
    inner class BooksTableRebuild {
        @Test
        fun `books table retains only the 8 denormalized columns`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            columnNames("books").shouldContainExactlyInAnyOrder(
                "id", "title", "defaultEditionId", "rating",
                "description", "releaseYear", "coverUrl", "usersCount",
            )
        }

        @Test
        fun `no userBook_ columns remain on books after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val cols = columnNames("books")

            cols.none { it.startsWith("userBook_") }.shouldBeTrue()
            cols.none { it.startsWith("userBookRead_") }.shouldBeTrue()
        }
    }

    @Nested
    inner class DataMigration {
        @Test
        fun `book with user-book data is migrated to user_books`() {
            // ----- Arrange -----
            connection.execSQL(
                """
                INSERT INTO books (
                    id, title, defaultEditionId, rating, description, releaseYear, coverUrl, usersCount,
                    userBook_id, userBook_statusCode, userBook_dateAdded, userBook_privacySettingId,
                    userBook_reviewHasSpoilers, userBook_editionId, userBook_lastReadDate, userBook_rating,
                    userBook_referrerUserId, userBook_reviewedAt, userBook_updatedAt,
                    userBookRead_id, userBookRead_currentPage, userBookRead_progress,
                    userBookRead_startedAt, userBookRead_finishedAt
                ) VALUES (
                    1, 'Dune', 100, 4.5, 'Sci-fi epic', 1965, 'https://cover.url', 5000,
                    42, 2, '2024-01-01', 1,
                    0, 100, NULL, 4.0,
                    NULL, NULL, NULL,
                    7, 150, 0.5,
                    '2024-01-01', NULL
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare("SELECT id, bookId, statusCode, editionId FROM user_books")

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 42L   // userBook_id → id
                stmt.getLong(1) shouldBe 1L    // books.id → bookId
                stmt.getLong(2) shouldBe 2L    // userBook_statusCode → statusCode
                stmt.getLong(3) shouldBe 100L  // userBook_editionId → editionId
                stmt.step().shouldBeFalse()    // exactly one row
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `book with user-book-read data is migrated to user_book_reads`() {
            // ----- Arrange -----
            // userBook_id (42) is also the userBookId FK for the user_book_reads row
            connection.execSQL(
                """
                INSERT INTO books (
                    id, title, defaultEditionId, rating, description, releaseYear, coverUrl, usersCount,
                    userBook_id, userBook_statusCode, userBook_dateAdded, userBook_privacySettingId,
                    userBook_reviewHasSpoilers, userBook_editionId, userBook_lastReadDate, userBook_rating,
                    userBook_referrerUserId, userBook_reviewedAt, userBook_updatedAt,
                    userBookRead_id, userBookRead_currentPage, userBookRead_progress,
                    userBookRead_startedAt, userBookRead_finishedAt
                ) VALUES (
                    1, 'Dune', 100, 4.5, 'Sci-fi epic', 1965, 'https://cover.url', 5000,
                    42, 2, '2024-01-01', 1,
                    0, 100, NULL, 4.0,
                    NULL, NULL, NULL,
                    7, 150, 0.5,
                    '2024-01-01', '2024-03-01'
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT id, userBookId, currentPage, progress, startedAt, finishedAt FROM user_book_reads",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 7L                 // userBookRead_id → id
                stmt.getLong(1) shouldBe 42L                // userBook_id → userBookId
                stmt.getLong(2) shouldBe 150L               // userBookRead_currentPage
                stmt.getDouble(3) shouldBe 0.5              // userBookRead_progress
                stmt.getText(4) shouldBe "2024-01-01"       // userBookRead_startedAt
                stmt.getText(5) shouldBe "2024-03-01"       // userBookRead_finishedAt
                stmt.step().shouldBeFalse()
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `book without user-book data produces no user_books row`() {
            // ----- Arrange -----
            connection.execSQL(
                """
                INSERT INTO books (
                    id, title, defaultEditionId, rating, description, releaseYear, coverUrl, usersCount,
                    userBook_id, userBook_statusCode, userBook_dateAdded, userBook_privacySettingId,
                    userBook_reviewHasSpoilers, userBook_editionId, userBook_lastReadDate, userBook_rating,
                    userBook_referrerUserId, userBook_reviewedAt, userBook_updatedAt,
                    userBookRead_id, userBookRead_currentPage, userBookRead_progress,
                    userBookRead_startedAt, userBookRead_finishedAt
                ) VALUES (
                    2, 'Foundation', NULL, 4.2, 'Space opera', 1951, 'https://cover2.url', 3000,
                    NULL, NULL, NULL, NULL,
                    NULL, NULL, NULL, NULL,
                    NULL, NULL, NULL,
                    NULL, NULL, NULL,
                    NULL, NULL
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare("SELECT COUNT(*) FROM user_books")

            try {
                stmt.step()
                stmt.getLong(0) shouldBe 0L
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `both books (with and without user-book data) survive in books after migration`() {
            // ----- Arrange -----
            connection.execSQL(
                """
                INSERT INTO books (
                    id, title, defaultEditionId, rating, description, releaseYear, coverUrl, usersCount,
                    userBook_id, userBook_statusCode, userBook_dateAdded, userBook_privacySettingId,
                    userBook_reviewHasSpoilers, userBook_editionId, userBook_lastReadDate, userBook_rating,
                    userBook_referrerUserId, userBook_reviewedAt, userBook_updatedAt,
                    userBookRead_id, userBookRead_currentPage, userBookRead_progress,
                    userBookRead_startedAt, userBookRead_finishedAt
                ) VALUES (
                    1, 'Dune', 100, 4.5, 'Sci-fi epic', 1965, 'https://cover.url', 5000,
                    42, 2, '2024-01-01', 1,
                    0, 100, NULL, 4.0,
                    NULL, NULL, NULL,
                    7, 150, 0.5,
                    '2024-01-01', NULL
                )
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO books (
                    id, title, defaultEditionId, rating, description, releaseYear, coverUrl, usersCount,
                    userBook_id, userBook_statusCode, userBook_dateAdded, userBook_privacySettingId,
                    userBook_reviewHasSpoilers, userBook_editionId, userBook_lastReadDate, userBook_rating,
                    userBook_referrerUserId, userBook_reviewedAt, userBook_updatedAt,
                    userBookRead_id, userBookRead_currentPage, userBookRead_progress,
                    userBookRead_startedAt, userBookRead_finishedAt
                ) VALUES (
                    2, 'Foundation', NULL, 4.2, 'Space opera', 1951, 'https://cover2.url', 3000,
                    NULL, NULL, NULL, NULL,
                    NULL, NULL, NULL, NULL,
                    NULL, NULL, NULL,
                    NULL, NULL, NULL,
                    NULL, NULL
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare("SELECT COUNT(*) FROM books")

            try {
                stmt.step()
                stmt.getLong(0) shouldBe 2L
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `book fields are carried across correctly in new books table`() {
            // ----- Arrange -----
            connection.execSQL(
                """
                INSERT INTO books (
                    id, title, defaultEditionId, rating, description, releaseYear, coverUrl, usersCount,
                    userBook_id, userBook_statusCode, userBook_dateAdded, userBook_privacySettingId,
                    userBook_reviewHasSpoilers, userBook_editionId, userBook_lastReadDate, userBook_rating,
                    userBook_referrerUserId, userBook_reviewedAt, userBook_updatedAt,
                    userBookRead_id, userBookRead_currentPage, userBookRead_progress,
                    userBookRead_startedAt, userBookRead_finishedAt
                ) VALUES (
                    1, 'Dune', 100, 4.5, 'Sci-fi epic', 1965, 'https://cover.url', 5000,
                    NULL, NULL, NULL, NULL,
                    NULL, NULL, NULL, NULL,
                    NULL, NULL, NULL,
                    NULL, NULL, NULL,
                    NULL, NULL
                )
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT id, title, defaultEditionId, rating, description, releaseYear, coverUrl, usersCount FROM books",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 1L
                stmt.getText(1) shouldBe "Dune"
                stmt.getLong(2) shouldBe 100L
                stmt.getDouble(3) shouldBe 4.5
                stmt.getText(4) shouldBe "Sci-fi epic"
                stmt.getLong(5) shouldBe 1965L
                stmt.getText(6) shouldBe "https://cover.url"
                stmt.getLong(7) shouldBe 5000L
            } finally {
                stmt.close()
            }
        }
    }
}
