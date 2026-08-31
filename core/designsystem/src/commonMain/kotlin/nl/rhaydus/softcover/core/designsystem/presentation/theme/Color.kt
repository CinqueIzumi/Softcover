package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/**
 * The house palette — the warm clay, old gold, and warm-white paper Softcover has always worn, and
 * the default look ([SpinePalette.DEFAULT]). Its five accent tones per family
 * are the exact hexes the flat `primaryLight` / `primaryDark` / … pairs used to spell out one role at
 * a time, and its neutral ramp is the exact paper the app shipped with, so nothing about the default
 * look changed when palettes arrived.
 */
internal val softcoverPalette = PaletteColors(
    primary = AccentFamily(
        tone20 = Color(0xFF561F0F),
        tone30 = Color(0xFF723523),
        tone40 = Color(0xFF8F4C38),
        tone80 = Color(0xFFFFB5A0),
        tone90 = Color(0xFFFFDBD1),
    ),
    secondary = AccentFamily(
        tone20 = Color(0xFF442A22),
        tone30 = Color(0xFF5D4037),
        tone40 = Color(0xFF77574E),
        tone80 = Color(0xFFE7BDB2),
        tone90 = Color(0xFFFFDBD1),
    ),
    tertiary = AccentFamily(
        tone20 = Color(0xFF3B2F05),
        tone30 = Color(0xFF534619),
        tone40 = Color(0xFF6C5D2F),
        tone80 = Color(0xFFD8C58D),
        tone90 = Color(0xFFF5E1A7),
    ),
    neutral = NeutralFamily(
        tone4 = Color(0xFF140C0A),
        tone6 = Color(0xFF1A110F),
        tone10 = Color(0xFF231917),
        tone12 = Color(0xFF271D1B),
        tone17 = Color(0xFF322825),
        tone20 = Color(0xFF392E2B),
        tone22 = Color(0xFF3D322F),
        tone24 = Color(0xFF423734),
        tone87 = Color(0xFFE8D6D2),
        tone90 = Color(0xFFF1DFDA),
        tone92 = Color(0xFFF7E4E0),
        tone94 = Color(0xFFFCEAE5),
        tone95 = Color(0xFFFFEDE8),
        tone96 = Color(0xFFFFF1ED),
        tone98 = Color(0xFFFFF8F6),
        tone100 = Color(0xFFFFFFFF),
    ),
    neutralVariant = NeutralVariantFamily(
        tone30 = Color(0xFF53433F),
        tone50 = Color(0xFF85736E),
        tone60 = Color(0xFFA08C87),
        tone80 = Color(0xFFD8C2BC),
        tone90 = Color(0xFFF5DED8),
    ),
)

/**
 * Aged paper: the house gold promoted to the lead, a sage green as its second note, and a cream page
 * warm enough to read as parchment rather than as white.
 */
internal val vellumPalette = PaletteColors(
    primary = AccentFamily(
        tone20 = Color(0xFF402D00),
        tone30 = Color(0xFF5B4300),
        tone40 = Color(0xFF785A05),
        tone80 = Color(0xFFE9C25C),
        tone90 = Color(0xFFFFDF9E),
    ),
    secondary = AccentFamily(
        tone20 = Color(0xFF35331F),
        tone30 = Color(0xFF4C4934),
        tone40 = Color(0xFF64614A),
        tone80 = Color(0xFFCFCBB0),
        tone90 = Color(0xFFECE7CB),
    ),
    tertiary = AccentFamily(
        tone20 = Color(0xFF223616),
        tone30 = Color(0xFF384D2B),
        tone40 = Color(0xFF4F6442),
        tone80 = Color(0xFFB4D0A3),
        tone90 = Color(0xFFCFECBD),
    ),
    neutral = NeutralFamily(
        tone4 = Color(0xFF140D01),
        tone6 = Color(0xFF1A1202),
        tone10 = Color(0xFF231B07),
        tone12 = Color(0xFF261F0B),
        tone17 = Color(0xFF322A17),
        tone20 = Color(0xFF38301B),
        tone22 = Color(0xFF3C341F),
        tone24 = Color(0xFF413925),
        tone87 = Color(0xFFE7DABB),
        tone90 = Color(0xFFF0E3C3),
        tone92 = Color(0xFFF6E8C7),
        tone94 = Color(0xFFFBEECE),
        tone95 = Color(0xFFFEF1D1),
        tone96 = Color(0xFFFEF4DB),
        tone98 = Color(0xFFFEF9ED),
        tone100 = Color(0xFFFFFFFF),
    ),
    neutralVariant = NeutralVariantFamily(
        tone30 = Color(0xFF524627),
        tone50 = Color(0xFF847655),
        tone60 = Color(0xFF9F906C),
        tone80 = Color(0xFFD7C69F),
        tone90 = Color(0xFFF3E2BA),
    ),
)

/**
 * Printer's ink: a slate blue lead with the letterpress second pass's oxblood beside it, over a cool
 * blue-grey paper — the one palette whose page is colder than its ink.
 */
