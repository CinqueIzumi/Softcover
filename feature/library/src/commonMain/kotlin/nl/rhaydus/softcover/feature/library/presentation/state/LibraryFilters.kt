package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.feature.library.presentation.util.finishedYear

internal data class LibraryFilters(
    val hardcoverTags: TagSelection<HardcoverTagKey> = TagSelection(),
    val yourTags: TagSelection<UserTagKey> = TagSelection(),
    val formats: ImmutableSet<String> = persistentSetOf(),
    val releaseYears: ImmutableSet<Int> = persistentSetOf(),
    /** Single-select year-finished facet, offered on the Read tab only. */
    val readYear: Int? = null,
    val owned: Boolean? = null,
    val ratingMin: Double? = null,
) {
    val isEmpty: Boolean
        get() = hardcoverTags.isEmpty &&
            yourTags.isEmpty &&
            formats.isEmpty() &&
            releaseYears.isEmpty() &&
            readYear == null &&
            owned == null &&
            ratingMin == null

    fun matchesBook(
        book: Book,
        userTags: List<UserTag>,
    ): Boolean {
        val tagsMatch = matchesTags(
            communityTags = book.tags,
            userTags = userTags,
        )

        if (tagsMatch.not()) return false

        if (formats.isNotEmpty() && book.editions.none { it.format in formats }) return false

        if (releaseYears.isNotEmpty() && book.releaseYear !in releaseYears) return false

        if (readYear != null && book.finishedYear() != readYear) return false

        when (owned) {
            true -> if (book.editions.none { it.owned }) return false
            false -> if (book.editions.any { it.owned }) return false
            null -> Unit
        }

        if (ratingMin != null && book.rating < ratingMin) return false

        return true
    }

    /**
     * Edition-side filter for custom-list tabs. Book-level facets (tags, releaseYear, rating)
     * are resolved against the parent [book] when supplied; edition-level facets (format, owned)
     * are matched against the specific [edition].
     */
    fun matchesEdition(
        edition: BookEdition,
        book: Book?,
        userTags: List<UserTag>,
    ): Boolean {
        val tagsMatch = matchesTags(
            communityTags = book?.tags.orEmpty(),
            userTags = userTags,
        )

        if (tagsMatch.not()) return false

        if (formats.isNotEmpty() && edition.format !in formats) return false

        if (releaseYears.isNotEmpty()) {
            val year = book?.releaseYear ?: edition.releaseYear
            if (year !in releaseYears) return false
        }

        if (readYear != null && book?.finishedYear() != readYear) return false

        when (owned) {
            true -> if (edition.owned.not()) return false
            false -> if (edition.owned) return false
            null -> Unit
        }

        if (ratingMin != null) {
            val rating = book?.rating ?: 0.0
            if (rating < ratingMin) return false
        }

        return true
    }

    private fun matchesTags(
        communityTags: List<Tag>,
        userTags: List<UserTag>,
    ): Boolean {
        if (hardcoverTags.isEmpty.not()) {
            val carried = communityTags.mapTo(mutableSetOf()) { tag ->
                HardcoverTagKey(
                    id = tag.id,
                    name = tag.name,
                )
            }
            if (hardcoverTags.matches(carried = carried).not()) return false
        }

        if (yourTags.isEmpty.not()) {
            val carried = userTags.mapTo(mutableSetOf()) { tag ->
                UserTagKey(
                    category = tag.category,
                    name = tag.name,
                )
            }
            if (yourTags.matches(carried = carried).not()) return false
        }

        return true
    }
}
