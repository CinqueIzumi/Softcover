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

class Migration58To59Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 58 && it.endVersion == 59 }

    private val schema58 = ExportedSchema.load(version = 58)

    private val exported = ExportedSchema.load(version = 59)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema58.createIn(connection)
        schema58.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `a journal row keeps its event and moves its updatedAt value into actionAt`() {
            // ----- Arrange -----
            connection.execSQL(
                "INSERT INTO reading_journals (userBookId, event, updatedAt) " +
                    "VALUES (7, 'finished', '2024-05-06T07:08:09')",
            )

            val before = connection.snapshot(
                "reading_journals",
                listOf("localId", "userBookId", "event", "updatedAt"),
            )

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            connection.snapshot(
                "reading_journals",
                listOf("localId", "userBookId", "event", "actionAt"),
            ) shouldBe before
        }

        @Test
        fun `every other v58 table keeps identical rows`() {
            // ----- Arrange -----
            val kept = schema58.tables.filter { it.name != "reading_journals" }

            val rowsBefore = kept.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

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
        fun `resulting tables, indices and views equal the v59 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.userTables() shouldBe exported.tableNames
            db.indexNames() shouldBe exported.indexNames
            db.viewNames() shouldBe exported.viewNames
        }
    }
}
