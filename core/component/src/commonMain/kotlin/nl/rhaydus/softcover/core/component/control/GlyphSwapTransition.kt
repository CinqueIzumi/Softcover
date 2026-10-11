package nl.rhaydus.softcover.core.component.control

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MotionScheme

internal fun glyphSwapTransition(
    popsIn: Boolean,
    motionScheme: MotionScheme,
): ContentTransform {
    val fadeIn = fadeIn(animationSpec = motionScheme.fastEffectsSpec())
    val fadeOut = fadeOut(animationSpec = motionScheme.fastEffectsSpec())

    if (popsIn.not()) return fadeIn togetherWith fadeOut

    val pop = scaleIn(
        initialScale = 0.6f,
        animationSpec = motionScheme.defaultSpatialSpec(),
    ) + fadeIn

    return pop togetherWith fadeOut
}
