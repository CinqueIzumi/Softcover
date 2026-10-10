package nl.rhaydus.softcover.feature.reading.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import nl.rhaydus.softcover.core.component.progress.ProgressBar
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
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
        CompositionLocalProvider(LocalContentColor provides foreground) {
            Row {
                StatNumber(
                    model = model.pageLead,
                    modifier = Modifier.alignByBaseline(),
                )

                model.pageTail?.let { tail ->
                    SoftcoverText(
                        text = tail,
                        isLoading = model.isLoading,
                        standIn = tail,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = foreground.copy(alpha = 0.6f),
                        modifier = Modifier.alignByBaseline(),
                    )
                }
            }

            StatNumber(model = model.percent)
        }
    }

    Spacer(modifier = Modifier.height(Spacing.xs))

    ProgressBar(
        fraction = model.fraction,
        isLoading = model.isLoading,
    )
}
