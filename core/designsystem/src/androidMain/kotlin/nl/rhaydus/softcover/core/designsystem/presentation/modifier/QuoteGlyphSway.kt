package nl.rhaydus.softcover.core.designsystem.presentation.modifier

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import nl.rhaydus.softcover.core.designsystem.presentation.util.playDecorativeMotion

private const val SWAY_DEGREES = 2f
private const val SWAY_HALF_PERIOD_MS = 6_800

/**
 * Slow infinite ±2° drift for a decorative quote glyph in an empty state. The glyph rocks
 * imperceptibly slowly — a magazine pull-quote breathing, not a marquee. Suppressed under
 * reduced motion: the glyph sits perfectly upright as it does today.
 *
 * Apply only to decorative quote glyphs in empty states (Library empty tabs, Reading empty
 * currently-reading). Do not apply to quote glyphs that decorate populated cards (review
 * cards, share cards) — those carry content next to the glyph and a swaying mark fights
 * the body for the reader's eye.
 */
@Composable
fun Modifier.quoteGlyphSway(): Modifier {
    val playMotion = playDecorativeMotion()

    if (playMotion.not()) return this

    val transition = rememberInfiniteTransition(label = "QuoteGlyphSway")

    val rotation by transition.animateFloat(
        initialValue = -SWAY_DEGREES,
        targetValue = SWAY_DEGREES,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = SWAY_HALF_PERIOD_MS,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "QuoteGlyphSwayRotation",
    )

    return this.graphicsLayer { rotationZ = rotation }
}
