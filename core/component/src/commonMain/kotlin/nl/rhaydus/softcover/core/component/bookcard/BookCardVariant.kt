package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel

/** A `null` cover still reserves the cover's 2:3 footprint. */
@Immutable
sealed interface BookCardVariant {
    data class CoverOnly(val cover: CoverUiModel?) : BookCardVariant

    /**
     * @property menuLabel The overflow button's content description; non-null draws the button over the
     * cover's top-end corner, and a tap emits [BookCardEvent.MenuClicked].
     */
    data class Stacked(
        val cover: CoverUiModel?,
        val text: BookCardContent,
        val menuLabel: String? = null,
    ) : BookCardVariant

    /** @property lead `null` is a coverless row. */
    data class Row(
        val size: Size,
        val lead: BookCardLead?,
        val text: BookCardContent,
        val trailing: BookCardTrailing? = null,
        val deadline: DeadlineSummaryUiModel? = null,
    ) : BookCardVariant {
        /** Sets the lead's width (40, 56 or 74dp) and the title's type and line count. */
        enum class Size {
            Compact,
            Standard,
            Large,
        }
    }
}
