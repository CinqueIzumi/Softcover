package nl.rhaydus.softcover.feature.library.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.core.lists.domain.usecase.RemoveBookFromListUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnBulkToggleListMembershipActionTest {
    private lateinit var addBookToListUseCase: AddBookToListUseCase
    private lateinit var removeBookFromListUseCase: RemoveBookFromListUseCase
    private lateinit var dependencies: LibraryDependencies
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val listId = 5
    private val edition = mockk<BookEdition>(relaxed = true)

    @BeforeEach
    fun setUp() {
        addBookToListUseCase = mockk(relaxed = true)
        removeBookFromListUseCase = mockk(relaxed = true)
        dependencies = mockk<LibraryDependencies>(relaxed = true).also { mock ->
            every {
                mock.addBookToListUseCase
            } returns addBookToListUseCase

            every {
                mock.removeBookFromListUseCase
            } returns removeBookFromListUseCase
        }
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        mockkObject(SnackBarManager)

        every {
            SnackBarManager.showSnackbar(title = any())
        } returns mockk(relaxed = true)

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
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun stubBook(
        id: Int,
        bookEdition: BookEdition? = edition,
    ): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.id
        } returns id

        every {
            book.currentEdition
        } returns bookEdition

        every {
            book.defaultEdition
        } returns null
    }

    private fun stubList(onListCount: Int = 0): BookList = BookList(
        id = listId,
        name = "Favourites",
        slug = "favourites",
        books = (1..onListCount).map { bookId ->
            ListBook(
                listBookId = bookId,
                listId = listId,
                bookId = bookId,
                editionId = 1,
            )
        },
    )

    private fun stateWith(
        list: BookList,
        selected: List<Book>,
    ) = LibraryUiState(
        customLists = listOf(list),
        selectedBookIds = selected.map { it.id }.toSet(),
        booksByTab = mapOf("all" to selected),
    )

    @Nested
    inner class Execute {
        @Test
        fun `NONE adds every selected book to the list`() = runTest {
            // ----- Arrange -----
            val first = stubBook(id = 1)
            val second = stubBook(id = 2)
            stateFlow.value = stateWith(
                list = stubList(),
                selected = listOf(first, second),
            )

            // ----- Act -----
            OnBulkToggleListMembershipAction(
                listId = listId,
                currentMembership = ListMembership.NONE,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                addBookToListUseCase(
                    listId = listId,
                    bookId = 1,
                    edition = edition,
                )
            }
            coVerify(exactly = 1) {
                addBookToListUseCase(
                    listId = listId,
                    bookId = 2,
                    edition = edition,
                )
            }
        }

        @Test
        fun `PARTIAL adds only the books not already on the list`() = runTest {
            // ----- Arrange -----
            val onList = stubBook(id = 1)
            val missing = stubBook(id = 2)
            stateFlow.value = stateWith(
                list = stubList(1),
                selected = listOf(onList, missing),
            )

            // ----- Act -----
            OnBulkToggleListMembershipAction(
                listId = listId,
                currentMembership = ListMembership.PARTIAL,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                addBookToListUseCase(
                    listId = listId,
                    bookId = 1,
                    edition = any(),
                )
            }
            coVerify(exactly = 1) {
                addBookToListUseCase(
                    listId = listId,
                    bookId = 2,
                    edition = edition,
                )
            }
        }

        @Test
        fun `ALL removes the selected books that are on the list`() = runTest {
            // ----- Arrange -----
            val first = stubBook(id = 1)
            val second = stubBook(id = 2)
            stateFlow.value = stateWith(
                list = stubList(2),
                selected = listOf(first, second),
            )

            // ----- Act -----
            OnBulkToggleListMembershipAction(
                listId = listId,
                currentMembership = ListMembership.ALL,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                removeBookFromListUseCase(
                    listId = listId,
                    bookId = 1,
                )
            }
            coVerify(exactly = 1) {
                removeBookFromListUseCase(
                    listId = listId,
                    bookId = 2,
                )
            }
            coVerify(exactly = 0) {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            }
        }

        @Test
        fun `a list already being mutated is left alone`() = runTest {
            // ----- Arrange -----
            val book = stubBook(id = 1)
            stateFlow.value = stateWith(
                list = stubList(),
                selected = listOf(book),
            ).copy(listsBeingMutated = setOf(listId))

            // ----- Act -----
            OnBulkToggleListMembershipAction(
                listId = listId,
                currentMembership = ListMembership.NONE,
            ).execute(
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
        fun `an unknown list does nothing`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWith(
                list = stubList(),
                selected = listOf(stubBook(id = 1)),
            )

            // ----- Act -----
            OnBulkToggleListMembershipAction(
                listId = 99,
                currentMembership = ListMembership.NONE,
            ).execute(
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
        fun `an empty selection does nothing`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(customLists = listOf(stubList()))

            // ----- Act -----
            OnBulkToggleListMembershipAction(
                listId = listId,
                currentMembership = ListMembership.NONE,
            ).execute(
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
        fun `the list is no longer marked as mutating once the action finishes`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWith(
                list = stubList(),
                selected = listOf(stubBook(id = 1)),
            )

            // ----- Act -----
            OnBulkToggleListMembershipAction(
                listId = listId,
                currentMembership = ListMembership.NONE,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }

        @Test
        fun `a failed add shows a singular snackbar`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWith(
                list = stubList(),
                selected = listOf(stubBook(id = 1)),
            )

            coEvery {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            } returns Result.failure(IllegalStateException("boom"))

            // ----- Act -----
            OnBulkToggleListMembershipAction(
                listId = listId,
                currentMembership = ListMembership.NONE,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't add 1 book to \"Favourites\" — try again.")
            }
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }

        @Test
        fun `a failed remove shows a plural snackbar naming the removal`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWith(
                list = stubList(2),
                selected = listOf(stubBook(id = 1), stubBook(id = 2)),
            )

            coEvery {
                removeBookFromListUseCase(
                    listId = any(),
                    bookId = any(),
                )
            } returns Result.failure(IllegalStateException("boom"))

            // ----- Act -----
            OnBulkToggleListMembershipAction(
                listId = listId,
                currentMembership = ListMembership.ALL,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't remove 2 books from \"Favourites\" — try again.")
            }
        }

        @Test
        fun `a book with no edition is skipped and reported as a failure`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWith(
                list = stubList(),
                selected = listOf(
                    stubBook(
                        id = 1,
                        bookEdition = null,
                    ),
                ),
            )

            // ----- Act -----
            OnBulkToggleListMembershipAction(
                listId = listId,
                currentMembership = ListMembership.NONE,
            ).execute(
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
            coVerify(exactly = 1) {
                SnackBarManager.showSnackbar(title = "Couldn't add 1 book to \"Favourites\" — try again.")
            }
        }
    }
}
