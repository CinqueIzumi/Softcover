package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * A palette's **paper**: the neutral ramp every page, card, and sheet surface is cut from, as the
 * sixteen tonal steps the scheme needs. Light mode reads the top of the ramp and dark mode the
 * bottom, which is why one family serves both — a palette cannot end up with a cream page and a
 * blue-black one.
 *
 * The steps, by the slot they land in:
 *
 * - [tone4] — `surfaceContainerLowest` (dark).
 * - [tone6] — `background` / `surface` / `surfaceDim` (dark).
 * - [tone10] — `onSurface` / `onBackground` (light), `surfaceContainerLow` (dark).
 * - [tone12] — `surfaceContainer` (dark).
 * - [tone17] — `surfaceContainerHigh` (dark).
 * - [tone20] — `inverseSurface` (light), `inverseOnSurface` (dark).
 * - [tone22] — `surfaceContainerHighest` (dark).
 * - [tone24] — `surfaceBright` (dark).
 * - [tone87] — `surfaceDim` (light).
 * - [tone90] — `surfaceContainerHighest` (light), and the ink of dark mode (`onSurface`,
 *   `onBackground`, `inverseSurface`).
 * - [tone92] — `surfaceContainerHigh` (light).
 * - [tone94] — `surfaceContainer` (light).
 * - [tone95] — `inverseOnSurface` (light).
 * - [tone96] — `surfaceContainerLow` (light).
 * - [tone98] — `background` / `surface` / `surfaceBright` (light).
 * - [tone100] — `surfaceContainerLowest` (light); white in every palette.
 *
 * Every palette's ramp is the house ramp **re-tinted at identical lightness** (each step keeps its
 * OkLab L and takes the palette's neutral hue at a scaled chroma), which is what lets a palette
 * repaint the whole page without re-checking a single contrast pair — see the ramp's provenance note
 * in `docs/reference/design-system/foundations.md` §2.1.
 */
internal data class NeutralFamily(
    val tone4: Color,
    val tone6: Color,
    val tone10: Color,
    val tone12: Color,
    val tone17: Color,
    val tone20: Color,
    val tone22: Color,
    val tone24: Color,
    val tone87: Color,
    val tone90: Color,
    val tone92: Color,
    val tone94: Color,
    val tone95: Color,
    val tone96: Color,
    val tone98: Color,
    val tone100: Color,
)
