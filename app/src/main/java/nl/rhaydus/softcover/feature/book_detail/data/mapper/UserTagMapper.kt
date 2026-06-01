package nl.rhaydus.softcover.feature.book_detail.data.mapper

import nl.rhaydus.softcover.FindTagsByUserAndTaggableQuery
import nl.rhaydus.softcover.SaveTagsMutation
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.type.BasicTag

/**
 * The user's own tag as returned by the find query — a tagging plus its joined tag.
 */
fun FindTagsByUserAndTaggableQuery.Data.Tagging.toUserTag(): UserTag = UserTag(
    name = tag.tag,
    category = TagCategory.fromApiString(tag.tag_category.category),
    count = tag.count,
    spoiler = spoiler,
)

/**
 * The user's own tag as echoed back by the upsert mutation. This payload carries no tag id, so the
 * tag is rebuilt purely from its category + name (the pair the save API round-trips on).
 */
fun SaveTagsMutation.Data.UpsertTags.Tag.toUserTag(): UserTag = UserTag(
    name = tag,
    category = TagCategory.fromApiString(category),
    count = count ?: 0,
    spoiler = spoiler,
)

/**
 * The input shape the upsert mutation expects. The category travels as its API string (e.g.
 * "Content Warning", not the display label "Content warning"), so it round-trips losslessly.
 */
fun UserTag.toBasicTag(): BasicTag = BasicTag(
    category = category.apiValue,
    spoiler = spoiler,
    tag = name,
)
