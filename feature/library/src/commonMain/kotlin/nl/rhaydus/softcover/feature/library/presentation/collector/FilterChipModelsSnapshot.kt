package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterOptions
import nl.rhaydus.softcover.feature.library.presentation.state.buildLibraryFilterChips

/**
 * The state field the Filter sheet's chip models are derived from. [compute] gates on
 * `distinctUntilChanged` upstream in `FilterChipModelsCollector`, so its output (`filterChipsByTab`)
 * is deliberately absent from this bundle — a self-write can't retrigger the derivation. Mirrors
 * [FilterOptionsInputs]'s shape.
 */
internal data class FilterChipModelsSnapshot(
    val filterOptionsByTab: Map<String, LibraryFilterOptions>,
) {
    fun compute(): Map<String, LibraryFilterChips> =
        filterOptionsByTab.mapValues { (_, options) -> buildLibraryFilterChips(options = options) }
}
