package nl.rhaydus.softcover.core.domain.model

/**
 * Which curated look the reader has chosen to paint the app in — the "spine colour", as a *persisted
 * preference*. This enum is the stored identifier and nothing more: the palette's hex table, its
 * user-facing name and its one-line gloss belong to the design system's own `SpinePalette` token,
 * which `:core:uibinding`'s `toSpinePalette()` maps this onto.
 *
 * [SOFTCOVER] is the house look and the default. The four alternates are named after the marks a
 * book carries: the ink it was printed with, the paper it was printed on, the browning it picks up,
 * the water it survived. Declared in picker order, the house palette first, since the Appearance
 * screen renders one preview tile per entry.
 *
 * A new palette joins this enum, gains a `SpinePalette` entry with its copy, and gains a hex table
 * in the design system's `Color.kt`. The mapper's exhaustive `when` makes skipping the second step a
 * compile error. The dynamic-colour preference stays a separate opt-in, because it takes the whole
 * scheme from the wallpaper rather than being a sixth curated look.
 */
enum class ColorPalette {
    SOFTCOVER,
    VELLUM,
    INK,
    FOXED,
    SEA,
    ;

    companion object {
        val DEFAULT: ColorPalette = SOFTCOVER
    }
}
