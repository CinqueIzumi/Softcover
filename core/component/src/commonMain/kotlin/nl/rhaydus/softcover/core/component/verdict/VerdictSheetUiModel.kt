package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipFace
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextMark
import nl.rhaydus.softcover.core.component.richtext.RichTextMarkType
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
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
) {
    companion object : UiModelPreviews<VerdictSheetUiModel> {
        private val markChips: ImmutableMap<RichTextMarkType, ChipUiModel> = RichTextMarkType.entries
            .associateWith { mark ->
                ChipUiModel(
                    key = mark.name,
                    label = when (mark) {
                        RichTextMarkType.BOLD -> "Bold"
                        RichTextMarkType.ITALIC -> "Italic"
                        RichTextMarkType.SPOILER -> "Spoiler"
                    },
                    face = when (mark) {
                        RichTextMarkType.BOLD -> ChipFace.Bold
                        RichTextMarkType.ITALIC -> ChipFace.Italic
                        RichTextMarkType.SPOILER -> ChipFace.Plain
                    },
                )
            }
            .toImmutableMap()

        private val finishedScaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = "The last page",
                    headline = "You finished Piranesi.",
                    description = "Add a rating or a few words. Both are optional.",
                ),
            ),
            dismissLabel = "Not now",
            footer = SheetFooterUiModel.Stacked(
                primary = SheetFooterButton(label = "Save"),
                secondary = null,
            ),
            layout = SheetLayout.Pinned,
        )

        private const val REVIEW = "A quiet house of statues and tides, and a mystery that unfolds gently."

        override val previews: ImmutableMap<String, VerdictSheetUiModel> = persistentMapOf(
            "Finished, unrated" to VerdictSheetUiModel(
                scaffold = finishedScaffold,
                rating = null,
                review = TextFieldValue(),
                marks = persistentListOf(),
                spoilers = ToggleRowUiModel(
                    key = "spoilers",
                    title = "Contains spoilers",
                    gloss = "Readers will need to tap to reveal the whole review.",
                    checked = false,
                ),
                markChips = markChips,
            ),
            "Edit, rated with a review" to VerdictSheetUiModel(
                scaffold = finishedScaffold.copy(
                    header = SheetHeaderUiModel(
                        section = SectionHeaderUiModel.Section(
                            eyebrow = "Your verdict",
                            headline = "Your take on Piranesi.",
                        ),
                    ),
                    dismissLabel = "Cancel",
                    footer = SheetFooterUiModel.Stacked(
                        primary = SheetFooterButton(label = "Save changes"),
                        secondary = SheetFooterButton(label = "Delete review"),
                    ),
                ),
                rating = 4.5,
                review = TextFieldValue(
                    text = REVIEW,
                    selection = TextRange(REVIEW.length),
                ),
                marks = persistentListOf(
                    RichTextMark(
                        start = 2,
                        end = 13,
                        type = RichTextMarkType.ITALIC,
                    ),
                    RichTextMark(
                        start = 55,
                        end = 69,
                        type = RichTextMarkType.SPOILER,
                    ),
                ),
                spoilers = ToggleRowUiModel(
                    key = "spoilers",
                    title = "Contains spoilers",
                    gloss = "Readers will need to tap to reveal the whole review.",
                    checked = true,
                ),
                markChips = markChips,
            ),
        )
    }
}
