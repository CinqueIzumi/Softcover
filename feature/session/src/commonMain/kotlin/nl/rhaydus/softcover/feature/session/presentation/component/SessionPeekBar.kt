package nl.rhaydus.softcover.feature.session.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.softcover.core.designsystem.presentation.component.DesktopTooltip
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.session.formatSessionElapsed
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.session.presentation.screen.FocusModeScreen
import nl.rhaydus.ui.common.currentInstant

/**
 * Persistent live-timer bar shown above the bottom nav while a reading session is active. Tapping it
 * opens Focus Mode; it carries pause/resume and stop controls inline. Reads the shared
 * [ActiveSessionController] and slides out of the way the moment the session ends.
 *
 * [flush] picks the form: the default rounded floating card sits above the compact bottom bar (the
 * touch aesthetic); the flush variant is an edge-to-edge strip with a top hairline — the desktop
 * now-playing convention — for the wide shell, where there is no bottom bar for it to float over.
 */
@Composable
fun SessionPeekBar(
    modifier: Modifier = Modifier,
    flush: Boolean = false,
) {
    val controller = koinInject<ActiveSessionController>()
    val navigator = LocalNavigator.currentOrThrow
    val active by controller.activeSession.collectAsStateWithLifecycle()

    var lastActive by remember { mutableStateOf<ActiveSession?>(null) }

    active?.let { lastActive = it }

    AnimatedVisibility(
        visible = active != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier,
    ) {
        lastActive?.let { session ->
            SessionPeekBarContent(
                active = session,
                flush = flush,
                onOpen = { navigator.parent?.push(item = FocusModeScreen) },
                onPauseResume = {
                    if (session.session.isPaused) controller.resume() else controller.pause()
                },
                onStop = { controller.stop() },
            )
        }
    }
}

@Composable
private fun SessionPeekBarContent(
    active: ActiveSession,
    flush: Boolean,
    onOpen: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
) {
    val haptics = rememberHaptics()

    var now by remember { mutableStateOf(currentInstant()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = currentInstant()

            delay(timeMillis = 1_000)
        }
    }

    val elapsed = active.session.readingDuration(now = now)

    Column(modifier = Modifier.fillMaxWidth()) {
        // The flush desktop strip is led by a top hairline so it reads as docked chrome rather than
        // a card hovering over the page; the floating form needs none.
        if (flush) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Surface(
            onClick = onOpen,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = if (flush) RectangleShape else MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (flush) 0.dp else 12.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EditionImage(
                    edition = active.book.currentEdition,
                    defaultEdition = active.book.defaultEdition,
                    isLoading = false,
                    fallbackCoverUrl = active.book.coverUrl,
                    cornerRadius = 4.dp,
                    modifier = Modifier.width(32.dp),
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = active.book.title,
                        style = MaterialTheme.editorialTypography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = if (active.session.isPaused) {
                            "Paused · ${formatSessionElapsed(elapsed = elapsed)}"
                        } else {
                            formatSessionElapsed(elapsed = elapsed)
                        },
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }

                val pauseResumeIcon = SoftcoverIconResource.Drawable(
                    icon = if (active.session.isPaused) SoftcoverIcon.Play else SoftcoverIcon.Pause,
                    contentDescription = if (active.session.isPaused) {
                        "Resume session"
                    } else {
                        "Pause session"
                    },
                )

                DesktopTooltip(text = if (active.session.isPaused) "Resume" else "Pause") {
                    IconButton(
                        onClick = {
                            haptics.select()

                            onPauseResume()
                        },
                    ) {
                        Icon(
                            painter = pauseResumeIcon.getIconPainter(),
                            contentDescription = pauseResumeIcon.contentDescription,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                val stopIcon = SoftcoverIconResource.Drawable(
                    icon = SoftcoverIcon.Stop,
                    contentDescription = "Stop session",
                )

                DesktopTooltip(text = "Stop") {
                    IconButton(
                        onClick = {
                            haptics.threshold()

                            onStop()
                        },
                    ) {
                        Icon(
                            painter = stopIcon.getIconPainter(),
                            contentDescription = stopIcon.contentDescription,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
