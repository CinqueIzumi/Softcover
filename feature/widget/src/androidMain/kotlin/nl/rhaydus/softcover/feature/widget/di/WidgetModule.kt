package nl.rhaydus.softcover.feature.widget.di

import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgets
import nl.rhaydus.softcover.feature.widget.data.datastore.RandomPickStore
import nl.rhaydus.softcover.feature.widget.data.datastore.TrendingStore
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
    single {
        CurrentlyReadingWidgetDependencies(
            getUserIdAsFlow = get(),
            getThemeConfiguration = get(),
            getCurrentlyReadingUserBooks = get(),
            appEntryPoint = get(),
        )
    }

    single { RandomPickStore(context = androidContext()) }

    single {
        RandomPickWidgetDependencies(
            getUserIdAsFlow = get(),
            getThemeConfiguration = get(),
            getWantToReadUserBooks = get(),
            getAllUserLists = get(),
            store = get(),
            appEntryPoint = get(),
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

    single { TrendingStore(context = androidContext()) }

    single {
        TrendingWidgetDependencies(
            getUserIdAsFlow = get(),
            getThemeConfiguration = get(),
            observeTrendingBooks = get(),
            refreshTrendingBooks = get(),
            store = get(),
            appEntryPoint = get(),
        )
    }

    single {
        TrendingRefreshScheduler(
            context = androidContext(),
            store = get(),
        )
    }

    factory {
        TrendingSettingsScreenModel(
            store = get(),
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
            store = get(),
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
        )
    }

    single { WidgetInstanceChanges() }

    single<HomeScreenWidgets> {
        AndroidHomeScreenWidgets(
            context = androidContext(),
            ownedWidgets = get(),
            randomPickStore = get(),
            trendingStore = get(),
            getAllUserLists = get(),
            instanceChanges = get(),
            appDispatchers = get(),
        )
    }
}
