package nl.rhaydus.softcover.feature.settings.di

import nl.rhaydus.softcover.feature.profile.presentation.initializer.ProfileInitializer
import nl.rhaydus.softcover.feature.profile.presentation.initializer.UserInformationInitializer
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.feature.settings.data.datastore.appSettings
import nl.rhaydus.softcover.feature.settings.data.repository.SettingsRepositoryImpl
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetDateStyleAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.presentation.flows.DateStyleCollector
import nl.rhaydus.softcover.feature.settings.presentation.flows.SettingsInitializer
import nl.rhaydus.softcover.feature.settings.presentation.flows.ThemeConfigurationCollector
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
            getThemeConfigurationUseCase = get(),
            getDateStyleAsFlowUseCase = get(),
            setDateStyleUseCase = get(),
        )
    }

    factory { UserInformationInitializer() } bind ProfileInitializer::class

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

    factory { ThemeConfigurationCollector() } bind SettingsInitializer::class

    factory { DateStyleCollector() } bind SettingsInitializer::class

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
        InitializeUserIdAndBooksUseCase(
            settingsRepository = get(),
            booksRepository = get(),
        )
    }

    factory {
        ResetUserDataUseCase(
            settingsRepository = get(),
            booksRepository = get(),
        )
    }

    factory {
        UpdateApiKeyUseCase(settingsRepository = get())
    }

    factory {
        SetBottomBarStyleUseCase(settingsRepository = get())
    }

    factory {
        SetDateStyleUseCase(settingsRepository = get())
    }

    factory {
        GetDateStyleAsFlowUseCase(settingsRepository = get())
    }

    single<GetThemeConfigurationUseCase> {
        GetThemeConfigurationUseCase(settingsRepository = get())
    }
}