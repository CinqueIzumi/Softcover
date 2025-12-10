package nl.rhaydus.softcover.feature.reading.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.hilt.getViewModel
import coil.compose.SubcomposeAsyncImage
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverSplitButton
import nl.rhaydus.softcover.core.presentation.model.ButtonSize
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.model.SoftcoverIconResource
import nl.rhaydus.softcover.core.presentation.model.SoftcoverMenuItem
import nl.rhaydus.softcover.core.presentation.model.SplitButtonStyle
import nl.rhaydus.softcover.core.presentation.modifier.conditional
import nl.rhaydus.softcover.core.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.presentation.modifier.shimmer
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.presentation.ProgressSheetTab
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenUiEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenViewModel
import kotlin.math.min
import kotlin.math.roundToInt

// TODO: Add optimistic graphql updates to make the app seem more responsive?
// TODO: Dismiss bottom sheet smoothly when clicking on confirm, don't wait for the api call to finish...
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

    @OptIn(ExperimentalMaterial3Api::class)
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

            if (state.bookToUpdate != null && state.showProgressSheet) {
                ModalBottomSheet(
                    onDismissRequest = { onEvent(ReadingScreenUiEvent.DismissProgressSheet) },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                ) {
                    ProgressBottomSheetContent(
                        bookWithProgress = state.bookToUpdate,
                        progressSheetTab = state.progressSheetTab,
                        onEvent = onEvent,
                    )
                }
            }

            if (state.bookToUpdate != null && state.showEditionSheet) {
                ModalBottomSheet(
                    onDismissRequest = { onEvent(ReadingScreenUiEvent.DismissEditionSheet) },
                    sheetState = rememberModalBottomSheetState(),
                ) {
                    EditionBottomSheetContent(
                        book = state.bookToUpdate,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }

    @Composable
    fun EditionBottomSheetContent(
        book: BookWithProgress,
        onEvent: (ReadingScreenUiEvent) -> Unit,
    ) {
        var selectedEdition by remember {
            mutableStateOf(book.currentEdition)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftcoverButton(
                    label = "Cancel",
                    onClick = { onEvent(ReadingScreenUiEvent.DismissEditionSheet) },
                    style = ButtonStyle.TEXT
                )

                Text(
                    text = "Change edition",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge
                )

                SoftcoverButton(
                    label = "Confirm",
                    enabled = selectedEdition != book.currentEdition,
                    onClick = {
                        onEvent(ReadingScreenUiEvent.OnNewEditionSaveClick(edition = selectedEdition))
                    },
                    style = ButtonStyle.TEXT
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = book.book.title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(book.book.editions) { edition ->
                    EditionItem(
                        edition = edition,
                        selected = edition == selectedEdition,
                        onEditionClick = { selectedEdition = edition }
                    )
                }
            }
        }
    }

    @Composable
    fun EditionItem(
        edition: BookEdition,
        selected: Boolean,
        onEditionClick: () -> Unit,
    ) {
        val cardShape = RoundedCornerShape(8.dp)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = cardShape)
                .conditional(
                    condition = selected,
                    ifTrue = {
                        Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = cardShape
                        )
                    }
                )
                .background(color = MaterialTheme.colorScheme.surface)
                .noRippleClickable(onEditionClick)
                .padding(all = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubcomposeAsyncImage(
                    model = edition.url,
                    modifier = Modifier
                        .size(
                            width = 60.dp,
                            height = 90.dp
                        )
                        .clip(shape = RoundedCornerShape(4.dp)),
                    contentDescription = "Book image",
                    loading = { Box(modifier = Modifier.shimmer()) }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    edition.title?.let {
                        Text(
                            text = edition.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    edition.publisher?.let {
                        Text(
                            text = edition.publisher,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    edition.pages?.let {
                        Text(
                            text = "${edition.pages} pages",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    edition.isbn10?.let { isbn ->
                        Text(
                            text = "ISBN: $isbn",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ProgressBottomSheetContent(
        progressSheetTab: ProgressSheetTab,
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
                ProgressSheetTab.entries.forEach { tab ->
                    val isSelected = tab == progressSheetTab

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                onEvent(ReadingScreenUiEvent.OnProgressTabClick(newProgressSheetTab = tab))
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

            when (progressSheetTab) {
                ProgressSheetTab.PAGE -> {
                    ProgressBottomSheetPageContent(
                        bookWithProgress = bookWithProgress,
                        onEvent = onEvent,
                    )
                }

                ProgressSheetTab.PERCENTAGE -> {
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
        var number by remember {
            val currentPageString = bookWithProgress.currentPage.toString()

            mutableStateOf(TextFieldValue(text = currentPageString))
        }

        var firstTimeFocusedGained by remember { mutableStateOf(true) }

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
                onValueChange = { newValue ->
                    if (firstTimeFocusedGained.not()) {
                        number = number.copy(selection = newValue.selection)
                    } else {
                        firstTimeFocusedGained = false
                    }

                    if (newValue.text == number.text) return@OutlinedTextField

                    if (newValue.text.isEmpty()) {
                        number = newValue
                        return@OutlinedTextField
                    }

                    val newNumber = newValue.text.toIntOrNull() ?: run {
                        number = number.copy(text = "", selection = newValue.selection)
                        return@OutlinedTextField
                    }

                    val updatedNumber = min(newNumber, bookWithProgress.currentEdition.pages ?: 0)

                    number = newValue.copy(text = updatedNumber.toString())
                },
                singleLine = true,
                textStyle = textFieldTextStyle.copy(textAlign = TextAlign.Center),
                modifier = Modifier
                    .width(textFieldWidth)
                    .onFocusChanged {
                        if (it.hasFocus.not()) {
                            firstTimeFocusedGained = true

                            number = number.copy(selection = TextRange.Zero)

                            return@onFocusChanged
                        }

                        number = number.copy(
                            selection = TextRange(start = 0, end = number.text.length),
                        )
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = "/ ${bookWithProgress.currentEdition.pages}",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SoftcoverButton(
            label = "Update Progress",
            onClick = { onEvent(ReadingScreenUiEvent.OnUpdatePageProgressClick(newPage = number.text)) },
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
        var number by remember {
            val currentPageString = bookWithProgress.progress.roundToInt()

            mutableStateOf(TextFieldValue(text = currentPageString.toString()))
        }

        var firstTimeFocusedGained by remember { mutableStateOf(true) }

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
                onValueChange = { newValue ->
                    if (firstTimeFocusedGained.not()) {
                        number = number.copy(selection = newValue.selection)
                    } else {
                        firstTimeFocusedGained = false
                    }

                    if (newValue.text == number.text) return@OutlinedTextField

                    if (newValue.text.isEmpty()) {
                        number = newValue
                        return@OutlinedTextField
                    }

                    val newNumber = newValue.text.toIntOrNull() ?: run {
                        number = number.copy(text = "", selection = newValue.selection)
                        return@OutlinedTextField
                    }

                    val updatedNumber = min(newNumber, 100)

                    number = newValue.copy(text = updatedNumber.toString())
                },
                singleLine = true,
                textStyle = textFieldTextStyle.copy(textAlign = TextAlign.Center),
                modifier = Modifier
                    .width(textFieldWidth)
                    .onFocusChanged {
                        if (it.hasFocus.not()) {
                            firstTimeFocusedGained = true

                            number = number.copy(selection = TextRange.Zero)

                            return@onFocusChanged
                        }

                        number = number.copy(
                            selection = TextRange(start = 0, end = number.text.length),
                        )
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                onEvent(ReadingScreenUiEvent.OnUpdatePercentageProgressClick(newProgress = number.text))
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
                        model = bookWithProgress.currentEdition.url,
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

                        bookWithProgress.currentEdition.authors.forEachIndexed { index, author ->
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
                            text = "Page ${bookWithProgress.currentPage} of ${bookWithProgress.currentEdition.pages}",
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
                                        onEvent(ReadingScreenUiEvent.OnShowEditionSheetClick(book = bookWithProgress))
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
                editions = listOf(
                    BookEdition(
                        id = 20,
                        title = "Ed. The Dungeon Anarchist's Cookbook",
                        url = "",
                        publisher = "",
                        isbn10 = "",
                        pages = 534,
                        authors = listOf(
                            Author(name = "Matt Dinniman"),
                        )
                    )
                ),
            ),
            currentPage = 470,
            progress = 88.014984f,
            editionId = 20,
            startedAt = null,
            finishedAt = null,
            userBookId = -1,
            userBookReadId = -1
        ),
        BookWithProgress(
            book = Book(
                id = 1,
                title = "Last to Leave the Room",
                editions = listOf(
                    BookEdition(
                        id = 20,
                        title = "Ed. Last to Leave the Room",
                        url = "",
                        publisher = "",
                        isbn10 = "",
                        pages = 320,
                        authors = listOf(
                            Author(name = "Caitlin Starling"),
                        )
                    )
                ),
            ),
            currentPage = 262,
            progress = 81.875f,
            editionId = 20,
            userBookReadId = -1,
            userBookId = -1,
            startedAt = null,
            finishedAt = null,
        ),
        BookWithProgress(
            book = Book(
                id = 1,
                title = "Cursed Bunny",
                editions = listOf(
                    BookEdition(
                        id = 20,
                        title = "Ed. Cursed Bunny",
                        url = "",
                        publisher = "",
                        isbn10 = "",
                        pages = 256,
                        authors = listOf(
                            Author(name = "Bora Chung"),
                            Author(name = "Anton Hur"),
                        )
                    )
                ),
            ),
            currentPage = 49,
            progress = 19.140625f,
            editionId = 20,
            startedAt = null,
            finishedAt = null,
            userBookId = -1,
            userBookReadId = -1
        ),
        BookWithProgress(
            book = Book(
                id = 1,
                title = "Sherlock Holmes: The complete illustrated novels",
                editions = listOf(
                    BookEdition(
                        id = 20,
                        title = "Ed. Sherlock Holmes: The complete illustrated novels",
                        url = "",
                        publisher = "",
                        isbn10 = "",
                        pages = 496,
                        authors = listOf(
                            Author(name = "Arthur Conan Doyle"),
                        )
                    )
                ),
            ),
            currentPage = 200,
            progress = 40.322582f,
            editionId = 20,
            startedAt = null,
            finishedAt = null,
            userBookId = -1,
            userBookReadId = -1
        ),
        BookWithProgress(
            book = Book(
                id = 1,
                title = "The Complete Fiction",
                editions = listOf(
                    BookEdition(
                        id = 20,
                        title = "Ed. The Complete Fiction",
                        url = "",
                        publisher = "",
                        isbn10 = "",
                        pages = 1098,
                        authors = listOf(
                            Author(name = "H. P. Lovecraft"),
                            Author(name = "S.T. Joshi"),
                        )
                    )
                ),
            ),
            currentPage = 110,
            progress = 10.018215f,
            editionId = -1,
            startedAt = null,
            finishedAt = null,
            userBookId = -1,
            userBookReadId = -1
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
            progressSheetTab = ProgressSheetTab.PAGE,
            bookWithProgress = BookWithProgress(
                book = Book(
                    id = 1,
                    title = "The Dungeon Anarchist's Cookbook",
                    editions = listOf(
                        BookEdition(
                            id = 20,
                            title = "Ed. The Dungeon Anarchist's Cookbook",
                            url = "",
                            publisher = "",
                            isbn10 = "",
                            pages = 534,
                            authors = listOf(
                                Author(name = "Matt Dinniman"),
                            )
                        )
                    ),
                ),
                currentPage = 470,
                progress = 88.014984f,
                editionId = 20,
                startedAt = null,
                finishedAt = null,
                userBookId = -1,
                userBookReadId = -1
            )
        )
    }
}

@StandardPreview
@Composable
private fun EditionBottomSheetContentPreview() {
    val baseEdition = BookEdition(
        id = 20,
        publisher = "Publisher",
        title = "Snake-Eater",
        url = "url",
        isbn10 = "isbn",
        pages = 20,
        authors = listOf(
            Author(name = "T. Kingfisher")
        )
    )

    SoftcoverTheme {
        ReadingScreen.EditionBottomSheetContent(
            onEvent = {},
            book = BookWithProgress(
                book = Book(
                    id = 1,
                    title = "Snake-Eater",
                    editions = listOf(
                        baseEdition.copy(
                            pages = 271,
                            isbn10 = "123",
                            publisher = "47 north",
                            id = 40
                        ),
                        baseEdition.copy(
                            pages = 352,
                            isbn10 = "234",
                            publisher = "Titan Books",
                            id = 20
                        ),
                        baseEdition.copy(
                            pages = 267,
                            isbn10 = null,
                            publisher = "47 north",
                            id = 80
                        ),
                    )
                ),
                currentPage = 470,
                progress = 88.014984f,
                editionId = 20,
                startedAt = null,
                finishedAt = null,
                userBookId = -1,
                userBookReadId = -1
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
            progressSheetTab = ProgressSheetTab.PERCENTAGE,
            bookWithProgress = BookWithProgress(
                book = Book(
                    id = 1,
                    title = "The Dungeon Anarchist's Cookbook",
                    editions = listOf(
                        BookEdition(
                            id = 20,
                            title = "Ed. The Dungeon Anarchist's Cookbook",
                            url = "",
                            publisher = "",
                            isbn10 = "",
                            pages = 534,
                            authors = listOf(
                                Author(name = "Matt Dinniman"),
                            )
                        )
                    ),
                ),
                currentPage = 470,
                progress = 88.014984f,
                editionId = 20,
                startedAt = null,
                finishedAt = null,
                userBookId = -1,
                userBookReadId = -1
            )
        )
    }
}