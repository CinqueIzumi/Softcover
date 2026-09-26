package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.seconds
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnRemoveAllSearchQueriesClickedAction

/**
 * The feed's recent-search history block (explore-3a §4 "Recent searches"): an eyebrow + "Clear all"
 * row over a wrapping row of tap-to-search pill chips. No per-chip remove here — that affordance lives
 * on the search-focus state's recent rows (`SearchFocusRecentRow`) instead.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecentSearchesSection(
    queries: List<String>,
    runAction: (ExploreAction) -> Unit,
) {
    if (queries.isEmpty()) return

    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
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

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            queries.forEach { query ->
                RecentSearchChip(
                    query = query,
                    onClick = {
                        runAction(
                            OnQueryChangeAction(
                                newQuery = query,
                                searchDelay = 0.seconds,
                            ),
                        )
                    },
                )
            }
        }
    }
}
