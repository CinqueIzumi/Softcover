package nl.rhaydus.softcover.feature.book_detail.presentation.state

internal sealed interface TagEditorSheetEvent {
    data class CategorySelected(val categoryKey: String) : TagEditorSheetEvent

    data class DraftChanged(val text: String) : TagEditorSheetEvent

    data object DraftSubmitted : TagEditorSheetEvent

    data class SuggestionSelected(val chipKey: String) : TagEditorSheetEvent

    data class TagRemoved(val chipKey: String) : TagEditorSheetEvent

    data class TagSpoilerToggled(val chipKey: String) : TagEditorSheetEvent

    data object Dismissed : TagEditorSheetEvent
}
