package nl.rhaydus.softcover.feature.settings.di

import nl.rhaydus.softcover.feature.settings.data.datasource.ApiKeyLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.ApiKeyLocalDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.feature.settings.data.datastore.appSettings
import nl.rhaydus.softcover.feature.settings.data.repository.SettingsRepositoryImpl
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledListIdsAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetEnabledStatusCodesAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetLibraryGridLayoutAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDynamicColorUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.DismissPlanTodayUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetLibrarySortSettingsAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetLibraryTabOrderAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObservePlanTodayDismissalsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryGridLayoutUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibrarySortUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryTabOrderUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.presentation.flows.DateStyleCollector
import nl.rhaydus.softcover.feature.settings.presentation.flows.LibraryVisibilityInitializer
import nl.rhaydus.softcover.feature.settings.presentation.flows.PersistedLibraryVisibilityCollector
import nl.rhaydus.softcover.feature.settings.presentation.flows.SettingsInitializer
import nl.rhaydus.softcover.feature.settings.presentation.flows.ThemeConfigurationCollector
import nl.rhaydus.softcover.feature.settings.presentation.flows.UserListsCollector
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    factory {
        SettingsScreenScreenModel(
            appDispatchers = get(),
            flows = getAll(),
            setBottomBarStyleUseCase = get(),
            setDynamicColorUseCase = get(),
            getThemeConfigurationUseCase = get(),
            getDateStyleAsFlowUseCase = get(),
            setDateStyleUseCase = get(),
        )
    }

    single<SettingsLocalDataSource> {
        SettingsLocalDataSourceImpl(
            appSettingsDataStore = get(),
            apiKeyLocalDataSource = get(),
        )
    }

    single<ApiKeyLocalDataSource> {
        ApiKeyLocalDataSourceImpl(
            context = androidContext(),
            appSettingsDataStore = get(),
            dispatchers = get(),
        )
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

    factory { ThemeConfigurationCollector() } bind SettingsInitializer::class

    factory { DateStyleCollector() } bind SettingsInitializer::class

    factory {
        GetUserIdUseCase(getUserIdAsFlowUseCase = get())
    }

    factory {
        GetUserIdAsFlowUseCase(settingsRepository = get())
    }

    factory {
        InitializeUserIdAndBooksUseCase(
            settingsRepository = get(),
            booksRepository = get(),
        )
    }

    factory {
        ResetUserDataUseCase(
            settingsRepository = get(),
            booksRepository = get(),
            profileRepository = get(),
        )
    }

    factory {
        UpdateApiKeyUseCase(settingsRepository = get())
    }

    factory {
        SetBottomBarStyleUseCase(settingsRepository = get())
    }

    factory {
        SetDynamicColorUseCase(settingsRepository = get())
    }

    factory {
        SetDateStyleUseCase(settingsRepository = get())
    }

    factory {
        GetDateStyleAsFlowUseCase(settingsRepository = get())
    }

    factory {
        GetLibraryGridLayoutAsFlowUseCase(settingsRepository = get())
    }

    factory {
        SetLibraryGridLayoutUseCase(settingsRepository = get())
    }

    factory { GetLibrarySortSettingsAsFlowUseCase(settingsRepository = get()) }

    factory { SetLibrarySortUseCase(settingsRepository = get()) }

    factory { ObservePlanTodayDismissalsUseCase(settingsRepository = get()) }

    factory { DismissPlanTodayUseCase(settingsRepository = get()) }

    single<GetThemeConfigurationUseCase> {
        GetThemeConfigurationUseCase(settingsRepository = get())
    }

    factory { GetEnabledStatusCodesAsFlowUseCase(settingsRepository = get()) }

    factory { GetEnabledListIdsAsFlowUseCase(settingsRepository = get()) }

    factory { SetEnabledStatusCodesUseCase(settingsRepository = get()) }

    factory { SetEnabledListIdsUseCase(settingsRepository = get()) }

    factory { GetLibraryTabOrderAsFlowUseCase(settingsRepository = get()) }

    factory { SetLibraryTabOrderUseCase(settingsRepository = get()) }

    factory { PersistedLibraryVisibilityCollector() } bind LibraryVisibilityInitializer::class

    factory { UserListsCollector() } bind LibraryVisibilityInitializer::class

    factory {
        LibraryVisibilitySettingsScreenModel(
            getEnabledStatusCodesAsFlowUseCase = get(),
            getEnabledListIdsAsFlowUseCase = get(),
            getLibraryTabOrderAsFlowUseCase = get(),
            setEnabledStatusCodesUseCase = get(),
            setEnabledListIdsUseCase = get(),
            setLibraryTabOrderUseCase = get(),
            getAllUserListsUseCase = get(),
            refreshUserBooksUseCase = get(),
            applicationScope = get(),
            appDispatchers = get(),
            flows = getAll(),
        )
    }
}