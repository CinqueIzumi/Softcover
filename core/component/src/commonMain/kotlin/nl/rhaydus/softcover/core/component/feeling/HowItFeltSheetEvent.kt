package nl.rhaydus.softcover.core.component.feeling

import nl.rhaydus.softcover.core.component.field.TextFieldEvent

sealed interface HowItFeltSheetEvent {
    data class ChipClicked(val key: String) : HowItFeltSheetEvent

    data class OwnWordChanged(val event: TextFieldEvent) : HowItFeltSheetEvent

    data class StrengthChanged(
        val key: String,
        val strength: Int,
    ) : HowItFeltSheetEvent

    data object SaveRequested : HowItFeltSheetEvent

    data object CancelRequested : HowItFeltSheetEvent

    data object Dismissed : HowItFeltSheetEvent
}
