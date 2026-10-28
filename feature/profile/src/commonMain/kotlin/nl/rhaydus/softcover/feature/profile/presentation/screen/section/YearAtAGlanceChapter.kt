package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.YearGrid
import nl.rhaydus.softcover.core.component.chart.YearGridEvent
import nl.rhaydus.softcover.core.component.chart.YearGridUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTile
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/** The time-of-day chart does not cross-fade, because its labels are the same in every scope and only the bars move. */
@Composable
internal fun YearAtAGlanceChapter(
    header: SectionHeaderUiModel,
    title: String,
    caption: String,
    chart: ColumnChartUiModel,
    yearGrid: YearGridUiModel,
    currentStreakTile: StatTileUiModel,
    longestStreakTile: StatTileUiModel,
    onDayTapped: (LocalDate) -> Unit,
    footnote: String,
    hasTimeOfDayPages: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ScopedSectionHeader(header = header)

        Spacer(modifier = Modifier.height(Spacing.md))

        ScopeCrossfade(
            target = yearGrid,
            contentKey = { it.year },
        ) { grid ->
            YearGrid(
                model = grid,
                onEvent = { event ->
                    when (event) {
                        is YearGridEvent.DayTapped -> onDayTapped(event.date)
                    }
                },
            )
        }

        Spacer(modifier = Modifier.height(Spacing.md))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            StatTile(
                model = currentStreakTile,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )

            StatTile(
                model = longestStreakTile,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }

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
