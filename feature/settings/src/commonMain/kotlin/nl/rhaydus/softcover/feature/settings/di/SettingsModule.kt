package nl.rhaydus.softcover.feature.settings.di

import nl.rhaydus.softcover.core.book.di.bookModule
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.di.databaseModule
import nl.rhaydus.softcover.core.domain.di.dispatcherModule
import nl.rhaydus.softcover.core.lists.di.listsModule
import nl.rhaydus.softcover.core.network.di.httpModule
import nl.rhaydus.softcover.core.preferences.di.preferencesModule
import nl.rhaydus.softcover.core.presentation.di.presentationModule
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentBundledDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentBundledDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentLocalDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentRemoteDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.PublishedDocumentRemoteDataSourceImpl
import nl.rhaydus.softcover.feature.settings.data.repository.ChangelogRepositoryImpl
import nl.rhaydus.softcover.feature.settings.data.repository.PublishedDocumentCache
import nl.rhaydus.softcover.feature.settings.data.repository.RoadmapRepositoryImpl
import nl.rhaydus.softcover.feature.settings.domain.repository.ChangelogRepository
import nl.rhaydus.softcover.feature.settings.domain.repository.RoadmapRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObserveChangelogUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObserveRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshChangelogUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetColorPaletteUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDateStyleUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetDynamicColorUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledListIdsUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetEnabledStatusCodesUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetLibraryTabOrderUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetThemeModeUseCase
import nl.rhaydus.softcover.feature.settings.presentation.collector.AppUpdateCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.ComponentGalleryCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.DateStyleCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.DisplayToggleRowsCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.DocumentPageCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.DocumentPageContentCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.DocumentPageRefreshCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.GalleryThemeConfigurationCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.LibraryTabCountsCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.LibraryVisibilityCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.PersistedLibraryVisibilityCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.SettingsCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.ThemeConfigurationCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.UiScaleCollector
import nl.rhaydus.softcover.feature.settings.presentation.collector.UserListsCollector
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPage
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.ComponentGalleryScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.DocumentPageScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.LibraryVisibilitySettingsScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenScreenModel
import nl.rhaydus.softcover.feature.settings.presentation.source.ChangelogPageSource
import nl.rhaydus.softcover.feature.settings.presentation.source.DocumentPageSource
import nl.rhaydus.softcover.feature.settings.presentation.source.RoadmapPageSource
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsModule = module {
    includes(
        dispatcherModule,
        listsModule,
        preferencesModule,
        presentationModule,
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
            observeAppUpdateStateUseCase = get(),
            appVersionProvider = get(),
        )
    }

    factory { ThemeConfigurationCollector() } bind SettingsCollector::class

    factory { DateStyleCollector() } bind SettingsCollector::class

    factory { DisplayToggleRowsCollector() } bind SettingsCollector::class

    factory { UiScaleCollector() } bind SettingsCollector::class

    factory { AppUpdateCollector() } bind SettingsCollector::class

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
    // region Published documents
    // Documents published from the repo (the roadmap) are fetched live so an edit reaches readers with
    // no app release, cached as raw markdown keyed by document, and backed by the build-time bundled
    // copy until the first fetch lands. The DAO is bound here rather than in `databaseModule`
    // — the local convention is that the consumer of a DAO owns its binding.
    single { get<SoftcoverDatabase>().publishedDocumentDao() }

    single<PublishedDocumentRemoteDataSource> {
        PublishedDocumentRemoteDataSourceImpl(
            httpEngine = get(),
            appDispatchers = get(),
        )
    }

    single<PublishedDocumentLocalDataSource> { PublishedDocumentLocalDataSourceImpl(dao = get()) }

    single<PublishedDocumentBundledDataSource> { PublishedDocumentBundledDataSourceImpl(appDispatchers = get()) }

    single {
        PublishedDocumentCache(
            remoteDataSource = get(),
            localDataSource = get(),
            bundledDataSource = get(),
        )
    }

    single<RoadmapRepository> { RoadmapRepositoryImpl(documentCache = get()) }

    factory { ObserveRoadmapUseCase(roadmapRepository = get()) }

    factory { RefreshRoadmapUseCase(roadmapRepository = get()) }
    // endregion
    // region Changelog
    single<ChangelogRepository> { ChangelogRepositoryImpl(documentCache = get()) }

    factory {
        ObserveChangelogUseCase(
            changelogRepository = get(),
            appVersionProvider = get(),
        )
    }

    factory { RefreshChangelogUseCase(changelogRepository = get()) }
    // endregion
    // region Document pages
    // One stack renders both the Roadmap and What's new pages; the page arrives as a Koin parameter and
    // selects its source.
    factory { DocumentPageContentCollector() } bind DocumentPageCollector::class

    factory { DocumentPageRefreshCollector() } bind DocumentPageCollector::class

    factory<DocumentPageSource>(named(DocumentPage.ROADMAP.name)) {
        RoadmapPageSource(
            observeRoadmapUseCase = get(),
            refreshRoadmapUseCase = get(),
        )
    }

    factory<DocumentPageSource>(named(DocumentPage.WHATS_NEW.name)) {
        ChangelogPageSource(
            observeChangelogUseCase = get(),
            refreshChangelogUseCase = get(),
            appVersionProvider = get(),
        )
    }

    factory { (page: DocumentPage) ->
        DocumentPageScreenModel(
            page = page,
            source = get(named(page.name)),
            appDispatchers = get(),
            flows = getAll(),
        )
    }
    // endregion
    // region Component gallery
    factory { GalleryThemeConfigurationCollector() } bind ComponentGalleryCollector::class

    factory {
        ComponentGalleryScreenModel(
            getThemeConfigurationUseCase = get(),
            appDispatchers = get(),
            flows = getAll(),
        )
    }
    // endregion
}
