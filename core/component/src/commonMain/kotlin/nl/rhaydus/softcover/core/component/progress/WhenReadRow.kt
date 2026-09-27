package nl.rhaydus.softcover.core.component.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.ChipVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

private const val WHEN_READ_CHIP_KEY = "when-read"

/**
 * "When did you read this?" (patterns/reading.md, "Backdate a logged action"). A [Chip] defaulting
 * to "Just now" — no backdating, so the submitted `actionAt` is null and the server stamps the
 * mutation with its own current time. Tapping it seeds the picker with the current moment and
 * expands an inline day + time-of-day editor below; the chip's dismiss ✕ resets to "Just now" and
 * collapses.
 */
@Composable
internal fun WhenReadRow(
    pickedDateTime: LocalDateTime?,
    onPickedDateTimeChange: (LocalDateTime?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    // The sheet is a short-lived interaction (seconds, not minutes), so a single snapshot of "now"
    // taken at first composition is precise enough to anchor the "no future" clamp below — it does
    // not need to keep ticking while the picker is open.
    val now = remember { currentLocalDateTime() }
    val timeZone = remember { TimeZone.currentSystemDefault() }

    val isCustomized = pickedDateTime != null

    Column(modifier = Modifier.fillMaxWidth()) {
        // S6-1 lifts this model onto `UpdateProgressBottomSheet`'s `UiState` (R10); until then it is
        // the one `WhenReadRow`-local exception to "a UI model is never built in composition".
        Chip(
            model = whenReadChipModel(
                picked = pickedDateTime,
                now = now,
            ),
            onEvent = { event ->
                when (event) {
                    is ChipEvent.Clicked -> {
                        if (isCustomized.not()) {
                            onPickedDateTimeChange(now)
                        }

                        expanded = expanded.not()
                    }

                    is ChipEvent.Dismissed -> {
                        onPickedDateTimeChange(null)
                        expanded = false
                    }

                    is ChipEvent.SpoilerToggled -> Unit
                }
            },
        )

        AnimatedVisibility(
            visible = expanded && isCustomized,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            WhenReadEditor(
                pickedDateTime = pickedDateTime ?: now,
                now = now,
                timeZone = timeZone,
                onPickedDateTimeChange = onPickedDateTimeChange,
            )
        }
    }
}

/** Pure so S6-1 can lift it onto `UiState` unchanged once [pickedDateTime] moves there too. */
internal fun whenReadChipModel(
    picked: LocalDateTime?,
    now: LocalDateTime,
): ChipUiModel = ChipUiModel(
    key = WHEN_READ_CHIP_KEY,
    label = picked?.let { formatWhenReadLabel(
        picked = it,
        now = now,
    ) } ?: "Just now",
    variant = ChipVariant.Tonal(selected = picked != null),
    interaction = ChipInteraction.Clickable,
    leadingIcon = SoftcoverIcon.DateRange,
    dismissLabel = if (picked != null) "Reset to just now" else null,
)

/** [dayLabelFor], paired with the picked time-of-day. */
private fun formatWhenReadLabel(
    picked: LocalDateTime,
    now: LocalDateTime,
): String {
    val hh = picked.hour.toString().padStart(
        2,
        '0',
    )
    val mm = picked.minute.toString().padStart(
        2,
        '0',
    )

    return "${dayLabelFor(
        picked = picked,
        now = now,
    )}, $hh:$mm"
}
