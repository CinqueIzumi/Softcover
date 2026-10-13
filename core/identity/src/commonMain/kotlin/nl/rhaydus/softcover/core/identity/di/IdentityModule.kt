package nl.rhaydus.softcover.core.identity.di

import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.UpdateApiKeyUseCase
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import org.koin.dsl.module

val identityModule = module {
    includes(
        dispatcherModule,
        preferencesModule,
    )

    factory { GetUserIdUseCase(getUserIdAsFlowUseCase = get()) }

    factory { GetUserIdAsFlowUseCase(settingsRepository = get()) }

    factory { UpdateApiKeyUseCase(settingsRepository = get()) }
}
