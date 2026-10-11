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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import nl.rhaydus.designsystem.editorial.component.EditorialSuffix
import nl.rhaydus.designsystem.editorial.component.HeroStatNumberField
import nl.rhaydus.softcover.core.component.field.TextField
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.component.modifier.rememberShakeTrigger
import nl.rhaydus.softcover.core.component.modifier.shakeOnError
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun ColumnScope.ProgressBottomSheetPageContent(
    medium: ProgressSheetMedium.Paged,
    onNumberChange: (String) -> Unit,
) {
    SteppedNumberContent(
        initial = medium.currentPage,
        max = if (medium.hasKnownTotal) medium.totalPages else null,
        charCount = 4,
        noun = "page",
        unitSymbol = null,
        suffix = medium.suffix,
        onNumberChange = onNumberChange,
    )
}

@Composable
internal fun ColumnScope.ProgressBottomSheetPercentageContent(
    progressPercent: Int,
    onNumberChange: (String) -> Unit,
) {
    SteppedNumberContent(
        initial = progressPercent,
        max = 100,
        charCount = 3,
        noun = "percentage",
        unitSymbol = "%",
        suffix = "of the way through",
        onNumberChange = onNumberChange,
    )
}

@Composable
internal fun ColumnScope.ProgressBottomSheetTimeContent(
    medium: ProgressSheetMedium.Timed,
    onEvent: (ProgressSheetEvent) -> Unit,
) {
    // Every time field's key is its ProgressTimeUnit name, so one handler serves all three.
    val onTimeFieldEvent: (TextFieldEvent) -> Unit = remember(onEvent) {
        { event ->
            when (event) {
                is TextFieldEvent.ValueChanged -> onEvent(
                    ProgressSheetEvent.TimeChanged(
                        unit = ProgressTimeUnit.valueOf(event.key),
                        text = event.text,
                    ),
                )

                is TextFieldEvent.Submitted,
                is TextFieldEvent.TrailingClicked,
                is TextFieldEvent.ShakeEnded -> Unit
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            model = medium.hours,
            onEvent = onTimeFieldEvent,
        )

        TimeColon()

        StepperCircle(
            icon = SoftcoverIcon.Remove,
            contentDescription = "Decrease minutes",
            onClick = { onEvent(ProgressSheetEvent.MinutesStepped(delta = -1)) },
            modifier = Modifier.padding(end = Spacing.xxs),
            enabled = medium.canDecreaseMinutes,
        )

        TextField(
            model = medium.minutes,
            onEvent = onTimeFieldEvent,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )

        StepperCircle(
            icon = SoftcoverIcon.Add,
            contentDescription = "Increase minutes",
            onClick = { onEvent(ProgressSheetEvent.MinutesStepped(delta = 1)) },
            modifier = Modifier.padding(start = Spacing.xxs),
            enabled = medium.canIncreaseMinutes,
        )

        TimeColon()

        TextField(
            model = medium.seconds,
            onEvent = onTimeFieldEvent,
        )
    }

    ProgressSuffixAndBar(
        suffix = medium.suffix,
        fraction = medium.fraction,
    )
}

/**
 * A hero number between a minus and a plus stepper, kept within `0..max` (no upper bound when [max] is
 * null): the steppers stop at either end, and a typed number above [max] shakes the field and is
 * dropped rather than clamped. [unitSymbol] is drawn after the number when set.
 */
@Composable
private fun ColumnScope.SteppedNumberContent(
    initial: Int,
    max: Int?,
    charCount: Int,
    noun: String,
    unitSymbol: String?,
    suffix: String?,
    onNumberChange: (String) -> Unit,
) {
    var number by remember {
        mutableStateOf(TextFieldValue(text = initial.toString()))
    }

    LaunchedEffect(number.text) { onNumberChange(number.text) }

    var firstTimeFocusedGained by remember { mutableStateOf(true) }

    val shake = rememberShakeTrigger()

    val parsed = number.text.toIntOrNull() ?: 0

    val fraction = if (max != null && max > 0) {
        parsed.toFloat() / max
    } else {
        0f
    }

    fun stepBy(delta: Int) {
        val stepped = parsed + delta
        val next = if (max != null) stepped.coerceIn(
            0,
            max,
        ) else stepped.coerceAtLeast(0)

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
            icon = SoftcoverIcon.Remove,
            contentDescription = "Decrease $noun",
            onClick = { stepBy(-1) },
            enabled = parsed > 0,
        )

        Spacer(modifier = Modifier.width(Spacing.lg))

        Row(verticalAlignment = Alignment.Bottom) {
            HeroStatNumberField(
                value = number,
                charCount = charCount,
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

                    if (max != null && newNumber > max) {
                        shake.shake()
                        return@HeroStatNumberField
                    }

                    number = newValue
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
                modifier = Modifier
                    .shakeOnError(trigger = shake)
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )

            if (unitSymbol != null) {
                SoftcoverText(
                    text = unitSymbol,
                    modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.xs),
                    style = MaterialTheme.editorialTypography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(modifier = Modifier.width(Spacing.lg))

        StepperCircle(
            icon = SoftcoverIcon.Add,
            contentDescription = "Increase $noun",
            onClick = { stepBy(1) },
            enabled = max == null || parsed < max,
        )
    }

    ProgressSuffixAndBar(
        suffix = suffix,
        fraction = fraction,
    )
}

@Composable
private fun ColumnScope.ProgressSuffixAndBar(
    suffix: String?,
    fraction: Float,
) {
    if (suffix != null) {
        Spacer(modifier = Modifier.height(Spacing.xs))

        EditorialSuffix(text = suffix)

        Spacer(modifier = Modifier.height(Spacing.xxl))

        ProgressBar(fraction = fraction)
    }
}

@Composable
private fun TimeColon() {
    SoftcoverText(
        text = ":",
        modifier = Modifier.padding(horizontal = Spacing.xxs),
        style = MaterialTheme.editorialTypography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
