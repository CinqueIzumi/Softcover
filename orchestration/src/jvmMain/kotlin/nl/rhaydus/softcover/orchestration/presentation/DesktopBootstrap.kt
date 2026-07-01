package nl.rhaydus.softcover.orchestration.presentation

import coil3.ImageLoader
import org.koin.core.Koin
import nl.rhaydus.softcover.orchestration.di.initKoinDesktop

/**
 * Opaque handle to the desktop app's process-lifetime resources, produced by [bootstrapDesktop] and
 * consumed by [shutdownDesktop]. Threading this from bootstrap to shutdown keeps Koin and Coil off
 * the thin `:desktopApp` shell's classpath — `main` names only this orchestration type.
 */
class DesktopBootstrap internal constructor(
    internal val koin: Koin,
    internal val imageLoader: ImageLoader,
)

/**
 * Brings up the desktop app's process-lifetime resources before the first composition: installs the
 * Coil image loader and starts Koin. Returns a [DesktopBootstrap] handle so `main` can hand it to
 * [shutdownDesktop] on exit. The desktop counterpart of Android's `SoftCoverApp` setup.
 */
fun bootstrapDesktop(): DesktopBootstrap {
    val imageLoader = installDesktopImageLoader()
    val koin = initKoinDesktop()

    return DesktopBootstrap(
        koin = koin,
        imageLoader = imageLoader,
    )
}
