package nl.rhaydus.softcover.orchestration.di

/**
 * Desktop Koin bootstrap, called once from the desktop `main` before the first composition.
 * Registers the shared [softcoverModules] plus the desktop-only app bindings ([desktopAppModule]) —
 * the desktop counterpart of the Android `SoftCoverApp` `initKoin { … }` call and the iOS
 * [initKoinIos].
 */
fun initKoinDesktop() {
    val koin = initKoin {
        modules(desktopAppModule)
    }.koin

    startAppServices(koin)
}
