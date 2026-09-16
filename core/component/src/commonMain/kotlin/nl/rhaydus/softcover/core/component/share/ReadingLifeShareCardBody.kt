package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.component.StarRatingInput
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun ReadingLifeShareCardBody(content: ReadingLifeShareCardUiModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "SOFTCOVER",
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(letterSpacing = 3.sp),
            color = LocalContentColor.current,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "A reading life",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(20.dp))

        MiniScallopPortrait(
            avatarUrl = content.avatarUrl,
            readerName = content.readerName,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "THE READER",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current.copy(alpha = 0.75f),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = content.readerName,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = LocalContentColor.current,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TOTAL PAGES READ",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = LocalContentColor.current.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = formatGroupedNumber(content.totalPagesRead),
            style = MaterialTheme.editorialTypography.statHero.copy(
                fontSize = 58.sp,
                lineHeight = 58.sp,
            ),
            color = LocalContentColor.current,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "across ${content.totalBooksRead} books",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.78f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // The genre ranking sits directly under the pages hero — it is the card's second headline,
        // not a footnote, so it precedes the reading-year chart rather than trailing it.
        if (content.topGenres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            ReadingLifeDivider()

            Spacer(modifier = Modifier.height(20.dp))

            ReadingLifeGenreRanking(genres = content.topGenres)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "THE READING YEAR",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = LocalContentColor.current.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "pages, month by month",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = LocalContentColor.current.copy(alpha = 0.75f),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(14.dp))

        ReadingLifeRidgeline(monthlyPages = content.pagesByMonth)

        Spacer(modifier = Modifier.height(20.dp))

        ReadingLifeDivider()

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ReadingLifeFooterStat(
                label = "Avg rating",
                value = formatDecimalNumber(
                    value = content.averageRating,
                    fractionDigits = 1,
                ),
                accessory = {
                    // The share-card exception: on the primary-filled pink card, a gold star reads poorly,
                    // so this tints with the card's own content colour instead — the on-screen profile
                    // stars stay RatingGold.
                    StarRatingInput(
                        rating = content.averageRating,
                        onRatingChange = {},
                        starIcon = drawableIconResource(
                            contentDescription = "",
                            icon = SoftcoverIcon.StarFilled,
                        ),
                        filledColor = LocalContentColor.current,
                        enabled = false,
                        starSize = 10.dp,
                    )
                },
            )

            ReadingLifeFooterStat(
                label = "Day streak",
                value = content.dayStreak.toString(),
            )

            ReadingLifeFooterStat(
                label = "Tracked yrs",
                value = content.trackedYears.toString(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MiniScallopPortrait(
    avatarUrl: String?,
    readerName: String,
) {
    val shape = MaterialShapes.Cookie12Sided.toShape()
    val rimColor = LocalContentColor.current

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(shape)
            .background(rimColor.copy(alpha = 0.16f))
            .border(
                width = 3.dp,
                color = rimColor,
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (avatarUrl != null) {
            RhaydusShimmerImage(
                model = avatarUrl,
                contentDescription = "$readerName's portrait",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = readingLifeInitials(readerName = readerName),
                style = MaterialTheme.editorialTypography.titleSmall,
                color = rimColor,
            )
        }
    }
}

/**
 * The pages-by-month sparkline: a polyline over its own translucent area fill, drawn against the
 * card's [LocalContentColor] (always `onPrimary`, since the card is `primary`-filled) so it reads
 * correctly in both themes without a hex value. [monthlyPages] is normalized to twelve entries first
 * (see [normalizedReadingLifeMonths]) so a partial or empty history still renders a flat baseline
 * instead of a malformed chart.
 */
@Composable
private fun ReadingLifeRidgeline(monthlyPages: List<Int>) {
    val values = normalizedReadingLifeMonths(monthlyPages = monthlyPages)
    val maxValue = values.max().coerceAtLeast(1)
    val lineColor = LocalContentColor.current.copy(alpha = 0.9f)
    val areaColor = LocalContentColor.current.copy(alpha = 0.22f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
        ) {
            val topInset = size.height * 0.12f
            val plotHeight = size.height - topInset
            val lastIndex = values.lastIndex

            val points = values.mapIndexed { index, value ->
                val x = size.width * index / lastIndex
                val fraction = value / maxValue.toFloat()
                val y = size.height - (fraction * plotHeight)

                Offset(
                    x = x,
                    y = y,
                )
            }

            val linePath = Path().apply {
                points.forEachIndexed { index, point ->
                    if (index == 0) {
                        moveTo(
                            point.x,
                            point.y,
                        )
                    } else {
                        lineTo(
                            point.x,
                            point.y,
                        )
                    }
                }
            }

            val areaPath = Path().apply {
                moveTo(
                    points.first().x,
                    size.height,
                )

                points.forEach { point ->
                    lineTo(
                        point.x,
                        point.y,
                    )
                }

                lineTo(
                    points.last().x,
                    size.height,
                )

                close()
            }

            drawPath(
                path = areaPath,
                color = areaColor,
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Only the endpoints are labelled — twelve three-letter month labels crowded under a 12-point
        // sparkline read as noise, so this keeps just "JAN" at the left and "DEC" at the right.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "JAN",
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(fontSize = 8.sp),
                color = LocalContentColor.current.copy(alpha = 0.65f),
            )

            Text(
                text = "DEC",
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(fontSize = 8.sp),
                color = LocalContentColor.current.copy(alpha = 0.65f),
            )
        }
    }
}

@Composable
private fun ReadingLifeGenreRanking(genres: List<ReadingLifeGenre>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "WHAT YOU READ MOST",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = LocalContentColor.current.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(10.dp))

        genres.forEachIndexed { index, genre ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(6.dp))
            }

            ReadingLifeGenreRow(
                genre = genre,
                isLeading = index == 0,
            )
        }
    }
}

