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
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
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
import nl.rhaydus.softcover.core.component.badge.Badge
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.Cover
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.shape
import nl.rhaydus.softcover.core.component.divider.Divider
import nl.rhaydus.softcover.core.component.divider.DividerInk
import nl.rhaydus.softcover.core.component.modifier.navSharedBounds
import nl.rhaydus.softcover.core.component.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.component.state.skeletonMask
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Corners
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.domain.model.BookEdition

@Composable
internal fun GeneralBookInfoSection(
    edition: BookEdition?,
    heroCover: CoverUiModel?,
    backdropCover: CoverUiModel?,
    title: String?,
    titleTransitionKey: String?,
    ratingTransitionKey: String?,
    seriesText: String?,
    rating: Double?,
    releaseYear: Int?,
    unreleasedBadge: BadgeUiModel?,
    isLoading: Boolean,
    isExpired: Boolean,
    ownedCoverBadge: BadgeUiModel?,
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
        Cover(
            model = backdropCover,
            modifier = Modifier
                .matchParentSize()
                .blur(8.dp)
                .scale(1.8f),
        )

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
                    start = Spacing.gutter,
                    end = Spacing.gutter,
                    top = 64.dp,
                    bottom = Spacing.x4l,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = if (isLoading.not()) {
                    Modifier.pressScaleClickable(
                        onClick = onCoverClick,
                        hoverShape = heroCover?.variant?.shape() ?: RectangleShape,
                    )
                } else {
                    Modifier
                },
            ) {
                Cover(
                    model = heroCover,
                    modifier = Modifier.height(imageHeight * 0.8f),
                )

                val navScope = LocalNavAnimatedVisibilityScope.current

                val enterSettled = navScope == null ||
                    navScope.transition.currentState == EnterExitState.Visible

                if (ownedCoverBadge != null) {
                    val badgeAlpha by animateFloatAsState(
                        targetValue = if (enterSettled) 1f else 0f,
                        label = "OwnedCoverBadgeAlpha",
                    )

                    Badge(
                        model = ownedCoverBadge,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Spacing.xs)
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

            Column(modifier = Modifier.padding(horizontal = Spacing.md)) {
                val seriesUnknown = isLoading && seriesText == null

                if (seriesUnknown || seriesText.isNullOrBlank().not()) {
                    SoftcoverText(
                        text = seriesText,
                        standIn = "#1 of 3 in Series name",
                        color = Color.White.copy(alpha = textSecondaryAlpha),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp,
                            shadow = secondaryShadow,
                        ),
                    )

                    Spacer(modifier = Modifier.height(Spacing.xxxs))
                }

                val titleUnknown = isLoading && title == null

                SoftcoverText(
                    text = if (titleUnknown) null else title.orEmpty(),
                    standIn = "A short book title",
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
                    modifier = Modifier.navSharedBounds(key = titleTransitionKey),
                )

                Spacer(modifier = Modifier.height(Spacing.xxs))

                val editionUnknown = isLoading && edition == null
                val authorName = edition?.authorString.orEmpty()
                val showReleased = releaseYear != null && releaseYear > 0 && unreleasedBadge == null
                val bylineText = if (editionUnknown) {
                    null
                } else {
                    buildString {
                        append("By ")
                        append(authorName)

                        if (showReleased) {
                            append(" · ")
                            append(releaseYear)
                        }
                    }
                }

                SoftcoverText(
                    text = bylineText,
                    standIn = "By Author Name · 2024",
                    color = Color.White.copy(alpha = textSecondaryAlpha),
                    style = MaterialTheme.editorialTypography.bodySmall.copy(
                        shadow = secondaryShadow,
                    ),
                )

                if (unreleasedBadge != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Badge(model = unreleasedBadge)
                }

                val isAudiobook = edition?.isAudiobook == true

                val ratingUnknown = isLoading && rating == null
                val showRating = ratingUnknown || (rating ?: 0.0) > 0.0
                val showLength = editionUnknown || if (isAudiobook) {
                    (edition.audioSeconds ?: 0) > 0
                } else {
                    (edition?.pages ?: 0) > 0
                }

                if (showRating || showLength) {
                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Divider(ink = DividerInk.OnImage)

                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
                    ) {
                        if (showRating) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                SoftcoverText(
                                    text = "Rating",
                                    style = labelSmall,
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.navSharedBounds(key = ratingTransitionKey),
                                ) {
                                    SoftcoverText(
                                        text = rating?.let {
                                            formatDecimalNumber(
                                                value = it,
                                                fractionDigits = 1,
                                            )
                                        },
                                        standIn = "4.2",
                                        style = bodySmall,
                                    )

                                    Spacer(modifier = Modifier.width(Spacing.xxs))

                                    val ratingStarIcon = drawableIconResource(
                                        icon = SoftcoverIcon.StarFilled,
                                        contentDescription = "",
                                    )

                                    Icon(
                                        painter = ratingStarIcon.getIconPainter(),
                                        contentDescription = ratingStarIcon.contentDescription,
                                        tint = RatingGold,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .skeletonMask(visible = ratingUnknown),
                                    )
                                }
                            }
                        }

                        if (showLength) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                SoftcoverText(
                                    text = "Length",
                                    style = labelSmall,
                                )

                                Row {
                                    val lengthText = when {
                                        editionUnknown -> null
                                        isAudiobook -> secondsToHm(seconds = edition.audioSeconds ?: 0)
                                        else -> "${edition?.pages ?: ""}"
                                    }

                                    SoftcoverText(
                                        text = lengthText,
                                        standIn = "320",
                                        style = bodySmall,
                                        modifier = Modifier.alignByBaseline(),
                                    )

                                    if (editionUnknown.not() && isAudiobook.not()) {
                                        Spacer(modifier = Modifier.width(Spacing.xxs))

                                        SoftcoverText(
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(20.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = Corners.xxl.copy(
                        bottomStart = ZeroCornerSize,
                        bottomEnd = ZeroCornerSize,
                    ),
                ),
        )
    }
}
