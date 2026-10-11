package nl.rhaydus.softcover.feature.session.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import nl.rhaydus.common.currentInstant
import nl.rhaydus.softcover.core.domain.model.readingDuration
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionUiModel
import kotlin.time.Duration

@Composable
internal fun rememberSessionElapsed(active: ActiveSessionUiModel): Duration {
    var now by remember { mutableStateOf(currentInstant()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = currentInstant()

            delay(timeMillis = 1_000)
        }
    }

    return readingDuration(
        startedAtEpochMs = active.startedAtEpochMs,
        pausedSeconds = active.pausedSeconds,
        lastPausedAtEpochMs = active.pausedAtEpochMs,
        endEpochMs = now.toEpochMilliseconds(),
    )
}
