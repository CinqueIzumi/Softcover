package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.designsystem.modifier.grayscale
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.domain.model.BookEdition

@Composable
internal fun GeneralBookInfoSection(
    // Still the domain edition: the byline, format and length metadata below read it. Only the
    // cover resolution moved out, into `CoverModelsCollector`.
    edition: BookEdition?,
    heroCover: CoverUiModel?,
    backdropCover: CoverUiModel?,
    title: String?,
    seriesText: String?,
    rating: Double?,
    releaseYear: Int?,
    unreleasedBadge: BadgeUiModel?,
    isLoading: Boolean,
    isExpired: Boolean,
    isOwned: Boolean,
    onCoverClick: () -> Unit,
) {
    val imageHeight = with(LocalDensity.current) {
        (LocalWindowInfo.current.containerSize.height * 0.3f).toDp()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .conditional(
                condition = isExpired,
                ifTrue = { Modifier.grayscale() },
            ),
    ) {
        if (backdropCover != null) {
            Cover(
                model = backdropCover,
                modifier = Modifier
                    .matchParentSize()
                    .blur(8.dp)
                    .scale(1.8f),
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.65f),
                        ),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = imageHeight)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 64.dp,
                    bottom = 36.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = if (isLoading.not()) {
                    Modifier.pressScaleClickable(onClick = onCoverClick)
                } else {
                    Modifier
                },
            ) {
                if (heroCover != null) {
                    Cover(
                        model = heroCover,
                        modifier = Modifier.height(imageHeight * 0.8f),
                    )
                }

                val navScope = LocalNavAnimatedVisibilityScope.current

                val enterSettled = navScope == null ||
                    navScope.transition.currentState == EnterExitState.Visible

                if (isOwned) {
                    val badgeAlpha by animateFloatAsState(
                        targetValue = if (enterSettled) 1f else 0f,
                        label = "OwnedCoverBadgeAlpha",
                    )

                    OwnedCoverBadge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .alpha(badgeAlpha),
                    )
                }
            }

            val textSecondaryAlpha = 0.85f

            val secondaryShadow = Shadow(
                color = Color.Black.copy(alpha = 0.6f),
                offset = Offset(
                    x = 0f,
                    y = 1f,
                ),
                blurRadius = 6f,
            )

            val bodySmall = MaterialTheme.typography.bodySmall.copy(
                color = Color.White,
                shadow = secondaryShadow,
                fontWeight = FontWeight.Bold,
            )

            val labelSmall = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = textSecondaryAlpha),
                shadow = secondaryShadow,
            )

            SkeletonCrossfade(
                isLoading = isLoading,
                modifier = Modifier.padding(horizontal = 16.dp),
                label = "BookDetailTitleBlock",
            ) { loading ->
                if (loading) {
                    Column {
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .width(120.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer(isLoading = true),
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .height(22.dp)
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer(isLoading = true),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .width(140.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmer(isLoading = true),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            repeat(2) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .height(10.dp)
                                            .width(48.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .shimmer(isLoading = true),
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .height(12.dp)
                                            .width(36.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .shimmer(isLoading = true),
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column {
                        seriesText?.takeIf { it.isNotBlank() }?.let { series ->
                            Text(
                                text = series,
                                color = Color.White.copy(alpha = textSecondaryAlpha),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.6.sp,
                                    shadow = secondaryShadow,
                                ),
                            )

                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        Text(
                            text = title.orEmpty(),
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.75f),
                                    offset = Offset(
                                        x = 0f,
                                        y = 1f,
                                    ),
                                    blurRadius = 8f,
                                ),
                            ),
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val authorName = edition?.authorString.orEmpty()
                        val showReleased = releaseYear != null && releaseYear > 0 && unreleasedBadge == null
                        val bylineText = buildString {
                            append("By ")
                            append(authorName)

                            if (showReleased) {
                                append(" · ")
                                append(releaseYear)
                            }
                        }

                        Text(
                            text = bylineText,
                            color = Color.White.copy(alpha = textSecondaryAlpha),
                            style = MaterialTheme.editorialTypography.bodySmall.copy(
                                shadow = secondaryShadow,
                            ),
                        )

                        if (unreleasedBadge != null) {
                            Spacer(modifier = Modifier.height(6.dp))

                            Badge(model = unreleasedBadge)
                        }

                        val isAudiobook = edition?.isAudiobook == true

                        val showRating = rating != null && rating > 0.0
                        val showLength = if (isAudiobook) {
                            (edition.audioSeconds ?: 0) > 0
                        } else {
                            (edition?.pages ?: 0) > 0
                        }

                        if (showRating || showLength) {
                            Spacer(modifier = Modifier.height(8.dp))

                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (showRating) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = "Rating",
                                            style = labelSmall,
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = formatDecimalNumber(
                                                    value = rating,
                                                    fractionDigits = 1,
                                                ),
                                                style = bodySmall,
                                            )

                                            Spacer(modifier = Modifier.width(4.dp))

                                            val ratingStarIcon = drawableIconResource(
                                                icon = SoftcoverIcon.StarFilled,
                                                contentDescription = "",
                                            )

                                            Icon(
                                                painter = ratingStarIcon.getIconPainter(),
                                                contentDescription = ratingStarIcon.contentDescription,
                                                tint = RatingGold,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }

                                if (showLength) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = "Length",
                                            style = labelSmall,
                                        )

                                        Row {
                                            val lengthText = if (isAudiobook) {
                                                secondsToHm(seconds = edition.audioSeconds ?: 0)
                                            } else {
                                                "${edition?.pages ?: ""}"
                                            }

                                            Text(
                                                text = lengthText,
                                                style = bodySmall,
                                                modifier = Modifier.alignByBaseline(),
                                            )

                                            if (isAudiobook.not()) {
                                                Spacer(modifier = Modifier.width(4.dp))

                                                Text(
                                                    text = "pgs",
                                                    style = labelSmall,
                                                    modifier = Modifier.alignByBaseline(),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                    ),
                ),
        )
    }
}

@Composable
private fun OwnedCoverBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.Black.copy(alpha = 0.55f),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val ownedIcon = drawableIconResource(
                icon = SoftcoverIcon.Check,
                contentDescription = "Owned",
            )

            Icon(
                painter = ownedIcon.getIconPainter(),
                contentDescription = ownedIcon.contentDescription,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Owned",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}
