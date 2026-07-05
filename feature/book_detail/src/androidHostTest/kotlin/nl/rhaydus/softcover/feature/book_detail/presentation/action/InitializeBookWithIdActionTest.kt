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
import nl.rhaydus.softcover.core.book.domain.usecase.FetchBookByIdUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InitializeBookWithIdActionTest {
    private lateinit var fetchBookByIdUseCase: FetchBookByIdUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var localVariablesFlow: MutableStateFlow<BookDetailLocalVariables>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    @BeforeEach
    fun setUp() {
        fetchBookByIdUseCase = mockk()
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
                mock.fetchBookByIdUseCase
            } returns fetchBookByIdUseCase

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
        editions: List<BookEdition> = listOf(mockk(), mockk()),
    ): Book = mockk {
        every {
            this@mockk.id
        } returns id
        every {
            this@mockk.editions
        } returns editions
    }

    @Nested
    inner class Execute {
        @Test
        fun `sets book and clears loadingBookDetails flag when use case succeeds`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val expectedEditions = listOf<BookEdition>(mockk(), mockk())
            val expectedBook = stubBook(
                id = bookId,
                editions = expectedEditions,
            )
            dependencies = stubDependencies(this)

            coEvery {
                fetchBookByIdUseCase(id = bookId)
            } returns Result.success(expectedBook)

            val action = InitializeBookWithIdAction(id = bookId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.book shouldBe expectedBook
            stateFlow.value.loadingBookDetails shouldBe false
            stateFlow.value.editions shouldBe expectedEditions
            localVariablesFlow.value.editionsLoadedForBookId shouldBe bookId
        }

        @Test
        fun `sets book to null, clears loadingBookDetails, leaves editions empty, and does not stamp editionsLoadedForBookId when use case fails`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            dependencies = stubDependencies(this)

            coEvery {
                fetchBookByIdUseCase(id = bookId)
            } returns Result.failure(RuntimeException("network error"))

            val action = InitializeBookWithIdAction(id = bookId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.book shouldBe null
            stateFlow.value.loadingBookDetails shouldBe false
            stateFlow.value.editions shouldBe emptyList()
            localVariablesFlow.value.editionsLoadedForBookId shouldBe null
        }

        @Test
        fun `invokes use case with the id provided to the action constructor`() = runTest {
            // ----- Arrange -----
            val bookId = 99
            val expectedBook = stubBook(id = bookId)
            dependencies = stubDependencies(this)

            coEvery {
                fetchBookByIdUseCase(id = bookId)
            } returns Result.success(expectedBook)

            val action = InitializeBookWithIdAction(id = bookId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                fetchBookByIdUseCase(id = bookId)
            }
        }

        @Test
        fun `loadingBookDetails remains true while the use case is executing`() = runTest {
            // ----- Arrange -----
            val bookId = 5
            val expectedBook = stubBook(id = bookId)
            dependencies = stubDependencies(this)

            var loadingAtInvocation: Boolean? = null
            coEvery {
                fetchBookByIdUseCase(id = bookId)
            } coAnswers {
                loadingAtInvocation = stateFlow.value.loadingBookDetails
                Result.success(expectedBook)
            }

            val action = InitializeBookWithIdAction(id = bookId)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            loadingAtInvocation shouldBe true
            stateFlow.value.loadingBookDetails shouldBe false
        }
    }
}
