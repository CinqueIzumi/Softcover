package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * One palette's error colours, expressed as the five tonal steps a Material scheme needs. It is
 * deliberately not [AccentFamily]: red cannot hold its chroma at tone 80/90, so the dark `error`
 * sits at tone 70 and the container at tone 86.
 *
 * - [tone20] — dark `onError`.
 * - [tone30] — dark `errorContainer` and light `onErrorContainer`.
 * - [tone40] — light `error`.
 * - [tone70] — dark `error`.
 * - [tone86] — light `errorContainer` and dark `onErrorContainer`.
 *
 * Light `onError` is white, so it isn't a step here. Every pair must clear 4.5:1, and each family
 * is chosen to stay distinguishable from its own palette's accents.
 */
internal data class ErrorFamily(
    val tone20: Color,
    val tone30: Color,
    val tone40: Color,
    val tone70: Color,
    val tone86: Color,
)
