package nl.rhaydus.softcover.feature.lists.presentation.event

data class ListCreatedEvent(
    val listId: Int,
    val name: String,
) : CreateListEvent
