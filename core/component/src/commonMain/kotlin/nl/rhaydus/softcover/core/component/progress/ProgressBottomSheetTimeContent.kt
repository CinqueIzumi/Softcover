package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.toHoursMinutesSeconds
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSuffix
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun ColumnScope.ProgressBottomSheetTimeContent(
    medium: ProgressSheetMedium.Timed,
    buttonSize: ButtonSize,
    actionAt: String?,
    onEvent: (ProgressSheetEvent) -> Unit,
) {
    val initial = medium.currentSeconds.toHoursMinutesSeconds()
    val totalSeconds = medium.totalSeconds
    val hasTotal = medium.hasKnownTotal
    val totalHms = totalSeconds.toHoursMinutesSeconds()

    var hours by remember { mutableStateOf(TextFieldValue(text = initial.hours.toString())) }
    var minutes by remember { mutableStateOf(TextFieldValue(text = initial.minutes.toString())) }
    var seconds by remember { mutableStateOf(TextFieldValue(text = initial.seconds.toString())) }

    val currentSeconds = (hours.text.toIntOrNull() ?: 0) * 3600 +
        (minutes.text.toIntOrNull() ?: 0) * 60 +
        (seconds.text.toIntOrNull() ?: 0)

    val fraction = if (hasTotal) {
        (currentSeconds.toFloat() / totalSeconds).coerceIn(
            minimumValue = 0f,
            maximumValue = 1f,
        )
    } else {
        0f
    }

    val timeStyle = MaterialTheme.editorialTypography.headlineMedium

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimeField(
            value = hours,
            charCount = 2,
            textStyle = timeStyle,
            onValueChange = { newValue ->
                val parsed = newValue.text.toIntOrNull()
                hours = when {
                    newValue.text.isEmpty() -> newValue
                    parsed == null -> hours
                    else -> newValue.copy(text = parsed.coerceAtLeast(minimumValue = 0).toString())
                }
            },
        )

        TimeColon(textStyle = timeStyle)

        StepperCircle(
            symbol = "−",
            contentDescription = "Decrease minutes",
            onClick = {
                val next = ((minutes.text.toIntOrNull() ?: 0) - 1).coerceIn(
                    0,
                    59,
                )
                minutes = minutes.copy(text = next.toString())
            },
            modifier = Modifier.padding(end = 6.dp),
        )

        TimeField(
            value = minutes,
            charCount = 2,
            textStyle = timeStyle,
            onValueChange = { newValue ->
                val parsed = newValue.text.toIntOrNull()
                minutes = when {
                    newValue.text.isEmpty() -> newValue
                    parsed == null -> minutes
                    else -> newValue.copy(
                        text = parsed.coerceIn(
                            minimumValue = 0,
                            maximumValue = 59,
                        ).toString(),
                    )
                }
            },
        )

        StepperCircle(
            symbol = "+",
            contentDescription = "Increase minutes",
            onClick = {
                val next = ((minutes.text.toIntOrNull() ?: 0) + 1).coerceIn(
                    0,
                    59,
                )
                minutes = minutes.copy(text = next.toString())
            },
            modifier = Modifier.padding(start = 6.dp),
        )

        TimeColon(textStyle = timeStyle)

        TimeField(
            value = seconds,
            charCount = 2,
            textStyle = timeStyle,
            onValueChange = { newValue ->
                val parsed = newValue.text.toIntOrNull()
                seconds = when {
                    newValue.text.isEmpty() -> newValue
                    parsed == null -> seconds
                    else -> newValue.copy(
                        text = parsed.coerceIn(
                            minimumValue = 0,
                            maximumValue = 59,
                        ).toString(),
                    )
                }
            },
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
                    hours = hours.text,
                    minutes = minutes.text,
                    seconds = seconds.text,
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
    Text(
        text = ":",
        modifier = Modifier.padding(horizontal = 4.dp),
        style = textStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
