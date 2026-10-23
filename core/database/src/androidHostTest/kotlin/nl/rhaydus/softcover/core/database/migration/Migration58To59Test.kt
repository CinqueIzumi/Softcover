package nl.rhaydus.softcover.core.database.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotContainAnyOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
        connection.execSQL("DELETE FROM trending_books")
        connection.execSQL("DELETE FROM trending_meta")
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    private fun seedRankedLists() {
        connection.execSQL(
            "INSERT INTO trending_books (period, bookId, rank) VALUES " +
                "('week', 1, 0), ('upcoming_week', 1, 3), ('recent_month', 1, 7), ('recommendations', 1, 12)",
        )
        connection.execSQL(
            "INSERT INTO trending_meta (period, fetchedAtEpochMillis) VALUES " +
                "('week', 1111), ('recommendations', 2222)",
        )
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `ranked rows of every list keep their key, book and rank`() {
            // ----- Arrange -----
            seedRankedLists()

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            connection.snapshot(
                "ranked_books",
                listOf("listKey", "bookId", "rank"),
            ) shouldContainExactlyInAnyOrder listOf(
                listOf("week", "1", "0"),
                listOf("upcoming_week", "1", "3"),
                listOf("recent_month", "1", "7"),
                listOf("recommendations", "1", "12"),
            )
        }

        @Test
        fun `list meta keeps its fetch times`() {
            // ----- Arrange -----
            seedRankedLists()

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            connection.snapshot(
                "ranked_list_meta",
                listOf("listKey", "fetchedAtEpochMillis"),
            ) shouldContainExactlyInAnyOrder listOf(
                listOf("week", "1111"),
                listOf("recommendations", "2222"),
            )
        }

        @Test
        fun `every other v58 table keeps identical rows`() {
            // ----- Arrange -----
            seedRankedLists()
            val kept = schema58.tables.filter { it.name != "trending_books" && it.name != "trending_meta" }
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
    }

    @Nested
    inner class TableShape {
        @Test
        fun `the old trending tables no longer exist`() {
            // ----- Arrange -----

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            connection.userTables() shouldNotContainAnyOf listOf("trending_books", "trending_meta")
        }

        @Test
        fun `resulting tables, columns, indices and views equal the v59 schema`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.indexNames() shouldBe exported.indexNames
            db.viewNames() shouldBe exported.viewNames
            listOf("ranked_books", "ranked_list_meta").forEach {
                db.columnNames(it) shouldBe exported.table(it).columns.map { column -> column.name }
            }
        }
    }

    @Nested
    inner class ForeignKey {
        @Test
        fun `deleting a book cascades to its ranked rows`() {
            // ----- Arrange -----
            seedRankedLists()
            migration.migrate(connection)
            connection.execSQL("PRAGMA foreign_keys = ON")
            connection.countRows("ranked_books") shouldNotBe 0

            // ----- Act -----
            connection.execSQL("DELETE FROM books WHERE id = 1")

            // ----- Assert -----
            connection.countRows("ranked_books") shouldBe 0
        }
    }
}
