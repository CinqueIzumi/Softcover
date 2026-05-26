package nl.rhaydus.softcover.feature.lists.presentation.event

data class ListNameTakenEvent(
    val name: String,
) : CreateListEvent
