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
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.designsystem.presentation.util.SnackBarManager
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnBulkMoveShelfActionTest {
    private lateinit var markBookAsReadUseCase: MarkBookAsReadUseCase
    private lateinit var markBookAsReadingUseCase: MarkBookAsReadingUseCase
    private lateinit var markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabKey = "status-3"

    @BeforeEach
    fun setUp() {
        markBookAsReadUseCase = mockk(relaxed = true)
        markBookAsReadingUseCase = mockk(relaxed = true)
        markBookAsWantToReadUseCase = mockk(relaxed = true)
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
            every { mock.markBookAsReadUseCase } returns markBookAsReadUseCase
            every { mock.markBookAsReadingUseCase } returns markBookAsReadingUseCase
            every { mock.markBookAsWantToReadUseCase } returns markBookAsWantToReadUseCase
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
                markBookAsReadUseCase(book = selectedBook)
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnBulkMoveShelfAction(
                status = UserBookStatus.READ,
                explicitBookIds = null,
            )
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { markBookAsReadUseCase(book = selectedBook) }
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
                markBookAsReadUseCase(book = selectedBook)
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnBulkMoveShelfAction(
                status = UserBookStatus.READ,
                explicitBookIds = null,
            )
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { markBookAsReadUseCase(book = unselectedBook) }
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
                markBookAsReadUseCase(book = explicitBook)
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnBulkMoveShelfAction(
                status = UserBookStatus.READ,
                explicitBookIds = setOf(99),
            )
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { markBookAsReadUseCase(book = explicitBook) }
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
                markBookAsReadUseCase(book = explicitBook)
            } returns Result.success(ShelfMutationOutcome.Applied)

            val action = OnBulkMoveShelfAction(
                status = UserBookStatus.READ,
                explicitBookIds = setOf(99),
            )
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { markBookAsReadUseCase(book = selectedBook1) }
            coVerify(exactly = 0) { markBookAsReadUseCase(book = selectedBook2) }
        }
    }
}
