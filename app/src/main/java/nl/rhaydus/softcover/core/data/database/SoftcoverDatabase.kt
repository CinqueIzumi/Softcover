package nl.rhaydus.softcover.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import nl.rhaydus.softcover.feature.caching.data.database.BookDao
import nl.rhaydus.softcover.feature.caching.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookAuthorCrossRef
import nl.rhaydus.softcover.feature.caching.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookEntity
import nl.rhaydus.softcover.feature.caching.data.model.EditionAuthorCrossRef

@Database(
    entities = [
        BookEntity::class,
        BookEditionEntity::class,
        AuthorEntity::class,
        BookAuthorCrossRef::class,
        EditionAuthorCrossRef::class
    ],
    version = 2,
)
abstract class SoftcoverDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}