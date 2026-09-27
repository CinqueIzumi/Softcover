package nl.rhaydus.softcover.core.component.chip

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

/**
 * Chip metrics shared by every [ChipTone] (`component-contract.md` § 7.2 R2). Only the padding
 * varies, by [ChipUiModel.size].
 */
internal object ChipDimensions {
    val iconSize = 18.dp
    val iconGap = 8.dp
    val dismissIconSize = 16.dp
    val dismissIconGap = 8.dp
    val borderWidth = 1.dp
    const val disabledAlpha = 0.45f

    fun paddingFor(size: ChipSize): PaddingValues = when (size) {
        ChipSize.Regular -> PaddingValues(
            horizontal = 14.dp,
            vertical = 8.dp,
        )
        ChipSize.Compact -> PaddingValues(
            horizontal = 10.dp,
            vertical = 6.dp,
        )
    }
}
