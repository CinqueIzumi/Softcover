package nl.rhaydus.softcover.showcase.live.sidebar.event

import nl.rhaydus.toad.UiEvent

internal sealed interface SidebarLiveEvent : UiEvent {
    data object NavigateBack : SidebarLiveEvent
}
