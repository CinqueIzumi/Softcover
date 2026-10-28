package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chart.RankedBarUiModel
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter

/** Genres shown before any "Show more". */
internal const val COLLAPSED_GENRE_COUNT = 5

/** Genres each "Show more" adds. */
internal const val GENRE_REVEAL_STEP = 10

private const val LOADING_ROW_COUNT = 3
private const val GENRE_SHOW_MORE_KEY = "genre-ranking-show-more"
private const val GENRE_SHOW_LESS_KEY = "genre-ranking-show-less"
private const val EMPTY_HEADLINE = "The genres you read most"
private const val LOADING_HEADLINE = "Mostly fantasy"
private const val LOADING_DESCRIPTION = "62% of your books were fantasy. Each book counts with its top 10 genres " +
    "on Hardcover plus any you added yourself, so the shares add up to more than 100%."
private val GENRES_EYEBROW = atlasChapterEyebrow(AtlasChapter.GENRES_AND_HOW_YOU_READ)

/** The masked header shown until the first stats land. */
internal fun loadingGenreHeader(): SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
    eyebrow = GENRES_EYEBROW,
    headline = LOADING_HEADLINE,
    description = LOADING_DESCRIPTION,
    isLoading = true,
)

/**
 * The chapter header, headlined by the top genre. A null [slices] yields the masked header and an empty
 * one says [emptyCaption].
 */
internal fun genreHeader(
    slices: List<GenreSlice>?,
    emptyCaption: String,
): SectionHeaderUiModel.Section {
    if (slices == null) return loadingGenreHeader()

    val lead = slices.firstOrNull() ?: return SectionHeaderUiModel.Section(
        eyebrow = GENRES_EYEBROW,
        headline = EMPTY_HEADLINE,
        description = emptyCaption,
    )

    return SectionHeaderUiModel.Section(
        eyebrow = GENRES_EYEBROW,
        headline = "Mostly ${lead.name.lowercasedGenre()}",
        description = "${percentLabel(fraction = lead.fraction.toFloat())} of your books were " +
            "${lead.name.lowercasedGenre()}. Each book counts with its top 10 genres on Hardcover plus any " +
            "you added yourself, so the shares add up to more than 100%.",
    )
}

/**
 * One row per genre, filled relative to the lead, of which the first [shownCount] are visible. A null
 * [slices] yields the loading rows; a ranking shorter than [shownCount] just shows every genre.
 */
internal fun genreRankingUiModel(
    slices: List<GenreSlice>?,
    shownCount: Int,
): RankedBarsUiModel {
    if (slices == null) {
        return RankedBarsUiModel(
            rows = List(LOADING_ROW_COUNT) {
                RankedBarUiModel(
                    label = "",
                    share = "",
                    fill = 0f,
                )
            }.toImmutableList(),
            visibleCount = LOADING_ROW_COUNT,
            isLoading = true,
        )
    }

    val leadFraction = slices.firstOrNull()?.fraction ?: 0.0

    return RankedBarsUiModel(
        rows = slices
            .map { slice ->
                RankedBarUiModel(
                    label = slice.name,
                    share = percentLabel(fraction = slice.fraction.toFloat()),
                    fill = if (leadFraction > 0.0) (slice.fraction / leadFraction).toFloat() else 0f,
                )
            }
            .toImmutableList(),
        visibleCount = shownCount.coerceAtMost(slices.size),
    )
}

/** The "Show more" button, present only while [slices] holds genres beyond [shownCount]. */
internal fun genreRankingShowMore(
    slices: List<GenreSlice>?,
    shownCount: Int,
): PillButtonUiModel? {
    if (shownCount >= (slices?.size ?: 0)) return null

    return PillButtonUiModel(
        key = GENRE_SHOW_MORE_KEY,
        content = PillButtonContent.Label(text = "Show more"),
        tone = PillButtonTone.Outlined,
    )
}

/** The "Show less" button, present only while more than the collapsed five genres are shown. */
internal fun genreRankingShowLess(
    slices: List<GenreSlice>?,
    shownCount: Int,
): PillButtonUiModel? {
    if (shownCount.coerceAtMost(slices?.size ?: 0) <= COLLAPSED_GENRE_COUNT) return null

    return PillButtonUiModel(
        key = GENRE_SHOW_LESS_KEY,
        content = PillButtonContent.Label(text = "Show less"),
        tone = PillButtonTone.Outlined,
    )
}

/** What the genre section says in place of its bars: the scope has no finished books, or none with a tag. */
internal fun genreEmptyCaption(
    booksRead: Int,
    scope: StatsScope = StatsScope.AllTime,
): String = scope.noFinishedBooksSentence(booksRead = booksRead)
    ?: "Finish a few books and your top genres show up here."

/** Lowercases each word except an all-caps one such as "LGBTQ+". */
private fun String.lowercasedGenre(): String = split(" ").joinToString(separator = " ") { word ->
    val letters = word.filter { it.isLetter() }

    if (letters.isNotEmpty() && letters.all { it.isUpperCase() }) word else word.lowercase()
}
