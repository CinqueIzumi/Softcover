package nl.rhaydus.softcover.feature.library.presentation.action

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

class OnBulkRemoveFromLibraryActionTest {
    private lateinit var removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabKey = "status-3"

    @BeforeEach
    fun setUp() {
        removeBookFromLibraryUseCase = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        mockkObject(SnackBarManager)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun buildBook(id: Int): Book = Book(
        id = id,
        canonicalId = null,
        title = "Book $id",
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        releaseDate = null,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
        userBookRead = null,
    )

    private fun stubDependencies(): LibraryDependencies =
        mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.removeBookFromLibraryUseCase
            } returns removeBookFromLibraryUseCase
        }

    @Nested
    inner class Execute {
        @Test
        fun `null explicitBookIds — invokes use case for each selected book`() = runTest {
            // ----- Arrange -----
            val selectedBook = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1),
                booksByTab = mapOf(tabKey to listOf(selectedBook)),
            )

            coEvery {
                removeBookFromLibraryUseCase(book = selectedBook)
            } returns Result.success(Unit)

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = null)
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { removeBookFromLibraryUseCase(book = selectedBook) }
        }

        @Test
        fun `null explicitBookIds — does not invoke use case for books outside the selection`() = runTest {
            // ----- Arrange -----
            val selectedBook = buildBook(id = 1)
            val unselectedBook = buildBook(id = 2)
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1),
                booksByTab = mapOf(tabKey to listOf(selectedBook, unselectedBook)),
            )

            coEvery {
                removeBookFromLibraryUseCase(book = selectedBook)
            } returns Result.success(Unit)

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = null)
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { removeBookFromLibraryUseCase(book = unselectedBook) }
        }

        @Test
        fun `non-null explicitBookIds — invokes use case for the explicit book`() = runTest {
            // ----- Arrange -----
            val explicitBook = buildBook(id = 99)
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1, 2),
                booksByTab = mapOf(tabKey to listOf(buildBook(id = 1), buildBook(id = 2), explicitBook)),
            )

            coEvery {
                removeBookFromLibraryUseCase(book = explicitBook)
            } returns Result.success(Unit)

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = setOf(99))
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { removeBookFromLibraryUseCase(book = explicitBook) }
        }

        @Test
        fun `non-null explicitBookIds — does not invoke use case for the currently selected books`() = runTest {
            // ----- Arrange -----
            val selectedBook1 = buildBook(id = 1)
            val selectedBook2 = buildBook(id = 2)
            val explicitBook = buildBook(id = 99)
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1, 2),
                booksByTab = mapOf(tabKey to listOf(selectedBook1, selectedBook2, explicitBook)),
            )

            coEvery {
                removeBookFromLibraryUseCase(book = explicitBook)
            } returns Result.success(Unit)

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = setOf(99))
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { removeBookFromLibraryUseCase(book = selectedBook1) }
            coVerify(exactly = 0) { removeBookFromLibraryUseCase(book = selectedBook2) }
        }
    }
}
