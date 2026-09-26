package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.feature.profile.presentation.screen.YearMetric

private val YEAR_COLUMN_VALUE_LABEL_HEIGHT = 16.dp
private val YEAR_COLUMN_BAR_AREA_HEIGHT = 96.dp
private val YEAR_COLUMN_YEAR_LABEL_HEIGHT = 16.dp
private val YEAR_COLUMN_MIN_BAR_HEIGHT = 12.dp
private val YEAR_COLUMN_ZERO_BAR_HEIGHT = 3.dp
private val YEAR_COLUMN_BAR_WIDTH = 20.dp
private const val YEAR_COLUMN_PEAK_ALPHA = 1f
private const val YEAR_COLUMN_OFF_PEAK_ALPHA = 0.82f
private const val YEAR_COLUMN_ZERO_ALPHA = 0.24f
private const val COMPACT_COUNT_THOUSAND = 1_000
private const val COMPACT_COUNT_MILLION = 1_000_000

/**
 * "The shape of the years" — a Books/Pages toggle over a peak-annotated column chart. The toggle is
 * composable-local state (no TOAD action) since it only picks which of [ReadingLife.booksByYear] /
 * [ReadingLife.pagesByYear] the chart currently reads.
 */
@Composable
internal fun YearColumnHistorySection(
    readingLife: ReadingLife?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    var selectedMetric by remember { mutableStateOf(YearMetric.BOOKS) }

    val data = when (selectedMetric) {
        YearMetric.BOOKS -> readingLife?.booksByYear.orEmpty()
        YearMetric.PAGES -> readingLife?.pagesByYear.orEmpty()
    }

    Column(modifier = modifier) {
        SectionIntro(
            eyebrow = "The shape of the years",
            headline = "Reading, year by year",
        )

        Spacer(modifier = Modifier.height(16.dp))

        YearMetricToggle(
            selected = selectedMetric,
            onSelected = { selectedMetric = it },
            enabled = isLoading.not(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (data.isEmpty() && isLoading.not()) {
            Text(
                text = "Your reading years will chart themselves in as books finish.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            YearColumnChart(
                data = data,
                modifier = Modifier.shimmer(isLoading = isLoading),
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = yearHistoryCaption(
                    data = data,
                    metric = selectedMetric,
                ),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearMetricToggle(
    selected: YearMetric,
    onSelected: (YearMetric) -> Unit,
    enabled: Boolean,
) {
    val metrics = YearMetric.entries

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        metrics.forEachIndexed { index, metric ->
            SegmentedButton(
                selected = metric == selected,
                onClick = { onSelected(metric) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = metrics.size,
                ),
                modifier = Modifier.weight(1f),
                label = {
                    Text(
                        text = metric.label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

/**
 * Renders [data] as-given, one evenly-spaced column per entry, oldest-to-newest — the domain layer now
 * hands this a contiguous, zero-filled 8-year series, so the chart never reorders or drops entries.
 * Every non-zero column carries its own compact value annotation (not just the peak — a shorter
 * current-year bar would otherwise have no readable number), with the peak still called out via full
 * opacity/bold weight. Every column reserves its own fixed-height slot for the value annotation, the
 * bar, and the `'YY` label, so a tall bar can never push a sibling's label out of frame.
 */
@Composable
private fun YearColumnChart(
    data: List<YearCount>,
    modifier: Modifier = Modifier,
) {
    val actualMax = data.maxOfOrNull { it.count } ?: 0
    val maxValue = actualMax.coerceAtLeast(1)
    val peakYear = if (actualMax > 0) data.maxByOrNull { it.count }?.year else null

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        data.forEach { yearCount ->
            val isPeak = yearCount.year == peakYear
            val hasCount = yearCount.count > 0
            val barHeight = if (hasCount) {
                YEAR_COLUMN_MIN_BAR_HEIGHT +
                    (YEAR_COLUMN_BAR_AREA_HEIGHT - YEAR_COLUMN_MIN_BAR_HEIGHT) * (yearCount.count.toFloat() / maxValue)
            } else {
                YEAR_COLUMN_ZERO_BAR_HEIGHT
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier.height(YEAR_COLUMN_VALUE_LABEL_HEIGHT),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (hasCount) {
                        Text(
                            text = formatCompactCount(yearCount.count),
                            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                                fontWeight = if (isPeak) FontWeight.Bold else FontWeight.SemiBold,
                            ),
                            color = if (isPeak) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier.height(YEAR_COLUMN_BAR_AREA_HEIGHT),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .width(YEAR_COLUMN_BAR_WIDTH)
                            .height(barHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = when {
                                        hasCount.not() -> YEAR_COLUMN_ZERO_ALPHA
                                        isPeak -> YEAR_COLUMN_PEAK_ALPHA
                                        else -> YEAR_COLUMN_OFF_PEAK_ALPHA
                                    },
                                ),
                            ),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.height(YEAR_COLUMN_YEAR_LABEL_HEIGHT),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(
                        text = "'${(yearCount.year % 100).toString().padStart(
                            2,
                            '0',
                        )}",
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Abbreviates a bar's value for its in-chart label — plain integers read fine for the small Books
 * counts, but eight full Pages counts (e.g. 7,753) crowd and collide across the column width, so
 * thousands/millions collapse to a one-decimal "7.8k" / "1.2M" form instead.
 */
private fun formatCompactCount(value: Int): String = when {
    value >= COMPACT_COUNT_MILLION -> {
        val millions = formatDecimalNumber(
            value = value / COMPACT_COUNT_MILLION.toDouble(),
            fractionDigits = 1,
        )

        "${millions}M"
    }

    value >= COMPACT_COUNT_THOUSAND -> {
        val thousands = formatDecimalNumber(
            value = value / COMPACT_COUNT_THOUSAND.toDouble(),
            fractionDigits = 1,
        )

        "${thousands}k"
    }

    else -> value.toString()
}

private fun yearHistoryCaption(
    data: List<YearCount>,
    metric: YearMetric,
): String {
    val peak = data.maxByOrNull { it.count }?.takeIf { it.count > 0 }
        ?: return "Your reading years will chart themselves in as books finish."

    val unit = when (metric) {
        YearMetric.BOOKS -> "books"
        YearMetric.PAGES -> "pages"
    }

    return "${peak.year} was your busiest year yet — ${formatGroupedNumber(peak.count)} $unit."
}
