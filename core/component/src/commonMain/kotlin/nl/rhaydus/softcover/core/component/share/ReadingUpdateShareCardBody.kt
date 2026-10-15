package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.rhaydus.designsystem.component.StarRatingInput
import nl.rhaydus.softcover.core.component.richtext.RichText
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.OverMediaInk
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

@Composable
internal fun ReadingUpdateShareCardBody(model: ReadingUpdateShareCardUiModel) {
    when (model) {
        is ReadingUpdateShareCardUiModel.Finished -> {
            if (model.ratingStars != null) {
                StarRatingInput(
                    rating = model.ratingStars,
                    onRatingChange = {},
                    starIcon = drawableIconResource(
                        contentDescription = "",
                        icon = SoftcoverIcon.StarFilled,
                    ),
                    filledColor = RatingGold,
                    emptyColor = OverMediaInk.copy(alpha = 0.3f),
                    enabled = false,
                    starSize = 24.dp,
                )
            }

            if (model.review != null) {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    RichText(
                        model = model.review,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        is ReadingUpdateShareCardUiModel.Reading -> {
            if (model.progressLabel != null) {
                GlassPanel(shape = Corners.pill) {
                    SoftcoverText(
                        text = model.progressLabel,
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = OverMediaInk.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    ShareCardTags(
        tags = model.tags,
        format = model.format,
    )
}
