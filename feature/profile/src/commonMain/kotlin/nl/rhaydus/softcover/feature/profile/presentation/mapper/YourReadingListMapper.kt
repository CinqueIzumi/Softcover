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
import nl.rhaydus.softcover.core.component.statistic.CountRowUiModel
import nl.rhaydus.softcover.core.component.statistic.CountRowsUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.DidNotFinishStats
import nl.rhaydus.softcover.core.profile.domain.model.ReadingListStats
import nl.rhaydus.softcover.core.profile.domain.model.StopQuarter
import nl.rhaydus.softcover.core.uibinding.didnotfinish.toLabel
import nl.rhaydus.softcover.core.uibinding.format.percentLabel
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.DidNotFinishUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.VersusCellUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.YourReadingListUiModel
import kotlin.math.roundToInt

private const val STILL_TO_COME = "Still to come"
private const val SET_ASIDE_EYEBROW = "Set aside"
private const val ADDED_EYEBROW = "Added"
private const val ADDED_CAPTION = "to Want to Read"
private const val READ_EYEBROW = "Read"
private const val READ_CAPTION = "books finished"
private const val TIME_ON_LIST_EYEBROW = "Time on the list"
private const val NO_WAIT_ALL_TIME = "Start a book from your list and the wait shows up here."
private const val DAYS_PER_WEEK = 7.0
private const val DAYS_PER_MONTH = 30.44
private const val DAYS_PER_YEAR = 365.25
private const val DAYS_BEFORE_WEEKS = 14
private const val DAYS_BEFORE_MONTHS = 56
private const val DAYS_BEFORE_YEARS = 730
private const val STOP_POINT_TITLE = "Where you stopped, by quarter of the book"
private const val WHY_TITLE = "Why"
private const val WHY_EMPTY_LINE = "Reasons you add on a book's page gather here."
private const val WHY_OTHER_LABEL = "Something else"
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
 * The Still to come parts load until both [stats] and [booksRead] are present, and the Set aside parts
 * until [didNotFinish] is. With no book put down the set-aside header carries [EMPTY_LINE] and the chapter
 * has no section under it.
 */
internal fun yourReadingListUiModel(
    stats: ReadingListStats?,
    booksRead: Int?,
    didNotFinish: DidNotFinishStats?,
    scope: StatsScope,
): YourReadingListUiModel {
    val setAside = setAside(didNotFinish)

    if (stats == null || booksRead == null) {
        return stillToComeLoading().copy(
            setAside = setAside.first,
            didNotFinish = setAside.second,
        )
    }

    return YourReadingListUiModel(
        header = yourReadingListHeader(
            description = framingSentence(
                added = stats.addedCount,
                read = booksRead,
                scope = scope,
            ),
            isLoading = false,
        ),
        added = VersusCellUiModel(
            eyebrow = ADDED_EYEBROW,
            stat = countStat(stats.addedCount),
            caption = ADDED_CAPTION,
            isLead = false,
        ),
        read = VersusCellUiModel(
            eyebrow = READ_EYEBROW,
            stat = countStat(booksRead),
            caption = READ_CAPTION,
            isLead = true,
        ),
        timeOnList = timeOnList(
            stats = stats,
            scope = scope,
        ),
        setAside = setAside.first,
        didNotFinish = setAside.second,
    )
}

private fun countStat(count: Int) = StatNumberUiModel(value = count.toDouble())

private fun framingSentence(
    added: Int,
    read: Int,
    scope: StatsScope,
): String {
    val phrase = scope.scopePhrase()?.let { " $it" }.orEmpty()

    return when {
        added == 0 && read == 0 -> "Books you add to your list and finish$phrase show up here."
        added == 0 -> "You read ${booksCount(read)}$phrase and added none to your list."
        read == 0 -> "You added ${booksCount(added)} to your list$phrase."
        else -> "You added ${booksCount(added)} to your list$phrase and read ${formatGroupedNumber(read)}."
    }
}

private fun timeOnList(
    stats: ReadingListStats,
    scope: StatsScope,
): StatTileUiModel {
    val waitDays = stats.waitDays

    if (waitDays == null) {
        return StatTileUiModel(
            eyebrow = TIME_ON_LIST_EYEBROW,
            stat = null,
            variant = StatTileVariant.Compact(
                unit = "days",
                caption = scope.scopePhrase()?.let { "No book from your list was started $it." }
                    ?: NO_WAIT_ALL_TIME,
            ),
        )
    }

    val (number, unit) = waitDays.asDuration()

    val books = if (stats.waitedCount == 1) "the one book" else "the ${formatGroupedNumber(stats.waitedCount)} books"

    return StatTileUiModel(
        eyebrow = TIME_ON_LIST_EYEBROW,
        stat = StatNumberUiModel(
            value = number.toDouble(),
            format = StatNumberFormat.Plain,
        ),
        variant = StatTileVariant.Compact(
            unit = unit,
            caption = "Typical wait from adding a book to starting it, for $books you started.",
        ),
    )
}

