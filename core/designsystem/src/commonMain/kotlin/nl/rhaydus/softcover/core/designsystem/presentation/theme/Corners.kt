package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * The corner-radius scale: Material 3 Expressive's default radii plus a 2dp hairline and a pill.
 * An off-scale radius snaps to the nearest step.
 */
object Corners {
    val xxs: RoundedCornerShape = RoundedCornerShape(2.dp)
    val xs: RoundedCornerShape = RoundedCornerShape(4.dp)
    val sm: RoundedCornerShape = RoundedCornerShape(8.dp)
    val md: RoundedCornerShape = RoundedCornerShape(12.dp)
    val lg: RoundedCornerShape = RoundedCornerShape(16.dp)
    val xl: RoundedCornerShape = RoundedCornerShape(20.dp)
    val xxl: RoundedCornerShape = RoundedCornerShape(28.dp)
    val pill: RoundedCornerShape = RoundedCornerShape(percent = 50)
}
