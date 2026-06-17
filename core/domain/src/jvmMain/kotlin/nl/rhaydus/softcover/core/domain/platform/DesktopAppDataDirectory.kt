package nl.rhaydus.softcover.core.domain.platform

import java.io.File

/**
 * The Softcover application-data directory for the desktop (JVM) target, created if absent. The
 * desktop analogue of Android's `Context.filesDir` and iOS's `NSDocumentDirectory` — the single
 * platform-bound location the database, the DataStores, and the edition-image cache resolve their
 * paths under.
 *
 * Follows each OS's convention: macOS `~/Library/Application Support/Softcover`, Windows
 * `%APPDATA%\Softcover` (falling back to `~\AppData\Roaming`), and the XDG base directory elsewhere
 * (`$XDG_DATA_HOME`, default `~/.local/share`) `/Softcover`.
 */
fun desktopAppDataDirectory(): String {
    val home = System.getProperty("user.home")
    val osName = System.getProperty("os.name").lowercase()

    val base = when {
        osName.contains("mac") -> "$home/Library/Application Support"
        osName.contains("win") -> System.getenv("APPDATA") ?: "$home\\AppData\\Roaming"
        else -> System.getenv("XDG_DATA_HOME")?.takeIf { it.isNotBlank() } ?: "$home/.local/share"
    }

    val dir = File(
        base,
        "Softcover",
    )
    dir.mkdirs()

    return dir.absolutePath
}
