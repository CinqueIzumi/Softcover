package nl.rhaydus.softcover.showcase.di

import nl.rhaydus.softcover.showcase.appearance.collector.ThemeChipsCollector
import nl.rhaydus.softcover.showcase.live.callout.screenmodel.CalloutLiveScreenModel
import nl.rhaydus.softcover.showcase.live.celebration.screenmodel.CelebrationLiveScreenModel
import nl.rhaydus.softcover.showcase.live.dialog.screenmodel.DialogLiveScreenModel
import nl.rhaydus.softcover.showcase.live.snackbar.screenmodel.SnackbarLiveScreenModel
import org.koin.dsl.module

internal val liveOverlaysModule = module {
    factory {
        DialogLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        SnackbarLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        CelebrationLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        CalloutLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }
}
