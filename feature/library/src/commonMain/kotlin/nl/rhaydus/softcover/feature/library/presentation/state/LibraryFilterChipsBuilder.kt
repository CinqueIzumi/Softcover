package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipUiModel

/**
 * Pure, dispatcher-friendly builder mapping a tab's [LibraryFilterOptions] to [ChipUiModel]s
 * (`component-contract.md` § 7.2 R9) — kept top-level, like [buildBookFilterOptions], so
 * `FilterChipModelsCollector` can call it on `Dispatchers.Default` without keeping the whole UI state
 * on the worker thread.
 *
 * Every chip's key encodes the facet it belongs to (`"format:Hardcover"`, `"tag:42"`, …) so the
 * returned lookup map stays collision-free across facets while sharing one flat namespace per tab.
 */
internal fun buildLibraryFilterChips(
    options: LibraryFilterOptions,
): Pair<LibraryFilterChips, Map<String, LibraryFilterValue>> {
    val ownership = ownershipChipEntries(options = options)
    val formats = formatChipEntries(options = options)
    val releaseYears = releaseYearChipEntries(options = options)
    val readYears = readYearChipEntries(options = options)
    val tags = tagChipEntries(options = options)
    val ratings = ratingChipEntries(options = options)

    val valueByKey = (ownership + formats + releaseYears + readYears + tags + ratings)
        .associate { (chip, value) -> chip.key to value }

    val chips = LibraryFilterChips(
        ownershipChips = ownership.map { it.first },
        formatChips = formats.map { it.first },
        releaseYearChips = releaseYears.map { it.first },
        readYearChips = readYears.map { it.first },
        tagChips = tags.map { it.first },
        ratingChips = ratings.map { it.first },
    )

    return chips to valueByKey
}

private fun ownershipChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> {
    if (options.supportsOwnedFilter.not()) return emptyList()

    return listOf(
        LibraryFilterValue.Owned(owned = true).let { value ->
            ChipUiModel(
                key = value.chipKey(),
                label = "Owned",
            ) to value
        },
        LibraryFilterValue.Owned(owned = false).let { value ->
            ChipUiModel(
                key = value.chipKey(),
                label = "Unowned",
            ) to value
        },
    )
}

private fun formatChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> = options.formats.map { format ->
    val value = LibraryFilterValue.Format(value = format)

    ChipUiModel(
        key = value.chipKey(),
        label = format,
    ) to value
}

private fun releaseYearChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> = options.releaseYears.map { year ->
    val value = LibraryFilterValue.ReleaseYear(year = year)

    ChipUiModel(
        key = value.chipKey(),
        label = year.toString(),
    ) to value
}

private fun readYearChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> = options.readYears.map { year ->
    val value = LibraryFilterValue.ReadYear(year = year)

    ChipUiModel(
        key = value.chipKey(),
        label = year.toString(),
    ) to value
}

private fun tagChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> = options.tags.map { tag ->
    val value = LibraryFilterValue.Tag(tag = tag)

    ChipUiModel(
        key = value.chipKey(),
        label = tag.name,
    ) to value
}

private fun ratingChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> = options.ratingBuckets.map { threshold ->
    val value = LibraryFilterValue.RatingMin(threshold = threshold)

    ChipUiModel(
        key = value.chipKey(),
        label = formatRatingLabel(threshold = threshold),
    ) to value
}

/**
 * The single source for a rating threshold's chip label — `LibraryFilterSheet` used to keep its own
 * copy, now dead since the render takes its labels straight off the built [ChipUiModel]s.
 */
private fun formatRatingLabel(threshold: Double): String =
    "${roundedRatingThreshold(threshold = threshold)}★ and up"

/** Shared by [formatRatingLabel] and `LibraryActiveFilterChipsBuilder`'s active-chip label. */
internal fun roundedRatingThreshold(threshold: Double): String =
    if (threshold % 1.0 == 0.0) threshold.toInt().toString() else threshold.toString()
