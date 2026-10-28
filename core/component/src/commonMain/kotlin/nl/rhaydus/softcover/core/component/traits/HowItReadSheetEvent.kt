package nl.rhaydus.softcover.core.component.traits

/**
 * Everything the reader can do in [HowItReadSheet]. Every draft edit leaves as an event and returns as a
 * new model; the sheet keeps none of it.
 */
sealed interface HowItReadSheetEvent {
    /** A tap on a chip of any group. The host decides what the key's group does with it. */
    data class ChipClicked(val key: String) : HowItReadSheetEvent

    data class BalanceChanged(val value: Float) : HowItReadSheetEvent

    data object BalanceCleared : HowItReadSheetEvent

    /** The sheet dismisses itself right after emitting this; the host only records the answers. */
    data object SaveRequested : HowItReadSheetEvent

    /** The sheet dismisses itself right after emitting this. */
    data object CancelRequested : HowItReadSheetEvent

    data object Dismissed : HowItReadSheetEvent
}
