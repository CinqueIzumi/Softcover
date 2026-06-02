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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import nl.rhaydus.softcover.core.designsystem.R
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSession
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.session.formatSessionElapsed
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberHaptics
import nl.rhaydus.softcover.feature.session.presentation.screen.FocusModeScreen
import java.time.Instant

/**
 * Persistent live-timer bar shown above the bottom nav while a reading session is active. Tapping it
 * opens Focus Mode; it carries pause/resume and stop controls inline. Reads the shared
 * [ActiveSessionController] and slides out of the way the moment the session ends.
 */
@Composable
fun SessionPeekBar(modifier: Modifier = Modifier) {
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
    onOpen: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit,
) {
    val haptics = rememberHaptics()

    var now by remember { mutableStateOf(Instant.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Instant.now()

            delay(timeMillis = 1_000)
        }
    }

    val elapsed = active.session.readingDuration(now = now)

    Surface(
        onClick = onOpen,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
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

            IconButton(
                onClick = {
                    haptics.select()

                    onPauseResume()
                },
            ) {
                Icon(
                    painter = painterResource(
                        id = if (active.session.isPaused) R.drawable.ic_play else R.drawable.ic_pause,
                    ),
                    contentDescription = if (active.session.isPaused) "Resume session" else "Pause session",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            IconButton(
                onClick = {
                    haptics.threshold()

                    onStop()
                },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stop),
                    contentDescription = "Stop session",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
