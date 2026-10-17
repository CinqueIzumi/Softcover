package nl.rhaydus.softcover.core.component.progress

/** What the "When did you read this?" row asks of its host. */
sealed interface WhenReadEvent {
    /**
     * The reader moved the backdate by [minutes] (negative is earlier). From "Just now" it starts at the
     * current moment; the host clamps the result so it is never in the future.
     */
    data class Shifted(val minutes: Int) : WhenReadEvent

    /** The reader dismissed the backdate; the host clears it back to "Just now". */
    data object Reset : WhenReadEvent
}
