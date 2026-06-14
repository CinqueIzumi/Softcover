package nl.rhaydus.softcover.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.orchestration.di.initKoinDesktop
import nl.rhaydus.softcover.orchestration.presentation.DesktopApp
import nl.rhaydus.softcover.orchestration.presentation.installDesktopImageLoader

/**
 * Desktop entry point. Installs the logging facade, the Coil image loader, and starts Koin (mirroring
 * Android's `SoftCoverApp` / the iOS `initKoinIos` + `MainViewController`), then opens a single window
 * hosting the shared [DesktopApp] composable.
 */
fun main() {
    AppLog.install(debug = true)
    installDesktopImageLoader()
    initKoinDesktop()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Softcover",
        ) {
            DesktopApp()
        }
    }
}
