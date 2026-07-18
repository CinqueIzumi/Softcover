package nl.rhaydus.softcover.feature.explore.data.mapper

import nl.rhaydus.softcover.GetMoodTagsQuery
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag

internal fun GetMoodTagsQuery.Data.Tag.toMoodTag(): MoodTag = MoodTag(
    id = id.toInt(),
    label = tag,
    slug = slug,
    bookCount = count,
)
