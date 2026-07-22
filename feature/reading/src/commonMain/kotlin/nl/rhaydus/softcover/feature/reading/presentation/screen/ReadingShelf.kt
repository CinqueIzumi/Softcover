package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import org.koin.compose.koinInject
import kotlin.math.roundToInt
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.common.currentLocalDateTime
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.designsystem.component.RhaydusButton
import nl.rhaydus.designsystem.component.mutationAnimated
import nl.rhaydus.designsystem.component.rememberLazyItemMutationAnimator
import nl.rhaydus.designsystem.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.designsystem.component.staggeredEntry
import nl.rhaydus.designsystem.haptics.Haptics
import nl.rhaydus.designsystem.haptics.rememberHaptics
import nl.rhaydus.designsystem.layout.rememberBottomBarPadding
import nl.rhaydus.designsystem.model.ButtonSize
import nl.rhaydus.designsystem.model.ButtonStyle
import nl.rhaydus.designsystem.modifier.hoverHighlight
import nl.rhaydus.designsystem.modifier.pointerHandCursor
import nl.rhaydus.designsystem.modifier.pressScale
import nl.rhaydus.designsystem.modifier.shakeOnError
import nl.rhaydus.designsystem.motion.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.component.DeadlineCoverOverlay
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.component.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.designsystem.presentation.component.VerdictSheet
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberEditionImageRequest
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.model.VerdictSheetContext
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.theme.ReadingHeroBackdropForeground
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineStatus
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.notification.rememberNotificationPermissionRequester
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.feature.reading.presentation.action.DismissProgressSheetAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnDismissPlanTodayAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnDismissVerdictPromptAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnSaveVerdictAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnShowProgressSheetClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdatePageProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdatePercentageProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdateTimeProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStripSheet
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

