package nl.rhaydus.softcover.core.identity.di

import org.koin.dsl.module
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.UpdateApiKeyUseCase

val identityModule = module {
    factory { GetUserIdUseCase(getUserIdAsFlowUseCase = get()) }

    factory { GetUserIdAsFlowUseCase(settingsRepository = get()) }

    factory { UpdateApiKeyUseCase(settingsRepository = get()) }
}
