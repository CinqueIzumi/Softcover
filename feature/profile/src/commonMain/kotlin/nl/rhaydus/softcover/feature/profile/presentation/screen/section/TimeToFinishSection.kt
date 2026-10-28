package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.statistic.StatTile
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.profile.presentation.state.TimeToFinishUiModel

/**
 * Median days to finish a book, all books and per length band, as a two by two grid of tiles. When
 * [TimeToFinishUiModel.hasReads] is false the grid and caption are dropped and the framing says why.
 */
@Composable
internal fun TimeToFinishSection(
    model: TimeToFinishUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AtlasSubhead(
            title = model.title,
            caption = model.framing,
            isLoading = model.isLoading,
        )

        ScopeEmptySwitch(
            isEmpty = model.hasReads.not() && model.isLoading.not(),
            empty = {},
            filled = {
                Column {
                    Spacer(modifier = Modifier.height(Spacing.md))

                    model.tiles.chunked(2).forEachIndexed { index, pair ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(Spacing.sm))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            pair.forEach { tile ->
                                StatTile(
                                    model = tile,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    FadeThroughText(
                        text = model.caption,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        isLoading = model.isLoading,
                        standIn = model.caption,
                    )
                }
            },
        )
    }
}
