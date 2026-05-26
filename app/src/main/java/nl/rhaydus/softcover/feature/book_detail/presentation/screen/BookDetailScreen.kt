package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.core.parameter.parametersOf
import nl.rhaydus.softcover.R
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.presentation.component.DeadlineBadge
import nl.rhaydus.softcover.core.presentation.component.DropCapText
import nl.rhaydus.softcover.core.presentation.component.EditionImage
import nl.rhaydus.softcover.core.presentation.component.MarkAsReadBurst
import nl.rhaydus.softcover.core.presentation.component.SoftcoverImage
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.component.UnreleasedBadge
import nl.rhaydus.softcover.core.presentation.component.UnreleasedBadgeStyle
import nl.rhaydus.softcover.core.presentation.component.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.presentation.modifier.conditional
import nl.rhaydus.softcover.core.presentation.modifier.grayscale
import nl.rhaydus.softcover.core.presentation.modifier.pressScaleClickable
import nl.rhaydus.softcover.core.presentation.modifier.shakeOnError
import nl.rhaydus.softcover.core.presentation.modifier.shimmer
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.theme.displayFontFamily
import nl.rhaydus.softcover.core.presentation.theme.editorialTypography
import nl.rhaydus.softcover.core.presentation.transition.LocalNavAnimatedVisibilityScope
import nl.rhaydus.softcover.core.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.presentation.util.SkeletonCrossfade
import nl.rhaydus.softcover.core.presentation.util.BottomNavigationSpacer
import nl.rhaydus.softcover.core.presentation.util.ObserveAsEvents
import nl.rhaydus.softcover.core.presentation.util.htmlToAnnotatedString
import nl.rhaydus.softcover.core.presentation.util.playDecorativeMotion
import nl.rhaydus.softcover.core.presentation.util.rememberHaptics
import nl.rhaydus.softcover.core.presentation.util.secondsToHm
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.FetchBookReviewsAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.InitializeBookWithIdAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearDeadlineAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearMutationFailureAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDeadlinePickedAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissDeadlinePickerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissChooseListsSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissProgressSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissShareSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditionOwnedToggleAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditionSearchQueryChangeAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadingClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsWantToReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnNewEditionSaveClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenDeadlinePickerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRemoveBookClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRevealReviewSpoilerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShareBookClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowChooseListsSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnToggleListMembershipAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowUpdateProgressSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdatePageProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdatePercentageProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdateTimeProgressClickAction
import nl.rhaydus.softcover.feature.lists.presentation.component.ChooseListsBottomSheet
import nl.rhaydus.softcover.feature.lists.presentation.component.ListMembership
import nl.rhaydus.softcover.feature.book_detail.presentation.component.EditionBottomSheetSelector
import nl.rhaydus.softcover.feature.book_detail.presentation.component.ShareBookBottomSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookMarkedAsReadEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.RefreshDetailBookEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailScreenScreenModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookInitialCover
import nl.rhaydus.softcover.feature.connectivity.presentation.component.OfflineScreenContent
import nl.rhaydus.softcover.feature.connectivity.presentation.component.rememberIsOnline
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineProgress
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineUnit
import nl.rhaydus.softcover.feature.lists.presentation.screen.CreateListScreen
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.roundToInt

private val GoldStar = Color(0xFFFBBF23)

private const val REVIEW_COLLAPSED_LINES = 8

