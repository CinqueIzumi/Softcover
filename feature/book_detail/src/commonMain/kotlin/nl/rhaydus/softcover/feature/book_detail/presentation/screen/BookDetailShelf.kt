package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.component.StarRatingInput
import nl.rhaydus.designsystem.editorial.component.DropCapText
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.icon.RhaydusIconResource
import nl.rhaydus.designsystem.image.RhaydusShimmerImage
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.conditional
import nl.rhaydus.designsystem.modifier.grayscale
import nl.rhaydus.designsystem.modifier.pressScaleClickable
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.designsystem.modifier.shimmer
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.designsystem.util.SkeletonCrossfade
import nl.rhaydus.designsystem.util.htmlToAnnotatedString
import nl.rhaydus.softcover.core.designsystem.presentation.component.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.designsystem.presentation.component.DeadlineBadge
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.component.ListMembership
import nl.rhaydus.softcover.core.designsystem.presentation.component.MarkAsReadBurst
import nl.rhaydus.softcover.core.designsystem.presentation.component.PillChip
import nl.rhaydus.softcover.core.designsystem.presentation.component.ReviewDocumentText
import nl.rhaydus.softcover.core.designsystem.presentation.component.UnreleasedBadge
import nl.rhaydus.softcover.core.designsystem.presentation.component.UnreleasedBadgeStyle
import nl.rhaydus.softcover.core.designsystem.presentation.component.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.theme.RatingGold
import nl.rhaydus.softcover.core.designsystem.presentation.theme.displayFontFamily
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.isBlank
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnAddUserTagAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearDeadlineAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDeadlinePickedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDeleteReviewAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissChooseListsSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissDeadlinePickerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissProgressSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissReviewSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissScanEditionBannerClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissShareSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissTagEditorAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditionOwnedToggleAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditionSearchQueryChangeAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnExternalLinkClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadingClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsWantToReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnNewEditionSaveClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenDeadlinePickerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenReviewSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenTagEditorAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRateBookAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRemoveBookClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRemoveUserTagAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRevealReviewSpoilerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnSaveReviewAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShareBookClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowChooseListsSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowUpdateProgressSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnTagDraftChangeAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnTagEditorCategoryChangeAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnToggleListMembershipAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnToggleUserTagSpoilerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdatePageProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdatePercentageProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdateTimeProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdateToScannedEditionClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.component.EditionBottomSheetSelector
import nl.rhaydus.softcover.feature.book_detail.presentation.component.ReviewEditorBottomSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.component.ShareBookBottomSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.component.TagEditorBottomSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

