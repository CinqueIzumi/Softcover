package nl.rhaydus.softcover.core.component.header

/** `component-contract.md` § 7.2 R1 — one sealed event lambda, never a loose `onClick`. */
sealed interface MastheadRefreshEvent {
    data object Refresh : MastheadRefreshEvent
}
