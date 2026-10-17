package nl.rhaydus.softcover.feature.explore.presentation.mapper

import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.mood.MoodTileInk
import nl.rhaydus.softcover.core.component.mood.MoodTileUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import nl.rhaydus.softcover.feature.explore.presentation.util.toTitleCaseWords

/** The tile at grid position [index]; a fifth tag onward repeats the four inks rather than adding one. */
internal fun MoodTag.toMoodTile(index: Int): MoodTileUiModel = MoodTileUiModel(
    key = id,
    title = label.toTitleCaseWords(),
    countLabel = "${formatGroupedNumber(bookCount)} books",
    ink = when (index % 4) {
        0 -> MoodTileInk.Cosy
        1 -> MoodTileInk.Dread
        3 -> MoodTileInk.HeartWrench
        else -> MoodTileInk.Neutral
    },
)
