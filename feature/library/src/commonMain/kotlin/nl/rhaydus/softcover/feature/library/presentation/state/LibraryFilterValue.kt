package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.toImmutableSet

internal sealed interface LibraryFilterValue {
    data class HardcoverTag(
        val key: HardcoverTagKey,
        val direction: TagDirection,
    ) : LibraryFilterValue

    data class YourTag(
        val key: UserTagKey,
        val direction: TagDirection,
    ) : LibraryFilterValue

    data class Format(val value: String) : LibraryFilterValue

    data class ReleaseYear(val year: Int) : LibraryFilterValue

    /** Year-finished facet, offered on the Read tab only (see [LibraryFilterOptions.readYears]). */
    data class ReadYear(val year: Int) : LibraryFilterValue

    data class Owned(val owned: Boolean) : LibraryFilterValue

    data class RatingMin(val threshold: Double) : LibraryFilterValue
}

/** Clears the tag [value] names from its source, whichever direction it is currently chosen in. */
internal fun LibraryFilters.withoutTag(value: LibraryFilterValue): LibraryFilters = when (value) {
    is LibraryFilterValue.HardcoverTag -> copy(
        hardcoverTags = TagSelection(
            included = (hardcoverTags.included - value.key).toImmutableSet(),
            excluded = (hardcoverTags.excluded - value.key).toImmutableSet(),
        ),
    )

    is LibraryFilterValue.YourTag -> copy(
        yourTags = TagSelection(
            included = (yourTags.included - value.key).toImmutableSet(),
            excluded = (yourTags.excluded - value.key).toImmutableSet(),
        ),
    )

    else -> this
}

internal fun LibraryFilters.toggle(value: LibraryFilterValue): LibraryFilters = when (value) {
    is LibraryFilterValue.HardcoverTag -> copy(
        hardcoverTags = hardcoverTags.toggled(
            key = value.key,
            direction = value.direction,
        ),
    )

    is LibraryFilterValue.YourTag -> copy(
        yourTags = yourTags.toggled(
            key = value.key,
            direction = value.direction,
        ),
    )

    is LibraryFilterValue.Format -> copy(
        formats = formats.toggleElement(value.value).toImmutableSet(),
    )

    is LibraryFilterValue.ReleaseYear -> copy(
        releaseYears = releaseYears.toggleElement(value.year).toImmutableSet(),
    )

    is LibraryFilterValue.ReadYear -> copy(
        readYear = if (readYear == value.year) null else value.year,
    )

    is LibraryFilterValue.Owned -> copy(
        owned = if (owned == value.owned) null else value.owned,
    )

    is LibraryFilterValue.RatingMin -> copy(
        ratingMin = if (ratingMin == value.threshold) null else value.threshold,
    )
}

private fun <T> Set<T>.toggleElement(element: T): Set<T> =
    if (element in this) this - element else this + element
