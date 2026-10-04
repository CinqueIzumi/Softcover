package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextMarkType
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/**
 * Everything [VerdictSheet] renders: the [scaffold] chrome carrying the copy (header, dismiss and save
 * labels, and a secondary delete action only when there is a review to delete), and the verdict the draft
 * starts from.
 *
 * [initialRating], [initialReview] and [spoilers] seed the sheet's local draft once; later changes to the
 * model do not reset what the reader has typed. [markChips] is the toolbar's chip for each mark; the sheet
 * only layers the selection's state (selected, enabled) onto them.
 */
@Immutable
data class VerdictSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val initialRating: Double?,
    val initialReview: RichTextUiModel,
    val spoilers: ToggleRowUiModel<String>,
    val markChips: ImmutableMap<RichTextMarkType, ChipUiModel>,
)
