package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ColumnUiModel
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.profile.domain.model.DidNotFinishStats
import nl.rhaydus.softcover.core.profile.domain.model.StopQuarter
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.DidNotFinishUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.YourReadingListUiModel

private const val STOP_POINT_TITLE = "Where you stopped, by quarter of the book"
private const val GENRES_TITLE = "Genres"
private const val GENRES_NOTE = "A book can count in more than one genre."
private const val DESCRIPTION = "Putting a book down is part of reading."
private const val EMPTY_HEADLINE = "Books you didn't finish"
private const val EMPTY_LINE =
    "Books you mark Did not finish show up here, with where you stopped and their genres."
private const val GENRES_SHOWN = 5
private const val MIN_KNOWN_FOR_TREND = 2
private const val LOADING_COLUMN_FRACTION = 0.5f

/**
 * A null [stats] yields the stand-in chapter; with no book put down the header carries [EMPTY_LINE] and
 * the chapter has no section.
 */
internal fun yourReadingListUiModel(stats: DidNotFinishStats?): YourReadingListUiModel {
    if (stats == null) return loadingYourReadingList()

    if (stats.bookCount == 0) {
        return YourReadingListUiModel(
            header = yourReadingListHeader(
                headline = EMPTY_HEADLINE,
                description = EMPTY_LINE,
                isLoading = false,
            ),
            didNotFinish = null,
            isLoading = false,
        )
    }

    val known = stats.bookCount - stats.unknownStopCount

    return YourReadingListUiModel(
        header = yourReadingListHeader(
            headline = "${booksCount(stats.bookCount)} you didn't finish",
            description = DESCRIPTION + stats.dominantQuarterSentence(known),
            isLoading = false,
        ),
        didNotFinish = DidNotFinishUiModel(
            stopPointTitle = STOP_POINT_TITLE,
            stopPoint = stats.stopPointChart(),
            unknownStopCaption = unknownStopCaption(stats.unknownStopCount),
            genresTitle = GENRES_TITLE,
            genres = stats.genreChips(),
            genresNote = GENRES_NOTE,
        ),
        isLoading = false,
    )
}

private fun yourReadingListHeader(
    headline: String,
    description: String,
    isLoading: Boolean,
): SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
    eyebrow = atlasChapterEyebrow(AtlasChapter.YOUR_READING_LIST),
    headline = headline,
    description = description,
    isLoading = isLoading,
)

private fun booksCount(count: Int): String =
    if (count == 1) "1 book" else "${formatGroupedNumber(count)} books"

/** The sentence naming the quarter that holds more than half of the [known] stops, or empty. */
private fun DidNotFinishStats.dominantQuarterSentence(known: Int): String {
    if (known < MIN_KNOWN_FOR_TREND) return ""

    val (quarter, count) = quarterCounts.maxByOrNull { it.value } ?: return ""

    return if (count * 2 > known) " Most of these you stopped in the ${quarter.phrase()}." else ""
}

private fun DidNotFinishStats.stopPointChart(): ColumnChartUiModel? {
    val counts = StopQuarter.entries.map { quarterCounts[it] ?: 0 }
    val known = counts.sum()

    if (known == 0) return null

    val peak = peakIndex(counts)

    return ColumnChartUiModel(
        columns = StopQuarter.entries.mapIndexed { index, quarter ->
            val share = counts[index].toFloat() / known
            val percent = percentLabel(fraction = share)

            ColumnUiModel(
                fraction = share,
                tone = if (index == peak) ChartTone.Lead else ChartTone.Second,
                label = quarter.label(),
                value = percent,
                contentDescription = stopReadout(
                    quarter = quarter,
                    count = counts[index],
                    percent = percent,
                ),
            )
        }.toImmutableList(),
        showsValues = true,
    )
}

/** The spoken column: the quarter, its book count and its share, matching the time-of-day readout. */
private fun stopReadout(
    quarter: StopQuarter,
    count: Int,
    percent: String,
): String {
    val books = if (count == 0) "no books" else booksCount(count)

    return "${quarter.phrase().replaceFirstChar { it.uppercase() }}, $books, $percent"
}

private fun unknownStopCaption(count: Int): String? = when {
    count <= 0 -> null
    count == 1 -> "1 book has no stop point."
    else -> "${formatGroupedNumber(count)} books have no stop point."
}

private fun DidNotFinishStats.genreChips(): ChipSet<Unit>? = genres.slices
    .take(GENRES_SHOWN)
    .map { slice ->
        ChipUiModel(
            key = slice.name,
            label = slice.name,
            trailing = ChipTrailing.Count(text = formatGroupedNumber(slice.count)),
            interaction = ChipInteraction.Inert,
            contentDescription = "${slice.name}, ${booksCount(slice.count)}",
        ) to Unit
    }
    .toChipSet()
    .takeUnless { it.isEmpty }

/** The short column label, which fits four phone-width columns. */
private fun StopQuarter.label(): String = when (this) {
    StopQuarter.FIRST -> "First"
    StopQuarter.SECOND -> "Second"
    StopQuarter.THIRD -> "Third"
    StopQuarter.LAST -> "Last"
}

/** The quarter as it reads in a sentence, such as "first quarter". */
private fun StopQuarter.phrase(): String = "${label().lowercase()} quarter"

private fun loadingYourReadingList(): YourReadingListUiModel = YourReadingListUiModel(
    header = yourReadingListHeader(
        headline = "0 books you didn't finish",
        description = DESCRIPTION,
        isLoading = true,
    ),
    didNotFinish = DidNotFinishUiModel(
        stopPointTitle = STOP_POINT_TITLE,
        stopPoint = ColumnChartUiModel(
            columns = StopQuarter.entries.map { quarter ->
                ColumnUiModel(
                    fraction = LOADING_COLUMN_FRACTION,
                    tone = ChartTone.Second,
                    label = quarter.label(),
                    value = "",
                    contentDescription = "",
                )
            }.toImmutableList(),
            showsValues = true,
            isLoading = true,
        ),
        unknownStopCaption = null,
        genresTitle = GENRES_TITLE,
        genres = null,
        genresNote = GENRES_NOTE,
    ),
    isLoading = true,
)
