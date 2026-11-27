package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.datetime.LocalDate
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.PagesRead
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasOpeningUiModel

/**
 * A null [stats] or [pagesRead] yields the loading tiles. The hero counts [pagesRead] and its caption
 * names [scope] unless it is every year; [today] decides whether a calendar-year scope is already over.
 */
internal fun atlasOpeningUiModel(
    stats: ReadingStats?,
    pagesRead: PagesRead? = null,
    scope: StatsScope = StatsScope.AllTime,
    today: LocalDate? = null,
): AtlasOpeningUiModel {
    val isLoading = stats == null

    return AtlasOpeningUiModel(
        hero = StatTileUiModel(
            eyebrow = "Pages read",
            stat = StatNumberUiModel(
                value = (pagesRead?.total ?: 0).toDouble(),
                isLoading = isLoading || pagesRead == null,
            ),
            variant = StatTileVariant.Hero(
                caption = pagesReadCaption(
                    pagesRead = pagesRead,
                    scope = scope,
                    isPastYear = scope is StatsScope.CalendarYear && today != null && scope.year < today.year,
                ),
            ),
        ),
        books = StatTileUiModel(
            eyebrow = "Books",
            stat = StatNumberUiModel(
                value = (stats?.booksRead ?: 0).toDouble(),
                isLoading = isLoading,
            ),
            variant = StatTileVariant.Standard(caption = "books finished"),
        ),
    )
}

private fun pagesReadCaption(
    pagesRead: PagesRead?,
    scope: StatsScope,
    isPastYear: Boolean,
): String {
    val phrase = scope.scopePhrase()

    if (pagesRead == null) return "From your reading."

    if (pagesRead.total == 0) return phrase?.let { "No pages read $it." } ?: "No pages read yet."

    val parts = buildList {
        addAll(
            finishedAndStoppedBookParts(
                finished = pagesRead.finishedBookCount,
                stopped = pagesRead.stoppedBookCount,
            ),
        )

        if (pagesRead.hasInProgressPages) add(
            inProgressPhrase(
                isOnlyPart = isEmpty(),
                isPastYear = isPastYear,
            ),
        )
    }

    val counts = when (parts.size) {
        0 -> "your reading"
        1 -> parts.first()
        else -> "${parts.dropLast(1).joinToString(", ")} and ${parts.last()}"
    }

    val pace = pagesRead.pagesPerDay?.let { ", about ${if (it == 1) "1 page" else "${formatGroupedNumber(it)} pages"} a day" }

    return "From $counts${phrase?.let { " $it" }.orEmpty()}${pace.orEmpty()}."
}

private fun inProgressPhrase(
    isOnlyPart: Boolean,
    isPastYear: Boolean,
): String {
    val subject = if (isOnlyPart) "the books" else "the ones"
    val verb = if (isPastYear) "you were reading" else "you're reading"

    return "$subject $verb"
}
