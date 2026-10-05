package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.lensToggleFor
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LensToggleCollectorTest {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk(relaxed = true)
    }

    private fun stubBook(userBook: UserBook?): Book = mockk {
        every {
            this@mockk.userBook
        } returns userBook
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `disables Yours when there is no book yet`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = null)
            val collector = LensToggleCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.lensToggle shouldBe lensToggleFor(
                selectedLens = BookDetailLens.THE_BOOK,
                yoursEnabled = false,
            )
            job.cancel()
        }

        @Test
        fun `disables Yours when the book has no user copy`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = stubBook(userBook = null))
            val collector = LensToggleCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.lensToggle shouldBe lensToggleFor(
                selectedLens = BookDetailLens.THE_BOOK,
                yoursEnabled = false,
            )
            job.cancel()
        }

        @Test
        fun `enables Yours when the book has a user copy`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = stubBook(userBook = mockk()))
            val collector = LensToggleCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.lensToggle shouldBe lensToggleFor(
                selectedLens = BookDetailLens.THE_BOOK,
                yoursEnabled = true,
            )
            job.cancel()
        }

        @Test
        fun `reflects the selected lens already in state`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(
                book = stubBook(userBook = mockk()),
                selectedLens = BookDetailLens.YOURS,
            )
            val collector = LensToggleCollector()

            // ----- Act -----
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Assert -----
            stateFlow.value.lensToggle.selected shouldBe BookDetailLens.YOURS
            job.cancel()
        }
    }
}
