package nl.rhaydus.softcover.showcase.appearance.screenmodel

import nl.rhaydus.softcover.showcase.store.ShowcaseThemeStore

/** The dependencies the shared appearance actions need from a screen. */
internal interface AppearanceDependencies {
    val themeStore: ShowcaseThemeStore
}
