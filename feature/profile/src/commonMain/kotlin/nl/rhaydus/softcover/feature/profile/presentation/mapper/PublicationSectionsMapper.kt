package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chart.ChartSliceUiModel
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ColumnUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionChartVariant
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.profile.domain.model.AcquisitionSplit
import nl.rhaydus.softcover.core.profile.domain.model.LanguageSplit
import nl.rhaydus.softcover.core.profile.domain.model.PublicationDecades
import nl.rhaydus.softcover.core.profile.domain.model.PublicationStats
import nl.rhaydus.softcover.core.uibinding.acquisition.toLabel
import nl.rhaydus.softcover.feature.profile.presentation.state.ColumnSectionUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ProportionSectionUiModel

private const val LANGUAGES_TITLE = "Languages"
private const val LANGUAGES_CAPTION = "The language you read each book in."
private const val LANGUAGES_EMPTY = "This fills in as Hardcover learns the language of the editions you read."
private const val OTHER_LANGUAGES_LABEL = "Other languages"
private const val DECADES_TITLE = "When they were written"
private const val DECADES_EMPTY = "This fills in as Hardcover learns when your books came out."
private const val DECADES_STAND_IN = "Most of your books came out this century. The oldest is from 1871."
private const val OLDER_LABEL = "Older"
private const val ACQUISITION_TITLE = "Where you got them"
private const val ACQUISITION_EMPTY = "This fills in as you note where you got each book, under Your copy on its page."
private const val ACQUISITION_STAND_IN = "For the 30 books where you noted it."
private const val SOMEWHERE_ELSE_LABEL = "Somewhere else"
private const val NOT_NOTED_LABEL = "Not noted"
private const val UNKNOWN_LABEL = "Unknown"
private const val NAMED_LANGUAGE_COUNT = 4
private const val MAX_DECADE_COLUMNS = 6
private const val NEWEST_DECADE_COUNT = 5
private const val LOADING_SEGMENT_COUNT = 3
private const val LOADING_COLUMN_COUNT = 6
private const val LOADING_COLUMN_FRACTION = 0.5f

private data class ChartEntry(
    val label: String,
    val count: Int,
    val tone: ChartTone,
)

private data class DecadeBar(
    val label: String,
    val count: Int,
)

internal fun PublicationStats.hasUnknownBooks(): Boolean = languages.unknown > 0 ||
    decades.unknownYear > 0 ||
    acquisition.notNoted > 0

internal fun languagesSection(
    split: LanguageSplit,
    hideBooksWithoutDetail: Boolean,
    isFillingLanguages: Boolean,
): ProportionSectionUiModel {
    val entries = buildList {
        split.languages.take(NAMED_LANGUAGE_COUNT).forEachIndexed { rank, language ->
            add(
                ChartEntry(
                    label = language.name,
                    count = language.books,
                    tone = ChartTone.forRank(rank),
                ),
            )
        }

        val others = split.languages.drop(NAMED_LANGUAGE_COUNT).sumOf { it.books }

        if (others > 0) add(
            ChartEntry(
                label = OTHER_LANGUAGES_LABEL,
                count = others,
                tone = ChartTone.Fifth,
            ),
        )

        if (hideBooksWithoutDetail.not() && split.unknown > 0) {
            add(
                ChartEntry(
                    label = UNKNOWN_LABEL,
                    count = split.unknown,
                    tone = ChartTone.Unknown,
                ),
            )
        }
    }

    return ProportionSectionUiModel(
        title = LANGUAGES_TITLE,
        caption = LANGUAGES_CAPTION,
        emptyCaption = LANGUAGES_EMPTY,
        chart = if (isFillingLanguages) {
            loadingPublicationChart(ProportionChartVariant.Keys)
        } else {
            proportionChart(
                entries = entries,
                variant = ProportionChartVariant.Keys,
            )
        },
        hasData = split.languages.isNotEmpty(),
    )
}

internal fun decadesSection(
    decades: PublicationDecades,
    hideBooksWithoutDetail: Boolean,
): ColumnSectionUiModel {
    val known = decades.booksByDecade.entries
        .filter { it.value > 0 }
        .sortedBy { it.key }

    val bars = decadeBars(known.map { it.key to it.value })
    val unknown = decades.unknownYear.takeIf { hideBooksWithoutDetail.not() && it > 0 }
    val maxCount = (bars.map { it.count } + listOfNotNull(unknown)).maxOrNull() ?: 0

    val ranks = bars.indices
        .sortedWith(compareByDescending<Int> { bars[it].count }.thenBy { it })

    fun column(
        label: String,
        count: Int,
        tone: ChartTone,
    ) = ColumnUiModel(
        fraction = count.toFloat() / maxCount.coerceAtLeast(1),
        tone = tone,
        label = label,
        value = count.toString(),
        contentDescription = "$label, $count ${if (count == 1) "book" else "books"}",
    )

    val columns = bars.mapIndexed { index, bar ->
        column(
            label = bar.label,
            count = bar.count,
            tone = when (ranks.indexOf(index)) {
                0 -> ChartTone.Lead
                1 -> ChartTone.Second
                2 -> ChartTone.Third
                else -> ChartTone.Fourth
            },
        )
    } + listOfNotNull(
        unknown?.let {
            column(
                label = UNKNOWN_LABEL,
                count = it,
                tone = ChartTone.Unknown,
            )
        },
    )

    return ColumnSectionUiModel(
        title = DECADES_TITLE,
        caption = decadesCaption(
            known = known.map { it.key to it.value },
            decades = decades,
        ),
        emptyCaption = DECADES_EMPTY,
        chart = ColumnChartUiModel(
            columns = columns.toImmutableList(),
            showsValues = true,
        ),
        hasData = known.isNotEmpty(),
    )
}

