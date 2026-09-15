package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.designsystem.component.StarRatingInput
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.richtext.RichText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

private const val TABULAR_NUMS = "tnum"

@Composable
fun ShareCard(
    content: ShareCardUiModel,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = when (content) {
        is StatShareCardUiModel,
        is ReadingLifeShareCardUiModel -> MaterialTheme.colorScheme.primary
        is BookShareCardUiModel,
        is ReadingUpdateShareCardUiModel,
        is QuoteShareCardUiModel,
        is YearRecapShareCardUiModel -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (content) {
        is StatShareCardUiModel,
        is ReadingLifeShareCardUiModel -> MaterialTheme.colorScheme.onPrimary
        is BookShareCardUiModel,
        is ReadingUpdateShareCardUiModel,
        is QuoteShareCardUiModel,
        is YearRecapShareCardUiModel -> MaterialTheme.colorScheme.onSurface
    }

    val dimensions = ShareCardDimensions.forContent(content = content)

    val sizeModifier = if (dimensions.fixedHeight != null) {
        Modifier.requiredSize(
            width = dimensions.width,
            height = dimensions.fixedHeight,
        )
    } else {
        Modifier
            .requiredWidth(width = dimensions.width)
            .requiredHeightIn(min = dimensions.minHeight)
    }

    Surface(
        modifier = modifier
            .then(other = sizeModifier)
            .clip(RoundedCornerShape(20.dp)),
        color = surfaceColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensions.padding),
        ) {
            when (content) {
                is BookShareCardUiModel -> BookShareCardBody(content)
                is ReadingUpdateShareCardUiModel -> ReadingUpdateShareCardBody(content)
                is StatShareCardUiModel -> StatShareCardBody(content)
                is QuoteShareCardUiModel -> QuoteShareCardBody(content)
                is YearRecapShareCardUiModel -> YearRecapShareCardBody(content)
                is ReadingLifeShareCardUiModel -> ReadingLifeShareCardBody(content)
            }

            Spacer(modifier = Modifier.weight(1f))

            ShareCardSignOff()
        }
    }
}

