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

class Migration69To70Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 69 && it.endVersion == 70 }

    private val schema69 = ExportedSchema.load(version = 69)

    private val exported = ExportedSchema.load(version = 70)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema69.createIn(connection)
        schema69.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v69 table keeps identical rows`() {
            // ----- Arrange -----
            val rowsBefore = schema69.tables.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            schema69.tables.forEach { table ->
                connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                ) shouldBe rowsBefore.getValue(table.name)
            }
        }
    }

    @Nested
    inner class DidNotFinishReasonsTable {
        @Test
        fun `the table arrives empty`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.countRows("did_not_finish_reasons") shouldBe 0
        }

        @Test
        fun `a row written after the migration reads its flags back`() {
            // ----- Arrange -----
            val db = connection

            migration.migrate(db)

            // ----- Act -----
            db.execSQL(
                "INSERT INTO did_not_finish_reasons " +
                    "(bookId, notTheRightTime, notMyKindOfWriting, tooSlow, lostInterest, unwantedContent, " +
                    "ownWords, updatedAt) VALUES (7, 0, 1, 0, 1, 0, NULL, 99)",
            )

            // ----- Assert -----
            db.snapshot(
                "did_not_finish_reasons",
                listOf(
                    "bookId",
                    "notTheRightTime",
                    "notMyKindOfWriting",
                    "tooSlow",
                    "lostInterest",
                    "unwantedContent",
                    "ownWords",
                    "updatedAt",
                ),
            ) shouldBe listOf(listOf("7", "0", "1", "0", "1", "0", null, "99"))
        }
    }

    @Nested
    inner class ResultingSchema {
        @Test
        fun `resulting tables, columns, indices and views equal the v70 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.userTables() shouldBe exported.tableNames
            db.indexNames() shouldBe exported.indexNames
            db.viewNames() shouldBe exported.viewNames

            exported.tables.forEach {
                db.columnNames(it.name).toSet() shouldBe it.columns.map { column -> column.name }.toSet()
            }
        }
    }
}
