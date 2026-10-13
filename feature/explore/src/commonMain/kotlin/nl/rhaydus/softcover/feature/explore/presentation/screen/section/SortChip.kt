package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnSortMenuExpandedChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnSortModeChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.mapper.displayLabel
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

/** The sort control for search results (explore-3a deviation 4: Relevance / Popularity only). */
@Composable
internal fun SortChip(
    state: ExploreScreenUiState,
    runAction: (ExploreAction) -> Unit,
) {
    Box {
        PillButton(
            model = state.sortPill,
            onEvent = { runAction(OnSortMenuExpandedChangeAction(expanded = true)) },
        )

        DropdownMenu(
            expanded = state.isSortMenuExpanded,
            onDismissRequest = { runAction(OnSortMenuExpandedChangeAction(expanded = false)) },
        ) {
            ExploreSortMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { SoftcoverText(text = mode.displayLabel) },
                    onClick = { runAction(OnSortModeChangeAction(mode = mode)) },
                )
            }
        }
    }
}
