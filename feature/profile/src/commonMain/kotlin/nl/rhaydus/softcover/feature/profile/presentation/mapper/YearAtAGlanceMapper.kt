package nl.rhaydus.softcover.feature.profile.presentation.mapper

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter

private val YEAR_AT_A_GLANCE_EYEBROW = atlasChapterEyebrow(AtlasChapter.YEAR_AT_A_GLANCE)
private const val HEADLINE = "Day by day"
internal const val LOADING_YEAR = 2026
internal val LOADING_TODAY = LocalDate(
    year = LOADING_YEAR,
    month = 1,
    day = 1,
)

/** The masked header shown until the first stats land. */
internal fun loadingYearAtAGlanceHeader(): SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
    eyebrow = YEAR_AT_A_GLANCE_EYEBROW,
    headline = HEADLINE,
    description = description(LOADING_YEAR),
    isLoading = true,
)

internal fun yearAtAGlanceHeader(year: Int): SectionHeaderUiModel.Section = SectionHeaderUiModel.Section(
    eyebrow = YEAR_AT_A_GLANCE_EYEBROW,
    headline = HEADLINE,
    description = description(year),
)

/** The calendar year a per-year chapter shows: the selected one, else the year [today] falls in. */
internal fun atlasYear(
    scope: StatsScope,
    today: LocalDate,
): Int = (scope as? StatsScope.CalendarYear)?.year ?: today.year

private fun description(year: Int): String =
    "Each square is a day of $year. Darker days held more pages. Tap one to see what you read."
