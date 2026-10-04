package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.mood.MoodTile
import nl.rhaydus.softcover.core.component.mood.MoodTileEvent
import nl.rhaydus.softcover.core.component.mood.MoodTileUiModel
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnMoodTileClickedAction

/**
 * The 2-column "Browse by mood" grid. Each row is measured at
 * [androidx.compose.foundation.layout.IntrinsicSize.Max] so a tile whose title wraps to two lines and
 * its one-line neighbour land at the same height.
 */
@Composable
internal fun MoodGrid(
    tiles: ImmutableList<MoodTileUiModel>,
    header: SectionHeaderUiModel,
    runAction: (ExploreAction) -> Unit,
) {
    if (tiles.isEmpty()) return

    val onTileEvent: (MoodTileEvent) -> Unit = remember(runAction) {
        { event -> runAction(OnMoodTileClickedAction(key = event.key)) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionHeader(
            model = header,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            tiles.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { tile ->
                        MoodTile(
                            model = tile,
                            onEvent = onTileEvent,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }

                    if (row.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
