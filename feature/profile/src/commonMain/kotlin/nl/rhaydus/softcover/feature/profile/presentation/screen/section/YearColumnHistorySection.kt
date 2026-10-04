package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControl
import nl.rhaydus.softcover.core.component.control.SegmentedControlEvent
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric

/** "The shape of the years" — a Books/Pages toggle over a peak-annotated column chart. */
@Composable
internal fun YearColumnHistorySection(
    chart: ColumnChartUiModel,
    caption: String,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    metricToggle: SegmentedControlUiModel<YearMetric>,
    onMetricSelected: (YearMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(16.dp))

        SegmentedControl(
            model = metricToggle,
            onEvent = { event ->
                when (event) {
                    is SegmentedControlEvent.Selected -> onMetricSelected(event.key)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (chart.columns.isEmpty() && isLoading.not()) {
            SoftcoverText(
                text = caption,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                isLoading = isLoading,
                standIn = "2024 was your busiest year yet — 00 books.",
            )
        } else {
            ColumnChart(model = chart)

            Spacer(modifier = Modifier.height(10.dp))

            Divider(model = DividerUiModel.Horizontal())

            Spacer(modifier = Modifier.height(10.dp))

            SoftcoverText(
                text = caption,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                isLoading = isLoading,
                standIn = "2024 was your busiest year yet — 00 books.",
            )
        }
    }
}

