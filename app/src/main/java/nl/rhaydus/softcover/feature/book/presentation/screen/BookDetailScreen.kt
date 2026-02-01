package nl.rhaydus.softcover.feature.book.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.presentation.component.EditionBottomSheetSelector
import nl.rhaydus.softcover.core.presentation.component.EditionImage
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.component.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.modifier.shimmer
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.core.presentation.util.ObserveAsEvents
import nl.rhaydus.softcover.feature.book.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book.presentation.action.InitializeBookWithIdAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnDismissEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnDismissProgressSheetAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnFabClickAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnMarkBookAsReadingClickAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnMarkBookAsWantToReadClickAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnNewEditionSaveClickAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnRemoveBookClickAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnShowEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnShowUpdateProgressSheetClickAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnUpdatePageProgressClickAction
import nl.rhaydus.softcover.feature.book.presentation.action.OnUpdatePercentageProgressClickAction
import nl.rhaydus.softcover.feature.book.presentation.event.RefreshDetailBookEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.viewmodel.BookDetailScreenViewModel
import kotlin.math.roundToInt

class BookDetailScreen(
    val id: Int,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val viewModel: BookDetailScreenViewModel = koinScreenModel<BookDetailScreenViewModel>()

        val state: BookDetailUiState by viewModel.state.collectAsStateWithLifecycle()

        ObserveAsEvents(flow = viewModel.events) {
            when (it) {
                is RefreshDetailBookEvent -> {
                    viewModel.runAction(action = InitializeBookWithIdAction(id = id))
                }
            }
        }

        LaunchedEffect(Unit) {
            val action = InitializeBookWithIdAction(id = id)

            viewModel.runAction(action)
        }

        Screen(
            navigateUp = navigator::pop,
            runAction = viewModel::runAction,
            state = state,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun Screen(
        state: BookDetailUiState,
        runAction: (BookDetailAction) -> Unit,
        navigateUp: () -> Unit,
    ) {
        Scaffold(
            topBar = {
                SoftcoverTopBar(
                    title = "Book Details",
                    navigateUp = navigateUp,
                )
            },
            floatingActionButton = {
                val userStatus = state.book?.userStatus ?: return@Scaffold

                FloatingActionButtonMenu(
                    expanded = state.fabMenuExpanded,
                    button = {
                        FloatingActionButton(
                            onClick = {
                                runAction(OnFabClickAction())
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit book data",
                            )
                        }
                    }
                ) {
                    if (userStatus == BookStatus.Reading) {
                        FloatingActionButtonMenuItem(
                            onClick = {
                                runAction(OnShowEditEditionSheetClickAction())
                            },
                            text = { Text(text = "Change edition") },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.LibraryBooks,
                                    contentDescription = "Edition icon"
                                )
                            }
                        )

                        FloatingActionButtonMenuItem(
                            onClick = {
                                runAction(OnShowUpdateProgressSheetClickAction())
                            },
                            text = { Text(text = "Update progress") },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.MenuBook,
                                    contentDescription = "Progress icon"
                                )
                            }
                        )
                    }

                    FloatingActionButtonMenuItem(
                        onClick = {
                            runAction(OnRemoveBookClickAction(book = state.book))
                        },
                        text = { Text(text = "Remove") },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete icon"
                            )
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    EditionImage(
                        edition = state.book?.currentEdition,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .shimmer(
                                shape = RoundedCornerShape(4.dp),
                                isLoading = state.loading,
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.book?.title ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .shimmer(isLoading = state.loading)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = state.book?.currentEdition?.authorString ?: "",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .shimmer(isLoading = state.loading)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shimmer(isLoading = state.loading),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "",
                            tint = Color(0xFFFBBF23),
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "Average rating: ${state.book?.rating}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                BookStatusWidget(
                    state = state,
                    runAction = runAction,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    HighlightedInfoWidget(
                        title = "RELEASE DATE",
                        subtitle = "${state.book?.releaseYear}",
                        loading = state.loading,
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    HighlightedInfoWidget(
                        title = "PAGES",
                        subtitle = "${state.book?.currentEdition?.pages}",
                        loading = state.loading,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                        .shimmer(isLoading = state.loading)
                )
            }

            if (state.showEditEditionSheet && state.book != null) {
                EditionBottomSheetSelector(
                    book = state.book,
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
    fun ColumnScope.BookStatusWidget(
        state: BookDetailUiState,
        runAction: (BookDetailAction) -> Unit,
    ) {
        if (state.loading) return

        val book = state.book ?: return

        when (book.userStatus) {
            BookStatus.Reading -> ReadingContainer(state = state)

            BookStatus.None -> {
                WantToReadButton(
                    runAction = runAction,
                    book = book
                )
            }

            else -> MarkAsReadingButton(
                book = book,
                markBookAsReading = {
                    runAction(OnMarkBookAsReadingClickAction(book = book))
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    @Composable
    fun ReadingContainer(state: BookDetailUiState) {
        if (state.book == null || state.book.progress == null) return

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
                        imageVector = Icons.AutoMirrored.Default.MenuBook,
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
                        text = "${state.book.progress.roundToInt()}%",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { state.book.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    drawStopIndicator = {},
                    gapSize = (-2).dp,
                )

                val amountOfPagesLeft =
                    state.book.currentEdition.pages?.minus(state.book.currentPage ?: 0)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${state.book.currentPage} of ${state.book.currentEdition.pages} pages • $amountOfPagesLeft pages left",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    @Composable
    fun MarkAsReadingButton(
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
            icon = SoftcoverIconResource.Vector(
                vector = Icons.AutoMirrored.Default.MenuBook,
                contentDescription = "Add to want to read icon"
            )
        )
    }

    @Composable
    fun WantToReadButton(
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
            icon = SoftcoverIconResource.Vector(
                vector = Icons.Default.BookmarkAdd,
                contentDescription = "Add to want to read icon"
            )
        )
    }

    @Composable
    private fun RowScope.HighlightedInfoWidget(
        title: String,
        loading: Boolean,
        subtitle: String,
    ) {
        val widgetShape = RoundedCornerShape(16.dp)

        Surface(
            modifier = Modifier
                .shimmer(isLoading = loading, shape = widgetShape)
                .fillMaxWidth()
                .weight(1f),
            tonalElevation = 1.dp,
            shape = widgetShape,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(all = 16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
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
                userStatus = BookStatus.Reading,
                progress = 0.8f,
                currentPage = 20,
            )

            BookDetailScreen(
                id = 1,
            ).Screen(
                state = BookDetailUiState(
                    book = book,
                    loading = false,
                ),
                navigateUp = {},
                runAction = {},
            )
        }
    }
}

@StandardPreview
@Composable
private fun BookDetailScreenIgnoredPreview() {
    SoftcoverTheme {
        Column(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background),
        ) {
            val book = PreviewData.baseBook.copy(userStatus = BookStatus.None)

            BookDetailScreen(
                id = 1,
            ).Screen(
                state = BookDetailUiState(
                    book = book,
                    loading = false,
                ),
                navigateUp = {},
                runAction = {},
            )
        }
    }
}