package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag

private const val MAX_BOOK_TAGS_PER_CATEGORY = 5

/** The book's community tags a reader may put on a Book card: the most-used few per category. */
internal fun Book.shareTagCandidates(): Map<TagCategory, List<String>> =
    tags
        .groupBy { it.category }
        .mapValues { (_, tags) ->
            tags
                .sortedByDescending { it.count }
                .map { it.name }
                .distinct()
                .take(MAX_BOOK_TAGS_PER_CATEGORY)
        }
        .inCategoryOrder()

/** The reader's own tags a reader may put on a My update card; spoiler-flagged tags never qualify. */
internal fun List<UserTag>.shareTagCandidates(): Map<TagCategory, List<String>> =
    filterNot { it.spoiler }
        .groupBy { it.category }
        .mapValues { (_, tags) -> tags.map { it.name }.distinct() }
        .inCategoryOrder()

private fun Map<TagCategory, List<String>>.inCategoryOrder(): Map<TagCategory, List<String>> {
    val ordered = TagCategory.entries.mapNotNull { category ->
        this[category]?.takeIf { it.isNotEmpty() }?.let { category to it }
    }

    val seen = mutableSetOf<String>()

    return ordered
        .filter { it.first != TagCategory.OTHER }
        .ifEmpty { ordered }
        .mapNotNull { (category, names) ->
            names.filter { seen.add(it) }.takeIf { it.isNotEmpty() }?.let { category to it }
        }
        .toMap()
}
