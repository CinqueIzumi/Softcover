package nl.rhaydus.softcover.feature.profile.presentation.event

import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.toad.UiEvent

internal sealed interface StatsAtlasEvent : UiEvent {
    class OpenBookDetail(
        val bookId: Int,
        val initialCover: BookInitialCover?,
    ) : StatsAtlasEvent

    /** Emitted when an own-tag row is tapped; carries the tag to filter the library by. */
    data class OpenLibraryForTag(val tagName: String) : StatsAtlasEvent
}
