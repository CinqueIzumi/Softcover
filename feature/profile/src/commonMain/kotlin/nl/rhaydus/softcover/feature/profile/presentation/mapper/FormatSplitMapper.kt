package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chart.ChartSliceUiModel
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionChartVariant
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.FormatSplit

internal const val FORMAT_SPLIT_TITLE = "Print, ebook or audio"
private const val STAND_IN_CAPTION = "Mostly paper, with a third on a screen."
private const val EMPTY_ALL_TIME = "Finish a few books and the formats you read in show up here."
private const val NONE_KNOWN_CAPTION = "None of these books has a known format."
private const val MOSTLY_UNKNOWN_CAPTION = "At least half of these books have no known format."
private const val UNKNOWN_LABEL = "Unknown"
private const val PERCENT = 100
private const val SECOND_FORMAT_MIN_PERCENT = 15
private const val NEARLY_HALF_PERCENT = 45
private const val THIRD_PERCENT = 30
private const val QUARTER_PERCENT = 22

private val FORMAT_ORDER = listOf(
    ReadingFormat.Physical,
    ReadingFormat.Ebook,
    ReadingFormat.Audio,
    ReadingFormat.Both,
)

private val FORMAT_TONES = mapOf(
    ReadingFormat.Physical to ChartTone.Lead,
    ReadingFormat.Ebook to ChartTone.Second,
    ReadingFormat.Audio to ChartTone.Third,
    ReadingFormat.Both to ChartTone.Fourth,
)

private val ReadingFormat.segmentLabel: String
    get() = when (this) {
        ReadingFormat.Physical -> "Print"
        ReadingFormat.Ebook -> "Ebook"
        ReadingFormat.Audio -> "Audiobook"
        ReadingFormat.Both -> "Print and audio"
    }

private val ReadingFormat.noun: String
    get() = when (this) {
        ReadingFormat.Physical -> "paper"
        ReadingFormat.Ebook -> "ebooks"
        ReadingFormat.Audio -> "audiobooks"
        ReadingFormat.Both -> "print and audio"
    }

private val ReadingFormat.phrase: String
    get() = when (this) {
        ReadingFormat.Physical -> "on paper"
        ReadingFormat.Ebook -> "on a screen"
        ReadingFormat.Audio -> "as audiobooks"
        ReadingFormat.Both -> "in print and audio"
    }

private val ReadingFormat.onlySentence: String
    get() = when (this) {
        ReadingFormat.Physical -> "Only paper."
        ReadingFormat.Ebook -> "Only ebooks."
        ReadingFormat.Audio -> "Only audiobooks."
        ReadingFormat.Both -> "Only print and audio."
    }

internal fun formatSplitCaption(
    split: FormatSplit?,
    booksRead: Int,
    scope: StatsScope,
): String {
    if (split == null) return STAND_IN_CAPTION

    val total = split.total
    if (booksRead == 0 || total == 0) {
        return scope.noFinishedBooksSentence(booksRead = booksRead) ?: EMPTY_ALL_TIME
    }

    val known = knownCounts(split)
    if (known.isEmpty()) return NONE_KNOWN_CAPTION
    if (split.unknownCount * 2 >= total) return MOSTLY_UNKNOWN_CAPTION

    val ranked = known.sortedByDescending { it.second }
    val (lead, leadCount) = ranked.first()
    val second = ranked.getOrNull(1)
    val mostly = "Mostly ${lead.noun}"

    return when {
        leadCount * 2 > total -> when {
            known.size == 1 && split.unknownCount == 0 -> lead.onlySentence
            second != null && second.second * PERCENT >= SECOND_FORMAT_MIN_PERCENT * total -> {
                val portion = portion(
                    count = second.second,
                    total = total,
                )

                "$mostly, with $portion ${second.first.phrase}."
            }
            else -> "$mostly."
        }
        second == null -> "$mostly."
        else -> "Split between ${lead.noun} and ${second.first.noun}."
    }
}

private fun knownCounts(split: FormatSplit): List<Pair<ReadingFormat, Int>> = FORMAT_ORDER
    .mapNotNull { format -> split.counts[format]?.takeIf { it > 0 }?.let { format to it } }

private fun portion(
    count: Int,
    total: Int,
): String = when {
    count * PERCENT >= NEARLY_HALF_PERCENT * total -> "nearly half"
    count * PERCENT >= THIRD_PERCENT * total -> "a third"
    count * PERCENT >= QUARTER_PERCENT * total -> "a quarter"
    else -> "a fifth"
}

internal fun hasFormatSplit(split: FormatSplit?): Boolean = split != null && split.counts.values.sum() > 0

/** A null [split] yields the masked stand-in chart. */
internal fun formatSplitChart(split: FormatSplit?): ProportionChartUiModel {
    if (split == null) return LOADING_FORMAT_CHART

    val total = split.total
    val entries = knownCounts(split).map { (format, count) ->
        Triple(
            format.segmentLabel,
            count,
            FORMAT_TONES.getValue(format),
        )
    } + listOfNotNull(
        split.unknownCount.takeIf { it > 0 }?.let {
            Triple(
                UNKNOWN_LABEL,
                it,
                ChartTone.Unknown,
            )
        },
    )
    val percents = wholePercents(
        counts = entries.map { it.second },
        total = total,
    )

    return ProportionChartUiModel(
        segments = entries.mapIndexed { index, (label, count, tone) ->
            ChartSliceUiModel(
                label = label,
                share = "${percents[index]}%",
                fraction = count.toFloat() / total,
                tone = tone,
            )
        }.toImmutableList(),
        variant = ProportionChartVariant.Keys,
    )
}

/**
 * Whole percents by largest remainder so they sum to 100. A non-zero count never shows 0%: it shows 1% and
 * the largest share gives up the point.
 */
private fun wholePercents(
    counts: List<Int>,
    total: Int,
): List<Int> {
    if (total <= 0 || counts.isEmpty()) return counts.map { 0 }

    val floors = counts.map { it * PERCENT / total }.toMutableList()
    val remainders = counts.map { it * PERCENT % total }
    val missing = PERCENT - floors.sum()
    remainders.indices
        .sortedWith(compareByDescending<Int> { remainders[it] }.thenBy { it })
        .take(missing)
        .forEach { floors[it] += 1 }

    counts.indices.filter { counts[it] > 0 && floors[it] == 0 }.forEach { index ->
        val largest = floors.indices.maxByOrNull { floors[it] } ?: return@forEach
        floors[largest] -= 1
        floors[index] = 1
    }

    return floors
}

private val LOADING_FORMAT_CHART = ProportionChartUiModel(
    segments = persistentListOf(
        loadingSegment(ChartTone.Lead),
        loadingSegment(ChartTone.Second),
        loadingSegment(ChartTone.Third),
    ),
    isLoading = true,
    variant = ProportionChartVariant.Keys,
)

private fun loadingSegment(tone: ChartTone) = ChartSliceUiModel(
    label = "",
    share = "",
    fraction = 0f,
    tone = tone,
)
