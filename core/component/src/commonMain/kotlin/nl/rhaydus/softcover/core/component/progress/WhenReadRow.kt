package nl.rhaydus.softcover.core.component.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent

/**
 * "When did you read this?". A [Chip] defaulting
 * to "Just now" — no backdating, so the submitted time is null and the server stamps the mutation
 * with its own current time. Tapping it asks the host to start a backdate at the current moment and
 * expands an inline day + time-of-day editor below; the chip's dismiss ✕ resets to "Just now" and
 * collapses. Only whether the editor is expanded is local; the backdate itself lives in the host.
 */
@Composable
internal fun WhenReadRow(
    model: WhenReadUiModel,
    onEvent: (ProgressSheetEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Chip(
            model = model.chip,
            onEvent = { event ->
                when (event) {
                    is ChipEvent.Clicked -> {
                        if (model.chip.selected.not()) {
                            onEvent(ProgressSheetEvent.WhenReadShifted(minutes = 0))
                        }

                        expanded = expanded.not()
                    }

                    is ChipEvent.Dismissed -> {
                        onEvent(ProgressSheetEvent.WhenReadReset)
                        expanded = false
                    }

                    is ChipEvent.SpoilerToggled -> Unit
                }
            },
        )

        AnimatedVisibility(
            visible = expanded && model.chip.selected,
            enter = expandVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = shrinkVertically(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()) +
                fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
        ) {
            WhenReadEditor(
                model = model,
                onEvent = onEvent,
            )
        }
    }
}
