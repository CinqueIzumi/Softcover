package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration66To67Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 66 && it.endVersion == 67 }

    private val schema66 = ExportedSchema.load(version = 66)

    private val exported = ExportedSchema.load(version = 67)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema66.createIn(connection)
        schema66.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v66 table keeps identical rows`() {
            // ----- Arrange -----
            val rowsBefore = schema66.tables.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            schema66.tables.forEach { table ->
                connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                ) shouldBe rowsBefore.getValue(table.name)
            }
        }
    }

    @Nested
    inner class BookCopiesTable {
        @Test
        fun `resulting tables, columns, indices and views equal the v67 schema`() {
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
