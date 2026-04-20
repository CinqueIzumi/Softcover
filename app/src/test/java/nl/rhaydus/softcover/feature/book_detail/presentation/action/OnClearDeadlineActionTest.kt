package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.ClearBookDeadlineUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnClearDeadlineActionTest {

    private lateinit var clearBookDeadlineUseCase: ClearBookDeadlineUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        clearBookDeadlineUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.clearBookDeadlineUseCase
            } returns clearBookDeadlineUseCase

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

    private fun stubBook(id: Int = 42): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    @Nested
    inner class Execute {

        @Test
        fun `calls clearBookDeadlineUseCase with the book id from state`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 99)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                clearBookDeadlineUseCase(bookId = 99)
            } returns Result.success(Unit)

            val action = OnClearDeadlineAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                clearBookDeadlineUseCase(bookId = 99)
            }
        }

        @Test
        fun `does nothing when state has no book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = null)
            dependencies = stubDependencies(this)
            val action = OnClearDeadlineAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify(exactly = 0) {
                clearBookDeadlineUseCase(bookId = any())
            }
        }

        @Test
        fun `state is not mutated when book is present`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 5)
            stateFlow.value = BookDetailUiState(book = book, loadingBookDetails = false)
            dependencies = stubDependencies(this)

            coEvery {
                clearBookDeadlineUseCase(bookId = 5)
            } returns Result.success(Unit)

            val action = OnClearDeadlineAction()

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.loadingBookDetails shouldBe false
        }
    }
}
