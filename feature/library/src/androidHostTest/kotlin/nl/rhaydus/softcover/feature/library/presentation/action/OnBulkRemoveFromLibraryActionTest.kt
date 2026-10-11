package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.removal.UndoableBookRemover
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.openChooseListsSession
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.CreateList
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnBulkRemoveFromLibraryActionTest {
    private lateinit var undoableBookRemover: UndoableBookRemover
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabKey = "status-3"

    @BeforeEach
    fun setUp() {
        undoableBookRemover = mockk()
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
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
                mock.undoableBookRemover
            } returns undoableBookRemover
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
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(selectedBook, unselectedBook)),
                isBulkRemoveSheetRequested = true,
            )

            coEvery {
                undoableBookRemover.remove(books = listOf(selectedBook))
            } returns Result.success(Unit)

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = null)

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { undoableBookRemover.remove(books = listOf(selectedBook)) }

            stateFlow.value.selectionMode shouldBe false
            stateFlow.value.selectedBookIds shouldBe emptySet()
            stateFlow.value.bulkActionInProgress shouldBe false
            stateFlow.value.isBulkRemoveSheetRequested shouldBe false
            stateFlow.value.isChooseListsSheetRequested shouldBe false
        }

        @Test
        fun `non-null explicitBookIds removes only the explicit book, not the current selection`() = runTest {
            // ----- Arrange -----
            val selectedBook1 = buildBook(id = 1)
            val selectedBook2 = buildBook(id = 2)
            val explicitBook = buildBook(id = 99)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1, 2),
                booksByTab = persistentMapOf(tabKey to persistentListOf(selectedBook1, selectedBook2, explicitBook)),
            )

            coEvery {
                undoableBookRemover.remove(books = listOf(explicitBook))
            } returns Result.success(Unit)

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = setOf(99))

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { undoableBookRemover.remove(books = listOf(explicitBook)) }
            coVerify(exactly = 1) { undoableBookRemover.remove(books = any()) }
        }

        @Test
        fun `keeps the bulk remove sheet requested with progress on while the stage runs`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
                isBulkRemoveSheetRequested = true,
            )
            var stateDuringRemoval: LibraryUiState? = null

            coEvery {
                undoableBookRemover.remove(books = listOf(book))
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
            during.isBulkRemoveSheetRequested shouldBe true
            during.bulkActionInProgress shouldBe true
        }

        @Test
        fun `a failed stage still resets selection and withdraws the sheet request`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
                isBulkRemoveSheetRequested = true,
            )

            coEvery {
                undoableBookRemover.remove(books = listOf(book))
            } returns Result.failure(RuntimeException("boom"))

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = null)

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.selectionMode shouldBe false
            stateFlow.value.selectedBookIds shouldBe emptySet()
            stateFlow.value.isBulkRemoveSheetRequested shouldBe false
            stateFlow.value.bulkActionInProgress shouldBe false
        }

        @Test
        fun `no books resolved withdraws the sheet request without calling the remover`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(),
                isBulkRemoveSheetRequested = true,
            )

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = null)

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { undoableBookRemover.remove(books = any()) }
            stateFlow.value.isBulkRemoveSheetRequested shouldBe false
            stateFlow.value.bulkActionInProgress shouldBe false
        }

        @Test
        fun `a successful stage also withdraws the choose-lists request and keeps its create-list request`() =
            runTest {
                // ----- Arrange -----
                val book = buildBook(id = 1)
                stateFlow.value = LibraryUiState(
                    selectionMode = true,
                    selectedBookIds = persistentSetOf(1),
                    booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
                    isBulkRemoveSheetRequested = true,
                    chooseListsSheet = openChooseListsSession(pending = CreateList),
                )

                coEvery {
                    undoableBookRemover.remove(books = listOf(book))
                } returns Result.success(Unit)

                val action = OnBulkRemoveFromLibraryAction(explicitBookIds = null)

                // ----- Act -----
                action.execute(
                    dependencies = stubDependencies(),
                    scope = scope,
                )

                // ----- Assert -----
                stateFlow.value.isChooseListsSheetRequested shouldBe false
                stateFlow.value.chooseListsSheet.dismissed() shouldBe CreateList
            }

        @Test
        fun `leaves the bulk remove session closed when nothing is shown and nothing resolves`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(selectedBookIds = persistentSetOf())

            val action = OnBulkRemoveFromLibraryAction(explicitBookIds = null)

            // ----- Act -----
            action.execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.bulkRemoveSheet shouldBe SheetSession.Closed
        }
    }
}
