package nl.rhaydus.softcover.feature.settings.di

import org.koin.dsl.bind
import org.koin.dsl.module
import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.designsystem.presentation.di.designSystemModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.core.network.di.httpModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetReadingStreakEnabledAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUiScaleAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetReadingStreakEnabledUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetUiScaleUseCase
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapBundledDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapBundledDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapLocalDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapRemoteDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.RoadmapRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.repository.RoadmapRepositoryImpl
import nl.rhaydus.softcover.feature.settings.domain.repository.RoadmapRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObserveRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetColorPaletteUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDynamicColorUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryTabOrderUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetThemeModeUseCase
import nl.rhaydus.softcover.feature.settings.presentation.collector.DateStyleCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.LibraryTabCountsCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.LibraryVisibilityCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.PersistedLibraryVisibilityCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.ReadingStreakCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.RoadmapCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.RoadmapDocumentCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.RoadmapRefreshCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.SettingsCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.ShelfSwipeCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.ThemeConfigurationCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.UiScaleCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.UserListsCollector
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.RoadmapScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel

val settingsModule = module {
    includes(
        dispatcherModule,
        listsModule,
        preferencesModule,
        designSystemModule,
        bookModule,
        databaseModule,
        httpModule,
    )

    factory {
        SettingsScreenScreenModel(
            appDispatchers = get(),
            flows = getAll(),
            setBottomBarStyleUseCase = get(),
            setThemeModeUseCase = get(),
            setColorPaletteUseCase = get(),
            setDynamicColorUseCase = get(),
            getThemeConfigurationUseCase = get(),
            getDateStyleAsFlowUseCase = get(),
            setDateStyleUseCase = get(),
            getReadingStreakEnabledAsFlowUseCase = get(),
            setReadingStreakEnabledUseCase = get(),
            getShelfSwipeEnabledAsFlowUseCase = get(),
            setShelfSwipeEnabledUseCase = get(),
            getUiScaleAsFlowUseCase = get(),
            setUiScaleUseCase = get(),
            appVersionProvider = get(),
        )
    }

    factory { ThemeConfigurationCollector() } bind SettingsCollector::class

    factory { DateStyleCollector() } bind SettingsCollector::class

    factory { ReadingStreakCollector() } bind SettingsCollector::class

    factory { ShelfSwipeCollector() } bind SettingsCollector::class

    factory { UiScaleCollector() } bind SettingsCollector::class

    factory { SetBottomBarStyleUseCase(settingsRepository = get()) }

    factory { SetThemeModeUseCase(settingsRepository = get()) }

    factory { SetColorPaletteUseCase(settingsRepository = get()) }

    factory { SetDynamicColorUseCase(settingsRepository = get()) }

    factory { SetDateStyleUseCase(settingsRepository = get()) }

    factory { SetEnabledStatusCodesUseCase(settingsRepository = get()) }

    factory { SetEnabledListIdsUseCase(settingsRepository = get()) }

    factory { SetLibraryTabOrderUseCase(settingsRepository = get()) }

    factory { PersistedLibraryVisibilityCollector() } bind LibraryVisibilityCollector::class

    factory { UserListsCollector() } bind LibraryVisibilityCollector::class

    factory { LibraryTabCountsCollector() } bind LibraryVisibilityCollector::class

    factory {
        LibraryVisibilitySettingsScreenModel(
            getEnabledStatusCodesAsFlowUseCase = get(),
            getEnabledListIdsAsFlowUseCase = get(),
            getLibraryTabOrderAsFlowUseCase = get(),
            setEnabledStatusCodesUseCase = get(),
            setEnabledListIdsUseCase = get(),
            setLibraryTabOrderUseCase = get(),
            getAllUserListsUseCase = get(),
            getAllUserBooksUseCase = get(),
            getCurrentlyReadingUserBooksUseCase = get(),
            getWantToReadUserBooksUseCase = get(),
            getReadUserBooksUseCase = get(),
            getDidNotFinishUserBooksUseCase = get(),
            refreshLibraryUseCase = get(),
            applicationScope = get(),
            appDispatchers = get(),
            flows = getAll(),
        )
    }
    // region Roadmap
    // The Roadmap screen reads the repo's published ROADMAP.md: fetched live so a milestone edit
    // reaches readers with no app release, cached as raw markdown, and backed by the build-time
    // bundled copy until the first fetch lands. The DAO is bound here rather than in `databaseModule`
    // — the local convention is that the consumer of a DAO owns its binding.
    single { get<SoftcoverDatabase>().roadmapDocumentDao() }

    single<RoadmapRemoteDataSource> {
        RoadmapRemoteDataSourceImpl(
            httpEngine = get(),
            appDispatchers = get(),
        )
    }

    single<RoadmapLocalDataSource> { RoadmapLocalDataSourceImpl(dao = get()) }

    single<RoadmapBundledDataSource> { RoadmapBundledDataSourceImpl(appDispatchers = get()) }

    single<RoadmapRepository> {
        RoadmapRepositoryImpl(
            remoteDataSource = get(),
            localDataSource = get(),
            bundledDataSource = get(),
        )
    }

    factory { ObserveRoadmapUseCase(roadmapRepository = get()) }

    factory { RefreshRoadmapUseCase(roadmapRepository = get()) }

    factory { RoadmapDocumentCollector() } bind RoadmapCollector::class

    factory { RoadmapRefreshCollector() } bind RoadmapCollector::class

    factory {
        RoadmapScreenModel(
            observeRoadmapUseCase = get(),
            refreshRoadmapUseCase = get(),
            appDispatchers = get(),
            flows = getAll(),
        )
    }
    // endregion
    // region Component gallery
    factory { ComponentGalleryScreenModel(appDispatchers = get()) }
    // endregion
}
