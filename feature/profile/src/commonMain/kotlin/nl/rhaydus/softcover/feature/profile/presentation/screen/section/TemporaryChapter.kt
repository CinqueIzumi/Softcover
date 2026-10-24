package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.profile.presentation.action.OnHideUntaggedAuthorsToggledAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnYearMetricSelectedAction
import nl.rhaydus.softcover.feature.profile.presentation.action.StatsAtlasAction
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState

@Composable
internal fun ColumnScope.TemporaryChapter(
    state: StatsAtlasUiState,
    runAction: (StatsAtlasAction) -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    SectionHeader(model = state.temporaryChapterHeader)

    Spacer(modifier = Modifier.height(Spacing.md))

    YearColumnHistorySection(
        chart = state.yearHistoryChart,
        caption = state.yearHistoryCaption,
        isLoading = state.isReadingLifeLoading,
        header = state.yearColumnHistoryHeader,
        metricToggle = state.yearMetricToggle,
        onMetricSelected = { runAction(OnYearMetricSelectedAction(metric = it)) },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Spacing.section))

    GenreRankingSection(
        hasGenres = state.hasGenres,
        genreScopeCaption = state.genreScopeCaption,
        genreRanking = state.genreRanking,
        isLoading = state.isReadingLifeLoading,
        header = state.genreRankingHeader,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Spacing.section))

    AuthorRepresentationSection(
        genderBreakdown = state.genderBreakdown,
        bipocBreakdown = state.bipocBreakdown,
        lgbtqBreakdown = state.lgbtqBreakdown,
        hideUntaggedAuthors = state.hideUntaggedAuthors,
        hideUntaggedAuthorsToggle = state.hideUntaggedAuthorsToggle,
        canHideUntaggedAuthors = state.hasUntaggedAuthors,
        isLoading = state.isReadingLifeLoading,
        header = state.authorRepresentationHeader,
        onHideUntaggedAuthorsChange = { runAction(OnHideUntaggedAuthorsToggledAction(newValue = it)) },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Spacing.section))

    RatingsHistogramSection(
        chart = state.ratingsChart,
        average = state.ratingsAverage,
        totalRatings = state.ratingsTotal,
        averageLabel = state.ratingsAverageLabel,
        isLoading = state.isReadingLifeLoading,
        header = state.ratingsHistogramHeader,
        caption = state.ratingsHistogramCaption,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Spacing.section))

    RecentlyLovedSection(
        cards = state.recentlyLoved.cards,
        hasRecentlyLoved = state.hasRecentlyLoved,
        isLoading = state.isReadingLifeLoading,
        header = state.recentlyLovedHeader,
        onEvent = onBookCardEvent,
        modifier = Modifier.fillMaxWidth(),
    )
}
