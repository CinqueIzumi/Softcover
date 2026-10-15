package nl.rhaydus.softcover.core.component.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.rememberCoverImageRequest
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.supportsBlur
import nl.rhaydus.softcover.core.designsystem.presentation.theme.OverMediaInk
import nl.rhaydus.softcover.core.designsystem.presentation.theme.OverMediaScrim
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography

/**
 * The 9:16 story frame the book and reading-update cards share. Unlike the paper cards it is never
 * clipped to rounded corners, so the exported story has no transparent corners. A `null` [username]
 * means the card is not attributed to a reader and the footer shows only the sign-off.
 */
@Composable
internal fun BookShareCardFrame(
    eyebrow: String,
    cover: CoverUiModel,
    title: String,
    author: String,
    username: String?,
    avatarUrl: String?,
    format: ShareCardFormat,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.editorialTypography
    val backdropRequest = rememberCoverImageRequest(model = cover)
    val scrimLift = if (supportsBlur()) 0f else 0.2f

    CompositionLocalProvider(LocalContentColor provides OverMediaInk) {
        Box(
            modifier = modifier
                .then(
                    when (format) {
                        ShareCardFormat.Story -> Modifier.requiredSize(
                            width = 360.dp,
                            height = 640.dp,
                        )

                        ShareCardFormat.FullText -> Modifier
                            .requiredWidth(360.dp)
                            .requiredHeightIn(min = 640.dp)
                    },
                )
                .clipToBounds(),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                colors.primary,
                                colors.tertiary,
                            ),
                        ),
                    ),
            )

            if (backdropRequest != null) {
                AsyncImage(
                    model = backdropRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .matchParentSize()
                        .blur(
                            radius = 40.dp,
                            edgeTreatment = BlurredEdgeTreatment.Rectangle,
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0f to OverMediaScrim.copy(alpha = 0.35f + scrimLift),
                                0.5f to OverMediaScrim.copy(alpha = 0.55f + scrimLift),
                                1f to OverMediaScrim.copy(alpha = 0.8f + scrimLift / 2f),
                            ),
                        ),
                    ),
            )

            Column(
                modifier = when (format) {
                    ShareCardFormat.Story -> Modifier.fillMaxSize()
                    ShareCardFormat.FullText -> Modifier
                        .fillMaxWidth()
                        .heightIn(min = 640.dp)
                }.padding(Spacing.xl),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = when (format) {
                        ShareCardFormat.Story -> Modifier.weight(1f)
                        ShareCardFormat.FullText -> Modifier
                    }.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        Spacing.sm,
                        Alignment.CenterVertically,
                    ),
                ) {
                    SoftcoverText(
                        text = eyebrow,
                        style = typography.eyebrow,
                        color = OverMediaInk.copy(alpha = 0.7f),
                    )

                    Cover(
                        model = cover,
                        modifier = when (format) {
                            ShareCardFormat.Story -> Modifier
                                .weight(1f, fill = false)
                                .widthIn(max = 200.dp)

                            ShareCardFormat.FullText -> Modifier.width(200.dp)
                        },
                    )

                    SoftcoverText(
                        text = if (author.isBlank()) title else "$title · $author",
                        style = typography.titleMedium,
                        color = OverMediaInk.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    content()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.sm),
                    horizontalArrangement = if (username != null) {
                        Arrangement.SpaceBetween
                    } else {
                        Arrangement.End
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (username != null) {
                        ReaderIdentity(
                            username = username,
                            avatarUrl = avatarUrl,
                        )
                    }

                    SoftcoverText(
                        text = "SOFTCOVER",
                        style = typography.eyebrowSmall,
                        color = OverMediaInk.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderIdentity(
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

            Spacer(modifier = Modifier.width(Spacing.xs))
        }

        SoftcoverText(
            text = username,
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = LocalContentColor.current,
        )
    }
}
