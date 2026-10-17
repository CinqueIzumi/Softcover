package nl.rhaydus.softcover.feature.settings.presentation.event

import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.component.haptics.FeedbackEvent
import nl.rhaydus.toad.UiEvent

internal sealed interface LibraryVisibilitySettingsEvent : UiEvent {
    data object NavigateBack : LibraryVisibilitySettingsEvent

    data object SaveFailed :
        LibraryVisibilitySettingsEvent,
        FeedbackEvent {
        override val feedback = Feedback.Failure
    }
}
