package nl.rhaydus.softcover.core.component.statistic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.theme.motion

private const val TABULAR_NUMS = "tnum"

/**
 * The canonical way to render a numeric stat that can change in place. Raw `Text` for a number that
 * can change should be treated as a bug. Springs between values when [StatNumberUiModel.value] changes,
 * and counts up from 0 when loading ends; a loading model's value is never shown.
 *
 * The text is locked to tabular figures so digits don't shift width while it springs. When the user
 * has disabled system animations the number snaps.
 */
@Composable
fun StatNumber(
    model: StatNumberUiModel,
    modifier: Modifier = Modifier,
) {
    val typography = MaterialTheme.editorialTypography

    val style = when (model.variant) {
        StatNumberVariant.Standard -> typography.statLarge

        StatNumberVariant.Average -> typography.statLarge.copy(
            fontSize = 52.sp,
            lineHeight = 52.sp,
        )

        StatNumberVariant.Hero -> typography.statHero

        StatNumberVariant.Headline -> typography.headlineSmall

        StatNumberVariant.BodyStrong -> MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Medium,
        )

        StatNumberVariant.Body -> typography.body

        StatNumberVariant.BodySmall -> typography.bodySmall

        StatNumberVariant.Label -> MaterialTheme.typography.labelSmall
    }

    StatNumber(
        model = model,
        style = style,
        autoSize = null,
        modifier = modifier,
    )
}

@Composable
internal fun StatNumber(
    model: StatNumberUiModel,
    style: TextStyle,
    autoSize: TextAutoSize?,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(
        targetValue = if (model.isLoading) 0f else model.value.toFloat(),
        animationSpec = MaterialTheme.motion.valueChange,
        label = "StatNumber",
    )

    val displayValue = animated.toDouble()

    SoftcoverText(
        text = remember(displayValue, model) { model.textAt(displayValue) },
        color = LocalContentColor.current,
        style = style.copy(fontFeatureSettings = TABULAR_NUMS),
        autoSize = autoSize,
        maxLines = 1,
        isLoading = model.isLoading,
        standIn = model.standIn(),
        modifier = modifier,
    )
}

internal fun StatNumberUiModel.textAt(displayValue: Double): String =
    prefix + format.render(displayValue) + suffix.render(integer = displayValue.toInt())

internal fun StatNumberUiModel.standIn(): String = prefix + format.standIn() + suffix.render(integer = 0)

private fun StatNumberSuffix?.render(integer: Int): String = when (this) {
    null -> ""
    is StatNumberSuffix.Fixed -> text
    is StatNumberSuffix.Plural -> if (integer == 1) one else other
}

private fun StatNumberFormat.standIn(): String = when (this) {
    StatNumberFormat.Grouped -> "0,000"
    StatNumberFormat.Plain -> "00"
    StatNumberFormat.Decimal -> "0.0"
    StatNumberFormat.Duration -> "0h 00m"
}

private fun StatNumberFormat.render(value: Double): String = when (this) {
    StatNumberFormat.Grouped -> formatGroupedNumber(value.toInt())
    StatNumberFormat.Plain -> value.toInt().toString()
    StatNumberFormat.Decimal -> formatDecimalNumber(
        value = value,
        fractionDigits = 1,
    )
    StatNumberFormat.Duration -> secondsToHm(value.toInt())
}
