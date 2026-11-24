package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.modifier.skeletonMask
import nl.rhaydus.designsystem.theme.Spacing
import nl.rhaydus.softcover.core.component.chart.ProportionChart
import nl.rhaydus.softcover.core.component.chart.ScoreBars
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.text.FadeThroughText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.profile.presentation.state.ChartBlockUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.HowYouReadUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.MoodsBlockUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.WritingBlockUiModel

@Composable
internal fun HowYouReadSection(
    model: HowYouReadUiModel,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.animateScopeContentSize()) {
        ScopeEmptySwitch(
            isEmpty = model.emptyLine != null,
            empty = {
                FadeThroughText(
                    text = model.emptyLine.orEmpty(),
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            filled = {
                ScopeCrossfade(
                    target = model,
                    contentKey = { listOf(it.split != null, it.pace != null, it.writing != null, it.moods != null) },
                ) { shown ->
                    HowYouReadContent(
                        model = shown,
                        isLoading = isLoading,
                    )
                }
            },
        )
    }
}

@Composable
private fun HowYouReadContent(
    model: HowYouReadUiModel,
    isLoading: Boolean,
) {
    Column {
        model.split?.let {
            SplitBlock(
                block = it,
                isLoading = isLoading,
            )
        }

        model.pace?.let {
            BlockGap(isFirst = model.split == null)

            PaceBlock(
                block = it,
                isLoading = isLoading,
            )
        }

        model.writing?.let {
            BlockGap(isFirst = model.split == null && model.pace == null)

            WritingBlock(
                block = it,
                isLoading = isLoading,
            )
        }

        model.moods?.let {
            BlockGap(isFirst = model.split == null && model.pace == null && model.writing == null)

            MoodsBlock(
                block = it,
                isLoading = isLoading,
            )
        }
    }
}

@Composable
private fun BlockGap(isFirst: Boolean) {
    if (isFirst.not()) {
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

@Composable
private fun SplitBlock(
    block: ChartBlockUiModel,
    isLoading: Boolean,
) {
    Column {
        AtlasSubhead(
            title = block.title,
            caption = block.caption,
            isLoading = isLoading,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        ProportionChart(model = block.chart)
    }
}

@Composable
private fun PaceBlock(
    block: ChartBlockUiModel,
    isLoading: Boolean,
) {
    Column {
        BlockEyebrow(title = block.title)

        Spacer(modifier = Modifier.height(Spacing.sm))

        ProportionChart(model = block.chart)

        Spacer(modifier = Modifier.height(Spacing.sm))

        BlockCaption(
            text = block.caption,
            isLoading = isLoading,
        )
    }
}

@Composable
private fun WritingBlock(
    block: WritingBlockUiModel,
    isLoading: Boolean,
) {
    Column {
        BlockEyebrow(title = block.title)

        Spacer(modifier = Modifier.height(Spacing.sm))

        if (block.styles.isEmpty.not()) {
            ChipFlow(
                set = block.styles,
                onEvent = { _, _ -> },
                modifier = Modifier.skeletonMask(visible = isLoading),
            )
        }

        if (block.styles.isEmpty.not() && block.flags.isEmpty.not()) {
            Spacer(modifier = Modifier.height(Spacing.xs))
        }

        if (block.flags.isEmpty.not()) {
            ChipFlow(
                set = block.flags,
                onEvent = { _, _ -> },
                modifier = Modifier.skeletonMask(visible = isLoading),
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        BlockCaption(
            text = block.caption,
            isLoading = isLoading,
        )
    }
}

@Composable
private fun MoodsBlock(
    block: MoodsBlockUiModel,
    isLoading: Boolean,
) {
    Column {
        AtlasSubhead(
            title = block.title,
            caption = block.caption,
            isLoading = isLoading,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        ScoreBars(model = block.bars)
    }
}

@Composable
private fun BlockEyebrow(title: String) {
    SoftcoverText(
        text = title.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun BlockCaption(
    text: String,
    isLoading: Boolean,
) {
    FadeThroughText(
        text = text,
        style = MaterialTheme.editorialTypography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        isLoading = isLoading,
        standIn = text,
    )
}
