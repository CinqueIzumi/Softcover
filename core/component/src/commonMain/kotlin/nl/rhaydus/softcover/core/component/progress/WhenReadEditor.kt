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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The inline day + time-of-day editor beneath [WhenReadRow]'s pill. Every stepper asks the host to shift
 * the backdate by a fixed number of minutes; the host rolls the day over and stops at the current moment,
 * so the reader can never dial in a future time.
 */
@Composable
internal fun WhenReadEditor(
    model: WhenReadUiModel,
    onEvent: (ProgressSheetEvent) -> Unit,
) {
    val timeStyle = MaterialTheme.editorialTypography.headlineMedium

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
                onClick = { onEvent(ProgressSheetEvent.WhenReadShifted(minutes = -1440)) },
            )

            Spacer(modifier = Modifier.width(18.dp))

            SoftcoverText(
                text = model.dayLabel,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(18.dp))

            StepperCircle(
                symbol = "+",
                contentDescription = "Later day",
                onClick = { onEvent(ProgressSheetEvent.WhenReadShifted(minutes = 1440)) },
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
                onClick = { onEvent(ProgressSheetEvent.WhenReadShifted(minutes = -60)) },
            )

            Spacer(modifier = Modifier.width(8.dp))

            StepperCircle(
                symbol = "−",
                contentDescription = "Earlier 15 minutes",
                onClick = { onEvent(ProgressSheetEvent.WhenReadShifted(minutes = -15)) },
            )

            Spacer(modifier = Modifier.width(14.dp))

            SoftcoverText(
                text = model.timeLabel,
                style = timeStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(14.dp))

            StepperCircle(
                symbol = "+",
                contentDescription = "Later 15 minutes",
                onClick = { onEvent(ProgressSheetEvent.WhenReadShifted(minutes = 15)) },
            )

            Spacer(modifier = Modifier.width(8.dp))

            StepperCircle(
                symbol = "+",
                contentDescription = "Later hour",
                onClick = { onEvent(ProgressSheetEvent.WhenReadShifted(minutes = 60)) },
            )
        }
    }
}
