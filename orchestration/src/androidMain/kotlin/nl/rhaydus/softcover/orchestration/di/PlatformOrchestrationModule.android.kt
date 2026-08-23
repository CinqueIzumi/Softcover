package nl.rhaydus.softcover.orchestration.di

import org.koin.core.module.Module
import org.koin.dsl.module
import nl.rhaydus.softcover.core.presentation.navigation.AppEntryPoint
import nl.rhaydus.softcover.orchestration.navigation.AppEntryPointImpl

internal actual val platformOrchestrationModule: Module = module {
    single<AppEntryPoint> { AppEntryPointImpl() }
}
