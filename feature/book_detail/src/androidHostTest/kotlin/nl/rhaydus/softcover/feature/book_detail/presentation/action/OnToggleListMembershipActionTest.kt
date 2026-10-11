package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.RemoveBookFromListUseCase
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnToggleListMembershipActionTest {
    private lateinit var addBookToListUseCase: AddBookToListUseCase
    private lateinit var removeBookFromListUseCase: RemoveBookFromListUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var eventChannel: Channel<BookDetailEvent>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>
    private lateinit var currentEdition: BookEdition
    private lateinit var defaultEdition: BookEdition

    @BeforeEach
    fun setUp() {
        currentEdition = mockk()
        defaultEdition = mockk()
        addBookToListUseCase = mockk()
        removeBookFromListUseCase = mockk()
        coEvery {
            addBookToListUseCase(
                listId = any(),
                bookId = any(),
                edition = any(),
            )
        } returns Result.success(Unit)
        coEvery {
            removeBookFromListUseCase(
                listId = any(),
                bookId = any(),
            )
        } returns Result.success(Unit)
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = eventChannel,
        )
        dependencies = mockk<BookDetailDependencies>(relaxed = true).also {
            every {
                it.addBookToListUseCase
            } returns addBookToListUseCase

            every {
                it.removeBookFromListUseCase
            } returns removeBookFromListUseCase
        }
    }

    private fun stubBook(
        current: BookEdition? = currentEdition,
        default: BookEdition? = defaultEdition,
    ): Book = mockk {
        every {
            this@mockk.id
        } returns 7

        every {
            this@mockk.currentEdition
        } returns current

        every {
            this@mockk.defaultEdition
        } returns default
    }

    private fun stubList(vararg bookIds: Int): BookList = BookList(
        id = 3,
        name = "Favourites",
        slug = "favourites",
        books = bookIds.map { bookId ->
            ListBook(
                listBookId = bookId,
                listId = 3,
                bookId = bookId,
                editionId = 1,
            )
        },
    )

    @Nested
    inner class Execute {
        @Test
        fun `adds the book with its current edition when it is not a member`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook())
            val action = OnToggleListMembershipAction(
                listId = 3,
                isMember = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                addBookToListUseCase(
                    listId = 3,
                    bookId = 7,
                    edition = currentEdition,
                )
            }
            coVerify(exactly = 0) {
                removeBookFromListUseCase(
                    listId = any(),
                    bookId = any(),
                )
            }
            val sentEvent = eventChannel.tryReceive().getOrNull()
            sentEvent.shouldBeInstanceOf<BookDetailEvent.MutationSucceededEvent>().feedback shouldBe Feedback.Routine
        }

        @Test
        fun `falls back to the default edition when there is no current edition`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook(current = null))
            val action = OnToggleListMembershipAction(
                listId = 3,
                isMember = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                addBookToListUseCase(
                    listId = 3,
                    bookId = 7,
                    edition = defaultEdition,
                )
            }
        }

        @Test
        fun `removes the book when it is already a member`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook())
            val action = OnToggleListMembershipAction(
                listId = 3,
                isMember = true,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                removeBookFromListUseCase(
                    listId = 3,
                    bookId = 7,
                )
            }
            coVerify(exactly = 0) {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            }
            val sentEvent = eventChannel.tryReceive().getOrNull()
            sentEvent.shouldBeInstanceOf<BookDetailEvent.MutationSucceededEvent>().feedback shouldBe null
        }

        @Test
        fun `does nothing without a book`() = runTest {
            // ----- Arrange -----
            val action = OnToggleListMembershipAction(
                listId = 3,
                isMember = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            }
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }

        @Test
        fun `does nothing when the book has no edition at all`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(
                    current = null,
                    default = null,
                ),
            )
            val action = OnToggleListMembershipAction(
                listId = 3,
                isMember = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            }
        }

        @Test
        fun `ignores a second toggle while the same list is being mutated`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(),
                listsBeingMutated = setOf(3),
            )
            val action = OnToggleListMembershipAction(
                listId = 3,
                isMember = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            }
            stateFlow.value.listsBeingMutated shouldBe setOf(3)
        }

        @Test
        fun `mutates a list while another list is being mutated`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(),
                listsBeingMutated = setOf(4),
            )
            val action = OnToggleListMembershipAction(
                listId = 3,
                isMember = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                addBookToListUseCase(
                    listId = 3,
                    bookId = 7,
                    edition = currentEdition,
                )
            }
            stateFlow.value.listsBeingMutated shouldBe setOf(4)
        }

        @Test
        fun `releases the list again after a failed mutation`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(book = stubBook())
            coEvery {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            } returns Result.failure(IllegalStateException("offline"))
            val action = OnToggleListMembershipAction(
                listId = 3,
                isMember = false,
            )

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.listsBeingMutated shouldBe emptySet()
            val sentEvent = eventChannel.tryReceive().getOrNull()
            sentEvent.shouldBeInstanceOf<BookDetailEvent.MutationFailedEvent>()
        }

        @Test
        fun `the list stays mutated until its book ids change and then clears`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(),
                userLists = listOf(stubList()),
            )
            val job = backgroundScope.launch {
                OnToggleListMembershipAction(
                    listId = 3,
                    isMember = false,
                ).execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            // ----- Act -----
            runCurrent()
            val mutatedBeforeChange = stateFlow.value.listsBeingMutated
            val completedBeforeChange = job.isCompleted
            stateFlow.update { current ->
                current.copy(userLists = listOf(stubList(7)))
            }
            runCurrent()

            // ----- Assert -----
            mutatedBeforeChange shouldBe setOf(3)
            completedBeforeChange shouldBe false
            job.isCompleted shouldBe true
            stateFlow.value.listsBeingMutated shouldBe emptySet()
            val sentEvent = eventChannel.tryReceive().getOrNull()
            sentEvent.shouldBeInstanceOf<BookDetailEvent.MutationSucceededEvent>().feedback shouldBe Feedback.Routine
        }

        @Test
        fun `the list clears when it disappears from state`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(),
                userLists = listOf(stubList()),
            )
            val job = backgroundScope.launch {
                OnToggleListMembershipAction(
                    listId = 3,
                    isMember = false,
                ).execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }
            runCurrent()

            // ----- Act -----
            stateFlow.update { current ->
                current.copy(userLists = emptyList())
            }
            runCurrent()

            // ----- Assert -----
            job.isCompleted shouldBe true
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }

        @Test
        fun `a success that leaves the list data unchanged clears the list only after the timeout`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(),
                userLists = listOf(stubList()),
            )
            val job = backgroundScope.launch {
                OnToggleListMembershipAction(
                    listId = 3,
                    isMember = false,
                ).execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            // ----- Act -----
            runCurrent()
            advanceTimeBy(1_999L)
            runCurrent()
            val mutatedBeforeTimeout = stateFlow.value.listsBeingMutated
            val completedBeforeTimeout = job.isCompleted
            advanceTimeBy(1L)
            runCurrent()

            // ----- Assert -----
            mutatedBeforeTimeout shouldBe setOf(3)
            completedBeforeTimeout shouldBe false
            job.isCompleted shouldBe true
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }

        @Test
        fun `cancelling the action while it waits for list data clears the list`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(),
                userLists = listOf(stubList()),
            )
            val job = backgroundScope.launch {
                OnToggleListMembershipAction(
                    listId = 3,
                    isMember = false,
                ).execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }
            runCurrent()
            val mutatedBeforeCancel = stateFlow.value.listsBeingMutated

            // ----- Act -----
            job.cancel()
            runCurrent()

            // ----- Assert -----
            mutatedBeforeCancel shouldBe setOf(3)
            job.isCancelled shouldBe true
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }

        @Test
        fun `a failed mutation clears the list at once without waiting for list data`() = runTest {
            // ----- Arrange -----
            stateFlow.value = buildBookDetailUiState().copy(
                book = stubBook(),
                userLists = listOf(stubList()),
            )
            coEvery {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            } returns Result.failure(IllegalStateException("offline"))
            val job = backgroundScope.launch {
                OnToggleListMembershipAction(
                    listId = 3,
                    isMember = false,
                ).execute(
                    dependencies = dependencies,
                    scope = scope,
                )
            }

            // ----- Act -----
            runCurrent()

            // ----- Assert -----
            job.isCompleted shouldBe true
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }
    }
}
