package nl.rhaydus.softcover.core.designsystem.presentation.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The spacing scale: a 4dp grid plus a 2dp hairline.
 *
 * Spacing is the value of `padding`, `PaddingValues`, `contentPadding`, `spacedBy`, `offset` or a
 * `Spacer`'s size. Component sizes, strokes and borders stay literal `.dp`. An off-grid value snaps
 * to the nearest step; a tie rounds down for inner padding and up for gaps.
 */
object Spacing {
    val xxxs: Dp = 2.dp
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 28.dp
    val xxxl: Dp = 32.dp
    val x4l: Dp = 40.dp
    val x5l: Dp = 48.dp

    /** The page horizontal gutter, shared by every page on every platform. */
    val gutter: Dp = xl

    /** The gap between major page sections. */
    val section: Dp = x4l
}
