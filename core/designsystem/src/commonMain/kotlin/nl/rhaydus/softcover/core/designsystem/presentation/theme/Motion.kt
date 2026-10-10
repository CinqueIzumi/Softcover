package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * The motion specs the app owns because the expressive `MotionScheme` has none for them.
 */
object Motion {
    /** A critically damped spring slow enough to read a number counting between values; settles in about 0.9 s. */
    val countUp: SpringSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryLow,
        visibilityThreshold = 0.01f,
    )
}
