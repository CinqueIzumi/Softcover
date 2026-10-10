package nl.rhaydus.softcover.feature.library.presentation.event

import nl.rhaydus.toad.UiEvent

internal sealed interface LibraryEvent : UiEvent {
    class BulkMoveAppliedEvent : LibraryEvent

    class BulkMoveFailedEvent : LibraryEvent

    class ListMembershipChangedEvent : LibraryEvent

    class ListMembershipChangeFailedEvent : LibraryEvent

    class NewListBooksAddFailedEvent : LibraryEvent
}
