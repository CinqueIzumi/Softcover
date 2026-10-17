package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration55To56Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 55 && it.endVersion == 56 }

    private val schema55 = ExportedSchema.load(version = 55)

    private val exported = ExportedSchema.load(version = 56)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema55.createIn(connection)
        schema55.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun uniqueIndexNames(table: String): Set<String> {
        val stmt = connection.prepare("PRAGMA index_list(`$table`)")

        try {
            val names = mutableSetOf<String>()

            while (stmt.step()) {
                // PRAGMA index_list columns: seq, name, unique, origin, partial
                if (stmt.getLong(2) == 1L) {
                    names.add(stmt.getText(1))
                }
            }

            return names
        } finally {
            stmt.close()
        }
    }

    @Nested
    inner class TableShape {
        @Test
        fun `book_quotes has the exported columns`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.columnNames("book_quotes") shouldBe exported.table("book_quotes").columns.map { it.name }
        }

        @Test
        fun `pending_quote_writes has the exported columns`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.columnNames("pending_quote_writes") shouldBe
                exported.table("pending_quote_writes").columns.map { it.name }
        }

        @Test
        fun `the remoteId index is unique`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            uniqueIndexNames("book_quotes") shouldBe setOf("index_book_quotes_remoteId")
        }
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v55 table except book_highlights keeps identical rows`() {
            // ----- Arrange -----
            val kept = schema55.tables.filter { it.name != "book_highlights" }
            val rowsBefore = kept.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }
            kept.forEach { rowsBefore.getValue(it.name).size shouldBe 1 }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            kept.forEach { table ->
                connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                ) shouldBe rowsBefore.getValue(table.name)
            }
        }

        @Test
        fun `resulting tables, indices and views equal the v56 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.userTables() shouldBe exported.tableNames
            db.indexNames() shouldBe exported.indexNames
            db.viewNames() shouldBe exported.viewNames
        }

        @Test
        fun `a second remote id is rejected by the unique index`() {
            // ----- Arrange -----
            migration.migrate(connection)
            val insert = "INSERT INTO book_quotes (remoteId, bookId, text, privacySettingId, " +
                "actionAtEpochMillis, syncState) VALUES (7, 1, 'a', 1, 1, 'SYNCED')"
            connection.execSQL(insert)

            // ----- Act & Assert -----
            runCatching { connection.execSQL(insert) }.isFailure shouldBe true
        }

        @Test
        fun `several rows without a remote id coexist`() {
            // ----- Arrange -----
            migration.migrate(connection)
            val insert = "INSERT INTO book_quotes (remoteId, bookId, text, privacySettingId, " +
                "actionAtEpochMillis, syncState) VALUES (NULL, 1, 'a', 1, 1, 'PENDING_INSERT')"

            // ----- Act -----
            connection.execSQL(insert)
            connection.execSQL(insert)

            // ----- Assert -----
            connection.countRows("book_quotes") shouldBe 2
        }
    }
}
