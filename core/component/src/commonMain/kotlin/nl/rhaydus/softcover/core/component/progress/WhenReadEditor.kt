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
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The inline day + time-of-day editor beneath [WhenReadRow]'s pill. Every stepper asks the host to shift
 * the backdate by a fixed number of minutes; the host rolls the day over and stops at the current moment,
 * so the reader can never dial in a future time.
 */
@Composable
internal fun WhenReadEditor(
    model: WhenReadUiModel,
    onEvent: (WhenReadEvent) -> Unit,
) {
    val timeStyle = MaterialTheme.editorialTypography.headlineMedium

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperCircle(
                icon = SoftcoverIcon.Remove,
                contentDescription = "Earlier day",
                onClick = { onEvent(WhenReadEvent.Shifted(minutes = -1440)) },
            )

            Spacer(modifier = Modifier.width(Spacing.lg))

            SoftcoverText(
                text = model.dayLabel,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(Spacing.lg))

            StepperCircle(
                icon = SoftcoverIcon.Add,
                contentDescription = "Later day",
                onClick = { onEvent(WhenReadEvent.Shifted(minutes = 1440)) },
                enabled = model.canShiftLater,
            )
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperCircle(
                icon = SoftcoverIcon.Remove,
                contentDescription = "Earlier hour",
                onClick = { onEvent(WhenReadEvent.Shifted(minutes = -60)) },
            )

            Spacer(modifier = Modifier.width(Spacing.xs))

            StepperCircle(
                icon = SoftcoverIcon.Remove,
                contentDescription = "Earlier 15 minutes",
                onClick = { onEvent(WhenReadEvent.Shifted(minutes = -15)) },
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            SoftcoverText(
                text = model.timeLabel,
                style = timeStyle,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            StepperCircle(
                icon = SoftcoverIcon.Add,
                contentDescription = "Later 15 minutes",
                onClick = { onEvent(WhenReadEvent.Shifted(minutes = 15)) },
                enabled = model.canShiftLater,
            )

            Spacer(modifier = Modifier.width(Spacing.xs))

            StepperCircle(
                icon = SoftcoverIcon.Add,
                contentDescription = "Later hour",
                onClick = { onEvent(WhenReadEvent.Shifted(minutes = 60)) },
                enabled = model.canShiftLater,
            )
        }
    }
}
