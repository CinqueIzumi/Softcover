package nl.rhaydus.softcover.core.designsystem.presentation.theme

import nl.rhaydus.softcover.core.domain.model.ColorPalette

/**
 * Everything one [ColorPalette] is made of: three accent families and the paper they are printed on.
 * A palette is a *whole look*, not an accent swap — the page, the cards, the hairlines, and the ink
 * all move with it — which is why the neutral ramps live here beside the accents rather than as one
 * shared set of surface hexes.
 *
 * The only colours a palette does **not** own are the ones that must not move: the error family, the
 * black scrim, and the fixed printed inks (`RatingGold`, the mood inks, `MonogramCoverInk`,
 * `ReadingHeroBackdropForeground`).
 */
internal data class PaletteColors(
    val primary: AccentFamily,
    val secondary: AccentFamily,
    val tertiary: AccentFamily,
    val neutral: NeutralFamily,
    val neutralVariant: NeutralVariantFamily,
)

/** The hex table this palette is painted from — see `Color.kt`, where all brand hexes live. */
internal val ColorPalette.colors: PaletteColors
    get() = when (this) {
        ColorPalette.SOFTCOVER -> softcoverPalette
        ColorPalette.VELLUM -> vellumPalette
        ColorPalette.INK -> inkPalette
        ColorPalette.FOXED -> foxedPalette
        ColorPalette.SEA -> seaPalette
    }
