package nl.rhaydus.softcover.feature.lists.presentation.event

import nl.rhaydus.toad.UiEvent

internal sealed interface CreateListEvent : UiEvent {
    data class ListCreatedEvent(
        val listId: Int,
        val name: String,
    ) : CreateListEvent

    data class ListCreationFailedEvent(
        val message: String?,
    ) : CreateListEvent
}
