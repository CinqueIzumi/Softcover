package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * One accent role family (primary, secondary, or tertiary) expressed as the five tonal steps a
 * Material scheme needs from it — the same five the brand scheme has always used, now named rather
 * than repeated per role and per mode:
 *
 * - [tone20] — the on-colour in dark (`onPrimary`, `onSecondary`, `onTertiary`).
 * - [tone30] — the container in dark, the on-container in light.
 * - [tone40] — the accent itself in light, and `inversePrimary` in dark.
 * - [tone80] — the accent itself in dark, and `inversePrimary` in light.
 * - [tone90] — the container in light, the on-container in dark.
 *
 * The light on-colour is white in every family, so it isn't a step here. Each tone must clear 4.5:1
 * against the partner it is painted on ([tone40] under white and on the light page, [tone80] over
 * [tone20] and on the dark page, [tone30]/[tone90] against each other) — the house palette sits
 * around 6:1 and no alternate may fall below that band by much.
 */
internal data class AccentFamily(
    val tone20: Color,
    val tone30: Color,
    val tone40: Color,
    val tone80: Color,
    val tone90: Color,
)
