package nl.rhaydus.softcover.core.designsystem.presentation.share

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.designsystem.presentation.component.PillChip
import nl.rhaydus.softcover.core.designsystem.presentation.component.ReviewDocumentText
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverImage
import nl.rhaydus.softcover.core.designsystem.presentation.component.StarRatingInput
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun

private const val TABULAR_NUMS = "tnum"

@Composable
fun ShareCard(
    content: ShareContent,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = when (content) {
        is StatShareContent -> MaterialTheme.colorScheme.primary
        is BookShareContent,
        is ReadingUpdateShareContent,
        is QuoteShareContent,
        is YearRecapShareContent -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (content) {
        is StatShareContent -> MaterialTheme.colorScheme.onPrimary
        is BookShareContent,
        is ReadingUpdateShareContent,
        is QuoteShareContent,
        is YearRecapShareContent -> MaterialTheme.colorScheme.onSurface
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
                is BookShareContent -> BookShareCardBody(content)
                is ReadingUpdateShareContent -> ReadingUpdateShareCardBody(content)
                is StatShareContent -> StatShareCardBody(content)
                is QuoteShareContent -> QuoteShareCardBody(content)
                is YearRecapShareContent -> YearRecapShareCardBody(content)
            }

            Spacer(modifier = Modifier.weight(1f))

            ShareCardSignOff()
        }
    }
}

@Composable
private fun BookShareCardBody(content: BookShareContent) {
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
            SoftcoverImage(
                model = content.coverUrl,
                contentDescription = "Cover of ${content.title}",
                isLoading = false,
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

private fun buildBookStatsLine(content: BookShareContent): String? {
    val parts = buildList {
        content.communityRating?.takeIf { it > 0.0 }?.let { add("★ %.1f".format(it)) }
        content.userRating?.let { add("Your $it/10") }
        content.releaseYear?.takeIf { it != -1 }?.let { add(it.toString()) }
        content.pageCount?.takeIf { it > 0 }?.let { add("$it pages") }
    }

    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = " · ")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReadingUpdateShareCardBody(content: ReadingUpdateShareContent) {
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
            SoftcoverImage(
                model = content.coverUrl,
                contentDescription = "Cover of ${content.title}",
                isLoading = false,
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
                    PillChip(label = tag)
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
                        enabled = false,
                        starSize = 20.dp,
                    )
                }

                if (content.review != null) {
                    Spacer(modifier = Modifier.height(20.dp))

                    ReviewDocumentText(
                        document = content.review,
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
                SoftcoverImage(
                    model = avatarUrl,
                    contentDescription = "$username's avatar",
                    isLoading = false,
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
private fun StatShareCardBody(content: StatShareContent) {
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
private fun QuoteShareCardBody(content: QuoteShareContent) {
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
private fun YearRecapShareCardBody(content: YearRecapShareContent) {
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

@StandardPreview
@Composable
private fun BookShareCardPreview() {
    SoftcoverTheme {
        ShareCard(
            content = BookShareContent(
                coverUrl = null,
                title = "We Have Always Lived in the Castle",
                author = "Shirley Jackson",
                communityRating = 4.2,
                userRating = 9,
                releaseYear = 1962,
                pageCount = 214,
                description = "Merricat Blackwood lives with her sister Constance and their uncle Julian in the family mansion outside the village. A quiet gothic tale of isolation, ritual, and the slow-burning aftermath of a single, devastating poisoning.",
                quote = "Merricat, said Connie, would you like a cup of tea?",
            ),
        )
    }
}

@StandardPreview
@Composable
private fun BookShareCardMinimalPreview() {
    SoftcoverTheme {
        ShareCard(
            content = BookShareContent(
                coverUrl = null,
                title = "Piranesi",
                author = "Susanna Clarke",
                communityRating = null,
                userRating = null,
                releaseYear = null,
                pageCount = null,
                description = null,
                quote = null,
            ),
        )
    }
}

@StandardPreview
@Composable
private fun ReadingUpdateFinishedShareCardPreview() {
    SoftcoverTheme {
        ShareCard(
            content = ReadingUpdateShareContent(
                username = "cinque",
                avatarUrl = null,
                coverUrl = null,
                title = "The Name of the Wind",
                author = "Patrick Rothfuss",
                kind = ReadingUpdateKind.FINISHED,
                ratingStars = 4.5,
                review = ReviewDocument(
                    paragraphs = listOf(
                        ReviewParagraph(
                            runs = listOf(
                                ReviewRun(text = "A gorgeous, immersive tale that pulls you into Kvothe's world. The ending — "),
                                ReviewRun(
                                    text = "he was the villain all along",
                                    spoiler = true,
                                ),
                                ReviewRun(text = " — still floored me."),
                            ),
                        ),
                    ),
                ),
                progressLabel = null,
            ),
        )
    }
}

@StandardPreview
@Composable
private fun ReadingUpdateReadingShareCardPreview() {
    SoftcoverTheme {
        ShareCard(
            content = ReadingUpdateShareContent(
                username = "cinque",
                avatarUrl = null,
                coverUrl = null,
                title = "The Name of the Wind",
                author = "Patrick Rothfuss",
                kind = ReadingUpdateKind.READING,
                ratingStars = null,
                review = null,
                progressLabel = "page 212 / 662 · 32%",
            ),
        )
    }
}

@StandardPreview
@Composable
private fun StatShareCardPreview() {
    SoftcoverTheme {
        ShareCard(
            content = StatShareContent(
                eyebrow = "Pages read in 2026",
                value = 8_402L,
                caption = "across 14 books, mostly on Sunday afternoons.",
            ),
        )
    }
}

@StandardPreview
@Composable
private fun QuoteShareCardPreview() {
    SoftcoverTheme {
        ShareCard(
            content = QuoteShareContent(
                quote = "The reader is the protagonist; the page is the stage.",
                sourceTitle = "Piranesi",
                sourceAuthor = "Susanna Clarke",
                page = 142,
            ),
        )
    }
}

@StandardPreview
@Composable
private fun YearRecapShareCardPreview() {
    SoftcoverTheme {
        ShareCard(
            content = YearRecapShareContent(
                eyebrow = "Year in Books",
                year = 2026,
                headline = "A year of long evenings.",
                highlights = listOf(
                    "24 finished, 8,402 pages.",
                    "Top genre: literary fiction.",
                    "Longest haul: Lonesome Dove.",
                ),
            ),
        )
    }
}
