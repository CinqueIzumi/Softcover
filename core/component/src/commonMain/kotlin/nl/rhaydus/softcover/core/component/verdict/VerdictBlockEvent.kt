package nl.rhaydus.softcover.core.component.verdict

/** Everything the reader can do on [VerdictBlock], behind a single `onEvent` lambda (R1). */
sealed interface VerdictBlockEvent {
    /** A tap anywhere on the card, or on its "Edit" link. */
    data object EditRequested : VerdictBlockEvent
}
