package nl.rhaydus.softcover.core.domain.model

/**
 * Which curated look the app is painted in — the "spine colour". A palette is a **whole look, not an
 * accent swap**: it carries its own paper (page, cards, hairlines) as well as its own ink (the
 * primary / secondary / tertiary families), so choosing one retints every surface rather than only
 * the accents. The error family, the scrim, and the app's fixed printed inks are the only colours
 * that stay put.
 *
 * [SOFTCOVER] is the house look and the default. The four alternates are named after the marks a
 * book carries: the ink it was printed with, the paper it was printed on, the browning it picks up,
 * the water it survived. Declared in picker order, the house palette first, since the Appearance
 * screen renders one preview tile per entry.
 *
 * A new palette joins this enum (and gains a hex table in the design system's `Color.kt`); the
 * dynamic-colour preference stays a separate opt-in, because it takes the whole scheme from the
 * wallpaper rather than being a sixth curated look.
 */
enum class ColorPalette(
    val label: String,
    val gloss: String,
) {
    SOFTCOVER(
        label = "Softcover",
        gloss = "The house look — warm clay and old gold, on warm white.",
    ),
    VELLUM(
        label = "Vellum",
        gloss = "Aged paper — a golden ink with a sage note, on cream.",
    ),
    INK(
        label = "Ink",
        gloss = "Printer's slate blue and an oxblood second pass, on a cool page.",
    ),
    FOXED(
        label = "Foxed",
        gloss = "The walnut brown of a browning page, on tanned paper.",
    ),
    SEA(
        label = "Sea",
        gloss = "Deep sea teal and warm sand, on a page washed pale blue.",
    ),
    ;

    companion object {
        val DEFAULT: ColorPalette = SOFTCOVER
    }
}
