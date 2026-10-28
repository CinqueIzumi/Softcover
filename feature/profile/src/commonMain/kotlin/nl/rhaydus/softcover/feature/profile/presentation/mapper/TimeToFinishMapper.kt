package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.LengthBand
import nl.rhaydus.softcover.core.profile.domain.model.TimeToFinish
import nl.rhaydus.softcover.feature.profile.presentation.state.TimeToFinishUiModel

private const val TIME_TO_FINISH_TITLE = "Time to finish"
private const val STAND_IN_FRAMING = "Your typical book took 9 days. Ones over 450 pages took 23."
private const val STAND_IN_CAPTION =
    "Median calendar days from start to finish, paused time included, across 31 reads with both dates. " +
        "A reread counts on its own."
private const val EMPTY_ALL_TIME = "Finish a few books and how long they take shows up here."
private const val ALL_BOOKS_EYEBROW = "All books"

private val BAND_EYEBROWS = mapOf(
    LengthBand.UNDER_250 to "Under 250 pages",
    LengthBand.FROM_250_TO_450 to "250 to 450 pages",
    LengthBand.OVER_450 to "Over 450 pages",
)

private val LengthBand.phrase: String
    get() = when (this) {
        LengthBand.UNDER_250 -> "under 250 pages"
        LengthBand.FROM_250_TO_450 -> "between 250 and 450 pages"
        LengthBand.OVER_450 -> "over 450 pages"
    }

/** A null [timeToFinish] yields the masked stand-in. */
internal fun timeToFinishUiModel(
    timeToFinish: TimeToFinish?,
    scope: StatsScope,
): TimeToFinishUiModel {
    if (timeToFinish == null) return loadingTimeToFinish()

    val median = timeToFinish.medianDays
    if (timeToFinish.readCount == 0 || median == null) {
        return TimeToFinishUiModel(
            title = TIME_TO_FINISH_TITLE,
            framing = emptyFraming(
                excludedReadCount = timeToFinish.excludedReadCount,
                scope = scope,
            ),
            tiles = persistentListOf(),
            caption = "",
            hasReads = false,
            isLoading = false,
        )
    }

    val reads = if (timeToFinish.readCount == 1) "1 read" else "${timeToFinish.readCount} reads"

    return TimeToFinishUiModel(
        title = TIME_TO_FINISH_TITLE,
        framing = framing(
            medianDays = median,
            bandMedianDays = timeToFinish.bandMedianDays,
        ),
        tiles = tiles(
            medianDays = median,
            bandMedianDays = timeToFinish.bandMedianDays,
        ),
        caption = "Median calendar days from start to finish, paused time included, across $reads " +
            "with both dates. A reread counts on its own.",
        hasReads = true,
        isLoading = false,
    )
}

private fun framing(
    medianDays: Int,
    bandMedianDays: Map<LengthBand, Int>,
): String {
    val typical = "Your typical book took ${days(medianDays)}."
    if (bandMedianDays.size < 2) return typical

    val longest = listOf(LengthBand.OVER_450, LengthBand.FROM_250_TO_450, LengthBand.UNDER_250)
        .first { it in bandMedianDays }

    return "$typical Ones ${longest.phrase} took ${bandMedianDays.getValue(longest)}."
}

private fun emptyFraming(
    excludedReadCount: Int,
    scope: StatsScope,
): String {
    val phrase = scope.scopePhrase()
    val inScope = phrase?.let { " $it" }.orEmpty()

    return when {
        excludedReadCount > 0 -> "None of the reads you finished$inScope has both a start and a finish date."
        phrase != null -> "No finished reads $phrase."
        else -> EMPTY_ALL_TIME
    }
}

private fun tiles(
    medianDays: Int,
    bandMedianDays: Map<LengthBand, Int>,
): ImmutableList<StatTileUiModel> {
    val values = listOf(ALL_BOOKS_EYEBROW to medianDays) +
        LengthBand.entries.map { band -> BAND_EYEBROWS.getValue(band) to bandMedianDays[band] }

    return values.map { (eyebrow, value) ->
        StatTileUiModel(
            eyebrow = eyebrow,
            stat = value?.let {
                StatNumberUiModel(
                    value = it.toDouble(),
                    format = StatNumberFormat.Plain,
                )
            },
            variant = StatTileVariant.Compact(unit = if (value == 1) "day" else "days"),
        )
    }.toImmutableList()
}

private fun days(count: Int): String = if (count == 1) "1 day" else "$count days"

private fun loadingTimeToFinish(): TimeToFinishUiModel {
    val eyebrows = listOf(ALL_BOOKS_EYEBROW) + BAND_EYEBROWS.values

    return TimeToFinishUiModel(
        title = TIME_TO_FINISH_TITLE,
        framing = STAND_IN_FRAMING,
        tiles = eyebrows.map { eyebrow ->
            StatTileUiModel(
                eyebrow = eyebrow,
                stat = StatNumberUiModel(
                    value = 0.0,
                    isLoading = true,
                ),
                variant = StatTileVariant.Compact(unit = "days"),
            )
        }.toImmutableList(),
        caption = STAND_IN_CAPTION,
        hasReads = true,
        isLoading = true,
    )
}
