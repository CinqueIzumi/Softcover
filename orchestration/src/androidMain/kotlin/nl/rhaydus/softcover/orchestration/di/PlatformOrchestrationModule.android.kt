package nl.rhaydus.softcover.orchestration.di

import nl.rhaydus.softcover.core.presentation.navigation.AppEntryPoint
import nl.rhaydus.softcover.feature.widget.di.widgetModule
import nl.rhaydus.softcover.orchestration.navigation.AppEntryPointImpl
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformOrchestrationModule: Module = module {
    includes(widgetModule)

    single<AppEntryPoint> { AppEntryPointImpl() }
}
