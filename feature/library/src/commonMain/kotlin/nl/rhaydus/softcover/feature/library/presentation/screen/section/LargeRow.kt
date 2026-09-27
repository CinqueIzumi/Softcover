package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScaleCombinedClickable
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryLine
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import kotlin.math.roundToInt

/**
 * The redesign's List (large) row: de-carded (no `surfaceContainer` Surface — the old card look) in
 * favour of a hairline top divider, matching the spec's flattened list anatomy. Cover, series eyebrow,
 * title, byline, meta line, a [DeadlineSummaryLine] whenever the book carries a tracked deadline, and
 * — while [progressFraction] is non-null — the wavy shelf-progress line + percentage. Edition rows
 * (custom lists) have no deadlines and pass no [deadlineSummary].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LargeRow(
    title: String,
    authorName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    seriesText: String? = null,
    releaseYear: Int? = null,
    usersCount: Int? = null,
    rating: Double? = null,
    progressFraction: Float? = null,
    deadlineSummary: DeadlineSummaryUiModel? = null,
    trailing: (@Composable () -> Unit)? = null,
    cover: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerHandCursor()
            .pressScaleCombinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // The deadline readout sits beneath the whole row rather than inside the weighted text
        // column: its width is unpredictable (the reader's own date format plus an unbounded pace)
        // and grows with the system font scale, so in that column it wrapped on narrower devices.
        Column(modifier = Modifier.padding(vertical = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                cover(
                    Modifier
                        .width(74.dp)
                        .aspectRatio(ratio = 2f / 3f),
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    seriesText?.takeIf { it.isNotBlank() }?.let { series ->
                        Text(
                            text = series.uppercase(),
                            style = MaterialTheme.editorialTypography.eyebrowSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.editorialTypography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (authorName.isNotBlank()) {
                        Text(
                            text = "By $authorName".uppercase(),
                            style = MaterialTheme.editorialTypography.eyebrowSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    val hasRating = rating != null && rating != 0.0

                    val statsLabel = listOfNotNull(
                        releaseYear?.takeIf { it != -1 }?.toString(),
                        usersCount?.let { "$it readers" },
                        rating?.takeIf { it != 0.0 }?.toString(),
                    ).joinToString(separator = " · ")

                    if (statsLabel.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = statsLabel,
                                style = MaterialTheme.editorialTypography.bodySmall.copy(fontFeatureSettings = "tnum"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            if (hasRating) {
                                Spacer(modifier = Modifier.width(4.dp))

                                val starIcon = drawableIconResource(
                                    icon = SoftcoverIcon.StarFilled,
                                    contentDescription = "",
                                )

                                Icon(
                                    painter = starIcon.getIconPainter(),
                                    contentDescription = starIcon.contentDescription,
                                    tint = RatingGold,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }

                    if (progressFraction != null) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            LinearWavyProgressIndicator(
                                progress = { progressFraction.coerceIn(
                                    0f,
                                    1f,
                                ) },
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(8.dp),
                            )

                            Text(
                                text = "${(progressFraction * 100f).roundToInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                if (trailing != null) {
                    Spacer(modifier = Modifier.width(8.dp))

                    trailing()
                }
            }

            if (deadlineSummary != null) {
                Spacer(modifier = Modifier.height(12.dp))

                DeadlineSummaryLine(model = deadlineSummary)
            }
        }
    }
}
