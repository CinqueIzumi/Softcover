package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import org.koin.compose.koinInject
import nl.rhaydus.softcover.feature.app_update.domain.usecase.CheckForAppUpdateUseCase

/**
 * Installs the singleton Coil [ImageLoader] with a network fetcher (the default loader ships none) so
 * cover images load. Called once from [bootstrapDesktop] before the first composition — the
 * non-composable counterpart of what Android's `SoftCoverApp.newImageLoader` and the iOS
 * `MainViewController` do. On desktop the fetcher rides OkHttp, the same engine Android uses.
 * [SingletonImageLoader.setSafe] only installs if none is set yet, so repeated calls are harmless.
 * Returns the built loader so [shutdownDesktop] can shut it down (releasing its OkHttp engine) as
 * part of the orderly shutdown teardown.
 */
internal fun installDesktopImageLoader(): ImageLoader {
    val imageLoader = ImageLoader.Builder(PlatformContext.INSTANCE)
        .components { add(OkHttpNetworkFetcherFactory()) }
        .build()

    SingletonImageLoader.setSafe { imageLoader }

    return imageLoader
}

/**
 * Desktop entry composable hosted by the `:desktopApp` window — the desktop counterpart of the iOS
 * `MainViewController`'s `ComposeUIViewController { App() }`. Koin must already be started via
 * [nl.rhaydus.softcover.orchestration.di.initKoinDesktop], and the image loader installed via
 * [installDesktopImageLoader], before the first composition.
 */
@Composable
fun DesktopApp() {
    val checkForAppUpdateUseCase = koinInject<CheckForAppUpdateUseCase>()

    // The desktop counterpart of Android's MainActivity.onResume() check — desktop has no Activity
    // lifecycle, so the GitHub-releases update check runs once when the window's content first
    // composes. A newer release surfaces through App()'s existing update snackbar.
    LaunchedEffect(Unit) {
        checkForAppUpdateUseCase()
    }

    App()
}
