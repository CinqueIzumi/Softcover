package nl.rhaydus.softcover.core.uibinding.verdict

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipFace
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextMarkType
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.richtext.documentToEditorBuffer
import nl.rhaydus.softcover.core.component.richtext.isBlank
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * Maps a [Book] onto what the verdict sheet renders.
 *
 * [review] is the book's review already mapped to rich text. Deletion is offered only when [context]
 * is [VerdictSheetContext.EDIT] and there is a non-blank [review] to delete, so the finished prompt
 * never carries it.
 *
 * [cover] is `null` when no cover has been resolved yet; the header then has no jacket.
 */
fun Book.toVerdictSheetUiModel(
    context: VerdictSheetContext,
    review: RichTextUiModel?,
    cover: CoverUiModel?,
): VerdictSheetUiModel {
    val canDelete = context == VerdictSheetContext.EDIT && review?.isBlank() == false

    val (header, dismissLabel, saveLabel) = when (context) {
        VerdictSheetContext.FINISHED -> Triple(
            SectionHeaderUiModel.Section(
                eyebrow = "The last page",
                headline = "You finished $title.",
                description = "Add a rating or a few words. Both are optional.",
            ),
            "Not now",
            "Save",
        )

        VerdictSheetContext.EDIT -> Triple(
            SectionHeaderUiModel.Section(
                eyebrow = "Your verdict",
                headline = "Your take on $title.",
            ),
            "Cancel",
            "Save changes",
        )
    }

    val buffer = documentToEditorBuffer(document = review ?: RichTextUiModel.EMPTY)

    return VerdictSheetUiModel(
        scaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = header,
                trailing = cover?.let { SheetHeaderTrailing.Jacket(cover = it) },
            ),
            dismissLabel = dismissLabel,
            footer = SheetFooterUiModel.Stacked(
                primary = SheetFooterButton(
                    label = saveLabel,
                    dismissesSheet = true,
                ),
                secondary = if (canDelete) {
                    SheetFooterButton(
                        label = "Delete review",
                        dismissesSheet = true,
                    )
                } else {
                    null
                },
            ),
            layout = SheetLayout.Pinned,
        ),
        rating = userBook?.rating?.takeIf { it > 0.0 },
        review = TextFieldValue(
            text = buffer.text,
            selection = TextRange(buffer.text.length),
        ),
        marks = buffer.marks,
        spoilers = ToggleRowUiModel(
            key = "spoilers",
            title = "Contains spoilers",
            gloss = "Readers will need to tap to reveal the whole review.",
            checked = userBook?.reviewHasSpoilers == true,
        ),
        markChips = RichTextMarkType.entries
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
            .toImmutableMap(),
    ).withToolbarState()
}
