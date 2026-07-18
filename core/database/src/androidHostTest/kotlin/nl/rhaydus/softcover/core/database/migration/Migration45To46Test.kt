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

class Migration45To46Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 45 && it.endVersion == 46 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV45Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun buildV45Schema(conn: SQLiteConnection) {
        conn.execSQL(
            """
            CREATE TABLE book_lists (
                id INTEGER NOT NULL,
                name TEXT NOT NULL,
                slug TEXT NOT NULL,
                ranked INTEGER NOT NULL,
                signature TEXT,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
        )

        conn.execSQL(
            """
            CREATE TABLE pending_list_writes (
                localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                kind TEXT NOT NULL,
                listId INTEGER,
                listName TEXT,
                bookId INTEGER,
                editionId INTEGER,
                listBookId INTEGER,
                startPosition INTEGER,
                orderedListBookIdsCsv TEXT,
                enqueuedAt TEXT NOT NULL,
                attempts INTEGER NOT NULL
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
    fun `migration runs cleanly from a real v45 database`() {
        // ----- Act / Assert -----
        migration.migrate(connection)
    }

    @Nested
    inner class BookListsColumns {
        @Test
        fun `privacySettingId column exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val columns = columnNames("book_lists")
            columns.contains("privacySettingId").shouldBeTrue()
        }

        @Test
        fun `existing row is preserved with privacySettingId defaulting to PUBLIC`() {
            // ----- Arrange -----
            connection.execSQL(
                "INSERT INTO book_lists (id, name, slug, ranked) " +
                    "VALUES (1, 'To Read', 'to-read', 0)",
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT id, privacySettingId FROM book_lists WHERE id = 1",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 1L
                stmt.getLong(1) shouldBe 1L
            } finally {
                stmt.close()
            }
        }
    }

    @Nested
    inner class PendingListWritesColumns {
        @Test
        fun `privacySettingId column exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val columns = columnNames("pending_list_writes")
            columns.contains("privacySettingId").shouldBeTrue()
        }

        @Test
        fun `existing row is preserved with privacySettingId defaulting to NULL`() {
            // ----- Arrange -----
            connection.execSQL(
                "INSERT INTO pending_list_writes (kind, enqueuedAt, attempts) " +
                    "VALUES ('CREATE', '2026-01-01T00:00:00Z', 0)",
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT localId, privacySettingId FROM pending_list_writes WHERE localId = 1",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 1L
                stmt.isNull(1).shouldBeTrue()
            } finally {
                stmt.close()
            }
        }
    }
}
