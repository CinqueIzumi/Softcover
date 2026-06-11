package nl.rhaydus.softcover.feature.app_update.di

import org.koin.dsl.module
import nl.rhaydus.softcover.feature.app_update.data.repository.AppUpdateRepositoryImpl
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CheckForAppUpdateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CompleteAppUpdateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.ObserveAppUpdateStateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.StartAppUpdateFlowUseCase

val appUpdateModule = module {
    includes(platformAppUpdateModule)

    single<AppUpdateRepository> {
        AppUpdateRepositoryImpl(appUpdateDataSource = get())
    }

    factory {
        ObserveAppUpdateStateUseCase(appUpdateRepository = get())
    }

    factory {
        CheckForAppUpdateUseCase(appUpdateRepository = get())
    }

    factory {
        StartAppUpdateFlowUseCase()
    }

    factory {
        CompleteAppUpdateUseCase(appUpdateRepository = get())
    }
}
