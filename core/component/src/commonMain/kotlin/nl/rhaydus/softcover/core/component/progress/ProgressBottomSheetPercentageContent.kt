package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.editorial.component.EditorialSuffix
import nl.rhaydus.designsystem.editorial.component.HeroStatNumberField
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import kotlin.math.min

@Composable
internal fun ColumnScope.ProgressBottomSheetPercentageContent(
    progressPercent: Int,
    buttonSize: ButtonSize,
    actionAt: String?,
    onEvent: (ProgressSheetEvent) -> Unit,
) {
    var number by remember {
        mutableStateOf(TextFieldValue(text = progressPercent.toString()))
    }

    var firstTimeFocusedGained by remember { mutableStateOf(true) }

    val parsed = number.text.toIntOrNull() ?: 0

    val fraction = (parsed.toFloat() / 100f).coerceIn(
        minimumValue = 0f,
        maximumValue = 1f,
    )

    fun stepPercentageBy(delta: Int) {
        val next = (parsed + delta).coerceIn(
            0,
            100,
        )

        number = number.copy(
            text = next.toString(),
            selection = TextRange(next.toString().length),
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperCircle(
            symbol = "−",
            contentDescription = "Decrease percentage",
            onClick = { stepPercentageBy(-1) },
        )

        Spacer(modifier = Modifier.width(18.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            HeroStatNumberField(
                value = number,
                charCount = 3,
                onValueChange = { newValue ->
                    if (firstTimeFocusedGained.not()) {
                        number = number.copy(selection = newValue.selection)
                    } else {
                        firstTimeFocusedGained = false
                    }

                    if (newValue.text == number.text) return@HeroStatNumberField

                    if (newValue.text.isEmpty()) {
                        number = newValue
                        return@HeroStatNumberField
                    }

                    val newNumber = newValue.text.toIntOrNull() ?: run {
                        number = number.copy(
                            text = "",
                            selection = newValue.selection,
                        )
                        return@HeroStatNumberField
                    }

                    val updatedNumber = min(
                        newNumber,
                        100,
                    )

                    number = newValue.copy(text = updatedNumber.toString())
                },
                onFocusReset = {
                    firstTimeFocusedGained = true
                    number = number.copy(selection = TextRange.Zero)
                },
                onFocusGained = {
                    number = number.copy(
                        selection = TextRange(
                            start = 0,
                            end = number.text.length,
                        ),
                    )
                },
            )

            Text(
                text = "%",
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(18.dp))

        StepperCircle(
            symbol = "+",
            contentDescription = "Increase percentage",
            onClick = { stepPercentageBy(1) },
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    EditorialSuffix(text = "of the way through")

    Spacer(modifier = Modifier.height(28.dp))

    EditorialProgressIndicator(fraction = fraction)

    Spacer(modifier = Modifier.height(28.dp))

    RhaydusButton(
        label = "Update progress",
        modifier = Modifier.fillMaxWidth(),
        style = ButtonStyle.FILLED,
        size = buttonSize,
        onClick = {
            onEvent(
                ProgressSheetEvent.PercentageSubmitted(
                    percentage = number.text,
                    actionAt = actionAt,
                ),
            )
        },
    )

    Spacer(modifier = Modifier.height(4.dp))
}
