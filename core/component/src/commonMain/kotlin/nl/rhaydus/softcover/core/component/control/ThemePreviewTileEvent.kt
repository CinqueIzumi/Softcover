package nl.rhaydus.softcover.core.component.control

/**
 * Everything the reader can do to a [ThemePreviewTile], behind a single `onEvent` lambda (R1).
 *
 * The tile is a radio choice among a small, fixed set — it carries no key of its own, because the
 * component library may not hold the domain `ThemeMode` its tap ultimately selects
 * (`component-contract.md` § 7.4). The host recovers which choice fired from the closure it hoisted
 * per tile, the same way [nl.rhaydus.softcover.core.component.lists.ChooseListsEvent] recovers row
 * identity from a domain-free payload.
 */
sealed interface ThemePreviewTileEvent {
    data object Clicked : ThemePreviewTileEvent
}
