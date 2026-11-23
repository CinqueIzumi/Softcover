package nl.rhaydus.softcover.core.component.feeling

import nl.rhaydus.softcover.core.component.field.TextFieldEvent

/**
 * Everything the reader can do in [MoodMomentSheet]. The mood is single choice: the host decides what a
 * re-tapped chip does.
 */
sealed interface MoodMomentSheetEvent {
    data class ChipClicked(val key: String) : MoodMomentSheetEvent

    data class OwnWordChanged(val event: TextFieldEvent) : MoodMomentSheetEvent

    data class StrengthChanged(val strength: Int) : MoodMomentSheetEvent

    data object SaveRequested : MoodMomentSheetEvent

    /** Skip when adding, Delete when editing. */
    data object SecondaryRequested : MoodMomentSheetEvent

    data object Dismissed : MoodMomentSheetEvent
}
