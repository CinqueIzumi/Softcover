package nl.rhaydus.softcover.core.component.traits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.softcover.core.component.modifier.tactileClickable
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * The plot-or-character slider. While unanswered it is a bare track: no fill and no thumb, but the slider
 * still rests at its midpoint so a touch anywhere, edges included, moves it and reports a value.
 */
@Composable
internal fun HowItReadBalance(
    model: HowItReadBalanceUiModel,
    onEvent: (HowItReadSheetEvent) -> Unit,
) {
    val value = model.value

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HowItReadSheetLabel(text = model.label)

            if (value != null) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .pointerHandCursor()
                        .tactileClickable(
                            onClick = { onEvent(HowItReadSheetEvent.BalanceCleared) },
                            role = Role.Button,
                        )
                        .padding(horizontal = Spacing.xs),
                    contentAlignment = Alignment.Center,
                ) {
                    SoftcoverText(
                        text = model.clearLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xs))

        Slider(
            value = value ?: 0.5f,
            modifier = Modifier.semantics {
                contentDescription = model.label
                if (value == null) {
                    stateDescription = model.unsetLabel
                }
            },
            onValueChange = { onEvent(HowItReadSheetEvent.BalanceChanged(value = it)) },
            valueRange = 0f..1f,
            thumb = {
                if (value != null) {
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
            track = { state ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant),
                ) {
                    if (value != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(state.value)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            },
        )

        HowItReadScaleLabels(
            startLabel = model.startLabel,
            centreLabel = model.centreLabel,
            endLabel = model.endLabel,
        )
    }
}
