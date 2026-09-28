package nl.rhaydus.softcover.core.component.badge

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Badge metrics shared by every [BadgeTone] (`component-contract.md` § 7.2 R2). Only the padding
 * varies, by [BadgeUiModel.size].
 */
internal object BadgeDimensions {
    val shape: Shape = RoundedCornerShape(6.dp)
    val iconSize = 12.dp
    val iconGap = 4.dp

    fun paddingFor(size: BadgeSize): PaddingValues = when (size) {
        BadgeSize.Compact -> PaddingValues(
            horizontal = 6.dp,
            vertical = 2.dp,
        )

        BadgeSize.Regular -> PaddingValues(
            horizontal = 8.dp,
            vertical = 4.dp,
        )
    }
}
