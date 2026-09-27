package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipUiModel

private const val RECENT_SEARCH_CHIP_KEY_PREFIX = "recent:"

internal data class RecentSearchChipsSnapshot(
    val previousSearchQueries: List<String>,
) {
    fun compute(): Pair<ImmutableList<ChipUiModel>, Map<String, String>> {
        val entries = previousSearchQueries.map { query ->
            val chip = ChipUiModel(
                key = RECENT_SEARCH_CHIP_KEY_PREFIX + query,
                label = query,
            )

            chip to query
        }

        val chips = entries.map { it.first }.toImmutableList()
        val queryByKey = entries.associate { (chip, query) -> chip.key to query }

        return chips to queryByKey
    }
}
