package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.chart.ProportionChart
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.profile.presentation.state.SpendBlockUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.SpendingChapterUiModel

@Composable
internal fun SpendingChapter(
    model: SpendingChapterUiModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.animateScopeContentSize()) {
        ScopedSectionHeader(header = model.header)

        ScopeCrossfade(
            target = model,
            contentKey = { it.emptyLine != null },
        ) { shown ->
            Column {
                Spacer(modifier = Modifier.height(Spacing.md))

                if (shown.emptyLine != null) {
                    FadeThroughText(
                        text = shown.emptyLine,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    SpendingContent(model = shown)
                }
            }
        }
    }
}

@Composable
private fun SpendingContent(model: SpendingChapterUiModel) {
    val spent = model.spent
    val typical = model.typical

    if (spent != null && typical != null) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            VersusCell(
                model = spent,
                modifier = Modifier.weight(1f),
            )

            VersusCell(
                model = typical,
                modifier = Modifier.weight(1f),
            )
        }
    }

    model.blocks.forEach { block ->
        Spacer(modifier = Modifier.height(Spacing.xl))

        SpendBlock(block = block)
    }
}

@Composable
private fun SpendBlock(block: SpendBlockUiModel) {
    Column {
        SoftcoverText(
            text = block.title.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(Spacing.sm))

        ProportionChart(model = block.chart)
    }
}
