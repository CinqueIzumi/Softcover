package nl.rhaydus.softcover.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.coroutines.CoroutineContext
import nl.rhaydus.softcover.core.database.dao.BookDao
import nl.rhaydus.softcover.core.database.dao.BookDeadlineDao
import nl.rhaydus.softcover.core.database.dao.DismissedContinueSeriesDao
import nl.rhaydus.softcover.core.database.dao.HighlightDao
import nl.rhaydus.softcover.core.database.dao.PendingListWriteDao
import nl.rhaydus.softcover.core.database.dao.PendingUserBookWriteDao
import nl.rhaydus.softcover.core.database.dao.ReadingLogDao
import nl.rhaydus.softcover.core.database.dao.ReadingSessionDao
import nl.rhaydus.softcover.core.database.model.AuthorEntity
import nl.rhaydus.softcover.core.database.model.BookAuthorCrossRef
import nl.rhaydus.softcover.core.database.model.BookDeadlineEntity
import nl.rhaydus.softcover.core.database.model.BookEditionEntity
import nl.rhaydus.softcover.core.database.model.BookEditionView
import nl.rhaydus.softcover.core.database.model.BookEntity
import nl.rhaydus.softcover.core.database.model.BookListEntity
import nl.rhaydus.softcover.core.database.model.BookSeriesEntity
import nl.rhaydus.softcover.core.database.model.BookTagCrossRef
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesEntity
import nl.rhaydus.softcover.core.database.model.EditionAuthorCrossRef
import nl.rhaydus.softcover.core.database.model.HighlightEntity
import nl.rhaydus.softcover.core.database.model.ListBookEntity
import nl.rhaydus.softcover.core.database.model.PendingListWriteEntity
import nl.rhaydus.softcover.core.database.model.PendingUserBookWriteEntity
import nl.rhaydus.softcover.core.database.model.ReadingJournalEntity
import nl.rhaydus.softcover.core.database.model.ReadingLogEntryEntity
import nl.rhaydus.softcover.core.database.model.ReadingSessionEntity
import nl.rhaydus.softcover.core.database.model.ShelfManualOrderEntity
import nl.rhaydus.softcover.core.database.model.TagEntity
import nl.rhaydus.softcover.core.database.model.UserBookEntity
import nl.rhaydus.softcover.core.database.model.UserBookReadEntity

