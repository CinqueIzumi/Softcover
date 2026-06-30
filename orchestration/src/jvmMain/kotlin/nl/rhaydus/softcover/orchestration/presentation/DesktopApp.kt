package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory

/**
 * Installs the singleton Coil [ImageLoader] with a network fetcher (the default loader ships none) so
 * cover images load. Call once at startup from the desktop `main`, before the first composition —
 * the non-composable counterpart of what Android's `SoftCoverApp.newImageLoader` and the iOS
 * `MainViewController` do. On desktop the fetcher rides OkHttp, the same engine Android uses.
 * [SingletonImageLoader.setSafe] only installs if none is set yet, so repeated calls are harmless.
 */
fun installDesktopImageLoader() {
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .build()
    }
}

/**
 * Desktop entry composable hosted by the `:desktopApp` window — the desktop counterpart of the iOS
 * `MainViewController`'s `ComposeUIViewController { App() }`. Koin must already be started via
 * [nl.rhaydus.softcover.orchestration.di.initKoinDesktop], and the image loader installed via
 * [installDesktopImageLoader], before the first composition.
 */
@Composable
fun DesktopApp() {
    App()
}
