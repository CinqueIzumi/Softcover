package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.ChipVariant

internal fun buildLibraryActiveFilterChips(
    filters: LibraryFilters,
): Pair<LibraryActiveFilterChips, Map<String, LibraryFilterValue>> {
    val entries = buildList {
        filters.tags.forEach { tag ->
            add(
                activeFilterChipEntry(
                    value = LibraryFilterValue.Tag(tag = tag),
                    label = tag.name,
                ),
            )
        }

        filters.formats.forEach { format ->
            add(
                activeFilterChipEntry(
                    value = LibraryFilterValue.Format(value = format),
                    label = format,
                ),
            )
        }

        filters.releaseYears.forEach { year ->
            add(
                activeFilterChipEntry(
                    value = LibraryFilterValue.ReleaseYear(year = year),
                    label = year.toString(),
                ),
            )
        }

        filters.readYear?.let { year ->
            add(
                activeFilterChipEntry(
                    value = LibraryFilterValue.ReadYear(year = year),
                    label = "Finished $year",
                ),
            )
        }

        filters.owned?.let { owned ->
            add(
                activeFilterChipEntry(
                    value = LibraryFilterValue.Owned(owned = owned),
                    label = if (owned) "Owned" else "Unowned",
                ),
            )
        }

        filters.ratingMin?.let { threshold ->
            add(
                activeFilterChipEntry(
                    value = LibraryFilterValue.RatingMin(threshold = threshold),
                    label = "${roundedRatingThreshold(threshold = threshold)}★+",
                ),
            )
        }
    }

    val valueByKey = entries.associate { (chip, value) -> chip.key to value }

    val clearAll = if (entries.size > 1) {
        ChipUiModel(
            key = LIBRARY_CLEAR_ALL_CHIP_KEY,
            label = "Clear all",
            variant = ChipVariant.Quiet(),
        )
    } else {
        null
    }

    val model = LibraryActiveFilterChips(
        chips = entries.map { it.first }.toImmutableList(),
        clearAll = clearAll,
    )

    return model to valueByKey
}

private fun activeFilterChipEntry(
    value: LibraryFilterValue,
    label: String,
): Pair<ChipUiModel, LibraryFilterValue> {
    val chip = ChipUiModel(
        key = value.chipKey(),
        label = label,
        variant = ChipVariant.Remove(removeLabel = "Remove filter $label"),
    )

    return chip to value
}
