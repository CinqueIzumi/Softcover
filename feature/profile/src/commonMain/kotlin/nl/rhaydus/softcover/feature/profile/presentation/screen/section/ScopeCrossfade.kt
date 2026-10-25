package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import nl.rhaydus.designsystem.motion.playDecorativeMotion

/**
 * Fades from one scope's [target] to the next so content a component cannot animate itself, such as a
 * row of labels, never pops. Reduced motion swaps it in place.
 *
 * Targets that share a [contentKey] are the same content, so a change between them recomposes in place
 * instead of fading; by default every distinct target fades.
 *
 * Size changes ease without clipping, so a chart's scrub readout may draw outside the content bounds.
 */
@Composable
internal fun <T> ScopeCrossfade(
    target: T,
    modifier: Modifier = Modifier,
    contentKey: (T) -> Any? = { it },
    content: @Composable (T) -> Unit,
) {
    if (playDecorativeMotion()) {
        AnimatedContent(
            targetState = target,
            modifier = modifier,
            transitionSpec = scopeTransition(),
            label = "ScopeCrossfade",
            contentKey = contentKey,
        ) { state ->
            content(state)
        }
    } else {
        content(target)
    }
}

/**
 * Swaps between a section's [empty] line and its [filled] content, easing the height without clipping.
 * Reduced motion swaps in place.
 */
@Composable
internal fun ScopeEmptySwitch(
    isEmpty: Boolean,
    empty: @Composable () -> Unit,
    filled: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (playDecorativeMotion()) {
        AnimatedContent(
            targetState = isEmpty,
            modifier = modifier,
            transitionSpec = scopeTransition(),
            label = "ScopeEmptySwitch",
        ) { showEmpty ->
            if (showEmpty) {
                empty()
            } else {
                filled()
            }
        }
    } else if (isEmpty) {
        empty()
    } else {
        filled()
    }
}

@Composable
private fun <S> scopeTransition(): AnimatedContentTransitionScope<S>.() -> ContentTransform {
    val effects = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val spatial = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
    return {
        (fadeIn(effects) togetherWith fadeOut(effects)).using(
            SizeTransform(clip = false) { _, _ -> spatial },
        )
    }
}
