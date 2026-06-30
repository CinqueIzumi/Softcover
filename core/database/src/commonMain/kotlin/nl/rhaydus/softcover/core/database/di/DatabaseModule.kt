package nl.rhaydus.softcover.core.database.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.di.dispatcherModule

val databaseModule = module {
    includes(
        platformDatabaseModule,
        dispatcherModule,
    )
}
