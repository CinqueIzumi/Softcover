package nl.rhaydus.softcover.feature.library.presentation.screen

import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab as LibraryContentTab
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount
import nl.rhaydus.softcover.feature.library.presentation.util.formatPageCount

internal fun subtitleFor(
    tab: LibraryContentTab?,
    bookCount: Int?,
    totalPages: Int,
): String {
    if (bookCount == null) return "Loading your shelf…"
    if (bookCount == 0) {
        return when {
            tab is LibraryContentTab.Status && tab.status == UserBookStatus.READ ->
                "Nothing finished yet — the page is still open."
            tab is LibraryContentTab.Status && tab.status == UserBookStatus.WANT_TO_READ ->
                "No titles set aside — discover one next."
            tab is LibraryContentTab.Status && tab.status == UserBookStatus.CURRENTLY_READING ->
                "No book open — pick one up."
            else -> "No titles yet — your story starts here."
        }
    }

    val titlesPart = formatBookCount(count = bookCount)
    val pagesPart = formatPageCount(pages = totalPages)

    return if (pagesPart != null) "$titlesPart · $pagesPart" else titlesPart
}

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
