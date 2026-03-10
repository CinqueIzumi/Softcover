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
import nl.rhaydus.softcover.feature.books.data.model.BookEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListEditionCrossRef
import nl.rhaydus.softcover.feature.books.data.model.BookListEntity
import nl.rhaydus.softcover.feature.books.data.model.EditionAuthorCrossRef
import nl.rhaydus.softcover.feature.books.data.model.ReadingJournalEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookReadEntity

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
        BookListEditionCrossRef::class,
    ],
    version = 7,
)
abstract class SoftcoverDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao

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
                            slug TEXT NOT NULL
                        )
                    """.trimIndent()
                )

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
    }
}