package nl.rhaydus.softcover.core.component.control

/** Everything the reader can do to a [ColorPalettePreviewTile], behind a single `onEvent` lambda. */
sealed interface ColorPalettePreviewTileEvent<K> {
    data class Clicked<K>(val key: K) : ColorPalettePreviewTileEvent<K>
}
