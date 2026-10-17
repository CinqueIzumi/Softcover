package nl.rhaydus.softcover.feature.session.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionUiModel
import nl.rhaydus.softcover.core.presentation.session.formatSessionElapsed
import nl.rhaydus.softcover.feature.session.presentation.action.FocusModeAction
import nl.rhaydus.softcover.feature.session.presentation.component.rememberSessionElapsed
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState

/**
 * The distraction-free reading hero shared by both platforms: eyebrow → cover → title/author → the
 * running timer as a `statHero` hero stat → the editable page field → progress → pause/resume + stop.
 * The caller supplies the [Column] modifier (insets / scroll / measure) and the [coverWidth] (mobile
 * stays compact; desktop renders a larger cover).
 */
@Composable
internal fun FocusReadingPanel(
    active: ActiveSessionUiModel,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    state: FocusModeUiState,
    runAction: (FocusModeAction) -> Unit,
    coverWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHaptics.current
    val isPaused = active.isPaused

    val elapsed = rememberSessionElapsed(active = active)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        SoftcoverText(
            text = "NOW READING",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Cover(
            model = active.focusCover,
            modifier = Modifier
                .width(coverWidth)
                .aspectRatio(2f / 3f),
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        SoftcoverText(
            text = active.title,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        active.author?.let { author ->
            Spacer(modifier = Modifier.height(Spacing.xxs))

            SoftcoverText(
                text = "By $author",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xxl))

        SoftcoverText(
            text = if (isPaused) "PAUSED" else "READING TIME",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = if (isPaused) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            },
        )

        Spacer(modifier = Modifier.height(Spacing.xxxs))

        SoftcoverText(
            text = formatSessionElapsed(elapsed = elapsed),
            style = MaterialTheme.editorialTypography.statHero,
            color = if (isPaused) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )

        Spacer(modifier = Modifier.height(Spacing.xxl))

        state.pageEditor?.let { pageEditor ->
            FocusPageEditor(
                pageEditor = pageEditor,
                pageDraft = state.pageDraft,
                pageDraftRejected = state.pageDraftRejected,
                runAction = runAction,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            RhaydusButton(
                label = if (isPaused) "Resume" else "Pause",
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = if (isPaused) SoftcoverIcon.Play else SoftcoverIcon.Pause,
                    contentDescription = "",
                ),
                onClick = {
                    haptics.select()

                    onPauseResume()
                },
                modifier = Modifier
                    .weight(1f)
                    .pointerHandCursor(),
            )

            RhaydusButton(
                label = "Stop",
                style = ButtonStyle.OUTLINED,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Stop,
                    contentDescription = "",
                ),
                onClick = {
                    onStop()
                },
                modifier = Modifier
                    .weight(1f)
                    .pointerHandCursor(),
            )
        }
    }
}
