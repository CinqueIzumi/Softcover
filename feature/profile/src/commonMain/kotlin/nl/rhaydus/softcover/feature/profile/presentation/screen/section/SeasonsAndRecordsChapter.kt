package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing

/**
 * The scoped pages-by-month chart under its [header]. The chart cross-fades only when [monthSetKey]
 * changes, because the month labels are then different; otherwise the bars animate in place. When
 * [hasMonthlyPages] is false the chart is dropped and the header's description says why.
 */
@Composable
internal fun SeasonsAndRecordsChapter(
    header: SectionHeaderUiModel,
    chart: ColumnChartUiModel,
    monthSetKey: String?,
    hasMonthlyPages: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ScopedSectionHeader(header = header)

        ScopeEmptySwitch(
            isEmpty = hasMonthlyPages.not() && chart.isLoading.not(),
            empty = {},
            filled = {
                Column {
                    Spacer(modifier = Modifier.height(Spacing.xl))

                    ScopeCrossfade(
                        target = monthSetKey to chart,
                        contentKey = { it.first },
                    ) { (_, scopedChart) ->
                        ColumnChart(model = scopedChart)
                    }
                }
            },
        )
    }
}
