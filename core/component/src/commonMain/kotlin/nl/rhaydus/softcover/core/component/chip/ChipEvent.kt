package nl.rhaydus.softcover.core.component.chip

sealed interface ChipEvent {
    val key: String

    data class Clicked(override val key: String) : ChipEvent
    data class Dismissed(override val key: String) : ChipEvent

    /** [ChipLeading.SpoilerToggle]'s eye, tapped independently of the chip's own [Clicked] or [Dismissed]. */
    data class SpoilerToggled(override val key: String) : ChipEvent
}
