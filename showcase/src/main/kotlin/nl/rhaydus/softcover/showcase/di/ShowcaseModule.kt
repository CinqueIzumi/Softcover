package nl.rhaydus.softcover.showcase.di

import kotlinx.coroutines.Dispatchers
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.showcase.appearance.collector.ThemeChipsCollector
import nl.rhaydus.softcover.showcase.family.screenmodel.FamilyScreenModel
import nl.rhaydus.softcover.showcase.home.screenmodel.HomeScreenModel
import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val showcaseModule = module {
    single {
        AppDispatchers(
            main = Dispatchers.Main.immediate,
            io = Dispatchers.IO,
            default = Dispatchers.Default,
        )
    }

    singleOf(::ShowcaseThemeStore)

    factory {
        HomeScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
        )
    }

    factory { (family: GalleryFamily) ->
        FamilyScreenModel(
            appDispatchers = get(),
            themeStore = get(),
            initializers = listOf(ThemeChipsCollector(themeStore = get())),
            family = family,
        )
    }
}
