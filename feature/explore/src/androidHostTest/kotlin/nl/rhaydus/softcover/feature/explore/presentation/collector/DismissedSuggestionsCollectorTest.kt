package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetDismissedContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetDismissedContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockBookAction
import nl.rhaydus.softcover.feature.explore.presentation.action.OnUnblockSeriesAction
import nl.rhaydus.softcover.feature.explore.presentation.event.HiddenSuggestionsEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.HiddenSuggestionsDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.HiddenSuggestionsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DismissedSuggestionsCollectorTest {
    private lateinit var getDismissedContinueSeriesBooksUseCase: GetDismissedContinueSeriesBooksUseCase
    private lateinit var getDismissedContinueSeriesUseCase: GetDismissedContinueSeriesUseCase
    private lateinit var dependencies: HiddenSuggestionsDependencies
    private lateinit var stateFlow: MutableStateFlow<HiddenSuggestionsUiState>
    private lateinit var scope: ActionScope<HiddenSuggestionsUiState, HiddenSuggestionsEvent, HiddenSuggestionsLocalVariables>
    private lateinit var dismissedBooksFlow: MutableSharedFlow<List<DismissedSeriesBook>>
    private lateinit var dismissedSeriesFlow: MutableSharedFlow<List<DismissedSeries>>

    @BeforeEach
    fun setUp() {
        dismissedBooksFlow = MutableSharedFlow()
        dismissedSeriesFlow = MutableSharedFlow()
        getDismissedContinueSeriesBooksUseCase = mockk()
        getDismissedContinueSeriesUseCase = mockk()
        stateFlow = MutableStateFlow(HiddenSuggestionsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(HiddenSuggestionsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getDismissedContinueSeriesBooksUseCase()
        } returns dismissedBooksFlow
        every {
            getDismissedContinueSeriesUseCase()
        } returns dismissedSeriesFlow

        dependencies = mockk<HiddenSuggestionsDependencies>(relaxed = true).also { mock ->
            every {
                mock.getDismissedContinueSeriesBooksUseCase
            } returns getDismissedContinueSeriesBooksUseCase
            every {
                mock.getDismissedContinueSeriesUseCase
            } returns getDismissedContinueSeriesUseCase
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

    private fun stubDismissedSeries(
        seriesId: Int,
        seriesName: String? = "Series $seriesId",
    ): DismissedSeries = DismissedSeries(
        seriesId = seriesId,
        seriesName = seriesName,
        coverUrl = null,
        authorText = null,
        bookCount = null,
    )

    // ----- OnLaunch -----

    @Nested
    inner class OnLaunch {
        @Test
        fun `maps each emitted book to a card whose payload restores that book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubDismissedBook(bookId = 1)
            val collector = DismissedSuggestionsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedBooksFlow.emit(listOf(book))
            dismissedSeriesFlow.emit(emptyList())

            // ----- Assert -----
            val hidden = stateFlow.value.hiddenBooks
            hidden.cards.map { it.key } shouldBe listOf(BookCardKey.Book(bookId = 1))
            val action = hidden[BookCardKey.Book(bookId = 1)].shouldBeInstanceOf<OnUnblockBookAction>()
            action.book shouldBe book
            job.cancel()
        }

        @Test
        fun `maps each emitted series to a card whose payload restores that series`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val series = stubDismissedSeries(seriesId = 1)
            val collector = DismissedSuggestionsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedBooksFlow.emit(emptyList())
            dismissedSeriesFlow.emit(listOf(series))

            // ----- Assert -----
            val hidden = stateFlow.value.hiddenSeries
            hidden.cards.map { it.key } shouldBe listOf(BookCardKey.Series(seriesId = 1))
            val action = hidden[BookCardKey.Series(seriesId = 1)].shouldBeInstanceOf<OnUnblockSeriesAction>()
            action.seriesId shouldBe 1
            action.seriesName shouldBe series.seriesName
            job.cancel()
        }

        @Test
        fun `settles both groups to no cards and a zero count when both lists are empty`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val collector = DismissedSuggestionsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                dismissedBooksFlow.emit(emptyList())
                dismissedSeriesFlow.emit(emptyList())

                // ----- Assert -----
                stateFlow.value.hiddenBooks.cards.shouldBeEmpty()
                stateFlow.value.hiddenSeries.cards.shouldBeEmpty()
                stateFlow.value.isLoading shouldBe false
                stateFlow.value.hiddenBooksGroupHeader.count shouldBe "0"
                stateFlow.value.hiddenSeriesGroupHeader.count shouldBe "0"
                job.cancel()
            }

        @Test
        fun `clears loading and sets each group header count to its list size after the first combined emission`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                val books = listOf(stubDismissedBook(bookId = 1), stubDismissedBook(bookId = 2), stubDismissedBook(bookId = 3))
                val series = listOf(stubDismissedSeries(seriesId = 1), stubDismissedSeries(seriesId = 2))
                val collector = DismissedSuggestionsCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                dismissedBooksFlow.emit(books)
                dismissedSeriesFlow.emit(series)

                // ----- Assert -----
                val booksHeader = stateFlow.value.hiddenBooksGroupHeader
                val seriesHeader = stateFlow.value.hiddenSeriesGroupHeader
                booksHeader.header.shouldBeInstanceOf<SectionHeaderUiModel.Inline>().isLoading shouldBe false
                seriesHeader.header.shouldBeInstanceOf<SectionHeaderUiModel.Inline>().isLoading shouldBe false
                booksHeader.count shouldBe "3"
                seriesHeader.count shouldBe "2"
                job.cancel()
            }

        @Test
        fun `writes no state until both flows have emitted`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val initial = stateFlow.value
            val collector = DismissedSuggestionsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            dismissedBooksFlow.emit(listOf(stubDismissedBook(bookId = 1)))

            // ----- Assert -----
            stateFlow.value shouldBeSameInstanceAs initial
            job.cancel()
        }

        @Test
        fun `a later series emission updates only the series group`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DismissedSuggestionsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            dismissedBooksFlow.emit(listOf(stubDismissedBook(bookId = 1)))
            dismissedSeriesFlow.emit(emptyList())

            // ----- Act -----
            dismissedSeriesFlow.emit(listOf(stubDismissedSeries(seriesId = 7), stubDismissedSeries(seriesId = 8)))

            // ----- Assert -----
            stateFlow.value.hiddenBooks.cards.map { it.key } shouldBe listOf(BookCardKey.Book(bookId = 1))
            stateFlow.value.hiddenBooksGroupHeader.count shouldBe "1"
            stateFlow.value.hiddenSeries.cards.map { it.key } shouldBe
                listOf(BookCardKey.Series(seriesId = 7), BookCardKey.Series(seriesId = 8))
            stateFlow.value.hiddenSeriesGroupHeader.count shouldBe "2"
            job.cancel()
        }

        @Test
        fun `a later book emission updates only the books group`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = DismissedSuggestionsCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            dismissedBooksFlow.emit(emptyList())
            dismissedSeriesFlow.emit(listOf(stubDismissedSeries(seriesId = 4)))

            // ----- Act -----
            dismissedBooksFlow.emit(listOf(stubDismissedBook(bookId = 5), stubDismissedBook(bookId = 6)))

            // ----- Assert -----
            stateFlow.value.hiddenSeries.cards.map { it.key } shouldBe listOf(BookCardKey.Series(seriesId = 4))
            stateFlow.value.hiddenSeriesGroupHeader.count shouldBe "1"
            stateFlow.value.hiddenBooks.cards.map { it.key } shouldBe
                listOf(BookCardKey.Book(bookId = 5), BookCardKey.Book(bookId = 6))
            stateFlow.value.hiddenBooksGroupHeader.count shouldBe "2"
            job.cancel()
        }
    }
}