@Database(
    entities = [
        BookEntity::class,
        UserBookEntity::class,
        UserBookReadEntity::class,
        BookEditionEntity::class,
        AuthorEntity::class,
        BookAuthorCrossRef::class,
        EditionAuthorCrossRef::class,
        ReadingJournalEntity::class,
        BookListEntity::class,
        ListBookEntity::class,
        BookSeriesEntity::class,
        BookDeadlineEntity::class,
        PendingUserBookWriteEntity::class,
        PendingListWriteEntity::class,
        DismissedContinueSeriesBookEntity::class,
        DismissedContinueSeriesEntity::class,
        HighlightEntity::class,
        ReadingSessionEntity::class,
        ReadingLogEntryEntity::class,
        TagEntity::class,
        BookTagCrossRef::class,
        ShelfManualOrderEntity::class,
    ],
    views = [
        BookEditionView::class
    ],
    version = 43,
)
@ConstructedBy(SoftcoverDatabaseConstructor::class)
abstract class SoftcoverDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    abstract fun bookDeadlineDao(): BookDeadlineDao

    abstract fun pendingUserBookWriteDao(): PendingUserBookWriteDao

    abstract fun pendingListWriteDao(): PendingListWriteDao

    abstract fun dismissedContinueSeriesDao(): DismissedContinueSeriesDao

    abstract fun highlightDao(): HighlightDao

    abstract fun readingSessionDao(): ReadingSessionDao

    abstract fun readingLogDao(): ReadingLogDao

    companion object {
        internal fun build(
            builder: RoomDatabase.Builder<SoftcoverDatabase>,
            queryContext: CoroutineContext,
        ): SoftcoverDatabase =
            builder
                .addMigrations(migrations = ALL_MIGRATIONS.toTypedArray())
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(queryContext)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
            ALTER TABLE book_editions
            ADD COLUMN format TEXT NOT NULL DEFAULT ''
        """.trimIndent(),
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                // Create the new reading_journals table
                connection.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS reading_journals (
                localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userBookId INTEGER NOT NULL,
                event TEXT NOT NULL,
                updatedAt TEXT NOT NULL
            )
        """.trimIndent(),
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS user_books (
                            id INTEGER NOT NULL PRIMARY KEY,
                            bookId INTEGER NOT NULL,
                            statusCode INTEGER NOT NULL,
                            dateAdded TEXT NOT NULL,
                            privacySettingId INTEGER NOT NULL,
                            reviewHasSpoilers INTEGER NOT NULL,
                            editionId INTEGER,
                            lastReadDate TEXT,
                            rating REAL,
                            referrerUserId INTEGER,
                            reviewedAt TEXT,
                            updatedAt TEXT,
                            FOREIGN KEY(bookId) REFERENCES books(id) ON DELETE CASCADE
                        )
                    """,
                )

                connection.execSQL("CREATE INDEX index_user_books_bookId ON user_books(bookId)")
                connection.execSQL("CREATE INDEX index_user_books_statusCode ON user_books(statusCode)")

                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS user_book_reads (
                            id INTEGER NOT NULL PRIMARY KEY,
                            userBookId INTEGER NOT NULL,
                            currentPage INTEGER,
                            progress REAL,
                            startedAt TEXT,
                            finishedAt TEXT,
                            FOREIGN KEY(userBookId) REFERENCES user_books(id) ON DELETE CASCADE
                        )
                    """,
                )

                connection.execSQL("CREATE INDEX index_user_book_reads_userBookId ON user_book_reads(userBookId)")

                connection.execSQL(
                    """
                        INSERT INTO user_books (
                            id,
                            bookId,
                            statusCode,
                            dateAdded,
                            privacySettingId,
                            reviewHasSpoilers,
                            editionId,
                            lastReadDate,
                            rating,
                            referrerUserId,
                            reviewedAt,
                            updatedAt
                        )
                        SELECT
                            userBook_id,
                            id,
                            userBook_statusCode,
                            userBook_dateAdded,
                            userBook_privacySettingId,
                            userBook_reviewHasSpoilers,
                            userBook_editionId,
                            userBook_lastReadDate,
                            userBook_rating,
                            userBook_referrerUserId,
                            userBook_reviewedAt,
                            userBook_updatedAt
                        FROM books
                        WHERE userBook_id IS NOT NULL
                    """,
                )

                connection.execSQL(
                    """
                        INSERT INTO user_book_reads (
                            id,
                            userBookId,
                            currentPage,
                            progress,
                            startedAt,
                            finishedAt
                        )
                        SELECT
                            userBookRead_id,
                            userBook_id,
                            userBookRead_currentPage,
                            userBookRead_progress,
                            userBookRead_startedAt,
                            userBookRead_finishedAt
                        FROM books
                        WHERE userBookRead_id IS NOT NULL
                    """,
                )

                connection.execSQL(
                    """
                        CREATE TABLE books_new (
                            id INTEGER NOT NULL PRIMARY KEY,
                            title TEXT NOT NULL,
                            defaultEditionId INTEGER,
                            rating REAL NOT NULL,
                            description TEXT NOT NULL,
                            releaseYear INTEGER NOT NULL,
                            coverUrl TEXT NOT NULL,
                            usersCount INTEGER NOT NULL
                        )
                    """,
                )

                connection.execSQL(
                    """
                        INSERT INTO books_new (
                            id,
                            title,
                            defaultEditionId,
                            rating,
                            description,
                            releaseYear,
                            coverUrl,
                            usersCount
                        )
                        SELECT
                            id,
                            title,
                            defaultEditionId,
                            rating,
                            description,
                            releaseYear,
                            coverUrl,
                            usersCount
                        FROM books
                    """,
                )

                connection.execSQL("DROP TABLE books")
                connection.execSQL("ALTER TABLE books_new RENAME TO books")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS book_lists (
                            id INTEGER PRIMARY KEY NOT NULL,
                            name TEXT NOT NULL,
                            slug TEXT NOT NULL DEFAULT ''
                        )
                    """.trimIndent(),
                )

                try {
                    connection.execSQL("ALTER TABLE book_lists ADD COLUMN slug TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {
                }

                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS book_list_edition_cross_ref (
                            bookListId INTEGER NOT NULL,
                            editionId INTEGER NOT NULL,
                            PRIMARY KEY(bookListId, editionId),
                            FOREIGN KEY(bookListId) REFERENCES book_lists(id) ON DELETE CASCADE,
                            FOREIGN KEY(editionId) REFERENCES book_editions(id) ON DELETE CASCADE
                        )
                    """.trimIndent(),
                )

                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_book_list_edition_cross_ref_bookListId " +
                            "ON book_list_edition_cross_ref(bookListId)",
                )

                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_book_list_edition_cross_ref_editionId " +
                            "ON book_list_edition_cross_ref(editionId)",
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
            CREATE VIEW `book_edition_view` AS SELECT
                    edition.*,
                    EXISTS(
                        SELECT 1
                        FROM book_list_edition_cross_ref bler
                        JOIN book_lists bl ON bl.id = bler.bookListId
                        WHERE bler.editionId = edition.id
                        AND bl.slug = 'owned'
                    ) AS isOwned
                FROM book_editions edition
            """.trimIndent(),
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(connection: SQLiteConnection) {
                try {
                    connection.execSQL("ALTER TABLE book_lists ADD COLUMN slug TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {
                }

                connection.execSQL(
                    """
                CREATE TABLE IF NOT EXISTS list_books (
                    listBookId INTEGER NOT NULL,
                    listId INTEGER NOT NULL,
                    bookId INTEGER NOT NULL,
                    editionId INTEGER NOT NULL,
                    position INTEGER,
                    PRIMARY KEY(listId, bookId, editionId, listBookId),
                    FOREIGN KEY(listId) REFERENCES book_lists(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
                    FOREIGN KEY(bookId) REFERENCES books(id) ON UPDATE NO ACTION ON DELETE NO ACTION,
                    FOREIGN KEY(editionId) REFERENCES book_editions(id) ON UPDATE NO ACTION ON DELETE NO ACTION
                )
            """.trimIndent(),
                )

                connection.execSQL(
                    """
                INSERT INTO list_books (listId, bookId, editionId)
                SELECT bler.bookListId, be.bookId, bler.editionId
                FROM book_list_edition_cross_ref bler
                JOIN book_editions be ON bler.editionId = be.id
            """.trimIndent(),
                )

                connection.execSQL("DROP VIEW IF EXISTS book_edition_view")
                connection.execSQL(
                    """
                CREATE VIEW `book_edition_view` AS SELECT
                        edition.*,
                        EXISTS(
                            SELECT 1
                            FROM list_books lb
                            JOIN book_lists bl ON bl.id = lb.listId
                            WHERE lb.editionId = edition.id
                            AND bl.slug = 'owned'
                        ) AS isOwned
                    FROM book_editions edition
            """.trimIndent(),
                )

                connection.execSQL("DROP TABLE IF EXISTS book_list_edition_cross_ref")

                connection.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_listId ON list_books(listId)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_bookId ON list_books(bookId)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_editionId ON list_books(editionId)")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE list_books_new (
                            listBookId INTEGER NOT NULL,
                            listId INTEGER NOT NULL,
                            bookId INTEGER NOT NULL,
                            editionId INTEGER NOT NULL,
                            position INTEGER,
                            PRIMARY KEY(listId, bookId, editionId, listBookId)
                        )
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                        INSERT INTO list_books_new (listBookId, listId, bookId, editionId, position)
                        SELECT listBookId, listId, bookId, editionId, position FROM list_books
                    """.trimIndent(),
                )

                connection.execSQL("DROP TABLE list_books")
                connection.execSQL("ALTER TABLE list_books_new RENAME TO list_books")

                connection.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_listId ON list_books(listId)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_bookId ON list_books(bookId)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_editionId ON list_books(editionId)")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        DELETE FROM book_editions
                        WHERE id NOT IN (
                            SELECT editionId FROM user_books WHERE editionId IS NOT NULL
                        )
                        AND id NOT IN (
                            SELECT editionId FROM list_books
                        )
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                        DELETE FROM edition_author_cross_ref
                        WHERE editionId NOT IN (SELECT id FROM book_editions)
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                        DELETE FROM authors
                        WHERE id NOT IN (SELECT authorId FROM book_author_cross_ref)
                        AND id NOT IN (SELECT authorId FROM edition_author_cross_ref)
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE book_editions ADD COLUMN localImagePath TEXT DEFAULT NULL")

                connection.execSQL("DROP VIEW IF EXISTS book_edition_view")
                connection.execSQL(
                    "CREATE VIEW `book_edition_view` AS SELECT${' '}\n" +
                            "        edition.*,\n" +
                            "        EXISTS(\n" +
                            "            SELECT 1\n" +
                            "            FROM list_books lb\n" +
                            "            JOIN book_lists bl ON bl.id = lb.listId\n" +
                            "            WHERE lb.editionId = edition.id\n" +
                            "            AND bl.slug = 'owned'\n" +
                            "        ) AS isOwned\n" +
                            "    FROM book_editions edition",
                )
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE book_editions ADD COLUMN canonicalId INTEGER DEFAULT NULL")

                connection.execSQL("DROP VIEW IF EXISTS book_edition_view")
                connection.execSQL(
                    """
                        CREATE VIEW `book_edition_view` AS SELECT
                                edition.*,
                                EXISTS(
                                    SELECT 1
                                    FROM list_books lb
                                    JOIN book_lists bl ON bl.id = lb.listId
                                    WHERE bl.slug = 'owned'
                                    AND (
                                        lb.editionId = edition.id
                                        OR lb.editionId = edition.canonicalId
                                        OR lb.editionId IN (
                                            SELECT sub.id FROM book_editions sub
                                            WHERE sub.canonicalId = edition.id
                                        )
                                    )
                                ) AS isOwned
                            FROM book_editions edition
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS book_deadlines (
                            bookId INTEGER NOT NULL PRIMARY KEY,
                            deadlineDate TEXT NOT NULL,
                            setAt TEXT NOT NULL,
                            initialPagesPerDay REAL NOT NULL
                        )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE book_editions ADD COLUMN audioSeconds INTEGER DEFAULT NULL")
                connection.execSQL("ALTER TABLE user_book_reads ADD COLUMN currentSeconds INTEGER DEFAULT NULL")

                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS book_deadlines_new (
                            bookId INTEGER NOT NULL PRIMARY KEY,
                            deadlineDate TEXT NOT NULL,
                            setAt TEXT NOT NULL,
                            initialPerDay REAL NOT NULL,
                            unit TEXT NOT NULL DEFAULT 'PAGES'
                        )
                    """.trimIndent(),
                )
                connection.execSQL(
                    """
                        INSERT INTO book_deadlines_new (bookId, deadlineDate, setAt, initialPerDay, unit)
                        SELECT bookId, deadlineDate, setAt, initialPagesPerDay, 'PAGES'
                        FROM book_deadlines
                    """.trimIndent(),
                )
                connection.execSQL("DROP TABLE book_deadlines")
                connection.execSQL("ALTER TABLE book_deadlines_new RENAME TO book_deadlines")

                connection.execSQL("DROP VIEW IF EXISTS book_edition_view")
                connection.execSQL(
                    """
                        CREATE VIEW `book_edition_view` AS SELECT
                                edition.*,
                                EXISTS(
                                    SELECT 1
                                    FROM list_books lb
                                    JOIN book_lists bl ON bl.id = lb.listId
                                    WHERE bl.slug = 'owned'
                                    AND (
                                        lb.editionId = edition.id
                                        OR lb.editionId = edition.canonicalId
                                        OR lb.editionId IN (
                                            SELECT sub.id FROM book_editions sub
                                            WHERE sub.canonicalId = edition.id
                                        )
                                    )
                                ) AS isOwned
                            FROM book_editions edition
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS book_series (
                id INTEGER NOT NULL,
                name TEXT NOT NULL,
                amountOfBooks INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent(),
                )

                connection.execSQL("ALTER TABLE books ADD COLUMN seriesId INTEGER DEFAULT NULL")
                connection.execSQL("ALTER TABLE books ADD COLUMN positionInSeries INTEGER DEFAULT NULL")
            }
        }

        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE list_books ADD COLUMN addedAt TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE user_books ADD COLUMN createdAt TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE books ADD COLUMN ratingsCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE books_new (
                            id INTEGER NOT NULL PRIMARY KEY,
                            title TEXT NOT NULL,
                            defaultEditionId INTEGER,
                            rating REAL NOT NULL,
                            description TEXT NOT NULL,
                            releaseYear INTEGER NOT NULL,
                            coverUrl TEXT NOT NULL,
                            usersCount INTEGER NOT NULL,
                            ratingsCount INTEGER NOT NULL DEFAULT 0,
                            positionInSeries REAL,
                            seriesId INTEGER
                        )
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                        INSERT INTO books_new (
                            id,
                            title,
                            defaultEditionId,
                            rating,
                            description,
                            releaseYear,
                            coverUrl,
                            usersCount,
                            ratingsCount,
                            positionInSeries,
                            seriesId
                        )
                        SELECT
                            id,
                            title,
                            defaultEditionId,
                            rating,
                            description,
                            releaseYear,
                            coverUrl,
                            usersCount,
                            ratingsCount,
                            CAST(positionInSeries AS REAL),
                            seriesId
                        FROM books
                    """.trimIndent(),
                )

                connection.execSQL("DROP TABLE books")
                connection.execSQL("ALTER TABLE books_new RENAME TO books")
            }
        }

        private val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE books_new (
                            id INTEGER NOT NULL PRIMARY KEY,
                            title TEXT NOT NULL,
                            defaultEditionId INTEGER,
                            rating REAL NOT NULL,
                            description TEXT NOT NULL,
                            releaseYear INTEGER NOT NULL,
                            coverUrl TEXT NOT NULL,
                            usersCount INTEGER NOT NULL,
                            ratingsCount INTEGER NOT NULL DEFAULT 0,
                            positionsInSeries TEXT NOT NULL DEFAULT '',
                            isCompilation INTEGER NOT NULL DEFAULT 0,
                            seriesId INTEGER
                        )
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                        INSERT INTO books_new (
                            id,
                            title,
                            defaultEditionId,
                            rating,
                            description,
                            releaseYear,
                            coverUrl,
                            usersCount,
                            ratingsCount,
                            positionsInSeries,
                            isCompilation,
                            seriesId
                        )
                        SELECT
                            id,
                            title,
                            defaultEditionId,
                            rating,
                            description,
                            releaseYear,
                            coverUrl,
                            usersCount,
                            ratingsCount,
                            COALESCE(CAST(positionInSeries AS TEXT), ''),
                            0,
                            seriesId
                        FROM books
                    """.trimIndent(),
                )

                connection.execSQL("DROP TABLE books")
                connection.execSQL("ALTER TABLE books_new RENAME TO books")
            }
        }

        private val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS dismissed_continue_series_books (
                            bookId INTEGER NOT NULL PRIMARY KEY
                        )
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS dismissed_continue_series (
                            seriesId INTEGER NOT NULL PRIMARY KEY
                        )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE books ADD COLUMN releaseDate TEXT DEFAULT NULL")
                connection.execSQL("ALTER TABLE book_editions ADD COLUMN releaseDate TEXT DEFAULT NULL")

                connection.execSQL("DROP VIEW IF EXISTS book_edition_view")
                connection.execSQL(
                    """
                        CREATE VIEW `book_edition_view` AS SELECT
                                edition.*,
                                EXISTS(
                                    SELECT 1
                                    FROM list_books lb
                                    JOIN book_lists bl ON bl.id = lb.listId
                                    WHERE bl.slug = 'owned'
                                    AND (
                                        lb.editionId = edition.id
                                        OR lb.editionId = edition.canonicalId
                                        OR lb.editionId IN (
                                            SELECT sub.id FROM book_editions sub
                                            WHERE sub.canonicalId = edition.id
                                        )
                                    )
                                ) AS isOwned
                            FROM book_editions edition
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_24_25 = object : Migration(24, 25) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS personal_reviews (
                            bookId INTEGER NOT NULL PRIMARY KEY,
                            body TEXT NOT NULL,
                            hasSpoilers INTEGER NOT NULL,
                            isDraft INTEGER NOT NULL,
                            updatedAt TEXT NOT NULL
                        )
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS book_highlights (
                            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            bookId INTEGER NOT NULL,
                            quote TEXT NOT NULL,
                            page INTEGER,
                            note TEXT,
                            createdAt TEXT NOT NULL
                        )
                    """.trimIndent(),
                )
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_book_highlights_bookId ON book_highlights(bookId)")

                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS reading_sessions (
                            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            bookId INTEGER NOT NULL,
                            startedAt TEXT NOT NULL,
                            endedAt TEXT,
                            startPage INTEGER,
                            endPage INTEGER,
                            startSeconds INTEGER,
                            endSeconds INTEGER
                        )
                    """.trimIndent(),
                )
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_reading_sessions_bookId ON reading_sessions(bookId)")

                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS reading_log_entries (
                            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            bookId INTEGER NOT NULL,
                            startedAt TEXT,
                            finishedAt TEXT,
                            rating REAL,
                            note TEXT,
                            createdAt TEXT NOT NULL
                        )
                    """.trimIndent(),
                )
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_reading_log_entries_bookId ON reading_log_entries(bookId)")
            }
        }

        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS pending_progress_updates (
                            localId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            kind TEXT NOT NULL,
                            userBookId INTEGER NOT NULL,
                            userBookReadId INTEGER NOT NULL,
                            bookId INTEGER NOT NULL,
                            editionId INTEGER,
                            progressPages INTEGER,
                            progressSeconds INTEGER,
                            startedAt TEXT,
                            finishedAt TEXT,
                            enqueuedAt TEXT NOT NULL,
                            attempts INTEGER NOT NULL DEFAULT 0
                        )
                    """.trimIndent(),
                )
                connection.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_pending_progress_updates_userBookId_kind " +
                            "ON pending_progress_updates(userBookId, kind)",
                )
            }
        }

        private val MIGRATION_26_27 = object : Migration(26, 27) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS shelf_manual_order (
                            statusCode INTEGER NOT NULL,
                            bookId INTEGER NOT NULL,
                            position INTEGER NOT NULL,
                            PRIMARY KEY(statusCode, bookId)
                        )
                    """.trimIndent(),
                )

                connection.execSQL("CREATE INDEX IF NOT EXISTS index_shelf_manual_order_statusCode ON shelf_manual_order(statusCode)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_shelf_manual_order_bookId ON shelf_manual_order(bookId)")
            }
        }

        private val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE book_lists ADD COLUMN ranked INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE pending_progress_updates ADD COLUMN rating REAL DEFAULT NULL")
            }
        }

        // The pending-write queue is no longer progress-only (it carries mark-as-read and rating
        // writes too), so the table and its unique index are renamed to match the generalised model.
        private val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE pending_progress_updates RENAME TO pending_user_book_writes")

                connection.execSQL("DROP INDEX IF EXISTS index_pending_progress_updates_userBookId_kind")

                connection.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_pending_user_book_writes_userBookId_kind " +
                            "ON pending_user_book_writes(userBookId, kind)",
                )
            }
        }

        // The pending-write queue now also carries personal-review publishes, which need the review
        // body and its spoiler flag for offline replay; both are nullable so existing rows are intact.
        private val MIGRATION_32_33 = object : Migration(32, 33) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE pending_user_book_writes ADD COLUMN reviewBody TEXT DEFAULT NULL")

                connection.execSQL("ALTER TABLE pending_user_book_writes ADD COLUMN reviewHasSpoilers INTEGER DEFAULT NULL")
            }
        }

        // The user's own review text now rides on the cached user_book row (Hardcover is the source
        // of truth; the local copy is overwritten on every refresh), so the column is added here.
        private val MIGRATION_33_34 = object : Migration(33, 34) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE user_books ADD COLUMN review TEXT DEFAULT NULL")
            }
        }

        // The local personal-review draft store is retired — reviews are now owned by Hardcover and
        // cached on the user_book row — so its table is dropped.
        private val MIGRATION_34_35 = object : Migration(34, 35) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("DROP TABLE IF EXISTS personal_reviews")
            }
        }

        // Reviews become structured rich text: the cached review (previously the server's HTML in
        // `review`) and the offline-replay payload (previously the plain-text `reviewBody`) both become
        // a serialised `ReviewDocument` in `reviewSlateJson`. Neither old format is valid ReviewDocument
        // JSON, so both tables are recreated dropping the old column and the new one is left NULL — the
        // review re-fetches from Hardcover on the next refresh. SQLite < 3.35 (minSdk 26) can't drop a
        // column, so each table is rebuilt; all non-review data is carried across.
        private val MIGRATION_35_36 = object : Migration(35, 36) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE user_books_new (
                            id INTEGER NOT NULL,
                            bookId INTEGER NOT NULL,
                            statusCode INTEGER NOT NULL,
                            dateAdded TEXT NOT NULL,
                            privacySettingId INTEGER NOT NULL,
                            reviewHasSpoilers INTEGER NOT NULL,
                            editionId INTEGER,
                            lastReadDate TEXT,
                            rating REAL,
                            referrerUserId INTEGER,
                            reviewSlateJson TEXT,
                            reviewedAt TEXT,
                            updatedAt TEXT,
                            createdAt TEXT,
                            PRIMARY KEY(id),
                            FOREIGN KEY(bookId) REFERENCES books(id) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                        INSERT INTO user_books_new (
                            id, bookId, statusCode, dateAdded, privacySettingId, reviewHasSpoilers,
                            editionId, lastReadDate, rating, referrerUserId, reviewSlateJson, reviewedAt,
                            updatedAt, createdAt
                        )
                        SELECT
                            id, bookId, statusCode, dateAdded, privacySettingId, reviewHasSpoilers,
                            editionId, lastReadDate, rating, referrerUserId, NULL, reviewedAt,
                            updatedAt, createdAt
                        FROM user_books
                    """.trimIndent(),
                )

                connection.execSQL("DROP TABLE user_books")

                connection.execSQL("ALTER TABLE user_books_new RENAME TO user_books")

                connection.execSQL("CREATE INDEX IF NOT EXISTS index_user_books_bookId ON user_books(bookId)")

                connection.execSQL("CREATE INDEX IF NOT EXISTS index_user_books_statusCode ON user_books(statusCode)")

                connection.execSQL(
                    """
                        CREATE TABLE pending_user_book_writes_new (
                            localId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            kind TEXT NOT NULL,
                            userBookId INTEGER NOT NULL,
                            userBookReadId INTEGER NOT NULL,
                            bookId INTEGER NOT NULL,
                            editionId INTEGER,
                            progressPages INTEGER,
                            progressSeconds INTEGER,
                            startedAt TEXT,
                            finishedAt TEXT,
                            rating REAL,
                            reviewSlateJson TEXT,
                            reviewHasSpoilers INTEGER,
                            enqueuedAt TEXT NOT NULL,
                            attempts INTEGER NOT NULL DEFAULT 0
                        )
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                        INSERT INTO pending_user_book_writes_new (
                            localId, kind, userBookId, userBookReadId, bookId, editionId, progressPages,
                            progressSeconds, startedAt, finishedAt, rating, reviewSlateJson,
                            reviewHasSpoilers, enqueuedAt, attempts
                        )
                        SELECT
                            localId, kind, userBookId, userBookReadId, bookId, editionId, progressPages,
                            progressSeconds, startedAt, finishedAt, rating, NULL,
                            reviewHasSpoilers, enqueuedAt, attempts
                        FROM pending_user_book_writes
                    """.trimIndent(),
                )

                connection.execSQL("DROP TABLE pending_user_book_writes")

                connection.execSQL("ALTER TABLE pending_user_book_writes_new RENAME TO pending_user_book_writes")

                connection.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_pending_user_book_writes_userBookId_kind " +
                            "ON pending_user_book_writes(userBookId, kind)",
                )
            }
        }

        // Reading sessions gain honest pause tracking: pausedSeconds accumulates idle time from
        // completed pauses, and lastPausedAt marks a currently-open pause (NULL while running).
        // Both are ADD COLUMN (no DROP), so this is safe on SQLite < 3.35 (minSdk 26). An in-flight
        // active session migrates as a never-paused running session.
        private val MIGRATION_36_37 = object : Migration(36, 37) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE reading_sessions ADD COLUMN pausedSeconds INTEGER NOT NULL DEFAULT 0")

                connection.execSQL("ALTER TABLE reading_sessions ADD COLUMN lastPausedAt TEXT DEFAULT NULL")
            }
        }

        // Editions gain Hardcover's reading_format_id (1=Physical, 2=Audio, 3=Both, 4=Ebook),
        // surfaced in the edition selector. ADD COLUMN is safe on SQLite < 3.35 (minSdk 26); the
        // view selects edition.*, so it is dropped and recreated to expose the new column.
        private val MIGRATION_37_38 = object : Migration(37, 38) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE book_editions ADD COLUMN readingFormatId INTEGER DEFAULT NULL")

                connection.execSQL("DROP VIEW IF EXISTS book_edition_view")
                connection.execSQL(
                    """
                        CREATE VIEW `book_edition_view` AS SELECT
                                edition.*,
                                EXISTS(
                                    SELECT 1
                                    FROM list_books lb
                                    JOIN book_lists bl ON bl.id = lb.listId
                                    WHERE bl.slug = 'owned'
                                    AND (
                                        lb.editionId = edition.id
                                        OR lb.editionId = edition.canonicalId
                                        OR lb.editionId IN (
                                            SELECT sub.id FROM book_editions sub
                                            WHERE sub.canonicalId = edition.id
                                        )
                                    )
                                ) AS isOwned
                            FROM book_editions edition
                    """.trimIndent(),
                )
            }
        }

        // Books gain Hardcover's `headline` — a short editorial standfirst surfaced above the
        // book-detail description. ADD COLUMN is safe on SQLite < 3.35 (minSdk 26); existing rows
        // default to an empty headline and refill on the next book-detail refresh.
        private val MIGRATION_38_39 = object : Migration(38, 39) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE books ADD COLUMN headline TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_39_40 = object : Migration(39, 40) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE tags ADD COLUMN category TEXT NOT NULL DEFAULT 'OTHER'")

                connection.execSQL("ALTER TABLE book_tag_cross_ref ADD COLUMN count INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Lists gain a cached freshness `signature` (updated_at + list_books count + max list_book
        // updated_at) so a full refresh can skip re-fetching a list's contents when nothing changed.
        // ADD COLUMN is safe on SQLite < 3.35 (minSdk 26); existing rows default to NULL, which forces
        // one re-fetch per list on the first refresh after upgrade.
        private val MIGRATION_40_41 = object : Migration(40, 41) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE book_lists ADD COLUMN signature TEXT DEFAULT NULL")
            }
        }

        // Editions gain `localImageUrl` — the URL a locally-persisted cover file was downloaded from
        // — so a refresh can detect when the cover URL changed and evict the stale local file (covers
        // weren't updating on refresh). ADD COLUMN is safe on SQLite < 3.35 (minSdk 26); the view
        // selects edition.*, so it is dropped and recreated. Existing rows default to NULL, which is
        // treated as unknown provenance: any already-cached cover is re-fetched on the next refresh.
        private val MIGRATION_41_42 = object : Migration(41, 42) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE book_editions ADD COLUMN localImageUrl TEXT DEFAULT NULL")

                connection.execSQL("DROP VIEW IF EXISTS book_edition_view")
                connection.execSQL(
                    """
                        CREATE VIEW `book_edition_view` AS SELECT
                                edition.*,
                                EXISTS(
                                    SELECT 1
                                    FROM list_books lb
                                    JOIN book_lists bl ON bl.id = lb.listId
                                    WHERE bl.slug = 'owned'
                                    AND (
                                        lb.editionId = edition.id
                                        OR lb.editionId = edition.canonicalId
                                        OR lb.editionId IN (
                                            SELECT sub.id FROM book_editions sub
                                            WHERE sub.canonicalId = edition.id
                                        )
                                    )
                                ) AS isOwned
                            FROM book_editions edition
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_42_43 = object : Migration(42, 43) {
            override fun migrate(connection: SQLiteConnection) {
                // Index the authorId foreign side of both author junction tables so Room resolves the
                // author relationship via the index instead of a full table scan. Names match Room's
                // generated `index_<table>_authorId` so the migrated schema validates against 43.json.
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_book_author_cross_ref_authorId " +
                        "ON book_author_cross_ref(authorId)",
                )

                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_edition_author_cross_ref_authorId " +
                        "ON edition_author_cross_ref(authorId)",
                )
            }
        }

        private val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS pending_list_writes (
                            localId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                            kind TEXT NOT NULL,
                            listId INTEGER,
                            listName TEXT,
                            bookId INTEGER,
                            editionId INTEGER,
                            listBookId INTEGER,
                            startPosition INTEGER,
                            orderedListBookIdsCsv TEXT,
                            enqueuedAt TEXT NOT NULL,
                            attempts INTEGER NOT NULL DEFAULT 0
                        )
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_27_28 = object : Migration(27, 28) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE book_editions ADD COLUMN isbn13 TEXT DEFAULT NULL")

                connection.execSQL("DROP VIEW IF EXISTS book_edition_view")
                connection.execSQL(
                    """
                        CREATE VIEW `book_edition_view` AS SELECT
                                edition.*,
                                EXISTS(
                                    SELECT 1
                                    FROM list_books lb
                                    JOIN book_lists bl ON bl.id = lb.listId
                                    WHERE bl.slug = 'owned'
                                    AND (
                                        lb.editionId = edition.id
                                        OR lb.editionId = edition.canonicalId
                                        OR lb.editionId IN (
                                            SELECT sub.id FROM book_editions sub
                                            WHERE sub.canonicalId = edition.id
                                        )
                                    )
                                ) AS isOwned
                            FROM book_editions edition
                    """.trimIndent(),
                )
            }
        }

        private val MIGRATION_25_26 = object : Migration(25, 26) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS tags (
                            id INTEGER NOT NULL PRIMARY KEY,
                            name TEXT NOT NULL
                        )
                    """.trimIndent(),
                )

                connection.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS book_tag_cross_ref (
                            bookId INTEGER NOT NULL,
                            tagId INTEGER NOT NULL,
                            PRIMARY KEY(bookId, tagId)
                        )
                    """.trimIndent(),
                )

                connection.execSQL("CREATE INDEX IF NOT EXISTS index_book_tag_cross_ref_tagId ON book_tag_cross_ref(tagId)")
            }
        }

        // The single source of truth for the migration set, consumed by [build] and by migration
        // tests. Declared after every MIGRATION_* val so all are initialised before this references
        // them. Room selects the applicable path by version, so order here is for readability only.
        internal val ALL_MIGRATIONS: List<Migration> = listOf(
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
            MIGRATION_17_18,
            MIGRATION_18_19,
            MIGRATION_19_20,
            MIGRATION_20_21,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
            MIGRATION_33_34,
            MIGRATION_34_35,
            MIGRATION_35_36,
            MIGRATION_36_37,
            MIGRATION_37_38,
            MIGRATION_38_39,
            MIGRATION_39_40,
            MIGRATION_40_41,
            MIGRATION_41_42,
            MIGRATION_42_43,
        )
    }
}
