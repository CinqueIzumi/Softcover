package nl.rhaydus.softcover.feature.explore.presentation.collector

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet

private const val RECENT_SEARCH_CHIP_KEY_PREFIX = "recent:"

internal data class RecentSearchChipsSnapshot(
    val previousSearchQueries: List<String>,
) {
    fun compute(): ChipSet<String> = previousSearchQueries.map { query ->
        val chip = ChipUiModel(
            key = RECENT_SEARCH_CHIP_KEY_PREFIX + query,
            label = query,
        )

        chip to query
    }.toChipSet()
}
