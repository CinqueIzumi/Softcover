package nl.rhaydus.softcover.core.database.di

import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import org.koin.dsl.module

val databaseModule = module {
    includes(
        platformDatabaseModule,
        dispatcherModule,
    )
}
