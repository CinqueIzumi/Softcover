package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextMark
import nl.rhaydus.softcover.core.component.richtext.RichTextMarkType
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

/**
 * Everything [VerdictSheet] renders: the [scaffold] chrome carrying the copy (header, dismiss and save
 * labels, and a secondary delete action only when there is a review to delete), and the draft verdict.
 *
 * The draft is [rating], [review] with its [marks], and [spoilers]'s `checked`. The host owns it: each
 * [VerdictDraftEvent] is folded into a new model, so the sheet shows exactly what the host holds.
 * [review] is a [TextFieldValue] so the selection and the IME's composing range survive the round trip.
 * [markChips] are the toolbar's chips, already carrying whether the selection holds each mark and
 * whether a selection exists.
 */
@Immutable
data class VerdictSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val rating: Double?,
    val review: TextFieldValue,
    val marks: ImmutableList<RichTextMark>,
    val spoilers: ToggleRowUiModel<String>,
    val markChips: ImmutableMap<RichTextMarkType, ChipUiModel>,
)
