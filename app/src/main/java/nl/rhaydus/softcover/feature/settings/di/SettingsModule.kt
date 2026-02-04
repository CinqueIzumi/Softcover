package nl.rhaydus.softcover.feature.settings.di

import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.feature.settings.data.datastore.appSettings
import nl.rhaydus.softcover.feature.settings.data.repository.SettingsRepositoryImpl
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.presentation.viewmodel.SettingsScreenViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val settingsModule = module {
    single<SettingsScreenViewModel> {
        SettingsScreenViewModel(
            updateApiKeyUseCase = get(),
            getApiKeyUseCase = get(),
            resetUserDataUseCase = get(),
            initializeUserBooksUseCase = get(),
            appDispatchers = get(),
            flows = getAll(),
        )
    }

    single<SettingsLocalDataSource> {
        SettingsLocalDataSourceImpl(appSettingsDataStore = get())
    }

    single<SettingsRemoteDataSource> {
        SettingsRemoteDataSourceImpl(apolloClient = get())
    }

    single<AppSettingsDataStore> {
        AppSettingsDataStore(store = androidContext().appSettings)
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            settingsLocalDataSource = get(),
            settingsRemoteDataSource = get(),
        )
    }

    factory {
        GetApiKeyUseCase(
            settingsRepository = get()
        )
    }

    factory {
        GetUserIdUseCase(getUserIdAsFlowUseCase = get())
    }

    factory {
        GetUserIdAsFlowUseCase(settingsRepository = get())
    }

    factory {
        InitializeUserDataUseCase(
            settingsRepository = get(),
            cachingRepository = get(),
        )
    }

    factory {
        ResetUserDataUseCase(
            settingsRepository = get(),
            cachingRepository = get()
        )
    }

    factory {
        UpdateApiKeyUseCase(
            settingsRepository = get()
        )
    }
}