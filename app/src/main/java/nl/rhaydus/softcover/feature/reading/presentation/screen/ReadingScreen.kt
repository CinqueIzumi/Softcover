package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.component.EditionBottomSheetSelector
import nl.rhaydus.softcover.core.presentation.component.EditionImage
import nl.rhaydus.softcover.core.presentation.component.SoftcoverSplitButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.component.UpdateProgressBottomSheet
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.model.SoftcoverMenuItem
import nl.rhaydus.softcover.core.presentation.model.SplitButtonStyle
import nl.rhaydus.softcover.core.presentation.screen.LocalBottomBarPadding
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.book.presentation.screen.BookDetailScreen
import nl.rhaydus.softcover.feature.reading.presentation.action.DismissEditionSheetAction
import nl.rhaydus.softcover.feature.reading.presentation.action.DismissProgressSheetAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnMarkBookAsReadClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnNewEditionSaveClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnProgressTabClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnShowEditionSheetClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnShowProgressSheetClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdatePageProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.OnUpdatePercentageProgressClickAction
import nl.rhaydus.softcover.feature.reading.presentation.action.ReadingAction
import nl.rhaydus.softcover.feature.reading.presentation.action.RefreshAction
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenViewModel
import kotlin.math.roundToInt

object ReadingScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<ReadingScreenViewModel>()

        val state by viewModel.state.collectAsStateWithLifecycle()

        val navigator = LocalNavigator.currentOrThrow

        Screen(
            state = state,
            runAction = viewModel::runAction,
            onBookClick = {
                navigator.parent?.push(item = BookDetailScreen(id = it.id))
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        state: ReadingScreenUiState,
        runAction: (ReadingAction) -> Unit,
        onBookClick: (Book) -> Unit,
    ) {
        Scaffold(
            topBar = {
                SoftcoverTopBar(title = "Currently Reading")
            },
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
                ) {
                    when {
                        state.books.isNotEmpty() -> {
                            BooksDisplay(
                                books = state.books,
                                runAction = runAction,
                                onBookClick = onBookClick,
                            )
                        }

                        state.isLoading -> Unit // Show nothing if the books are still loading
                        else -> EmptyCurrentlyReadingScreen()
                    }
                }

                if (state.bookToUpdate != null && state.showProgressSheet) {
                    UpdateProgressBottomSheet(
                        bookToUpdate = state.bookToUpdate,
                        selectedTab = state.progressSheetTab,
                        onDismissRequest = {
                            runAction(DismissProgressSheetAction)
                        },
                        onProgressTabClick = {
                            runAction(OnProgressTabClickAction(it))
                        },
                        onUpdatePercentageClick = {
                            runAction(
                                OnUpdatePercentageProgressClickAction(it)
                            )
                        },
                        onUpdatePageProgressClick = {
                            runAction(OnUpdatePageProgressClickAction(it))
                        },
                    )
                }

                if (state.bookToUpdate != null && state.showEditionSheet) {
                    EditionBottomSheetSelector(
                        onDismissRequest = { runAction(DismissEditionSheetAction) },
                        onCancelClick = { runAction(DismissEditionSheetAction) },
                        onConfirmClick = { runAction(OnNewEditionSaveClickAction(edition = it)) },
                        book = state.bookToUpdate,
                    )
                }
            }
        }
    }

    @Composable
    private fun BooksDisplay(
        books: List<Book>,
        runAction: (ReadingAction) -> Unit,
        onBookClick: (Book) -> Unit,
    ) {
        val bottomPadding = LocalBottomBarPadding.current

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = bottomPadding)
        ) {
            items(books) {
                BookEntry(
                    book = it,
                    runAction = runAction,
                    onBookClick = onBookClick
                )
            }
        }
    }

    @Composable
    private fun EmptyCurrentlyReadingScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // Required for the scroll to refresh
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(all = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.MenuBook,
                    contentDescription = "Book icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No books yet",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "You haven't added any books to your 'Currently Reading' list. Start by adding new books to your list.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun BookEntry(
        book: Book,
        runAction: (ReadingAction) -> Unit,
        onBookClick: (Book) -> Unit,
    ) {
        var updateProgressSplitButtonActive by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                    onBookClick(book)
                }
                .padding(all = 16.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EditionImage(
                        edition = book.currentEdition,
                        modifier = Modifier.width(100.dp),
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = book.currentEdition.authorString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Page ${book.currentPage} of ${book.currentEdition.pages}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SoftcoverSplitButton(
                            checked = updateProgressSplitButtonActive,
                            dropDownItems = listOf(
                                SoftcoverMenuItem(
                                    label = "Mark as Read",
                                    onClick = {
                                        updateProgressSplitButtonActive = false

                                        runAction(OnMarkBookAsReadClickAction(book = book))
                                    },
                                    icon = SoftcoverIconResource.Vector(
                                        vector = Icons.Default.CheckCircle,
                                        contentDescription = "Mark as Read icon"
                                    )
                                ),
                                SoftcoverMenuItem(
                                    label = "Switch Edition",
                                    onClick = {
                                        updateProgressSplitButtonActive = false

                                        runAction(OnShowEditionSheetClickAction(book = book))
                                    },
                                    icon = SoftcoverIconResource.Vector(
                                        vector = Icons.AutoMirrored.Default.LibraryBooks,
                                        contentDescription = "Mark as Read icon"
                                    )
                                ),
                            ),
                            label = "Set Progress",
                            trailingIcon = SoftcoverIconResource.Vector(
                                vector = Icons.Default.ArrowDropDown,
                                contentDescription = "Drop down icon",
                            ),
                            onDismissMenuRequest = {
                                updateProgressSplitButtonActive = false
                            },
                            onLeadingButtonClick = {
                                runAction(OnShowProgressSheetClickAction(book = book))
                            },
                            onTrailingButtonClick = {
                                updateProgressSplitButtonActive = it
                            },
                            leadingButtonStyle = SplitButtonStyle.OUTLINED,
                            size = ButtonSize.XS,
                        )

                        book.progress?.let { bookProgress ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val progress = bookProgress / 100f

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.weight(1f),
                                    drawStopIndicator = {},
                                    gapSize = (-2).dp,
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "${bookProgress.roundToInt()}%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@StandardPreview
@Composable
private fun ReadingScreenEmptyPreview() {
    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(isLoading = false),
            runAction = {},
            onBookClick = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenPreview() {
    val books = listOf(
        PreviewData.baseBook.copy(
            title = "The Dungeon Anarchist's Cookbook",
            editions = listOf(
                PreviewData.baseEdition.copy(
                    pages = 534,
                    id = 20,
                    authors = listOf(
                        PreviewData.baseAuthor.copy(name = "Matt Dinniman")
                    )
                )
            ),
            currentPage = 470,
            progress = 88.014984f,
            userEditionId = 20,
        ),
        PreviewData.baseBook.copy(
            title = "Last to Leave the Room",
            editions = listOf(
                PreviewData.baseEdition.copy(
                    pages = 320,
                    id = 20,
                )
            ),
            currentPage = 262,
            progress = 81.875f,
            userEditionId = 20,
        ),
        PreviewData.baseBook.copy(
            title = "Cursed Bunny",
            editions = listOf(
                PreviewData.baseEdition.copy(
                    pages = 534,
                    id = 20,
                    authors = listOf(
                        PreviewData.baseAuthor.copy(name = "Bora Chung"),
                        PreviewData.baseAuthor.copy(name = "Anton Hur"),
                    )
                )
            ),
            currentPage = 49,
            progress = 19.140625f,
            userEditionId = 20,
        ),
        PreviewData.baseBook.copy(
            title = "Sherlock Holmes: The complete illustrated novels",
            editions = listOf(
                PreviewData.baseEdition.copy(
                    pages = 534,
                    id = 20,
                    authors = listOf(
                        PreviewData.baseAuthor.copy(name = "Arthur Conan Doyle")
                    )
                )
            ),
            currentPage = 200,
            progress = 40.322582f,
            userEditionId = 20,
        ),
        PreviewData.baseBook.copy(
            title = "The Complete Fiction",
            editions = listOf(
                PreviewData.baseEdition.copy(
                    pages = 1098,
                    id = 20,
                    authors = listOf(
                        PreviewData.baseAuthor.copy(name = "H.P. Lovecraft"),
                        PreviewData.baseAuthor.copy(name = "S.T. Joshi"),
                    )
                )
            ),
            currentPage = 110,
            progress = 10.018215f,
            userEditionId = 20,
        ),
    )

    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(books = books),
            runAction = {},
            onBookClick = {},
        )
    }
}