package nl.rhaydus.softcover.core.domain.model

/**
 * Which colour scheme the app paints in. [SYSTEM] defers to the device's own light/dark setting; the
 * other entries override it. [DIM] is a standalone warm, low-contrast scheme with its own colours,
 * so it ignores the spine colour and dynamic colour. Declared in picker order (Light, Dark, Dim, then
 * the deferring option last), since the Appearance screen renders one preview tile per entry.
 */
enum class ThemeMode(val label: String) {
    LIGHT(label = "Light"),
    DARK(label = "Dark"),
    DIM(label = "Dim"),
    SYSTEM(label = "System"),
    ;

    companion object {
        val DEFAULT: ThemeMode = SYSTEM
    }
}
