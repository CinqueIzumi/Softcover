package nl.rhaydus.softcover.feature.book_detail.presentation.collector

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
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshBookQuotesUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuotesRefreshCollectorTest {
    private lateinit var refreshBookQuotesUseCase: RefreshBookQuotesUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        refreshBookQuotesUseCase = mockk()
        coEvery {
            refreshBookQuotesUseCase(bookId = any())
        } returns Result.success(Unit)
        stateFlow = MutableStateFlow(buildBookDetailUiState(bookId = 7))
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true).also {
            every {
                it.refreshBookQuotesUseCase
            } returns refreshBookQuotesUseCase
        }
    }

    private fun TestScope.launchCollector() {
        backgroundScope.launch {
            QuotesRefreshCollector().onLaunch(
                scope = scope,
                dependencies = dependencies,
            )
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `refreshes the book's quotes when a shelved book shows the Yours lens`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = buildBookDetailUiState(bookId = 7).copy(
                    book = buildQuoteBook(),
                    selectedLens = BookDetailLens.YOURS,
                )

                // ----- Act -----
                launchCollector()

                // ----- Assert -----
                coVerify(exactly = 1) {
                    refreshBookQuotesUseCase(bookId = 7)
                }
            }

        @Test
        fun `waits while the book lens is showing`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 7).copy(
                book = buildQuoteBook(),
                selectedLens = BookDetailLens.THE_BOOK,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            coVerify(exactly = 0) {
                refreshBookQuotesUseCase(bookId = any())
            }
        }

        @Test
        fun `never refreshes a book that is not on a shelf`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 7).copy(
                book = buildQuoteBook(shelved = false),
                selectedLens = BookDetailLens.YOURS,
            )

            // ----- Act -----
            launchCollector()

            // ----- Assert -----
            coVerify(exactly = 0) {
                refreshBookQuotesUseCase(bookId = any())
            }
        }

        @Test
        fun `refreshes once the reader switches to the Yours lens`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState(bookId = 7).copy(
                book = buildQuoteBook(),
                selectedLens = BookDetailLens.THE_BOOK,
            )
            launchCollector()

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(selectedLens = BookDetailLens.YOURS)

            // ----- Assert -----
            coVerify(exactly = 1) {
                refreshBookQuotesUseCase(bookId = 7)
            }
        }

        @Test
        fun `does not refresh again when the reader returns to the Yours lens`() =
            runTest(UnconfinedTestDispatcher()) {
                // ----- Arrange -----
                stateFlow.value = buildBookDetailUiState(bookId = 7).copy(
                    book = buildQuoteBook(),
                    selectedLens = BookDetailLens.YOURS,
                )
                launchCollector()
                stateFlow.value = stateFlow.value.copy(selectedLens = BookDetailLens.THE_BOOK)

                // ----- Act -----
                stateFlow.value = stateFlow.value.copy(selectedLens = BookDetailLens.YOURS)

                // ----- Assert -----
                coVerify(exactly = 1) {
                    refreshBookQuotesUseCase(bookId = 7)
                }
            }
    }
}
