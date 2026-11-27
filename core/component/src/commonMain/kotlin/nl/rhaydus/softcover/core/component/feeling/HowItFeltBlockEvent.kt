package nl.rhaydus.softcover.core.component.feeling

sealed interface HowItFeltBlockEvent {
    data object EditRequested : HowItFeltBlockEvent

    data class MomentClicked(val id: String) : HowItFeltBlockEvent
}
