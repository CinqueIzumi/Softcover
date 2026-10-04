package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.softcover.core.component.chart.ColumnChart
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.divider.VerticalDivider
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumber
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * "How you score" — a number-forward average and a half-star histogram, with [header] and [caption]
 * supplying the band-driven copy.
 */
@Composable
internal fun RatingsHistogramSection(
    chart: ColumnChartUiModel,
    average: StatNumberUiModel,
    totalRatings: Int,
    isLoading: Boolean,
    header: SectionHeaderUiModel,
    caption: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionHeader(model = header)

        Spacer(modifier = Modifier.height(20.dp))

        if (totalRatings == 0 && isLoading.not()) {
            SoftcoverText(
                text = "Rate a few books you've finished and this fills in.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            RatingsAverageRow(
                average = average,
                totalRatings = totalRatings,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(24.dp))

            ColumnChart(model = chart)

            Spacer(modifier = Modifier.height(16.dp))

            SoftcoverText(
                text = caption,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RatingsAverageRow(
    average: StatNumberUiModel,
    totalRatings: Int,
    isLoading: Boolean,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.Bottom) {
            StatNumber(
                model = average,
                style = MaterialTheme.editorialTypography.statLarge.copy(
                    fontSize = 52.sp,
                    lineHeight = 52.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            SoftcoverText(
                text = "/5",
                style = MaterialTheme.editorialTypography.headlineSmall.copy(fontSize = 19.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp),
            )

            Spacer(modifier = Modifier.width(6.dp))

            val star = drawableIconResource(
                icon = SoftcoverIcon.StarFilled,
                contentDescription = "",
            )

            Icon(
                painter = star.getIconPainter(),
                contentDescription = star.contentDescription,
                tint = RatingGold,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterVertically),
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        VerticalDivider(modifier = Modifier.height(32.dp))

        Spacer(modifier = Modifier.width(20.dp))

        SoftcoverText(
            text = "AVERAGE ACROSS $totalRatings BOOKS",
            isLoading = isLoading,
            standIn = "AVERAGE ACROSS 00 BOOKS",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
