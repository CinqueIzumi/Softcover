package nl.rhaydus.softcover.feature.widget.di

import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgets
import nl.rhaydus.softcover.feature.widget.data.repository.CurrentlyReadingRepositoryImpl
import nl.rhaydus.softcover.feature.widget.data.repository.RandomPickRepositoryImpl
import nl.rhaydus.softcover.feature.widget.data.repository.TrendingRepositoryImpl
import nl.rhaydus.softcover.feature.widget.domain.repository.CurrentlyReadingRepository
import nl.rhaydus.softcover.feature.widget.domain.repository.RandomPickRepository
import nl.rhaydus.softcover.feature.widget.domain.repository.TrendingRepository
import nl.rhaydus.softcover.feature.widget.domain.usecase.AdvanceTrendingPageUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.CycleSelectedBookUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetPlacedTrendingSettingsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetRandomPickSettingsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetSelectedBookIdUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetTrendingPositionUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetTrendingSettingsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveRandomPickChangesUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveSelectedBookChangesUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingChangesUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ResolveRandomPickUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.SaveRandomPickSettingsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.SaveTrendingSettingsUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ShuffleRandomPickUseCase
import nl.rhaydus.softcover.feature.widget.presentation.collector.LastUsedProgressUnitCollector
import nl.rhaydus.softcover.feature.widget.presentation.collector.ProgressSheetCollector
import nl.rhaydus.softcover.feature.widget.presentation.collector.RandomPickListsCollector
import nl.rhaydus.softcover.feature.widget.presentation.collector.RandomPickThemeCollector
import nl.rhaydus.softcover.feature.widget.presentation.collector.TrendingThemeCollector
import nl.rhaydus.softcover.feature.widget.presentation.collector.UpdateProgressThemeCollector
import nl.rhaydus.softcover.feature.widget.presentation.glance.CurrentlyReadingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.CurrentlyReadingWidgetDependencies
import nl.rhaydus.softcover.feature.widget.presentation.glance.OwnedWidgets
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.RandomPickWidgetDependencies
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidget
import nl.rhaydus.softcover.feature.widget.presentation.glance.TrendingWidgetDependencies
import nl.rhaydus.softcover.feature.widget.presentation.glance.WidgetEnvironment
import nl.rhaydus.softcover.feature.widget.presentation.glance.WidgetRepainter
import nl.rhaydus.softcover.feature.widget.presentation.placed.AndroidHomeScreenWidgets
import nl.rhaydus.softcover.feature.widget.presentation.placed.WidgetInstanceChanges
import nl.rhaydus.softcover.feature.widget.presentation.refresh.WidgetRefresher
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.RandomPickSettingsScreenModel
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsScreenModel
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.UpdateProgressScreenModel
import nl.rhaydus.softcover.feature.widget.presentation.worker.TrendingRefreshScheduler
import nl.rhaydus.softcover.feature.widget.presentation.worker.WidgetMaintenanceScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val widgetModule = module {
    single<CurrentlyReadingRepository> { CurrentlyReadingRepositoryImpl(context = androidContext()) }

    single<RandomPickRepository> { RandomPickRepositoryImpl(context = androidContext()) }

    single<TrendingRepository> { TrendingRepositoryImpl(context = androidContext()) }

    factory { GetSelectedBookIdUseCase(currentlyReadingRepository = get()) }

    factory { CycleSelectedBookUseCase(currentlyReadingRepository = get()) }

    factory { ObserveSelectedBookChangesUseCase(currentlyReadingRepository = get()) }

    factory { GetRandomPickSettingsUseCase(randomPickRepository = get()) }

    factory { SaveRandomPickSettingsUseCase(randomPickRepository = get()) }

    factory { ResolveRandomPickUseCase(randomPickRepository = get()) }

    factory { ShuffleRandomPickUseCase(randomPickRepository = get()) }

    factory { ObserveRandomPickChangesUseCase(randomPickRepository = get()) }

    factory { GetTrendingSettingsUseCase(trendingRepository = get()) }

    factory { GetPlacedTrendingSettingsUseCase(trendingRepository = get()) }

    factory { GetTrendingPositionUseCase(trendingRepository = get()) }

    factory { SaveTrendingSettingsUseCase(trendingRepository = get()) }

    factory { AdvanceTrendingPageUseCase(trendingRepository = get()) }

    factory { ObserveTrendingChangesUseCase(trendingRepository = get()) }

    single { WidgetRepainter(context = androidContext()) }

    single {
        WidgetEnvironment(
            getUserIdAsFlow = get(),
            getThemeConfiguration = get(),
            appEntryPoint = get(),
        )
    }

    single {
        CurrentlyReadingWidgetDependencies(
            environment = get(),
            repainter = get(),
            getCurrentlyReadingUserBooks = get(),
            getSelectedBookId = get(),
            cycleSelectedBook = get(),
            observeSelectedBookChanges = get(),
        )
    }

    single {
        RandomPickWidgetDependencies(
            environment = get(),
            repainter = get(),
            getWantToReadUserBooks = get(),
            getAllUserLists = get(),
            resolveRandomPick = get(),
            shuffleRandomPick = get(),
            observeRandomPickChanges = get(),
        )
    }

    single {
        OwnedWidgets(
            widgets = listOf(
                CurrentlyReadingWidget(),
                RandomPickWidget(),
                TrendingWidget(),
            ),
        )
    }

    single {
        TrendingWidgetDependencies(
            environment = get(),
            repainter = get(),
            observeTrendingBooks = get(),
            refreshTrendingBooks = get(),
            getTrendingPosition = get(),
            advanceTrendingPage = get(),
            observeTrendingChanges = get(),
        )
    }

    single {
        TrendingRefreshScheduler(
            context = androidContext(),
            getPlacedTrendingSettings = get(),
        )
    }

    factory {
        TrendingSettingsScreenModel(
            getTrendingSettings = get(),
            saveTrendingSettings = get(),
            repainter = get(),
            scheduler = get(),
            getThemeConfiguration = get(),
            appDispatchers = get(),
            flows = listOf(TrendingThemeCollector()),
        )
    }

    single {
        WidgetMaintenanceScheduler(
            context = androidContext(),
            ownedWidgets = get(),
        )
    }

    factory {
        RandomPickSettingsScreenModel(
            getRandomPickSettings = get(),
            saveRandomPickSettings = get(),
            repainter = get(),
            getThemeConfiguration = get(),
            getAllUserLists = get(),
            appDispatchers = get(),
            flows = listOf(
                RandomPickThemeCollector(),
                RandomPickListsCollector(),
            ),
        )
    }

    factory {
        UpdateProgressScreenModel(
            context = androidContext(),
            getUserIdAsFlow = get(),
            getCurrentlyReadingUserBooks = get(),
            recordBookProgress = get(),
            markBookAsRead = get(),
            getLastUsedProgressUnitAsFlow = get(),
            setLastUsedProgressUnit = get(),
            getThemeConfiguration = get(),
            appEntryPoint = get(),
            appDispatchers = get(),
            flows = listOf(
                UpdateProgressThemeCollector(),
                LastUsedProgressUnitCollector(),
                ProgressSheetCollector(),
            ),
        )
    }

    single {
        WidgetRefresher(
            context = androidContext(),
            applicationScope = get(),
            widgets = get<OwnedWidgets>().widgets,
            getThemeConfiguration = get(),
            getUserIdAsFlow = get(),
            getCurrentlyReadingUserBooks = get(),
            getWantToReadUserBooks = get(),
            getAllUserLists = get(),
            observeTrendingBooks = get(),
            trendingRefreshScheduler = get(),
        )
    }

    single { WidgetInstanceChanges() }

    single<HomeScreenWidgets> {
        AndroidHomeScreenWidgets(
            context = androidContext(),
            ownedWidgets = get(),
            observeRandomPickChanges = get(),
            observeTrendingChanges = get(),
            getRandomPickSettings = get(),
            getTrendingSettings = get(),
            getAllUserLists = get(),
            instanceChanges = get(),
            appDispatchers = get(),
        )
    }
}
