package nl.rhaydus.softcover.orchestration.di

import org.koin.core.Koin

/**
 * Desktop Koin bootstrap, called once from `bootstrapDesktop` before the first composition.
 * Registers the shared [softcoverModules] plus the desktop-only app bindings ([desktopAppModule]) —
 * the desktop counterpart of the Android `SoftCoverApp` `initKoin { … }` call and the iOS
 * [initKoinIos]. Returns the started [Koin] so the shutdown teardown can drive it.
 */
internal fun initKoinDesktop(): Koin {
    val koin = initKoin {
        modules(desktopAppModule)
    }.koin

    startAppServices(koin)

    return koin
}
