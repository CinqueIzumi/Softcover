package nl.rhaydus.softcover.showcase.di

import nl.rhaydus.softcover.showcase.appearance.collector.ThemeChipsCollector
import nl.rhaydus.softcover.showcase.live.lists.screenmodel.ListsLiveScreenModel
import nl.rhaydus.softcover.showcase.live.progress.screenmodel.ProgressLiveScreenModel
import nl.rhaydus.softcover.showcase.live.quote.screenmodel.QuoteLiveScreenModel
import nl.rhaydus.softcover.showcase.live.share.screenmodel.ShareLiveScreenModel
import nl.rhaydus.softcover.showcase.live.sheet.screenmodel.SheetLiveScreenModel
import nl.rhaydus.softcover.showcase.live.verdict.screenmodel.VerdictLiveScreenModel
import org.koin.dsl.module

internal val liveSheetsModule = module {
    factory {
        SheetLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        ProgressLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        VerdictLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        QuoteLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        ListsLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        ShareLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }
}
