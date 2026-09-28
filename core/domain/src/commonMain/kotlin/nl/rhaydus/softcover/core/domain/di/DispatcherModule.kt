package nl.rhaydus.softcover.core.domain.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCase
import nl.rhaydus.softcover.core.domain.util.ObserveCurrentDateUseCaseImpl
import org.koin.dsl.bind
import org.koin.dsl.module

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

    factory { ObserveCurrentDateUseCaseImpl() } bind ObserveCurrentDateUseCase::class
}
