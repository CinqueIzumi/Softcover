package nl.rhaydus.softcover.core.designsystem.presentation.theme

/**
 * The three looks [SoftcoverTheme] can paint. [DIM] is a warm, low-light cousin of [DARK]: it is one
 * standalone scheme, so it ignores the spine-colour palette and dynamic colour.
 */
enum class SchemeBrightness {
    LIGHT,
    DARK,
    DIM,
    ;

    /** Whether the look paints dark surfaces, which [DARK] and [DIM] both do. */
    val isDark: Boolean
        get() = this != LIGHT
}
