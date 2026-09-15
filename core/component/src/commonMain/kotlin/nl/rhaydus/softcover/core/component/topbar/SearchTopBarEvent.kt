package nl.rhaydus.softcover.core.component.topbar

/**
 * Everything [SearchTopBar] reports (R1).
 *
 * These are **intents, not focus notifications** — the distinction the search chrome's whole focus
 * contract rests on. [SearchActivated] fires on every tap of the pill (and on a focus gain from
 * elsewhere, such as a desktop Tab key), because re-activating while the caller already holds
 * `focused = true` writes an equal state a `StateFlow` drops; without the tap intent a chrome whose
 * keyboard was dismissed by the system back button could never be reopened.
 */
sealed interface SearchTopBarEvent {
    data class QueryChanged(val query: String) : SearchTopBarEvent

    data object SearchActivated : SearchTopBarEvent

    data object SearchDismissed : SearchTopBarEvent

    data object SearchCleared : SearchTopBarEvent

    data object ScanRequested : SearchTopBarEvent
}
