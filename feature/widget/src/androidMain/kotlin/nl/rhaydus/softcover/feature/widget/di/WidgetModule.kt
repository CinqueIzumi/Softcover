package nl.rhaydus.softcover.feature.widget.di

import nl.rhaydus.softcover.feature.widget.currentlyreading.CurrentlyReadingWidget
import nl.rhaydus.softcover.feature.widget.currentlyreading.CurrentlyReadingWidgetDependencies
import nl.rhaydus.softcover.feature.widget.refresh.WidgetRefresher
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

    single {
        WidgetRefresher(
            context = androidContext(),
            applicationScope = get(),
            widgets = listOf(CurrentlyReadingWidget()),
            getThemeConfiguration = get(),
            getUserIdAsFlow = get(),
            getCurrentlyReadingUserBooks = get(),
            getWantToReadUserBooks = get(),
            observeTrendingBooks = get(),
        )
    }
}
