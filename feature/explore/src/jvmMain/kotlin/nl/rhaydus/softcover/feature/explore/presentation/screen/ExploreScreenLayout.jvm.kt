package nl.rhaydus.softcover.feature.explore.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.editorial.component.EditorialSearchField
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.modifier.dismissOnEscape
import nl.rhaydus.softcover.core.component.state.EmptyState
import nl.rhaydus.softcover.core.component.state.offlineEmptyStateUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRefreshAction
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.DesktopDiscovery
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.DesktopExploreHeader
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.DesktopSearchResults
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

/**
 * Desktop Explore (explore-3a): a full-width discovery surface (no two-pane — a tapped book pushes
 * detail full-screen, see `wideGridTabsUseDetailPane`). A static editorial header carries an explicit
 * Refresh control and the barcode-scan affordance; a persistent [EditorialSearchField] sits beneath it
 * (desktop has no search-focus takeover — the field is always in place, per the foundation's persistent-
 * field pattern for a static editorial header). With the field empty and no mood filter active, the body
 * is the discovery wall: the featured release, then trending / because-you-read / up-next rendered as
 * wrapping multi-column grids ([FlowRow][androidx.compose.foundation.layout.FlowRow] of the shared cards),
 * the mood grid, and recent-search history — all scrolled by a
 * [DesktopVerticalScrollbar][nl.rhaydus.designsystem.component.DesktopVerticalScrollbar]. With a query or
 * mood filter active the body becomes a multi-column results grid. No pager and no pull-to-refresh. The
 * cards, dismiss sheet, mood grid, and recent-searches block are the shared shelf pieces.
 */
@Composable
internal actual fun ExploreScreenLayout(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
    onBookClick: (Book, String?) -> Unit,
    onScanClick: () -> Unit,
    isOnline: Boolean,
) {
    if (isOnline.not()) {
        EmptyState(
            model = offlineEmptyStateUiModel(),
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = rememberBottomBarPadding()),
        )
        return
    }

    // Esc clears the search - the desktop counterpart of mobile's back rung (DS §3.1 "Back leaves
    // search before it leaves the screen"). Only the one rung exists here: the field is persistent,
    // so there is no focus surface to close underneath it, and it dispatches what the field's own ×
    // dispatches rather than OnClearSearchAction, whose focus drop means nothing to a field that is
    // never focus-driven.
    //
    // Held enabled unconditionally rather than gated on `hasActiveSearch`, which is what it looks
    // like it wants: `dismissOnEscape` takes focus on the disabled -> enabled flip, and here that
    // flip *is* the user typing their first character - gating it would pull focus straight out of
    // the field being typed into. So the guard sits inside the callback instead, and an Esc on the
    // resting discovery wall is a no-op. Filed upstream as F26 (a listen-without-taking-focus
    // variant), which would let this gate the modifier properly.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .dismissOnEscape {
                if (state.hasActiveSearch) {
                    runAction(OnQueryChangeAction(newQuery = ""))
                }
            },
    ) {
        DesktopExploreHeader(
            isRefreshing = state.isRefreshing,
            onRefreshClick = { runAction(OnRefreshAction) },
            onScanClick = onScanClick,
        )

        Spacer(modifier = Modifier.height(8.dp))

        EditorialSearchField(
            query = state.searchText,
            onQueryChange = { query ->
                runAction(OnQueryChangeAction(newQuery = query))
            },
            onClearClick = {
                runAction(OnQueryChangeAction(newQuery = ""))
            },
            searchIcon = drawableIconResource(
                icon = SoftcoverIcon.Search,
                contentDescription = "Search",
            ),
            clearIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Clear search",
            ),
            placeholder = "Search books, authors…",
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (state.hasActiveSearch) {
                DesktopSearchResults(
                    state = state,
                    runAction = runAction,
                    onBookClick = onBookClick,
                )
            } else {
                DesktopDiscovery(
                    state = state,
                    runAction = runAction,
                    onBookClick = onBookClick,
                )
            }
        }
    }
}
