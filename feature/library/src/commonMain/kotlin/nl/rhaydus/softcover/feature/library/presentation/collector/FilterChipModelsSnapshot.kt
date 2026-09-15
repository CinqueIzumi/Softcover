package nl.rhaydus.softcover.feature.library.presentation.collector

import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterChips
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterOptions
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterValue
import nl.rhaydus.softcover.feature.library.presentation.state.buildLibraryFilterChips

/**
 * The state field the Filter sheet's chip models are derived from. [compute] gates on
 * `distinctUntilChanged` upstream in `FilterChipModelsCollector`, so its outputs
 * (`filterChipsByTab`, `filterValueByChipKey`) are deliberately absent from this bundle — a
 * self-write can't retrigger the derivation. Mirrors [FilterOptionsInputs]'s shape.
 */
internal data class FilterChipModelsSnapshot(
    val filterOptionsByTab: Map<String, LibraryFilterOptions>,
) {
    fun compute(): Pair<Map<String, LibraryFilterChips>, Map<String, LibraryFilterValue>> {
        val perTab = filterOptionsByTab.mapValues { (_, options) -> buildLibraryFilterChips(options = options) }

        val chipsByTab = perTab.mapValues { it.value.first }
        // Flattened across tabs on purpose: a chip key already names its own facet
        // (`"format:Hardcover"`, `"tag:42"`), so the same key from two tabs is the same filter value
        // and collapsing them loses nothing. A facet whose keys were *not* namespaced would silently
        // let the last tab win here — so a new facet keys itself the way the others do.
        val valueByKey = perTab.values.fold(emptyMap<String, LibraryFilterValue>()) { acc, (_, values) -> acc + values }

        return chipsByTab to valueByKey
    }
}
