package nl.rhaydus.softcover.core.component.mood

/** @property key The tile's [MoodTileUiModel.key]. */
sealed interface MoodTileEvent {
    val key: Int

    data class Clicked(override val key: Int) : MoodTileEvent
}
