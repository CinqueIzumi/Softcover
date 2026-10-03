package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.mood.MoodTileUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toMoodTile

internal data class MoodTilesSnapshot(
    val moodTags: List<MoodTag>,
) {
    fun compute(): ImmutableList<MoodTileUiModel> = moodTags
        .mapIndexed { index, mood -> mood.toMoodTile(index = index) }
        .toImmutableList()
}
