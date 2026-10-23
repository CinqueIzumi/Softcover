package nl.rhaydus.softcover.showcase.di

import nl.rhaydus.softcover.showcase.appearance.collector.ThemeChipsCollector
import nl.rhaydus.softcover.showcase.live.chip.screenmodel.ChipLiveScreenModel
import nl.rhaydus.softcover.showcase.live.chrome.screenmodel.ChromeLiveScreenModel
import nl.rhaydus.softcover.showcase.live.control.screenmodel.ControlLiveScreenModel
import nl.rhaydus.softcover.showcase.live.field.screenmodel.FieldLiveScreenModel
import nl.rhaydus.softcover.showcase.live.selection.screenmodel.SelectionLiveScreenModel
import nl.rhaydus.softcover.showcase.live.sidebar.screenmodel.SidebarLiveScreenModel
import nl.rhaydus.softcover.showcase.live.topbar.screenmodel.TopbarLiveScreenModel
import org.koin.dsl.module

internal val liveInPageModule = module {
    factory {
        TopbarLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        SidebarLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        ChipLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        ControlLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        FieldLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        SelectionLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory {
        ChromeLiveScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }
}
