package nl.rhaydus.softcover.core.component.richtext

/** Everything [ClickableText] reports (R1). */
sealed interface ClickableTextEvent {
    /** A run carrying a `url` annotation was tapped; [url] is that annotation's payload. */
    data class LinkClicked(val url: String) : ClickableTextEvent
}
