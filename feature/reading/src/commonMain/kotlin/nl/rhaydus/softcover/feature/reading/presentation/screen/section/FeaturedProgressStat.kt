package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.progress.ProgressBar
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.reading.presentation.state.FeaturedProgressUiModel

/** The page position beside the "NN%" stat, then a wavy bar. */
@Composable
internal fun FeaturedProgressStat(
    model: FeaturedProgressUiModel,
) {
    val foreground = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        val pageLabel = pageLabel(
            lead = model.pageLead,
            tail = model.pageTail,
            foreground = foreground,
        )

        SoftcoverText(
            text = pageLabel,
            isLoading = model.isLoading,
            standIn = pageLabel.text,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = foreground,
        )

        SoftcoverText(
            text = model.percent,
            isLoading = model.isLoading,
            standIn = model.percent,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = foreground,
        )
    }

    Spacer(modifier = Modifier.height(9.dp))

    ProgressBar(
        fraction = model.fraction,
        isLoading = model.isLoading,
    )
}

/** [lead] at full [foreground] alpha followed by [tail] demoted to 0.6 alpha, in one line. */
@Composable
private fun pageLabel(
    lead: String,
    tail: String?,
    foreground: Color,
): AnnotatedString {
    val dimColor = foreground.copy(alpha = 0.6f)

    return buildAnnotatedString {
        append(lead)

        if (tail != null) {
            withStyle(SpanStyle(color = dimColor)) {
                append(tail)
            }
        }
    }
}
