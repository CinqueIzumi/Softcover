package nl.rhaydus.softcover.core.database.di

import org.koin.dsl.module

val databaseModule = module {
    includes(platformDatabaseModule)
}
