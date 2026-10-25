package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.TagCategory

/**
 * One tag of a source, resolved against the draft: [direction] is how it is chosen, null when it is not.
 * [includeKey] and [excludeKey] are the chip keys that pick it in each direction. A chosen tag the shelf no
 * longer carries stays listed with [count] 0, and a null [category] when its key does not hold one.
 */
internal data class TagEntry(
    val name: String,
    val category: TagCategory?,
    val count: Int,
    val includeKey: String,
    val excludeKey: String,
    val direction: TagDirection?,
)

internal fun LibraryFilterOptions.tagEntries(
    source: TagSource,
    filters: LibraryFilters,
): List<TagEntry> = when (source) {
    TagSource.YOURS -> yourTags.toEntries(
        selection = filters.yourTags,
        categoryOf = { it.category },
        valueOf = { key, direction ->
            LibraryFilterValue.YourTag(
                key = key,
                direction = direction,
            )
        },
    )

    TagSource.HARDCOVER -> hardcoverTags.toEntries(
        selection = filters.hardcoverTags,
        categoryOf = { null },
        valueOf = { key, direction ->
            LibraryFilterValue.HardcoverTag(
                key = key,
                direction = direction,
            )
        },
    )
}

internal fun LibraryFilterOptions.tagValueFor(
    key: String,
    filters: LibraryFilters,
): LibraryFilterValue? {
    val yours = yourTags.map { it.key } + filters.yourTags.entries().map { it.first }
    val hardcover = hardcoverTags.map { it.key } + filters.hardcoverTags.entries().map { it.first }

    return TagDirection.entries.firstNotNullOfOrNull { direction ->
        yours.firstNotNullOfOrNull { tagKey ->
            LibraryFilterValue.YourTag(
                key = tagKey,
                direction = direction,
            ).takeIf { it.chipKey() == key }
        } ?: hardcover.firstNotNullOfOrNull { tagKey ->
            LibraryFilterValue.HardcoverTag(
                key = tagKey,
                direction = direction,
            ).takeIf { it.chipKey() == key }
        }
    }
}

private fun <K : TagKey> List<LibraryTagOption<K>>.toEntries(
    selection: TagSelection<K>,
    valueOf: (K, TagDirection) -> LibraryFilterValue,
    categoryOf: (K) -> TagCategory?,
): List<TagEntry> {
    val onShelf = map { option ->
        option.toEntry(
            selection = selection,
            valueOf = valueOf,
        )
    }
    val shelfKeys = map { it.key }.toSet()

    val gone = selection.entries()
        .filter { (key, _) -> key !in shelfKeys }
        .map { (key, direction) ->
            TagEntry(
                name = key.name,
                category = categoryOf(key),
                count = 0,
                includeKey = valueOf(
                    key,
                    TagDirection.INCLUDE,
                ).chipKey(),
                excludeKey = valueOf(
                    key,
                    TagDirection.EXCLUDE,
                ).chipKey(),
                direction = direction,
            )
        }

    return onShelf + gone
}

private fun <K : TagKey> LibraryTagOption<K>.toEntry(
    selection: TagSelection<K>,
    valueOf: (K, TagDirection) -> LibraryFilterValue,
): TagEntry = TagEntry(
    name = key.name,
    category = category,
    count = count,
    includeKey = valueOf(
        key,
        TagDirection.INCLUDE,
    ).chipKey(),
    excludeKey = valueOf(
        key,
        TagDirection.EXCLUDE,
    ).chipKey(),
    direction = when {
        key in selection.included -> TagDirection.INCLUDE
        key in selection.excluded -> TagDirection.EXCLUDE
        else -> null
    },
)
