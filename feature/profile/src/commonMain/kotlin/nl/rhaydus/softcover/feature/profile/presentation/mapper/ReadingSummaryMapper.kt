package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.feature.profile.presentation.state.ReadingSummaryStatUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.ReadingSummaryUiModel

/** A null [readingLife] yields loading stats; a year with no entry counts as zero. */
internal fun readingSummaryUiModel(
    readingLife: ReadingLife?,
    currentYear: Int,
): ReadingSummaryUiModel {
    val isLoading = readingLife == null

    return ReadingSummaryUiModel(
        header = SectionHeaderUiModel.Inline(eyebrow = "Your reading"),
        stats = persistentListOf(
            summaryStat(
                count = readingLife?.booksByYear.countFor(currentYear),
                isLoading = isLoading,
                caption = "books this year",
            ),
            summaryStat(
                count = readingLife?.pagesByYear.countFor(currentYear),
                isLoading = isLoading,
                caption = "pages",
            ),
        ),
        linkLabel = "Open your Reading Atlas",
    )
}

private fun summaryStat(
    count: Int,
    isLoading: Boolean,
    caption: String,
): ReadingSummaryStatUiModel = ReadingSummaryStatUiModel(
    stat = StatNumberUiModel(
        value = count.toDouble(),
        format = StatNumberFormat.Grouped,
        isLoading = isLoading,
        variant = StatNumberVariant.Headline,
    ),
    caption = caption,
)

private fun List<YearCount>?.countFor(year: Int): Int = this?.firstOrNull { it.year == year }?.count ?: 0
