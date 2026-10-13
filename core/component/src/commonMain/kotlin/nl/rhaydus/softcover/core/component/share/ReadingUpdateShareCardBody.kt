package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.StarRatingInput
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.softcover.core.component.chip.Chip
import nl.rhaydus.softcover.core.component.richtext.RichText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

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

        Spacer(modifier = Modifier.height(Spacing.lg))

        ReadingUpdateReaderIdentity(
            username = model.username,
            avatarUrl = model.avatarUrl,
        )

        if (model.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.md))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(
                    Spacing.xs,
                    Alignment.CenterHorizontally,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.fillMaxWidth(),
            ) {
                model.tags.forEach { tag ->
                    Chip(model = tag)
                }
            }
        }

        when (model) {
            is ReadingUpdateShareCardUiModel.Finished -> {
                if (model.ratingStars != null) {
                    Spacer(modifier = Modifier.height(Spacing.sm))

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
                    Spacer(modifier = Modifier.height(Spacing.lg))

                    RichText(
                        model = model.review,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            is ReadingUpdateShareCardUiModel.Reading -> {
                if (model.progressLabel != null) {
                    Spacer(modifier = Modifier.height(Spacing.sm))

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

            Spacer(modifier = Modifier.width(Spacing.sm))
        }

        SoftcoverText(
            text = username,
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current,
        )
    }
}
