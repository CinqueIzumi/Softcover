package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberSuffix
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.uibinding.masthead.mastheadRefreshFor
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryTabStats

internal val MastheadSubtitleStandIn: PageMastheadSubtitle = countsSubtitle(
    bookCount = 128,
    totalPages = 34_000,
    isLoading = true,
)

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
                MastheadSubtitleStandIn
            } else {
                subtitleFor(
                    tab = tab,
                    bookCount = stats.itemCount,
                    totalPages = stats.totalPages,
                )
            },
            isLoading = stats == null,
        )
    }.toImmutableMap()

    fun fallbackMasthead(): PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = eyebrow(),
        title = "Library",
        subtitle = MastheadSubtitleStandIn,
        isLoading = true,
    )

    fun refresh(): MastheadRefreshUiModel = mastheadRefreshFor(isLoading = isLoading)

    private fun eyebrow(): String = if (isLoading) "Refreshing your shelf…" else "Your collection"
}

private fun subtitleFor(
    tab: LibraryTab,
    bookCount: Int,
    totalPages: Int,
): PageMastheadSubtitle {
    if (bookCount == 0) {
        return PageMastheadSubtitle.Text(
            text = when {
                tab is LibraryTab.Status && tab.status == UserBookStatus.READ ->
                    "Nothing finished yet — the page is still open."
                tab is LibraryTab.Status && tab.status == UserBookStatus.WANT_TO_READ ->
                    "No titles set aside — discover one next."
                tab is LibraryTab.Status && tab.status == UserBookStatus.CURRENTLY_READING ->
                    "No book open — pick one up."
                else -> "No titles yet — your story starts here."
            },
        )
    }

    return countsSubtitle(
        bookCount = bookCount,
        totalPages = totalPages,
        isLoading = false,
    )
}

private fun countsSubtitle(
    bookCount: Int,
    totalPages: Int,
    isLoading: Boolean,
): PageMastheadSubtitle.Counts = PageMastheadSubtitle.Counts(
    titles = StatNumberUiModel(
        value = bookCount.toDouble(),
        format = StatNumberFormat.Plain,
        isLoading = isLoading,
        variant = StatNumberVariant.Body,
        suffix = StatNumberSuffix.Plural(
            one = " title",
            other = " titles",
        ),
    ),
    pages = StatNumberUiModel(
        value = totalPages.toDouble(),
        isLoading = isLoading,
        variant = StatNumberVariant.Body,
        prefix = " · ",
        suffix = StatNumberSuffix.Plural(
            one = " page",
            other = " pages",
        ),
    ).takeIf { totalPages > 0 },
)
