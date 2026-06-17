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
import nl.rhaydus.softcover.core.book.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnNewEditionSaveClickActionTest {
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

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): BookDetailDependencies {
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

    private fun stubEdition(id: Int = 10): BookEdition = mockk<BookEdition>().also { mock ->
        every { mock.id } returns id
    }

    private fun stubUserBook(): UserBook = mockk()

    private fun stubBook(userBook: UserBook?): Book = mockk<Book>().also { mock ->
        every { mock.userBook } returns userBook
    }

    @Nested
    inner class Execute {
        @Test
        fun `closes the edit edition sheet immediately`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook()
            val book = stubBook(userBook = userBook)
            val edition = stubEdition()
            stateFlow.value = BookDetailUiState(
                book = book,
                showEditEditionSheet = true,
            )
            dependencies = stubDependencies(this)

            coEvery {
                updateBookEditionUseCase(
                    userBook = userBook,
                    newEditionId = any(),
                )
            } returns Result.success(Unit)

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showEditEditionSheet shouldBe false
        }

        @Test
        fun `sets loadingBookDetails to true then false around the use case call`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook()
            val book = stubBook(userBook = userBook)
            val edition = stubEdition()
            stateFlow.value = BookDetailUiState(
                book = book,
                loadingBookDetails = false,
            )
            dependencies = stubDependencies(this)

            coEvery {
                updateBookEditionUseCase(
                    userBook = userBook,
                    newEditionId = any(),
                )
            } returns Result.success(Unit)

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingBookDetails shouldBe false
        }

        @Test
        fun `invokes use case with the edition id provided to the action constructor`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook()
            val book = stubBook(userBook = userBook)
            val edition = stubEdition(id = 99)
            stateFlow.value = BookDetailUiState(book = book)
            dependencies = stubDependencies(this)

            coEvery {
                updateBookEditionUseCase(
                    userBook = userBook,
                    newEditionId = 99,
                )
            } returns Result.success(Unit)

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookEditionUseCase(
                    userBook = userBook,
                    newEditionId = 99,
                )
            }
        }

        @Test
        fun `does not invoke use case when book is null`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition()
            stateFlow.value = BookDetailUiState(
                book = null,
                showEditEditionSheet = true,
            )
            dependencies = stubDependencies(this)

            val action = OnNewEditionSaveClickAction(edition = edition)

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
        fun `sets previewEdition and closes sheet when userBook is null`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val edition = stubEdition(id = 77)
            stateFlow.value = BookDetailUiState(
                book = book,
                showEditEditionSheet = true,
            )
            dependencies = stubDependencies(this)

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.previewEdition shouldBe edition
            stateFlow.value.showEditEditionSheet shouldBe false
            coVerify(exactly = 0) {
                updateBookEditionUseCase(
                    any(),
                    any(),
                )
            }
        }

        @Test
        fun `clears scannedEditionId when userBook is null`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)
            val edition = stubEdition(id = 77)
            stateFlow.value = BookDetailUiState(
                book = book,
                scannedEditionId = 42,
                showEditEditionSheet = true,
            )
            dependencies = stubDependencies(this)

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.scannedEditionId shouldBe null
        }

        @Test
        fun `clears scannedEditionId when userBook is present`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook()
            val book = stubBook(userBook = userBook)
            val edition = stubEdition(id = 99)
            stateFlow.value = BookDetailUiState(
                book = book,
                scannedEditionId = 42,
                showEditEditionSheet = true,
            )
            dependencies = stubDependencies(this)

            coEvery {
                updateBookEditionUseCase(
                    userBook = userBook,
                    newEditionId = 99,
                )
            } returns Result.success(Unit)

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.scannedEditionId shouldBe null
        }

        @Test
        fun `clears loadingBookDetails after use case fails`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook()
            val book = stubBook(userBook = userBook)
            val edition = stubEdition()
            stateFlow.value = BookDetailUiState(
                book = book,
                loadingBookDetails = false,
            )
            dependencies = stubDependencies(this)

            coEvery {
                updateBookEditionUseCase(
                    userBook = userBook,
                    newEditionId = any(),
                )
            } returns Result.failure(RuntimeException("update failed"))

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingBookDetails shouldBe false
        }

        @Test
        fun `closes the edit edition sheet even when use case fails`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook()
            val book = stubBook(userBook = userBook)
            val edition = stubEdition()
            stateFlow.value = BookDetailUiState(
                book = book,
                showEditEditionSheet = true,
            )
            dependencies = stubDependencies(this)

            coEvery {
                updateBookEditionUseCase(
                    userBook = userBook,
                    newEditionId = any(),
                )
            } returns Result.failure(RuntimeException("edition update failed"))

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showEditEditionSheet shouldBe false
        }

        @Test
        fun `loadingBookDetails is true during use case execution then false after`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook()
            val book = stubBook(userBook = userBook)
            val edition = stubEdition()
            stateFlow.value = BookDetailUiState(
                book = book,
                loadingBookDetails = false,
            )
            dependencies = stubDependencies(this)

            val loadingStates = mutableListOf<Boolean>()

            coEvery {
                updateBookEditionUseCase(
                    userBook = userBook,
                    newEditionId = any(),
                )
            } coAnswers {
                loadingStates.add(stateFlow.value.loadingBookDetails)
                Result.success(Unit)
            }

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            loadingStates shouldBe listOf(true)
            stateFlow.value.loadingBookDetails shouldBe false
        }
    }
}
