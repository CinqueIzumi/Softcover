package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserTag

/** [books] must hold each book once. */
internal fun buildHardcoverTagOptions(books: List<Book>): ImmutableList<LibraryTagOption<HardcoverTagKey>> {
    val counts = linkedMapOf<HardcoverTagKey, LibraryTagOption<HardcoverTagKey>>()

    books.forEach { book ->
        book.tags.distinctBy { it.id }.forEach { tag ->
            val key = HardcoverTagKey(
                id = tag.id,
                name = tag.name,
            )
            val option = counts[key] ?: LibraryTagOption(
                key = key,
                category = tag.category,
                count = 0,
            )

            counts[key] = option.copy(count = option.count + 1)
        }
    }

    return counts.values.sortedForPicker()
}

/** [userTagsPerBook] must hold one entry per book. */
internal fun buildYourTagOptions(userTagsPerBook: List<List<UserTag>>): ImmutableList<LibraryTagOption<UserTagKey>> {
    val counts = linkedMapOf<UserTagKey, LibraryTagOption<UserTagKey>>()

    userTagsPerBook.forEach { userTags ->
        userTags.distinctBy { it.category to it.name }.forEach { tag ->
            val key = UserTagKey(
                category = tag.category,
                name = tag.name,
            )
            val option = counts[key] ?: LibraryTagOption(
                key = key,
                category = tag.category,
                count = 0,
            )

            counts[key] = option.copy(count = option.count + 1)
        }
    }

    return counts.values.sortedForPicker()
}

private fun <K : TagKey> Collection<LibraryTagOption<K>>.sortedForPicker(): ImmutableList<LibraryTagOption<K>> =
    sortedWith(
        compareByDescending<LibraryTagOption<K>> { it.count }
            .thenBy { it.key.name.lowercase() },
    ).toImmutableList()
