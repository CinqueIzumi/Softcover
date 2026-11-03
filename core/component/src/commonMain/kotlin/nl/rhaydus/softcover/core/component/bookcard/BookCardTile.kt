package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.theme.Corners
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun BookCardTile(
    model: BookCardUiModel,
    variant: BookCardVariant.Tile,
    onEvent: (BookCardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val eyebrow = variant.text.eyebrow

    Column(
        modifier = modifier
            .clip(Corners.xl)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .bookCardInteraction(
                model = model,
                onEvent = onEvent,
                interactionSource = interactionSource,
                hoverWash = false,
            )
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (eyebrow != null) {
            BookCardEyebrowText(
                eyebrow = eyebrow,
                isLoading = model.isLoading,
            )
        }

        BookCardCoverLayout(
            cover = variant.cover,
            selection = null,
            modifier = Modifier.width(72.dp),
        )

        BookCardTextBlock(
            content = variant.text.copy(eyebrow = null),
            titleStyle = MaterialTheme.editorialTypography.titleSmall,
            titleMinLines = 1,
            titleMaxLines = 2,
            isLoading = model.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
