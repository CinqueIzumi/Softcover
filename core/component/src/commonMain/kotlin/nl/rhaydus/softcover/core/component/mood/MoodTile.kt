package nl.rhaydus.softcover.core.component.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkCosyBackground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkCosyForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkDreadBackground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkDreadEyebrow
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkDreadForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkHeartWrenchBackground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.MoodInkHeartWrenchForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * A tappable tile that opens a mood-filtered search. Grows past 104.dp with its title and fills the
 * height its parent gives it, so a row measured at `IntrinsicSize.Max` lines two tiles up. While
 * [MoodTileUiModel.isLoading] its leaves mask in place and it ignores presses.
 */
@Composable
fun MoodTile(
    model: MoodTileUiModel,
    onEvent: (MoodTileEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLoading = model.isLoading

    val background: Color
    val foreground: Color
    val eyebrowColor: Color
    val sublineColor: Color

    when (model.ink) {
        MoodTileInk.Cosy -> {
            background = MoodInkCosyBackground
            foreground = MoodInkCosyForeground
            eyebrowColor = MoodInkCosyForeground
            sublineColor = MoodInkCosyForeground.copy(alpha = 0.65f)
        }

        MoodTileInk.Dread -> {
            background = MoodInkDreadBackground
            foreground = MoodInkDreadForeground
            eyebrowColor = MoodInkDreadEyebrow
            sublineColor = MoodInkDreadForeground.copy(alpha = 0.6f)
        }

        MoodTileInk.HeartWrench -> {
            background = MoodInkHeartWrenchBackground
            foreground = MoodInkHeartWrenchForeground
            eyebrowColor = MoodInkHeartWrenchForeground
            sublineColor = MoodInkHeartWrenchForeground.copy(alpha = 0.6f)
        }

        MoodTileInk.Neutral -> {
            background = MaterialTheme.colorScheme.surfaceContainerHigh
            foreground = MaterialTheme.colorScheme.onSurface
            eyebrowColor = MaterialTheme.colorScheme.primary
            sublineColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Column(
        modifier = modifier
            .heightIn(min = 104.dp)
            .clip(Corners.lg)
            .background(background)
            .then(
                if (isLoading) {
                    Modifier
                } else {
                    Modifier
                        .pointerHandCursor()
                        .pressScaleClickable(
                            onClick = { onEvent(MoodTileEvent.Clicked(key = model.key)) },
                            hoverShape = Corners.lg,
                        )
                },
            )
            .padding(Spacing.md),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            SoftcoverText(
                text = "MOOD",
                isLoading = isLoading,
                standIn = "MOOD",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = eyebrowColor,
            )

            val searchIcon = drawableIconResource(
                icon = SoftcoverIcon.Search,
                contentDescription = "",
            )

            Icon(
                painter = searchIcon.getIconPainter(),
                contentDescription = searchIcon.contentDescription,
                tint = foreground.copy(alpha = 0.75f),
                modifier = Modifier
                    .size(16.dp)
                    .skeletonMask(visible = isLoading),
            )
        }

        Column {
            // Title exception: headlineSmall sized down to the spec's 19/22 mood-tile role. The API
            // returns long single-word moods ("Adventurous"); at 24sp they broke mid-word on a
            // half-grid tile. `Hyphens.None` + `LineBreak.Heading` mirror `CoverlessTitleCover`'s guard.
            SoftcoverText(
                text = model.title,
                isLoading = isLoading,
                standIn = model.title,
                style = MaterialTheme.editorialTypography.headlineSmall.copy(
                    fontSize = 19.sp,
                    lineHeight = 22.sp,
                    hyphens = Hyphens.None,
                    lineBreak = LineBreak.Heading,
                ),
                color = foreground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            SoftcoverText(
                text = model.countLabel,
                isLoading = isLoading,
                standIn = model.countLabel,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = sublineColor,
            )
        }
    }
}
