package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The chapter under its [header]: the share of pages read in each three-hour block of the day. The chart
 * does not cross-fade, because its labels are the same in every scope and only the bars move. When
 * [hasTimeOfDayPages] is false the chart is dropped and [caption] says why.
 */
@Composable
internal fun YearAtAGlanceChapter(
    header: SectionHeaderUiModel,
    title: String,
    caption: String,
    chart: ColumnChartUiModel,
    footnote: String,
    hasTimeOfDayPages: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ScopedSectionHeader(header = header)

        Spacer(modifier = Modifier.height(Spacing.xl))

        AtlasSubhead(
            title = title,
            caption = caption,
            isLoading = chart.isLoading,
        )

        ScopeEmptySwitch(
            isEmpty = hasTimeOfDayPages.not() && chart.isLoading.not(),
            empty = {},
            filled = {
                Column {
                    Spacer(modifier = Modifier.height(Spacing.md))

                    ColumnChart(model = chart)

                    Spacer(modifier = Modifier.height(Spacing.md))

                    SoftcoverText(
                        text = footnote,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
    }
}
