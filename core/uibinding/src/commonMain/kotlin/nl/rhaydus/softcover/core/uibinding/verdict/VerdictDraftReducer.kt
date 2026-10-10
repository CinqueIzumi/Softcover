package nl.rhaydus.softcover.core.uibinding.verdict

import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.richtext.applyEditToMarks
import nl.rhaydus.softcover.core.component.richtext.editorBufferToDocument
import nl.rhaydus.softcover.core.component.richtext.isBlank
import nl.rhaydus.softcover.core.component.richtext.isRangeMarked
import nl.rhaydus.softcover.core.component.richtext.toggleMark
import nl.rhaydus.softcover.core.component.verdict.VerdictDraftEvent
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel

/**
 * Folds one [VerdictDraftEvent] into the draft this model holds.
 */
fun VerdictSheetUiModel.reduce(event: VerdictDraftEvent): VerdictSheetUiModel = when (event) {
    is VerdictDraftEvent.RatingChanged -> copy(rating = event.rating)

    is VerdictDraftEvent.ReviewEdited -> copy(
        review = event.value,
        marks = if (event.value.text == review.text) {
            marks
        } else {
            applyEditToMarks(
                marks = marks,
                oldText = review.text,
                newText = event.value.text,
            )
        },
    ).withToolbarState()

    is VerdictDraftEvent.MarkToggled -> copy(
        marks = toggleMark(
            marks = marks,
            start = review.selection.min,
            end = review.selection.max,
            type = event.mark,
        ),
    ).withToolbarState()

    is VerdictDraftEvent.SpoilersToggled -> copy(spoilers = spoilers.copy(checked = event.checked))
}

/**
 * Keeps the draft [previous] holds while taking everything else from this freshly built model, so a
 * rebuild triggered by the book or its cover changing does not discard what the reader has typed.
 */
fun VerdictSheetUiModel.carryingDraftOf(previous: VerdictSheetUiModel?): VerdictSheetUiModel {
    if (previous == null) return this

    return copy(
        rating = previous.rating,
        review = previous.review,
        marks = previous.marks,
        spoilers = spoilers.copy(checked = previous.spoilers.checked),
    ).withToolbarState()
}

/** The draft review as the structured document that gets persisted. */
fun VerdictSheetUiModel.draftReview(): RichTextUiModel {
    val document = editorBufferToDocument(
        text = review.text,
        marks = marks,
    )

    return if (document.isBlank()) RichTextUiModel.EMPTY else document
}

/** Re-derives each toolbar chip's selected and enabled state from the current marks and selection. */
internal fun VerdictSheetUiModel.withToolbarState(): VerdictSheetUiModel {
    val selection = review.selection

    return copy(
        markChips = markChips
            .mapValues { (type, chip) ->
                chip.copy(
                    selected = isRangeMarked(
                        marks = marks,
                        start = selection.min,
                        end = selection.max,
                        type = type,
                    ),
                    interaction = if (selection.collapsed) {
                        ChipInteraction.Disabled
                    } else {
                        ChipInteraction.Clickable
                    },
                )
            }
            .toImmutableMap(),
    )
}
