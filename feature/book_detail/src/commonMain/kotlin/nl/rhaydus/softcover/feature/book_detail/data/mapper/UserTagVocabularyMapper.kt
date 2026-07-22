package nl.rhaydus.softcover.feature.book_detail.data.mapper

import nl.rhaydus.softcover.core.database.model.UserTagVocabularyEntity
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag

/**
 * [UserTagVocabularyEntity.category] stores the enum's [TagCategory.name] (e.g. "CONTENT_WARNING")
 * rather than its API string, since this row never round-trips through the network layer.
 */
internal fun UserTagVocabularyEntity.toUserTag(): UserTag = UserTag(
    name = name,
    category = TagCategory.fromName(category),
    count = usageCount,
    spoiler = false,
)

internal fun UserTag.toVocabularyEntity(userId: Int): UserTagVocabularyEntity = UserTagVocabularyEntity(
    userId = userId,
    category = category.name,
    name = name,
    usageCount = count,
)
