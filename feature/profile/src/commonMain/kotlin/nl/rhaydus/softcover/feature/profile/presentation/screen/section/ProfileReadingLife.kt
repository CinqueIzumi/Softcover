package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.feature.profile.presentation.action.OnHideUntaggedAuthorsToggledAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnYearMetricSelectedAction
import nl.rhaydus.softcover.feature.profile.presentation.action.ProfileAction
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

@Composable
internal fun ColumnScope.ProfileReadingLife(
    state: ProfileUiState,
    runAction: (ProfileAction) -> Unit,
    onShareClick: () -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    ReadingAtlasSection(
        readingAtlas = state.readingAtlas,
        header = state.readingAtlasHeader,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    ShareEntryRow(
        isLoading = state.isReadingLifeLoading,
        enabled = state.readingLifeShareCard != null,
        onClick = onShareClick,
    )

    Spacer(modifier = Modifier.height(40.dp))

    YearColumnHistorySection(
        chart = state.yearHistoryChart,
        caption = state.yearHistoryCaption,
        isLoading = state.isReadingLifeLoading,
        header = state.yearColumnHistoryHeader,
        metricToggle = state.yearMetricToggle,
        onMetricSelected = { runAction(OnYearMetricSelectedAction(metric = it)) },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(40.dp))

    GenreRankingSection(
        genres = state.readingLife?.genres ?: GenreBreakdown(),
        genreRanking = state.genreRanking,
        isLoading = state.isReadingLifeLoading,
        header = state.genreRankingHeader,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(40.dp))

    AuthorRepresentationSection(
        authorDemographics = state.authorDemographics,
        genderChart = state.genderChart,
        bipocChart = state.bipocChart,
        lgbtqChart = state.lgbtqChart,
        hideUntaggedAuthors = state.hideUntaggedAuthors,
        hideUntaggedAuthorsToggle = state.hideUntaggedAuthorsToggle,
        canHideUntaggedAuthors = state.hasUntaggedAuthors,
        isLoading = state.isReadingLifeLoading,
        header = state.authorRepresentationHeader,
        onHideUntaggedAuthorsChange = { runAction(OnHideUntaggedAuthorsToggledAction(newValue = it)) },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(40.dp))

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

    Spacer(modifier = Modifier.height(40.dp))

    RecentlyLovedSection(
        cards = state.recentlyLoved.cards,
        hasRecentlyLoved = state.hasRecentlyLoved,
        isLoading = state.isReadingLifeLoading,
        header = state.recentlyLovedHeader,
        onEvent = onBookCardEvent,
        modifier = Modifier.fillMaxWidth(),
    )
}
