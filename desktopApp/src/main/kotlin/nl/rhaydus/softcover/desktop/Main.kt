package nl.rhaydus.softcover.desktop

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.orchestration.di.initKoinDesktop
import nl.rhaydus.softcover.orchestration.presentation.DesktopApp
import nl.rhaydus.softcover.orchestration.presentation.installDesktopImageLoader
import nl.rhaydus.softcover.orchestration.presentation.rememberPersistedWindowState
import java.awt.Dimension

// Kept below the 600dp COMPACT breakpoint so dragging the window narrow still falls back gracefully
// to the phone-shaped shell rather than clamping at the wide layout's minimum.
private val MINIMUM_WINDOW_SIZE = Dimension(
    480,
    640,
)

/**
 * Desktop entry point. Installs the logging facade, the Coil image loader, and starts Koin (mirroring
 * Android's `SoftCoverApp` / the iOS `initKoinIos` + `MainViewController`), then opens a single window
 * hosting the shared [DesktopApp] composable. The window's size, position, and placement are restored
 * from and persisted to preferences via [rememberPersistedWindowState].
 */
fun main() {
    // Name the app for the windowing system BEFORE any AWT/Swing toolkit init. On Linux the taskbar
    // WM_CLASS (and on macOS the menu-bar name) otherwise falls back to the main thread's class name,
    // which surfaces to users as "java-lang-Thread".
    System.setProperty(
        "apple.awt.application.name",
        "Softcover",
    )
    System.setProperty(
        "awt.application.name",
        "Softcover",
    )

    AppLog.install(debug = true)
    installDesktopImageLoader()
    initKoinDesktop()

    application {
        val windowState = rememberPersistedWindowState()

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Softcover",
            icon = painterResource("softcover.png"),
        ) {
            LaunchedEffect(Unit) {
                window.minimumSize = MINIMUM_WINDOW_SIZE
            }

            DesktopApp()
        }
    }
}
