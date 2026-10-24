package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration60To61Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 60 && it.endVersion == 61 }

    private val schema60 = ExportedSchema.load(version = 60)

    private val exported = ExportedSchema.load(version = 61)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema60.createIn(connection)
        schema60.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v60 table keeps identical rows`() {
            // ----- Arrange -----
            val rowsBefore = schema60.tables.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            schema60.tables.forEach { table ->
                val after = connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
                after shouldBe rowsBefore.getValue(table.name)
            }
        }
    }

    @Nested
    inner class BookTraitsTable {
        @Test
        fun `resulting tables, columns, indices and views equal the v61 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.userTables() shouldBe exported.tableNames
            db.indexNames() shouldBe exported.indexNames
            db.viewNames() shouldBe exported.viewNames
            exported.tables.forEach { table ->
                val actual = db.columnNames(table.name)
                actual shouldBe table.columns.map { column -> column.name }
            }
        }
    }
}
