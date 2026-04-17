package nl.rhaydus.softcover.feature.app_update.di

import nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSource
import nl.rhaydus.softcover.feature.app_update.data.datasource.AppUpdateDataSourceImpl
import nl.rhaydus.softcover.feature.app_update.data.repository.AppUpdateRepositoryImpl
import nl.rhaydus.softcover.feature.app_update.domain.repository.AppUpdateRepository
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CheckForAppUpdateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CompleteAppUpdateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.ObserveAppUpdateStateUseCase
import nl.rhaydus.softcover.feature.app_update.domain.usecase.StartAppUpdateFlowUseCase
import org.koin.dsl.module

val appUpdateModule = module {
    single {
        AppUpdateDataSourceImpl(appUpdateManager = get())
    }

    single<AppUpdateDataSource> { get<AppUpdateDataSourceImpl>() }

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
        StartAppUpdateFlowUseCase(appUpdateRepository = get())
    }

    factory {
        CompleteAppUpdateUseCase(appUpdateRepository = get())
    }
}
