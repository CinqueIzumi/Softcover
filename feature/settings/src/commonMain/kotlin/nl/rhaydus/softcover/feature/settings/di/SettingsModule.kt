package nl.rhaydus.softcover.feature.settings.di

import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.library.di.libraryServiceModule
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetReadingStreakEnabledUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDynamicColorUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryTabOrderUseCase
import nl.rhaydus.softcover.feature.settings.presentation.collector.DateStyleCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.LibraryVisibilityCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.PersistedLibraryVisibilityCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.ReadingStreakCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.SettingsCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.ThemeConfigurationCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.UserListsCollector
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    includes(
        dispatcherModule,
        listsModule,
        libraryServiceModule,
        preferencesModule,
        designSystemModule,
    )

    factory {
        SettingsScreenScreenModel(
            appDispatchers = get(),
            flows = getAll(),
            setBottomBarStyleUseCase = get(),
            setDynamicColorUseCase = get(),
            getThemeConfigurationUseCase = get(),
            getDateStyleAsFlowUseCase = get(),
            setDateStyleUseCase = get(),
            getReadingStreakEnabledAsFlowUseCase = get(),
            setReadingStreakEnabledUseCase = get(),
            appVersionProvider = get(),
        )
    }

    factory { ThemeConfigurationCollector() } bind SettingsCollector::class

    factory { DateStyleCollector() } bind SettingsCollector::class

    factory { ReadingStreakCollector() } bind SettingsCollector::class

    factory { SetBottomBarStyleUseCase(settingsRepository = get()) }

    factory { SetDynamicColorUseCase(settingsRepository = get()) }

    factory { SetDateStyleUseCase(settingsRepository = get()) }

    factory { SetEnabledStatusCodesUseCase(settingsRepository = get()) }

    factory { SetEnabledListIdsUseCase(settingsRepository = get()) }

    factory { SetLibraryTabOrderUseCase(settingsRepository = get()) }

    factory { PersistedLibraryVisibilityCollector() } bind LibraryVisibilityCollector::class

    factory { UserListsCollector() } bind LibraryVisibilityCollector::class

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
