package nl.rhaydus.softcover.core.domain.model

/**
 * Persisted desktop window geometry, restored on the next launch. Dimensions are in dp; [x] and [y]
 * are null until the window has been positioned (null ⇒ center on first launch). [placement] records
 * whether the window was floating, maximized, or fullscreen. Desktop-only — mobile targets never read
 * or write this preference.
 */
data class DesktopWindowState(
    val width: Int = DEFAULT_WIDTH,
    val height: Int = DEFAULT_HEIGHT,
    val x: Int? = null,
    val y: Int? = null,
    val placement: WindowPlacement = WindowPlacement.FLOATING,
) {
    companion object {
        const val DEFAULT_WIDTH = 1280
        const val DEFAULT_HEIGHT = 800
    }
}
