package nl.rhaydus.softcover.core.component.modifier

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.ResizeMode
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalSharedTransitionScope

/**
 * Registers this node as a shared element under [key] across a navigation transition. A `null` [key],
 * decorative motion turned off, or a missing transition scope leaves the modifier untouched.
 *
 * Text passes `ResizeMode.scaleToBounds()` so a smaller style scales into a larger one without
 * reflowing mid-flight.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Modifier.navSharedBounds(
    key: String?,
    resizeMode: ResizeMode = ResizeMode.scaleToBounds(),
): Modifier {
    val playMotion = playDecorativeMotion()
    val sharedScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
    val boundsSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Rect>()

    if (
        playMotion.not() ||
        key == null ||
        sharedScope == null ||
        animatedVisibilityScope == null
    ) {
        return this
    }

    return with(sharedScope) {
        sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = BoundsTransform { _, _ -> boundsSpec },
            resizeMode = resizeMode,
        )
    }
}