/**
 * The scrolling currently-reading column shared by both platforms: a [header] slot (the mobile
 * collapsing header vs. the desktop static one), the featured card (which fuses its own
 * plan-today/pace nudge to its top edge), and the "also reading" list. The hosting [listState] and
 * [contentPadding] are supplied by each layout so desktop can attach a scrollbar and mobile can
 * reserve bottom-bar space.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingBooksColumn(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    controller: MarkAsReadController,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
) {
    val featured = state.books.first()
    val rest = state.books.drop(1)

    val animator = rememberLazyItemMutationAnimator(keys = rest.map { it.id })

    val entry = rememberStaggeredEntryCoordinator(key = "reading:rest")

    val isInspection = LocalInspectionMode.current
    val prefetcher = if (isInspection) null else rememberBookDetailPrefetcher()

    val today = remember { currentLocalDate().toString() }

    CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            item(key = "header") {
                header()
            }

            val featuredDeadlineProgress = featured.deadlineProgressFrom(state)
            val planTodayMessage = planTodayNudgeFor(progress = featuredDeadlineProgress)
            val isPlanTodayDismissed = state.dismissedPlanTodayByBook[featured.id] == today

            item(key = "featured-${featured.id}") {
                FeaturedBookCard(
                    book = featured,
                    deadlineProgress = featuredDeadlineProgress,
                    mutationFailed = featured.id in state.failedMutationBookIds,
                    paceForecast = state.featuredBookPace,
                    planTodayMessage = planTodayMessage.takeIf { isPlanTodayDismissed.not() },
                    onDismissPlanToday = {
                        runAction(OnDismissPlanTodayAction(bookId = featured.id))
                    },
                    runAction = runAction,
                    onBookClick = onBookClick,
                    modifier = controller.slideModifier(featured.id),
                )
            }

            if (rest.isNotEmpty()) {
                item(key = "also-reading-label") {
                    Spacer(modifier = Modifier.height(32.dp))

                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        AlsoReadingSectionHeader(count = rest.size)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(rest, key = { _, book -> book.id }) { index, book ->
                    CompactBookEntry(
                        modifier = Modifier.mutationAnimated(
                            scope = this,
                            animator = animator,
                            itemKey = book.id,
                        )
                            .staggeredEntry(
                                coordinator = entry,
                                index = index,
                            )
                            .then(controller.slideModifier(book.id)),
                        book = book,
                        deadlineProgress = book.deadlineProgressFrom(state),
                        mutationFailed = book.id in state.failedMutationBookIds,
                        runAction = runAction,
                        onBookClick = onBookClick,
                    )
                }
            }
        }
    }
}

/**
 * The modal overlays shared by both layouts: the progress-update bottom sheet (a progress entry that
 * finishes the book fires the same celebration as the split-button "Mark as Read", gated on the real
 * finish outcome — see the verdict-prompt effect below), the verdict sheet raised on a finish, and
 * the reading-streak sheet. Confetti is routed through [controller] so every entry point shares one
 * burst.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingOverlays(
    state: ReadingScreenUiState,
    runAction: (ReadingAction) -> Unit,
    controller: MarkAsReadController,
    showStreakSheet: Boolean,
    onDismissStreakSheet: () -> Unit,
) {
    val haptics = rememberHaptics()

    if (state.bookToUpdate != null && state.showProgressSheet) {
        val updatingBook = state.bookToUpdate

        UpdateProgressBottomSheet(
            bookToUpdate = updatingBook,
            selectedTab = state.progressSheetTab,
            onDismissRequest = {
                runAction(DismissProgressSheetAction)
            },
            onProgressTabClick = {
                runAction(OnProgressTabClickAction(it))
            },
            onUpdatePercentageClick = { percentage, actionAt ->
                runAction(
                    OnUpdatePercentageProgressClickAction(
                        newPercentage = percentage,
                        actionAt = actionAt,
                    ),
                )
            },
            onUpdatePageProgressClick = { pages, actionAt ->
                runAction(
                    OnUpdatePageProgressClickAction(
                        newPage = pages,
                        actionAt = actionAt,
                    ),
                )
            },
            onUpdateTimeProgressClick = { h, m, s, actionAt ->
                runAction(
                    OnUpdateTimeProgressClickAction(
                        hours = h,
                        minutes = m,
                        seconds = s,
                        actionAt = actionAt,
                    ),
                )
            },
            onMarkAsReadClick = { actionAt ->
                // Routes through the same controller the row/hero "mark as read" affordances used
                // to drive directly, so the sheet-triggered path keeps the full commit choreography
                // (haptic, burst, bottom-bar pulse, and — when motion is enabled — the "slide to
                // shelf" follow-through, §2.5) rather than only the haptic + burst a bare dispatch
                // would give it. The picked backdate travels with it, same as the progress tabs.
                controller.requestMarkAsRead(
                    book = updatingBook,
                    actionAt = actionAt,
                )

                runAction(DismissProgressSheetAction)
            },
        )
    }

    val verdictBook = state.verdictPromptBook

    // A finish reached through the progress sheet (any of page/percentage/time) has no synchronous
    // "finished" moment to burst from, so the celebration rides the same Applied outcome that opens
    // this prompt — firing only on a genuine transition, never on a no-op re-record. The explicit
    // "mark as read" affordances already burst at the instant of the gesture, so they flag that they
    // handled this finish and the effect skips it, keeping any single finish to exactly one burst.
    LaunchedEffect(verdictBook?.id) {
        if (verdictBook == null) return@LaunchedEffect

        if (controller.consumeExplicitBurstFired().not()) {
            haptics.commit()
            controller.celebrate()
        }
    }

    if (verdictBook != null) {
        VerdictSheet(
            context = VerdictSheetContext.FINISHED,
            bookTitle = verdictBook.title,
            coverEdition = verdictBook.currentEdition,
            fallbackCoverUrl = verdictBook.coverUrl,
            initialRating = verdictBook.userBook?.rating?.takeIf { it > 0.0 },
            initialReview = verdictBook.userBook?.reviewDocument ?: ReviewDocument.EMPTY,
            initialHasSpoilers = verdictBook.userBook?.reviewHasSpoilers == true,
            canDelete = false,
            onSave = { rating, review, hasSpoilers ->
                runAction(
                    OnSaveVerdictAction(
                        book = verdictBook,
                        rating = rating,
                        review = review,
                        hasSpoilers = hasSpoilers,
                    ),
                )
            },
            onDelete = {},
            onDismissRequest = { runAction(OnDismissVerdictPromptAction()) },
        )
    }

    if (showStreakSheet) {
        StreakStripSheet(
            activity = state.recentReadingActivity,
            onDismiss = onDismissStreakSheet,
        )
    }
}

/**
 * The Reading screen's emotional centre (design-system.md §5 "Reading featured-hero card"): a
 * `surfaceContainerLow` card whose top band fuses the pace-nudge ribbon (when one applies) directly
 * onto the card rather than floating in-flow above it, a dark fixed-ink backdrop carrying the cover
 * + title/byline/pace meta, and a body section (page count, wavy progress, deadline status, hero
 * actions) on the card's own surface colour.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FeaturedBookCard(
    book: Book,
    deadlineProgress: DeadlineProgress?,
    mutationFailed: Boolean,
    paceForecast: ReadingPaceForecast?,
    planTodayMessage: String?,
    onDismissPlanToday: () -> Unit,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    val cardColor = MaterialTheme.colorScheme.surfaceContainerLow
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .prefetchBookDetailOnPress(book.id)
            .pointerHandCursor()
            .pressScale(interactionSource)
            .shakeOnError(
                trigger = mutationFailed,
                onShakeEnd = {
                    runAction(OnClearMutationFailureAction(bookId = book.id))
                },
            ),
        color = cardColor,
        shape = shape,
        onClick = { onBookClick(book) },
        interactionSource = interactionSource,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (planTodayMessage != null) {
                PaceNudgeRibbon(
                    text = planTodayMessage,
                    onDismiss = onDismissPlanToday,
                )
            }

            FeaturedBackdropCard(
                book = book,
                deadlineProgress = deadlineProgress,
                mutationFailed = mutationFailed,
                paceForecast = paceForecast,
                cardColor = cardColor,
                runAction = runAction,
            )
        }
    }
}

/**
 * The single full-card blurred-cover backdrop, restored to the pre-redesign treatment: ONE
 * continuous image (full-opacity blur, a black top scrim for the series-eyebrow's legibility near
 * the top edge, and a fade at the foot into [cardColor]) behind ALL of the card's content — cover,
 * meta, the page-count/percentage stat, the wavy bar, the deadline row, both pills, and the caption
 * — rather than a backdrop band handed off to a separate flat body surface below it. The content
 * column is the last (topmost) child, so it paints over every background layer including the fade.
 */
