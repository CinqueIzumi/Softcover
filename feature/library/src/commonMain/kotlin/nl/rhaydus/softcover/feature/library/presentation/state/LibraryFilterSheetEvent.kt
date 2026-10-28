package nl.rhaydus.softcover.feature.library.presentation.state

internal sealed interface LibraryFilterSheetEvent {
    data object ApplyClicked : LibraryFilterSheetEvent

    data object ClearAllClicked : LibraryFilterSheetEvent

    data object Dismissed : LibraryFilterSheetEvent

    data class ChipToggled(val key: String) : LibraryFilterSheetEvent

    data class TagRemoved(val key: String) : LibraryFilterSheetEvent

    data class TagSearchChanged(
        val source: TagSource,
        val query: String,
    ) : LibraryFilterSheetEvent

    data class TagCategoryPicked(
        val source: TagSource,
        val key: String,
    ) : LibraryFilterSheetEvent
}
