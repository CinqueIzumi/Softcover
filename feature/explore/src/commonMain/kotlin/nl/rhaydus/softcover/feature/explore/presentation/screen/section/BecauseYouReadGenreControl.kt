package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.modifier.tactileClickable
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.explore.presentation.model.BecauseYouReadGenreControlUiModel

/**
 * The "because you read {genre}" picker: a compact `"Genre · {genre}"` label with a trailing chevron that
 * opens the genre sheet. Hidden when the reader has no genre options to switch between.
 */
@Composable
internal fun BecauseYouReadGenreControl(
    model: BecauseYouReadGenreControlUiModel,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (model.isLoading.not() && model.hasOptions.not()) return

    Row(
        modifier = modifier
            .then(
                if (model.isLoading) {
                    Modifier
                } else {
                    Modifier
                        .tactileClickable(
                            onClick = onOpen,
                        )
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SoftcoverText(
            text = model.label,
            isLoading = model.isLoading,
            standIn = model.label,
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Decorative — the adjacent genre label already carries the control's meaning, the
        // same convention MoodTile's search glyph and the rating star use elsewhere in this
        // file; a real description here would double-announce the one tap target.
        val chevronIcon = drawableIconResource(
            icon = SoftcoverIcon.ArrowDropDown,
            contentDescription = "",
        )

        Icon(
            painter = chevronIcon.getIconPainter(),
            contentDescription = chevronIcon.contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(16.dp)
                .skeletonMask(visible = model.isLoading),
        )
    }
}
