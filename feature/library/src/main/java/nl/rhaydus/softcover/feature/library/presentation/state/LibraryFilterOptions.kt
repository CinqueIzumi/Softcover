package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Tag

/**
 * The set of filter values actually present in the source list for a tab. The picker only ever
 * offers options that would narrow the visible set — no empty-result chips. Rating buckets are
 * derived (not raw values) because per-book ratings are continuous; the buckets express the
 * common "show me ≥X★" question instead of forcing the user to pick a precise threshold.
 */
data class LibraryFilterOptions(
    val tags: List<Tag> = emptyList(),
    val formats: List<String> = emptyList(),
    val releaseYears: List<Int> = emptyList(),
    val supportsOwnedFilter: Boolean = false,
    val ratingBuckets: List<Double> = emptyList(),
) {
    val isEmpty: Boolean
        get() = tags.isEmpty() &&
            formats.isEmpty() &&
            releaseYears.isEmpty() &&
            supportsOwnedFilter.not() &&
            ratingBuckets.isEmpty()
}
