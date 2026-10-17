package nl.rhaydus.softcover.core.component.richtext

sealed interface RichTextEvent {
    /** A tap that did not land on a concealed spoiler. */
    data object Tapped : RichTextEvent
}
