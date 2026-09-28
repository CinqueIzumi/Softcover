package nl.rhaydus.softcover.feature.library.presentation.screen

import nl.rhaydus.softcover.core.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * The Arrange/Filter sheets' footer "Show N titles" count — the current display list's size, falling
 * back to the tab's precomputed stats while the display list hasn't landed yet (cold start). Shared by
 * both the mobile and desktop layouts.
 */
internal fun LibraryContentTab.resultCountFor(state: LibraryUiState): Int =
    when (this) {
        is LibraryContentTab.CustomList -> state.displayEditionsFor(tabId = id)?.size
        is LibraryContentTab.All, is LibraryContentTab.Status -> state.displayBooksFor(tabId = id)?.size
    } ?: state.tabStatsFor(tabId = id).itemCount
