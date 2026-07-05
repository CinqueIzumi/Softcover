package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.common.formatDecimalNumber
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
                            text = title ?: "",
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
// region Shelf Action Bar
@Composable
internal fun ShelfActionBar(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    celebrationKey: Int,
) {
    SkeletonCrossfade(
        isLoading = state.loadingBookDetails && state.book == null,
        label = "ShelfActionBar",
    ) { loading ->
        if (loading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                SectionLabel(text = "Your shelf")

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .shimmer(isLoading = true),
                        )
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
                            SectionLabel(
                                text = "Couldn't save — tap to retry",
                                color = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            SectionLabel(
                                text = "Your shelf",
                                pulseKey = celebrationKey,
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shakeOnError(
                                    trigger = mutationFailed,
                                    onShakeEnd = {
                                        runAction(OnClearMutationFailureAction(bookId = book.id))
                                    },
                                ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ShelfChip(
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
                                modifier = Modifier.weight(1f),
                            )

                            ShelfChip(
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
                                modifier = Modifier.weight(1f),
                                enabled = status != BookStatus.None,
                            )

                            ShelfChip(
                                label = "Read",
                                iconRes = drawableIconResource(
                                    icon = SoftcoverIcon.BookmarkCheck,
                                    contentDescription = "",
                                ),
                                selected = status == BookStatus.Read,
                                onClick = { runAction(OnMarkBookAsReadClickAction(book = book)) },
                                modifier = Modifier.weight(1f),
                                celebrationKey = celebrationKey,
                            )
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
        SectionLabel(text = "Your rating")

        Spacer(modifier = Modifier.height(10.dp))

        StarRatingInput(
            rating = book.userBook?.rating?.takeIf { it > 0.0 },
            onRatingChange = { rating ->
                runAction(OnRateBookAction(
                    book = book,
                    rating = rating,
                ),)
            },
            starIcon = drawableIconResource(
                contentDescription = "",
                icon = SoftcoverIcon.StarFilled,
            ),
            filledColor = RatingGold,
        )
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
        SectionLabel(text = "Your review")

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
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                onClick = { runAction(OnOpenReviewSheetAction()) },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
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
private fun ShelfChip(
    label: String,
    iconRes: RhaydusIconResource,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    celebrationKey: Int = 0,
    enabled: Boolean = true,
) {
    val selectedContainer = MaterialTheme.colorScheme.secondaryContainer
    val unselectedContainer = MaterialTheme.colorScheme.surfaceContainer
    val selectedContent = MaterialTheme.colorScheme.onSecondaryContainer
    val unselectedContent = MaterialTheme.colorScheme.onSurface

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
            .height(72.dp)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                drawRect(color = fromContainer)
                clipRect(right = size.width * wipeProgress) {
                    drawRect(color = toContainer)
                }
            },
        color = Color.Transparent,
        contentColor = contentColor,
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        enabled = enabled && selected.not(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = iconRes.getIconPainter(),
                contentDescription = iconRes.contentDescription,
                modifier = Modifier
                    .size(22.dp)
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

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                ),
            )
        }
    }
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
                            runAction(OnEditionOwnedToggleAction(
                                edition = ownedEdition,
                                owned = isOwned.not(),
                            ),)
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

@Composable
internal fun ShelfStatusPanel(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    SkeletonCrossfade(
        isLoading = state.loadingBookDetails && state.book == null,
        label = "ShelfStatusPanel",
    ) { loading ->
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shimmer(isLoading = true),
            )
        } else {
            val book = state.book

            if (book != null) {
                when (book.status) {
                    BookStatus.Reading -> ReadingProgressCard(
                        state = state,
                        runAction = runAction,
                    )

                    BookStatus.DidNotFinish -> DnfInfoCallout(state = state)
                    BookStatus.Read,
                    BookStatus.WantToRead,
                    BookStatus.None,
                        -> Unit
                }
            }
        }
    }
}

