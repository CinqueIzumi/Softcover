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
import nl.rhaydus.softcover.feature.books.data.model.EditionAuthorCrossRef

@Database(
    entities = [
        BookEntity::class,
        BookEditionEntity::class,
        AuthorEntity::class,
        BookAuthorCrossRef::class,
        EditionAuthorCrossRef::class
    ],
    version = 4,
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
    }
}