@Composable
private fun FeaturedBackdropCard(
    book: Book,
    deadlineProgress: DeadlineProgress?,
    mutationFailed: Boolean,
    paceForecast: ReadingPaceForecast?,
    cardColor: Color,
    runAction: (ReadingAction) -> Unit,
) {
    val isInspection = LocalInspectionMode.current
    val backdropRequest = rememberEditionImageRequest(
        edition = book.currentEdition,
        defaultEdition = book.defaultEdition,
        fallbackCoverUrl = book.coverUrl,
    )
    val foreground = ReadingHeroBackdropForeground

    Box(modifier = Modifier.fillMaxWidth()) {
        if (isInspection) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.30f),
                            ),
                        ),
                    ),
            )
        } else if (backdropRequest != null) {
            AsyncImage(
                model = backdropRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(
                        radius = 64.dp,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded,
                    ),
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Black.copy(alpha = 0.45f),
                            0.35f to Color.Black.copy(alpha = 0.15f),
                            0.55f to Color.Transparent,
                            1f to Color.Transparent,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to cardColor.copy(alpha = 0f),
                            0.40f to cardColor.copy(alpha = 0.15f),
                            0.60f to cardColor.copy(alpha = 0.92f),
                            1f to cardColor,
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                FeaturedCover(
                    book = book,
                    deadlineProgress = deadlineProgress,
                )

                FeaturedBackdropMeta(
                    book = book,
                    mutationFailed = mutationFailed,
                    paceForecast = paceForecast,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FeaturedProgressStat(
                book = book,
                foreground = foreground,
            )

            // The deadline row arrives from its own collector, independently of the progress block
            // above — and a book may never carry a deadline at all — so it reveals in place rather
            // than reserving height (the same register as the pace row above; design-system.md §5
            // "Book-detail in-progress stat").
            val playMotion = playDecorativeMotion()
            val revealEnter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None
            val revealExit = if (playMotion) shrinkVertically() + fadeOut() else ExitTransition.None

            var lastDeadlineProgress by remember { mutableStateOf(deadlineProgress) }

            if (deadlineProgress != null) {
                lastDeadlineProgress = deadlineProgress
            }

            AnimatedVisibility(
                visible = deadlineProgress != null,
                enter = revealEnter,
                exit = revealExit,
            ) {
                lastDeadlineProgress?.let { progress ->
                    Column {
                        Spacer(modifier = Modifier.height(15.dp))

                        DeadlineStatusRow(
                            status = progress.status,
                            foreground = foreground,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            RhaydusButton(
                label = "Update progress",
                style = ButtonStyle.FILLED,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Edit,
                    contentDescription = "Update progress icon",
                ),
                onClick = { runAction(OnShowProgressSheetClickAction(book = book)) },
                modifier = Modifier.fillMaxWidth(),
            )

            FeaturedSessionButton(
                book = book,
                foreground = foreground,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = timeOfDayCaption(),
                style = MaterialTheme.editorialTypography.bodySmall,
                color = foreground.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** The featured cover, 2:3 at 108dp wide, with the deadline badge overlay. */
@Composable
private fun FeaturedCover(
    book: Book,
    deadlineProgress: DeadlineProgress?,
) {
    DeadlineCoverOverlay(progress = deadlineProgress) {
        EditionImage(
            edition = book.currentEdition,
            modifier = Modifier
                .width(108.dp)
                .aspectRatio(2f / 3f),
            isLoading = false,
            defaultEdition = book.defaultEdition,
            fallbackCoverUrl = book.coverUrl,
            coverlessTitle = book.title,
            elevation = 12.dp,
            cornerRadius = 4.dp,
            shadowColor = Color.Black.copy(alpha = 0.6f),
            sharedTransitionKey = bookCoverTransitionKey(
                editionId = book.currentEdition?.id,
                bookId = book.id,
            ),
        )
    }
}

/** The series eyebrow, hero title, byline, and "Your pace" row sitting over the dark backdrop. */
@Composable
private fun FeaturedBackdropMeta(
    book: Book,
    mutationFailed: Boolean,
    paceForecast: ReadingPaceForecast?,
    modifier: Modifier = Modifier,
) {
    val overlayShadow = Shadow(
        color = Color.Black.copy(alpha = 0.85f),
        offset = Offset(
            x = 0f,
            y = 1f,
        ),
        blurRadius = 14f,
    )
    val foreground = ReadingHeroBackdropForeground

    Column(modifier = modifier) {
        val topLineText = if (mutationFailed) {
            "Couldn't save — tap to retry".uppercase()
        } else {
            book.seriesText?.takeIf { it.isNotBlank() }?.uppercase()
        }

        if (topLineText != null) {
            Text(
                text = topLineText,
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.Bold,
                    shadow = overlayShadow,
                ),
                color = if (mutationFailed) MaterialTheme.colorScheme.error else foreground.copy(alpha = 0.86f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(6.dp))
        }

        Text(
            text = book.title,
            style = MaterialTheme.editorialTypography.headlineSmall.copy(
                lineHeight = 27.sp,
                shadow = overlayShadow,
            ),
            color = foreground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        book.currentEdition?.authorString?.takeIf { it.isNotBlank() }?.let { authors ->
            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = "By $authors".uppercase(),
                style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                    letterSpacing = 1.2.sp,
                    shadow = overlayShadow,
                ),
                color = foreground.copy(alpha = 0.74f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // The pace forecast is a separate network round trip that trails the rest of the card by a
        // beat, and may never resolve for a given book at all — so it does not reserve height; it
        // reveals in place with the app's standard vertical-reveal register instead of popping in and
        // shoving the deadline row/hero actions beneath it down in a single frame (design-system.md
        // §5 "Book-detail in-progress stat").
        val playMotion = playDecorativeMotion()
        val revealEnter = if (playMotion) expandVertically() + fadeIn() else EnterTransition.None
        val revealExit = if (playMotion) shrinkVertically() + fadeOut() else ExitTransition.None

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
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Your pace".uppercase(),
                            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                                letterSpacing = 1.4.sp,
                                shadow = overlayShadow,
                            ),
                            color = foreground.copy(alpha = 0.7f),
                            maxLines = 1,
                        )

                        Spacer(modifier = Modifier.width(9.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(foreground.copy(alpha = 0.24f)),
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = paceLineText(forecast),
                        style = MaterialTheme.editorialTypography.bodySmall.copy(shadow = overlayShadow),
                        color = foreground.copy(alpha = 0.95f),
                    )
                }
            }
        }
    }
}

/**
 * "Page {x} of {y}" (or the audiobook time equivalent) beside the "NN%" stat, then a wavy bar — set
 * in [foreground] (rather than a `onSurface`/`onSurfaceVariant` theme pair) since this now sits over
 * the featured card's single full-card blurred-cover backdrop, not a flat surface below it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FeaturedProgressStat(
    book: Book,
    foreground: Color,
) {
    val progressFraction = (book.userBookRead?.progress ?: 0f) / 100f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = pageCountLabel(
                book = book,
                foreground = foreground,
            ),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = foreground,
        )

        Text(
            text = "${(book.userBookRead?.progress ?: 0f).roundToInt()}%",
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = foreground,
        )
    }

    Spacer(modifier = Modifier.height(9.dp))

    LinearWavyProgressIndicator(
        progress = {
            progressFraction.coerceIn(
                0f,
                1f,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp),
    )
}

/** "Page 348" at full [foreground] alpha followed by " of 512" demoted to 0.6 alpha, in one line. */
@Composable
private fun pageCountLabel(
    book: Book,
    foreground: Color,
): AnnotatedString {
    val edition = book.currentEdition
    val dimColor = foreground.copy(alpha = 0.6f)

    return buildAnnotatedString {
        if (edition?.isAudiobook == true) {
            val current = book.userBookRead?.currentSeconds ?: 0
            val total = edition.audioSeconds ?: 0

            append(secondsToHm(current))
            withStyle(SpanStyle(color = dimColor)) {
                append(" of ${secondsToHm(total)}")
            }
        } else {
            val currentPage = book.userBookRead?.currentPage ?: 0
            val totalPages = edition?.pages ?: book.defaultEdition?.pages

            append("Page $currentPage")
            withStyle(SpanStyle(color = dimColor)) {
                append(" of $totalPages")
            }
        }
    }
}

/**
 * Calendar glyph + the app's own [DeadlineStatus] label — `OnTrack`/`Behind`/`Expired` — never the
 * redline spec's four-state wording (design-system.md §4 mandates the app's labels for Reading).
 * [compact] shrinks the icon/text for the secondary-row inline use. [foreground], when supplied,
 * overrides the status-tinted colour with a single fixed ink — the hero passes it because the row
 * sits on the featured card's blurred-cover backdrop rather than a flat surface; the secondary row
 * (which sits on the plain page background) omits it and keeps the status-tinted colour.
 */
@Composable
private fun DeadlineStatusRow(
    status: DeadlineStatus,
    compact: Boolean = false,
    foreground: Color? = null,
) {
    val tint = foreground ?: when (status) {
        DeadlineStatus.OnTrack -> MaterialTheme.colorScheme.primary
        DeadlineStatus.Behind -> MaterialTheme.colorScheme.error
        DeadlineStatus.Expired -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val iconTint = foreground ?: MaterialTheme.colorScheme.primary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val calendarIcon = drawableIconResource(
            icon = SoftcoverIcon.DateRange,
            contentDescription = "",
        )

        Icon(
            painter = calendarIcon.getIconPainter(),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(if (compact) 13.dp else 18.dp),
        )

        Text(
            text = status.label,
            style = if (compact) {
                MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            },
            color = tint,
        )
    }
}

@Composable
private fun FeaturedSessionButton(
    book: Book,
    foreground: Color,
) {
    val controller = koinInject<ActiveSessionController>()
    val navigator = LocalNavigator.currentOrThrow
    val appNavigator = koinInject<AppNavigator>()
    val haptics = rememberHaptics()
    val active by controller.activeSession.collectAsStateWithLifecycle()

    val currentBook by rememberUpdatedState(book)

    // The lock-screen surface is a plain notification, so it needs POST_NOTIFICATIONS (Android
    // 13+). Ask at the natural moment — the first session start — then start regardless of the
    // outcome, since the in-app peek bar and Focus Mode work without the permission.
    val sessionPermissionRequester = rememberNotificationPermissionRequester(
        onResult = { controller.start(book = currentBook) },
    )

    when {
        active?.book?.id == book.id -> {
            Spacer(modifier = Modifier.height(10.dp))

            // Kept tonal (rather than the idle state's outlined pill) so the active-session state
            // still reads with a touch more weight than a plain "start" affordance.
            RhaydusButton(
                label = "Focus mode",
                style = ButtonStyle.TONAL,
                size = ButtonSize.M,
                icon = drawableIconResource(
                    icon = SoftcoverIcon.Reading,
                    contentDescription = "Focus mode icon",
                ),
                onClick = {
                    navigator.parent?.push(item = appNavigator.screen(ScreenDestination.FocusMode))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Audiobooks are tracked by listening time, not page-based reading sessions, so
        // the start control is never offered for them.
        book.currentEdition?.isAudiobook == true -> Unit

        active == null -> {
            Spacer(modifier = Modifier.height(10.dp))

            // The catalog's OUTLINED style hardcodes its border and label colour to the theme's
            // `outline` / `onSurface` roles, tuned for a plain surface — not reliably visible now
            // that the button sits on the featured card's blurred-cover backdrop. A scoped
            // ColorScheme override (shapes/typography still inherited) keeps the border and label
            // legible against the image regardless of theme, without a bespoke button component.
            MaterialTheme(
                colorScheme = MaterialTheme.colorScheme.copy(
                    outline = foreground,
                    onSurface = foreground,
                ),
            ) {
                RhaydusButton(
                    label = "Start reading session",
                    style = ButtonStyle.OUTLINED,
                    size = ButtonSize.M,
                    icon = drawableIconResource(
                        icon = SoftcoverIcon.Play,
                        contentDescription = "Start reading session icon",
                    ),
                    onClick = {
                        haptics.threshold()

                        sessionPermissionRequester.request()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** "35 pages a day — about 5 days to go" (or the audiobook time equivalent). */
private fun paceLineText(forecast: ReadingPaceForecast): String {
    val amount = when (forecast.unit) {
        DeadlineUnit.PAGES -> {
            val perDay = forecast.avgPerReadingDay.roundToInt()
            val pageLabel = if (perDay == 1) "page" else "pages"

            "$perDay $pageLabel a day"
        }

        DeadlineUnit.SECONDS -> "${secondsToHm(forecast.avgPerReadingDay.roundToInt())} a day"
    }

    val days = forecast.forecastReadingDays
    val daysLabel = if (days == 1) "day" else "days"

    return "$amount — about $days $daysLabel to go"
}

/** Mirrors [greetingForNow]'s hour buckets for the hero's italic time-of-day caption. */
private fun timeOfDayCaption(): String {
    val hour = currentLocalDateTime().hour

    return when (hour) {
        in 5..11 -> "Mornings are made for a few quiet pages before the day gets loud."
        in 12..17 -> "A good afternoon for picking up where you left off."
        in 18..21 -> "Evenings were made for this. Pick up where you stopped."
        else -> "The late hours are yours. Pick up where you stopped."
    }
}

/**
 * A secondary "also reading" row (design-system.md §5 "Reading secondary row"): a flat,
 * hairline-topped row (never its own card) holding a small cover, title/author/deadline-status,
 * a slim wavy progress line, and a trailing compact "set progress" chip.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CompactBookEntry(
    book: Book,
    deadlineProgress: DeadlineProgress?,
    mutationFailed: Boolean,
    runAction: (ReadingAction) -> Unit,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progressFraction = (book.userBookRead?.progress ?: 0f) / 100f
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .prefetchBookDetailOnPress(book.id)
                .pointerHandCursor()
                .pressScale(interactionSource)
                .hoverHighlight(interactionSource = interactionSource)
                .shakeOnError(
                    trigger = mutationFailed,
                    onShakeEnd = {
                        runAction(OnClearMutationFailureAction(bookId = book.id))
                    },
                ),
            color = Color.Transparent,
            shape = RectangleShape,
            onClick = { onBookClick(book) },
            interactionSource = interactionSource,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DeadlineCoverOverlay(progress = deadlineProgress) {
                    EditionImage(
                        edition = book.currentEdition,
                        modifier = Modifier
                            .width(54.dp)
                            .aspectRatio(2f / 3f),
                        isLoading = false,
                        defaultEdition = book.defaultEdition,
                        fallbackCoverUrl = book.coverUrl,
                        coverlessTitle = book.title,
                        elevation = 4.dp,
                        cornerRadius = 6.dp,
                        sharedTransitionKey = bookCoverTransitionKey(
                            editionId = book.currentEdition?.id,
                            bookId = book.id,
                        ),
                    )
                }

                Spacer(modifier = Modifier.width(15.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (mutationFailed) {
                        Text(
                            text = "Couldn't save — tap to retry".uppercase(),
                            style = MaterialTheme.editorialTypography.eyebrowSmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.height(2.dp))
                    } else {
                        book.seriesText?.takeIf { it.isNotBlank() }?.let { series ->
                            Text(
                                text = series.uppercase(),
                                style = MaterialTheme.editorialTypography.eyebrowSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }

                    Text(
                        text = book.title,
                        style = MaterialTheme.editorialTypography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    book.currentEdition?.authorString?.takeIf { it.isNotBlank() }
                        ?.let { authors ->
                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "By $authors",
                                style = MaterialTheme.editorialTypography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                    if (deadlineProgress != null) {
                        Spacer(modifier = Modifier.height(6.dp))

                        DeadlineStatusRow(
                            status = deadlineProgress.status,
                            compact = true,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ProgressBlock(
                        progressFraction = progressFraction,
                        percentage = book.userBookRead?.progress,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                SetProgressChip(
                    onClick = { runAction(OnShowProgressSheetClickAction(book = book)) },
                )
            }
        }
    }
}

/** Slim wavy progress line + trailing "NN%" — the secondary row's progress indication. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ProgressBlock(
    progressFraction: Float,
    percentage: Float?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        LinearWavyProgressIndicator(
            progress = {
                progressFraction.coerceIn(
                    0f,
                    1f,
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(6.dp),
        )

        Text(
            text = "${(percentage ?: 0f).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * The compact per-row "open the progress sheet" control (design-system.md §5 "Reading secondary
 * row"): a plain pencil (edit) glyph on a `surfaceContainerHigh` pill. No trailing chevron — that
 * implied a dropdown menu, but the tap always just opens the Update-progress sheet (mark-as-read
 * lives only in the sheet, not a menu on this chip).
 */
@Composable
private fun SetProgressChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DesktopTooltip(text = "Set progress") {
        Surface(
            onClick = onClick,
            modifier = modifier
                .height(38.dp)
                .pointerHandCursor()
                .clearAndSetSemantics {
                    role = Role.Button
                    contentDescription = "Set progress"
                    onClick(label = null) {
                        onClick()
                        true
                    }
                },
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                val editIcon = drawableIconResource(
                    icon = SoftcoverIcon.Edit,
                    contentDescription = "",
                )

                Icon(
                    painter = editIcon.getIconPainter(),
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                )
            }
        }
    }
}

@Composable
internal fun SectionLabel(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(4.dp)
                .width(32.dp)
                .background(MaterialTheme.colorScheme.primary),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text.uppercase(),
            style = MaterialTheme.editorialTypography.eyebrow,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * The "also reading" section opener: a tracked-caps eyebrow naming the count ("Three more in
 * motion") over the italic `headlineSmall` headline "Also between your fingers" — the two-tier
 * editorial section pattern (design-system.md §3.2), rather than [SectionLabel]'s single
 * accent+eyebrow line. Deliberately stepped down from the full `display` role, which read as
 * oversized for a body-section opener that isn't the page's own masthead.
 */
@Composable
internal fun AlsoReadingSectionHeader(count: Int) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(32.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = alsoReadingEyebrowText(count).uppercase(),
                style = MaterialTheme.editorialTypography.eyebrow,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Also between your fingers",
            style = MaterialTheme.editorialTypography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun alsoReadingEyebrowText(count: Int): String =
    if (count == 1) "One more in motion" else "$count more in motion"

@Composable
internal fun EmptyCurrentlyReadingScreen(
    wantToReadBooks: List<Book> = emptyList(),
    trendingBooks: List<Book> = emptyList(),
    streakEnabled: Boolean = false,
    recentReadingActivity: List<ReadingDayActivity> = emptyList(),
    onExpandStreak: () -> Unit = {},
    onBookClick: (Book) -> Unit = {},
    onNavigateToSearch: () -> Unit,
) {
    val pickUpNext = wantToReadBooks.take(3)
    val trendingTile = trendingBooks.firstOrNull()
    val showAdaptive = pickUpNext.isNotEmpty() || trendingTile != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState())
            .padding(bottom = rememberBottomBarPadding()),
        verticalArrangement = if (showAdaptive) Arrangement.Top else Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showAdaptive) Spacer(modifier = Modifier.height(40.dp))
        val quoteAlpha = if (isSystemInDarkTheme()) 0.15f else 0.3f

        Text(
            text = "“",
            style = MaterialTheme.editorialTypography.quoteGlyph.copy(
                fontSize = 140.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = quoteAlpha),
            modifier = Modifier
                .padding(top = 8.dp)
                .quoteGlyphSway(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "An open page awaits",
            style = MaterialTheme.editorialTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Nothing is currently between your fingers. Find a title worth losing an evening to.",
            style = MaterialTheme.editorialTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            onClick = onNavigateToSearch,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(percent = 50),
            modifier = Modifier.pointerHandCursor(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val searchIcon = drawableIconResource(
                    icon = SoftcoverIcon.Search,
                    contentDescription = "",
                )

                Icon(
                    painter = searchIcon.getIconPainter(),
                    contentDescription = searchIcon.contentDescription,
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Find a book",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }

        // When the streak is enabled we show the full 21-day grid even with zero days
        // read (all-unlit), inviting a brand-new reader to start a streak; isNotEmpty()
        // only suppresses the brief pre-load window before activity data arrives.
        if (streakEnabled && recentReadingActivity.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))

            StreakStrip(
                activity = recentReadingActivity,
                onClick = onExpandStreak,
            )
        }

        if (pickUpNext.isNotEmpty()) {
            Spacer(modifier = Modifier.height(40.dp))

            PickUpNextSection(
                books = pickUpNext,
                onBookClick = onBookClick,
            )
        } else if (trendingTile != null) {
            Spacer(modifier = Modifier.height(40.dp))

            TrendingTileSection(
                book = trendingTile,
                onBookClick = onBookClick,
            )
        }
    }
}

@Composable
private fun PickUpNextSection(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionLabel(text = "Pick up next")

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                16.dp,
                Alignment.CenterHorizontally,
            ),
        ) {
            books.forEach { book ->
                PickUpNextTile(
                    book = book,
                    onClick = { onBookClick(book) },
                )
            }
        }
    }
}

@Composable
private fun PickUpNextTile(
    book: Book,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(96.dp)
            .pressScale(remember { MutableInteractionSource() }),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.pointerHandCursor(),
        ) {
            EditionImage(
                edition = book.currentEdition,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 2f / 3f),
                isLoading = false,
                defaultEdition = book.defaultEdition,
                fallbackCoverUrl = book.coverUrl,
                coverlessTitle = book.title,
                elevation = 4.dp,
                cornerRadius = 10.dp,
                sharedTransitionKey = bookCoverTransitionKey(
                    editionId = book.currentEdition?.id,
                    bookId = book.id,
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = book.title,
            style = MaterialTheme.editorialTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TrendingTileSection(
    book: Book,
    onBookClick: (Book) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SectionLabel(text = "Trending now")

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            onClick = { onBookClick(book) },
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHandCursor(),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EditionImage(
                    edition = book.currentEdition,
                    modifier = Modifier
                        .width(80.dp)
                        .aspectRatio(ratio = 2f / 3f),
                    isLoading = false,
                    defaultEdition = book.defaultEdition,
                    fallbackCoverUrl = book.coverUrl,
                    coverlessTitle = book.title,
                    elevation = 4.dp,
                    cornerRadius = 8.dp,
                    sharedTransitionKey = bookCoverTransitionKey(
                        editionId = book.currentEdition?.id,
                        bookId = book.id,
                    ),
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "Trending".uppercase(),
                        style = MaterialTheme.editorialTypography.eyebrowSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Text(
                        text = book.title,
                        style = MaterialTheme.editorialTypography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val author = book.authors.firstOrNull()?.name.orEmpty()

                    if (author.isNotBlank()) {
                        Text(
                            text = "By $author",
                            style = MaterialTheme.editorialTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/**
 * The pace-nudge ribbon, fused to the featured hero card's top edge (design-system.md §5 "Pace-nudge
 * ribbon" — formerly an in-flow row above the card). `primaryContainer` fill, an info glyph, the
 * italic message, and a × dismiss; the outer card's own rounded clip gives it matching top corners.
 */
@Composable
internal fun PaceNudgeRibbon(
    text: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val infoIcon = drawableIconResource(
            icon = SoftcoverIcon.Info,
            contentDescription = "",
        )

        Icon(
            painter = infoIcon.getIconPainter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(18.dp),
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = text,
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(22.dp),
        ) {
            val closeIcon = drawableIconResource(
                icon = SoftcoverIcon.Close,
                contentDescription = "Dismiss pace nudge",
            )

            Icon(
                painter = closeIcon.getIconPainter(),
                contentDescription = closeIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

internal fun Book.deadlineProgressFrom(state: ReadingScreenUiState): DeadlineProgress? {
    val deadline = state.deadlines[id] ?: return null
    val edition = currentEdition ?: return null

    val current = when (deadline.unit) {
        DeadlineUnit.PAGES -> userBookRead?.currentPage ?: 0
        DeadlineUnit.SECONDS -> userBookRead?.currentSeconds ?: 0
    }
    val total = when (deadline.unit) {
        DeadlineUnit.PAGES -> edition.pages ?: 0
        DeadlineUnit.SECONDS -> edition.audioSeconds ?: 0
    }

    return DeadlineProgress.compute(
        deadline = deadline,
        current = current,
        total = total,
    )
}

internal fun List<Book>.averageProgress(): Float? {
    if (isEmpty()) return null
    val values = mapNotNull { it.userBookRead?.progress }
    if (values.isEmpty()) return null
    return values.average().toFloat()
}

internal fun greetingForNow(): String {
    val hour = currentLocalDateTime().hour
    return when (hour) {
        in 5..11 -> "Good morning."
        in 12..17 -> "Good afternoon."
        in 18..21 -> "Good evening."
        else -> "Late hours."
    }
}

internal fun buildSubtitle(
    bookCount: Int,
    averageProgress: Float?,
): String {
    val countPart = when (bookCount) {
        1 -> "One title in motion"
        else -> "$bookCount titles in motion"
    }

    val progressPart = averageProgress?.let { "${it.roundToInt()}% along, on average" }

    return listOfNotNull(countPart, progressPart).joinToString(" · ")
}