class BookDetailScreen(
    val id: Int,
    @Transient private val initialCover: BookInitialCover? = null,
    private val transitionSurface: String? = null,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel: BookDetailScreenScreenModel =
            koinScreenModel<BookDetailScreenScreenModel> { parametersOf(id, initialCover) }

        val state: BookDetailUiState by screenModel.state.collectAsStateWithLifecycle()

        val haptics = rememberHaptics()

        var celebrationKey by remember { mutableIntStateOf(0) }

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                is RefreshDetailBookEvent -> {
                    screenModel.runAction(
                        action = InitializeBookWithIdAction(id = id)
                    )

                    screenModel.runAction(
                        action = FetchBookReviewsAction(bookId = id)
                    )
                }

                is BookMarkedAsReadEvent -> {
                    haptics.commit()
                    celebrationKey++
                }
            }
        }

        val isOnline = rememberIsOnline()

        Screen(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = navigator::pop,
            onCoverClick = {
                val book = state.book ?: return@Screen

                navigator.push(
                    FullScreenCoverScreen(
                        edition = book.currentEdition,
                        defaultEdition = book.defaultEdition,
                        fallbackCoverUrl = book.coverUrl,
                    )
                )
            },
            onCreateNewListClick = {
                screenModel.runAction(OnDismissChooseListsSheetAction())

                navigator.push(item = CreateListScreen())
            },
            isOnline = isOnline,
            celebrationKey = celebrationKey,
        )
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: BookDetailUiState,
        runAction: (BookDetailAction) -> Unit,
        onNavigateBack: () -> Unit,
        onCoverClick: () -> Unit,
        onCreateNewListClick: () -> Unit,
        isOnline: Boolean,
        celebrationKey: Int = 0,
    ) {
        val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val lazyListState = rememberLazyListState()

        val shouldBeExpanded by remember {
            derivedStateOf { lazyListState.firstVisibleItemIndex >= 1 }
        }

        val title = remember(shouldBeExpanded) {
            if (shouldBeExpanded.not()) {
                ""
            } else {
                state.book?.title ?: ""
            }
        }

        val containerColor by animateColorAsState(
            if (shouldBeExpanded.not()) {
                Color.Transparent
            } else {
                Color.Unspecified
            }
        )

        val defaultIconButtonColors = IconButtonDefaults.iconButtonColors()
        val iconButtonColorsWithScrim = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.35f),
            contentColor = Color.White,
        )

        val backButtonColors = remember(shouldBeExpanded) {
            if (shouldBeExpanded) {
                defaultIconButtonColors
            } else {
                iconButtonColorsWithScrim
            }
        }

        val showOfflinePlaceholder =
            isOnline.not() && state.book == null && state.loadingBookDetails.not()

        Scaffold(
            contentWindowInsets = WindowInsets(),
        ) { innerPadding ->
            if (showOfflinePlaceholder) {
                OfflineScreenContent(modifier = Modifier.padding(innerPadding))

                SoftcoverTopBar(
                    title = "",
                    onNavigateBack = onNavigateBack,
                    navigateBackButton = {
                        IconButton(
                            onClick = onNavigateBack,
                            colors = backButtonColors,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = "Navigate back icon",
                            )
                        }
                    },
                    scrollBehavior = topAppBarScrollBehavior,
                )

                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                state = lazyListState,
            ) {
                item {
                    val currentEditionId = state.book?.currentEdition?.id

                    val editionMutationFailed =
                        currentEditionId != null && currentEditionId in state.failedMutationEditionIds

                    Box(
                        modifier = Modifier.shakeOnError(
                            trigger = editionMutationFailed,
                            onShakeEnd = {
                                if (currentEditionId != null) {
                                    runAction(
                                        OnClearMutationFailureAction(editionId = currentEditionId)
                                    )
                                }
                            },
                        ),
                    ) {
                        GeneralBookInfoSection(
                            edition = state.book?.currentEdition ?: state.initialCover?.currentEdition,
                            isLoading = state.loadingBookDetails && state.book == null,
                            fallBackEdition = state.book?.defaultEdition ?: state.initialCover?.defaultEdition,
                            fallbackCoverUrl = state.book?.coverUrl ?: state.initialCover?.fallbackCoverUrl,
                            isExpired = state.deadlineProgress?.isExpired == true,
                            rating = state.book?.rating,
                            title = state.book?.title,
                            seriesText = state.book?.seriesText,
                            releaseYear = state.book?.currentEdition?.releaseYear.takeIf { it != -1 }
                                ?: state.book?.releaseYear,
                            unreleasedDate = state.book?.takeIf { it.isUnreleased }?.effectiveReleaseDate,
                            onCoverClick = onCoverClick,
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }

                item {
                    ShelfActionBar(
                        state = state,
                        runAction = runAction,
                        celebrationKey = celebrationKey,
                    )
                }

                val shelfPanelStatus = state.book?.status
                val shelfPanelWillRender = shelfPanelStatus == BookStatus.Reading ||
                    shelfPanelStatus == BookStatus.DidNotFinish

                if (shelfPanelWillRender) {
                    item { Spacer(modifier = Modifier.height(20.dp)) }

                    item {
                        ShelfStatusPanel(
                            state = state,
                            runAction = runAction,
                        )
                    }

                    item { Spacer(modifier = Modifier.height(28.dp)) }
                } else {
                    item { Spacer(modifier = Modifier.height(28.dp)) }
                }

                item { AboutSection(state = state) }

                item { EditionMetadataStrip(state = state) }

                item {
                    BelowDescriptionStatusPanel(
                        state = state,
                        topSpacing = 28.dp,
                    )
                }

                item { Spacer(modifier = Modifier.height(36.dp)) }

                item {
                    ReviewsSection(
                        state = state,
                        runAction = runAction,
                        dateStyle = state.dateStyle,
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                item { BottomNavigationSpacer() }
            }

            SoftcoverTopBar(
                title = title,
                onNavigateBack = onNavigateBack,
                navigateBackButton = {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = backButtonColors,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Navigate back icon",
                        )
                    }
                },
                additionalActions = {
                    BookOverflowMenu(
                        state = state,
                        runAction = runAction,
                        isOnline = isOnline,
                        iconColors = backButtonColors,
                    )
                },
                scrollBehavior = topAppBarScrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = containerColor,
                ),
            )

            if (state.isShareSheetVisible && state.book != null) {
                ShareBookBottomSheet(
                    book = state.book,
                    onDismissRequest = { runAction(OnDismissShareSheetAction()) },
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
                            )
                        )
                    },
                    onCreateNewListClick = onCreateNewListClick,
                )
            }

            val currentEditionForSheet = state.book?.currentEdition
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
                            )
                        )
                    },
                )
            }
        }
    }

    // region Hero

    @Composable
    private fun GeneralBookInfoSection(
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
                            bookId = id,
                            surface = transitionSurface,
                        ),
                    )

                    val navScope = LocalNavAnimatedVisibilityScope.current

                    val enterSettled = navScope == null ||
                        navScope.transition.currentState == EnterExitState.Visible

                    if (edition?.owned == true) {
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
                    offset = Offset(x = 0f, y = 1f),
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
                                        offset = Offset(x = 0f, y = 1f),
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
                                                    text = "%.1f".format(rating),
                                                    style = bodySmall,
                                                )

                                                Spacer(modifier = Modifier.width(4.dp))

                                                Icon(
                                                    painter = painterResource(R.drawable.ic_star_filled),
                                                    contentDescription = "",
                                                    tint = GoldStar,
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
                                                    secondsToHm(seconds = edition?.audioSeconds ?: 0)
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
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
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
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = "Owned",
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
    private fun ShelfActionBar(
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
                                    iconRes = R.drawable.ic_bookmark_add,
                                    selected = status == BookStatus.WantToRead,
                                    onClick = {
                                        haptics.select()
                                        runAction(OnMarkBookAsWantToReadClickAction(book = book))
                                    },
                                    modifier = Modifier.weight(1f),
                                )

                                ShelfChip(
                                    label = "Reading",
                                    iconRes = R.drawable.ic_reading,
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
                                    iconRes = R.drawable.ic_bookmark_check,
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
    private fun ShelfChip(
        label: String,
        iconRes: Int,
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
                    painter = painterResource(iconRes),
                    contentDescription = null,
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
    private fun BookOverflowMenu(
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
            IconButton(
                onClick = { menuOpen = true },
                colors = iconColors,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = "More actions",
                )
            }

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = dismiss,
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Share") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = null,
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
                            Icon(
                                painter = painterResource(R.drawable.ic_bookmark_add),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            dismiss()
                            runAction(OnShowChooseListsSheetAction())
                        },
                    )
                }

                if (isOnShelf.not()) return@DropdownMenu

                if (isOnline) {
                    DropdownMenuItem(
                        text = { Text(text = "Change edition") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_library_books),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            dismiss()
                            runAction(OnShowEditEditionSheetClickAction())
                        },
                    )

                    book.currentEdition?.let { currentEdition ->
                        val ownedLabel =
                            if (currentEdition.owned) "Unmark as owned" else "Mark as owned"
                        val ownedIconId =
                            if (currentEdition.owned) R.drawable.ic_close else R.drawable.ic_check

                        DropdownMenuItem(
                            text = { Text(text = ownedLabel) },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(ownedIconId),
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                dismiss()
                                runAction(OnEditionOwnedToggleAction(edition = currentEdition))
                            },
                        )
                    }
                }

                if (state.deadline == null) {
                    DropdownMenuItem(
                        text = { Text(text = "Set deadline") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_date_range),
                                contentDescription = null,
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
                            Icon(
                                painter = painterResource(R.drawable.ic_date_range),
                                contentDescription = null,
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
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = null,
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
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = null,
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
    private fun ShelfStatusPanel(
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
    private fun BelowDescriptionStatusPanel(
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
                        Icon(
                            painter = painterResource(
                                if (isAudiobook) R.drawable.ic_headset else R.drawable.ic_menu_book
                            ),
                            contentDescription = "Progress icon",
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
                            Icon(
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = "Update progress",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (progress / 100f).coerceIn(0f, 1f) },
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
                                remainingSeconds
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
                    Icon(
                        painter = painterResource(R.drawable.ic_date_range),
                        contentDescription = "Deadline icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Finish by ${progress.deadline.format(dateStyle.formatter)}",
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
            iconRes = R.drawable.ic_bookmark_check,
            body = when (
                val readDate = userBook.getReadDateString(
                    style = state.dateStyle,
                    finishedAt = book.userBookRead?.finishedAt,
                )
            ) {
                null -> "This book has been in your library since ${
                    userBook.getFallbackDateString(
                        style = state.dateStyle
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
            iconRes = R.drawable.ic_bookmark,
            body = when (val dnfDate = userBook.getDnfDateString(style = state.dateStyle)) {
                null -> "This book has been in your library since ${
                    userBook.getFallbackDateString(
                        style = state.dateStyle
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
            iconRes = R.drawable.ic_bookmark_add,
            body = "On your shelf since ${userBook.getFallbackDateString(style = state.dateStyle)}. Ready when you are.",
        )
    }

    @Composable
    private fun StatusCallout(
        eyebrow: String,
        iconRes: Int,
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
                        painter = painterResource(iconRes),
                        contentDescription = null,
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

    @Composable
    private fun AboutSection(state: BookDetailUiState) {
        val description = state.book?.description.orEmpty()

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            SectionLabel(text = "About")

            Spacer(modifier = Modifier.height(12.dp))

            SkeletonCrossfade(
                isLoading = state.loadingBookDetails && description.isBlank(),
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
                } else if (description.isBlank()) {
                    Text(
                        text = "No description for this book yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    DropCapText(
                        text = htmlToAnnotatedString(html = description),
                        bodyStyle = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 26.sp,
                        ),
                        bodyColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    @Composable
    private fun EditionMetadataStrip(state: BookDetailUiState) {
        val edition = state.book?.currentEdition ?: return

        val publisher = edition.publisher?.takeIf { it.isNotBlank() }
        val isbn = edition.isbn10?.takeIf { it.isNotBlank() }

        if (publisher == null && isbn == null) return

        val parts = listOfNotNull(
            publisher,
            isbn?.let { "ISBN $it" },
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

    // endregion

    // region Reviews

    @Composable
    private fun ReviewsSection(
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
                        Icon(
                            painter = painterResource(R.drawable.ic_star_filled),
                            contentDescription = null,
                            tint = GoldStar,
                            modifier = Modifier.size(18.dp),
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "%.1f".format(book.rating),
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

                        Text(
                            text = htmlToAnnotatedString(html = review.review),
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
                        SoftcoverImage(
                            model = review.reviewer.avatarUrl,
                            contentDescription = "Reviewer avatar",
                            isLoading = false,
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
                                    Icon(
                                        painter = painterResource(R.drawable.ic_star_filled),
                                        contentDescription = "Rating",
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(14.dp),
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = "%.1f".format(rating),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontFamily = displayFontFamily,
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
            (initialDate ?: LocalDate.now())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            val picked = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

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
}

@StandardPreview
@Composable
private fun BookDetailScreenReadingPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(status = BookStatus.Reading),
                userBookRead = PreviewData.baseBook.userBookRead?.copy(
                    currentPage = 20,
                    progress = 0.8f,
                ),
                bookSeries = BookSeries(
                    id = 1,
                    name = "The Maze Runner",
                    amountOfBooks = 3,
                ),
                positionsInSeries = listOf(2.0),
            )

            BookDetailScreen(id = 1).Screen(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = true,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
            )
        }
    }
}

@StandardPreview
@Composable
private fun BookDetailScreenNonePreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(status = BookStatus.None),
            )

            BookDetailScreen(id = 1).Screen(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
            )
        }
    }
}

@StandardPreview
@Composable
private fun BookDetailScreenDnfPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(status = BookStatus.DidNotFinish),
            )

            BookDetailScreen(id = 1).Screen(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
            )
        }
    }
}

@StandardPreview
@Composable
private fun BookDetailScreenWantToReadPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(status = BookStatus.WantToRead),
            )

            BookDetailScreen(id = 1).Screen(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
            )
        }
    }
}

@StandardPreview
@Composable
private fun BookDetailScreenReadPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(
                userBook = PreviewData.baseBook.userBook?.copy(status = BookStatus.Read),
            )

            BookDetailScreen(id = 1).Screen(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
                onCoverClick = {},
                onCreateNewListClick = {},
                isOnline = true,
            )
        }
    }
}
