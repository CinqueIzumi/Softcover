package nl.rhaydus.softcover.feature.reading.presentation.action

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
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.books.domain.usecase.UpdateBookEditionUseCase
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnNewEditionSaveClickActionTest {

    private lateinit var updateBookEditionUseCase: UpdateBookEditionUseCase
    private lateinit var stateFlow: MutableStateFlow<ReadingScreenUiState>
    private lateinit var scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>

    @BeforeEach
    fun setUp() {
        updateBookEditionUseCase = mockk()
        stateFlow = MutableStateFlow(ReadingScreenUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(ReadingLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): ReadingScreenDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<ReadingScreenDependencies>(relaxed = true).also { mock ->
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

    private fun stubEdition(id: Int = 99): BookEdition = mockk<BookEdition>().also { edition ->
        every { edition.id } returns id
    }

    private fun stubBookWithUserBook(userBook: UserBook): Book = mockk<Book>().also { book ->
        every { book.userBook } returns userBook
    }

    private fun stubBookWithoutUserBook(): Book = mockk<Book>().also { book ->
        every { book.userBook } returns null
    }

    private fun stubUserBook(): UserBook = mockk()

    @Nested
    inner class Execute {

        @Test
        fun `sets showEditionSheet to false after execute`() = runTest {
            // ----- Arrange -----
            val editionId = 99
            val edition = stubEdition(id = editionId)
            val userBook = stubUserBook()
            val book = stubBookWithUserBook(userBook = userBook)
            stateFlow.value = ReadingScreenUiState(
                bookToUpdate = book,
                showEditionSheet = true,
            )
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookEditionUseCase(userBook = userBook, newEditionId = editionId)
            } returns Result.success(Unit)

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.showEditionSheet shouldBe false
        }

        @Test
        fun `invokes updateBookEditionUseCase with the userBook and edition id`() = runTest {
            // ----- Arrange -----
            val editionId = 99
            val edition = stubEdition(id = editionId)
            val userBook = stubUserBook()
            val book = stubBookWithUserBook(userBook = userBook)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookEditionUseCase(userBook = userBook, newEditionId = editionId)
            } returns Result.success(Unit)

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify {
                updateBookEditionUseCase(userBook = userBook, newEditionId = editionId)
            }
        }

        @Test
        fun `sets isLoading to true then false during use case execution`() = runTest {
            // ----- Arrange -----
            val editionId = 99
            val edition = stubEdition(id = editionId)
            val userBook = stubUserBook()
            val book = stubBookWithUserBook(userBook = userBook)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val loadingStates = mutableListOf<Boolean>()

            coEvery {
                updateBookEditionUseCase(userBook = userBook, newEditionId = editionId)
            } answers {
                loadingStates.add(stateFlow.value.isLoading)
                Result.success(Unit)
            }

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            loadingStates.first() shouldBe true
            stateFlow.value.isLoading shouldBe false
        }

        @Test
        fun `does not invoke updateBookEditionUseCase when bookToUpdate has no userBook`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition()
            val book = stubBookWithoutUserBook()
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)
            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                updateBookEditionUseCase(any(), any())
            }
        }

        @Test
        fun `does not invoke updateBookEditionUseCase when bookToUpdate is null`() = runTest {
            // ----- Arrange -----
            val edition = stubEdition()
            stateFlow.value = ReadingScreenUiState(bookToUpdate = null)
            val dependencies = stubDependencies(this)
            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                updateBookEditionUseCase(any(), any())
            }
        }

        @Test
        fun `does not throw when use case fails`() = runTest {
            // ----- Arrange -----
            val editionId = 99
            val edition = stubEdition(id = editionId)
            val userBook = stubUserBook()
            val book = stubBookWithUserBook(userBook = userBook)
            stateFlow.value = ReadingScreenUiState(bookToUpdate = book)
            val dependencies = stubDependencies(this)

            coEvery {
                updateBookEditionUseCase(userBook = userBook, newEditionId = editionId)
            } returns Result.failure(RuntimeException("network error"))

            val action = OnNewEditionSaveClickAction(edition = edition)

            // ----- Act & Assert -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
        }
    }
}
