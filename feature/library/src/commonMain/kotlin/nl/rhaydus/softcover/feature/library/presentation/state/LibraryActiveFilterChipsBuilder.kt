package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

internal fun buildLibraryActiveFilterChips(
    filters: LibraryFilters,
): LibraryActiveFilterChips {
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

    val clearAll = if (entries.size > 1) {
        ChipUiModel(
            key = LIBRARY_CLEAR_ALL_CHIP_KEY,
            label = "Clear all",
            tone = ChipTone.Tonal,
        )
    } else {
        null
    }

    return LibraryActiveFilterChips(
        chips = entries.toChipSet(),
        clearAll = clearAll,
    )
}

private fun activeFilterChipEntry(
    value: LibraryFilterValue,
    label: String,
): Pair<ChipUiModel, LibraryFilterValue> {
    val chip = ChipUiModel(
        key = value.chipKey(),
        label = label,
        tone = ChipTone.Container,
        trailing = ChipTrailing.Icon(
            icon = SoftcoverIcon.Close,
            description = "Remove filter $label",
        ),
    )

    return chip to value
}
