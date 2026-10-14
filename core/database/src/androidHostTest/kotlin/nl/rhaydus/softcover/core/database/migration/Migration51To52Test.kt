package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class Migration51To52Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 51 && it.endVersion == 52 }

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        connection.execSQL("PRAGMA foreign_keys = ON")
        buildV51Schema(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun buildV51Schema(conn: SQLiteConnection) {
        SqliteInspection.execAll(
            connection = conn,
            statements = ExportedSchema.createStatements(version = 51),
        )
        ExportedSchema.tableNames(version = 51).forEach {
            val insert = ExportedSchema.seedInsert(
                version = 51,
                table = it,
            )

            conn.execSQL(insert)
        }
    }

    private fun createSql(table: String): String {
        val stmt = connection.prepare("SELECT sql FROM sqlite_master WHERE name = '$table'")

        try {
            stmt.step().shouldBeTrue()

            return stmt.getText(0)
        } finally {
            stmt.close()
        }
    }

    private fun normalized(sql: String): String = sql
        .replace(
            "`",
            "",
        )
        .replace(
            "\"",
            "",
        )
        .filterNot { it.isWhitespace() }
        .replace(
            "IFNOTEXISTS",
            "",
        )

    @Nested
    inner class TrendingBooksTable {
        @Test
        fun `trending_books definition matches the exported v52 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            normalized(createSql("trending_books")) shouldBe
                normalized(ExportedSchema.createSql(
                    version = 52,
                    table = "trending_books",
                ),)
        }
    }

    @Nested
    inner class TrendingMetaTable {
        @Test
        fun `trending_meta definition matches the exported v52 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            normalized(createSql("trending_meta")) shouldBe
                normalized(ExportedSchema.createSql(
                    version = 52,
                    table = "trending_meta",
                ),)
        }
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v51 table keeps identical rows and columns`() {
            // ----- Arrange -----
            val tables = ExportedSchema.tableNames(version = 51)
            val rowsBefore = tables.associateWith { table ->
                SqliteInspection.rows(
                    connection = connection,
                    table = table,
                )
            }
            val columnsBefore = tables.associateWith { table ->
                SqliteInspection.columnNames(
                    connection = connection,
                    table = table,
                )
            }
            tables.forEach { rowsBefore.getValue(it).size shouldBe 1 }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            tables.forEach { table ->
                SqliteInspection.rows(
                    connection,
                    table,
                ) shouldBe rowsBefore.getValue(table)
                SqliteInspection.columnNames(
                    connection,
                    table,
                ) shouldBe columnsBefore.getValue(table)
            }
        }

        @Test
        fun `resulting tables, indices and views equal the v52 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            SqliteInspection.names(
                db,
                "table",
            ) shouldContainExactlyInAnyOrder ExportedSchema.tableNames(version = 52)
            SqliteInspection.names(
                db,
                "index",
            ) shouldContainExactlyInAnyOrder ExportedSchema.indexNames(version = 52)
            SqliteInspection.names(
                db,
                "view",
            ) shouldContainExactlyInAnyOrder ExportedSchema.viewNames(version = 52)
        }
    }
}