@Composable
private fun BookShareCardBody(content: BookShareCardUiModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "FROM THE SHELF",
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp)),
        ) {
            RhaydusShimmerImage(
                model = content.coverUrl,
                contentDescription = "Cover of ${content.title}",
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = content.title,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = LocalContentColor.current,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        if (content.author.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "BY ${content.author.uppercase()}",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        val statsLine = buildBookStatsLine(content = content)

        if (statsLine != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = statsLine,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        if (content.description != null && content.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = content.description,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (content.quote != null) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "“${content.quote}”",
                style = MaterialTheme.editorialTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun buildBookStatsLine(content: BookShareCardUiModel): String? {
    val parts = buildList {
        content.communityRating?.takeIf { it > 0.0 }?.let {
            val tenths = (it * 10).roundToInt()

            add("★ ${tenths / 10}.${tenths % 10}")
        }
        content.userRating?.let { add("Your $it/10") }
        content.releaseYear?.takeIf { it != -1 }?.let { add(it.toString()) }
        content.pageCount?.takeIf { it > 0 }?.let { add("$it pages") }
    }

    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = " · ")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadingUpdateShareCardBody(content: ReadingUpdateShareCardUiModel) {
    val eyebrow = when (content.kind) {
        ReadingUpdateKind.FINISHED -> "FROM MY SHELF"
        ReadingUpdateKind.READING -> "CURRENTLY READING"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = eyebrow,
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .width(140.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp)),
        ) {
            RhaydusShimmerImage(
                model = content.coverUrl,
                contentDescription = "Cover of ${content.title}",
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = content.title,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = LocalContentColor.current,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        if (content.author.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "BY ${content.author.uppercase()}",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        ReadingUpdateReaderIdentity(
            username = content.username,
            avatarUrl = content.avatarUrl,
        )

        if (content.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    Alignment.CenterHorizontally,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                content.tags.forEach { tag ->
                    Chip(
                        model = ChipUiModel(
                            key = tag,
                            label = tag,
                            clickable = false,
                        ),
                    )
                }
            }
        }

        when (content.kind) {
            ReadingUpdateKind.FINISHED -> {
                if (content.ratingStars != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    StarRatingInput(
                        rating = content.ratingStars,
                        onRatingChange = {},
                        starIcon = drawableIconResource(
                            contentDescription = "",
                            icon = SoftcoverIcon.StarFilled,
                        ),
                        filledColor = RatingGold,
                        enabled = false,
                        starSize = 20.dp,
                    )
                }

                if (content.review != null) {
                    Spacer(modifier = Modifier.height(20.dp))

                    RichText(
                        model = content.review,
                        style = MaterialTheme.editorialTypography.body.copy(
                            fontStyle = FontStyle.Normal,
                            textAlign = TextAlign.Center,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            ReadingUpdateKind.READING -> {
                if (content.progressLabel != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = content.progressLabel,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadingUpdateReaderIdentity(
    username: String,
    avatarUrl: String?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (avatarUrl != null) {
            Box(
                modifier = Modifier
                    .requiredSize(28.dp)
                    .clip(CircleShape),
            ) {
                RhaydusShimmerImage(
                    model = avatarUrl,
                    contentDescription = "$username's avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))
        }

        Text(
            text = username,
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current,
        )
    }
}

@Composable
private fun StatShareCardBody(content: StatShareCardUiModel) {
    Text(
        text = content.eyebrow.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrow,
        color = LocalContentColor.current.copy(alpha = 0.75f),
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = content.value.toString(),
        style = MaterialTheme.editorialTypography.statHero.copy(fontFeatureSettings = TABULAR_NUMS),
        color = LocalContentColor.current,
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = content.caption,
        style = MaterialTheme.editorialTypography.body,
        color = LocalContentColor.current.copy(alpha = 0.75f),
    )
}

@Composable
private fun QuoteShareCardBody(content: QuoteShareCardUiModel) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            modifier = Modifier.align(Alignment.TopStart),
        )

        Column(modifier = Modifier.padding(top = 40.dp)) {
            Text(
                text = content.quote,
                style = MaterialTheme.editorialTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(20.dp))

            val byline = buildString {
                append(content.sourceAuthor.uppercase())
                append(" — ")
                append(content.sourceTitle.uppercase())

                if (content.page != null) {
                    append(" · p. ")
                    append(content.page)
                }
            }

            Text(
                text = byline,
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun YearRecapShareCardBody(content: YearRecapShareCardUiModel) {
    Text(
        text = "${content.eyebrow.uppercase()} · ${content.year}",
        style = MaterialTheme.editorialTypography.eyebrow,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = content.headline,
        style = MaterialTheme.editorialTypography.display,
        color = MaterialTheme.colorScheme.onSurface,
    )

    Spacer(modifier = Modifier.height(24.dp))

    content.highlights.forEachIndexed { index, highlight ->
        if (index > 0) {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "—",
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = highlight,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadingLifeShareCardBody(content: ReadingLifeShareCardUiModel) {
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

@Composable
private fun ShareCardSignOff() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "SOFTCOVER",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current.copy(alpha = 0.6f),
        )

        Text(
            text = "— · —",
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current.copy(alpha = 0.4f),
        )
    }
}

// The nine `@Preview`s below render fixtures off `ShareCardUiModel.previews` rather than
// re-declaring the sample literals inline, so the preview set and the Component Gallery's set
// (§7.2 R5) cannot drift apart.
private val BookShareCardFixtures = ShareCardUiModel.previews.filterIsInstance<BookShareCardUiModel>()

private val ReadingUpdateShareCardFixtures = ShareCardUiModel.previews.filterIsInstance<ReadingUpdateShareCardUiModel>()

@StandardPreview
@Composable
private fun BookShareCardPreview() {
    SoftcoverTheme {
        ShareCard(content = BookShareCardFixtures.first { it.description != null })
    }
}

@StandardPreview
@Composable
private fun BookShareCardMinimalPreview() {
    SoftcoverTheme {
        ShareCard(content = BookShareCardFixtures.first { it.description == null })
    }
}

@StandardPreview
@Composable
private fun ReadingUpdateFinishedShareCardPreview() {
    SoftcoverTheme {
        ShareCard(content = ReadingUpdateShareCardFixtures.first { it.kind == ReadingUpdateKind.FINISHED })
    }
}

@StandardPreview
@Composable
private fun ReadingUpdateReadingShareCardPreview() {
    SoftcoverTheme {
        ShareCard(content = ReadingUpdateShareCardFixtures.first { it.kind == ReadingUpdateKind.READING })
    }
}

@StandardPreview
@Composable
private fun StatShareCardPreview() {
    SoftcoverTheme {
        ShareCard(content = ShareCardUiModel.previews.filterIsInstance<StatShareCardUiModel>().single())
    }
}

@StandardPreview
@Composable
private fun QuoteShareCardPreview() {
    SoftcoverTheme {
        ShareCard(content = ShareCardUiModel.previews.filterIsInstance<QuoteShareCardUiModel>().single())
    }
}

@StandardPreview
@Composable
private fun YearRecapShareCardPreview() {
    SoftcoverTheme {
        ShareCard(content = ShareCardUiModel.previews.filterIsInstance<YearRecapShareCardUiModel>().single())
    }
}

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
