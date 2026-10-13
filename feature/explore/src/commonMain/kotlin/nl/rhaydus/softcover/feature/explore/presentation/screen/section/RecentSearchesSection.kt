package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRecentSearchChipClickedAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveAllSearchQueriesClickedAction

/**
 * The feed's recent-search history block (explore-3a §4 "Recent searches"): an eyebrow + "Clear all"
 * row over a wrapping row of tap-to-search pill chips. No per-chip remove here — that affordance lives
 * on the search-focus state's recent rows (`SearchFocusRecentRow`) instead.
 */
@Composable
internal fun RecentSearchesSection(
    chips: ChipSet<String>,
    runAction: (ExploreAction) -> Unit,
) {
    if (chips.isEmpty) return

    Column(
        modifier = Modifier.padding(horizontal = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SoftcoverText(
                text = "RECENT SEARCHES",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RhaydusButton(
                label = "Clear all",
                onClick = { runAction(OnRemoveAllSearchQueriesClickedAction()) },
                style = ButtonStyle.TEXT,
            )
        }

        ChipFlow(
            set = chips,
            onEvent = { event, _ ->
                if (event is ChipEvent.Clicked) {
                    runAction(OnRecentSearchChipClickedAction(key = event.key))
                }
            },
        )
    }
}
