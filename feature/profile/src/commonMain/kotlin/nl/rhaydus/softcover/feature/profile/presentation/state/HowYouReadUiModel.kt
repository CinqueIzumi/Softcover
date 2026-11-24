package nl.rhaydus.softcover.feature.profile.presentation.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.chart.ScoreBarsUiModel
import nl.rhaydus.softcover.core.component.chip.ChipSet

/**
 * The How you read and Moods you reach for blocks of chapter 03. A null block has no answers in scope and is
 * hidden; [emptyLine] is set instead of all four when none has, and the loading stand-in has none.
 */
@Immutable
internal data class HowYouReadUiModel(
    val split: ChartBlockUiModel?,
    val pace: ChartBlockUiModel?,
    val writing: WritingBlockUiModel?,
    val moods: MoodsBlockUiModel?,
    val emptyLine: String?,
    val isLoading: Boolean,
)

@Immutable
internal data class ChartBlockUiModel(
    val title: String,
    val caption: String,
    val chart: ProportionChartUiModel,
)

@Immutable
internal data class WritingBlockUiModel(
    val title: String,
    val styles: ChipSet<Unit>,
    val flags: ChipSet<Unit>,
    val caption: String,
)

@Immutable
internal data class MoodsBlockUiModel(
    val title: String,
    val caption: String,
    val bars: ScoreBarsUiModel,
)
