package nl.rhaydus.softcover.di

import kotlinx.coroutines.Dispatchers
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import org.koin.dsl.module

val dispatcherModule = module {
    single {
        AppDispatchers(
            main = Dispatchers.Main,
            io = Dispatchers.IO,
            default = Dispatchers.Default
        )
    }
}