/** The wait in the coarsest unit that keeps the number small, such as 3 and "months". */
private fun Int.asDuration(): Pair<Int, String> {
    val (number, one, other) = when {
        this < DAYS_BEFORE_WEEKS -> Triple(
            this,
            "day",
            "days",
        )
        this < DAYS_BEFORE_MONTHS -> Triple(
            (this / DAYS_PER_WEEK).roundToInt(),
            "week",
            "weeks",
        )
        this < DAYS_BEFORE_YEARS -> Triple(
            (this / DAYS_PER_MONTH).roundToInt(),
            "month",
            "months",
        )
        else -> Triple(
            (this / DAYS_PER_YEAR).roundToInt(),
            "year",
            "years",
        )
    }

    return number to if (number == 1) one else other
}

/** The set-aside header with its section; the section is null when nothing was put down. */
private fun setAside(stats: DidNotFinishStats?): Pair<SectionHeaderUiModel.Section, DidNotFinishUiModel?> {
    if (stats == null) return setAsideHeader(
        headline = "0 books you didn't finish",
        description = DESCRIPTION,
        isLoading = true,
    ) to loadingDidNotFinish()

    if (stats.bookCount == 0) {
        return setAsideHeader(
            headline = EMPTY_HEADLINE,
            description = EMPTY_LINE,
            isLoading = false,
        ) to null
    }

    val known = stats.bookCount - stats.unknownStopCount

    return setAsideHeader(
        headline = "${booksCount(stats.bookCount)} you didn't finish",
        description = DESCRIPTION + stats.dominantQuarterSentence(known),
        isLoading = false,
    ) to DidNotFinishUiModel(
        stopPointTitle = STOP_POINT_TITLE,
        stopPoint = stats.stopPointChart(),
        unknownStopCaption = unknownStopCaption(stats.unknownStopCount),
        whyTitle = WHY_TITLE,
        why = stats.whyRows(),
        whyEmptyLine = WHY_EMPTY_LINE,
        genresTitle = GENRES_TITLE,
        genres = stats.genreChips(),
        genresNote = GENRES_NOTE,
    )
}

private fun yourReadingListHeader(
    description: String,
    isLoading: Boolean,
): SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
    eyebrow = atlasChapterEyebrow(AtlasChapter.YOUR_READING_LIST),
    headline = STILL_TO_COME,
    description = description,
    isLoading = isLoading,
)

private fun setAsideHeader(
    headline: String,
    description: String,
    isLoading: Boolean,
): SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
    eyebrow = SET_ASIDE_EYEBROW,
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

private fun DidNotFinishStats.whyRows(): CountRowsUiModel? {
    val rows = reasonCounts.entries
        .filter { it.value > 0 }
        .sortedByDescending { it.value }
        .map { (reason, count) ->
            CountRowUiModel(
                label = reason.toLabel(),
                count = formatGroupedNumber(count),
            )
        } + listOfNotNull(
        otherReasonCount.takeIf { it > 0 }?.let {
            CountRowUiModel(
                label = WHY_OTHER_LABEL,
                count = formatGroupedNumber(it),
            )
        },
    )

    return if (rows.isEmpty()) null else CountRowsUiModel(rows = rows.toImmutableList())
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

private fun stillToComeLoading(): YourReadingListUiModel = YourReadingListUiModel(
    header = yourReadingListHeader(
        description = "You added 0 books to your list and read 0.",
        isLoading = true,
    ),
    added = loadingCell(
        eyebrow = ADDED_EYEBROW,
        caption = ADDED_CAPTION,
        isLead = false,
    ),
    read = loadingCell(
        eyebrow = READ_EYEBROW,
        caption = READ_CAPTION,
        isLead = true,
    ),
    timeOnList = StatTileUiModel(
        eyebrow = TIME_ON_LIST_EYEBROW,
        stat = StatNumberUiModel(
            value = 0.0,
            format = StatNumberFormat.Plain,
            isLoading = true,
        ),
        variant = StatTileVariant.Compact(
            unit = "months",
            caption = "Typical wait from adding a book to starting it, for the 0 books you started.",
        ),
    ),
    setAside = setAsideHeader(
        headline = "0 books you didn't finish",
        description = DESCRIPTION,
        isLoading = true,
    ),
    didNotFinish = null,
)

private fun loadingCell(
    eyebrow: String,
    caption: String,
    isLead: Boolean,
) = VersusCellUiModel(
    eyebrow = eyebrow,
    stat = StatNumberUiModel(
        value = 0.0,
        isLoading = true,
    ),
    caption = caption,
    isLead = isLead,
)

private fun loadingDidNotFinish(): DidNotFinishUiModel = DidNotFinishUiModel(
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
    whyTitle = WHY_TITLE,
    why = null,
    whyEmptyLine = null,
    genresTitle = GENRES_TITLE,
    genres = null,
    genresNote = GENRES_NOTE,
)
