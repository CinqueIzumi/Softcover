package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration31To32Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 31 && it.endVersion == 32 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV31Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }
    // region Helpers
    private fun buildV31Schema(conn: SQLiteConnection) {
        conn.execSQL(
            """
            CREATE TABLE pending_progress_updates (
                localId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                kind TEXT NOT NULL,
                userBookId INTEGER NOT NULL,
                userBookReadId INTEGER NOT NULL,
                bookId INTEGER NOT NULL,
                editionId INTEGER,
                progressPages INTEGER,
                rating REAL
            )
            """.trimIndent(),
        )
        conn.execSQL(
            "CREATE UNIQUE INDEX index_pending_progress_updates_userBookId_kind " +
                    "ON pending_progress_updates(userBookId, kind)",
        )
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
    inner class TableRename {
        @Test
        fun `pending_user_book_writes table exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            tableExists("pending_user_book_writes").shouldBeTrue()
        }

        @Test
        fun `pending_progress_updates table no longer exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            tableExists("pending_progress_updates").shouldBeFalse()
        }
    }

    @Nested
    inner class IndexRename {
        @Test
        fun `new unique index on pending_user_book_writes exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            indexExists("index_pending_user_book_writes_userBookId_kind").shouldBeTrue()
        }

        @Test
        fun `old index on pending_progress_updates no longer exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            indexExists("index_pending_progress_updates_userBookId_kind").shouldBeFalse()
        }
    }

    @Nested
    inner class DataPreservation {
        @Test
        fun `existing rows survive the rename with values intact`() {
            // ----- Arrange -----
            connection.execSQL(
                """
                INSERT INTO pending_progress_updates (kind, userBookId, userBookReadId, bookId, progressPages, rating)
                VALUES ('PROGRESS', 10, 20, 30, 100, 4.0)
                """.trimIndent(),
            )
            connection.execSQL(
                """
                INSERT INTO pending_progress_updates (kind, userBookId, userBookReadId, bookId, progressPages, rating)
                VALUES ('FINISH', 11, 21, 31, NULL, NULL)
                """.trimIndent(),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT kind, userBookId, userBookReadId, bookId, progressPages, rating FROM pending_user_book_writes ORDER BY userBookId",
            )

            try {
                // Row 1
                stmt.step().shouldBeTrue()
                stmt.getText(0) shouldBe "PROGRESS"
                stmt.getLong(1) shouldBe 10L
                stmt.getLong(2) shouldBe 20L
                stmt.getLong(3) shouldBe 30L
                stmt.getLong(4) shouldBe 100L
                stmt.getDouble(5) shouldBe 4.0

                // Row 2
                stmt.step().shouldBeTrue()
                stmt.getText(0) shouldBe "FINISH"
                stmt.getLong(1) shouldBe 11L
                stmt.isNull(4).shouldBeTrue()
                stmt.isNull(5).shouldBeTrue()

                stmt.step().shouldBeFalse()
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `row count is preserved after migration`() {
            // ----- Arrange -----
            connection.execSQL(
                "INSERT INTO pending_progress_updates (kind, userBookId, userBookReadId, bookId) VALUES ('PROGRESS', 1, 2, 3)",
            )
            connection.execSQL(
                "INSERT INTO pending_progress_updates (kind, userBookId, userBookReadId, bookId) VALUES ('FINISH', 4, 5, 6)",
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare("SELECT COUNT(*) FROM pending_user_book_writes")

            try {
                stmt.step()
                stmt.getLong(0) shouldBe 2L
            } finally {
                stmt.close()
            }
        }
    }
}
