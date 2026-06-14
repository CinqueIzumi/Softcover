package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.IndicatorBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import coil3.compose.AsyncImage
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import nl.rhaydus.softcover.core.designsystem.presentation.component.DeadlineCoverOverlay
import nl.rhaydus.softcover.core.designsystem.presentation.component.DeadlineSummaryLine
import nl.rhaydus.softcover.core.designsystem.presentation.component.EditionImage
import nl.rhaydus.softcover.core.designsystem.presentation.component.MarkAsReadBurst
import nl.rhaydus.softcover.core.designsystem.presentation.component.PullToRefreshEyebrow
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.designsystem.presentation.component.SoftcoverSplitButton
import nl.rhaydus.softcover.core.designsystem.presentation.component.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.designsystem.presentation.component.mutationAnimated
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberEditionImageRequest
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberLazyItemMutationAnimator
import nl.rhaydus.softcover.core.designsystem.presentation.component.rememberStaggeredEntryCoordinator
import nl.rhaydus.softcover.core.designsystem.presentation.component.staggeredEntry
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.model.BookInitialCover
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.designsystem.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.designsystem.presentation.model.SoftcoverMenuItem
import nl.rhaydus.softcover.core.designsystem.presentation.model.SplitButtonStyle
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.pressScale
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.quoteGlyphSway
import nl.rhaydus.softcover.core.designsystem.presentation.modifier.shakeOnError
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.AppNavigator
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.LocalBookDetailPresenter
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.ScreenDestination
import nl.rhaydus.softcover.core.designsystem.presentation.navigation.TabDestination
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.LocalBookDetailPrefetcher
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.prefetchBookDetailOnPress
import nl.rhaydus.softcover.core.designsystem.presentation.prefetch.rememberBookDetailPrefetcher
import nl.rhaydus.softcover.core.designsystem.presentation.preview.PreviewData
import nl.rhaydus.softcover.core.designsystem.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.designsystem.presentation.util.BottomBarPulseManager
import nl.rhaydus.softcover.core.designsystem.presentation.util.currentLocalDate
import nl.rhaydus.softcover.core.designsystem.presentation.util.currentLocalDateTime
import nl.rhaydus.softcover.core.designsystem.presentation.util.playDecorativeMotion
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberBottomBarPadding
import nl.rhaydus.softcover.core.designsystem.presentation.util.rememberHaptics
import nl.rhaydus.softcover.core.designsystem.presentation.util.secondsToHm
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.notification.rememberNotificationPermissionRequester
import nl.rhaydus.softcover.feature.reading.presentation.action.DismissProgressSheetAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnDismissPlanTodayAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnShowProgressSheetClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdatePageProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdatePercentageProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdateTimeProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.action.RefreshAction
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStrip
import nl.rhaydus.softcover.feature.reading.presentation.component.StreakStripSheet
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenScreenModel
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import org.koin.compose.koinInject
import kotlin.math.roundToInt

object ReadingScreen : Screen {
    private val booksListState = LazyListState()

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ReadingScreenScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow
        val tabNavigator = LocalTabNavigator.current
        val appNavigator = koinInject<AppNavigator>()
        val bookDetailPresenter = LocalBookDetailPresenter.current

