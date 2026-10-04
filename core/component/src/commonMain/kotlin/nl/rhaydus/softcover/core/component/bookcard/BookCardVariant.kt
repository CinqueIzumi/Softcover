package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel

/** A `null` [Column.cover] still reserves the cover's 2:3 footprint. */
@Immutable
sealed interface BookCardVariant {
    data class CoverOnly(val cover: BookCardCover) : BookCardVariant

    /**
     * @property hasMenu Draws the overflow button over the cover's top-end corner; a tap emits
     * [BookCardEvent.MenuClicked].
     */
    data class Column(
        val cover: BookCardCover?,
        val text: BookCardContent,
        val hasMenu: Boolean = false,
    ) : BookCardVariant

    /** @property lead `null` is a coverless row. */
    data class Row(
        val size: Size,
        val lead: BookCardLead?,
        val text: BookCardContent,
        val trailing: BookCardTrailing? = null,
        val deadline: DeadlineSummaryUiModel? = null,
        val contentHorizontalInset: Dp = 0.dp,
        val contentVerticalInset: Dp = 12.dp,
    ) : BookCardVariant {
        /** Sets the lead's width (40, 56 or 74dp) and the title's type and line count. */
        enum class Size {
            Compact,
            Standard,
            Large,
        }
    }
}
