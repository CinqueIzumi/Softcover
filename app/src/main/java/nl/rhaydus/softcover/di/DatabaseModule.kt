package nl.rhaydus.softcover.di

import androidx.room.Room
import nl.rhaydus.softcover.core.data.database.SoftcoverDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<SoftcoverDatabase> {
        Room.databaseBuilder(
            androidContext(),
            SoftcoverDatabase::class.java,
            "books.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}