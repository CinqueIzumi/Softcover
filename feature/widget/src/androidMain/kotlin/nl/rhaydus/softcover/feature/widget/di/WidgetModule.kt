package nl.rhaydus.softcover.feature.widget.di

import nl.rhaydus.softcover.feature.widget.currentlyreading.CurrentlyReadingWidget
import nl.rhaydus.softcover.feature.widget.currentlyreading.CurrentlyReadingWidgetDependencies
import nl.rhaydus.softcover.feature.widget.progress.LastUsedProgressUnitCollector
import nl.rhaydus.softcover.feature.widget.progress.ProgressSheetCollector
import nl.rhaydus.softcover.feature.widget.progress.UpdateProgressScreenModel
import nl.rhaydus.softcover.feature.widget.progress.UpdateProgressThemeCollector
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickStore
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickWidget
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickWidgetDependencies
import nl.rhaydus.softcover.feature.widget.refresh.WidgetRefresher
import nl.rhaydus.softcover.feature.widget.settings.randompick.ListsCollector
import nl.rhaydus.softcover.feature.widget.settings.randompick.RandomPickSettingsScreenModel
import nl.rhaydus.softcover.feature.widget.settings.randompick.ThemeCollector
import nl.rhaydus.softcover.feature.widget.settings.trending.TrendingSettingsScreenModel
import nl.rhaydus.softcover.feature.widget.settings.trending.TrendingThemeCollector
import nl.rhaydus.softcover.feature.widget.trending.TrendingStore
import nl.rhaydus.softcover.feature.widget.trending.TrendingWidget
import nl.rhaydus.softcover.feature.widget.trending.TrendingWidgetDependencies
import nl.rhaydus.softcover.feature.widget.worker.OwnedWidgets
import nl.rhaydus.softcover.feature.widget.worker.TrendingRefreshScheduler
import nl.rhaydus.softcover.feature.widget.worker.WidgetMaintenanceScheduler
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
                ThemeCollector(),
                ListsCollector(),
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
}
