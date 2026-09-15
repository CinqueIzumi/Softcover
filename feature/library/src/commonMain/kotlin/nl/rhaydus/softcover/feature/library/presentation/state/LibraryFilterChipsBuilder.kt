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
        ChipUiModel(
            key = "owned:true",
            label = "Owned",
        ) to LibraryFilterValue.Owned(owned = true),
        ChipUiModel(
            key = "owned:false",
            label = "Unowned",
        ) to LibraryFilterValue.Owned(owned = false),
    )
}

private fun formatChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> = options.formats.map { format ->
    ChipUiModel(
        key = "format:$format",
        label = format,
    ) to LibraryFilterValue.Format(value = format)
}

private fun releaseYearChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> = options.releaseYears.map { year ->
    ChipUiModel(
        key = "releaseYear:$year",
        label = year.toString(),
    ) to LibraryFilterValue.ReleaseYear(year = year)
}

private fun readYearChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> = options.readYears.map { year ->
    ChipUiModel(
        key = "readYear:$year",
        label = year.toString(),
    ) to LibraryFilterValue.ReadYear(year = year)
}

private fun tagChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> = options.tags.map { tag ->
    ChipUiModel(
        key = "tag:${tag.id}",
        label = tag.name,
    ) to LibraryFilterValue.Tag(tag = tag)
}

private fun ratingChipEntries(
    options: LibraryFilterOptions,
): List<Pair<ChipUiModel, LibraryFilterValue>> = options.ratingBuckets.map { threshold ->
    ChipUiModel(
        key = "rating:$threshold",
        label = formatRatingLabel(threshold = threshold),
    ) to LibraryFilterValue.RatingMin(threshold = threshold)
}

/**
 * The single source for a rating threshold's chip label — `LibraryFilterSheet` used to keep its own
 * copy, now dead since the render takes its labels straight off the built [ChipUiModel]s.
 */
private fun formatRatingLabel(threshold: Double): String {
    val rounded = if (threshold % 1.0 == 0.0) threshold.toInt().toString() else threshold.toString()

    return "$rounded★ and up"
}
