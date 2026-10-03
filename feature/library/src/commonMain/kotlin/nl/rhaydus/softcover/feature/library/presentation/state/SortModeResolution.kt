package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.presentation.model.LibraryTab

/** The sort mode in force for [tabId]: the user's choice, else the tab's default. */
internal fun resolveSortMode(
    sortModeByTab: Map<String, LibrarySortMode>,
    tabId: String,
): LibrarySortMode = sortModeByTab[tabId] ?: LibraryTab.defaultSortMode(tabId = tabId)