private const val REVIEW_COLLAPSED_LINES = 8
// region Hero
@Composable
internal fun GeneralBookInfoSection(
    edition: BookEdition?,
    fallBackEdition: BookEdition?,
    title: String?,
    seriesText: String?,
    rating: Double?,
    releaseYear: Int?,
    unreleasedDate: LocalDate?,
    isLoading: Boolean,
    fallbackCoverUrl: String?,
    isExpired: Boolean,
    isOwned: Boolean,
    bookId: Int,
    transitionSurface: String?,
    onCoverClick: () -> Unit,
) {
    val imageHeight = with(LocalDensity.current) {
        (LocalWindowInfo.current.containerSize.height * 0.3f).toDp()
    }

    val coverHasContent = edition != null || fallBackEdition != null || fallbackCoverUrl != null
    val coverIsLoading = isLoading && coverHasContent.not()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .conditional(
                condition = isExpired,
                ifTrue = { Modifier.grayscale() },
            ),
    ) {
        EditionImage(
            edition = edition,
            defaultEdition = fallBackEdition,
            fallbackCoverUrl = fallbackCoverUrl,
            isLoading = coverIsLoading,
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
                EditionImage(
                    edition = edition,
                    defaultEdition = fallBackEdition,
                    fallbackCoverUrl = fallbackCoverUrl,
                    isLoading = coverIsLoading,
                    modifier = Modifier.height(imageHeight * 0.8f),
                    cornerRadius = 16.dp,
                    sharedTransitionKey = bookCoverTransitionKey(
                        editionId = edition?.id,
                        bookId = bookId,
                        surface = transitionSurface,
                    ),
                )

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
                        val showReleased = releaseYear != null && releaseYear > 0 && unreleasedDate == null
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

                        if (unreleasedDate != null) {
                            Spacer(modifier = Modifier.height(6.dp))

                            UnreleasedBadge(
                                releaseDate = unreleasedDate,
                                style = UnreleasedBadgeStyle.Prominent,
                            )
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
// endregion
// region Lens Toggle
/**
 * The sticky lens toggle (design-system.md's lens-toggle pattern): a full-width segmented pill on a
 * `surfaceContainer` track that switches the sections below between "Yours" and "The Book". Both
 * segments are always visible; "Yours" is disabled (dimmed, non-interactive) until the book has a
 * user copy. The active segment crossfades over ~220ms, gated by [playDecorativeMotion]; a `select`
 * haptic fires on switch. The caller composes this as a `stickyHeader` under the top bar.
 */
@Composable
internal fun LensToggle(
    selectedLens: BookDetailLens,
    onLensSelected: (BookDetailLens) -> Unit,
    yoursEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val haptics = rememberHaptics()

    // The caller composes this as a `stickyHeader`, so the root paints an opaque, edge-to-edge
    // page-background layer first — otherwise content scrolling underneath would show through the
    // padded pill's own margin while the header is pinned.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(percent = 50),
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                LensSegment(
                    label = "The Book",
                    selected = selectedLens == BookDetailLens.THE_BOOK,
                    enabled = true,
                    onClick = {
                        haptics.select()
                        onLensSelected(BookDetailLens.THE_BOOK)
                    },
                    modifier = Modifier.weight(1f),
                )

                LensSegment(
                    label = "Yours",
                    selected = selectedLens == BookDetailLens.YOURS,
                    enabled = yoursEnabled,
                    onClick = {
                        haptics.select()
                        onLensSelected(BookDetailLens.YOURS)
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LensSegment(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playMotion = playDecorativeMotion()
    val colorSpec = if (playMotion) tween<Color>(durationMillis = 220) else snap()

    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = colorSpec,
        label = "LensSegmentContainer",
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = colorSpec,
        label = "LensSegmentContent",
    )

    // "Yours" flips enabled the instant a shelved book lands (t1 of the load choreography) — an
    // un-animated alpha snap reads as a flash amid everything else settling at once, so this rides
    // the same color-spec tween/snap gate as the selection crossfade above.
    val floatSpec = if (playMotion) tween<Float>(durationMillis = 220) else snap()
    val disabledAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.4f,
        animationSpec = floatSpec,
        label = "LensSegmentEnabledAlpha",
    )

    Surface(
        modifier = modifier.alpha(disabledAlpha),
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(percent = 50),
        onClick = onClick,
        enabled = enabled && selected.not(),
    ) {
        Box(
            modifier = Modifier.padding(vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

/**
 * Crossfades between the "Yours" and "The Book" lens content on [BookDetailUiState.selectedLens]
 * switch — a subtle fade consistent with the app's motion register, gated by [playDecorativeMotion].
 * Sections belonging to the inactive lens are simply not composed. Shared by both the mobile and
 * desktop layouts so the switch behaves identically everywhere.
 */
@Composable
internal fun LensContent(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val playMotion = playDecorativeMotion()
    val fadeSpec = if (playMotion) tween<Float>(durationMillis = 220) else snap()

    AnimatedContent(
        targetState = state.selectedLens,
        transitionSpec = {
            fadeIn(animationSpec = fadeSpec) togetherWith fadeOut(animationSpec = fadeSpec)
        },
        label = "BookDetailLensContent",
    ) { lens ->
        when (lens) {
            BookDetailLens.YOURS -> YoursLensContent(
                state = state,
                runAction = runAction,
            )

            BookDetailLens.THE_BOOK -> TheBookLensContent(
                state = state,
                runAction = runAction,
            )
        }
    }
}
// endregion
// region Shelve Control
/**
 * The "Shelve this book" control (design-system.md §5 shelve-rows). A `surfaceContainerLow` card of
 * three VERTICAL rows — Want to read / Reading / Read — replacing the earlier horizontal chip strip.
 * The active row fills `primary`/`onPrimary` and shows a live trailing status; the section opener is
 * the inline 20×1 bar + `eyebrowSmall` contract (never the full section bar). All prior behavior is
 * preserved: optimistic writes, [Modifier.shakeOnError] + "Couldn't save — tap to retry", the
 * [MarkAsReadBurst] + commit haptic celebration on Read, and Reading disabled while `status == None`.
 */
@Composable
internal fun ShelveControlCard(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    dateStyle: DateStyle,
    celebrationKey: Int,
) {
    SkeletonCrossfade(
        isLoading = state.loadingBookDetails && state.book == null,
        label = "ShelveControlCard",
    ) { loading ->
        if (loading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                InlineAccentLabel(text = "Shelve this book")

                Spacer(modifier = Modifier.height(10.dp))

                // Mirrors the loaded card's own anatomy — the same outer Surface plus three
                // 42dp row-shaped bars at the same 5dp padding / 2dp gap / 11dp corner radius —
                // rather than one oversized blob, so the shimmer resolves into the real rows at
                // an identical height instead of visibly shrinking.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(5.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .shimmer(isLoading = true),
                            )
                        }
                    }
                }
            }
        } else {
            val book = state.book

            if (book != null) {
                val status = book.status

                val mutationFailed = book.id in state.failedMutationBookIds

                val haptics = rememberHaptics()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (mutationFailed) {
                            InlineAccentLabel(
                                text = "Couldn't save — tap to retry",
                                color = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            InlineAccentLabel(text = "Shelve this book")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            shape = RoundedCornerShape(15.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(5.dp)
                                    .shakeOnError(
                                        trigger = mutationFailed,
                                        onShakeEnd = {
                                            runAction(OnClearMutationFailureAction(bookId = book.id))
                                        },
                                    ),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                ShelveRow(
                                    label = "Want to read",
                                    iconRes = drawableIconResource(
                                        icon = SoftcoverIcon.BookmarkAdd,
                                        contentDescription = "",
                                    ),
                                    selected = status == BookStatus.WantToRead,
                                    onClick = {
                                        haptics.select()
                                        runAction(OnMarkBookAsWantToReadClickAction(book = book))
                                    },
                                )

                                ShelveRow(
                                    label = "Reading",
                                    iconRes = drawableIconResource(
                                        icon = SoftcoverIcon.Reading,
                                        contentDescription = "",
                                    ),
                                    selected = status == BookStatus.Reading,
                                    onClick = {
                                        haptics.select()
                                        runAction(OnMarkBookAsReadingClickAction(book = book))
                                    },
                                    enabled = status != BookStatus.None,
                                    trailingStatus = readingTrailingStatus(book = book),
                                )

                                ShelveRow(
                                    label = "Read",
                                    iconRes = drawableIconResource(
                                        icon = SoftcoverIcon.BookmarkCheck,
                                        contentDescription = "",
                                    ),
                                    selected = status == BookStatus.Read,
                                    onClick = { runAction(OnMarkBookAsReadClickAction(book = book)) },
                                    celebrationKey = celebrationKey,
                                    trailingStatus = readStatusDateTrailing(
                                        book = book,
                                        dateStyle = dateStyle,
                                    ),
                                )
                            }
                        }
                    }

                    MarkAsReadBurst(
                        triggerKey = celebrationKey,
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
        }
    }
}

/** Live "Reading" trailing status: `{pct}% · p. {page}`, time-based for audiobooks. */
private fun readingTrailingStatus(book: Book): String? {
    val read = book.userBookRead ?: return null
    val edition = book.currentEdition
    val pct = read.progress.roundToInt()

    return if (edition?.isAudiobook == true) {
        "$pct% · ${secondsToHm(seconds = read.currentSeconds ?: 0)}"
    } else {
        "$pct% · p. ${read.currentPage ?: 0}"
    }
}

/** Live "Read" trailing status: the read date, when known. */
private fun readStatusDateTrailing(
    book: Book,
    dateStyle: DateStyle,
): String? {
    val userBook = book.userBook ?: return null

    return userBook.getReadDateString(
        style = dateStyle,
        finishedAt = book.userBookRead?.finishedAt,
    )
}

@Composable
internal fun PersonalRatingRow(
    book: Book,
    runAction: (BookDetailAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        SmallSectionLabel(text = "Your rating")

        Spacer(modifier = Modifier.height(10.dp))

        val rating = book.userBook?.rating?.takeIf { it > 0.0 }

        Row(verticalAlignment = Alignment.CenterVertically) {
            StarRatingInput(
                rating = rating,
                onRatingChange = { newRating ->
                    runAction(
                        OnRateBookAction(
                            book = book,
                            rating = newRating,
                        ),
                    )
                },
                starIcon = drawableIconResource(
                    contentDescription = "",
                    icon = SoftcoverIcon.StarFilled,
                ),
                filledColor = RatingGold,
            )

            if (rating != null) {
                Spacer(modifier = Modifier.width(12.dp))

                // The stars run 0–5 in half-star steps; the label speaks the 10-point scale, so the
                // value doubles — every half star lands on an exact integer (4.5 stars = 9/10).
                Text(
                    text = "Your ${
                        formatDecimalNumber(
                            value = rating * 2,
                            fractionDigits = 0,
                        )
                    }/10",
                    style = MaterialTheme.editorialTypography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
internal fun PersonalReviewSection(
    reviewDocument: ReviewDocument?,
    hasSpoilers: Boolean,
    runAction: (BookDetailAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallSectionLabel(text = "Your review")

            if (reviewDocument != null && reviewDocument.isBlank().not()) {
                Text(
                    text = "Edit",
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = { runAction(OnOpenReviewSheetAction()) }),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (reviewDocument == null || reviewDocument.isBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = { runAction(OnOpenReviewSheetAction()) },
            ) {
                Text(
                    text = "Write a few words…",
                    style = MaterialTheme.editorialTypography.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                )
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                onClick = { runAction(OnOpenReviewSheetAction()) },
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                ) {
                    ReviewDocumentText(
                        document = reviewDocument,
                        style = MaterialTheme.editorialTypography.body.copy(
                            fontStyle = FontStyle.Normal,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = REVIEW_COLLAPSED_LINES,
                        overflow = TextOverflow.Ellipsis,
                        onClick = { runAction(OnOpenReviewSheetAction()) },
                    )

                    if (hasSpoilers) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Marked as containing spoilers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShelveRow(
    label: String,
    iconRes: RhaydusIconResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingStatus: String? = null,
    celebrationKey: Int = 0,
    enabled: Boolean = true,
) {
    val selectedContainer = MaterialTheme.colorScheme.primary
    val unselectedContainer = Color.Transparent
    val selectedContent = MaterialTheme.colorScheme.onPrimary
    val unselectedContent = MaterialTheme.colorScheme.onSurfaceVariant

    val playMotion = playDecorativeMotion()

    var settledSelected by remember { mutableStateOf(selected) }
    val wipe = remember { Animatable(initialValue = 1f) }

    LaunchedEffect(selected) {
        if (selected == settledSelected) return@LaunchedEffect

        if (playMotion.not()) {
            wipe.snapTo(targetValue = 1f)
            settledSelected = selected
            return@LaunchedEffect
        }

        wipe.snapTo(targetValue = 0f)
        wipe.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 180),
        )
        settledSelected = selected
    }

    val fromContainer = if (settledSelected) selectedContainer else unselectedContainer
    val toContainer = if (selected) selectedContainer else unselectedContainer

    val contentColor by animateColorAsState(
        targetValue = if (selected) selectedContent else unselectedContent,
        animationSpec = tween(durationMillis = 180),
        label = "ShelfChipContent",
    )

    val wipeProgress = wipe.value

    val celebration = remember { Animatable(initialValue = 1f) }

    LaunchedEffect(celebrationKey) {
        if (celebrationKey == 0 || playMotion.not()) return@LaunchedEffect

        celebration.snapTo(targetValue = 0f)
        celebration.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400),
        )
    }

    val progress = celebration.value

    val iconScale = if (progress < 0.5f) {
        1f + progress * 2f * 0.25f
    } else {
        1.25f - (progress - 0.5f) * 2f * 0.25f
    }

    val iconReveal = (progress / 0.6f).coerceIn(
        minimumValue = 0f,
        maximumValue = 1f,
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(11.dp))
            .drawBehind {
                drawRect(color = fromContainer)
                clipRect(right = size.width * wipeProgress) {
                    drawRect(color = toContainer)
                }
            },
        color = Color.Transparent,
        contentColor = contentColor,
        shape = RoundedCornerShape(11.dp),
        onClick = onClick,
        enabled = enabled && selected.not(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = iconRes.getIconPainter(),
                contentDescription = iconRes.contentDescription,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
                    .drawWithContent {
                        clipRect(right = size.width * iconReveal) {
                            this@drawWithContent.drawContent()
                        }
                    },
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
            )

            if (selected && trailingStatus != null) {
                Text(
                    text = trailingStatus,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFeatureSettings = "tnum",
                    ),
                    color = contentColor.copy(alpha = 0.9f),
                )
            }
        }
    }
}

/**
 * Small in-flow section label with the 20×1 inline hairline bar + `eyebrowSmall` (design-system.md
 * §2.3 "inline bar" contract). Used for compact labels living inside a card or hero region — here,
 * the "Shelve this book" opener — never mixed with the full [SectionLabel] bar.
 */
@Composable
private fun InlineAccentLabel(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(1.dp)
                .width(20.dp)
                .background(color),
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrowSmall,
            color = color,
        )
    }
}

/**
 * Small in-flow label with NO accent bar (design-system.md's "small in-flow labels" contract) — used
 * for the compact labels that sit directly inside a section's flow rather than opening a new region:
 * YOUR RATING / YOUR TAGS / YOUR REVIEW / TAGS / FIND IT.
 */
@Composable
private fun SmallSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.editorialTypography.eyebrowSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
// endregion
// region Top Bar Overflow / Status
@Composable
internal fun BookOverflowMenu(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    isOnline: Boolean,
    iconColors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
) {
    if (state.loadingBookDetails) return

    val book = state.book ?: return

    val isOnShelf = book.status != BookStatus.None

    var menuOpen by remember { mutableStateOf(false) }
    val dismiss = { menuOpen = false }

    Box {
        DesktopTooltip(text = "More actions") {
            IconButton(
                onClick = { menuOpen = true },
                colors = iconColors,
            ) {
                val moreActionsIcon = drawableIconResource(
                    icon = SoftcoverIcon.Edit,
                    contentDescription = "More actions",
                )

                Icon(
                    painter = moreActionsIcon.getIconPainter(),
                    contentDescription = moreActionsIcon.contentDescription,
                )
            }
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = dismiss,
        ) {
            DropdownMenuItem(
                text = { Text(text = "Share") },
                leadingIcon = {
                    val shareIcon = drawableIconResource(
                        icon = SoftcoverIcon.Share,
                        contentDescription = "",
                    )

                    Icon(
                        painter = shareIcon.getIconPainter(),
                        contentDescription = shareIcon.contentDescription,
                    )
                },
                onClick = {
                    dismiss()
                    runAction(OnShareBookClickAction())
                },
            )

            if (isOnline) {
                DropdownMenuItem(
                    text = { Text(text = "Choose lists") },
                    leadingIcon = {
                        val chooseListsIcon = drawableIconResource(
                            icon = SoftcoverIcon.BookmarkAdd,
                            contentDescription = "",
                        )

                        Icon(
                            painter = chooseListsIcon.getIconPainter(),
                            contentDescription = chooseListsIcon.contentDescription,
                        )
                    },
                    onClick = {
                        dismiss()
                        runAction(OnShowChooseListsSheetAction())
                    },
                )
            }

            // Switching editions is a preview that works without a user book, so it sits above the
            // on-shelf gate. Marking an edition as owned mutates the user's shelf, so it stays below.
            // Hidden when the book has no known editions (e.g. a freshly added-by-ISBN book) — there
            // is nothing to switch between.
            if (isOnline && book.editions.isNotEmpty()) {
                DropdownMenuItem(
                    text = { Text(text = "Change edition") },
                    leadingIcon = {
                        val changeEditionIcon = drawableIconResource(
                            icon = SoftcoverIcon.LibraryBooks,
                            contentDescription = "",
                        )

                        Icon(
                            painter = changeEditionIcon.getIconPainter(),
                            contentDescription = changeEditionIcon.contentDescription,
                        )
                    },
                    onClick = {
                        dismiss()
                        runAction(OnShowEditEditionSheetClickAction())
                    },
                )
            }

            // Owning an edition is a list operation, independent of the reading shelf, so it stays
            // available off-shelf (e.g. for a scanned edition) and sits above the on-shelf gate.
            // The displayed edition — the scanned one when arriving from a scan — is what it acts on.
            if (isOnline) {
                state.displayedEdition?.let { ownedEdition ->
                    val isOwned = state.isEditionOwned(edition = ownedEdition)

                    val ownedLabel = if (isOwned) "Unmark as owned" else "Mark as owned"
                    val ownedToggleIcon = drawableIconResource(
                        icon = if (isOwned) SoftcoverIcon.Close else SoftcoverIcon.Check,
                        contentDescription = "",
                    )

                    DropdownMenuItem(
                        text = { Text(text = ownedLabel) },
                        leadingIcon = {
                            Icon(
                                painter = ownedToggleIcon.getIconPainter(),
                                contentDescription = ownedToggleIcon.contentDescription,
                            )
                        },
                        onClick = {
                            dismiss()
                            runAction(
                                OnEditionOwnedToggleAction(
                                    edition = ownedEdition,
                                    owned = isOwned.not(),
                                ),
                            )
                        },
                    )
                }
            }

            if (isOnShelf.not()) return@DropdownMenu

            if (state.deadline == null) {
                DropdownMenuItem(
                    text = { Text(text = "Set deadline") },
                    leadingIcon = {
                        val setDeadlineIcon = drawableIconResource(
                            icon = SoftcoverIcon.DateRange,
                            contentDescription = "",
                        )

                        Icon(
                            painter = setDeadlineIcon.getIconPainter(),
                            contentDescription = setDeadlineIcon.contentDescription,
                        )
                    },
                    onClick = {
                        dismiss()
                        runAction(OnOpenDeadlinePickerAction())
                    },
                )
            } else {
                DropdownMenuItem(
                    text = { Text(text = "Edit deadline") },
                    leadingIcon = {
                        val editDeadlineIcon = drawableIconResource(
                            icon = SoftcoverIcon.DateRange,
                            contentDescription = "",
                        )

                        Icon(
                            painter = editDeadlineIcon.getIconPainter(),
                            contentDescription = editDeadlineIcon.contentDescription,
                        )
                    },
                    onClick = {
                        dismiss()
                        runAction(OnOpenDeadlinePickerAction())
                    },
                )

                DropdownMenuItem(
                    text = { Text(text = "Clear deadline") },
                    leadingIcon = {
                        val clearDeadlineIcon = drawableIconResource(
                            icon = SoftcoverIcon.Delete,
                            contentDescription = "",
                        )

                        Icon(
                            painter = clearDeadlineIcon.getIconPainter(),
                            contentDescription = clearDeadlineIcon.contentDescription,
                        )
                    },
                    onClick = {
                        dismiss()
                        runAction(OnClearDeadlineAction())
                    },
                )
            }

            if (isOnline) {
                DropdownMenuItem(
                    text = { Text(text = "Remove") },
                    leadingIcon = {
                        val removeIcon = drawableIconResource(
                            icon = SoftcoverIcon.Delete,
                            contentDescription = "",
                        )

                        Icon(
                            painter = removeIcon.getIconPainter(),
                            contentDescription = removeIcon.contentDescription,
                        )
                    },
                    onClick = {
                        dismiss()
                        runAction(OnRemoveBookClickAction(book = book))
                    },
                )
            }
        }
    }
}

/**
 * The "In progress" section (Yours lens, `status == Reading`). Section-opener bar + eyebrow, a
 * trailing "Update" pill opening the existing Update-progress sheet, the percent as a `statHero`
 * (tabular), the M3 expressive **wavy** progress indicator (never the flat bar — design-system.md's
 * "wavy always" rule), the `p. x of y` / `n pages to go` counts row, the reading-pace forecast line
 * (hidden when unavailable), and the existing [DeadlineRow] when a deadline is set.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun InProgressSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val book = state.book ?: return
    val read = book.userBookRead ?: return
    val edition = book.currentEdition ?: return
    val progress = read.progress
    val isAudiobook = edition.isAudiobook

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = "In progress")

            Surface(
                shape = RoundedCornerShape(percent = 50),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.primary,
                onClick = { runAction(OnShowUpdateProgressSheetClickAction()) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val updateProgressIcon = drawableIconResource(
                        icon = SoftcoverIcon.Edit,
                        contentDescription = "Update progress",
                    )

                    Icon(
                        painter = updateProgressIcon.getIconPainter(),
                        contentDescription = updateProgressIcon.contentDescription,
                        modifier = Modifier.size(15.dp),
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Update",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${progress.roundToInt()}%",
            style = MaterialTheme.editorialTypography.statHero,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(10.dp))

        LinearWavyProgressIndicator(
            progress = { (progress / 100f).coerceIn(
                0f,
                1f,
            ) },
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp),
        )

        Spacer(modifier = Modifier.height(10.dp))

        val summaryLeft: String
        val summaryRight: String

        if (isAudiobook) {
            val totalSeconds = edition.audioSeconds ?: 0
            val currentSeconds = read.currentSeconds ?: 0
            val remainingSeconds = (totalSeconds - currentSeconds).coerceAtLeast(0)

            summaryLeft = "${secondsToHm(currentSeconds)} of ${secondsToHm(totalSeconds)}"
            summaryRight = "${secondsToHm(remainingSeconds)} left"
        } else {
            val pageProgress = read.currentPage ?: 0
            val left = edition.pages?.minus(pageProgress) ?: 0

            summaryLeft = "p. $pageProgress of ${edition.pages ?: 0}"
            summaryRight = "$left pages to go"
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = summaryLeft,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = summaryRight,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Both the pace forecast and the deadline row arrive on their own timers well after the
        // progress block above is already settled (the forecast is a separate network round trip;
        // the deadline collector is independent too) — and either may never arrive at all for a
        // given book. Reserving their height permanently would leave a dead gap far more often than
        // it earns its keep, so both reveal with the app's standard vertical-reveal register (§5
        // "Book-detail in-progress stat") instead of popping in and shoving the row(s) beneath them
        // down in a single frame.
        val playMotion = playDecorativeMotion()
        val revealEnter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None
        val revealExit = if (playMotion) shrinkVertically() + fadeOut() else ExitTransition.None

        // AnimatedVisibility's exit transition animates whatever the content lambda renders during
        // PostExit — if that reads the live nullable directly, `?.let` collapses to nothing the same
        // recomposition `visible` flips false, so shrinkVertically()/fadeOut() have no content left
        // to shrink/fade and the row disappears in one frame. Render off a "sticky" last-known value
        // instead, updated only while the live value is non-null, so the exiting row keeps its
        // content through the whole shrink/fade.
        val paceForecast = state.readingPaceForecast
        var lastPaceForecast by remember { mutableStateOf(paceForecast) }

        if (paceForecast != null) {
            lastPaceForecast = paceForecast
        }

        AnimatedVisibility(
            visible = paceForecast != null,
            enter = revealEnter,
            exit = revealExit,
        ) {
            lastPaceForecast?.let { forecast ->
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Finish in ${forecast.forecastReadingDays} reading days",
                        style = MaterialTheme.editorialTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        val deadlineProgress = state.deadlineProgress
        var lastDeadlineProgress by remember { mutableStateOf(deadlineProgress) }

        if (deadlineProgress != null) {
            lastDeadlineProgress = deadlineProgress
        }

        AnimatedVisibility(
            visible = deadlineProgress != null,
            enter = revealEnter,
            exit = revealExit,
        ) {
            lastDeadlineProgress?.let { deadline ->
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    DeadlineRow(
                        progress = deadline,
                        dateStyle = state.dateStyle,
                    )
                }
            }
        }
    }
}

@Composable
private fun DeadlineRow(
    progress: DeadlineProgress,
    dateStyle: DateStyle,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 12.dp,
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val deadlineIcon = drawableIconResource(
                    icon = SoftcoverIcon.DateRange,
                    contentDescription = "Deadline icon",
                )

                Icon(
                    painter = deadlineIcon.getIconPainter(),
                    contentDescription = deadlineIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Finish by ${dateStyle.formatter.format(progress.deadline)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                DeadlineBadge(status = progress.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = buildGoalText(progress = progress),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReadInfoCallout(state: BookDetailUiState) {
    val book = state.book ?: return
    val userBook = book.userBook ?: return

    StatusCallout(
        eyebrow = "Finished",
        iconRes = drawableIconResource(
            icon = SoftcoverIcon.BookmarkCheck,
            contentDescription = "",
        ),
        body = when (
            val readDate = userBook.getReadDateString(
                style = state.dateStyle,
                finishedAt = book.userBookRead?.finishedAt,
            )
        ) {
            null -> "This book has been in your library since ${
                userBook.getFallbackDateString(
                    style = state.dateStyle,
                )
            }."

            else -> "You finished this on $readDate. A great one to revisit."
        },
    )
}

@Composable
private fun DnfInfoCallout(state: BookDetailUiState) {
    val userBook = state.book?.userBook ?: return

    StatusCallout(
        eyebrow = "Did not finish",
        iconRes = drawableIconResource(
            icon = SoftcoverIcon.Bookmark,
            contentDescription = "",
        ),
        body = when (val dnfDate = userBook.getDnfDateString(style = state.dateStyle)) {
            null -> "This book has been in your library since ${
                userBook.getFallbackDateString(
                    style = state.dateStyle,
                )
            }."

            else -> "You set this aside on $dnfDate."
        },
    )
}

@Composable
private fun WantToReadInfoCallout(state: BookDetailUiState) {
    val userBook = state.book?.userBook ?: return

    StatusCallout(
        eyebrow = "Up next",
        iconRes = drawableIconResource(
            icon = SoftcoverIcon.BookmarkAdd,
            contentDescription = "",
        ),
        body = "On your shelf since ${userBook.getFallbackDateString(style = state.dateStyle)}. Ready when you are.",
    )
}

@Composable
private fun StatusCallout(
    eyebrow: String,
    iconRes: RhaydusIconResource,
    body: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp,
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = iconRes.getIconPainter(),
                    contentDescription = iconRes.contentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.editorialTypography.eyebrow,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = body,
                style = MaterialTheme.editorialTypography.body,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * The "Yours" lens content (design-system.md's lens-toggle pattern): your-copy sections in order —
 * in-progress / DNF / read / want-to-read status, your rating, your tags, your review. Each section
 * keeps its existing visibility gate, so a section with nothing to show simply doesn't compose.
 */
@Composable
internal fun YoursLensContent(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val book = state.book ?: return

    Column(modifier = Modifier.fillMaxWidth()) {
        when (book.status) {
            BookStatus.Reading -> InProgressSection(
                state = state,
                runAction = runAction,
            )

            BookStatus.DidNotFinish -> DnfInfoCallout(state = state)
            BookStatus.Read -> ReadInfoCallout(state = state)
            BookStatus.WantToRead -> WantToReadInfoCallout(state = state)
            BookStatus.None -> Unit
        }

        if (book.status == BookStatus.Read) {
            Spacer(modifier = Modifier.height(28.dp))

            PersonalRatingRow(
                book = book,
                runAction = runAction,
            )
        }

        if (book.userBook != null) {
            Spacer(modifier = Modifier.height(28.dp))

            UserTagsSection(
                state = state,
                runAction = runAction,
            )
        }

        if (book.status == BookStatus.Read) {
            Spacer(modifier = Modifier.height(28.dp))

            PersonalReviewSection(
                reviewDocument = book.userBook?.reviewDocument,
                hasSpoilers = book.userBook?.reviewHasSpoilers == true,
                runAction = runAction,
            )
        }
    }
}
// endregion
// region About
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ScanEditionUpdateBanner(
    isUpdating: Boolean,
    runAction: (BookDetailAction) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(
                start = 20.dp,
                top = 16.dp,
                end = 8.dp,
                bottom = 16.dp,
            ),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ALREADY ON YOUR SHELVES",
                    style = MaterialTheme.editorialTypography.eyebrowSmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "You've added a different edition of this book. Update it to the one you scanned?",
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isUpdating) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularWavyProgressIndicator(modifier = Modifier.size(20.dp))

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Updating edition…",
                            style = MaterialTheme.editorialTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    RhaydusButton(
                        label = "Update edition",
                        style = ButtonStyle.TONAL,
                        size = ButtonSize.S,
                        onClick = { runAction(OnUpdateToScannedEditionClickAction()) },
                    )
                }
            }

            IconButton(
                onClick = { runAction(OnDismissScanEditionBannerClickAction()) },
                modifier = Modifier.size(32.dp),
            ) {
                val dismissIcon = drawableIconResource(
                    icon = SoftcoverIcon.Close,
                    contentDescription = "Dismiss",
                )

                Icon(
                    painter = dismissIcon.getIconPainter(),
                    contentDescription = dismissIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
internal fun AboutSection(state: BookDetailUiState) {
    val headline = state.book?.headline.orEmpty()
    val description = state.book?.description.orEmpty()

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionLabel(text = "About")

        Spacer(modifier = Modifier.height(12.dp))

        SkeletonCrossfade(
            isLoading = state.loadingBookDetails && headline.isBlank() && description.isBlank(),
            label = "AboutSection",
        ) { loading ->
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmer(isLoading = true),
                )
            } else {
                Column {
                    if (headline.isNotBlank()) {
                        Text(
                            text = headline,
                            style = MaterialTheme.editorialTypography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (headline.isNotBlank() && description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (description.isNotBlank()) {
                        DropCapText(
                            text = htmlToAnnotatedString(html = description),
                            bodyStyle = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 26.sp,
                            ),
                            bodyColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            dropCapFontFamily = displayFontFamily(),
                        )
                    } else if (headline.isBlank()) {
                        Text(
                            text = "No description for this book yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UserTagsSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val tags = state.userTags

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SmallSectionLabel(text = "Your tags")

        Spacer(modifier = Modifier.height(16.dp))

        if (tags.isEmpty()) {
            DashedTagOpenerChip(
                label = "+ Add tags",
                onClick = { runAction(OnOpenTagEditorAction()) },
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                tags.forEach { tag ->
                    key(tag.category, tag.name) {
                        PillChip(label = tag.name)
                    }
                }

                DashedTagOpenerChip(
                    label = "Edit tags",
                    onClick = { runAction(OnOpenTagEditorAction()) },
                )
            }
        }
    }
}

/**
 * A dashed-`outline` pill with a primary label (design-system.md's "your tags" opener) — the "+ Add
 * tags" / "Edit tags" affordance that opens [TagEditorBottomSheet]. Distinct from the solid
 * [PillChip] used for read-only tags.
 */
@Composable
private fun DashedTagOpenerChip(
    label: String,
    onClick: () -> Unit,
) {
    val color = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(percent = 50)

    Surface(
        modifier = Modifier
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    color = color,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(6f, 4f)),
                    ),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
            },
        color = Color.Transparent,
        contentColor = color,
        shape = shape,
        onClick = onClick,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

/**
 * The "Tags" section (The Book lens): the community tag block (grouped by category, top-5,
 * content-warning tags concealed via [ConcealableTagChip]'s spoiler reveal-in-place), followed by the
 * edition colophon line (publisher · format, year · ISBN-13) — folded into one section per the spec,
 * rather than two separate strips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TagsSection(state: BookDetailUiState) {
    val tags = state.book?.tags.orEmpty()

    val groups: List<Pair<TagCategory, List<Tag>>> = remember(tags) {
        listOf(
            TagCategory.GENRE,
            TagCategory.MOOD,
            TagCategory.CONTENT_WARNING,
        ).mapNotNull { category ->
            // The remote query orders by count desc, but the Room (offline) read does not
            // preserve that order — sort here so both paths rank identically.
            val top = tags
                .filter { it.category == category }
                .sortedByDescending { it.count }
                .take(5)

            top.takeIf { it.isNotEmpty() }?.let { category to it }
        }
    }

    val edition = state.displayedEdition
    val colophon = editionColophonLine(edition = edition)
    val hasContent = groups.isNotEmpty() || colophon != null

    // Community tags and the edition colophon are both unknown until the book/edition resolves
    // (rarely before, since neither travels on `initialCover`), so this section is entirely absent
    // through the loading phase — a bare content-dependent condition, exactly like the DS's
    // "Actionable inline banner" precedent, so no skeleton is reserved for it. What DOES need
    // fixing is the pop itself: appearing mid-column with no transition shoves Find it / Voices
    // down in a single frame, so the reveal rides the standard vertical-reveal register instead.
    val playMotion = playDecorativeMotion()

    AnimatedVisibility(
        visible = hasContent,
        enter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(36.dp))

            if (groups.isNotEmpty()) {
                SmallSectionLabel(text = "Tags")
            }

            groups.forEach { (category, categoryTags) ->
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = category.label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categoryTags.forEach { tag ->
                        if (category == TagCategory.CONTENT_WARNING) {
                            key(tag.id) {
                                ConcealableTagChip(label = tag.name)
                            }
                        } else {
                            PillChip(label = tag.name)
                        }
                    }
                }
            }

            if (colophon != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = colophon,
                    style = MaterialTheme.editorialTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** `{publisher} · {format}, {year} · ISBN-13 {isbn13}`, keeping only the parts that are known. */
private fun editionColophonLine(edition: BookEdition?): String? {
    if (edition == null) return null

    val publisher = edition.publisher?.takeIf { it.isNotBlank() }

    val formatAndYear = buildString {
        val format = edition.format.takeIf { it.isNotBlank() }
        val year = edition.releaseYear.takeIf { it != -1 && it > 0 }

        if (format != null) append(format)

        if (year != null) {
            if (format != null) append(", ")
            append(year)
        }
    }.takeIf { it.isNotBlank() }

    val isbn13 = edition.isbn13?.takeIf { it.isNotBlank() }?.let { "ISBN-13 $it" }

    val parts = listOfNotNull(publisher, formatAndYear, isbn13)

    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = " · ")
}

@Composable
private fun ConcealableTagChip(label: String) {
    var revealed by rememberSaveable { mutableStateOf(false) }

    if (revealed) {
        PillChip(label = label)
    } else {
        PillChip(
            label = label,
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .clickable(
                    onClickLabel = "Reveal content warning",
                    role = Role.Button,
                ) {
                    revealed = true
                },
            concealed = true,
        )
    }
}

/**
 * The "Find it" section (The Book lens): labeled outline pills — icon + label, hairline `outline`
 * border, `999` radius — replacing the earlier icon-only strip (design-system.md §5 external-links
 * pattern). Same UiEvent-based handoff, same ISBN gating, same verbose content descriptions.
 */
@Composable
internal fun ExternalLinksSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val isbn = state.displayedEdition?.let { edition ->
        edition.isbn13?.takeIf { it.isNotBlank() } ?: edition.isbn10?.takeIf { it.isNotBlank() }
    }

    // The ISBN is unknown until the edition resolves, almost always after t0 — so, like Tags, this
    // section is condition-gated rather than skeleton-reserved, but its arrival still animates in
    // (§5 "Book-detail lens-section reveal") instead of popping and shoving Voices down.
    // `AnimatedVisibility` must stay composed across the null→non-null flip for the enter transition
    // to play at all, so this reads `isbn` inside the content slot rather than guard-returning early.
    val playMotion = playDecorativeMotion()

    AnimatedVisibility(
        visible = isbn != null,
        enter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        isbn?.let { knownIsbn ->
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(36.dp))

                SmallSectionLabel(text = "Find it")

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ExternalLinkPill(
                        label = "Bookshop",
                        iconRes = drawableIconResource(
                            icon = SoftcoverIcon.Storefront,
                            contentDescription = "Find on Bookshop.org",
                        ),
                        onClick = {
                            runAction(
                                OnExternalLinkClickAction(
                                    url = "https://bookshop.org/search?keywords=$knownIsbn",
                                ),
                            )
                        },
                    )

                    ExternalLinkPill(
                        label = "Amazon",
                        iconRes = drawableIconResource(
                            icon = SoftcoverIcon.ShoppingBag,
                            contentDescription = "Find on Amazon",
                        ),
                        onClick = {
                            runAction(
                                OnExternalLinkClickAction(
                                    url = "https://www.amazon.com/s?k=$knownIsbn",
                                ),
                            )
                        },
                    )

                    ExternalLinkPill(
                        label = "OpenLibrary",
                        iconRes = drawableIconResource(
                            icon = SoftcoverIcon.LibraryBooks,
                            contentDescription = "Find on OpenLibrary",
                        ),
                        onClick = {
                            runAction(
                                OnExternalLinkClickAction(
                                    url = "https://openlibrary.org/search?isbn=$knownIsbn",
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExternalLinkPill(
    label: String,
    iconRes: RhaydusIconResource,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(percent = 50),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = iconRes.getIconPainter(),
                contentDescription = iconRes.contentDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            )
        }
    }
}
// endregion
// region Reviews
/**
 * "Voices" (The Book lens): section-opener bar + eyebrow, the "What readers think" italic headline,
 * the community review cards, and a footer summary line (`★ avg across N ratings`).
 */
@Composable
internal fun ReviewsSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    // Reviews fetch on their own timer, separate from and typically slower than the book itself
    // (`loadingReviews`), so this stayed entirely absent through both phases before — the "second
    // pop" once the rest of the lens had already settled. It now shows a skeleton matching the real
    // card anatomy (same `surfaceContainerLow` shimmer, same header) for the whole `isLoading` span,
    // the same crossfade idiom `AboutSection` / `ShelveControlCard` already use, and only collapses
    // to nothing once loading is truly finished and the book turns out to have no reviews.
    val isLoading = state.loadingBookDetails || state.loadingReviews

    if (isLoading.not() && state.reviews.isEmpty()) return

    Spacer(modifier = Modifier.height(36.dp))

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionLabel(text = "Voices")

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "What readers think",
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(14.dp))

        SkeletonCrossfade(
            isLoading = isLoading,
            label = "ReviewsSection",
        ) { loading ->
            if (loading) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .shimmer(isLoading = true),
                        )
                    }
                }
            } else {
                Column {
                    state.reviews.forEachIndexed { index, review ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        ReviewCard(
                            review = review,
                            isSpoilerRevealed = review.id in state.revealedSpoilerReviewIds,
                            onRevealSpoilerClick = {
                                runAction(OnRevealReviewSpoilerAction(reviewId = review.id))
                            },
                        )
                    }

                    state.book?.let { book ->
                        if (book.ratingsCount > 0) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val footerStarIcon = drawableIconResource(
                                    icon = SoftcoverIcon.StarFilled,
                                    contentDescription = "",
                                )

                                Icon(
                                    painter = footerStarIcon.getIconPainter(),
                                    contentDescription = footerStarIcon.contentDescription,
                                    tint = RatingGold,
                                    modifier = Modifier.size(14.dp),
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "${
                                        formatDecimalNumber(
                                            value = book.rating,
                                            fractionDigits = 1,
                                        )
                                    } across ${formatGroupedNumber(book.ratingsCount)} ratings",
                                    style = MaterialTheme.editorialTypography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: BookReview,
    isSpoilerRevealed: Boolean,
    onRevealSpoilerClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(20.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val quoteAlpha = if (isSystemInDarkTheme()) 0.22f else 0.32f

            Text(
                text = "“",
                style = MaterialTheme.editorialTypography.quoteGlyph.copy(
                    fontSize = 92.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = quoteAlpha),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 4.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Whole-review spoiler gate (HEAD behavior): a review flagged as containing
                // spoilers withholds its body entirely behind a quiet "Show spoiler" text button —
                // never rendered underneath, unlike an inline-span reveal. Reveal state is
                // transient, held by the caller per review id.
                if (review.hasSpoilers && isSpoilerRevealed.not()) {
                    TextButton(onClick = onRevealSpoilerClick) {
                        Text(text = "Show spoiler")
                    }
                } else {
                    var expanded by rememberSaveable(review.id) { mutableStateOf(false) }
                    var hasOverflow by rememberSaveable(review.id) { mutableStateOf(false) }

                    ReviewDocumentText(
                        document = review.reviewDocument,
                        style = MaterialTheme.editorialTypography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (expanded) Int.MAX_VALUE else REVIEW_COLLAPSED_LINES,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layout ->
                            if (expanded.not() && layout.hasVisualOverflow) {
                                hasOverflow = true
                            }
                        },
                    )

                    if (hasOverflow) {
                        TextButton(
                            onClick = { expanded = expanded.not() },
                            contentPadding = PaddingValues(horizontal = 0.dp),
                        ) {
                            Text(text = if (expanded) "Show less" else "Show more")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RhaydusShimmerImage(
                        model = review.reviewer.avatarUrl,
                        contentDescription = "Reviewer avatar",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop,
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        val reviewerName = review.reviewer.name?.takeIf { it.isNotBlank() }
                            ?: review.reviewer.username

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = reviewerName.uppercase(),
                                style = MaterialTheme.editorialTypography.eyebrowSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )

                            review.getReviewedMonthYear()?.let { monthYear ->
                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = monthYear,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    review.rating?.let { rating ->
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp,
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val ratingBadgeIcon = drawableIconResource(
                                    icon = SoftcoverIcon.StarFilled,
                                    contentDescription = "Rating",
                                )

                                Icon(
                                    painter = ratingBadgeIcon.getIconPainter(),
                                    contentDescription = ratingBadgeIcon.contentDescription,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(14.dp),
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = formatDecimalNumber(
                                        value = rating,
                                        fractionDigits = 1,
                                    ),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = displayFontFamily(),
                                    ),
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The "The Book" lens content (design-system.md's lens-toggle pattern): the book's own facts, in
 * order — About, Tags (community tags + edition colophon), Find it, Voices. 36dp gaps between
 * sections, per the design system's "gap between distinct content blocks" rhythm.
 */
@Composable
internal fun TheBookLensContent(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    // Each section below (Tags, Find it, Voices) supplies its own leading 36dp gap, guarded on the
    // same early-return that decides whether it renders anything — so a book missing one of them
    // (e.g. no ISBN, no community tags or colophon) never leaves a dead gap between its neighbors.
    Column(modifier = Modifier.fillMaxWidth()) {
        AboutSection(state = state)

        TagsSection(state = state)

        ExternalLinksSection(
            state = state,
            runAction = runAction,
        )

        ReviewsSection(
            state = state,
            runAction = runAction,
        )
    }
}
// endregion
// region Section Label
@Composable
private fun SectionLabel(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    pulseKey: Int = 0,
) {
    val playMotion = playDecorativeMotion()

    val pulse = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(pulseKey) {
        if (pulseKey == 0 || playMotion.not()) return@LaunchedEffect

        pulse.snapTo(targetValue = 0f)
        pulse.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400),
        )
    }

    val envelope = (1f - abs(pulse.value * 2f - 1f)).coerceIn(
        minimumValue = 0f,
        maximumValue = 1f,
    )

    val barWidth = 32.dp + (16.dp * envelope)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(barWidth)
                .graphicsLayer {
                    scaleY = 1f + envelope * 0.5f
                }
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrow,
            color = color,
        )
    }
}
// endregion
// region Helpers
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlinePickerDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val initialMillis = remember(initialDate) {
        (initialDate ?: currentLocalDate())
            .atStartOfDayIn(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    }

    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        val picked = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date

                        onConfirm(picked)
                    } else {
                        onDismiss()
                    }
                },
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

private fun buildGoalText(progress: DeadlineProgress): String {
    if (progress.isExpired) {
        val daysPast = -progress.daysRemaining
        val dayLabel = if (daysPast == 1L) "day" else "days"

        return "Deadline passed $daysPast $dayLabel ago."
    }

    val required = ceilToInt(progress.requiredPerDay)
    val dayLabel = if (progress.daysRemaining == 1L) "day" else "days"

    val isSeconds = progress.unit == DeadlineUnit.SECONDS

    val verb = if (isSeconds) "Listen to" else "Read"
    val perDayLabel = if (isSeconds) {
        secondsToHm(required)
    } else {
        val pageLabel = if (required == 1) "page" else "pages"

        "$required $pageLabel"
    }

    val base =
        "$verb $perDayLabel/day for the next ${progress.daysRemaining} $dayLabel to finish on time."

    if (progress.isOnTrack || progress.unitsBehindSchedule <= 0) return base

    val behindLabel = if (isSeconds) {
        secondsToHm(progress.unitsBehindSchedule)
    } else {
        val label = if (progress.unitsBehindSchedule == 1) "page" else "pages"

        "${progress.unitsBehindSchedule} $label"
    }

    return "You're $behindLabel behind schedule — $base"
}

private fun ceilToInt(value: Float): Int {
    val rounded = value.toInt()

    return if (value > rounded) rounded + 1 else rounded
}
// endregion
// region Overlays
/**
 * Every modal surface the Book Detail screen can raise — share, tag editor, choose-lists, review
 * editor, edition selector, deadline picker, and the update-progress sheet — hosted in one place so
 * both the mobile and desktop layouts raise an identical set of overlays from a single call. Each is
 * gated on its own state flag; nothing renders until the matching flag is set.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun BookDetailOverlays(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    onCreateNewListClick: () -> Unit,
) {
    if (state.isShareSheetVisible && state.book != null) {
        ShareBookBottomSheet(
            book = state.book,
            edition = state.displayedEdition,
            currentUsername = state.currentUsername,
            currentUserAvatarUrl = state.currentUserAvatarUrl,
            userTags = state.userTags,
            onDismissRequest = { runAction(OnDismissShareSheetAction()) },
        )
    }

    if (state.showTagEditorSheet && state.book != null) {
        TagEditorBottomSheet(
            bookTitle = state.book.title,
            edition = state.displayedEdition,
            defaultEdition = state.book.defaultEdition,
            userTags = state.userTags,
            selectedCategory = state.tagEditorCategory,
            draft = state.tagEditorInput,
            onCategorySelected = { runAction(OnTagEditorCategoryChangeAction(category = it)) },
            onDraftChange = { runAction(OnTagDraftChangeAction(input = it)) },
            onAddTag = { name, category ->
                runAction(
                    OnAddUserTagAction(
                        name = name,
                        category = category,
                    ),
                )
            },
            onRemoveTag = { runAction(OnRemoveUserTagAction(tag = it)) },
            onToggleSpoiler = { runAction(OnToggleUserTagSpoilerAction(tag = it)) },
            onDismissRequest = { runAction(OnDismissTagEditorAction()) },
        )
    }

    if (state.showChooseListsSheet && state.book != null) {
        ChooseListsBottomSheet(
            bookIds = setOf(state.book.id),
            customLists = state.userLists.filter { it.isOwned.not() },
            listsBeingMutated = state.listsBeingMutated,
            onDismissRequest = { runAction(OnDismissChooseListsSheetAction()) },
            onToggleMembership = { listId, membership ->
                runAction(
                    OnToggleListMembershipAction(
                        listId = listId,
                        isMember = membership == ListMembership.ALL,
                    ),
                )
            },
            onCreateNewListClick = onCreateNewListClick,
        )
    }

    val reviewBook = state.book
    if (state.showReviewSheet && reviewBook != null) {
        ReviewEditorBottomSheet(
            initialDocument = state.reviewEditorDocument,
            initialHasSpoilers = state.reviewEditorHasSpoilers,
            canDelete = reviewBook.userBook?.reviewDocument != null,
            onSave = { document, hasSpoilers ->
                runAction(
                    OnSaveReviewAction(
                        book = reviewBook,
                        review = document,
                        hasSpoilers = hasSpoilers,
                    ),
                )
            },
            onDelete = { runAction(OnDeleteReviewAction(book = reviewBook)) },
            onDismissRequest = { runAction(OnDismissReviewSheetAction()) },
        )
    }

    val currentEditionForSheet = state.displayedEdition
    if (state.showEditEditionSheet && state.book != null && currentEditionForSheet != null) {
        EditionBottomSheetSelector(
            bookTitle = state.book.title,
            currentEdition = currentEditionForSheet,
            defaultEdition = state.book.defaultEdition,
            editions = state.filteredEditions,
            isLoading = state.loadingEditions,
            searchQuery = state.editionSearchQuery,
            onSearchQueryChange = {
                runAction(OnEditionSearchQueryChangeAction(query = it))
            },
            onDismissRequest = {
                runAction(OnDismissEditEditionSheetClickAction())
            },
            onConfirmClick = {
                runAction(OnNewEditionSaveClickAction(edition = it))
            },
        )
    }

    if (state.showDeadlinePicker) {
        DeadlinePickerDialog(
            initialDate = state.deadline?.deadlineDate,
            onDismiss = { runAction(OnDismissDeadlinePickerAction()) },
            onConfirm = { runAction(OnDeadlinePickedAction(date = it)) },
        )
    }

    if (state.showUpdateProgressSheet && state.book != null) {
        UpdateProgressBottomSheet(
            bookToUpdate = state.book,
            selectedTab = state.selectedProgressSheetTab,
            onDismissRequest = {
                runAction(OnDismissProgressSheetAction())
            },
            onProgressTabClick = {
                runAction(OnProgressTabClickAction(tab = it))
            },
            onUpdatePercentageClick = {
                runAction(OnUpdatePercentageProgressClickAction(newPercentage = it))
            },
            onUpdatePageProgressClick = {
                runAction(OnUpdatePageProgressClickAction(newPage = it))
            },
            onUpdateTimeProgressClick = { h, m, s ->
                runAction(
                    OnUpdateTimeProgressClickAction(
                        hours = h,
                        minutes = m,
                        seconds = s,
                    ),
                )
            },
        )
    }
}
// endregion
