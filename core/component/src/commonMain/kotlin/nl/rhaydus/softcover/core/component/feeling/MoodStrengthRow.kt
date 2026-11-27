package nl.rhaydus.softcover.core.component.feeling

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.tabularFigures
import kotlin.math.roundToInt

/**
 * A mood's 0 to 100 strength slider. While unset it rests at its midpoint with no fill and no thumb, so a
 * touch anywhere moves it and reports a value.
 */
@Composable
internal fun MoodStrengthRow(
    model: MoodStrengthUiModel,
    onStrengthChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHaptics.current
    val strength = model.strength

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftcoverText(
                text = model.label,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )

            SoftcoverText(
                text = strength?.toString() ?: model.unsetLabel,
                style = MaterialTheme.typography.labelMedium.tabularFigures(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Slider(
            value = (strength ?: 50).toFloat(),
            modifier = Modifier.semantics {
                contentDescription = model.label

                if (strength == null) {
                    stateDescription = model.unsetLabel
                }
            },
            onValueChange = { raw ->
                val step = raw.roundToInt()

                if (step != strength) {
                    haptics.tickle()
                    onStrengthChange(step)
                }
            },
            valueRange = 0f..100f,
            thumb = {
                if (strength != null) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                    )
                } else {
                    Spacer(modifier = Modifier.size(Spacing.md))
                }
            },
            track = {
                FeelingTrack(strength = strength)
            },
        )
    }
}
