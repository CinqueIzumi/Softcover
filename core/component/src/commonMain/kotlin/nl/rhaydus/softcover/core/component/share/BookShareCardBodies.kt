package nl.rhaydus.softcover.core.component.share

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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.StarRatingInput
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.designsystem.theme.StandardPreview
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.richtext.RichText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import kotlin.math.roundToInt

@Composable
internal fun BookShareCardBody(model: BookShareCardUiModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BookShareCardHeader(
            eyebrow = "FROM THE SHELF",
            coverUrl = model.coverUrl,
            title = model.title,
            author = model.author,
        )

        val statsLine = buildBookStatsLine(model = model)

        if (statsLine != null) {
            Spacer(modifier = Modifier.height(12.dp))

            SoftcoverText(
                text = statsLine,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        if (model.description != null && model.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(20.dp))

            SoftcoverText(
                text = model.description,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReadingUpdateShareCardBody(model: ReadingUpdateShareCardUiModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BookShareCardHeader(
            eyebrow = when (model) {
                is ReadingUpdateShareCardUiModel.Finished -> "FROM MY SHELF"
                is ReadingUpdateShareCardUiModel.Reading -> "CURRENTLY READING"
            },
            coverUrl = model.coverUrl,
            title = model.title,
            author = model.author,
        )

        Spacer(modifier = Modifier.height(20.dp))

        ReadingUpdateReaderIdentity(
            username = model.username,
            avatarUrl = model.avatarUrl,
        )

        if (model.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    Alignment.CenterHorizontally,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                model.tags.forEach { tag ->
                    Chip(
                        model = ChipUiModel(
                            key = tag,
                            label = tag,
                            interaction = ChipInteraction.Inert,
                        ),
                    )
                }
            }
        }

        when (model) {
            is ReadingUpdateShareCardUiModel.Finished -> {
                if (model.ratingStars != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    StarRatingInput(
                        rating = model.ratingStars,
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

                if (model.review != null) {
                    Spacer(modifier = Modifier.height(20.dp))

                    RichText(
                        model = model.review,
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

            is ReadingUpdateShareCardUiModel.Reading -> {
                if (model.progressLabel != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    SoftcoverText(
                        text = model.progressLabel,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** The eyebrow, cover, title and byline both book-flavoured cards open with. */
@Composable
private fun BookShareCardHeader(
    eyebrow: String,
    coverUrl: String?,
    title: String,
    author: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SoftcoverText(
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
                model = coverUrl,
                contentDescription = "Cover of $title",
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SoftcoverText(
            text = title,
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = LocalContentColor.current,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        if (author.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            SoftcoverText(
                text = "BY ${author.uppercase()}",
                style = MaterialTheme.editorialTypography.eyebrowSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun buildBookStatsLine(model: BookShareCardUiModel): String? {
    val parts = buildList {
        model.communityRating?.takeIf { it > 0.0 }?.let {
            val tenths = (it * 10).roundToInt()

            add("★ ${tenths / 10}.${tenths % 10}")
        }
        model.userRating?.let { add("Your $it/10") }
        model.releaseYear?.takeIf { it != -1 }?.let { add(it.toString()) }
        model.pageCount?.takeIf { it > 0 }?.let { add("$it pages") }
    }

    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = " · ")
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

        SoftcoverText(
            text = username,
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current,
        )
    }
}

@StandardPreview
@Composable
private fun BookShareCardPreview() {
    SoftcoverTheme {
        ShareCard(model = ShareCardUiModel.previews.getValue("Book"))
    }
}

@StandardPreview
@Composable
private fun BookShareCardMinimalPreview() {
    SoftcoverTheme {
        ShareCard(model = ShareCardUiModel.previews.getValue("Book (minimal)"))
    }
}

@StandardPreview
@Composable
private fun ReadingUpdateFinishedShareCardPreview() {
    SoftcoverTheme {
        ShareCard(model = ShareCardUiModel.previews.getValue("Reading update (finished)"))
    }
}

@StandardPreview
@Composable
private fun ReadingUpdateReadingShareCardPreview() {
    SoftcoverTheme {
        ShareCard(model = ShareCardUiModel.previews.getValue("Reading update (in progress)"))
    }
}
