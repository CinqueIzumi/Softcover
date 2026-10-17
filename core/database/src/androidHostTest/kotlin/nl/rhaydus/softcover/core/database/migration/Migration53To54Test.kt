package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration53To54Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 53 && it.endVersion == 54 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        buildV53Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun buildV53Schema(conn: SQLiteConnection) {
        conn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS roadmap_documents (
                id INTEGER NOT NULL PRIMARY KEY,
                markdown TEXT NOT NULL,
                fetchedAtEpochMillis INTEGER NOT NULL
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

    private fun primaryKeyColumns(table: String): List<String> {
        val stmt = connection.prepare("PRAGMA table_info($table)")

        try {
            val names = mutableListOf<String>()

            while (stmt.step()) {
                if (stmt.getLong(5) > 0L) {
                    names.add(stmt.getText(1))
                }
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

    private fun publishedRows(): List<Triple<String, String, Long>> {
        val stmt = connection.prepare(
            "SELECT `key`, markdown, fetchedAtEpochMillis FROM published_documents",
        )

        try {
            val rows = mutableListOf<Triple<String, String, Long>>()

            while (stmt.step()) {
                rows.add(
                    Triple(
                        stmt.getText(0),
                        stmt.getText(1),
                        stmt.getLong(2),
                    ),
                )
            }

            return rows
        } finally {
            stmt.close()
        }
    }

    @Nested
    inner class RealV53Database {
        @Test
        fun `migration runs cleanly from a real v53 database`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            val result = runCatching { migration.migrate(db) }

            // ----- Assert -----
            result.isSuccess.shouldBeTrue()
        }
    }

    @Nested
    inner class RoadmapRowCarryOver {
        @Test
        fun `existing roadmap row is carried over under the roadmap key`() {
            // ----- Arrange -----
            connection.execSQL(
                "INSERT INTO roadmap_documents (id, markdown, fetchedAtEpochMillis) " +
                    "VALUES (1, '# Roadmap', 1700000000000)",
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            publishedRows() shouldBe listOf(
                Triple(
                    "roadmap",
                    "# Roadmap",
                    1700000000000L,
                ),
            )
        }

        @Test
        fun `empty roadmap_documents yields an empty published_documents`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            publishedRows().shouldBeEmpty()
        }
    }

    @Nested
    inner class TableShape {
        @Test
        fun `roadmap_documents is dropped after migration`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            val tables = sqliteMasterNames(type = "table")
            tables shouldNotContain "roadmap_documents"
            tables shouldContain "published_documents"
        }

        @Test
        fun `published_documents has the entity columns after migration`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            columnNames("published_documents") shouldBe listOf(
                "key",
                "markdown",
                "fetchedAtEpochMillis",
            )
        }

        @Test
        fun `published_documents is keyed on key alone`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            primaryKeyColumns("published_documents") shouldBe listOf("key")
        }
    }
}
