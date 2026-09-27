package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal class CurrentlyReadingBooksCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        dependencies.getCurrentlyReadingBooksUseCase().collectLatest { books: List<Book> ->
            val restCount = books.drop(1).size

            scope.setState {
                it.copy(
                    books = books,
                    isLoading = false,
                    alsoReadingHeader = SectionHeaderUiModel.Section(
                        eyebrow = if (restCount == 1) "One more in motion" else "$restCount more in motion",
                        headline = "Also between your fingers",
                    ),
                )
            }
        }
    }
}
