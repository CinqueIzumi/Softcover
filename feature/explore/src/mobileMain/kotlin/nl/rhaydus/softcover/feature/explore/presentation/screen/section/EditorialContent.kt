package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun EditorialContent(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    contentPadding: PaddingValues,
    scrollState: ScrollState,
    pullToRefreshState: PullToRefreshState,
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { runAction(OnRefreshAction) },
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize(),
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.LoadingIndicator(
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = Spacing.xs,
                    bottom = rememberBottomBarPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.section),
        ) {
            DiscoverySections(
                state = state,
                runAction = runAction,
                onBookClick = onBookClick,
            ) { rail, set, onCardEvent ->
                ExploreCardRail(
                    rail = rail,
                    set = set,
                    onCardEvent = onCardEvent,
                )
            }
        }
    }
}