// The leading genre carries the section: name and share both step up a size and drop the alpha
// knock-back the runners-up take, so the reader's top genre is legible at a glance on the poster.
@Composable
private fun ReadingLifeGenreRow(
    genre: ReadingLifeGenre,
    isLeading: Boolean,
) {
    val nameStyle = if (isLeading) {
        MaterialTheme.editorialTypography.headlineSmall
    } else {
        MaterialTheme.editorialTypography.body
    }

    val contentAlpha = if (isLeading) 1f else 0.78f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = genre.name,
            style = nameStyle,
            color = LocalContentColor.current.copy(alpha = contentAlpha),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${genre.percentage}%",
            style = nameStyle,
            color = LocalContentColor.current.copy(alpha = contentAlpha),
        )
    }
}

@Composable
private fun ReadingLifeFooterStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accessory: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.editorialTypography.headlineSmall.copy(
                fontSize = 22.sp,
                lineHeight = 24.sp,
                fontFeatureSettings = TABULAR_NUMS,
            ),
            color = LocalContentColor.current,
        )

        if (accessory != null) {
            Spacer(modifier = Modifier.height(4.dp))

            accessory()
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(fontSize = 9.sp),
            color = LocalContentColor.current.copy(alpha = 0.75f),
        )
    }
}

@Composable
private fun ReadingLifeDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LocalContentColor.current.copy(alpha = 0.25f)),
    )
}

private fun normalizedReadingLifeMonths(monthlyPages: List<Int>): List<Int> {
    val twelve = monthlyPages.take(READING_LIFE_MONTHS_IN_YEAR)

    return if (twelve.size == READING_LIFE_MONTHS_IN_YEAR) {
        twelve
    } else {
        twelve + List(READING_LIFE_MONTHS_IN_YEAR - twelve.size) { 0 }
    }
}

private fun readingLifeInitials(readerName: String): String {
    val letters = readerName
        .trim()
        .split(" ")
        .mapNotNull { word -> word.firstOrNull()?.uppercaseChar() }

    return when {
        letters.isEmpty() -> "?"
        letters.size == 1 -> letters.first().toString()
        else -> "${letters.first()}${letters.last()}"
    }
}

private const val READING_LIFE_MONTHS_IN_YEAR = 12

private val ReadingLifePreviewContent = ShareCardUiModel.previews.filterIsInstance<ReadingLifeShareCardUiModel>().single()

@StandardPreview
@Composable
private fun ReadingLifeShareCardLightPreview() {
    SoftcoverTheme(darkTheme = false) {
        ShareCard(content = ReadingLifePreviewContent)
    }
}

@StandardPreview
@Composable
private fun ReadingLifeShareCardDarkPreview() {
    SoftcoverTheme(darkTheme = true) {
        ShareCard(content = ReadingLifePreviewContent)
    }
}