@Composable
internal fun BelowDescriptionStatusPanel(
    state: BookDetailUiState,
    topSpacing: Dp,
) {
    if (state.loadingBookDetails) return

    val book = state.book ?: return

    when (book.status) {
        BookStatus.Read -> {
            Spacer(modifier = Modifier.height(topSpacing))
            ReadInfoCallout(state = state)
        }

        BookStatus.WantToRead -> {
            Spacer(modifier = Modifier.height(topSpacing))
            WantToReadInfoCallout(state = state)
        }

        BookStatus.Reading,
        BookStatus.DidNotFinish,
        BookStatus.None,
            -> Unit
    }
}

@Composable
private fun ReadingProgressCard(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val book = state.book ?: return
    val read = book.userBookRead ?: return
    val edition = book.currentEdition ?: return
    val progress = read.progress
    val isAudiobook = edition.isAudiobook

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 8.dp,
                    top = 12.dp,
                    bottom = 16.dp,
                ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val progressIcon = drawableIconResource(
                        icon = if (isAudiobook) SoftcoverIcon.Headset else SoftcoverIcon.MenuBook,
                        contentDescription = "Progress icon",
                    )

                    Icon(
                        painter = progressIcon.getIconPainter(),
                        contentDescription = progressIcon.contentDescription,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (isAudiobook) "Listening progress" else "Reading progress",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = "${progress.roundToInt()}%",
                        style = MaterialTheme.editorialTypography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    IconButton(
                        onClick = {
                            runAction(OnShowUpdateProgressSheetClickAction())
                        },
                    ) {
                        val updateProgressIcon = drawableIconResource(
                            icon = SoftcoverIcon.Edit,
                            contentDescription = "Update progress",
                        )

                        Icon(
                            painter = updateProgressIcon.getIconPainter(),
                            contentDescription = updateProgressIcon.contentDescription,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { (progress / 100f).coerceIn(
                        0f,
                        1f,
                    ) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    drawStopIndicator = {},
                    gapSize = (-2).dp,
                )

                Spacer(modifier = Modifier.height(10.dp))

                val summaryText = if (isAudiobook) {
                    val totalSeconds = edition.audioSeconds ?: 0
                    val currentSeconds = read.currentSeconds ?: 0
                    val remainingSeconds = (totalSeconds - currentSeconds).coerceAtLeast(0)

                    "${secondsToHm(currentSeconds)} of ${secondsToHm(totalSeconds)} • ${
                        secondsToHm(
                            remainingSeconds,
                        )
                    } left"
                } else {
                    val pageProgress = read.currentPage ?: 0
                    val left = edition.pages?.minus(pageProgress) ?: 0

                    "$pageProgress of ${edition.pages ?: 0} pages • $left pages left"
                }

                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        state.deadlineProgress?.let { deadline ->
            Spacer(modifier = Modifier.height(12.dp))

            DeadlineRow(
                progress = deadline,
                dateStyle = state.dateStyle,
            )
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
        SectionLabel(text = "Your tags")

        Spacer(modifier = Modifier.height(16.dp))

        if (tags.isEmpty()) {
            PillChip(
                label = "Add tags",
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

                PillChip(
                    label = "Edit tags",
                    selected = true,
                    onClick = { runAction(OnOpenTagEditorAction()) },
                )
            }
        }
    }
}

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

    if (groups.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(28.dp))

        SectionLabel(text = "Tags")

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
    }
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

@Composable
internal fun EditionMetadataStrip(state: BookDetailUiState) {
    val edition = state.displayedEdition ?: return

    val publisher = edition.publisher?.takeIf { it.isNotBlank() }
    val isbn13 = edition.isbn13?.takeIf { it.isNotBlank() }
    val isbn10 = edition.isbn10?.takeIf { it.isNotBlank() }

    if (publisher == null && isbn13 == null && isbn10 == null) return

    val parts = listOfNotNull(
        publisher,
        isbn13?.let { "ISBN-13 $it" },
        isbn10?.let { "ISBN-10 $it" },
    )

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = parts.joinToString(separator = "  ·  "),
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun ExternalLinksStrip(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
) {
    val edition = state.displayedEdition ?: return

    val isbn = edition.isbn13?.takeIf { it.isNotBlank() }
        ?: edition.isbn10?.takeIf { it.isNotBlank() }
        ?: return

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(28.dp))

        SectionLabel(text = "Find it")

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExternalLinkButton(
                iconRes = drawableIconResource(
                    icon = SoftcoverIcon.Storefront,
                    contentDescription = "Find on Bookshop.org",
                ),
                onClick = {
                    runAction(
                        OnExternalLinkClickAction(
                            url = "https://bookshop.org/search?keywords=$isbn",
                        ),
                    )
                },
            )

            ExternalLinkButton(
                iconRes = drawableIconResource(
                    icon = SoftcoverIcon.ShoppingBag,
                    contentDescription = "Find on Amazon",
                ),
                onClick = {
                    runAction(
                        OnExternalLinkClickAction(
                            url = "https://www.amazon.com/s?k=$isbn",
                        ),
                    )
                },
            )

            ExternalLinkButton(
                iconRes = drawableIconResource(
                    icon = SoftcoverIcon.LibraryBooks,
                    contentDescription = "Find on OpenLibrary",
                ),
                onClick = {
                    runAction(
                        OnExternalLinkClickAction(
                            url = "https://openlibrary.org/search?isbn=$isbn",
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun ExternalLinkButton(
    iconRes: RhaydusIconResource,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = iconRes.getIconPainter(),
            contentDescription = iconRes.contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
// endregion
// region Reviews
@Composable
internal fun ReviewsSection(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    dateStyle: DateStyle,
) {
    if (state.loadingBookDetails || state.loadingReviews) return

    if (state.reviews.isEmpty()) return

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionLabel(text = "Voices")

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "What readers think",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            state.book?.let { book ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val reviewsStarIcon = drawableIconResource(
                        icon = SoftcoverIcon.StarFilled,
                        contentDescription = "",
                    )

                    Icon(
                        painter = reviewsStarIcon.getIconPainter(),
                        contentDescription = reviewsStarIcon.contentDescription,
                        tint = RatingGold,
                        modifier = Modifier.size(18.dp),
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = formatDecimalNumber(
                            value = book.rating,
                            fractionDigits = 1,
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        state.reviews.forEachIndexed { index, review ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            ReviewCard(
                review = review,
                isSpoilerRevealed = review.id in state.revealedSpoilerReviewIds,
                dateStyle = dateStyle,
                onRevealSpoilerClick = {
                    runAction(OnRevealReviewSpoilerAction(reviewId = review.id))
                },
            )
        }
    }
}

@Composable
private fun ReviewCard(
    review: BookReview,
    isSpoilerRevealed: Boolean,
    dateStyle: DateStyle,
    onRevealSpoilerClick: () -> Unit,
) {
    val formattedDate = review.getFormattedDate(style = dateStyle)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp),
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
                    .padding(
                        horizontal = 20.dp,
                        vertical = 20.dp,
                    ),
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                if (review.hasSpoilers && isSpoilerRevealed.not()) {
                    TextButton(onClick = onRevealSpoilerClick) {
                        Text(text = "Show spoiler")
                    }
                } else {
                    var expanded by rememberSaveable(review.id) { mutableStateOf(false) }
                    var hasOverflow by rememberSaveable(review.id) { mutableStateOf(false) }

                    ReviewDocumentText(
                        document = review.reviewDocument,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                        ),
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
                        Text(
                            text = review.reviewer.name?.takeIf { it.isNotBlank() }
                                ?: review.reviewer.username,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        formattedDate?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
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
