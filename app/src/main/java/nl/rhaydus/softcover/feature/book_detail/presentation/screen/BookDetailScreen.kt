package nl.rhaydus.softcover.feature.book_detail.presentation.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.R
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.presentation.component.EditionBottomSheetSelector
import nl.rhaydus.softcover.core.presentation.component.EditionImage
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverImage
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.component.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.modifier.shimmer
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.util.BottomNavigationSpacer
import nl.rhaydus.softcover.core.presentation.util.ObserveAsEvents
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.InitializeBookWithIdAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnDismissProgressSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditionOwnedToggleAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnFabClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsReadingClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnMarkBookAsWantToReadClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnNewEditionSaveClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRemoveBookClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowUpdateProgressSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdatePageProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnUpdatePercentageProgressClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.event.RefreshDetailBookEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailScreenScreenModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import kotlin.math.roundToInt

class BookDetailScreen(
    val id: Int,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenModel: BookDetailScreenScreenModel =
            koinScreenModel<BookDetailScreenScreenModel>()

        val state: BookDetailUiState by screenModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                is RefreshDetailBookEvent -> {
                    screenModel.runAction(
                        action = InitializeBookWithIdAction(id = id)
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            val action = InitializeBookWithIdAction(id = id)

            screenModel.runAction(action)
        }

        Screen(
            state = state,
            runAction = screenModel::runAction,
            onNavigateBack = navigator::pop,
        )
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: BookDetailUiState,
        runAction: (BookDetailAction) -> Unit,
        onNavigateBack: () -> Unit,
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
            contentColor = Color.White
        )

        val backButtonColors = remember(shouldBeExpanded) {
            if (shouldBeExpanded) {
                defaultIconButtonColors
            } else {
                iconButtonColorsWithScrim
            }
        }

        Scaffold(
            contentWindowInsets = WindowInsets(),
            floatingActionButton = {
                FloatingActionButtonMenu(
                    state = state,
                    runAction = runAction,
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                state = lazyListState,
            ) {
                item {
                    CoverImageSection(
                        edition = state.book?.currentEdition,
                        isLoading = state.loadingBookDetails,
                        fallBackEdition = state.book?.defaultEdition
                    )
                }

                generalBookInfoSection(state = state)

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item { ReviewsPagesReleaseDateSection(state = state) }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                item {
                    EditionOwnedSection(
                        state = state,
                        runAction = runAction
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                item {
                    BookStatusWidget(
                        state = state,
                        runAction = runAction,
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }

                item { DescriptionSection(state = state) }

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
                            contentDescription = "Navigate back icon"
                        )
                    }
                },
                scrollBehavior = topAppBarScrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = containerColor,
                )
            )

            if (state.showEditEditionSheet && state.book != null) {
                EditionBottomSheetSelector(
                    bookTitle = state.book.title,
                    currentEdition = state.book.currentEdition,
                    defaultEdition = state.book.defaultEdition,
                    editions = state.editions,
                    isLoading = state.loadingEditions,
                    onDismissRequest = {
                        runAction(OnDismissEditEditionSheetClickAction())
                    },
                    onCancelClick = {
                        runAction(OnDismissEditEditionSheetClickAction())
                    },
                    onConfirmClick = {
                        runAction(OnNewEditionSaveClickAction(edition = it))
                    },
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
                        runAction(
                            OnProgressTabClickAction(tab = it)
                        )
                    },
                    onUpdatePercentageClick = {
                        runAction(
                            OnUpdatePercentageProgressClickAction(newPercentage = it)
                        )
                    },
                    onUpdatePageProgressClick = {
                        runAction(
                            OnUpdatePageProgressClickAction(newPage = it)
                        )
                    },
                )
            }
        }
    }

    @Composable
    private fun EditionOwnedSection(
        state: BookDetailUiState,
        runAction: (BookDetailAction) -> Unit,
    ) {
        val edition = state.book?.currentEdition ?: return

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shimmer(isLoading = state.loadingBookDetails),
            horizontalArrangement = Arrangement.Center,
        ) {
            val leadingIcon: @Composable (() -> Unit)? = when (edition.owned) {
                true -> {
                    {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = "Check icon"
                        )
                    }
                }

                false -> null
            }

            val chipLabel = when (edition.owned) {
                true -> "Owned"
                false -> "Mark as owned"
            }

            FilterChip(
                selected = edition.owned,
                enabled = state.settingEditionOwned.not(),
                onClick = {
                    runAction(OnEditionOwnedToggleAction(edition = edition))
                },
                leadingIcon = leadingIcon,
                label = { Text(text = chipLabel) },
            )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun FloatingActionButtonMenu(
        state: BookDetailUiState,
        runAction: (BookDetailAction) -> Unit,
    ) {
        val userStatus = state.book?.status ?: return

        if (userStatus == BookStatus.None) return

        FloatingActionButtonMenu(
            expanded = state.fabMenuExpanded,
            button = {
                FloatingActionButton(
                    onClick = {
                        runAction(OnFabClickAction())
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit book data",
                    )
                }
            }
        ) {
            if (userStatus != BookStatus.Read) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        runAction(OnMarkBookAsReadClickAction(book = state.book))
                    },
                    text = { Text(text = "Mark as Read") },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_bookmark_check),
                            contentDescription = "Mark book as read icon"
                        )
                    }
                )
            }

            if (userStatus == BookStatus.Reading) {
                FloatingActionButtonMenuItem(
                    onClick = {
                        runAction(OnShowUpdateProgressSheetClickAction())
                    },
                    text = { Text(text = "Update progress") },
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu_book),
                            contentDescription = "Progress icon"
                        )
                    }
                )
            }

            FloatingActionButtonMenuItem(
                onClick = {
                    runAction(OnShowEditEditionSheetClickAction())
                },
                text = { Text(text = "Change edition") },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_library_books),
                        contentDescription = "Edition icon"
                    )
                }
            )

            FloatingActionButtonMenuItem(
                onClick = {
                    runAction(OnRemoveBookClickAction(book = state.book))
                },
                text = { Text(text = "Remove") },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Delete icon"
                    )
                }
            )
        }
    }

    @Composable
    private fun CoverImageSection(
        edition: BookEdition?,
        fallBackEdition: BookEdition?,
        isLoading: Boolean,
    ) {
        val editionUrl = edition?.url ?: fallBackEdition?.url

        val imageHeight = with(LocalDensity.current) {
            (LocalWindowInfo.current.containerSize.height * 0.5f).toDp()
        }

        val cornerRadius = 32.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(IntrinsicSize.Min)
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = cornerRadius,
                        bottomEnd = cornerRadius,
                    )
                )
        ) {
            SoftcoverImage(
                url = editionUrl,
                contentDescription = "Blurred cover edition image",
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .blur(8.dp)
                    .scale(1.8f)
            )

            EditionImage(
                edition = edition,
                defaultEdition = fallBackEdition,
                isLoading = isLoading,
                modifier = Modifier
                    .height(imageHeight * 0.8f)
                    .align(Alignment.Center)
            )
        }
    }

    private fun LazyListScope.generalBookInfoSection(state: BookDetailUiState) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            state.book?.seriesText?.let { seriesText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        tonalElevation = 2.dp,
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text = seriesText,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "${state.book?.title}",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .shimmer(isLoading = state.loadingBookDetails)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = state.book?.currentEdition?.authorString ?: "",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shimmer(isLoading = state.loadingBookDetails),
                textAlign = TextAlign.Center,
            )
        }
    }

    @Composable
    private fun ReviewsPagesReleaseDateSection(state: BookDetailUiState) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp)
                .shimmer(isLoading = state.loadingBookDetails),
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_star_filled),
                    contentDescription = "",
                    tint = Color(0xFFFBBF23),
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${state.book?.rating}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            VerticalDivider()

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu_book),
                    contentDescription = "",
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${state.book?.currentEdition?.pages}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            VerticalDivider()

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_date_range),
                    contentDescription = "",
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${state.book?.releaseYear}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    @Composable
    private fun DescriptionSection(state: BookDetailUiState) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = state.book?.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxSize()
                    .shimmer(isLoading = state.loadingBookDetails)
            )
        }
    }

    @Composable
    private fun BookStatusWidget(
        state: BookDetailUiState,
        runAction: (BookDetailAction) -> Unit,
    ) {
        if (state.loadingBookDetails) return

        val book = state.book ?: return

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            when (book.status) {
                BookStatus.Reading -> ReadingContainer(state = state)
                BookStatus.DidNotFinish -> DidNotFinishContainer(state = state)
                BookStatus.Read -> ReadContainer(state = state)
                BookStatus.Paused -> Unit

                BookStatus.WantToRead -> {
                    WantToReadContainer(
                        state = state,
                        runAction = runAction,
                    )
                }

                BookStatus.None -> {
                    WantToReadButton(
                        runAction = runAction,
                        book = book
                    )
                }
            }
        }
    }

    @Composable
    private fun DidNotFinishContainer(state: BookDetailUiState) {
        val userBook = state.book?.userBook ?: return

        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            ) {
                Text(
                    text = "Did not Finish",
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(4.dp))

                when (val dnfDate = userBook.getDnfDateString(style = state.dateStyle)) {
                    null -> {
                        FallBackDateText(
                            userBook = userBook,
                            dateStyle = state.dateStyle,
                        )
                    }

                    else -> {
                        Text(
                            text = "You've tried reading this book before, but marked it as 'did not finish' on $dnfDate",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ReadContainer(state: BookDetailUiState) {
        val userBook = state.book?.userBook ?: return

        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            ) {
                Text(
                    text = "Read",
                    style = MaterialTheme.typography.titleMedium,
                )

                Spacer(modifier = Modifier.height(4.dp))

                when (val readDate = userBook.getReadDateString(style = state.dateStyle)) {
                    null -> {
                        FallBackDateText(
                            userBook = userBook,
                            dateStyle = state.dateStyle,
                        )
                    }

                    else -> {
                        Text(
                            text = "You've read this book before! The last time you've finished reading this book was on $readDate",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun WantToReadContainer(
        state: BookDetailUiState,
        runAction: (BookDetailAction) -> Unit,
    ) {
        val userBook = state.book?.userBook ?: return

        Column {
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 4.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Text(
                        text = "Want to Read",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val wantToReadDate = userBook.getWantToReadDateString(style = state.dateStyle)

                    when (wantToReadDate) {
                        null -> {
                            FallBackDateText(
                                userBook = userBook,
                                dateStyle = state.dateStyle,
                            )
                        }

                        else -> {
                            Text(
                                text = "You've marked this book as 'want to read' on $wantToReadDate",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MarkAsReadingButton(
                book = state.book,
                markBookAsReading = {
                    runAction(OnMarkBookAsReadingClickAction(book = state.book))
                }
            )
        }
    }

    @Composable
    private fun ReadingContainer(state: BookDetailUiState) {
        if (state.book == null || state.book.userBookRead == null) return

        val progress = state.book.userBookRead.progress ?: 0f
        val pageProgress = state.book.userBookRead.currentPage ?: 0

        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu_book),
                        contentDescription = "Book icon",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Reading progress",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Text(
                        text = "${progress.roundToInt()}%",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    drawStopIndicator = {},
                    gapSize = (-2).dp,
                )

                val amountOfPagesLeft =
                    state.book.currentEdition.pages?.minus(state.book.userBookRead.currentPage ?: 0)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$pageProgress of ${state.book.currentEdition.pages} pages • $amountOfPagesLeft pages left",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    @Composable
    private fun FallBackDateText(
        userBook: UserBook,
        dateStyle: DateStyle,
    ) {
        Text(
            text = "This book has been in your library since ${userBook.getFallbackDateString(style = dateStyle)}.",
            style = MaterialTheme.typography.bodySmall
        )
    }

    @Composable
    private fun MarkAsReadingButton(
        book: Book,
        markBookAsReading: (Book) -> Unit,
    ) {
        SoftcoverButton(
            label = "Start Reading",
            onClick = {
                markBookAsReading(book)
            },
            style = ButtonStyle.FILLED,
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.M,
            icon = SoftcoverIconResource.Drawable(
                id = R.drawable.ic_menu_book,
                contentDescription = "Add to want to read icon"
            )
        )
    }

    @Composable
    private fun WantToReadButton(
        book: Book,
        runAction: (BookDetailAction) -> Unit,
    ) {
        SoftcoverButton(
            label = "Want to Read",
            onClick = {
                runAction(OnMarkBookAsWantToReadClickAction(book = book))
            },
            style = ButtonStyle.FILLED,
            modifier = Modifier.fillMaxWidth(),
            size = ButtonSize.M,
            icon = SoftcoverIconResource.Drawable(
                id = R.drawable.ic_bookmark_add,
                contentDescription = "Add to want to read icon"
            )
        )
    }
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
                positionInSeries = 2,
            )

            BookDetailScreen(
                id = 1,
            ).Screen(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
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

            BookDetailScreen(
                id = 1,
            ).Screen(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
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

            BookDetailScreen(
                id = 1,
            ).Screen(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
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

            BookDetailScreen(
                id = 1,
            ).Screen(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
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

            BookDetailScreen(
                id = 1,
            ).Screen(
                state = BookDetailUiState(
                    book = book,
                    loadingBookDetails = false,
                ),
                runAction = {},
                onNavigateBack = {},
            )
        }
    }
}