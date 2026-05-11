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
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsWantToReadUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnMarkBookAsWantToReadClickActionTest {

    private lateinit var markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        markBookAsWantToReadUseCase = mockk()
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
                mock.markBookAsWantToReadUseCase
            } returns markBookAsWantToReadUseCase

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

    private fun stubBook(id: Int = 1): Book = mockk<Book>().also { mock ->
        every { mock.id } returns id
    }

    @Nested
    inner class Execute {

        @Test
        fun `invokes use case with the book provided to the action constructor`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 42)
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsWantToReadUseCase(book = book)
            } returns Result.success(Unit)

            val action = OnMarkBookAsWantToReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                markBookAsWantToReadUseCase(book = book)
            }
        }

        @Test
        fun `does not alter ui state when use case succeeds`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)
            val initialState = stateFlow.value

            coEvery {
                markBookAsWantToReadUseCase(book = any())
            } returns Result.success(Unit)

            val action = OnMarkBookAsWantToReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
        }

        @Test
        fun `does not alter ui state when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)
            val initialState = stateFlow.value

            coEvery {
                markBookAsWantToReadUseCase(book = any())
            } returns Result.failure(RuntimeException("api error"))

            val action = OnMarkBookAsWantToReadClickAction(book = book)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value shouldBe initialState
        }

        @Test
        fun `completes without throwing when use case fails`() = runTest {
            // ----- Arrange -----
            val book = stubBook()
            dependencies = stubDependencies(this)

            coEvery {
                markBookAsWantToReadUseCase(book = any())
            } returns Result.failure(RuntimeException("network failure"))

            val action = OnMarkBookAsWantToReadClickAction(book = book)

            // ----- Act & Assert -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
        }
    }
}
