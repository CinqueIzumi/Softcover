package nl.rhaydus.softcover.core.designsystem.presentation.theme

/**
 * Which curated look the app is painted in — the "spine colour". A palette is a **whole look, not an
 * accent swap**: it carries its own paper (page, cards, hairlines) as well as its own ink (the
 * primary / secondary / tertiary families), so choosing one retints every surface rather than only
 * the accents. The error family, the scrim, and the app's fixed printed inks are the only colours
 * that stay put.
 *
 * This is the design system's own palette **token** — the thing a hex table is keyed by and a
 * component selects a scheme with. It deliberately does not know how the reader's choice is stored:
 * `:core:domain`'s `ColorPalette` is the persisted preference, and `:core:uibinding` maps one onto
 * the other. That split is what lets this module hold zero project dependencies, and it is why the
 * palette's user-facing [label] and [gloss] live here, beside its hexes, rather than in the domain
 * layer.
 *
 * [SOFTCOVER] is the house look and the default. The four alternates are named after the marks a
 * book carries: the ink it was printed with, the paper it was printed on, the browning it picks up,
 * the water it survived. Declared in picker order, the house palette first, since the Appearance
 * screen renders one preview tile per entry.
 *
 * **Add a palette starting from the domain preference, not from here.** A new `ColorPalette` entry
 * fails to compile until the mapper gains a branch, which in turn needs an entry here — so that
 * direction is guarded. The reverse is not: the mapper's `when` is exhaustive over `ColorPalette`,
 * its receiver, so an entry added *here* first compiles happily and becomes a look nothing can ever
 * select. `SpinePaletteMapperTest` is what catches that, since no `when` can.
 *
 * The dynamic-colour preference stays a separate opt-in, because it takes the whole scheme from the
 * wallpaper rather than being a sixth curated look.
 */
enum class SpinePalette(
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
        val DEFAULT: SpinePalette = SOFTCOVER
    }
}
