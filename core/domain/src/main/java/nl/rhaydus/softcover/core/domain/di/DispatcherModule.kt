package nl.rhaydus.softcover.core.domain.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.ApplicationScope

val dispatcherModule = module {
    single {
        AppDispatchers(
            main = Dispatchers.Main,
            io = Dispatchers.IO,
            default = Dispatchers.Default,
        )
    }

    single {
        ApplicationScope(scope = CoroutineScope(Dispatchers.Default + SupervisorJob()))
    }
}