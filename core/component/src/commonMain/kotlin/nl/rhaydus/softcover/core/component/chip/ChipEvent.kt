package nl.rhaydus.softcover.core.component.chip

/** `component-contract.md` § 7.2 R1 — one sealed event lambda, never a loose `onClick`. */
sealed interface ChipEvent {
    data class Clicked(val key: String) : ChipEvent
}
