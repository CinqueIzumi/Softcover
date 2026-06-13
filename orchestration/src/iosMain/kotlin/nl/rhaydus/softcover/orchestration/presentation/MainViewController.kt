package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.ui.window.ComposeUIViewController
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import platform.UIKit.UIViewController

/**
 * iOS entry point. The Xcode app hosts the returned [UIViewController] (via a
 * `UIViewControllerRepresentable`); Swift reaches it as `MainViewControllerKt.MainViewController()`.
 *
 * Mirrors what the Android `SoftCoverApp` does for Coil: registers a singleton [ImageLoader] with a
 * network fetcher. On iOS that fetcher rides Ktor's Darwin engine (Coil's default loader ships no
 * network fetcher, and the OkHttp one is JVM/Android-only), so cover images load. [SingletonImageLoader.setSafe]
 * only installs if none is set yet, so repeated calls are harmless. Koin must already be started via
 * [nl.rhaydus.softcover.orchestration.di.initKoinIos] before the first composition.
 */
fun MainViewController(): UIViewController {
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory(httpClient = { HttpClient(Darwin) })) }
            .build()
    }

    return ComposeUIViewController { App() }
}
