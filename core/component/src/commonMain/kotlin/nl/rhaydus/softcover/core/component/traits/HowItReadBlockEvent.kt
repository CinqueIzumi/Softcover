package nl.rhaydus.softcover.core.component.traits

/** Everything the reader can do on [HowItReadBlock], behind a single `onEvent` lambda (R1). */
sealed interface HowItReadBlockEvent {
    /** A tap on the summary row. */
    data object ExpandToggled : HowItReadBlockEvent

    /** A tap on the invitation, or on the "Edit" link; the caller wires it to open [HowItReadSheet]. */
    data object EditRequested : HowItReadBlockEvent
}
