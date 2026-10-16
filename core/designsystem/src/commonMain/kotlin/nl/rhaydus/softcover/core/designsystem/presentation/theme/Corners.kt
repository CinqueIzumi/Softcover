package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * The corner-radius scale: Material 3 Expressive's default radii plus a 2dp hairline and a pill.
 * An off-scale radius snaps to the nearest step.
 */
object Corners {
    val xxs: RoundedCornerShape = RoundedCornerShape(CornerRadius.xxs)
    val xs: RoundedCornerShape = RoundedCornerShape(CornerRadius.xs)
    val sm: RoundedCornerShape = RoundedCornerShape(CornerRadius.sm)
    val md: RoundedCornerShape = RoundedCornerShape(CornerRadius.md)
    val lg: RoundedCornerShape = RoundedCornerShape(CornerRadius.lg)
    val xl: RoundedCornerShape = RoundedCornerShape(CornerRadius.xl)
    val xxl: RoundedCornerShape = RoundedCornerShape(CornerRadius.xxl)
    val pill: RoundedCornerShape = RoundedCornerShape(percent = 50)
}
