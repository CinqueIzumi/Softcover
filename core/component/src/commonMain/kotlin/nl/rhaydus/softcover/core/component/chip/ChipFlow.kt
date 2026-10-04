package nl.rhaydus.softcover.core.component.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Wraps every chip of [set] in one [FlowRow]. A [ChipEvent] resolves its key through [set] before
 * it reaches [onEvent], so an event whose chip has left the set is dropped.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <P : Any> ChipFlow(
    set: ChipSet<P>,
    onEvent: (event: ChipEvent, payload: P) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        set.chips.forEach { chip ->
            Chip(
                model = chip,
                onEvent = { event ->
                    set[event.key]?.let { payload ->
                        onEvent(
                            event,
                            payload,
                        )
                    }
                },
            )
        }
    }
}
