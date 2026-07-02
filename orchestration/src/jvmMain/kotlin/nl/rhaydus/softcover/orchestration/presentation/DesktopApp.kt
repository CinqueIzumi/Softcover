package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetUiScaleAsFlowUseCase
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
    val getUiScaleAsFlowUseCase = koinInject<GetUiScaleAsFlowUseCase>()

    // The desktop counterpart of Android's MainActivity.onResume() check — desktop has no Activity
    // lifecycle, so the GitHub-releases update check runs once when the window's content first
    // composes. A newer release surfaces through App()'s existing update snackbar.
    LaunchedEffect(Unit) {
        checkForAppUpdateUseCase()
    }

    // Seed the scale synchronously (a single small local-file read at window open, mirroring
    // rememberPersistedWindowState) so the very first frame renders at the saved scale instead of
    // flashing at 100% and reflowing once DataStore emits — a whole-window pop for a scaled-up user.
    val uiScaleSeed = remember { runBlocking { getUiScaleAsFlowUseCase().first() } }

    val uiScale by getUiScaleAsFlowUseCase().collectAsStateWithLifecycle(initialValue = uiScaleSeed)

    // Scaling density (not fontScale) scales dp and sp uniformly, and rememberWindowSizeClass() reads
    // LocalDensity, so window-size breakpoints reflow with the chosen scale. This is the fix for
    // Compose Desktop inheriting a 1.0 density on Linux, where the app otherwise renders tiny.
    val baseDensity = LocalDensity.current

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = baseDensity.density * uiScale.factor,
            fontScale = baseDensity.fontScale,
        ),
    ) {
        App()
    }
}
