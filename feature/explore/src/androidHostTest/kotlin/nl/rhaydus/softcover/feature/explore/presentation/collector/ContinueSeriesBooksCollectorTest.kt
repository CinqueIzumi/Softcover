package nl.rhaydus.softcover.feature.explore.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.model.ContinueSeriesEntryUiModel
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ContinueSeriesBooksCollectorTest {
    private lateinit var getContinueSeriesBooksUseCase: GetContinueSeriesBooksUseCase
    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var dependencies: ExploreDependencies
    private lateinit var stateFlow: MutableStateFlow<ExploreScreenUiState>
    private lateinit var scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>
    private lateinit var continueSeriesBooksFlow: MutableSharedFlow<List<Book>>
    private lateinit var allUserBooksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        continueSeriesBooksFlow = MutableSharedFlow()
        allUserBooksFlow = MutableSharedFlow()
        getContinueSeriesBooksUseCase = mockk()
        getAllUserBooksUseCase = mockk()
        stateFlow = MutableStateFlow(ExploreScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ExploreLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getContinueSeriesBooksUseCase()
        } returns continueSeriesBooksFlow

        every {
            getAllUserBooksUseCase()
        } returns allUserBooksFlow

        dependencies = mockk<ExploreDependencies>(relaxed = true).also { mock ->
            every {
                mock.continueSeriesRefreshTrigger
            } returns MutableStateFlow(0L)

            every {
                mock.getContinueSeriesBooksUseCase
            } returns getContinueSeriesBooksUseCase

            every {
                mock.getAllUserBooksUseCase
            } returns getAllUserBooksUseCase
        }
    }

    private fun stubBook(
        id: Int,
        title: String = "Book $id",
        bookSeries: BookSeries? = null,
    ): Book = mockk {
        every {
            this@mockk.id
        } returns id

        every {
            this@mockk.title
        } returns title

        every {
            this@mockk.bookSeries
        } returns bookSeries
    }

    // ----- OnLaunch -----

    @Nested
    inner class OnLaunch {
        @Test
        fun `updates continueSeriesBooks and clears loading flag on success`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val book = stubBook(id = 1)
            val collector = ContinueSeriesBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            continueSeriesBooksFlow.emit(listOf(book))
            allUserBooksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.continueSeriesBooks shouldBe listOf(book)
            stateFlow.value.loadingContinueSeriesBooks shouldBe false
            job.cancel()
        }

        @Test
        fun `clears loading flag and sets continueSeriesBooks to empty on failure`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            every {
                getContinueSeriesBooksUseCase()
            } returns kotlinx.coroutines.flow.flow {
                throw RuntimeException("use case error")
            }
            val collector = ContinueSeriesBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            allUserBooksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.loadingContinueSeriesBooks shouldBe false
            stateFlow.value.continueSeriesBooks shouldBe emptyList()
            job.cancel()
        }

        @Test
        fun `replaces fetched book with matching user book when ids match`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fetchedBook = stubBook(id = 5)
            val userBook = stubBook(id = 5)
            val collector = ContinueSeriesBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            continueSeriesBooksFlow.emit(listOf(fetchedBook))
            allUserBooksFlow.emit(listOf(userBook))

            // ----- Assert -----
            stateFlow.value.continueSeriesBooks shouldBe listOf(userBook)
            job.cancel()
        }

        @Test
        fun `keeps fetched book when no matching user book is found`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val fetchedBook = stubBook(id = 10)
            val userBook = stubBook(id = 99)
            val collector = ContinueSeriesBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            continueSeriesBooksFlow.emit(listOf(fetchedBook))
            allUserBooksFlow.emit(listOf(userBook))

            // ----- Assert -----
            stateFlow.value.continueSeriesBooks shouldBe listOf(fetchedBook)
            job.cancel()
        }

        @Test
        fun `preserves other state fields when updating continueSeriesBooks on success`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                searchText = "fantasy",
                trendingBooks = emptyList(),
                loadingTrendingBooks = false,
            )
            val book = stubBook(id = 2)
            val collector = ContinueSeriesBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            continueSeriesBooksFlow.emit(listOf(book))
            allUserBooksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.searchText shouldBe "fantasy"
            stateFlow.value.loadingTrendingBooks shouldBe false
            job.cancel()
        }

        @Test
        fun `preserves other state fields when clearing loading on failure`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = ExploreScreenUiState(
                searchText = "sci-fi",
                loadingTrendingBooks = false,
            )
            every {
                getContinueSeriesBooksUseCase()
            } returns kotlinx.coroutines.flow.flow {
                throw RuntimeException("error")
            }
            val collector = ContinueSeriesBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            allUserBooksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.searchText shouldBe "sci-fi"
            stateFlow.value.loadingTrendingBooks shouldBe false
            job.cancel()
        }

        @Test
        fun `builds one continueSeriesEntries per book in the same order`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstBook = stubBook(
                id = 1,
                title = "Mistborn",
                bookSeries = BookSeries(
                    id = 1,
                    name = "Mistborn Era",
                    amountOfBooks = 3,
                ),
            )
            val secondBook = stubBook(
                id = 2,
                title = "Elantris",
            )
            val collector = ContinueSeriesBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }

            // ----- Act -----
            continueSeriesBooksFlow.emit(listOf(firstBook, secondBook))
            allUserBooksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.continueSeriesEntries shouldBe listOf(
                ContinueSeriesEntryUiModel(
                    book = firstBook,
                    menuHeader = SectionHeaderUiModel.Section(
                        eyebrow = "Mistborn Era",
                        headline = "Mistborn",
                    ),
                ),
                ContinueSeriesEntryUiModel(
                    book = secondBook,
                    menuHeader = SectionHeaderUiModel.Section(
                        eyebrow = "Up next",
                        headline = "Elantris",
                    ),
                ),
            )
            job.cancel()
        }

        @Test
        fun `shrinks continueSeriesEntries when a later emission drops a book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstBook = stubBook(id = 1)
            val secondBook = stubBook(id = 2)
            val collector = ContinueSeriesBooksCollector()
            val job = launch { collector.onLaunch(
                scope = scope,
                dependencies = dependencies,
            ) }
            continueSeriesBooksFlow.emit(listOf(firstBook, secondBook))
            allUserBooksFlow.emit(emptyList())
            stateFlow.value.continueSeriesEntries.size shouldBe 2

            // ----- Act -----
            continueSeriesBooksFlow.emit(listOf(firstBook))

            // ----- Assert -----
            stateFlow.value.continueSeriesEntries.size shouldBe 1
            stateFlow.value.continueSeriesEntries.single().book shouldBe firstBook
            job.cancel()
        }
    }
}
