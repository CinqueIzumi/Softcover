package nl.rhaydus.softcover.core.component.control

/** Everything the reader can do to a [ThemePreviewTile] or a [ColorPalettePreviewTile], behind a single `onEvent` lambda. */
sealed interface PreviewTileEvent<K> {
    data class Clicked<K>(val key: K) : PreviewTileEvent<K>
}
