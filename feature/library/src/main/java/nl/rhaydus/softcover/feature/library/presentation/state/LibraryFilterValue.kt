package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Tag as DomainTag

internal sealed interface LibraryFilterValue {
    data class Tag(val tag: DomainTag) : LibraryFilterValue

    data class Format(val value: String) : LibraryFilterValue

    data class ReleaseYear(val year: Int) : LibraryFilterValue

    data class Owned(val owned: Boolean) : LibraryFilterValue

    data class RatingMin(val threshold: Double) : LibraryFilterValue
}

internal fun LibraryFilters.toggle(value: LibraryFilterValue): LibraryFilters = when (value) {
    is LibraryFilterValue.Tag -> copy(
        tags = tags.toggleElement(value.tag),
    )

    is LibraryFilterValue.Format -> copy(
        formats = formats.toggleElement(value.value),
    )

    is LibraryFilterValue.ReleaseYear -> copy(
        releaseYears = releaseYears.toggleElement(value.year),
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
