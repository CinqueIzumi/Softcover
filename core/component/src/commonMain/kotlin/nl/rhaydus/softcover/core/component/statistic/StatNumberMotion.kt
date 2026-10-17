package nl.rhaydus.softcover.core.component.statistic

/**
 * How a [StatNumber] moves to a new value.
 */
enum class StatNumberMotion {
    /** Eases through the values in between, so the final value is reached late. */
    Spring,

    /** Shows the new value in the frame it arrives and slides it in; for a count the reader checks against something else on screen. */
    Roll,
}
