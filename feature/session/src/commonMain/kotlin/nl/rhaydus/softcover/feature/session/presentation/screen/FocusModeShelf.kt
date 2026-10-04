package nl.rhaydus.softcover.feature.session.presentation.screen

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import nl.rhaydus.common.currentInstant
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSuffix
import nl.rhaydus.designsystem.editorial.component.HeroStatNumberField
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.progress.ProgressBar
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.presentation.session.formatSessionElapsed
import nl.rhaydus.softcover.feature.session.presentation.action.FocusModeAction
import nl.rhaydus.softcover.feature.session.presentation.action.OnPageDraftChangedAction
import nl.rhaydus.softcover.feature.session.presentation.action.OnPageFocusGainedAction
import nl.rhaydus.softcover.feature.session.presentation.action.OnPageFocusLostAction
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.FocusPageEditorUiModel

/**
 * The distraction-free reading hero shared by both platforms: eyebrow → cover → title/author → the
 * running timer as a `statHero` hero stat → the editable page field → progress → pause/resume + stop.
 * Owns the 1s ticking [currentInstant] state so the timer advances identically on mobile and desktop.
 * The caller supplies the [Column] modifier (insets / scroll / measure) and the [coverWidth] (mobile
 * stays compact; desktop renders a larger cover). Hover/cursor on the buttons is inert on touch, so
 * mobile renders identically.
 */
@Composable
internal fun FocusReadingPanel(
    active: ActiveSession,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
    state: FocusModeUiState,
    runAction: (FocusModeAction) -> Unit,
    coverWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()
    val isPaused = active.session.isPaused

    var now by remember { mutableStateOf(currentInstant()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = currentInstant()

            delay(timeMillis = 1_000)
        }
    }

    val elapsed = active.session.readingDuration(now = now)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        SoftcoverText(
            text = "NOW READING",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(14.dp))

        Cover(
            model = active.focusCover,
            modifier = Modifier
                .width(coverWidth)
                .aspectRatio(2f / 3f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        SoftcoverText(
            text = active.book.title,
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        active.book.authors.firstOrNull()?.let { author ->
            Spacer(modifier = Modifier.height(4.dp))

            SoftcoverText(
                text = "By ${author.name}",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        SoftcoverText(
            text = if (isPaused) "PAUSED" else "READING TIME",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = if (isPaused) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary
            },
        )

        Spacer(modifier = Modifier.height(2.dp))

        SoftcoverText(
            text = formatSessionElapsed(elapsed = elapsed),
            style = MaterialTheme.editorialTypography.statHero,
            color = if (isPaused) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )

        Spacer(modifier = Modifier.height(28.dp))

        state.pageEditor?.let { pageEditor ->
            FocusPageEditor(
                pageEditor = pageEditor,
                pageDraft = state.pageDraft,
                runAction = runAction,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    haptics.threshold()

                    onStop()
                },
                modifier = Modifier
                    .weight(1f)
                    .pointerHandCursor(),
            )
        }
    }
}

/**
 * The reading position rendered as an editable hero stat (DS §5) — the same treatment as the
 * progress sheet's page tab: a borderless [HeroStatNumberField] in the stat face over an editorial
 * caption and a progress bar. Tapping it opens the keyboard; the new page commits when the field
 * loses focus (keyboard "Done" or tapping away). Books without a page count (audiobooks / unknown)
 * show a read-only caption instead of the field.
 */
@Composable
private fun FocusPageEditor(
    pageEditor: FocusPageEditorUiModel,
    pageDraft: TextFieldValue,
    runAction: (FocusModeAction) -> Unit,
) {
    if (pageEditor.isEditable.not()) {
        EditorialSuffix(text = pageEditor.caption)

        Spacer(modifier = Modifier.height(12.dp))

        ProgressBar(model = pageEditor.bar)

        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        HeroStatNumberField(
            value = pageDraft,
            charCount = 4,
            onValueChange = { runAction(OnPageDraftChangedAction(newValue = it)) },
            onFocusReset = { runAction(OnPageFocusLostAction) },
            onFocusGained = { runAction(OnPageFocusGainedAction) },
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    EditorialSuffix(text = pageEditor.caption)

    Spacer(modifier = Modifier.height(20.dp))

    ProgressBar(model = pageEditor.bar)
}
