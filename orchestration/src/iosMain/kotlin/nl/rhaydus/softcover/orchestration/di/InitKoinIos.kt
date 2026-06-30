package nl.rhaydus.softcover.orchestration.di

/**
 * iOS Koin bootstrap, called once from Swift (`InitKoinIosKt.doInitKoinIos()`) before the first
 * `MainViewController()`. Registers the shared [softcoverModules] plus the iOS-only app bindings
 * ([iosAppModule]) — the iOS counterpart of the Android `SoftCoverApp` `initKoin { … }` call.
 */
fun initKoinIos() {
    val koin = initKoin {
        modules(iosAppModule)
    }.koin

    startAppServices(koin)
}
