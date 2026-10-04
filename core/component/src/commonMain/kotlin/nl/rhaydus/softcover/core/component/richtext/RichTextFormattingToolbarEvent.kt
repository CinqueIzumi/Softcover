package nl.rhaydus.softcover.core.component.richtext

sealed interface RichTextFormattingToolbarEvent {
    data class MarkToggled(val mark: RichTextMarkType) : RichTextFormattingToolbarEvent
}
