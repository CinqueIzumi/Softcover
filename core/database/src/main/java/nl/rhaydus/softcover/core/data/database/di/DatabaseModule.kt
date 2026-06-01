package nl.rhaydus.softcover.core.data.database.di

import nl.rhaydus.softcover.core.data.database.SoftcoverDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<SoftcoverDatabase> {
        SoftcoverDatabase.buildDatabase(context = androidContext())
    }
}