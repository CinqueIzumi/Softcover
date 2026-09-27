package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import kotlin.math.min

@Composable
internal fun ColumnScope.ProgressBottomSheetPageContent(
    medium: ProgressSheetMedium.Paged,
    buttonSize: ButtonSize,
    actionAt: String?,
    onEvent: (ProgressSheetEvent) -> Unit,
) {
    val totalPages = medium.totalPages
    val hasTotal = medium.hasKnownTotal

    var number by remember {
        mutableStateOf(TextFieldValue(text = medium.currentPage.toString()))
    }

    var firstTimeFocusedGained by remember { mutableStateOf(true) }

    val parsed = number.text.toIntOrNull() ?: 0

    val fraction = if (hasTotal) {
        (parsed.toFloat() / totalPages).coerceIn(
            minimumValue = 0f,
            maximumValue = 1f,
        )
    } else {
        0f
    }

    fun stepPageBy(delta: Int) {
        val next = (parsed + delta).let {
            if (hasTotal) {
                it.coerceIn(
                    0,
                    totalPages,
                )
            } else {
                it.coerceAtLeast(0)
            }
        }

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
            contentDescription = "Decrease page",
            onClick = { stepPageBy(-1) },
        )

        Spacer(modifier = Modifier.width(18.dp))

        HeroStatNumberField(
            value = number,
            charCount = 4,
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

                val updatedNumber = if (hasTotal) {
                    min(
                        newNumber,
                        totalPages,
                    )
                } else {
                    newNumber
                }

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

        Spacer(modifier = Modifier.width(18.dp))

        StepperCircle(
            symbol = "+",
            contentDescription = "Increase page",
            onClick = { stepPageBy(1) },
        )
    }

    if (hasTotal) {
        Spacer(modifier = Modifier.height(8.dp))

        EditorialSuffix(text = "of $totalPages pages")

        Spacer(modifier = Modifier.height(28.dp))

        EditorialProgressIndicator(fraction = fraction)
    }

    Spacer(modifier = Modifier.height(28.dp))

    RhaydusButton(
        label = "Update progress",
        onClick = {
            onEvent(
                ProgressSheetEvent.PagesSubmitted(
                    page = number.text,
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
