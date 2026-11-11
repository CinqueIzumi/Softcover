package nl.rhaydus.softcover.core.profile.domain.mapper

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice

private const val GENRES_PER_BOOK = 10

// Fractions are shares of the books with at least one genre, not of genre assignments; untagged books
// stay out of the denominator. A book counts with its top Hardcover genres plus the reader's own, deduped
// ignoring case so one genre is one slice, and the most common Hardcover spelling (ties by name) wins where both exist.
internal fun List<Book>.toGenreBreakdown(userTagsByBookId: Map<Int, List<UserTag>>): GenreBreakdown {
    val genreNamesPerBook = map { book ->
        book.hardcoverGenreNames() + userGenreNames(userTagsByBookId[book.id].orEmpty())
    }.map { names -> names.distinctBy { it.lowercase() } }
        .filter { it.isNotEmpty() }

    if (genreNamesPerBook.isEmpty()) return GenreBreakdown()

    val hardcoverSpellings = flatMap { it.hardcoverGenreNames() }
        .groupBy { it.lowercase() }
        .mapValues { (_, spellings) ->
            spellings
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedWith(
                    compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key },
                )
                .first()
                .key
        }

    val slices = genreNamesPerBook
        .flatten()
        .groupBy { it.lowercase() }
        .map { (key, spellings) ->
            GenreSlice(
                name = hardcoverSpellings[key] ?: spellings.first(),
                count = spellings.size,
                fraction = spellings.size.toDouble() / genreNamesPerBook.size,
            )
        }
        .sortedWith(
            compareByDescending<GenreSlice> { it.count }.thenBy { it.name },
        )

    return GenreBreakdown(slices = slices)
}

private fun Book.hardcoverGenreNames(): List<String> = tags
    .filter { it.category == TagCategory.GENRE }
    .sortedWith(
        compareByDescending<Tag> { it.count }.thenBy { it.name },
    )
    .take(GENRES_PER_BOOK)
    .map { it.name }

private fun userGenreNames(userTags: List<UserTag>): List<String> = userTags
    .filter { it.category == TagCategory.GENRE }
    .map { it.name }
