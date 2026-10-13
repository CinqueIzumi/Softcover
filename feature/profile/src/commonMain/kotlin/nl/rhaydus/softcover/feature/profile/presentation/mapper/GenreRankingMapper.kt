package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.chart.ChartSliceUiModel
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.uibinding.format.percentLabel

private const val RANKED_GENRE_COUNT = 5
private const val LOADING_ROW_COUNT = 3

/** Ranks the top five genres onto `Lead` to `Fifth` by position. A null [genres] yields the loading rows. */
internal fun genreRankingUiModel(genres: GenreBreakdown?): RankedBarsUiModel {
    if (genres == null) {
        return RankedBarsUiModel(
            rows = List(LOADING_ROW_COUNT) { index ->
                ChartSliceUiModel(
                    label = "",
                    share = "",
                    fraction = 0f,
                    tone = ChartTone.forRank(index),
                )
            }.toImmutableList(),
            isLoading = true,
        )
    }

    return RankedBarsUiModel(
        rows = genres.slices
            .take(RANKED_GENRE_COUNT)
            .mapIndexed { index, slice ->
                ChartSliceUiModel(
                    label = slice.name,
                    share = percentLabel(fraction = slice.fraction.toFloat()),
                    fraction = slice.fraction.toFloat(),
                    tone = ChartTone.forRank(index),
                )
            }
            .toImmutableList(),
    )
}

/**
 * Falls back to the scope-free wording when the count is 0 — that is either a genuinely empty library
 * or a profile cached by a build older than 3.1.1 (the stored count defaults to 0 and fills in on the
 * session's refresh), and naming "0 books" beneath five populated bars would be worse than saying
 * nothing.
 */
internal fun genreScopeCaption(taggedBookCount: Int): String {
    val scope = when (taggedBookCount) {
        0 -> "out of your finished books with genre tags"
        1 -> "out of 1 finished book with genre tags"
        else -> "out of ${formatGroupedNumber(taggedBookCount)} finished books with genre tags"
    }

    return "Your top five genres, $scope. " +
        "Books can have more than one genre, so these add up to more than 100%."
}
