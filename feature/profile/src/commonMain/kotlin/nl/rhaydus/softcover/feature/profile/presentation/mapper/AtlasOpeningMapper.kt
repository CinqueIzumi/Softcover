package nl.rhaydus.softcover.feature.profile.presentation.mapper

import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasOpeningUiModel

/** A null [stats] yields the loading tiles; the hero caption names [scope] unless it is every year. */
internal fun atlasOpeningUiModel(
    stats: ReadingStats?,
    scope: StatsScope = StatsScope.AllTime,
): AtlasOpeningUiModel {
    val isLoading = stats == null

    return AtlasOpeningUiModel(
        hero = StatTileUiModel(
            eyebrow = "Pages read",
            stat = StatNumberUiModel(
                value = (stats?.totalPages ?: 0).toDouble(),
                isLoading = isLoading,
            ),
            variant = StatTileVariant.Hero(
                caption = finishedBooksCaption(
                    booksRead = stats?.booksRead,
                    scope = scope,
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

private fun finishedBooksCaption(
    booksRead: Int?,
    scope: StatsScope,
): String {
    val phrase = scope.scopePhrase()

    return when {
        booksRead == null -> "From your finished books."
        booksRead == 0 && phrase != null -> "No books finished $phrase."
        booksRead == 0 -> "No finished books yet."
        else -> {
            val books = if (booksRead == 1) "1 finished book" else "${formatGroupedNumber(booksRead)} finished books"

            "From $books${phrase?.let { " $it" }.orEmpty()}."
        }
    }
}
