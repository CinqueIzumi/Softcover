package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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
        fun `updates hiddenBooks, cards and hiddenBooksLoaded on emission`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value.hiddenBooks shouldBe listOf(book)
            stateFlow.value.hiddenBookCards.shouldNotBeNull().map { it.key } shouldBe listOf(BookCardKey.Book(bookId = 1))
            stateFlow.value.hiddenBooksLoaded shouldBe true
            job.cancel()
        }

        @Test
        fun `leaves hiddenSeriesLoaded false when only books are emitted`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value.hiddenBooksLoaded shouldBe true
            stateFlow.value.hiddenSeriesLoaded shouldBe false
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

        @Test
        fun `sets the group header count to zero for an empty emission`() = runTest(UnconfinedTestDispatcher()) {
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
            stateFlow.value.hiddenBooksGroupHeader.count shouldBe "0"
            job.cancel()
        }

        @Test
        fun `updates the count again on a later emission without touching the header text`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DismissedBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            dismissedBooksFlow.emit(listOf(stubDismissedBook(bookId = 1), stubDismissedBook(bookId = 2)))
            stateFlow.value.hiddenBooksGroupHeader.count shouldBe "2"

            // ----- Act -----
            dismissedBooksFlow.emit(listOf(stubDismissedBook(bookId = 1)))

            // ----- Assert -----
            stateFlow.value.hiddenBooksGroupHeader.count shouldBe "1"
            stateFlow.value.hiddenBooksGroupHeader.header shouldBe HiddenSuggestionsUiState().hiddenBooksGroupHeader.header
            stateFlow.value.hiddenBooksGroupHeader.subline shouldBe HiddenSuggestionsUiState().hiddenBooksGroupHeader.subline
            job.cancel()
        }

        @Test
        fun `preserves the series group header when updating the books header`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = HiddenSuggestionsUiState(
                hiddenSeriesGroupHeader = HiddenSuggestionsUiState().hiddenSeriesGroupHeader.copy(count = "7"),
            )
            val collector = DismissedBooksCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedBooksFlow.emit(listOf(stubDismissedBook(bookId = 1)))

            // ----- Assert -----
            stateFlow.value.hiddenSeriesGroupHeader.count shouldBe "7"
            job.cancel()
        }
    }
}
