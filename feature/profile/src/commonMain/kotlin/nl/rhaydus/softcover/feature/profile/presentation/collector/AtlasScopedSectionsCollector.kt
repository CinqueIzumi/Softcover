package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.resolve
import nl.rhaydus.softcover.core.profile.domain.model.HourlyPages
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.TimeToFinish
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasOpeningUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeLine
import nl.rhaydus.softcover.feature.profile.presentation.mapper.authorsEmptyCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.authorsScopeCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.formatSplitCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.formatSplitChart
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreEmptyCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasFormatSplit
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasMonthlyPages
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasTimeOfDayPages
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasTopAuthors
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesMonthSetKey
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsEmptyCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsHistogramCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsHistogramHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.timeOfDayCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.timeOfDayChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.timeToFinishUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.topAuthorRows
import nl.rhaydus.softcover.feature.profile.presentation.mapper.topAuthorsCaption
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of every section that answers for the selected scope: the opening tiles, who you
 * read, how you score, the format split, pages by month, pages by time of day and the time to finish,
 * with their framing copy and the scope line. For genres it writes only whether any exist and the
 * empty sentence; `GenreRankingCollector` maps the rest off the stats written here.
 *
 * It stays idle until the history has landed, because the history decides the default scope and
 * observing the placeholder one first would flash the wrong numbers. Switching scope re-observes the
 * stats for the new range; the previous values stay on screen until they arrive, so the components
 * animate from one to the other. Today comes from the midnight-aware date stream, so a rolling window
 * re-resolves when the day turns.
 */
internal class AtlasScopedSectionsCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        scope.state
            .map { state ->
                ScopeSnapshot(
                    scope = state.selectedScope,
                    isHistoryLoaded = state.readingHistory != null,
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { snapshot ->
                if (snapshot.isHistoryLoaded) dependencies.observeSections(snapshot.scope) else emptyFlow()
            }
            .collect { sections -> scope.setState { it.withSections(sections) } }
    }

    private fun StatsAtlasDependencies.observeSections(scope: StatsScope): Flow<ScopedSections> {
        return observeCurrentDateUseCase().flatMapLatest { today ->
            val range = scope.resolve(today)

            combine(
                observeReadingStatsUseCase(range)
                    .untilBackfilled(observeReadingStatsBackfilledUseCase()),
                observeMonthlyPagesReadUseCase(range),
                observeHourlyPagesReadUseCase(range),
                getHideUntaggedAuthorsAsFlowUseCase(),
                observeTimeToFinishUseCase(range)
                    .untilBackfilled(observeReadDatesBackfilledUseCase()),
            ) { stats, monthlyPages, hourlyPages, hideUntaggedAuthors, timeToFinish ->
                ScopedSections(
                    scope = scope,
                    today = today,
                    stats = stats,
                    monthlyPages = monthlyPages,
                    hourlyPages = hourlyPages,
                    hideUntaggedAuthors = hideUntaggedAuthors,
                    timeToFinish = timeToFinish,
                )
            }
        }
    }

    private fun StatsAtlasUiState.withSections(sections: ScopedSections): StatsAtlasUiState {
        val scope = sections.scope
        val hideUntaggedAuthors = sections.hideUntaggedAuthors
        val stats = sections.stats
        val monthlyPages = sections.monthlyPages
        val hourlyPages = sections.hourlyPages
        val genres = stats?.genres
        val ratings = stats?.ratings
        val booksRead = stats?.booksRead ?: 0
        val demographics = stats?.authorDemographics?.let {
            if (hideUntaggedAuthors) it.excludingUntaggedAuthors() else it
        }

        return copy(
            readingStats = stats,
            scopeLine = atlasScopeLine(
                scope = scope,
                today = sections.today,
            ),
            opening = atlasOpeningUiModel(
                stats = stats,
                scope = scope,
            ),
            genreEmptyCaption = genreEmptyCaption(
                booksRead = booksRead,
                scope = scope,
            ),
            genderBreakdown = genderBreakdownUiModel(
                demographics = demographics,
                hideUntaggedAuthors = hideUntaggedAuthors,
            ),
            bipocBreakdown = demographicBreakdownUiModel(
                breakdown = demographics?.bipocBreakdown,
                hideUntaggedAuthors = hideUntaggedAuthors,
            ),
            lgbtqBreakdown = demographicBreakdownUiModel(
                breakdown = demographics?.lgbtqBreakdown,
                hideUntaggedAuthors = hideUntaggedAuthors,
            ),
            authorsScopeCaption = authorsScopeCaption(
                hideUntaggedAuthors = hideUntaggedAuthors,
                scope = scope,
            ),
            authorsEmptyCaption = authorsEmptyCaption(
                hideUntaggedAuthors = hideUntaggedAuthors,
                booksRead = booksRead,
                scope = scope,
            ),
            ratingsChart = ratingsChartUiModel(ratings),
            ratingsAverage = ratingsAverageUiModel(ratings),
            ratingsHistogramHeader = ratings?.let {
                ratingsHistogramHeader(
                    ratings = it,
                    scope = scope,
                )
            } ?: ratingsHistogramHeader,
            ratingsHistogramCaption = ratings?.let {
                ratingsHistogramCaption(
                    ratings = it,
                    scope = scope,
                )
            } ?: ratingsHistogramCaption,
            ratingsAverageLabel = ratings?.let { ratingsAverageCaption(totalRatings = it.totalRatings) }
                ?: ratingsAverageLabel,
            ratingsEmptyCaption = ratingsEmptyCaption(
                booksRead = booksRead,
                scope = scope,
            ),
            timeOfDayCaption = timeOfDayCaption(
                hourly = hourlyPages,
                scope = scope,
            ),
            timeOfDayChart = timeOfDayChartUiModel(hourlyPages),
            hasTimeOfDayPages = hasTimeOfDayPages(hourlyPages),
            monthlyPagesHeader = monthlyPagesHeader(
                monthlyPages = monthlyPages,
                scope = scope,
                today = sections.today,
            ),
            monthlyPagesChart = monthlyPagesChartUiModel(
                monthlyPages = monthlyPages,
                scope = scope,
                today = sections.today,
            ),
            hasMonthlyPages = hasMonthlyPages(monthlyPages),
            monthlyPagesMonthSetKey = monthlyPagesMonthSetKey(monthlyPages),
            topAuthorsCaption = topAuthorsCaption(
                topAuthors = stats?.topAuthors,
                booksRead = booksRead,
                scope = scope,
            ),
            topAuthorRows = topAuthorRows(stats?.topAuthors),
            hasTopAuthors = hasTopAuthors(stats?.topAuthors),
            formatSplitCaption = formatSplitCaption(
                split = stats?.formatSplit,
                booksRead = booksRead,
                scope = scope,
            ),
            formatSplitChart = formatSplitChart(stats?.formatSplit),
            hasFormatSplit = hasFormatSplit(stats?.formatSplit),
            timeToFinish = timeToFinishUiModel(
                timeToFinish = sections.timeToFinish,
                scope = scope,
            ),
        )
    }

    private data class ScopeSnapshot(
        val scope: StatsScope,
        val isHistoryLoaded: Boolean,
    )

    private data class ScopedSections(
        val scope: StatsScope,
        val today: LocalDate,
        val stats: ReadingStats?,
        val monthlyPages: MonthlyPages,
        val hourlyPages: HourlyPages,
        val hideUntaggedAuthors: Boolean,
        val timeToFinish: TimeToFinish?,
    )
}
