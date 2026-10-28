package nl.rhaydus.softcover.core.book.data.mapper

import nl.rhaydus.softcover.SaveTagsMutation
import nl.rhaydus.softcover.core.database.model.BookUserTagEntity
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.fragment.TagFragment
import nl.rhaydus.softcover.fragment.TaggingFragment
import nl.rhaydus.softcover.type.BasicTag

/** [UserTag.count] is the tag's global count, not the user's own usage. */
internal fun TaggingFragment.toUserTag(tag: TagFragment): UserTag = UserTag(
    name = tag.tag,
    category = TagCategory.fromApiString(tag.tag_category.category),
    count = tag.count,
    spoiler = spoiler,
)

internal fun SaveTagsMutation.Data.UpsertTags.Tag.toUserTag(): UserTag = UserTag(
    name = tag,
    category = TagCategory.fromApiString(category),
    count = count ?: 0,
    spoiler = spoiler,
)

internal fun UserTag.toBasicTag(): BasicTag = BasicTag(
    category = category.apiValue,
    spoiler = spoiler,
    tag = name,
)

internal fun UserTag.toEntity(bookId: Int): BookUserTagEntity = BookUserTagEntity(
    bookId = bookId,
    category = category.name,
    name = name,
    spoiler = spoiler,
)

internal fun BookUserTagEntity.toUserTag(): UserTag = UserTag(
    name = name,
    category = TagCategory.fromName(category),
    spoiler = spoiler,
)
