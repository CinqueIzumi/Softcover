package nl.rhaydus.softcover.core.database.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import nl.rhaydus.softcover.core.database.SoftcoverDatabase

val databaseModule = module {
    single<SoftcoverDatabase> {
        SoftcoverDatabase.buildDatabase(context = androidContext())
    }
}
