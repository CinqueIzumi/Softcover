package nl.rhaydus.softcover.feature.app_update.data.install

import java.awt.Desktop
import java.io.File

/**
 * Hands a downloaded installer to the host OS. This is assisted, not silent: the user clicks through
 * the native installer once (drag-to-Applications on macOS, the MSI wizard on Windows, the system
 * package installer on Linux). The caller exits the app afterwards so the installer can replace the
 * running binary — on Windows in particular the executable is otherwise file-locked.
 */
internal class DesktopInstallerLauncher {
    /** Launches [installer] for the host OS. Returns `true` when the OS handoff succeeded. */
    fun launch(installer: File): Boolean {
        val target = DesktopInstallerTarget.current() ?: return false

        return runCatching {
            when (target) {
                DesktopInstallerTarget.MACOS -> Desktop.getDesktop().open(installer)

                DesktopInstallerTarget.WINDOWS -> ProcessBuilder(
                    "msiexec",
                    "/i",
                    installer.absolutePath,
                ).start()

                DesktopInstallerTarget.LINUX -> ProcessBuilder(
                    "xdg-open",
                    installer.absolutePath,
                ).start()
            }
        }.isSuccess
    }
}
