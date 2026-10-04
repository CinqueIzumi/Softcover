package nl.rhaydus.softcover.feature.explore.presentation.mapper

import nl.rhaydus.softcover.core.component.mood.MoodTileInk
import nl.rhaydus.softcover.core.component.mood.MoodTileUiModel

/** A tile sized like a real one, drawn masked while the mood tags load. */
internal val MOOD_STAND_IN_TILE = MoodTileUiModel(
    key = -1,
    title = "Adventurous",
    countLabel = "1,234 books",
    ink = MoodTileInk.Neutral,
    isLoading = true,
)
