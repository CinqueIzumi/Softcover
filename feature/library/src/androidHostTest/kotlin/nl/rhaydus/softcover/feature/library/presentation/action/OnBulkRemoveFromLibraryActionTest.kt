package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

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
        fun `null explicitBookIds removes only the selected books and then leaves selection mode`() = runTest {
            // ----- Arrange -----
            val selectedBook = buildBook(id = 1)
            val unselectedBook = buildBook(id = 2)
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = setOf(1),
                booksByTab = mapOf(tabKey to listOf(selectedBook, unselectedBook)),
                isBulkRemoveSheetShown = true,
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
            coVerify(exactly = 0) { removeBookFromLibraryUseCase(book = unselectedBook) }

            stateFlow.value.selectionMode shouldBe false
            stateFlow.value.selectedBookIds shouldBe emptySet()
            stateFlow.value.bulkActionInProgress shouldBe false
            stateFlow.value.isBulkRemoveCloseRequested shouldBe true
        }

        @Test
        fun `non-null explicitBookIds removes only the explicit book, not the current selection`() = runTest {
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
            coVerify(exactly = 1) { removeBookFromLibraryUseCase(book = explicitBook) }
            coVerify(exactly = 0) { removeBookFromLibraryUseCase(book = selectedBook1) }
            coVerify(exactly = 0) { removeBookFromLibraryUseCase(book = selectedBook2) }
        }

        @Test
        fun `keeps the bulk remove sheet open with progress on while the removal runs`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1),
                booksByTab = mapOf(tabKey to listOf(book)),
                isBulkRemoveSheetShown = true,
            )
            var stateDuringRemoval: LibraryUiState? = null

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } coAnswers {
                stateDuringRemoval = stateFlow.value

                Result.success(Unit)
            }

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = null)

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            val during = stateDuringRemoval.shouldNotBeNull()
            during.isBulkRemoveSheetShown shouldBe true
            during.isBulkRemoveCloseRequested shouldBe false
            during.bulkActionInProgress shouldBe true
        }

        @Test
        fun `a failed removal shows a snackbar and still requests the sheet to close`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1),
                booksByTab = mapOf(tabKey to listOf(book)),
                isBulkRemoveSheetShown = true,
            )

            coEvery {
                removeBookFromLibraryUseCase(book = book)
            } returns Result.failure(RuntimeException("boom"))
            every {
                SnackBarManager.showSnackbar(title = any())
            } returns mockk(relaxed = true)

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = null)

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't remove 1 book — try again.")
            }

            stateFlow.value.isBulkRemoveCloseRequested shouldBe true
            stateFlow.value.isBulkRemoveSheetShown shouldBe true
            stateFlow.value.bulkActionInProgress shouldBe false
        }

        @Test
        fun `no books resolved requests the close without starting progress`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectedBookIds = emptySet(),
                isBulkRemoveSheetShown = true,
            )

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = null)

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkRemoveCloseRequested shouldBe true
            stateFlow.value.isBulkRemoveSheetShown shouldBe true
            stateFlow.value.bulkActionInProgress shouldBe false
        }
    }
}
