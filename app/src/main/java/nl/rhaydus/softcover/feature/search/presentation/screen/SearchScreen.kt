package nl.rhaydus.softcover.feature.search.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import nl.rhaydus.softcover.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.component.EditionImage
import nl.rhaydus.softcover.core.presentation.component.SoftcoverButton
import nl.rhaydus.softcover.core.presentation.component.SoftcoverTopBar
import nl.rhaydus.softcover.core.presentation.model.ButtonStyle
import nl.rhaydus.softcover.core.presentation.modifier.noRippleClickable
import nl.rhaydus.softcover.core.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.presentation.theme.StandardPreview
import nl.rhaydus.softcover.feature.book.presentation.screen.BookDetailScreen
import nl.rhaydus.softcover.feature.search.presentation.action.OnQueryChangeAction
import nl.rhaydus.softcover.feature.search.presentation.action.OnRemoveAllSearchQueriesClickedAction
import nl.rhaydus.softcover.feature.search.presentation.action.OnRemoveSearchQueryClickedAction
import nl.rhaydus.softcover.feature.search.presentation.action.SearchAction
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import nl.rhaydus.softcover.feature.search.presentation.viewmodel.SearchScreenViewModel

class SearchScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val viewModel = koinScreenModel<SearchScreenViewModel>()

        val state by viewModel.state.collectAsStateWithLifecycle()

        Screen(
            onNavigateUp = navigator::pop,
            state = state,
            runAction = viewModel::runAction,
            onBookClick = {
                navigator.push(BookDetailScreen(id = it.id))
            }
        )
    }

    @Composable
    fun Screen(
        state: SearchScreenUiState,
        runAction: (SearchAction) -> Unit,
        onNavigateUp: () -> Unit,
        onBookClick: (Book) -> Unit,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                SoftcoverTopBar(
                    title = "Search",
                    navigateUp = onNavigateUp,
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(horizontal = 16.dp),
            ) {
                OutlinedTextField(
                    value = state.searchText,
                    onValueChange = {
                        runAction(OnQueryChangeAction(newQuery = it))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    placeholder = {
                        Text(
                            text = "Search for titles, authors or ISBNs",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    state.searchText.isEmpty() -> {
                        EmptySearchScreen(
                            state = state,
                            runAction = runAction
                        )
                    }

                    else -> {
                        ActiveSearchScreen(
                            state = state,
                            onBookClick = onBookClick,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ColumnScope.ActiveSearchScreen(
        state: SearchScreenUiState,
        onBookClick: (Book) -> Unit,
    ) {
        Text(
            text = "Showing ${state.queriedBooks.size} results",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.queriedBooks) {
                BookEntry(
                    book = it,
                    onBookClick = onBookClick,
                )
            }
        }
    }

    @Composable
    private fun ColumnScope.EmptySearchScreen(
        state: SearchScreenUiState,
        runAction: (SearchAction) -> Unit,
    ) {
        if (state.previousSearchQueries.isEmpty()) return

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RECENT SEARCHES",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SoftcoverButton(
                label = "Clear all",
                onClick = { runAction(OnRemoveAllSearchQueriesClickedAction()) },
                style = ButtonStyle.TEXT
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            state.previousSearchQueries.forEach { query: String ->
                QueryItem(
                    query = query,
                    runAction = runAction,
                )
            }
        }

        // TODO: Maybe add some sort of tag/genre searching here if possible?
    }

    @Composable
    private fun BookEntry(
        book: Book,
        onBookClick: (Book) -> Unit,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBookClick(book) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EditionImage(
                edition = book.currentEdition,
                modifier = Modifier.width(80.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Text(
                    text = "By ${book.currentEdition.authorString}",
                    style = MaterialTheme.typography.bodyMedium
                )

                // TODO: Fix this label for (readers, but also any missing data...
                var label = ""

                if (book.releaseYear != -1) {
                    label += book.releaseYear.toString()
                }

                if (book.rating != 0.0) {
                    label += " • ${book.rating}"
                }

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            val addedToLibrary = book.userBookId != null

            IconToggleButton(
                checked = addedToLibrary,
                onCheckedChange = { TODO() },
            ) {
                val iconResource = when {
                    addedToLibrary -> Icons.Default.BookmarkAdded
                    else -> Icons.Default.BookmarkBorder
                }

                Icon(iconResource, contentDescription = "")
            }
        }
    }

    // TODO: On click -> set query as search text (with instant search)
    @Composable
    private fun QueryItem(
        query: String,
        runAction: (SearchAction) -> Unit,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Previous search icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = query,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.noRippleClickable {
                        runAction(OnRemoveSearchQueryClickedAction(query = query))
                    }
                )
            }
        }
    }
}

@StandardPreview
@Composable
private fun SearchScreenPreview() {
    SoftcoverTheme {
        SearchScreen().Screen(
            onNavigateUp = {},
            runAction = {},
            onBookClick = {},
            state = SearchScreenUiState(
                searchText = "Last to leave",
                previousSearchQueries = listOf("Bubblegum", "Earthlings", "Convenience Store"),
                queriedBooks = listOf(
                    PreviewData.baseBook.copy(
                        title = "Last to Leave the Room",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2023),
                        rating = 3.7,
                    ),
                    PreviewData.baseBook.copy(
                        title = "The Last to Leave",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2021),
                        authors = listOf(PreviewData.baseAuthor.copy(name = "Erica Lee")),
                        rating = 4.2,
                        userBookId = 20,
                    ),
                    PreviewData.baseBook.copy(
                        title = "Last One to Leave",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2022),
                        authors = listOf(PreviewData.baseAuthor.copy(name = "Benjamin Stevenson")),
                        rating = 4.0,
                    ),
                    PreviewData.baseBook.copy(
                        title = "Will the Last Person To Leave the Planet Please Shut Off the Sun",
                        defaultEdition = PreviewData.baseEdition.copy(releaseYear = 2021),
                        authors = listOf(PreviewData.baseAuthor.copy(name = "Mike Resnick")),
                        rating = 0.0,
                    ),
                )
            )
        )
    }
}

@StandardPreview
@Composable
private fun ActiveSearchScreenPreview() {
    SoftcoverTheme {
        SearchScreen().Screen(
            onNavigateUp = {},
            runAction = {},
            onBookClick = {},
            state = SearchScreenUiState(
                previousSearchQueries = listOf("Bubblegum", "Earthlings", "Convenience Store")
            )
        )
    }
}