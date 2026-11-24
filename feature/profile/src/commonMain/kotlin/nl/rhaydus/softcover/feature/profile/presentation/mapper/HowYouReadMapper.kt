package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.chart.ChartSliceUiModel
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionChartVariant
import nl.rhaydus.softcover.core.component.chart.ScoreBarUiModel
import nl.rhaydus.softcover.core.component.chart.ScoreBarsUiModel
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.HowYouReadStats
import nl.rhaydus.softcover.core.profile.domain.model.MoodScore
import nl.rhaydus.softcover.core.profile.domain.model.PlotCharacterSplit
import nl.rhaydus.softcover.core.uibinding.mood.toLabel
import nl.rhaydus.softcover.core.uibinding.traits.toLabel
import nl.rhaydus.softcover.feature.profile.presentation.state.ChartBlockUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.HowYouReadUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.MoodsBlockUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.WritingBlockUiModel

private const val PACE_TITLE = "Pace"
private const val WRITING_TITLE = "Writing style"
private const val MOODS_TITLE = "Moods you reach for"
private const val NO_STRENGTH_TEXT = "–"
private const val NO_STRENGTH_DESCRIPTION = "no strength given"
private const val STRENGTH_SCALE = 20.0
private const val STRENGTH_MAX = 100f
private const val LOADING_MOOD_ROWS = 5
private const val STAND_IN_BOOKS = 18
private const val STAND_IN_SHARE = 69

/** A null [stats] yields the masked stand-in. */
internal fun howYouReadUiModel(
    stats: HowYouReadStats?,
    scope: StatsScope,
): HowYouReadUiModel {
    if (stats == null) return loadingHowYouRead(scope)

    val split = stats.plotCharacter.takeIf { it.total > 0 }?.let {
        splitBlock(
            it,
            scope,
        )
    }

    val pace = stats.paceBlock()
    val writing = stats.writingBlock()
    val moods = stats.moodsBlock(scope)

    val isEmpty = split == null && pace == null && writing == null && moods == null

    return HowYouReadUiModel(
        split = split,
        pace = pace,
        writing = writing,
        moods = moods,
        emptyLine = if (isEmpty) emptyLine(scope) else null,
        isLoading = false,
    )
}

private fun emptyLine(scope: StatsScope): String {
    val read = scope.scopePhrase()?.let { "read $it " }.orEmpty()

    return "No book ${read}has a How it read or How it felt answer yet."
}

private fun scopeSuffix(scope: StatsScope): String = scope.scopePhrase()?.let { " $it" }.orEmpty()

private fun booksCount(count: Int): String =
    if (count == 1) "1 book" else "${formatGroupedNumber(count)} books"

private fun splitBlock(
    split: PlotCharacterSplit,
    scope: StatsScope,
): ChartBlockUiModel {
    val top = maxOf(
        split.plot,
        split.mix,
        split.character,
    )

    val headline = when {
        split.mix == top || split.plot == split.character -> SplitHeadline.Mix
        split.plot == top -> SplitHeadline.Plot
        else -> SplitHeadline.Character
    }

    val leadingPart = when (headline) {
        SplitHeadline.Mix -> if (split.mix == top) 1 else 0
        SplitHeadline.Plot -> 0
        SplitHeadline.Character -> 2
    }

    val parts = listOf(
        "Plot" to split.plot,
        "A mix" to split.mix,
        "Character" to split.character,
    )

    val shown = parts.indices.filter { parts[it].second > 0 }

    val percents = wholePercents(
        shown.map { parts[it].second },
        split.total,
    )

    return ChartBlockUiModel(
        title = headline.title,
        caption = splitCaption(
            lean = headline.lean,
            percent = percents[shown.indexOf(leadingPart)],
            total = split.total,
            scope = scope,
        ),
        chart = ProportionChartUiModel(
            segments = shown.mapIndexed { position, part ->
                val (label, count) = parts[part]

                ChartSliceUiModel(
                    label = label,
                    share = "${percents[position]}%",
                    fraction = count.toFloat() / split.total,
                    tone = ChartTone.forRank(part),
                )
            }.toImmutableList(),
            variant = ProportionChartVariant.Keys,
        ),
    )
}

private fun splitCaption(
    lean: String,
    percent: Int,
    total: Int,
    scope: StatsScope,
): String {
    val book = if (total == 1) "book" else "books"

    return "$lean${scopeSuffix(scope)}: $percent% of the ${formatGroupedNumber(total)} $book with a How it read " +
        "answer. Books without one aren't counted."
}

private enum class SplitHeadline(
    val title: String,
    val lean: String,
) {
    Character("Character first", "Your reading leaned character-driven"),
    Plot("Plot first", "Your reading leaned plot-driven"),
    Mix("A mix", "Your reading was a mix of plot and character"),
}

private fun HowYouReadStats.paceBlock(): ChartBlockUiModel? {
    val shown = Pace.entries.filter { (paceCounts[it] ?: 0) > 0 }

    if (shown.isEmpty()) return null

    val counts = shown.map { paceCounts.getValue(it) }
    val total = counts.sum()

    val percents = wholePercents(
        counts,
        total,
    )

    return ChartBlockUiModel(
        title = PACE_TITLE,
        caption = "Of ${booksCount(booksWithPace)} with a pace.",
        chart = ProportionChartUiModel(
            segments = shown.mapIndexed { index, pace ->
                ChartSliceUiModel(
                    label = pace.toLabel(),
                    share = "${percents[index]}%",
                    fraction = counts[index].toFloat() / total,
                    tone = ChartTone.forRank(pace.ordinal),
                )
            }.toImmutableList(),
            variant = ProportionChartVariant.Keys,
        ),
    )
}