internal fun acquisitionSection(
    split: AcquisitionSplit,
    hideBooksWithoutDetail: Boolean,
): ProportionSectionUiModel {
    val presets = split.byKind.entries
        .filter { it.value > 0 }
        .sortedWith(compareByDescending<Map.Entry<AcquisitionKind, Int>> { it.value }.thenBy { it.key.ordinal })

    val entries = buildList {
        presets.forEachIndexed { rank, preset ->
            add(
                ChartEntry(
                    label = preset.key.toLabel(),
                    count = preset.value,
                    tone = ChartTone.forRank(rank),
                ),
            )
        }

        if (split.somewhereElse > 0) {
            add(
                ChartEntry(
                    label = SOMEWHERE_ELSE_LABEL,
                    count = split.somewhereElse,
                    tone = ChartTone.Fifth,
                ),
            )
        }

        if (hideBooksWithoutDetail.not() && split.notNoted > 0) {
            add(
                ChartEntry(
                    label = NOT_NOTED_LABEL,
                    count = split.notNoted,
                    tone = ChartTone.Unknown,
                ),
            )
        }
    }

    val noted = presets.sumOf { it.value } + split.somewhereElse

    return ProportionSectionUiModel(
        title = ACQUISITION_TITLE,
        caption = acquisitionCaption(
            noted = noted,
            notNoted = split.notNoted.takeIf { hideBooksWithoutDetail.not() } ?: 0,
        ),
        emptyCaption = ACQUISITION_EMPTY,
        chart = proportionChart(
            entries = entries,
            variant = ProportionChartVariant.Rows,
        ),
        hasData = noted > 0,
    )
}

internal fun loadingLanguagesSection(): ProportionSectionUiModel = ProportionSectionUiModel(
    title = LANGUAGES_TITLE,
    caption = LANGUAGES_CAPTION,
    emptyCaption = "",
    chart = loadingPublicationChart(ProportionChartVariant.Keys),
    hasData = false,
)

internal fun loadingDecadesSection(): ColumnSectionUiModel = ColumnSectionUiModel(
    title = DECADES_TITLE,
    caption = DECADES_STAND_IN,
    emptyCaption = "",
    chart = ColumnChartUiModel(
        columns = List(LOADING_COLUMN_COUNT) {
            ColumnUiModel(
                fraction = LOADING_COLUMN_FRACTION,
                tone = ChartTone.Second,
                label = "",
                value = "",
                contentDescription = "",
            )
        }.toImmutableList(),
        showsValues = true,
        isLoading = true,
    ),
    hasData = false,
)

internal fun loadingAcquisitionSection(): ProportionSectionUiModel = ProportionSectionUiModel(
    title = ACQUISITION_TITLE,
    caption = ACQUISITION_STAND_IN,
    emptyCaption = "",
    chart = loadingPublicationChart(ProportionChartVariant.Rows),
    hasData = false,
)

private fun decadeBars(known: List<Pair<Int, Int>>): List<DecadeBar> {
    val newest = if (known.size > MAX_DECADE_COLUMNS) known.takeLast(NEWEST_DECADE_COUNT) else known

    val older = known.take(known.size - newest.size).sumOf { it.second }

    return listOfNotNull(
        DecadeBar(
            label = OLDER_LABEL,
            count = older,
        ).takeIf { older > 0 },
    ) +
        newest.map { (decade, count) ->
            DecadeBar(
                label = "${decade}s",
                count = count,
            )
        }
}

private fun decadesCaption(
    known: List<Pair<Int, Int>>,
    decades: PublicationDecades,
): String {
    val knownBooks = known.sumOf { it.second }

    if (knownBooks == 0) return ""

    val peak = known.maxWithOrNull(compareBy<Pair<Int, Int>> { it.second }.thenByDescending { it.first })?.first

    val lead = if (decades.booksThisCentury * 2 > knownBooks) {
        "Most of your books came out this century."
    } else {
        "More came out in the ${peak}s than any other decade."
    }

    val oldest = decades.oldestYear?.takeIf { it > 0 }?.let { " The oldest is from $it." }.orEmpty()

    return "$lead$oldest"
}

private fun acquisitionCaption(
    noted: Int,
    notNoted: Int,
): String {
    if (noted == 0) return ""

    if (notNoted > 0) return "Noted for $noted of ${noted + notNoted} books."

    return "For the $noted ${if (noted == 1) "book" else "books"} where you noted it."
}

private fun proportionChart(
    entries: List<ChartEntry>,
    variant: ProportionChartVariant,
): ProportionChartUiModel {
    val total = entries.sumOf { it.count }

    val percents = wholePercents(
        counts = entries.map { it.count },
        total = total,
    )

    return ProportionChartUiModel(
        segments = entries.mapIndexed { index, entry ->
            ChartSliceUiModel(
                label = entry.label,
                share = "${percents[index]}%",
                fraction = entry.count.toFloat() / total,
                tone = entry.tone,
            )
        }.toImmutableList(),
        variant = variant,
    )
}

private fun loadingPublicationChart(variant: ProportionChartVariant): ProportionChartUiModel = ProportionChartUiModel(
    segments = List(LOADING_SEGMENT_COUNT) { index ->
        ChartSliceUiModel(
            label = "",
            share = "",
            fraction = 0f,
            tone = ChartTone.forRank(index),
        )
    }.toImmutableList(),
    isLoading = true,
    variant = variant,
)
