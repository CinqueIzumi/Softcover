package nl.rhaydus.softcover.feature.lists.presentation.event

internal data class ListNameTakenEvent(
    val name: String,
) : CreateListEvent
