package nl.rhaydus.softcover.core.database.migration

import android.content.Context
import androidx.room.Room
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * Opens an on-disk database laid out as an older release left it, through the production builder.
 * The builder falls back to destructive migration when no path exists, so any gap in the chain, or
 * any schema Room refuses to validate, would silently empty the tables checked here.
 */
class RoomOpenSurvivesUpgradeTest {
    @TempDir
    lateinit var tempDir: File

    private val latest = ExportedSchema.loadLatest()

    private fun databaseFile(): File = File(
        tempDir,
        "books.db",
    )

    private fun withConnection(
        file: File,
        block: (SQLiteConnection) -> Unit,
    ) {
        val connection = BundledSQLiteDriver().open(file.absolutePath)

        try {
            block(connection)
        } finally {
            connection.close()
        }
    }

    /** Lays the file out as [schema] would, with one row per table and a second read for the user book. */
    private fun writeOldDatabase(
        file: File,
        schema: ExportedSchema,
    ) {
        withConnection(file) { connection ->
            schema.createIn(connection)
            schema.seedOneRowPerTable(connection)
            connection.execSQL(
                "INSERT INTO user_book_reads (id, userBookId, currentPage, startedAt, finishedAt) " +
                    "VALUES (2, 1, 80, '2024-02-01T00:00:00', '2024-03-01T00:00:00')",
            )
        }
    }

    private fun snapshotsOf(
        file: File,
        schema: ExportedSchema,
    ): Map<String, List<List<String?>>> {
        val snapshots = mutableMapOf<String, List<List<String?>>>()

        withConnection(file) { connection ->
            schema.tables.forEach { table ->
                snapshots[table.name] = connection.snapshot(
                    table.name,
                    table.columns.map { it.name },
                )
            }
        }

        return snapshots
    }

    private fun openThroughProductionBuilder(file: File) {
        val context: Context = mockk(relaxed = true)

        every {
            context.getDatabasePath(any())
        } returns file

        val database = SoftcoverDatabase.build(
            builder = Room.databaseBuilder<SoftcoverDatabase>(
                context = context,
                name = file.absolutePath,
            ),
            queryContext = UnconfinedTestDispatcher(),
        )

        try {
            runBlocking { database.progressHistoryDao().observeByBookId(bookId = 1).first() }
        } finally {
            database.close()
        }
    }

    private fun assertSurvived(
        file: File,
        before: Map<String, List<List<String?>>>,
        oldSchema: ExportedSchema,
    ) {
        withConnection(file) { connection ->
            (oldSchema.tableNames intersect latest.tableNames).forEach { name ->
                val commonColumns = oldSchema.table(name).columns.map { it.name }
                    .filter { column -> latest.table(name).columns.any { it.name == column } }
                val oldColumns = oldSchema.table(name).columns.map { it.name }
                val keptIndexes = commonColumns.map { oldColumns.indexOf(it) }
                val expected = before.getValue(name).map { row -> keptIndexes.map { row[it] } }

                connection.snapshot(
                    name,
                    commonColumns,
                ) shouldBe expected
            }
        }
    }

    private fun assertMatchesLatestSchema(file: File) {
        withConnection(file) { connection ->
            connection.userVersion() shouldBe latest.version.toLong()
            connection.identityHash() shouldBe latest.identityHash
            connection.userTables() shouldBe latest.tableNames
            connection.userTables() shouldNotContain "reading_log_entries"
            connection.indexNames() shouldBe latest.tables.flatMap { it.indexNames }.toSet()
            connection.viewNames() shouldBe setOf("book_edition_view")
        }
    }

    @Nested
    inner class FromVersion52 {
        private val schema52 = ExportedSchema.load(version = 52)

        @Test
        fun `every user table keeps its rows after the production builder upgrades to the latest version`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema52,
            )
            val before = snapshotsOf(
                file,
                schema52,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            assertSurvived(
                file,
                before,
                schema52,
            )
        }

        @Test
        fun `the seeded tables cover the user data that must not be lost`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema52,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            withConnection(file) { connection ->
                listOf(
                    "books",
                    "user_books",
                    "reading_journals",
                    "pending_user_book_writes",
                    "reading_sessions",
                    "progress_history",
                    "book_deadlines",
                ).forEach { connection.countRows(it) shouldBe 1 }
            }
        }

        @Test
        fun `a progress history row from before the pages read column ends the chain with zero pages read`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema52,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            withConnection(file) { connection ->
                connection.snapshot(
                    "progress_history",
                    listOf("pagesRead"),
                ) shouldBe listOf(listOf("0"))
            }
        }

        @Test
        fun `both reads of a user book survive`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema52,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            withConnection(file) { connection ->
                connection.snapshot(
                    "user_book_reads",
                    listOf("id", "userBookId", "currentPage"),
                ) shouldContainExactly
                    listOf(listOf("1", "1", "1"), listOf("2", "1", "80"))
            }
        }

        @Test
        fun `the schema Room leaves behind is the exported latest schema without reading_log_entries`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema52,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            assertMatchesLatestSchema(file)
        }

        @Test
        fun `a database already on the latest version reopens with its rows intact`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema52,
            )
            openThroughProductionBuilder(file)
            val afterFirstOpen = snapshotsOf(
                file,
                latest,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            snapshotsOf(
                file,
                latest,
            ) shouldBe afterFirstOpen
        }
    }

    @Nested
    inner class FromVersion42 {
        private val schema42 = ExportedSchema.load(version = 42)

        @Test
        fun `every user table keeps its rows through the whole chain to the latest version`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema42,
            )
            val before = snapshotsOf(
                file,
                schema42,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            assertSurvived(
                file,
                before,
                schema42,
            )
        }

        @Test
        fun `both reads of a user book survive the chain`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema42,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            withConnection(file) { connection ->
                connection.countRows("user_book_reads") shouldBe 2
            }
        }

        @Test
        fun `the schema Room leaves behind is the exported latest schema`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema42,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            assertMatchesLatestSchema(file)
        }
    }

    @Nested
    inner class FromVersion55 {
        private val schema55 = ExportedSchema.load(version = 55)

        @Test
        fun `every surviving user table keeps its rows when book_highlights is dropped`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema55,
            )
            val before = snapshotsOf(
                file,
                schema55,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            assertSurvived(
                file,
                before,
                schema55,
            )
        }

        @Test
        fun `a trending row and its fetch time arrive as a ranked row and list meta`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema55,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            withConnection(file) { connection ->
                connection.snapshot(
                    "ranked_books",
                    listOf("listKey", "bookId", "rank"),
                ) shouldContainExactly listOf(listOf("x", "1", "1"))
                connection.snapshot(
                    "ranked_list_meta",
                    listOf("listKey", "fetchedAtEpochMillis"),
                ) shouldContainExactly listOf(listOf("x", "1"))
            }
        }

        @Test
        fun `the schema Room leaves behind is the exported latest schema`() {
            // ----- Arrange -----
            val file = databaseFile()
            writeOldDatabase(
                file,
                schema55,
            )

            // ----- Act -----
            openThroughProductionBuilder(file)

            // ----- Assert -----
            assertMatchesLatestSchema(file)
        }
    }
}
