package nl.rhaydus.softcover.feature.profile.presentation.screen.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife

private const val RECENTLY_LOVED_LIMIT = 3

/**
 * "The last few that landed" — up to three of [ReadingLife.recentlyLoved]: real reads, not
 * superlatives, each showing the reader's own rating and the month it landed.
 */
@Composable
internal fun RecentlyLovedSection(
    books: List<LovedBook>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionIntro(
            eyebrow = "Recently loved",
            headline = "The last few that landed",
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (books.isEmpty() && isLoading.not()) {
            Text(
                text = "Your next favorite is still ahead of you.",
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val slots: List<LovedBook?> = if (isLoading && books.isEmpty()) {
                List(RECENTLY_LOVED_LIMIT) { null }
            } else {
                books.take(RECENTLY_LOVED_LIMIT)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                slots.forEach { book ->
                    LovedBookCard(
                        book = book,
                        isLoading = isLoading,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun LovedBookCard(
    book: LovedBook?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .shadow(
                    elevation = 6.dp,
                    shape = shape,
                    clip = false,
                )
                .clip(shape),
        ) {
            RhaydusShimmerImage(
                model = book?.coverUrl,
                contentDescription = book?.let { "Cover of ${it.title}" },
                contentScale = ContentScale.Crop,
                isLoading = isLoading || book == null,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = book?.title.orEmpty(),
            style = MaterialTheme.editorialTypography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 17.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = book?.author.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (book != null) {
            Spacer(modifier = Modifier.height(4.dp))

            // Rating and month stack on their own lines — a shared row ("4.5★ · May 2026") can overflow
            // this card's narrow width once it sits three-up, so the two meta facts get one line each.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatDecimalNumber(
                        value = book.ratingStars,
                        fractionDigits = 1,
                    ),
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.width(3.dp))

                val star = drawableIconResource(
                    icon = SoftcoverIcon.StarFilled,
                    contentDescription = "",
                )

                Icon(
                    painter = star.getIconPainter(),
                    contentDescription = star.contentDescription,
                    tint = RatingGold,
                    modifier = Modifier.size(11.dp),
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = book.monthLabel,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