internal val inkPalette = PaletteColors(
    primary = AccentFamily(
        tone20 = Color(0xFF141F3A),
        tone30 = Color(0xFF283552),
        tone40 = Color(0xFF3F4C6B),
        tone80 = Color(0xFFB5C4E8),
        tone90 = Color(0xFFD8E1FA),
    ),
    secondary = AccentFamily(
        tone20 = Color(0xFF272E3A),
        tone30 = Color(0xFF3E4552),
        tone40 = Color(0xFF565D6B),
        tone80 = Color(0xFFC0C6D5),
        tone90 = Color(0xFFDCE2F1),
    ),
    tertiary = AccentFamily(
        tone20 = Color(0xFF45141C),
        tone30 = Color(0xFF5F2830),
        tone40 = Color(0xFF7D4048),
        tone80 = Color(0xFFF0B3BB),
        tone90 = Color(0xFFFFD9DD),
    ),
    neutral = NeutralFamily(
        tone4 = Color(0xFF090E19),
        tone6 = Color(0xFF0D1320),
        tone10 = Color(0xFF151C29),
        tone12 = Color(0xFF19202D),
        tone17 = Color(0xFF242B38),
        tone20 = Color(0xFF293140),
        tone22 = Color(0xFF2D3544),
        tone24 = Color(0xFF323A49),
        tone87 = Color(0xFFCFDBF3),
        tone90 = Color(0xFFD8E4FC),
        tone92 = Color(0xFFDEE9FF),
        tone94 = Color(0xFFE6EFFF),
        tone95 = Color(0xFFEBF2FF),
        tone96 = Color(0xFFEFF5FF),
        tone98 = Color(0xFFF7FAFF),
        tone100 = Color(0xFFFFFFFF),
    ),
    neutralVariant = NeutralVariantFamily(
        tone30 = Color(0xFF3C475D),
        tone50 = Color(0xFF6B7890),
        tone60 = Color(0xFF8491AC),
        tone80 = Color(0xFFB9C8E6),
        tone90 = Color(0xFFD7E4FF),
    ),
)

/**
 * Foxing: the walnut brown an old page browns to, over a tanned paper, cut with a dusty blue instead
 * of gold. Deliberately a near neighbour of the house clay, pushed browner and quieter — the pairing
 * is what separates the two, not the lead colour alone.
 */
internal val foxedPalette = PaletteColors(
    primary = AccentFamily(
        tone20 = Color(0xFF3A230D),
        tone30 = Color(0xFF543A22),
        tone40 = Color(0xFF6E5138),
        tone80 = Color(0xFFDFBE9D),
        tone90 = Color(0xFFFCDAB8),
    ),
    secondary = AccentFamily(
        tone20 = Color(0xFF392C22),
        tone30 = Color(0xFF524338),
        tone40 = Color(0xFF6B5B4F),
        tone80 = Color(0xFFDAC3B3),
        tone90 = Color(0xFFF7DFCE),
    ),
    tertiary = AccentFamily(
        tone20 = Color(0xFF1D3045),
        tone30 = Color(0xFF33465D),
        tone40 = Color(0xFF4A5D75),
        tone80 = Color(0xFFB3C8E3),
        tone90 = Color(0xFFD0E4FF),
    ),
    neutral = NeutralFamily(
        tone4 = Color(0xFF180B02),
        tone6 = Color(0xFF1E1003),
        tone10 = Color(0xFF281809),
        tone12 = Color(0xFF2C1C0D),
        tone17 = Color(0xFF372718),
        tone20 = Color(0xFF3F2D1D),
        tone22 = Color(0xFF433121),
        tone24 = Color(0xFF483626),
        tone87 = Color(0xFFF2D5BC),
        tone90 = Color(0xFFFBDDC5),
        tone92 = Color(0xFFFFE3CC),
        tone94 = Color(0xFFFFEAD9),
        tone95 = Color(0xFFFFEEE0),
        tone96 = Color(0xFFFFF2E7),
        tone98 = Color(0xFFFFF8F3),
        tone100 = Color(0xFFFFFFFF),
    ),
    neutralVariant = NeutralVariantFamily(
        tone30 = Color(0xFF5B412A),
        tone50 = Color(0xFF8F7158),
        tone60 = Color(0xFFAB8A6E),
        tone80 = Color(0xFFE4C0A1),
        tone90 = Color(0xFFFFDDC0),
    ),
)

/**
 * Water: a deep sea teal lead against a warm sand tertiary, over a pale blue-green page — the
 * palette's one cool-led pairing.
 */