        Screen(
            state = state,
            runAction = screenModel::runAction,
            onBookClick = {
                val destination = ScreenDestination.BookDetail(
                    id = it.id,
                    initialCover = BookInitialCover.fromBook(book = it),
                )

                if (bookDetailPresenter != null) {
                    bookDetailPresenter.open(destination)
                } else {
                    navigator.parent?.push(item = appNavigator.screen(destination))
                }
            },
            onNavigateToSearch = {
                tabNavigator.current = appNavigator.tab(TabDestination.EXPLORE)
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    internal fun Screen(
        state: ReadingScreenUiState,
        runAction: (ReadingAction) -> Unit,
        onBookClick: (Book) -> Unit,
        onNavigateToSearch: () -> Unit,
    ) {
        val pullToRefreshState = rememberPullToRefreshState()

        val haptics = rememberHaptics()

        val playMotion = playDecorativeMotion()

        var celebrationKey by remember { mutableIntStateOf(0) }

        var showStreakSheet by remember { mutableStateOf(false) }

        var slidingBookId by remember { mutableStateOf<Int?>(null) }
        val slideProgress = remember { Animatable(initialValue = 0f) }

        val onMarkAsRead: (Book) -> Unit = { book ->
            if (slidingBookId == null) {
                haptics.commit()
                celebrationKey++
                BottomBarPulseManager.pulseLibrary()

                if (playMotion) {
                    slidingBookId = book.id
                } else {
                    runAction(OnMarkBookAsReadClickAction(book = book))
                }
            }
        }

        LaunchedEffect(slidingBookId) {
            val targetId = slidingBookId ?: return@LaunchedEffect

            slideProgress.snapTo(targetValue = 0f)
            slideProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 320,
                    easing = FastOutLinearInEasing,
                ),
            )

            state.books.firstOrNull { it.id == targetId }?.let { book ->
                runAction(OnMarkBookAsReadClickAction(book = book))
            }

            slideProgress.snapTo(targetValue = 0f)
            slidingBookId = null
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                contentWindowInsets = WindowInsets.statusBars,
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .imePadding(),
                ) {
                    PullToRefreshBox(
                        isRefreshing = state.isLoading,
                        onRefresh = { runAction(RefreshAction) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        state = pullToRefreshState,
                        indicator = {
                            IndicatorBox(
                                modifier = Modifier.align(Alignment.TopCenter),
                                state = pullToRefreshState,
                                isRefreshing = state.isLoading,
                            ) {
                                ContainedLoadingIndicator(modifier = Modifier.align(Alignment.TopCenter))
                            }
                        },
                    ) {
                        when {
                            state.books.isNotEmpty() -> {
                                BooksDisplay(
                                    state = state,
                                    runAction = runAction,
                                    onBookClick = onBookClick,
                                    onMarkAsRead = onMarkAsRead,
                                    onExpandStreak = { showStreakSheet = true },
                                    pullToRefreshState = pullToRefreshState,
                                    slidingBookId = slidingBookId,
                                    slideProgress = slideProgress.value,
                                )
                            }

                            state.isLoading -> Unit
                            else -> EmptyCurrentlyReadingScreen(
                                wantToReadBooks = state.wantToReadBooks,
                                trendingBooks = state.trendingBooks,
                                onBookClick = onBookClick,
                                onNavigateToSearch = onNavigateToSearch,
                            )
                        }
                    }

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
                            onUpdatePercentageClick = { percentage ->
                                if (percentage.toFloatOrNull() == 100f) {
                                    haptics.commit()
                                    celebrationKey++
                                }

                                runAction(OnUpdatePercentageProgressClickAction(percentage))
                            },
                            onUpdatePageProgressClick = { pages ->
                                val total = updatingBook.currentEdition?.pages
                                    ?: updatingBook.defaultEdition?.pages

                                if (total != null && pages.toIntOrNull() == total) {
                                    haptics.commit()
                                    celebrationKey++
                                }

                                runAction(OnUpdatePageProgressClickAction(pages))
                            },
                            onUpdateTimeProgressClick = { h, m, s ->
                                runAction(
                                    OnUpdateTimeProgressClickAction(
                                        h,
                                        m,
                                        s,
                                    ),
                                )
                            },
                        )
                    }

                    if (showStreakSheet) {
                        StreakStripSheet(
                            activity = state.recentReadingActivity,
                            onDismiss = { showStreakSheet = false },
                        )
                    }
                }
            }

            MarkAsReadBurst(
                triggerKey = celebrationKey,
                modifier = Modifier.fillMaxSize(),
                particleCount = 28,
                durationMillis = 1000,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BooksDisplay(
        state: ReadingScreenUiState,
        runAction: (ReadingAction) -> Unit,
        onBookClick: (Book) -> Unit,
        onMarkAsRead: (Book) -> Unit,
        onExpandStreak: () -> Unit,
        pullToRefreshState: PullToRefreshState,
        slidingBookId: Int?,
        slideProgress: Float,
    ) {
        val featured = state.books.first()
        val rest = state.books.drop(1)

        val animator = rememberLazyItemMutationAnimator(keys = rest.map { it.id })

        val entry = rememberStaggeredEntryCoordinator(key = "reading:rest")

        val isInspection = LocalInspectionMode.current
        val prefetcher = if (isInspection) null else rememberBookDetailPrefetcher()

        val density = LocalDensity.current
        val slideDistancePx = remember(density) { with(density) { 96.dp.toPx() } }

        val today = remember { currentLocalDate().toString() }

        val slideModifier: (Int) -> Modifier = { bookId ->
            if (bookId == slidingBookId) {
                Modifier.graphicsLayer {
                    translationY = slideProgress * slideDistancePx
                    alpha = 1f - slideProgress
                }
            } else {
                Modifier
            }
        }

        CompositionLocalProvider(LocalBookDetailPrefetcher provides prefetcher) {
            LazyColumn(
                state = booksListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = rememberBottomBarPadding()),
            ) {
                item(key = "header") {
                    EditorialHeader(
                        bookCount = state.books.size,
                        averageProgress = state.books.averageProgress(),
                        recentReadingActivity = state.recentReadingActivity,
                        onExpandStreak = onExpandStreak,
                        pullToRefreshState = pullToRefreshState,
                        isRefreshing = state.isLoading,
                    )
                }

                val featuredDeadlineProgress = featured.deadlineProgressFrom(state)
                val planTodayMessage = planTodayNudgeFor(progress = featuredDeadlineProgress)
                val isPlanTodayDismissed = state.dismissedPlanTodayByBook[featured.id] == today

                if (planTodayMessage != null && isPlanTodayDismissed.not()) {
                    item(key = "plan-today-${featured.id}") {
                        PlanTodayNudge(
                            text = planTodayMessage,
                            onDismiss = {
                                runAction(OnDismissPlanTodayAction(bookId = featured.id))
                            },
                        )
                    }
                }

                item(key = "featured-${featured.id}") {
                    FeaturedBookCard(
                        book = featured,
                        deadlineProgress = featuredDeadlineProgress,
                        dateStyle = state.dateStyle,
                        mutationFailed = featured.id in state.failedMutationBookIds,
                        runAction = runAction,
                        onBookClick = onBookClick,
                        onMarkAsRead = onMarkAsRead,
                        modifier = slideModifier(featured.id),
                    )
                }

                if (rest.isNotEmpty()) {
                    item(key = "also-reading-label") {
                        Spacer(modifier = Modifier.height(16.dp))

                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            SectionLabel(text = "Also between your fingers")
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
                                .then(slideModifier(book.id)),
                            book = book,
                            deadlineProgress = book.deadlineProgressFrom(state),
                            dateStyle = state.dateStyle,
                            mutationFailed = book.id in state.failedMutationBookIds,
                            runAction = runAction,
                            onBookClick = onBookClick,
                            onMarkAsRead = onMarkAsRead,
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun EditorialHeader(
        bookCount: Int,
        averageProgress: Float?,
        recentReadingActivity: List<ReadingDayActivity>,
        onExpandStreak: () -> Unit,
        pullToRefreshState: PullToRefreshState,
        isRefreshing: Boolean,
    ) {
        val greeting = remember { greetingForNow() }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp),
        ) {
            PullToRefreshEyebrow(
                pullToRefreshState = pullToRefreshState,
                isRefreshing = isRefreshing,
                baseText = "Now reading",
                refreshingText = "Catching up on your reading…",
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = greeting,
                style = MaterialTheme.editorialTypography.headlineMedium.copy(
                    lineHeight = 32.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            val subtitle = buildSubtitle(
                bookCount = bookCount,
                averageProgress = averageProgress,
            )

            Text(
                text = subtitle,
                style = MaterialTheme.editorialTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (recentReadingActivity.any { it.didRead }) {
                Spacer(modifier = Modifier.height(14.dp))

                StreakStrip(
                    activity = recentReadingActivity,
                    onClick = onExpandStreak,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun FeaturedBookCard(
        book: Book,
        deadlineProgress: DeadlineProgress?,
        dateStyle: DateStyle,
        mutationFailed: Boolean,
        runAction: (ReadingAction) -> Unit,
        onBookClick: (Book) -> Unit,
        onMarkAsRead: (Book) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        var dropdownActive by remember { mutableStateOf(false) }
        val shape = RoundedCornerShape(28.dp)
        val progressFraction = (book.userBookRead?.progress ?: 0f) / 100f
        val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
        val isInspection = LocalInspectionMode.current
        val backdropRequest = rememberEditionImageRequest(
            edition = book.currentEdition,
            defaultEdition = book.defaultEdition,
            fallbackCoverUrl = book.coverUrl,
        )

        val interactionSource = remember { MutableInteractionSource() }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .prefetchBookDetailOnPress(book.id)
                .pressScale(interactionSource)
                .shakeOnError(
                    trigger = mutationFailed,
                    onShakeEnd = {
                        runAction(OnClearMutationFailureAction(bookId = book.id))
                    },
                ),
            color = surfaceColor,
            shape = shape,
            onClick = { onBookClick(book) },
            interactionSource = interactionSource,
        ) {
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
                                    0f to surfaceColor.copy(alpha = 0f),
                                    0.40f to surfaceColor.copy(alpha = 0.15f),
                                    0.60f to surfaceColor.copy(alpha = 0.92f),
                                    1f to surfaceColor,
                                ),
                            ),
                        ),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val overlayShadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(
                            x = 0f,
                            y = 1f,
                        ),
                        blurRadius = 14f,
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .height(1.dp)
                                .width(20.dp)
                                .background(Color.White),
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (mutationFailed) {
                                "Couldn't save — tap to retry".uppercase()
                            } else {
                                "Up next".uppercase()
                            },
                            style = MaterialTheme.editorialTypography.eyebrow.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = overlayShadow,
                            ),
                            color = if (mutationFailed) {
                                MaterialTheme.colorScheme.error
                            } else {
                                Color.White
                            },
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    DeadlineCoverOverlay(progress = deadlineProgress) {
                        EditionImage(
                            edition = book.currentEdition,
                            modifier = Modifier
                                .width(150.dp)
                                .aspectRatio(2f / 3f),
                            isLoading = false,
                            defaultEdition = book.defaultEdition,
                            fallbackCoverUrl = book.coverUrl,
                            elevation = 24.dp,
                            cornerRadius = 10.dp,
                            shadowColor = Color.Black.copy(alpha = 0.7f),
                            sharedTransitionKey = bookCoverTransitionKey(
                                editionId = book.currentEdition?.id,
                                bookId = book.id,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    book.seriesText?.takeIf { it.isNotBlank() }?.let { series ->
                        Text(
                            text = series.uppercase(),
                            style = MaterialTheme.editorialTypography.eyebrowSmall.copy(
                                letterSpacing = 1.4.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = book.title,
                        style = MaterialTheme.editorialTypography.headlineSmall.copy(
                            lineHeight = 30.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    book.currentEdition?.authorString?.takeIf { it.isNotBlank() }
                        ?.let { authors ->
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "By $authors",
                                style = MaterialTheme.editorialTypography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                    if (deadlineProgress != null) {
                        Spacer(modifier = Modifier.height(6.dp))

                        DeadlineSummaryLine(
                            progress = deadlineProgress,
                            dateStyle = dateStyle,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ProgressBlock(
                        primaryLabel = progressLabel(book),
                        progressFraction = progressFraction,
                        percentage = book.userBookRead?.progress,
                        emphasized = true,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SoftcoverSplitButton(
                        checked = dropdownActive,
                        dropDownItems = listOf(
                            SoftcoverMenuItem(
                                label = "Mark as Read",
                                onClick = {
                                    dropdownActive = false
                                    onMarkAsRead(book)
                                },
                                icon = SoftcoverIconResource.Drawable(
                                    icon = SoftcoverIcon.CheckCircle,
                                    contentDescription = "Mark as Read icon",
                                ),
                            ),
                        ),
                        label = "Update progress",
                        leadingIcon = SoftcoverIconResource.Drawable(
                            icon = SoftcoverIcon.Edit,
                            contentDescription = "Update progress icon",
                        ),
                        trailingIcon = SoftcoverIconResource.Drawable(
                            icon = SoftcoverIcon.ArrowDropDown,
                            contentDescription = "Drop down icon",
                        ),
                        onDismissMenuRequest = { dropdownActive = false },
                        onLeadingButtonClick = {
                            runAction(OnShowProgressSheetClickAction(book = book))
                        },
                        onTrailingButtonClick = { dropdownActive = it },
                        leadingButtonStyle = SplitButtonStyle.FILLED,
                        size = ButtonSize.M,
                        fillMaxWidth = true,
                    )

                    FeaturedSessionButton(book = book)
                }
            }
        }
    }

    @Composable
    private fun FeaturedSessionButton(book: Book) {
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

                SoftcoverButton(
                    label = "Focus mode",
                    style = ButtonStyle.TONAL,
                    size = ButtonSize.M,
                    icon = SoftcoverIconResource.Drawable(
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

                SoftcoverButton(
                    label = "Start reading session",
                    style = ButtonStyle.TONAL,
                    size = ButtonSize.M,
                    icon = SoftcoverIconResource.Drawable(
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

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun CompactBookEntry(
        book: Book,
        deadlineProgress: DeadlineProgress?,
        dateStyle: DateStyle,
        mutationFailed: Boolean,
        runAction: (ReadingAction) -> Unit,
        onBookClick: (Book) -> Unit,
        onMarkAsRead: (Book) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        var dropdownActive by remember { mutableStateOf(false) }
        val progressFraction = (book.userBookRead?.progress ?: 0f) / 100f

        val interactionSource = remember { MutableInteractionSource() }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 6.dp,
                )
                .prefetchBookDetailOnPress(book.id)
                .pressScale(interactionSource)
                .shakeOnError(
                    trigger = mutationFailed,
                    onShakeEnd = {
                        runAction(OnClearMutationFailureAction(bookId = book.id))
                    },
                ),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp),
            onClick = { onBookClick(book) },
            interactionSource = interactionSource,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                DeadlineCoverOverlay(progress = deadlineProgress) {
                    EditionImage(
                        edition = book.currentEdition,
                        modifier = Modifier
                            .width(80.dp)
                            .aspectRatio(2f / 3f),
                        isLoading = false,
                        defaultEdition = book.defaultEdition,
                        fallbackCoverUrl = book.coverUrl,
                        elevation = 6.dp,
                        cornerRadius = 8.dp,
                        sharedTransitionKey = bookCoverTransitionKey(
                            editionId = book.currentEdition?.id,
                            bookId = book.id,
                        ),
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

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
                        Spacer(modifier = Modifier.height(4.dp))

                        DeadlineSummaryLine(
                            progress = deadlineProgress,
                            dateStyle = dateStyle,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ProgressBlock(
                        primaryLabel = progressLabel(book),
                        progressFraction = progressFraction,
                        percentage = book.userBookRead?.progress,
                        emphasized = false,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SoftcoverSplitButton(
                        checked = dropdownActive,
                        dropDownItems = listOf(
                            SoftcoverMenuItem(
                                label = "Mark as Read",
                                onClick = {
                                    dropdownActive = false
                                    onMarkAsRead(book)
                                },
                                icon = SoftcoverIconResource.Drawable(
                                    icon = SoftcoverIcon.CheckCircle,
                                    contentDescription = "Mark as Read icon",
                                ),
                            ),
                        ),
                        label = "Set progress",
                        trailingIcon = SoftcoverIconResource.Drawable(
                            icon = SoftcoverIcon.ArrowDropDown,
                            contentDescription = "Drop down icon",
                        ),
                        onDismissMenuRequest = { dropdownActive = false },
                        onLeadingButtonClick = {
                            runAction(OnShowProgressSheetClickAction(book = book))
                        },
                        onTrailingButtonClick = { dropdownActive = it },
                        leadingButtonStyle = SplitButtonStyle.TONAL,
                        size = ButtonSize.XS,
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun ProgressBlock(
        primaryLabel: String,
        progressFraction: Float,
        percentage: Float?,
        emphasized: Boolean,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = primaryLabel,
                    style = if (emphasized) {
                        MaterialTheme.editorialTypography.body
                    } else {
                        MaterialTheme.editorialTypography.bodySmall
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${(percentage ?: 0f).roundToInt()}%",
                    style = if (emphasized) {
                        MaterialTheme.editorialTypography.headlineSmall
                    } else {
                        MaterialTheme.editorialTypography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(if (emphasized) 10.dp else 6.dp))

            LinearProgressIndicator(
                progress = {
                    progressFraction.coerceIn(
                        0f,
                        1f,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (emphasized) 10.dp else 6.dp),
                drawStopIndicator = {},
                gapSize = (-2).dp,
            )
        }
    }

    @Composable
    private fun SectionLabel(text: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .height(1.dp)
                    .width(20.dp)
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

    @Composable
    private fun EmptyCurrentlyReadingScreen(
        wantToReadBooks: List<Book> = emptyList(),
        trendingBooks: List<Book> = emptyList(),
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
                .verticalScroll(rememberScrollState()),
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
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val searchIcon = SoftcoverIconResource.Drawable(
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
            ) {
                EditionImage(
                    edition = book.currentEdition,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(ratio = 2f / 3f),
                    isLoading = false,
                    defaultEdition = book.defaultEdition,
                    fallbackCoverUrl = book.coverUrl,
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
                modifier = Modifier.fillMaxWidth(),
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
}

private fun Book.deadlineProgressFrom(state: ReadingScreenUiState): DeadlineProgress? {
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

private fun List<Book>.averageProgress(): Float? {
    if (isEmpty()) return null
    val values = mapNotNull { it.userBookRead?.progress }
    if (values.isEmpty()) return null
    return values.average().toFloat()
}

@Composable
private fun PlanTodayNudge(
    text: String,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.editorialTypography.bodySmall.copy(
                fontStyle = FontStyle.Italic,
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp),
        ) {
            val closeIcon = SoftcoverIconResource.Drawable(
                icon = SoftcoverIcon.Close,
                contentDescription = "Dismiss",
            )

            Icon(
                painter = closeIcon.getIconPainter(),
                contentDescription = closeIcon.contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun greetingForNow(): String {
    val hour = currentLocalDateTime().hour
    return when (hour) {
        in 5..11 -> "Good morning."
        in 12..17 -> "Good afternoon."
        in 18..21 -> "Good evening."
        else -> "Late hours."
    }
}

private fun buildSubtitle(
    bookCount: Int,
    averageProgress: Float?,
): String {
    val countPart = when (bookCount) {
        1 -> "One title in motion"
        else -> "$bookCount titles in motion"
    }

    val progressPart = averageProgress?.let { "${it.roundToInt()}% along, on average" }

    return listOfNotNull(countPart, progressPart).joinToString(" • ")
}

private fun progressLabel(book: Book): String {
    val edition = book.currentEdition
    return if (edition?.isAudiobook == true) {
        val current = book.userBookRead?.currentSeconds ?: 0
        val total = edition.audioSeconds ?: 0
        "${secondsToHm(current)} of ${secondsToHm(total)}"
    } else {
        val currentPage = book.userBookRead?.currentPage ?: 0
        val totalPages = edition?.pages ?: book.defaultEdition?.pages
        "Page $currentPage of $totalPages"
    }
}

private fun previewBook(
    id: Int,
    title: String,
    authorNames: List<String>,
    pages: Int,
    currentPage: Int,
    progress: Float,
    series: BookSeries? = null,
    positionsInSeries: List<Double> = emptyList(),
    audioSeconds: Int? = null,
    currentSeconds: Int? = null,
): Book {
    val editionId = id * 10
    val edition = PreviewData.baseEdition.copy(
        id = editionId,
        pages = pages,
        audioSeconds = audioSeconds,
        authors = authorNames.map { PreviewData.baseAuthor.copy(name = it) },
    )

    return PreviewData.baseBook.copy(
        id = id,
        title = title,
        editions = listOf(edition),
        defaultEdition = edition,
        authors = edition.authors,
        userBookRead = PreviewData.baseBook.userBookRead?.copy(
            currentPage = currentPage,
            currentSeconds = currentSeconds,
            progress = progress,
        ),
        userBook = PreviewData.baseBook.userBook?.copy(editionId = editionId),
        bookSeries = series,
        positionsInSeries = positionsInSeries,
    )
}

private val previewBooks: List<Book> = listOf(
    previewBook(
        id = 1,
        title = "The Dungeon Anarchist's Cookbook",
        authorNames = listOf("Matt Dinniman"),
        pages = 534,
        currentPage = 470,
        progress = 88.014984f,
        series = BookSeries(
            id = 1,
            name = "Dungeon Crawler Carl",
            amountOfBooks = 20,
        ),
    ),
    previewBook(
        id = 2,
        title = "Last to Leave the Room",
        authorNames = listOf("Caitlin Starling"),
        pages = 320,
        currentPage = 262,
        progress = 81.875f,
        series = BookSeries(
            id = 1,
            name = "Dungeon Crawler Carl",
            amountOfBooks = 20,
        ),
        positionsInSeries = listOf(3.0),
    ),
    previewBook(
        id = 3,
        title = "Cursed Bunny",
        authorNames = listOf("Bora Chung", "Anton Hur"),
        pages = 534,
        currentPage = 49,
        progress = 19.140625f,
    ),
    previewBook(
        id = 4,
        title = "Sherlock Holmes: The complete illustrated novels",
        authorNames = listOf("Arthur Conan Doyle"),
        pages = 534,
        currentPage = 200,
        progress = 40.322582f,
    ),
    previewBook(
        id = 5,
        title = "The Complete Fiction",
        authorNames = listOf("H.P. Lovecraft", "S.T. Joshi"),
        pages = 1098,
        currentPage = 110,
        progress = 10.018215f,
    ),
)

@StandardPreview
@Composable
private fun ReadingScreenEmptyPreview() {
    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(isLoading = false),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenEmptyWithPickUpNextPreview() {
    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(
                isLoading = false,
                wantToReadBooks = previewBooks.take(3),
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenEmptyWithTrendingPreview() {
    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(
                isLoading = false,
                trendingBooks = listOf(previewBooks.first()),
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenLoadingPreview() {
    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(isLoading = true),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenSingleBookPreview() {
    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(
                books = previewBooks.take(1),
                isLoading = false,
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenPreview() {
    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(
                books = previewBooks,
                isLoading = false,
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

private fun previewReadingActivity(): List<ReadingDayActivity> {
    val today = currentLocalDate()
    val firstDay = today.minus(
        20,
        DateTimeUnit.DAY,
    )
    val litOffsets = setOf(0, 1, 2, 3, 5, 6, 7, 8, 9, 12, 13, 16, 17, 18, 19, 20)

    return (0..20).map { offset ->
        ReadingDayActivity(
            date = firstDay.plus(
                offset,
                DateTimeUnit.DAY,
            ),
            didRead = offset in litOffsets,
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenWithStreakStripPreview() {
    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(
                books = previewBooks,
                isLoading = false,
                recentReadingActivity = previewReadingActivity(),
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenWithDeadlineAndNudgePreview() {
    val today = currentLocalDate()
    val featured = previewBooks.first()
    val behindBook = previewBooks[2]

    val deadlines = mapOf(
        featured.id to BookDeadline(
            bookId = featured.id,
            deadlineDate = today.plus(
                5,
                DateTimeUnit.DAY,
            ),
            setAt = today.minus(
                10,
                DateTimeUnit.DAY,
            ),
            initialPerDay = 30f,
            unit = DeadlineUnit.PAGES,
        ),
        behindBook.id to BookDeadline(
            bookId = behindBook.id,
            deadlineDate = today.plus(
                3,
                DateTimeUnit.DAY,
            ),
            setAt = today.minus(
                20,
                DateTimeUnit.DAY,
            ),
            initialPerDay = 15f,
            unit = DeadlineUnit.PAGES,
        ),
    )

    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(
                books = previewBooks,
                isLoading = false,
                deadlines = deadlines,
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenExpiredDeadlinePreview() {
    val today = currentLocalDate()
    val featured = previewBooks.first()

    val deadlines = mapOf(
        featured.id to BookDeadline(
            bookId = featured.id,
            deadlineDate = today.minus(
                2,
                DateTimeUnit.DAY,
            ),
            setAt = today.minus(
                30,
                DateTimeUnit.DAY,
            ),
            initialPerDay = 20f,
            unit = DeadlineUnit.PAGES,
        ),
    )

    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(
                books = previewBooks.take(3),
                isLoading = false,
                deadlines = deadlines,
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenMutationFailedPreview() {
    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(
                books = previewBooks,
                isLoading = false,
                failedMutationBookIds = setOf(previewBooks[0].id, previewBooks[2].id),
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenAudiobookPreview() {
    val audioBook = previewBook(
        id = 6,
        title = "Project Hail Mary",
        authorNames = listOf("Andy Weir"),
        pages = 0,
        currentPage = 0,
        progress = 42.5f,
        audioSeconds = 58_000,
        currentSeconds = 24_650,
    ).let {
        val edition = it.editions.first().copy(format = "Audiobook")
        it.copy(
            editions = listOf(edition),
            defaultEdition = edition,
        )
    }

    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(
                books = listOf(audioBook) + previewBooks.take(2),
                isLoading = false,
            ),
            runAction = {},
            onBookClick = {},
            onNavigateToSearch = {},
        )
    }
}

@StandardPreview
@Composable
private fun PlanTodayNudgePreview() {
    SoftcoverTheme {
        PlanTodayNudge(
            text = "Read 24 pages today to stay on pace.",
            onDismiss = {},
        )
    }
}
