package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

internal fun buildLibraryActiveFilterChips(
    filters: LibraryFilters,
): LibraryActiveFilterChips {
    val entries = buildList {
        filters.yourTags.entries().forEach { (key, direction) ->
            add(
                activeFilterChipEntry(
                    value = LibraryFilterValue.YourTag(
                        key = key,
                        direction = direction,
                    ),
                    label = activeTagLabel(
                        name = key.name,
                        direction = direction,
                    ),
                    leading = ChipLeading.Icon(icon = SoftcoverIcon.Sell),
                    removeDescription = removeTagDescription(
                        source = "Your tag",
                        name = key.name,
                        direction = direction,
                    ),
                ),
            )
        }

        filters.hardcoverTags.entries().forEach { (key, direction) ->
            add(
                activeFilterChipEntry(
                    value = LibraryFilterValue.HardcoverTag(
                        key = key,
                        direction = direction,
                    ),
                    label = activeTagLabel(
                        name = key.name,
                        direction = direction,
                    ),
                    removeDescription = removeTagDescription(
                        source = "Hardcover tag",
                        name = key.name,
                        direction = direction,
                    ),
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
        )
    } else {
        null
    }

    return LibraryActiveFilterChips(
        chips = entries.toChipSet(),
        clearAll = clearAll,
    )
}

private fun activeTagLabel(
    name: String,
    direction: TagDirection,
): String = when (direction) {
    TagDirection.INCLUDE -> name
    TagDirection.EXCLUDE -> "Not $name"
}

private fun removeTagDescription(
    source: String,
    name: String,
    direction: TagDirection,
): String = when (direction) {
    TagDirection.INCLUDE -> "$source $name. Remove filter"
    TagDirection.EXCLUDE -> "Excluding ${source.replaceFirstChar { it.lowercase() }} $name. Remove filter"
}

private fun activeFilterChipEntry(
    value: LibraryFilterValue,
    label: String,
    leading: ChipLeading? = null,
    removeDescription: String = "Remove filter $label",
): Pair<ChipUiModel, LibraryFilterValue> {
    val chip = ChipUiModel(
        key = value.chipKey(),
        label = label,
        tone = ChipTone.Container,
        leading = leading,
        trailing = ChipTrailing.Icon(
            icon = SoftcoverIcon.Close,
            description = removeDescription,
        ),
    )

    return chip to value
}
