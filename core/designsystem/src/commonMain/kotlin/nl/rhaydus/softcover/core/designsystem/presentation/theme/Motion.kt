package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * The motion specs the app owns because the expressive `MotionScheme` has none for them.
 */
object Motion {
    /** A critically damped spring slow enough to follow a value as it changes; settles in about 0.9 s without overshoot. */
    val valueChange: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryLow,
        visibilityThreshold = 0.01f,
    )
}
