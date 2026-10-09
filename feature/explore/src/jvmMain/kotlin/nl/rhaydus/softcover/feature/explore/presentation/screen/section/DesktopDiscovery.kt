package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.DesktopVerticalScrollbar
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

private val discoveryScrollState = ScrollState(initial = 0)

@Composable
internal fun DesktopDiscovery(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(discoveryScrollState)
                .padding(bottom = Spacing.xl + rememberBottomBarPadding()),
            verticalArrangement = Arrangement.spacedBy(Spacing.x4l),
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

        DesktopVerticalScrollbar(
            scrollState = discoveryScrollState,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = Spacing.xxs),
        )
    }
}
