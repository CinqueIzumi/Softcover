package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * The inline day + time-of-day editor beneath [WhenReadRow]'s pill. Every stepper shifts
 * [pickedDateTime] by a fixed [Duration] through instant arithmetic — which rolls a day/hour/minute
 * over correctly on its own — then clamps the result to [now] so the reader can never dial in a
 * future moment; a stepper simply stops moving once it reaches that boundary.
 */
@Composable
internal fun WhenReadEditor(
    pickedDateTime: LocalDateTime,
    now: LocalDateTime,
    timeZone: TimeZone,
    onPickedDateTimeChange: (LocalDateTime) -> Unit,
) {
    fun shiftBy(duration: Duration) {
        val shifted = pickedDateTime
            .toInstant(timeZone)
            .plus(duration)
            .toLocalDateTime(timeZone)

        onPickedDateTimeChange(if (shifted > now) now else shifted)
    }

    val dayLabel = dayLabelFor(
        picked = pickedDateTime,
        now = now,
    )

    val timeStyle = MaterialTheme.editorialTypography.headlineMedium
    val hh = pickedDateTime.hour.toString().padStart(
        2,
        '0',
    )
    val mm = pickedDateTime.minute.toString().padStart(
        2,
        '0',
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperCircle(
                symbol = "−",
                contentDescription = "Earlier day",
                onClick = { shiftBy((-1).days) },
            )

            Spacer(modifier = Modifier.width(18.dp))

            Text(
                text = dayLabel,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(18.dp))

            StepperCircle(
                symbol = "+",
                contentDescription = "Later day",
                onClick = { shiftBy(1.days) },
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperCircle(
                symbol = "−",
                contentDescription = "Earlier hour",
                onClick = { shiftBy((-1).hours) },
            )

            Spacer(modifier = Modifier.width(8.dp))

            StepperCircle(
                symbol = "−",
                contentDescription = "Earlier 15 minutes",
                onClick = { shiftBy((-15).minutes) },
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "$hh:$mm",
                style = timeStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(14.dp))

            StepperCircle(
                symbol = "+",
                contentDescription = "Later 15 minutes",
                onClick = { shiftBy(15.minutes) },
            )

            Spacer(modifier = Modifier.width(8.dp))

            StepperCircle(
                symbol = "+",
                contentDescription = "Later hour",
                onClick = { shiftBy(1.hours) },
            )
        }
    }
}

/** "Today" / "Yesterday" / "N days ago". */
internal fun dayLabelFor(
    picked: LocalDateTime,
    now: LocalDateTime,
): String = when (val daysAgo = picked.date.daysUntil(now.date)) {
    0 -> "Today"
    1 -> "Yesterday"
    else -> "$daysAgo days ago"
}
