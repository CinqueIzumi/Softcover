package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.bookcard.BookCardEvent
import nl.rhaydus.softcover.core.component.header.PageMasthead
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.profile.presentation.action.OnGenreRankingShowLessAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnGenreRankingShowMoreAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnReadingDayTappedAction
import nl.rhaydus.softcover.feature.profile.presentation.action.OnScopeSelectedAction
import nl.rhaydus.softcover.feature.profile.presentation.action.StatsAtlasAction
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState

/** The Atlas page from masthead to last chapter, shared by the mobile and desktop layouts. */
@Composable
internal fun ColumnScope.StatsAtlasBody(
    state: StatsAtlasUiState,
    jumper: AtlasChapterJumper,
    runAction: (StatsAtlasAction) -> Unit,
    onBookCardEvent: (BookCardEvent) -> Unit,
) {
    PageMasthead(model = state.masthead)

    Spacer(modifier = Modifier.height(Spacing.xl))

    AtlasScopeSection(
        scopeChips = state.scopeChips,
        isHistoryLoaded = state.readingHistory != null,
        scopeLine = state.scopeLine,
        onScopeSelected = { runAction(OnScopeSelectedAction(statsScope = it)) },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Spacing.xxxl))

    AtlasOpeningSection(
        opening = state.opening,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Spacing.section))

    AtlasContentsSection(
        header = state.contentsHeader,
        rows = state.contentsRows,
        onRowClick = jumper::jumpTo,
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Spacing.section))

    Column(modifier = jumper.chapter(AtlasChapter.YEAR_AT_A_GLANCE)) {
        YearAtAGlanceChapter(
            header = state.yearAtAGlanceHeader,
            title = state.timeOfDayTitle,
            caption = state.timeOfDayCaption,
            chart = state.timeOfDayChart,
            yearGrid = state.yearGrid,
            onDayTapped = { date ->
                runAction(OnReadingDayTappedAction(date = date))
            },
            footnote = state.timeOfDayFootnote,
            hasTimeOfDayPages = state.hasTimeOfDayPages,
        )
    }

    Spacer(modifier = Modifier.height(Spacing.section))

    Column(modifier = jumper.chapter(AtlasChapter.SEASONS_AND_RECORDS)) {
        SeasonsAndRecordsChapter(
            header = state.monthlyPagesHeader,
            chart = state.monthlyPagesChart,
            monthSetKey = state.monthlyPagesMonthSetKey,
            hasMonthlyPages = state.hasMonthlyPages,
            topAuthorsTitle = state.topAuthorsTitle,
            topAuthorsCaption = state.topAuthorsCaption,
            topAuthorRows = state.topAuthorRows,
            hasTopAuthors = state.hasTopAuthors,
            formatSplitTitle = state.formatSplitTitle,
            formatSplitCaption = state.formatSplitCaption,
            formatSplitChart = state.formatSplitChart,
            hasFormatSplit = state.hasFormatSplit,
        )
    }

    Spacer(modifier = Modifier.height(Spacing.section))

    Column(modifier = jumper.chapter(AtlasChapter.GENRES_AND_HOW_YOU_READ)) {
        GenresChapter(
            header = state.genreHeader,
            ranking = state.genreRanking,
            showMore = state.genreRankingShowMore,
            showLess = state.genreRankingShowLess,
            onShowMore = {
                runAction(OnGenreRankingShowMoreAction())
            },
            onShowLess = {
                runAction(OnGenreRankingShowLessAction())
            },
        )
    }

    Spacer(modifier = Modifier.height(Spacing.section))

    Column(modifier = jumper.chapter(AtlasChapter.TEMPORARY)) {
        TemporaryChapter(
            state = state,
            runAction = runAction,
            onBookCardEvent = onBookCardEvent,
        )
    }
}
