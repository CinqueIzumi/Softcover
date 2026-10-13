package nl.rhaydus.softcover.core.component.verdict

import androidx.compose.ui.text.input.TextFieldValue
import nl.rhaydus.softcover.core.component.richtext.RichTextMarkType

/** The edits to the verdict draft; none of them writes anything until [VerdictSheetEvent.Saved]. */
sealed interface VerdictDraftEvent : VerdictSheetEvent {
    data class RatingChanged(val rating: Double?) : VerdictDraftEvent

    /** The review field changed; [value] carries the new text, selection and composing range. */
    data class ReviewEdited(val value: TextFieldValue) : VerdictDraftEvent

    /** A toolbar chip was tapped to toggle [mark] over the current selection. */
    data class MarkToggled(val mark: RichTextMarkType) : VerdictDraftEvent

    data class SpoilersToggled(val checked: Boolean) : VerdictDraftEvent
}
