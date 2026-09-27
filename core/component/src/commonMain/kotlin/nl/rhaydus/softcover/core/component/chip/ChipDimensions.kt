package nl.rhaydus.softcover.core.component.chip

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Per-[ChipVariant] metrics read by [Chip]'s variant bodies (`component-contract.md` § 7.2 R2).
 * This is a straight port of today's inlined literals — **the table changes no pixel**, it only
 * gives each variant's existing choice a name so a new variant has one place to add its own.
 *
 * [innerGap] is the extra gap [ChipVariant.AddOutlined] and [ChipVariant.Remove] insert between
 * their leading glyph and the label (or the label and [removeIconSize]'s icon); unused elsewhere.
 */
internal data class ChipDimensions(
    val paddingStart: Dp,
    val paddingTop: Dp,
    val paddingEnd: Dp,
    val paddingBottom: Dp,
    val innerGap: Dp,
    val leadingIconSize: Dp,
    val leadingIconGap: Dp,
    val dismissIconSize: Dp,
    val dismissIconGap: Dp,
    val removeIconSize: Dp,
    val borderWidth: Dp,
    val disabledAlpha: Float,
) {
    companion object {
        private val LeadingIconSize = 18.dp
        private val LeadingIconGap = 8.dp
        private val DismissIconSize = 16.dp
        private val DismissIconGap = 8.dp
        private val RemoveIconSize = 12.dp
        private const val DISABLED_ALPHA = 0.45f

        fun forVariant(variant: ChipVariant): ChipDimensions = when (variant) {
            is ChipVariant.Tonal -> ChipDimensions(
                paddingStart = 14.dp,
                paddingTop = 10.dp,
                paddingEnd = 14.dp,
                paddingBottom = 10.dp,
                innerGap = 0.dp,
                leadingIconSize = LeadingIconSize,
                leadingIconGap = LeadingIconGap,
                dismissIconSize = DismissIconSize,
                dismissIconGap = DismissIconGap,
                removeIconSize = RemoveIconSize,
                borderWidth = 0.dp,
                disabledAlpha = DISABLED_ALPHA,
            )

            ChipVariant.Spoiler -> ChipDimensions(
                paddingStart = 14.dp,
                paddingTop = 10.dp,
                paddingEnd = 14.dp,
                paddingBottom = 10.dp,
                innerGap = 0.dp,
                leadingIconSize = LeadingIconSize,
                leadingIconGap = LeadingIconGap,
                dismissIconSize = DismissIconSize,
                dismissIconGap = DismissIconGap,
                removeIconSize = RemoveIconSize,
                borderWidth = 0.dp,
                disabledAlpha = DISABLED_ALPHA,
            )

            ChipVariant.Add -> ChipDimensions(
                paddingStart = 13.dp,
                paddingTop = 7.dp,
                paddingEnd = 13.dp,
                paddingBottom = 7.dp,
                innerGap = 0.dp,
                leadingIconSize = LeadingIconSize,
                leadingIconGap = LeadingIconGap,
                dismissIconSize = DismissIconSize,
                dismissIconGap = DismissIconGap,
                removeIconSize = RemoveIconSize,
                borderWidth = 0.dp,
                disabledAlpha = DISABLED_ALPHA,
            )

            ChipVariant.AddOutlined -> ChipDimensions(
                paddingStart = 14.dp,
                paddingTop = 7.dp,
                paddingEnd = 14.dp,
                paddingBottom = 7.dp,
                innerGap = 4.dp,
                leadingIconSize = LeadingIconSize,
                leadingIconGap = LeadingIconGap,
                dismissIconSize = DismissIconSize,
                dismissIconGap = DismissIconGap,
                removeIconSize = RemoveIconSize,
                borderWidth = 1.dp,
                disabledAlpha = DISABLED_ALPHA,
            )

            is ChipVariant.Remove -> ChipDimensions(
                paddingStart = 13.dp,
                paddingTop = 7.dp,
                paddingEnd = 10.dp,
                paddingBottom = 7.dp,
                innerGap = 4.dp,
                leadingIconSize = LeadingIconSize,
                leadingIconGap = LeadingIconGap,
                dismissIconSize = DismissIconSize,
                dismissIconGap = DismissIconGap,
                removeIconSize = RemoveIconSize,
                borderWidth = 0.dp,
                disabledAlpha = DISABLED_ALPHA,
            )

            is ChipVariant.Quiet -> ChipDimensions(
                paddingStart = 14.dp,
                paddingTop = 8.dp,
                paddingEnd = 14.dp,
                paddingBottom = 8.dp,
                innerGap = 0.dp,
                leadingIconSize = LeadingIconSize,
                leadingIconGap = LeadingIconGap,
                dismissIconSize = DismissIconSize,
                dismissIconGap = DismissIconGap,
                removeIconSize = RemoveIconSize,
                borderWidth = 0.dp,
                disabledAlpha = DISABLED_ALPHA,
            )

            is ChipVariant.Format -> ChipDimensions(
                paddingStart = 16.dp,
                paddingTop = 8.dp,
                paddingEnd = 16.dp,
                paddingBottom = 8.dp,
                innerGap = 0.dp,
                leadingIconSize = LeadingIconSize,
                leadingIconGap = LeadingIconGap,
                dismissIconSize = DismissIconSize,
                dismissIconGap = DismissIconGap,
                removeIconSize = RemoveIconSize,
                borderWidth = 0.dp,
                disabledAlpha = DISABLED_ALPHA,
            )
        }
    }
}
