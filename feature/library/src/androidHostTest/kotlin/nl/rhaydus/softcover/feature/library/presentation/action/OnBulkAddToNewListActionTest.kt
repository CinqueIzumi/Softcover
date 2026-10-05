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
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.lists.domain.usecase.AddBookToListUseCase
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnBulkAddToNewListActionTest {
    private lateinit var addBookToListUseCase: AddBookToListUseCase
    private lateinit var stateFlow: MutableStateFlow<LibraryUiState>
    private lateinit var scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>

    private val tabKey = "status-3"
    private val listId = 10
    private val listName = "Cozy Mysteries"

    @BeforeEach
    fun setUp() {
        addBookToListUseCase = mockk(relaxed = true)
        stateFlow = MutableStateFlow(LibraryUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LibraryLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        mockkObject(SnackBarManager)

        every {
            SnackBarManager.showSnackbar(title = any())
        } returns Unit
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(SnackBarManager)
    }

    private fun buildEdition(id: Int): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = id,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList(),
        releaseYear = 2020,
        releaseDate = null,
        format = "Hardcover",
        readingFormat = null,
        owned = false,
    )

    private fun buildBook(
        id: Int,
        edition: BookEdition? = buildEdition(id = id),
    ): Book = Book(
        id = id,
        canonicalId = null,
        title = "Book $id",
        editions = emptyList(),
        defaultEdition = edition,
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
                mock.addBookToListUseCase
            } returns addBookToListUseCase
        }

    @Nested
    inner class Execute {
        @Test
        fun `empty selection reopens the sheet without any use case calls or snackbar`() = runTest {
            // ----- Arrange -----
            stateFlow.value = LibraryUiState(
                selectedBookIds = emptySet(),
                booksByTab = emptyMap(),
            )

            val action = OnBulkAddToNewListAction(
                listId = listId,
                listName = listName,
            )
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.isBulkAddToListSheetShown shouldBe true
            coVerify(exactly = 0) {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            }
            verify(exactly = 0) {
                SnackBarManager.showSnackbar(title = any())
            }
        }

        @Test
        fun `adds each selected book to a list missing from customLists, reopens the sheet and shows no snackbar`() = runTest {
            // ----- Arrange -----
            val firstBook = buildBook(id = 1)
            val secondBook = buildBook(id = 2)
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1, 2),
                booksByTab = mapOf(tabKey to listOf(firstBook, secondBook)),
            )

            coEvery {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            } returns Result.success(Unit)

            val action = OnBulkAddToNewListAction(
                listId = listId,
                listName = listName,
            )
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                addBookToListUseCase(
                    listId = listId,
                    bookId = firstBook.id,
                    edition = firstBook.defaultEdition.shouldNotBeNull(),
                )
            }
            coVerify(exactly = 1) {
                addBookToListUseCase(
                    listId = listId,
                    bookId = secondBook.id,
                    edition = secondBook.defaultEdition.shouldNotBeNull(),
                )
            }
            verify(exactly = 0) {
                SnackBarManager.showSnackbar(title = any())
            }

            stateFlow.value.isBulkAddToListSheetShown shouldBe true
            stateFlow.value.listsBeingMutated shouldBe emptySet()
        }

        @Test
        fun `a book with no edition never reaches the use case and counts as a failure`() = runTest {
            // ----- Arrange -----
            val editionlessBook = buildBook(
                id = 1,
                edition = null,
            )
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1),
                booksByTab = mapOf(tabKey to listOf(editionlessBook)),
            )

            val action = OnBulkAddToNewListAction(
                listId = listId,
                listName = listName,
            )
            val dependencies = stubDependencies()

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
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(
                    title = match { title -> title.contains(listName) },
                )
            }
        }

        @Test
        fun `failed add-to-list call surfaces the failure snackbar with the list name`() = runTest {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            stateFlow.value = LibraryUiState(
                selectedBookIds = setOf(1),
                booksByTab = mapOf(tabKey to listOf(book)),
            )

            coEvery {
                addBookToListUseCase(
                    listId = any(),
                    bookId = any(),
                    edition = any(),
                )
            } returns Result.failure(RuntimeException("server error"))

            val action = OnBulkAddToNewListAction(
                listId = listId,
                listName = listName,
            )
            val dependencies = stubDependencies()

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            verify(exactly = 1) {
                SnackBarManager.showSnackbar(
                    title = match { title -> title.contains(listName) },
                )
            }
        }
    }
}
