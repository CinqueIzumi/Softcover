package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.feature.library.presentation.state.LibraryActiveFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilters
import nl.rhaydus.softcover.feature.library.presentation.state.buildLibraryActiveFilterChips

internal data class ActiveFilterChipsSnapshot(
    val filtersByTab: Map<String, LibraryFilters>,
) {
    fun compute(): Map<String, LibraryActiveFilterChips> =
        filtersByTab.mapValues { (_, filters) -> buildLibraryActiveFilterChips(filters = filters) }
}
