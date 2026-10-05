package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetDismissedContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockBookAction
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DismissedBooksCollectorTest {
    private lateinit var getDismissedContinueSeriesBooksUseCase: GetDismissedContinueSeriesBooksUseCase
    private lateinit var dependencies: HiddenSuggestionsDependencies
    private lateinit var stateFlow: MutableStateFlow<HiddenSuggestionsUiState>
    private lateinit var scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>
    private lateinit var dismissedBooksFlow: MutableSharedFlow<List<DismissedSeriesBook>>

    @BeforeEach
    fun setUp() {
        dismissedBooksFlow = MutableSharedFlow()
        getDismissedContinueSeriesBooksUseCase = mockk()
        stateFlow = MutableStateFlow(HiddenSuggestionsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(HiddenSuggestionsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getDismissedContinueSeriesBooksUseCase()
        } returns dismissedBooksFlow

        dependencies = mockk<HiddenSuggestionsDependencies>(relaxed = true).also { mock ->
            every {
                mock.getDismissedContinueSeriesBooksUseCase
            } returns getDismissedContinueSeriesBooksUseCase
        }
    }

    private fun stubDismissedBook(
        bookId: Int,
        title: String? = "Book $bookId",
    ): DismissedSeriesBook = DismissedSeriesBook(
        bookId = bookId,
        title = title,
        coverUrl = null,
        authorText = null,
        seriesName = null,
        seriesId = null,
        seriesPosition = null,
    )

    // ----- OnLaunch -----

    @Nested
    inner class OnLaunch {
        @Test
        fun `maps each emitted book to a card whose payload restores that book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubDismissedBook(bookId = 1)
            val collector = DismissedBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedBooksFlow.emit(listOf(book))

            // ----- Assert -----
            val hidden = stateFlow.value.hiddenBooks
            hidden.cards.map { it.key } shouldBe listOf(BookCardKey.Book(bookId = 1))
            val action = hidden[BookCardKey.Book(bookId = 1)].shouldBeInstanceOf<OnUnblockBookAction>()
            action.book shouldBe book
            job.cancel()
        }

        @Test
        fun `settles to no cards, cleared loading and a zero count when the emitted list is empty`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = DismissedBooksCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                dismissedBooksFlow.emit(emptyList())

                // ----- Assert -----
                stateFlow.value.hiddenBooks.cards.shouldBeEmpty()
                stateFlow.value.isLoadingBooks shouldBe false
                stateFlow.value.hiddenBooksGroupHeader.count shouldBe "0"
                job.cancel()
            }

        @Test
        fun `sets the group header count to the emitted list size`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val books = listOf(stubDismissedBook(bookId = 1), stubDismissedBook(bookId = 2), stubDismissedBook(bookId = 3))
            val collector = DismissedBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedBooksFlow.emit(books)

            // ----- Assert -----
            stateFlow.value.hiddenBooksGroupHeader.count shouldBe "3"
            job.cancel()
        }
    }
}
