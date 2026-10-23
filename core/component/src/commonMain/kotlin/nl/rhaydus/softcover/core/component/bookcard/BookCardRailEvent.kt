package nl.rhaydus.softcover.core.component.bookcard

sealed interface BookCardRailEvent {
    data class Card(val event: BookCardEvent) : BookCardRailEvent

    /** The visible window reached the last few cards; a paged rail fetches its next page. */
    data object NearEnd : BookCardRailEvent
}
