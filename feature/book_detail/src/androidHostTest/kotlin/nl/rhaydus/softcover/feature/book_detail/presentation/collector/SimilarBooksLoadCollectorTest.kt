package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.LoadSimilarBooksPageUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildSimilarBook
import nl.rhaydus.softcover.feature.book_detail.presentation.buildSimilarBooksPage
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SimilarBooksLoadCollectorTest {
    private lateinit var loadSimilarBooksPageUseCase: LoadSimilarBooksPageUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        loadSimilarBooksPageUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
        coEvery {
            loadSimilarBooksPageUseCase(
                sourceBookId = any(),
                offset = any(),
                pageSize = any(),
            )
        } answers {
            val sourceId = firstArg<Int>()
            Result.success(
                buildSimilarBooksPage(
                    ids = listOf(sourceId * 100),
                    nextOffset = 1,
                    hasMore = true,
                ),
            )
        }
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.loadSimilarBooksPageUseCase
            } returns loadSimilarBooksPageUseCase

            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher

            every {
                mock.launch(any())
            } answers { callOriginal() }
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `loads the first page of the loaded book on The Book lens`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                book = buildSimilarBook(id = 5),
                selectedLens = BookDetailLens.THE_BOOK,
            )
            val collector = SimilarBooksLoadCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            coVerify(exactly = 1) {
                loadSimilarBooksPageUseCase(
                    sourceBookId = 5,
                    offset = 0,
                    pageSize = any(),
                )
            }
            stateFlow.value.similarBookIds shouldBe listOf(500)
            localVariablesFlow.value.similarBooksSourceId shouldBe 5
            job.cancel()
        }

        @Test
        fun `loads nothing while the book is still loading`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                book = null,
                selectedLens = BookDetailLens.THE_BOOK,
            )
            val collector = SimilarBooksLoadCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            coVerify(exactly = 0) {
                loadSimilarBooksPageUseCase(
                    sourceBookId = any(),
                    offset = any(),
                    pageSize = any(),
                )
            }
            job.cancel()
        }

        @Test
        fun `loads nothing on the Yours lens until The Book lens is selected`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            stateFlow.value = stateFlow.value.copy(
                book = buildSimilarBook(id = 5),
                selectedLens = BookDetailLens.YOURS,
            )
            val collector = SimilarBooksLoadCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
            coVerify(exactly = 0) {
                loadSimilarBooksPageUseCase(
                    sourceBookId = any(),
                    offset = any(),
                    pageSize = any(),
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedLens = BookDetailLens.THE_BOOK)

            // ----- Assert -----
            coVerify(exactly = 1) {
                loadSimilarBooksPageUseCase(
                    sourceBookId = 5,
                    offset = 0,
                    pageSize = any(),
                )
            }
            job.cancel()
        }

        @Test
        fun `loads once per source book when the lens is toggled back and forth`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                dependencies = stubDependencies(this)
                stateFlow.value = stateFlow.value.copy(
                    book = buildSimilarBook(id = 5),
                    selectedLens = BookDetailLens.THE_BOOK,
                )
                val collector = SimilarBooksLoadCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(selectedLens = BookDetailLens.YOURS)
                stateFlow.value = stateFlow.value.copy(selectedLens = BookDetailLens.THE_BOOK)

                // ----- Assert -----
                coVerify(exactly = 1) {
                    loadSimilarBooksPageUseCase(
                        sourceBookId = any(),
                        offset = any(),
                        pageSize = any(),
                    )
                }
                job.cancel()
            }

        @Test
        fun `restarts from the first page with an empty list when the source book changes`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                dependencies = stubDependencies(this)
                stateFlow.value = stateFlow.value.copy(
                    book = buildSimilarBook(id = 5),
                    selectedLens = BookDetailLens.THE_BOOK,
                )
                val collector = SimilarBooksLoadCollector()
                val job = launch {
                    collector.onLaunch(
                        scope = scope,
                        dependencies = dependencies,
                    )
                }

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(book = buildSimilarBook(id = 6))

                // ----- Assert -----
                coVerify(exactly = 1) {
                    loadSimilarBooksPageUseCase(
                        sourceBookId = 6,
                        offset = 0,
                        pageSize = any(),
                    )
                }
                localVariablesFlow.value.similarBooksSourceId shouldBe 6
                localVariablesFlow.value.similarBooks.map { it.id } shouldBe listOf(600)
                stateFlow.value.similarBookIds shouldBe listOf(600)
                job.cancel()
            }
    }
}
