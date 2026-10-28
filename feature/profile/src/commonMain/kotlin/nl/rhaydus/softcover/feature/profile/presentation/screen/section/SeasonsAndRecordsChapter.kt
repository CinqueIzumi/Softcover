package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCard
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionChart
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.feature.profile.presentation.state.TimeToFinishUiModel

/**
 * The scoped pages-by-month chart under its [header]. The chart cross-fades only when [monthSetKey]
 * changes, because the month labels are then different; otherwise the bars animate in place. When
 * [hasMonthlyPages] is false the chart is dropped and the header's description says why. Below it the
 * [topAuthorRows] sit under their own subhead, and are dropped when [hasTopAuthors] is false. The
 * [formatSplitChart] follows under its own subhead and animates in place across scopes, and the
 * [timeToFinish] tiles follow it.
 */
@Composable
internal fun SeasonsAndRecordsChapter(
    header: SectionHeaderUiModel,
    chart: ColumnChartUiModel,
    monthSetKey: String?,
    hasMonthlyPages: Boolean,
    topAuthorsTitle: String,
    topAuthorsCaption: String,
    topAuthorRows: ImmutableList<BookCardUiModel>,
    hasTopAuthors: Boolean,
    formatSplitTitle: String,
    formatSplitCaption: String,
    formatSplitChart: ProportionChartUiModel,
    hasFormatSplit: Boolean,
    timeToFinish: TimeToFinishUiModel,
    modifier: Modifier = Modifier,
) {
    val isLoadingAuthors = topAuthorRows.any { it.isLoading }

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

        Spacer(modifier = Modifier.height(Spacing.xl))

        AtlasSubhead(
            title = topAuthorsTitle,
            caption = topAuthorsCaption,
            isLoading = isLoadingAuthors,
        )

        ScopeEmptySwitch(
            isEmpty = hasTopAuthors.not() && isLoadingAuthors.not(),
            empty = {},
            filled = {
                Column {
                    Spacer(modifier = Modifier.height(Spacing.md))

                    ScopeCrossfade(target = topAuthorRows) { rows ->
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            rows.forEach { row ->
                                BookCard(
                                    model = row,
                                    onEvent = {},
                                )
                            }
                        }
                    }
                }
            },
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        AtlasSubhead(
            title = formatSplitTitle,
            caption = formatSplitCaption,
            isLoading = formatSplitChart.isLoading,
        )

        ScopeEmptySwitch(
            isEmpty = hasFormatSplit.not() && formatSplitChart.isLoading.not(),
            empty = {},
            filled = {
                Column {
                    Spacer(modifier = Modifier.height(Spacing.md))

                    ProportionChart(model = formatSplitChart)
                }
            },
        )

        Spacer(modifier = Modifier.height(Spacing.xl))

        TimeToFinishSection(model = timeToFinish)
    }
}
