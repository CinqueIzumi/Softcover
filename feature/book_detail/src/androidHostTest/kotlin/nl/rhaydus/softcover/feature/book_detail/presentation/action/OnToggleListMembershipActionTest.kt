package nl.rhaydus.softcover.feature.book_detail.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.RemoveBookFromListUseCase
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
        stateFlow = MutableStateFlow(BookDetailUiState())
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

    @Nested
    inner class Execute {
        @Test
        fun `adds the book with its current edition when it is not a member`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = stubBook())
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
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationSucceededEvent>()
        }

        @Test
        fun `falls back to the default edition when there is no current edition`() = runTest {
            // ----- Arrange -----
            stateFlow.value = BookDetailUiState(book = stubBook(current = null))
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
            stateFlow.value = BookDetailUiState(book = stubBook())
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
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationSucceededEvent>()
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
            stateFlow.value = BookDetailUiState(
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
            stateFlow.value = BookDetailUiState(
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
            stateFlow.value = BookDetailUiState(
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
            stateFlow.value = BookDetailUiState(book = stubBook())
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
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<BookDetailEvent.MutationFailedEvent>()
        }
    }
}
