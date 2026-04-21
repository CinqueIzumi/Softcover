package nl.rhaydus.softcover.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import nl.rhaydus.softcover.feature.books.data.dao.BookDao
import nl.rhaydus.softcover.feature.books.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.books.data.model.BookAuthorCrossRef
import nl.rhaydus.softcover.feature.books.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.books.data.model.BookEditionView
import nl.rhaydus.softcover.feature.books.data.model.BookEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListEntity
import nl.rhaydus.softcover.feature.books.data.model.BookSeriesEntity
import nl.rhaydus.softcover.feature.books.data.model.EditionAuthorCrossRef
import nl.rhaydus.softcover.feature.books.data.model.ListBookEntity
import nl.rhaydus.softcover.feature.books.data.model.ReadingJournalEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookReadEntity
import nl.rhaydus.softcover.feature.deadlines.data.dao.BookDeadlineDao
import nl.rhaydus.softcover.feature.deadlines.data.model.BookDeadlineEntity

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
    ],
    views = [
        BookEditionView::class
    ],
    version = 18,
)
abstract class SoftcoverDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

    abstract fun bookDeadlineDao(): BookDeadlineDao

    companion object {
        fun buildDatabase(context: Context): SoftcoverDatabase {
            return Room
                .databaseBuilder(
                    context = context,
                    klass = SoftcoverDatabase::class.java,
                    name = "books.db"
                )
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .addMigrations(MIGRATION_6_7)
                .addMigrations(MIGRATION_7_8)
                .addMigrations(MIGRATION_8_9)
                .addMigrations(MIGRATION_9_10)
                .addMigrations(MIGRATION_10_11)
                .addMigrations(MIGRATION_11_12)
                .addMigrations(MIGRATION_12_13)
                .addMigrations(MIGRATION_13_14)
                .addMigrations(MIGRATION_14_15)
                .addMigrations(MIGRATION_15_16)
                .addMigrations(MIGRATION_16_17)
                .addMigrations(MIGRATION_17_18)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
            ALTER TABLE book_editions 
            ADD COLUMN format TEXT NOT NULL DEFAULT ''
        """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the new reading_journals table
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS reading_journals (
                localId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userBookId INTEGER NOT NULL,
                event TEXT NOT NULL,
                updatedAt TEXT NOT NULL
            )
        """.trimIndent()
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
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
                    """
                )

                db.execSQL("CREATE INDEX index_user_books_bookId ON user_books(bookId)")
                db.execSQL("CREATE INDEX index_user_books_statusCode ON user_books(statusCode)")

                db.execSQL(
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
                    """
                )

                db.execSQL("CREATE INDEX index_user_book_reads_userBookId ON user_book_reads(userBookId)")

                db.execSQL(
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
                    """
                )

                db.execSQL(
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
                    """
                )

                db.execSQL(
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
                    """
                )

                db.execSQL(
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
                    """
                )

                db.execSQL("DROP TABLE books")
                db.execSQL("ALTER TABLE books_new RENAME TO books")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS book_lists (
                            id INTEGER PRIMARY KEY NOT NULL,
                            name TEXT NOT NULL,
                            slug TEXT NOT NULL DEFAULT ''
                        )
                    """.trimIndent()
                )

                try {
                    db.execSQL("ALTER TABLE book_lists ADD COLUMN slug TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {
                }

                db.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS book_list_edition_cross_ref (
                            bookListId INTEGER NOT NULL,
                            editionId INTEGER NOT NULL,
                            PRIMARY KEY(bookListId, editionId),
                            FOREIGN KEY(bookListId) REFERENCES book_lists(id) ON DELETE CASCADE,
                            FOREIGN KEY(editionId) REFERENCES book_editions(id) ON DELETE CASCADE
                        )
                    """.trimIndent()
                )

                db.execSQL("CREATE INDEX IF NOT EXISTS index_book_list_edition_cross_ref_bookListId ON book_list_edition_cross_ref(bookListId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_book_list_edition_cross_ref_editionId ON book_list_edition_cross_ref(editionId)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
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
            """.trimIndent()
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE book_lists ADD COLUMN slug TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {
                }

                db.execSQL(
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
            """.trimIndent()
                )

                db.execSQL(
                    """
                INSERT INTO list_books (listId, bookId, editionId)
                SELECT bler.bookListId, be.bookId, bler.editionId
                FROM book_list_edition_cross_ref bler
                JOIN book_editions be ON bler.editionId = be.id
            """.trimIndent()
                )

                db.execSQL("DROP VIEW IF EXISTS book_edition_view")
                db.execSQL(
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
            """.trimIndent()
                )

                db.execSQL("DROP TABLE IF EXISTS book_list_edition_cross_ref")

                db.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_listId ON list_books(listId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_bookId ON list_books(bookId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_editionId ON list_books(editionId)")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                        CREATE TABLE list_books_new (
                            listBookId INTEGER NOT NULL,
                            listId INTEGER NOT NULL,
                            bookId INTEGER NOT NULL,
                            editionId INTEGER NOT NULL,
                            position INTEGER,
                            PRIMARY KEY(listId, bookId, editionId, listBookId)
                        )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                        INSERT INTO list_books_new (listBookId, listId, bookId, editionId, position)
                        SELECT listBookId, listId, bookId, editionId, position FROM list_books
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE list_books")
                db.execSQL("ALTER TABLE list_books_new RENAME TO list_books")

                db.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_listId ON list_books(listId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_bookId ON list_books(bookId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_list_books_editionId ON list_books(editionId)")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                        DELETE FROM book_editions
                        WHERE id NOT IN (
                            SELECT editionId FROM user_books WHERE editionId IS NOT NULL
                        )
                        AND id NOT IN (
                            SELECT editionId FROM list_books
                        )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                        DELETE FROM edition_author_cross_ref
                        WHERE editionId NOT IN (SELECT id FROM book_editions)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                        DELETE FROM authors
                        WHERE id NOT IN (SELECT authorId FROM book_author_cross_ref)
                        AND id NOT IN (SELECT authorId FROM edition_author_cross_ref)
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE book_editions ADD COLUMN localImagePath TEXT DEFAULT NULL")

                db.execSQL("DROP VIEW IF EXISTS book_edition_view")
                db.execSQL(
                    "CREATE VIEW `book_edition_view` AS SELECT${' '}\n" +
                        "        edition.*,\n" +
                        "        EXISTS(\n" +
                        "            SELECT 1\n" +
                        "            FROM list_books lb\n" +
                        "            JOIN book_lists bl ON bl.id = lb.listId\n" +
                        "            WHERE lb.editionId = edition.id\n" +
                        "            AND bl.slug = 'owned'\n" +
                        "        ) AS isOwned\n" +
                        "    FROM book_editions edition"
                )
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE book_editions ADD COLUMN canonicalId INTEGER DEFAULT NULL")

                db.execSQL("DROP VIEW IF EXISTS book_edition_view")
                db.execSQL(
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
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS book_deadlines (
                            bookId INTEGER NOT NULL PRIMARY KEY,
                            deadlineDate TEXT NOT NULL,
                            setAt TEXT NOT NULL,
                            initialPagesPerDay REAL NOT NULL
                        )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE book_editions ADD COLUMN audioSeconds INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE user_book_reads ADD COLUMN currentSeconds INTEGER DEFAULT NULL")

                db.execSQL(
                    """
                        CREATE TABLE IF NOT EXISTS book_deadlines_new (
                            bookId INTEGER NOT NULL PRIMARY KEY,
                            deadlineDate TEXT NOT NULL,
                            setAt TEXT NOT NULL,
                            initialPerDay REAL NOT NULL,
                            unit TEXT NOT NULL DEFAULT 'PAGES'
                        )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                        INSERT INTO book_deadlines_new (bookId, deadlineDate, setAt, initialPerDay, unit)
                        SELECT bookId, deadlineDate, setAt, initialPagesPerDay, 'PAGES'
                        FROM book_deadlines
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE book_deadlines")
                db.execSQL("ALTER TABLE book_deadlines_new RENAME TO book_deadlines")

                db.execSQL("DROP VIEW IF EXISTS book_edition_view")
                db.execSQL(
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
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS book_series (
                id INTEGER NOT NULL, 
                name TEXT NOT NULL, 
                amountOfBooks INTEGER NOT NULL, 
                PRIMARY KEY(id)
            )
            """.trimIndent()
                )

                db.execSQL("ALTER TABLE books ADD COLUMN seriesId INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE books ADD COLUMN positionInSeries INTEGER DEFAULT NULL")
            }
        }

        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE list_books ADD COLUMN addedAt TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_books ADD COLUMN createdAt TEXT DEFAULT NULL")
            }
        }
    }
}