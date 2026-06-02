package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnUpdateToScannedEditionClickActionTest {
    private lateinit var updateBookEditionUseCase: UpdateBookEditionUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        updateBookEditionUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)

        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.updateBookEditionUseCase
            } returns updateBookEditionUseCase

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

    private fun stubUserBook(): UserBook = mockk()

    private fun stubBook(userBook: UserBook?): Book = mockk<Book>().also { mock ->
        every { mock.userBook } returns userBook
    }

    @Nested
    inner class Execute {
        @Test
        fun `invokes use case with userBook and scannedEditionId when both are set`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook()
            val book = stubBook(userBook = userBook)
            stateFlow.value = BookDetailUiState(
                book = book,
                scannedEditionId = 42,
            )
            dependencies = stubDependencies(this)

            coEvery {
                updateBookEditionUseCase(
                    userBook = userBook,
                    newEditionId = 42,
                )
            } returns Result.success(Unit)

            val action = OnUpdateToScannedEditionClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                updateBookEditionUseCase(
                    userBook = userBook,
                    newEditionId = 42,
                )
            }

            scope.currentState.isUpdatingScannedEdition shouldBe false
        }

        @Test
        fun `does not invoke use case when isUpdatingScannedEdition is already true`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook()
            val book = stubBook(userBook = userBook)
            stateFlow.value = BookDetailUiState(
                book = book,
                scannedEditionId = 42,
                isUpdatingScannedEdition = true,
            )
            dependencies = stubDependencies(this)

            val action = OnUpdateToScannedEditionClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                updateBookEditionUseCase(
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `does not invoke use case when userBook is null`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            stateFlow.value = BookDetailUiState(
                book = book,
                scannedEditionId = 42,
            )
            dependencies = stubDependencies(this)

            val action = OnUpdateToScannedEditionClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                updateBookEditionUseCase(
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `does not invoke use case when scannedEditionId is null`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook()
            val book = stubBook(userBook = userBook)
            stateFlow.value = BookDetailUiState(
                book = book,
                scannedEditionId = null,
            )
            dependencies = stubDependencies(this)

            val action = OnUpdateToScannedEditionClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                updateBookEditionUseCase(
                    any(),
                    any(),
                )
            }
        }
    }
}
