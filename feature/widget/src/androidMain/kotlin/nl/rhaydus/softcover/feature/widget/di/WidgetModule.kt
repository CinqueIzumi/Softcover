package nl.rhaydus.softcover.feature.widget.di

import nl.rhaydus.softcover.feature.widget.SoftcoverWidget
import nl.rhaydus.softcover.feature.widget.SoftcoverWidgetDependencies
import nl.rhaydus.softcover.feature.widget.refresh.WidgetRefresher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val widgetModule = module {
    factory { SoftcoverWidgetDependencies(getThemeConfiguration = get()) }

    single {
        WidgetRefresher(
            context = androidContext(),
            applicationScope = get(),
            widgets = listOf(SoftcoverWidget()),
            getThemeConfiguration = get(),
            getUserIdAsFlow = get(),
            getCurrentlyReadingUserBooks = get(),
            getWantToReadUserBooks = get(),
            observeTrendingBooks = get(),
        )
    }
}
