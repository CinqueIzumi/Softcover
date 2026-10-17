package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * The palette's **hairlines and demoted ink** — the neutral-variant ramp, a slightly more chromatic
 * neighbour of [NeutralFamily] that carries the roles which must sit *just* off the paper:
 *
 * - [tone30] — `onSurfaceVariant` (light), `surfaceVariant` + `outlineVariant` (dark).
 * - [tone50] — `outline` (light).
 * - [tone60] — `outline` (dark).
 * - [tone80] — `outlineVariant` (light), `onSurfaceVariant` (dark).
 * - [tone90] — `surfaceVariant` (light).
 *
 * Re-tinted the same way as [NeutralFamily], at identical lightness per step.
 */
internal data class NeutralVariantFamily(
    val tone30: Color,
    val tone50: Color,
    val tone60: Color,
    val tone80: Color,
    val tone90: Color,
)
