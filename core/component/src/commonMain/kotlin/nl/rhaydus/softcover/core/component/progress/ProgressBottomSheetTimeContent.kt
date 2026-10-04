package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.toHoursMinutesSeconds
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSuffix
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun ColumnScope.ProgressBottomSheetTimeContent(
    medium: ProgressSheetMedium.Timed,
    buttonSize: ButtonSize,
    actionAt: String?,
    onEvent: (ProgressSheetEvent) -> Unit,
) {
    val totalSeconds = medium.totalSeconds
    val hasTotal = medium.hasKnownTotal
    val totalHms = totalSeconds.toHoursMinutesSeconds()

    val currentSeconds = (medium.hours.text.toIntOrNull() ?: 0) * 3600 +
        (medium.minutes.text.toIntOrNull() ?: 0) * 60 +
        (medium.seconds.text.toIntOrNull() ?: 0)

    val fraction = if (hasTotal) {
        (currentSeconds.toFloat() / totalSeconds).coerceIn(
            minimumValue = 0f,
            maximumValue = 1f,
        )
    } else {
        0f
    }

    val timeStyle = MaterialTheme.editorialTypography.headlineMedium

    val onTimeFieldEvent = remember(onEvent) { timeFieldEventHandler(onEvent = onEvent) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            model = medium.hours,
            onEvent = onTimeFieldEvent,
        )

        TimeColon(textStyle = timeStyle)

        StepperCircle(
            symbol = "−",
            contentDescription = "Decrease minutes",
            onClick = { onEvent(ProgressSheetEvent.MinutesStepped(delta = -1)) },
            modifier = Modifier.padding(end = 6.dp),
        )

        TextField(
            model = medium.minutes,
            onEvent = onTimeFieldEvent,
        )

        StepperCircle(
            symbol = "+",
            contentDescription = "Increase minutes",
            onClick = { onEvent(ProgressSheetEvent.MinutesStepped(delta = 1)) },
            modifier = Modifier.padding(start = 6.dp),
        )

        TimeColon(textStyle = timeStyle)

        TextField(
            model = medium.seconds,
            onEvent = onTimeFieldEvent,
        )
    }

    if (hasTotal) {
        Spacer(modifier = Modifier.height(8.dp))

        EditorialSuffix(
            text = "of " +
                "${totalHms.hours.toString().padStart(
                    2,
                    '0',
                )}:" +
                "${totalHms.minutes.toString().padStart(
                    2,
                    '0',
                )}:" +
                totalHms.seconds.toString().padStart(
                    2,
                    '0',
                ),
        )

        Spacer(modifier = Modifier.height(28.dp))

        EditorialProgressIndicator(fraction = fraction)
    }

    Spacer(modifier = Modifier.height(28.dp))

    RhaydusButton(
        label = "Update progress",
        onClick = {
            onEvent(
                ProgressSheetEvent.TimeSubmitted(
                    hours = medium.hours.text,
                    minutes = medium.minutes.text,
                    seconds = medium.seconds.text,
                    actionAt = actionAt,
                ),
            )
        },
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        size = buttonSize,
    )

    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun TimeColon(textStyle: TextStyle) {
    SoftcoverText(
        text = ":",
        modifier = Modifier.padding(horizontal = 4.dp),
        style = textStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** Every time field's key is its [ProgressTimeUnit] name, so one handler serves all three. */
private fun timeFieldEventHandler(
    onEvent: (ProgressSheetEvent) -> Unit,
): (TextFieldEvent) -> Unit = { event ->
    when (event) {
        is TextFieldEvent.ValueChanged -> onEvent(
            ProgressSheetEvent.TimeChanged(
                unit = ProgressTimeUnit.valueOf(event.key),
                text = event.text,
            ),
        )

        is TextFieldEvent.Submitted,
        is TextFieldEvent.TrailingClicked -> Unit
    }
}
