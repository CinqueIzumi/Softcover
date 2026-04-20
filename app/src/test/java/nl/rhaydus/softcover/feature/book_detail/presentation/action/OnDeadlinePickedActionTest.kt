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
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.deadlines.domain.usecase.SetBookDeadlineUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OnDeadlinePickedActionTest {

    private lateinit var setBookDeadlineUseCase: SetBookDeadlineUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        setBookDeadlineUseCase = mockk()
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
                mock.setBookDeadlineUseCase
            } returns setBookDeadlineUseCase

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

    private fun stubBook(
        id: Int = 42,
        pages: Int? = 300,
        currentPage: Int? = 50,
    ): Book {
        val edition = mockk<BookEdition> {
            every {
                this@mockk.pages
            } returns pages
        }
        val userBookRead = if (currentPage != null) {
            mockk<UserBookRead> {
                every {
                    this@mockk.currentPage
                } returns currentPage
            }
        } else {
            null
        }
        return mockk {
            every {
                this@mockk.id
            } returns id

            every {
                this@mockk.currentEdition
            } returns edition

            every {
                this@mockk.userBookRead
            } returns userBookRead
        }
    }

    @Nested
    inner class Execute {

        @Test
        fun `calls setBookDeadlineUseCase with bookId totalPages and currentPage from state`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 7, pages = 250, currentPage = 75)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 6, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = 7,
                    deadlineDate = date,
                    currentPage = 75,
                    totalPages = 250,
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                setBookDeadlineUseCase(
                    bookId = 7,
                    deadlineDate = date,
                    currentPage = 75,
                    totalPages = 250,
                )
            }
        }

        @Test
        fun `uses zero for totalPages when currentEdition pages is null`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 1, pages = null, currentPage = 10)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 6, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    currentPage = any(),
                    totalPages = 0,
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    currentPage = any(),
                    totalPages = 0,
                )
            }
        }

        @Test
        fun `uses zero for currentPage when userBookRead is null`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 3, pages = 200, currentPage = null)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 6, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    currentPage = 0,
                    totalPages = any(),
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            coVerify {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    currentPage = 0,
                    totalPages = any(),
                )
            }
        }

        @Test
        fun `sets showDeadlinePicker to false after calling use case`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 5, pages = 100, currentPage = 0)
            stateFlow.value = BookDetailUiState(book = book, showDeadlinePicker = true)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 7, 1)

            coEvery {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    currentPage = any(),
                    totalPages = any(),
                )
            } returns Result.success(Unit)

            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.showDeadlinePicker shouldBe false
        }

        @Test
        fun `does nothing when state has no book`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = null, showDeadlinePicker = true)
            dependencies = stubDependencies(this)
            val date = LocalDate.of(2026, 7, 1)
            val action = OnDeadlinePickedAction(date = date)

            // ----- Act -----
            action.execute(dependencies = dependencies, scope = scope)

            // ----- Assert -----
            stateFlow.value.showDeadlinePicker shouldBe true
            coVerify(exactly = 0) {
                setBookDeadlineUseCase(
                    bookId = any(),
                    deadlineDate = any(),
                    currentPage = any(),
                    totalPages = any(),
                )
            }
        }
    }
}
