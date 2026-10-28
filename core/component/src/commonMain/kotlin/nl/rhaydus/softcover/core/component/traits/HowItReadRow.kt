package nl.rhaydus.softcover.core.component.traits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun HowItReadRow(row: HowItReadRowUiModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SoftcoverText(
                text = row.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SoftcoverText(
                text = row.value,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }

        if (row is HowItReadRowUiModel.Scale) {
            HowItReadScale(row = row)
        }
    }
}

@Composable
private fun HowItReadScale(row: HowItReadRowUiModel.Scale) {
    val dotSize = 10.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(dotSize),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        Box(
            modifier = Modifier
                .align(
                    BiasAlignment(
                        horizontalBias = row.position.coerceIn(0f..1f) * 2f - 1f,
                        verticalBias = 0f,
                    ),
                )
                .size(dotSize)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                ),
        )
    }

    HowItReadScaleLabels(
        startLabel = row.startLabel,
        centreLabel = row.centreLabel,
        endLabel = row.endLabel,
        modifier = Modifier.padding(vertical = Spacing.xxs),
    )
}

/** The three captions under a spectrum: the start end, the midpoint and the finish end. */
@Composable
internal fun HowItReadScaleLabels(
    startLabel: String,
    centreLabel: String,
    endLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        HowItReadScaleLabel(
            text = startLabel,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f),
        )

        HowItReadScaleLabel(
            text = centreLabel,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )

        HowItReadScaleLabel(
            text = endLabel,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HowItReadScaleLabel(
    text: String,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
) {
    SoftcoverText(
        text = text,
        style = MaterialTheme.editorialTypography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = textAlign,
        modifier = modifier,
    )
}
