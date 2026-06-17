package nl.rhaydus.softcover.core.domain.model

/**
 * How a desktop window is laid out on screen, decoupled from Compose's `androidx.compose.ui.window`
 * `WindowPlacement` (which is JVM-only). The desktop persistence layer reads/writes this; the mapping
 * to the Compose enum lives in `jvmMain`. Mobile targets never use it.
 */
enum class WindowPlacement {
    FLOATING,
    MAXIMIZED,
    FULLSCREEN,
}
