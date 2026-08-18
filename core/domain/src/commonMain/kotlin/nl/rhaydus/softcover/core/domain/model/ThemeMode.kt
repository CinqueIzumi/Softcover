package nl.rhaydus.softcover.core.domain.model

/**
 * Which colour scheme the app paints in. [SYSTEM] defers to the device's own light/dark setting; the
 * other entries override it. Declared in picker order (Light, Dark, then the deferring option last),
 * since the Appearance screen renders one preview tile per entry.
 *
 * A future warm low-contrast "Dim" variant joins this enum rather than becoming a second, parallel
 * preference — the app has one answer to "which scheme", not a mode plus a set of modifiers.
 */
enum class ThemeMode(val label: String) {
    LIGHT(label = "Light"),
    DARK(label = "Dark"),
    SYSTEM(label = "System"),
    ;

    companion object {
        val DEFAULT: ThemeMode = SYSTEM
    }
}
