package nl.rhaydus.softcover.core.uibinding.verdict

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.richtext.isBlank
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterLayout
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * Maps a [Book] onto what the verdict sheet renders. Promoted straight to `:core:uibinding` rather
 * than starting feature-local (R6) because it landed with two consumers already — `book_detail` and
 * `reading` both raise the sheet off their own state.
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

    val (header, topAction, saveLabel) = when (context) {
        VerdictSheetContext.FINISHED -> Triple(
            SectionHeaderUiModel.Section(
                eyebrow = "The last page",
                headline = "You finished $title.",
                description = "A rating and a few words, if you like — both are optional.",
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

    return VerdictSheetUiModel(
        scaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = header,
                trailing = cover?.let { SheetHeaderTrailing.Jacket(cover = it) },
            ),
            topAction = topAction,
            footer = SheetFooterUiModel(
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
                layout = SheetFooterLayout.Stacked,
            ),
            layout = SheetLayout.Pinned,
        ),
        initialRating = userBook?.rating?.takeIf { it > 0.0 },
        initialReview = review ?: RichTextUiModel.EMPTY,
        initialHasSpoilers = userBook?.reviewHasSpoilers == true,
    )
}
