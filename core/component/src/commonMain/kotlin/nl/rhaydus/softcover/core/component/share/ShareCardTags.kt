package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ShareCardTags(
    tags: ImmutableList<ChipUiModel>,
    format: ShareCardFormat,
) {
    if (tags.isEmpty()) return

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(
            Spacing.xs,
            Alignment.CenterHorizontally,
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        maxLines = when (format) {
            ShareCardFormat.Story -> 2
            ShareCardFormat.FullText -> Int.MAX_VALUE
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        tags.forEach { tag ->
            Chip(model = tag)
        }
    }
}
