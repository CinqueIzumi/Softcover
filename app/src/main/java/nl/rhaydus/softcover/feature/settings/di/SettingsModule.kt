package nl.rhaydus.softcover.feature.settings.di

import org.koin.dsl.bind
import org.koin.dsl.module
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserIdAndBooksUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDynamicColorUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryTabOrderUseCase
import nl.rhaydus.softcover.feature.settings.presentation.flows.DateStyleCollector
import nl.rhaydus.softcover.feature.settings.presentation.flows.LibraryVisibilityInitializer
import nl.rhaydus.softcover.feature.settings.presentation.flows.PersistedLibraryVisibilityCollector
import nl.rhaydus.softcover.feature.settings.presentation.flows.SettingsInitializer
import nl.rhaydus.softcover.feature.settings.presentation.flows.ThemeConfigurationCollector
import nl.rhaydus.softcover.feature.settings.presentation.flows.UserListsCollector
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel

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

    factory { ThemeConfigurationCollector() } bind SettingsInitializer::class

    factory { DateStyleCollector() } bind SettingsInitializer::class

    factory {
        InitializeUserIdAndBooksUseCase(
            settingsRepository = get(),
            refreshLibraryUseCase = get(),
        )
    }

    factory {
        ResetUserDataUseCase(
            settingsRepository = get(),
            booksRepository = get(),
            profileRepository = get(),
        )
    }

    factory { SetBottomBarStyleUseCase(settingsRepository = get()) }

    factory { SetDynamicColorUseCase(settingsRepository = get()) }

    factory { SetDateStyleUseCase(settingsRepository = get()) }

    factory { SetEnabledStatusCodesUseCase(settingsRepository = get()) }

    factory { SetEnabledListIdsUseCase(settingsRepository = get()) }

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
            refreshLibraryUseCase = get(),
            applicationScope = get(),
            appDispatchers = get(),
            flows = getAll(),
        )
    }
}
