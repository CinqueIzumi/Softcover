package nl.rhaydus.softcover.core.component.control

/** Everything the reader can do to a [ThemePreviewTile], behind a single `onEvent` lambda. */
sealed interface ThemePreviewTileEvent<K> {
    data class Clicked<K>(val key: K) : ThemePreviewTileEvent<K>
}
