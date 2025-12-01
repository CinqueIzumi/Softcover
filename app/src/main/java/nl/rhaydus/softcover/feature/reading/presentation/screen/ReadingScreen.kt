package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getViewModel
import coil.compose.SubcomposeAsyncImage
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverSplitButton
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.model.SoftcoverMenuItem
import nl.rhaydus.softcover.core.presentation.model.SplitButtonStyle
import nl.rhaydus.softcover.core.presentation.modifier.conditional
import nl.rhaydus.softcover.core.presentation.modifier.shimmer
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenUiEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ProgressTab
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenViewModel
import kotlin.math.min
import kotlin.math.roundToInt

object ReadingScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getViewModel<ReadingScreenViewModel>()

        val state by viewModel.uiState.collectAsStateWithLifecycle()

        Screen(
            state = state,
            onEvent = viewModel::onEvent,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    fun Screen(
        state: ReadingScreenUiState,
        onEvent: (ReadingScreenUiEvent) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Currently Reading",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = { onEvent(ReadingScreenUiEvent.Refresh) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when {
                    state.books.isNotEmpty() -> {
                        BooksDisplay(
                            books = state.books,
                            onEvent = onEvent
                        )
                    }

                    state.isLoading -> Unit // Show nothing if the books are still loading
                    else -> EmptyCurrentlyReadingScreen()
                }
            }

            if (state.bookToUpdate != null) {
                ModalBottomSheet(
                    onDismissRequest = { onEvent(ReadingScreenUiEvent.DismissProgressSheet) },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    ProgressBottomSheetContent(
                        bookWithProgress = state.bookToUpdate,
                        progressTab = state.progressTab,
                        onEvent = onEvent
                    )
                }
            }
        }
    }

    @Composable
    fun ProgressBottomSheetContent(
        progressTab: ProgressTab,
        bookWithProgress: BookWithProgress,
        onEvent: (ReadingScreenUiEvent) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Update progress",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = bookWithProgress.book.title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(all = 4.dp)
            ) {
                ProgressTab.entries.forEach { tab ->
                    val isSelected = tab == progressTab

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onEvent(ReadingScreenUiEvent.OnProgressTabClick(newProgressTab = tab))
                            }
                            .conditional(
                                condition = isSelected,
                                ifTrue = {
                                    Modifier.background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp),
                                    )
                                }
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = tab.tabName,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (progressTab) {
                ProgressTab.PAGE -> {
                    ProgressBottomSheetPageContent(
                        bookWithProgress = bookWithProgress,
                        onEvent = onEvent,
                    )
                }

                ProgressTab.PERCENTAGE -> {
                    ProgressBottomSheetPercentageContent(
                        bookWithProgress = bookWithProgress,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }

    @Composable
    fun ColumnScope.ProgressBottomSheetPageContent(
        bookWithProgress: BookWithProgress,
        onEvent: (ReadingScreenUiEvent) -> Unit,
    ) {
        var number by remember { mutableStateOf("${bookWithProgress.currentPage}") }
        val density = LocalDensity.current

        val textFieldTextStyle = MaterialTheme.typography.bodyLarge

        val textFieldWidth = remember(density) {
            with(density) {
                val fontSizeInPx = textFieldTextStyle.fontSize.toPx()
                val charCount = 4
                val padding = 32.dp.toPx()

                ((charCount * fontSizeInPx * 0.6f) + padding).toDp()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            OutlinedTextField(
                value = number,
                onValueChange = {
                    val newNumber = it.toIntOrNull() ?: run {
                        number = ""
                        return@OutlinedTextField
                    }
                    val updatedNumber = min(newNumber, bookWithProgress.book.totalPages)

                    number = updatedNumber.toString()
                },
                singleLine = true,
                textStyle = textFieldTextStyle.copy(textAlign = TextAlign.Center),
                modifier = Modifier.width(textFieldWidth),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "/ ${bookWithProgress.book.totalPages}",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SoftcoverButton(
            label = "Update Progress",
            onClick = { onEvent(ReadingScreenUiEvent.OnUpdatePageProgressClick(newPage = number)) },
            modifier = Modifier.fillMaxWidth(),
            style = ButtonStyle.FILLED,
            size = ButtonSize.M,
        )

        Spacer(modifier = Modifier.height(4.dp))
    }

    @Composable
    fun ColumnScope.ProgressBottomSheetPercentageContent(
        bookWithProgress: BookWithProgress,
        onEvent: (ReadingScreenUiEvent) -> Unit,
    ) {
        var number by remember { mutableStateOf("${bookWithProgress.progress.roundToInt()}") }

        val density = LocalDensity.current

        val textFieldTextStyle = MaterialTheme.typography.bodyLarge

        val textFieldWidth = remember(density) {
            with(density) {
                val fontSizeInPx = textFieldTextStyle.fontSize.toPx()
                val charCount = 4
                val padding = 32.dp.toPx()

                ((charCount * fontSizeInPx * 0.6f) + padding).toDp()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            OutlinedTextField(
                value = number,
                onValueChange = {
                    val newNumber = it.toIntOrNull() ?: run {
                        number = ""

                        return@OutlinedTextField
                    }

                    val updatedNumber = min(newNumber, 100)

                    number = updatedNumber.toString()
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.width(textFieldWidth),
                textStyle = textFieldTextStyle.copy(textAlign = TextAlign.Center),
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "%",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SoftcoverButton(
            label = "Update Progress",
            modifier = Modifier.fillMaxWidth(),
            style = ButtonStyle.FILLED,
            size = ButtonSize.M,
            onClick = {
                onEvent(ReadingScreenUiEvent.OnUpdatePercentageProgressClick(newProgress = number))
            }
        )

        Spacer(modifier = Modifier.height(4.dp))
    }

    @Composable
    private fun BooksDisplay(
        books: List<BookWithProgress>,
        onEvent: (ReadingScreenUiEvent) -> Unit,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(books) {
                BookEntry(
                    bookWithProgress = it,
                    onEvent = onEvent
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
        bookWithProgress: BookWithProgress,
        onEvent: (ReadingScreenUiEvent) -> Unit,
    ) {
        var updateProgressSplitButtonActive by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(all = 16.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SubcomposeAsyncImage(
                        model = bookWithProgress.book.url,
                        modifier = Modifier
                            .size(
                                width = 100.dp,
                                height = 150.dp
                            )
                            .clip(shape = RoundedCornerShape(4.dp)),
                        contentDescription = "Book image",
                        loading = { Box(modifier = Modifier.shimmer()) }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = bookWithProgress.book.title,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        var authorString = ""

                        bookWithProgress.book.authors.forEachIndexed { index, author ->
                            if (index != 0) {
                                authorString += ", "
                            }

                            authorString += author.name
                        }

                        Text(
                            text = authorString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Page ${bookWithProgress.currentPage} of ${bookWithProgress.book.totalPages}",
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
                                        onEvent(ReadingScreenUiEvent.OnMarkBookAsReadClick(book = bookWithProgress))
                                    },
                                    icon = SoftcoverIconResource.Vector(
                                        vector = Icons.Default.CheckCircle,
                                        contentDescription = "Mark as Read icon"
                                    )
                                ),
                                SoftcoverMenuItem(
                                    label = "Switch Edition",
                                    onClick = {
                                        // TODO: Show edition sheet
                                    },
                                    icon = SoftcoverIconResource.Vector(
                                        vector = Icons.AutoMirrored.Default.LibraryBooks,
                                        contentDescription = "Mark as Read icon"
                                    )
                                ),
                            ),
                            label = "Update Progress",
                            trailingIcon = SoftcoverIconResource.Vector(
                                vector = Icons.Default.ArrowDropDown,
                                contentDescription = "Drop down icon",
                            ),
                            onDismissMenuRequest = {
                                updateProgressSplitButtonActive = false
                            },
                            onLeadingButtonClick = {
                                onEvent(ReadingScreenUiEvent.OnShowProgressSheetClick(book = bookWithProgress))
                            },
                            onTrailingButtonClick = {
                                updateProgressSplitButtonActive = it
                            },
                            leadingButtonStyle = SplitButtonStyle.OUTLINED,
                            size = ButtonSize.XS,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val progress = bookWithProgress.progress / 100f

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.weight(1f),
                                drawStopIndicator = {},
                                gapSize = (-2).dp,
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${(bookWithProgress.progress).roundToInt()}%",
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

@StandardPreview
@Composable
private fun ReadingScreenEmptyPreview() {
    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(isLoading = false),
            onEvent = {},
        )
    }
}

@StandardPreview
@Composable
private fun ReadingScreenPreview() {
    val books = listOf(
        BookWithProgress(
            book = Book(
                id = 1,
                title = "The Dungeon Anarchist's Cookbook",
                authors = listOf(
                    Author(name = "Matt Dinniman"),
                ),
                totalPages = 534,
                url = "",
            ),
            currentPage = 470,
            progress = 88.014984f,
            editionId = -1,
            userProgressId = -1,
            startedAt = null,
            finishedAt = null,
        ),
        BookWithProgress(
            book = Book(
                id = 1,
                title = "Last to Leave the Room",
                authors = listOf(
                    Author(name = "Caitlin Starling"),
                ),
                totalPages = 320,
                url = "",
            ),
            currentPage = 262,
            progress = 81.875f,
            editionId = -1,
            userProgressId = -1,
            startedAt = null,
            finishedAt = null,
        ),
        BookWithProgress(
            book = Book(
                id = 1,
                title = "Cursed Bunny",
                authors = listOf(
                    Author(name = "Bora Chung"),
                    Author(name = "Anton Hur"),
                ),
                totalPages = 256,
                url = "",
            ),
            currentPage = 49,
            progress = 19.140625f,
            editionId = -1,
            userProgressId = -1,
            startedAt = null,
            finishedAt = null,
        ),
        BookWithProgress(
            book = Book(
                id = 1,
                title = "Sherlock Holmes: The complete illustrated novels",
                authors = listOf(
                    Author(name = "Arthur Conan Doyle")
                ),
                totalPages = 496,
                url = "",
            ),
            currentPage = 200,
            progress = 40.322582f,
            editionId = -1,
            userProgressId = -1,
            startedAt = null,
            finishedAt = null,
        ),
        BookWithProgress(
            book = Book(
                id = 1,
                title = "The Complete Fiction",
                authors = listOf(
                    Author(name = "H. P. Lovecraft"),
                    Author(name = "S.T. Joshi"),
                ),
                totalPages = 1098,
                url = "",
            ),
            currentPage = 110,
            progress = 10.018215f,
            editionId = -1,
            userProgressId = -1,
            startedAt = null,
            finishedAt = null,
        ),
    )

    SoftcoverTheme {
        ReadingScreen.Screen(
            state = ReadingScreenUiState(books = books),
            onEvent = {},
        )
    }
}

@StandardPreview
@Composable
private fun ProgressSheetContentPagePreview() {
    SoftcoverTheme {
        ReadingScreen.ProgressBottomSheetContent(
            onEvent = {},
            progressTab = ProgressTab.PAGE,
            bookWithProgress = BookWithProgress(
                book = Book(
                    id = 1,
                    title = "The Dungeon Anarchist's Cookbook",
                    authors = listOf(
                        Author(name = "Matt Dinniman"),
                    ),
                    totalPages = 534,
                    url = "",
                ),
                currentPage = 470,
                progress = 88.014984f,
                editionId = -1,
                userProgressId = -1,
                startedAt = null,
                finishedAt = null,
            ),
        )
    }
}

@StandardPreview
@Composable
private fun ProgressSheetContentPercentagePreview() {
    SoftcoverTheme {
        ReadingScreen.ProgressBottomSheetContent(
            onEvent = {},
            progressTab = ProgressTab.PERCENTAGE,
            bookWithProgress = BookWithProgress(
                book = Book(
                    id = 1,
                    title = "The Dungeon Anarchist's Cookbook",
                    authors = listOf(
                        Author(name = "Matt Dinniman"),
                    ),
                    totalPages = 534,
                    url = "",
                ),
                currentPage = 470,
                progress = 88.014984f,
                editionId = -1,
                userProgressId = -1,
                startedAt = null,
                finishedAt = null,
            ),
        )
    }
}