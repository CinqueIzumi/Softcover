package nl.rhaydus.softcover.feature.widget.di

import nl.rhaydus.softcover.feature.widget.currentlyreading.CurrentlyReadingWidget
import nl.rhaydus.softcover.feature.widget.currentlyreading.CurrentlyReadingWidgetDependencies
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickStore
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickWidget
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickWidgetDependencies
import nl.rhaydus.softcover.feature.widget.refresh.WidgetRefresher
import nl.rhaydus.softcover.feature.widget.settings.RandomPickSettingsScreenModel
import nl.rhaydus.softcover.feature.widget.settings.collector.ListsCollector
import nl.rhaydus.softcover.feature.widget.settings.collector.ThemeCollector
import nl.rhaydus.softcover.feature.widget.worker.OwnedWidgets
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
            ),
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
