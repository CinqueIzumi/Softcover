package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.feature.library.presentation.state.LibraryActiveFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.buildLibraryActiveFilterChips

internal data class ActiveFilterChipsSnapshot(
    val filtersByTab: Map<String, LibraryFilters>,
) {
    fun compute(): Pair<Map<String, LibraryActiveFilterChips>, Map<String, LibraryFilterValue>> {
        val perTab = filtersByTab.mapValues { (_, filters) -> buildLibraryActiveFilterChips(filters = filters) }

        val chipsByTab = perTab.mapValues { it.value.first }
        val valueByKey = perTab.values.fold(emptyMap<String, LibraryFilterValue>()) { acc, (_, values) -> acc + values }

        return chipsByTab to valueByKey
    }
}
