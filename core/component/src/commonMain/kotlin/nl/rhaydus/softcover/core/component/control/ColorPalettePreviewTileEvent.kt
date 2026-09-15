package nl.rhaydus.softcover.core.component.control

/**
 * Everything the reader can do to a [ColorPalettePreviewTile], behind a single `onEvent` lambda (R1).
 *
 * Domain-free for the same reason as [nl.rhaydus.softcover.core.component.control.ThemePreviewTileEvent]
 * — the component library may not hold the domain `ColorPalette` its tap ultimately selects
 * (`component-contract.md` § 7.4). The host recovers which choice fired from the closure it hoisted
 * per tile.
 */
sealed interface ColorPalettePreviewTileEvent {
    data object Clicked : ColorPalettePreviewTileEvent
}
