package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.uibinding.masthead.mastheadRefreshFor
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTabStats
import nl.rhaydus.softcover.feature.library.presentation.util.formatBookCount
import nl.rhaydus.softcover.feature.library.presentation.util.formatPageCount

internal const val MASTHEAD_SUBTITLE_STAND_IN = "128 books · 34,000 pages"

internal data class MastheadSnapshot(
    val visibleTabs: List<LibraryTab>,
    val tabStatsByTab: Map<String, LibraryTabStats>,
    val isLoading: Boolean,
) {
    fun mastheads(): ImmutableMap<String, PageMastheadUiModel> = visibleTabs.associate { tab ->
        val stats = tabStatsByTab[tab.id]

        tab.id to PageMastheadUiModel(
            eyebrow = eyebrow(),
            title = tab.label,
            subtitle = if (stats == null) {
                MASTHEAD_SUBTITLE_STAND_IN
            } else {
                subtitleFor(
                    tab = tab,
                    bookCount = stats.itemCount,
                    totalPages = stats.totalPages,
                )
            },
        )
    }.toImmutableMap()

    fun fallbackMasthead(): PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = eyebrow(),
        title = "Library",
        subtitle = MASTHEAD_SUBTITLE_STAND_IN,
    )

    fun loadingTabIds(): ImmutableSet<String> = visibleTabs
        .map { it.id }
        .filter { it !in tabStatsByTab }
        .toImmutableSet()

    fun refresh(): MastheadRefreshUiModel = mastheadRefreshFor(isLoading = isLoading)

    private fun eyebrow(): String = if (isLoading) "Refreshing your shelf…" else "Your collection"
}

private fun subtitleFor(
    tab: LibraryTab,
    bookCount: Int,
    totalPages: Int,
): String {
    if (bookCount == 0) {
        return when {
            tab is LibraryTab.Status && tab.status == UserBookStatus.READ ->
                "Nothing finished yet — the page is still open."
            tab is LibraryTab.Status && tab.status == UserBookStatus.WANT_TO_READ ->
                "No titles set aside — discover one next."
            tab is LibraryTab.Status && tab.status == UserBookStatus.CURRENTLY_READING ->
                "No book open — pick one up."
            else -> "No titles yet — your story starts here."
        }
    }

    val titlesPart = formatBookCount(count = bookCount)
    val pagesPart = formatPageCount(pages = totalPages)

    return if (pagesPart != null) "$titlesPart · $pagesPart" else titlesPart
}
