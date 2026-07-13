package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.book.domain.usecase.GetEditionsByBookIdUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

class OnShowEditEditionSheetClickActionTest {
    private lateinit var getEditionsByBookIdUseCase: GetEditionsByBookIdUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        getEditionsByBookIdUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        localVariablesFlow = MutableStateFlow(BookDetailLocalVariables())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = localVariablesFlow,
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: TestScope): BookDetailDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.getEditionsByBookIdUseCase
            } returns getEditionsByBookIdUseCase

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
        every {
            mock.id
        } returns id
    }

    private fun stubEdition(): BookEdition = mockk()

    @Nested
    inner class Execute {
        @Test
        fun `sets showEditEditionSheet to true immediately on first open`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = stubBook(id = bookId)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                getEditionsByBookIdUseCase(bookId = bookId)
            } returns Result.success(emptyList())

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showEditEditionSheet shouldBe true
        }

        @Test
        fun `populates editions and clears loadingEditions after successful fetch`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = stubBook(id = bookId)
            val edition = stubEdition()
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                getEditionsByBookIdUseCase(bookId = bookId)
            } returns Result.success(listOf(edition))

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editions shouldBe listOf(edition)
            stateFlow.value.loadingEditions shouldBe false
        }

        @Test
        fun `stamps editionsLoadedForBookId in local variables after successful fetch`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = stubBook(id = bookId)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                getEditionsByBookIdUseCase(bookId = bookId)
            } returns Result.success(emptyList())

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.editionsLoadedForBookId shouldBe bookId
        }

        @Test
        fun `loadingEditions is true during use case execution and false after success`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            val book = stubBook(id = bookId)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            var loadingAtInvocation: Boolean? = null

            coEvery {
                getEditionsByBookIdUseCase(bookId = bookId)
            } coAnswers {
                loadingAtInvocation = stateFlow.value.loadingEditions
                Result.success(emptyList())
            }

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            loadingAtInvocation shouldBe true
            stateFlow.value.loadingEditions shouldBe false
        }

        @Test
        fun `does not call use case on second open when editions already loaded for same book`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val book = stubBook(id = bookId)
            stateFlow.value = BookDetailUiState(book = book)
            localVariablesFlow.value = BookDetailLocalVariables(editionsLoadedForBookId = bookId)
            dependencies = stubDependencies(this)

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                getEditionsByBookIdUseCase(bookId = any())
            }
        }

        @Test
        fun `sets showEditEditionSheet to true on memoized second open`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val book = stubBook(id = bookId)
            stateFlow.value = BookDetailUiState(
                book = book,
                showEditEditionSheet = false,
            )
            localVariablesFlow.value = BookDetailLocalVariables(editionsLoadedForBookId = bookId)
            dependencies = stubDependencies(this)

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showEditEditionSheet shouldBe true
        }

        @Test
        fun `does not change loadingEditions on memoized second open`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val book = stubBook(id = bookId)
            stateFlow.value = BookDetailUiState(
                book = book,
                loadingEditions = false,
            )
            localVariablesFlow.value = BookDetailLocalVariables(editionsLoadedForBookId = bookId)
            dependencies = stubDependencies(this)

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingEditions shouldBe false
        }

        @Test
        fun `clears loadingEditions and keeps sheet open after use case failure`() = runTest {
            // ----- Arrange -----
            val bookId = 99
            val book = stubBook(id = bookId)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                getEditionsByBookIdUseCase(bookId = bookId)
            } returns Result.failure(RuntimeException("network error"))

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingEditions shouldBe false
            stateFlow.value.showEditEditionSheet shouldBe true
        }

        @Test
        fun `does not populate editions after use case failure`() = runTest {
            // ----- Arrange -----
            val bookId = 99
            val book = stubBook(id = bookId)
            stateFlow.value = BookDetailUiState(
                book = book,
                editions = emptyList(),
            )
            dependencies = stubDependencies(this)

            coEvery {
                getEditionsByBookIdUseCase(bookId = bookId)
            } returns Result.failure(RuntimeException("network error"))

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.editions shouldBe emptyList()
        }

        @Test
        fun `does not stamp editionsLoadedForBookId in local variables after failure`() = runTest {
            // ----- Arrange -----
            val bookId = 99
            val book = stubBook(id = bookId)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                getEditionsByBookIdUseCase(bookId = bookId)
            } returns Result.failure(RuntimeException("network error"))

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value.editionsLoadedForBookId shouldBe null
        }

        @Test
        fun `is a no-op when book in state is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = null)
            dependencies = stubDependencies(this)

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showEditEditionSheet shouldBe false
            coVerify(exactly = 0) {
                getEditionsByBookIdUseCase(bookId = any())
            }
        }

        @Test
        fun `does not modify local variables when book in state is null`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = null)
            dependencies = stubDependencies(this)

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            localVariablesFlow.value shouldBe BookDetailLocalVariables()
        }

        @Test
        fun `invokes use case with the book id from state`() = runTest {
            // ----- Arrange -----
            val bookId = 13
            val book = stubBook(id = bookId)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                getEditionsByBookIdUseCase(bookId = bookId)
            } returns Result.success(emptyList())

            val action = OnShowEditEditionSheetClickAction()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                getEditionsByBookIdUseCase(bookId = bookId)
            }
        }
    }
}
