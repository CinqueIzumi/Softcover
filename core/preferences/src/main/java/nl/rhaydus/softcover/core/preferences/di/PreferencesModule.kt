package nl.rhaydus.softcover.core.preferences.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import nl.rhaydus.softcover.core.domain.auth.AuthTokenProvider
import nl.rhaydus.softcover.core.preferences.data.datasource.ApiKeyLocalDataSource
import nl.rhaydus.softcover.core.preferences.data.datasource.ApiKeyLocalDataSourceImpl
import nl.rhaydus.softcover.core.preferences.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.core.preferences.data.datasource.SettingsLocalDataSourceImpl
import nl.rhaydus.softcover.core.preferences.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.core.preferences.data.datasource.SettingsRemoteDataSourceImpl
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.datastore.createAppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.repository.SettingsRepositoryImpl
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.softcover.core.preferences.domain.usecase.DismissPlanTodayUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryGridLayoutAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibrarySortSettingsAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.ObservePlanTodayDismissalsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLibraryGridLayoutUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetLibrarySortUseCase

val preferencesModule = module {
    single<AppSettingsDataStore> {
        AppSettingsDataStore(store = createAppSettingsDataStore(context = androidContext()))
    }

    single<ApiKeyLocalDataSource> {
        ApiKeyLocalDataSourceImpl(
            context = androidContext(),
            appSettingsDataStore = get(),
            dispatchers = get(),
        )
    }

    single<AuthTokenProvider> { get<ApiKeyLocalDataSource>() }

    single<SettingsLocalDataSource> {
        SettingsLocalDataSourceImpl(
            appSettingsDataStore = get(),
            apiKeyLocalDataSource = get(),
        )
    }

    single<SettingsRemoteDataSource> {
        SettingsRemoteDataSourceImpl(apolloClient = get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(
            settingsLocalDataSource = get(),
            settingsRemoteDataSource = get(),
        )
    }

    factory { GetDateStyleAsFlowUseCase(settingsRepository = get()) }

    factory { GetLibraryGridLayoutAsFlowUseCase(settingsRepository = get()) }

    factory { GetLibrarySortSettingsAsFlowUseCase(settingsRepository = get()) }

    factory { GetEnabledStatusCodesAsFlowUseCase(settingsRepository = get()) }

    factory { GetEnabledListIdsAsFlowUseCase(settingsRepository = get()) }

    factory { GetLibraryTabOrderAsFlowUseCase(settingsRepository = get()) }

    factory { ObservePlanTodayDismissalsUseCase(settingsRepository = get()) }

    factory { SetLibraryGridLayoutUseCase(settingsRepository = get()) }

    factory { SetLibrarySortUseCase(settingsRepository = get()) }

    factory { DismissPlanTodayUseCase(settingsRepository = get()) }

    single<GetThemeConfigurationUseCase> {
        GetThemeConfigurationUseCase(settingsRepository = get())
    }
}
