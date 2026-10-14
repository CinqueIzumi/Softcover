package nl.rhaydus.softcover.core.component.document

sealed interface MarkdownDocumentEvent {
    data class LinkClicked(val url: String) : MarkdownDocumentEvent
}
