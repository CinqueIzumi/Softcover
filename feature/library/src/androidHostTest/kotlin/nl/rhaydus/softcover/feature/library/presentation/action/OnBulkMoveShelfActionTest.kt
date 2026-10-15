package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsReadingUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.PauseBookUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ShelfMutationOutcome
import nl.rhaydus.softcover.core.component.haptics.Feedback
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
    private lateinit var pauseBookUseCase: PauseBookUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var eventChannel: Channel<LibraryEvent>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabKey = "status-3"

    @BeforeEach
    fun setUp() {
        markBookAsReadUseCase = mockk(relaxed = true)
        markBookAsReadingUseCase = mockk(relaxed = true)
        markBookAsWantToReadUseCase = mockk(relaxed = true)
        pauseBookUseCase = mockk(relaxed = true)
        eventChannel = Channel(Channel.BUFFERED)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = eventChannel,
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
                mock.markBookAsReadUseCase
            } returns markBookAsReadUseCase
            every {
                mock.markBookAsReadingUseCase
            } returns markBookAsReadingUseCase
            every {
                mock.markBookAsWantToReadUseCase
            } returns markBookAsWantToReadUseCase
            every {
                mock.pauseBookUseCase
            } returns pauseBookUseCase
        }

    @Nested
    inner class Execute {
        @Test
        fun `null explicitBookIds moves only the selected books and then leaves selection mode`() = runTest {
            // ----- Arrange -----
            val selectedBook = buildBook(id = 1)
            val unselectedBook = buildBook(id = 2)
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(selectedBook, unselectedBook)),
                isBulkMoveMenuExpanded = true,
            )

            coEvery {
                markBookAsReadUseCase(book = selectedBook)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnBulkMoveShelfAction(
                status = UserBookStatus.READ,
                explicitBookIds = null,
            ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { markBookAsReadUseCase(book = selectedBook) }
            coVerify(exactly = 0) { markBookAsReadUseCase(book = unselectedBook) }

            stateFlow.value.selectionMode shouldBe false
            stateFlow.value.selectedBookIds shouldBe emptySet()
            stateFlow.value.bulkActionInProgress shouldBe false
            stateFlow.value.isBulkMoveMenuExpanded shouldBe false
        }

        @Test
        fun `non-null explicitBookIds moves only the explicit book, not the current selection`() = runTest {
            // ----- Arrange -----
            val selectedBook1 = buildBook(id = 1)
            val selectedBook2 = buildBook(id = 2)
            val explicitBook = buildBook(id = 99)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1, 2),
                booksByTab = persistentMapOf(tabKey to persistentListOf(selectedBook1, selectedBook2, explicitBook)),
            )

            coEvery {
                markBookAsReadUseCase(book = explicitBook)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnBulkMoveShelfAction(
                status = UserBookStatus.READ,
                explicitBookIds = setOf(99),
            ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { markBookAsReadUseCase(book = explicitBook) }
            coVerify(exactly = 0) { markBookAsReadUseCase(book = selectedBook1) }
            coVerify(exactly = 0) { markBookAsReadUseCase(book = selectedBook2) }
        }

        @Test
        fun `CURRENTLY_READING routes to the reading use case`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
            )

            coEvery {
                markBookAsReadingUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.CURRENTLY_READING).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { markBookAsReadingUseCase(book = book) }
            coVerify(exactly = 0) { markBookAsReadUseCase(book = any()) }
            coVerify(exactly = 0) { markBookAsWantToReadUseCase(book = any()) }
        }

        @Test
        fun `WANT_TO_READ routes to the want-to-read use case`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
            )

            coEvery {
                markBookAsWantToReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.WANT_TO_READ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { markBookAsWantToReadUseCase(book = book) }
            coVerify(exactly = 0) { markBookAsReadUseCase(book = any()) }
            coVerify(exactly = 0) { markBookAsReadingUseCase(book = any()) }
        }

        @Test
        fun `PAUSED routes to the pause use case`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
            )

            coEvery {
                pauseBookUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.PAUSED).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { pauseBookUseCase(book = book) }
            coVerify(exactly = 0) { markBookAsReadingUseCase(book = any()) }
            stateFlow.value.endSessionPauseBookIds shouldBe emptySet()
        }

        @Test
        fun `PAUSED with the open session's book asks for confirmation instead of pausing`() = runTest {
            // ----- Arrange -----
            val sessionBook = buildBook(id = 1)
            val otherBook = buildBook(id = 2)
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1, 2),
                booksByTab = persistentMapOf(tabKey to persistentListOf(sessionBook, otherBook)),
                activeSessionBookId = 1,
                isBulkMoveMenuExpanded = true,
            )

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.PAUSED).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { pauseBookUseCase(book = any()) }
            stateFlow.value.endSessionPauseBookIds shouldBe setOf(1, 2)
            stateFlow.value.isBulkMoveMenuExpanded shouldBe false
            stateFlow.value.bulkActionInProgress shouldBe false
            stateFlow.value.selectionMode shouldBe true
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `PAUSED with a confirmed session pause goes straight to the pause use case`() = runTest {
            // ----- Arrange -----
            val sessionBook = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                booksByTab = persistentMapOf(tabKey to persistentListOf(sessionBook)),
                activeSessionBookId = 1,
            )

            coEvery {
                pauseBookUseCase(book = sessionBook)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnBulkMoveShelfAction(
                status = UserBookStatus.PAUSED,
                explicitBookIds = setOf(1),
                sessionPauseConfirmed = true,
            ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { pauseBookUseCase(book = sessionBook) }
            stateFlow.value.endSessionPauseBookIds shouldBe emptySet()
        }

        @Test
        fun `moving the session's book to a status other than PAUSED does not ask for confirmation`() = runTest {
            // ----- Arrange -----
            val sessionBook = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(sessionBook)),
                activeSessionBookId = 1,
            )

            coEvery {
                markBookAsReadUseCase(book = sessionBook)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.READ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { markBookAsReadUseCase(book = sessionBook) }
            stateFlow.value.endSessionPauseBookIds shouldBe emptySet()
        }

        @Test
        fun `a failed move still moves the other books, shows a snackbar and ends the bulk action`() = runTest {
            // ----- Arrange -----
            val failingBook = buildBook(id = 1)
            val okBook = buildBook(id = 2)
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1, 2),
                booksByTab = persistentMapOf(tabKey to persistentListOf(failingBook, okBook)),
            )

            coEvery {
                markBookAsReadUseCase(book = failingBook)
            } returns Result.failure(RuntimeException("boom"))
            coEvery {
                markBookAsReadUseCase(book = okBook)
            } returns Result.success(ShelfMutationOutcome.Applied)
            every {
                SnackBarManager.showSnackbar(title = any())
            } returns mockk(relaxed = true)

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.READ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) { markBookAsReadUseCase(book = okBook) }
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't move 1 book. Try again.")
            }

            stateFlow.value.selectionMode shouldBe false
            stateFlow.value.bulkActionInProgress shouldBe false
        }

        @Test
        fun `no books resolved closes the move menu without starting progress`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(),
                isBulkMoveMenuExpanded = true,
            )

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.READ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkMoveMenuExpanded shouldBe false
            stateFlow.value.bulkActionInProgress shouldBe false
            coVerify(exactly = 0) { markBookAsReadUseCase(book = any()) }
        }

        @Test
        fun `moving to READ sends a Milestone BulkMoveAppliedEvent`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
            )

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.READ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            val sentEvent = eventChannel.tryReceive().getOrNull()
            sentEvent.shouldBeInstanceOf<LibraryEvent.BulkMoveAppliedEvent>().feedback shouldBe Feedback.Milestone
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `moving to a status other than READ sends a Routine BulkMoveAppliedEvent`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
            )

            coEvery {
                markBookAsReadingUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.CURRENTLY_READING).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<LibraryEvent.BulkMoveAppliedEvent>()
                .feedback shouldBe Feedback.Routine
        }

        @Test
        fun `any failed book sends BulkMoveFailedEvent and not BulkMoveAppliedEvent`() = runTest {
            // ----- Arrange -----
            val failingBook = buildBook(id = 1)
            val okBook = buildBook(id = 2)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1, 2),
                booksByTab = persistentMapOf(tabKey to persistentListOf(failingBook, okBook)),
            )

            coEvery {
                markBookAsReadUseCase(book = failingBook)
            } returns Result.failure(RuntimeException("boom"))
            coEvery {
                markBookAsReadUseCase(book = okBook)
            } returns Result.success(ShelfMutationOutcome.Applied)
            every {
                SnackBarManager.showSnackbar(title = any())
            } returns mockk(relaxed = true)

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.READ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            val sentEvent = eventChannel.tryReceive().getOrNull()
            sentEvent.shouldBeInstanceOf<LibraryEvent.BulkMoveFailedEvent>()
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `after the move it withdraws the bulk sheet requests`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectionMode = true,
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
                isBulkRemoveSheetRequested = true,
                isChooseListsSheetRequested = true,
            )

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.success(ShelfMutationOutcome.Applied)

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.READ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkRemoveSheetRequested shouldBe false
            stateFlow.value.isChooseListsSheetRequested shouldBe false
        }

        @Test
        fun `a failed move still withdraws the bulk sheet requests`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = persistentSetOf(1),
                booksByTab = persistentMapOf(tabKey to persistentListOf(book)),
                isChooseListsSheetRequested = true,
            )

            coEvery {
                markBookAsReadUseCase(book = book)
            } returns Result.failure(RuntimeException("boom"))
            every {
                SnackBarManager.showSnackbar(title = any())
            } returns mockk(relaxed = true)

            // ----- Act -----
            OnBulkMoveShelfAction(status = UserBookStatus.READ).execute(
                dependencies = stubDependencies(),
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isChooseListsSheetRequested shouldBe false
        }
    }
}