private fun HowYouReadStats.writingBlock(): WritingBlockUiModel? {
    val styles = writingStyleCounts.entries
        .filter { it.value > 0 }
        .sortedByDescending { it.value }
        .map { it.key.toLabel() to it.value }
        .countChips(keyPrefix = "style")

    val flags = writingFlagCounts.entries
        .filter { it.value > 0 }
        .sortedByDescending { it.value }
        .map { it.key.toLabel() to it.value }
        .countChips(keyPrefix = "flag")

    if (styles.isEmpty && flags.isEmpty) return null

    val caption = when {
        flags.isEmpty -> "${booksCount(booksWithWritingStyle)} with a style."
        styles.isEmpty -> "${booksCount(booksWithWritingFlag)} with a flag."
        else -> "${booksCount(booksWithWritingStyle)} with a style, ${formatGroupedNumber(booksWithWritingFlag)} with a flag."
    }

    return WritingBlockUiModel(
        title = WRITING_TITLE,
        styles = styles,
        flags = flags,
        caption = caption,
    )
}

private fun List<Pair<String, Int>>.countChips(keyPrefix: String): ChipSet<Unit> = mapIndexed { index, (label, count) ->
    ChipUiModel(
        key = "$keyPrefix-$label",
        label = label,
        selected = index == 0,
        trailing = ChipTrailing.Count(text = formatGroupedNumber(count)),
        interaction = ChipInteraction.Inert,
        contentDescription = "$label, ${booksCount(count)}",
    ) to Unit
}.toChipSet()

private fun HowYouReadStats.moodsBlock(scope: StatsScope): MoodsBlockUiModel? {
    if (moods.booksWithMoods == 0 || moods.topMoods.isEmpty()) return null

    val books = moods.booksWithMoods

    val intro = if (books == 1) {
        "From the 1 book you said how it felt"
    } else {
        "From the ${formatGroupedNumber(books)} books you said how they felt"
    }

    return MoodsBlockUiModel(
        title = MOODS_TITLE,
        caption = "$intro${scopeSuffix(scope)}. Your five most-used moods, ordered by how strongly you felt " +
            "them, out of 5.",
        bars = ScoreBarsUiModel(rows = moods.topMoods.map { it.toBar() }.toImmutableList()),
    )
}

private fun MoodScore.toBar(): ScoreBarUiModel {
    val strength = averageStrength
        ?: return ScoreBarUiModel(
            label = word.toLabel(),
            valueText = NO_STRENGTH_TEXT,
            fill = 0f,
            valueContentDescription = NO_STRENGTH_DESCRIPTION,
        )

    return ScoreBarUiModel(
        label = word.toLabel(),
        valueText = formatDecimalNumber(
            value = strength / STRENGTH_SCALE,
            fractionDigits = 1,
        ),
        fill = strength.toFloat() / STRENGTH_MAX,
    )
}

private fun loadingHowYouRead(scope: StatsScope): HowYouReadUiModel = HowYouReadUiModel(
    split = ChartBlockUiModel(
        title = SplitHeadline.Character.title,
        caption = splitCaption(
            lean = SplitHeadline.Character.lean,
            percent = STAND_IN_SHARE,
            total = STAND_IN_BOOKS,
            scope = scope,
        ),
        chart = ProportionChartUiModel(
            segments = persistentListOf(
                loadingSlice(ChartTone.Lead),
                loadingSlice(ChartTone.Second),
                loadingSlice(ChartTone.Third),
            ),
            isLoading = true,
            variant = ProportionChartVariant.Keys,
        ),
    ),
    pace = ChartBlockUiModel(
        title = PACE_TITLE,
        caption = "Of ${booksCount(STAND_IN_BOOKS)} with a pace.",
        chart = ProportionChartUiModel(
            segments = persistentListOf(
                loadingSlice(ChartTone.Lead),
                loadingSlice(ChartTone.Second),
                loadingSlice(ChartTone.Third),
            ),
            isLoading = true,
            variant = ProportionChartVariant.Keys,
        ),
    ),
    writing = WritingBlockUiModel(
        title = WRITING_TITLE,
        styles = listOf("Simple", "Moderate", "Demanding").standInChips(),
        flags = listOf("Vivid", "Poetic").standInChips(),
        caption = "${booksCount(STAND_IN_BOOKS)} with a style, $STAND_IN_BOOKS with a flag.",
    ),
    moods = MoodsBlockUiModel(
        title = MOODS_TITLE,
        caption = "From the $STAND_IN_BOOKS books you said how they felt${scopeSuffix(scope)}. Your five " +
            "most-used moods, ordered by how strongly you felt them, out of 5.",
        bars = ScoreBarsUiModel.loading(rowCount = LOADING_MOOD_ROWS),
    ),
    emptyLine = null,
    isLoading = true,
)

private fun loadingSlice(tone: ChartTone) = ChartSliceUiModel(
    label = "",
    share = "",
    fraction = 0f,
    tone = tone,
)

private fun List<String>.standInChips(): ChipSet<Unit> = map { label ->
    ChipUiModel(
        key = label,
        label = label,
        trailing = ChipTrailing.Count(text = "0"),
        interaction = ChipInteraction.Inert,
    ) to Unit
}.toChipSet()
