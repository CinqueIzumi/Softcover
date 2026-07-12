package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.database.SoftcoverDatabase

class Migration42To43Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 42 && it.endVersion == 43 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV42Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }
    // region Helpers
    private fun buildV42Schema(conn: SQLiteConnection) {
        conn.execSQL(
            "CREATE TABLE book_author_cross_ref " +
                "(bookId INTEGER NOT NULL, authorId INTEGER NOT NULL, PRIMARY KEY(bookId, authorId))",
        )

        conn.execSQL(
            "CREATE TABLE edition_author_cross_ref " +
                "(editionId INTEGER NOT NULL, authorId INTEGER NOT NULL, PRIMARY KEY(editionId, authorId))",
        )
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
    inner class BookAuthorCrossRefIndex {
        @Test
        fun `index_book_author_cross_ref_authorId does not exist before migration`() {
            // ----- Assert -----
            indexExists("index_book_author_cross_ref_authorId").shouldBeFalse()
        }

        @Test
        fun `index_book_author_cross_ref_authorId exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            indexExists("index_book_author_cross_ref_authorId").shouldBeTrue()
        }
    }

    @Nested
    inner class EditionAuthorCrossRefIndex {
        @Test
        fun `index_edition_author_cross_ref_authorId exists after migration`() {
            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            indexExists("index_edition_author_cross_ref_authorId").shouldBeTrue()
        }
    }

    @Nested
    inner class DataPreservation {
        @Test
        fun `existing book_author_cross_ref row is preserved after migration`() {
            // ----- Arrange -----
            connection.execSQL("INSERT INTO book_author_cross_ref (bookId, authorId) VALUES (1, 10)")

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT bookId, authorId FROM book_author_cross_ref WHERE bookId = 1",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 1L
                stmt.getLong(1) shouldBe 10L
            } finally {
                stmt.close()
            }
        }

        @Test
        fun `existing edition_author_cross_ref row is preserved after migration`() {
            // ----- Arrange -----
            connection.execSQL("INSERT INTO edition_author_cross_ref (editionId, authorId) VALUES (2, 20)")

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            val stmt = connection.prepare(
                "SELECT editionId, authorId FROM edition_author_cross_ref WHERE editionId = 2",
            )

            try {
                stmt.step().shouldBeTrue()
                stmt.getLong(0) shouldBe 2L
                stmt.getLong(1) shouldBe 20L
            } finally {
                stmt.close()
            }
        }
    }
}
