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

class Migration70To71Test {
    private lateinit var connection: SQLiteConnection

    private val migration = SoftcoverDatabase.ALL_MIGRATIONS
        .single { it.startVersion == 70 && it.endVersion == 71 }

    private val schema70 = ExportedSchema.load(version = 70)

    private val exported = ExportedSchema.load(version = 71)

    @BeforeEach
    fun setUp() {
        connection = BundledSQLiteDriver().open(":memory:")
        schema70.createIn(connection)
        schema70.seedOneRowPerTable(connection)
    }

    @AfterEach
    fun tearDown() {
        connection.close()
    }

    @Nested
    inner class ExistingData {
        @Test
        fun `every v70 table keeps identical rows`() {
            // ----- Arrange -----
            val rowsBefore = schema70.tables.associate { table ->
                table.name to connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }

            // ----- Act -----
            migration.migrate(connection)

            // ----- Assert -----
            schema70.tables.forEach { table ->
                connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                ) shouldBe rowsBefore.getValue(table.name)
            }
        }
    }

    @Nested
    inner class MoodTables {
        @Test
        fun `both tables arrive empty`() {
            // ----- Arrange -----
            val db = connection

            // ----- Act -----
            migration.migrate(db)

            // ----- Assert -----
            db.countRows("book_moods") shouldBe 0
            db.countRows("mood_moments") shouldBe 0
        }

        @Test
        fun `a book mood written after the migration reads back`() {
            // ----- Arrange -----
            val db = connection

            migration.migrate(db)

            // ----- Act -----
            db.execSQL("INSERT INTO book_moods (bookId, word, isOwn, strength) VALUES (7, 'cozy', 1, NULL)")

            // ----- Assert -----
            db.snapshot(
                "book_moods",
                listOf("bookId", "word", "isOwn", "strength"),
            ) shouldBe listOf(listOf("7", "cozy", "1", null))
        }

        @Test
        fun `a mood moment written after the migration reads back with a generated id`() {
            // ----- Arrange -----
            val db = connection

            migration.migrate(db)

            // ----- Act -----
            db.execSQL(
                "INSERT INTO mood_moments (bookId, word, isOwn, strength, percent, page, seconds, recordedAt) " +
                    "VALUES (7, 'bleak', 0, 3, NULL, 12, NULL, 99)",
            )

            // ----- Assert -----
            db.snapshot(
                "mood_moments",
                listOf("id", "bookId", "word", "isOwn", "strength", "percent", "page", "seconds", "recordedAt"),
            ) shouldBe listOf(listOf("1", "7", "bleak", "0", "3", null, "12", null, "99"))
        }
    }

    @Nested
    inner class ResultingSchema {
        @Test
        fun `resulting tables, columns, indices and views equal the v71 schema`() {
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