internal val seaPalette = PaletteColors(
    primary = AccentFamily(
        tone20 = Color(0xFF003037),
        tone30 = Color(0xFF05474F),
        tone40 = Color(0xFF1F5F68),
        tone80 = Color(0xFF9CD1DA),
        tone90 = Color(0xFFBAEDF7),
    ),
    secondary = AccentFamily(
        tone20 = Color(0xFF1F3336),
        tone30 = Color(0xFF354A4D),
        tone40 = Color(0xFF4C6265),
        tone80 = Color(0xFFB3CBCE),
        tone90 = Color(0xFFCFE8EB),
    ),
    tertiary = AccentFamily(
        tone20 = Color(0xFF422C00),
        tone30 = Color(0xFF61431A),
        tone40 = Color(0xFF7B5A2E),
        tone80 = Color(0xFFE9C08D),
        tone90 = Color(0xFFFFDDB3),
    ),
    neutral = NeutralFamily(
        tone4 = Color(0xFF031115),
        tone6 = Color(0xFF05161B),
        tone10 = Color(0xFF0C1F24),
        tone12 = Color(0xFF102328),
        tone17 = Color(0xFF1B2E33),
        tone20 = Color(0xFF21353A),
        tone22 = Color(0xFF25393E),
        tone24 = Color(0xFF2A3E43),
        tone87 = Color(0xFFC3E1E9),
        tone90 = Color(0xFFCBEAF2),
        tone92 = Color(0xFFD0EFF8),
        tone94 = Color(0xFFD7F5FD),
        tone95 = Color(0xFFDBF7FF),
        tone96 = Color(0xFFE3F9FF),
        tone98 = Color(0xFFF1FCFF),
        tone100 = Color(0xFFFFFFFF),
    ),
    neutralVariant = NeutralVariantFamily(
        tone30 = Color(0xFF2F4C54),
        tone50 = Color(0xFF5E7E86),
        tone60 = Color(0xFF7598A1),
        tone80 = Color(0xFFA9CFD9),
        tone90 = Color(0xFFC5ECF6),
    ),
)

// The colours no palette owns: the error family reads as a warning in every look, and the scrim is
// simply black. Every other scheme slot comes from the chosen palette's tables above.
internal val errorLight = Color(0xFFBA1A1A)
internal val onErrorLight = Color(0xFFFFFFFF)
internal val errorContainerLight = Color(0xFFFFDAD6)
internal val onErrorContainerLight = Color(0xFF93000A)
internal val scrimLight = Color(0xFF000000)

internal val errorDark = Color(0xFFFFB4AB)
internal val onErrorDark = Color(0xFF690005)
internal val errorContainerDark = Color(0xFF93000A)
internal val onErrorContainerDark = Color(0xFFFFDAD6)
internal val scrimDark = Color(0xFF000000)

/**
 * Brand gold reserved for rating stars — both the read-only community/review rating glyphs and
 * the interactive personal rating control. Lives outside the Material scheme because the warm
 * gold reads identically in light and dark and is a deliberate "rating" signal, not a theme role.
 */
val RatingGold = Color(0xFFFBBF23)

/**
 * Spoiler redaction treatment, derived from `onSurfaceVariant` so it tracks light/dark. The two roles
 * are deliberately different: in the review editor the author must keep reading their own words, so a
 * spoiler run gets only a translucent [spoilerEditorHighlight] wash; on display the run is hidden under
 * a near-solid [spoilerCover] block until the reader taps to reveal it.
 */
val ColorScheme.spoilerEditorHighlight: Color
    get() = onSurfaceVariant.copy(alpha = 0.20f)

internal val ColorScheme.spoilerCover: Color
    get() = onSurfaceVariant.copy(alpha = 0.90f)

/**
 * Fixed warm inks for the Explore "Browse by mood" tiles and the unreleased-book monogram cover
 * (explore-3a). Like [RatingGold] these sit outside the Material scheme — the spec states they read
 * identically in both themes, as a deliberate signal (a printed-jacket ink) rather than a theme role.
 * Never re-declare one of these hexes at a call site; two of the four mood tiles stay on ordinary
 * theme roles instead (`surfaceContainerHigh` / `onSurface` / `primary` / `onSurfaceVariant`) because
 * only two of the spec's tiles are ink-tinted — see `docs/reference/design-system.md`.
 */
val MoodInkCosyBackground = Color(0xFF3A3016)
val MoodInkCosyForeground = Color(0xFFF5E1A7)

val MoodInkDreadBackground = Color(0xFF2B1A15)
val MoodInkDreadForeground = Color(0xFFFFEDE8)
val MoodInkDreadEyebrow = Color(0xFFFFDBD1)

val MoodInkHeartWrenchBackground = Color(0xFF3A1E22)
val MoodInkHeartWrenchForeground = Color(0xFFFFD9DE)

/**
 * The fixed dark ink behind the app-wide coverless-cover monogram - used whenever no cover art
 * resolves, independent of release status.
 */
val MonogramCoverInk = Color(0xFF1A110F)
val MonogramCoverForeground = Color(0xFFFFEDE8)

/**
 * The fixed warm-white foreground for text painted directly on the Reading screen's featured-hero
 * backdrop (series eyebrow, hero title, byline, the "Your pace" row) - the backdrop itself is a
 * blurred, dimmed book cover (a theme-role black scrim + a fade into the card's own surface colour,
 * not a fixed-ink field), but the overlaid text still needs a colour that reads reliably against an
 * arbitrary cover image regardless of theme, so it stays fixed rather than switching to `onSurface` /
 * `onSurfaceVariant` - see `docs/reference/design-system.md`.
 */
val ReadingHeroBackdropForeground = Color(0xFFFFEDE8)
