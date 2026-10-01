package nl.rhaydus.softcover.core.domain.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import org.koin.dsl.module

val dispatcherModule = module {
    single {
        AppDispatchers(
            // Immediate so a collector's derived UI state lands in the same frame as the state it
            // derives from; a posted dispatch draws one frame without it and shifts the layout.
            main = Dispatchers.Main.immediate,
            io = Dispatchers.IO,
            default = Dispatchers.Default,
        )
    }

    single {
        ApplicationScope(scope = CoroutineScope(Dispatchers.Default + SupervisorJob()))
    }
}
