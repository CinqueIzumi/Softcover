package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserBooksFlowCollectorTest {

    private lateinit var getAllUserBooksUseCase: GetAllUserBooksUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>
    private lateinit var userBooksFlow: MutableSharedFlow<List<Book>>

    @BeforeEach
    fun setUp() {
        userBooksFlow = MutableSharedFlow()
        getAllUserBooksUseCase = mockk()
        stateFlow = MutableStateFlow(BookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every { getAllUserBooksUseCase() } returns userBooksFlow

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every { mock.getAllUserBooksUseCase } returns getAllUserBooksUseCase
        }
    }

    // -------------------------------------------------------------------------
    // Builder helpers — use real data-class instances so overlay/.copy() works
    // -------------------------------------------------------------------------

    private fun buildEdition(id: Int, owned: Boolean = false): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 0,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList<Author>(),
        releaseYear = 2020,
        format = "Paperback",
        owned = owned,
    )

    private fun buildUserBook(): UserBook = UserBook(
        id = 1,
        status = BookStatus.None,
        dateAdded = "2024-01-01",
        createdAt = null,
        privacySettingId = 0,
        reviewHasSpoilers = false,
        editionId = null,
        lastReadDate = null,
        rating = null,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = emptyList(),
    )

    private fun buildUserBookRead(): UserBookRead = UserBookRead(
        id = 1,
        currentPage = null,
        currentSeconds = null,
        progress = 0f,
        startedAt = null,
        finishedAt = null,
    )

    private fun buildBook(
        id: Int,
        title: String = "Remote Title",
        rating: Double = 4.5,
        description: String = "Remote description",
        editions: List<BookEdition> = emptyList(),
        userBook: UserBook? = null,
        userBookRead: UserBookRead? = null,
    ): Book = Book(
        id = id,
        title = title,
        editions = editions,
        defaultEdition = null,
        rating = rating,
        description = description,
        releaseYear = 2020,
        coverUrl = "https://example.com/cover.jpg",
        authors = emptyList<Author>(),
        usersCount = 42,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = userBook,
        userBookRead = userBookRead,
    )

    // -------------------------------------------------------------------------

    @Nested
    inner class OnLaunch {

        @Test
        fun `does not change state when book in state is null and flow emits a list`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emittedBook = buildBook(id = 3)
            stateFlow.value = BookDetailUiState(book = null)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(emittedBook))

            // ----- Assert -----
            stateFlow.value.book shouldBe null
            job.cancel()
        }

        @Test
        fun `overlays userBook from local book onto state book when ids match`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val localUserBook = buildUserBook()
            val remoteBook = buildBook(id = 10, userBook = null)
            val localBook = buildBook(id = 10, userBook = localUserBook)
            stateFlow.value = BookDetailUiState(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(localBook))

            // ----- Assert -----
            stateFlow.value.book!!.userBook shouldBe localUserBook
            job.cancel()
        }

        @Test
        fun `overlays userBookRead from local book onto state book when ids match`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val localUserBookRead = buildUserBookRead()
            val remoteBook = buildBook(id = 10, userBookRead = null)
            val localBook = buildBook(id = 10, userBookRead = localUserBookRead)
            stateFlow.value = BookDetailUiState(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(localBook))

            // ----- Assert -----
            stateFlow.value.book!!.userBookRead shouldBe localUserBookRead
            job.cancel()
        }

        @Test
        fun `preserves remote-only fields (title, rating, description) after overlay`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val remoteBook = buildBook(
                id = 5,
                title = "Remote Title",
                rating = 4.5,
                description = "Remote description",
            )
            val localBook = buildBook(
                id = 5,
                title = "Local Title Should Be Ignored",
                rating = 0.0,
                description = "Local description Should Be Ignored",
                userBook = buildUserBook(),
            )
            stateFlow.value = BookDetailUiState(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(localBook))

            // ----- Assert -----
            val resultBook = stateFlow.value.book!!
            resultBook.title shouldBe "Remote Title"
            resultBook.rating shouldBe 4.5
            resultBook.description shouldBe "Remote description"
            job.cancel()
        }

        @Test
        fun `preserves remote editions list size after overlay`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val remoteEditions = listOf(buildEdition(id = 1), buildEdition(id = 2), buildEdition(id = 3))
            val remoteBook = buildBook(id = 8, editions = remoteEditions)
            val localBook = buildBook(id = 8, editions = listOf(buildEdition(id = 1, owned = true)))
            stateFlow.value = BookDetailUiState(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(localBook))

            // ----- Assert -----
            stateFlow.value.book!!.editions.size shouldBe 3
            job.cancel()
        }

        @Test
        fun `sets owned true on a remote edition whose id appears in local owned editions`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val remoteEdition = buildEdition(id = 10, owned = false)
            val remoteBook = buildBook(id = 1, editions = listOf(remoteEdition))
            val localEdition = buildEdition(id = 10, owned = true)
            val localBook = buildBook(id = 1, editions = listOf(localEdition))
            stateFlow.value = BookDetailUiState(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(localBook))

            // ----- Assert -----
            stateFlow.value.book!!.editions.single().owned shouldBe true
            job.cancel()
        }

        @Test
        fun `leaves owned false on a remote edition whose id is not in local owned editions`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val remoteEdition = buildEdition(id = 20, owned = false)
            val remoteBook = buildBook(id = 2, editions = listOf(remoteEdition))
            val localEdition = buildEdition(id = 99, owned = true)
            val localBook = buildBook(id = 2, editions = listOf(localEdition))
            stateFlow.value = BookDetailUiState(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(localBook))

            // ----- Assert -----
            stateFlow.value.book!!.editions.single().owned shouldBe false
            job.cancel()
        }

        @Test
        fun `overlay is authoritative - flips a previously-owned edition to false when it is no longer in local owned set`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            // Start: remote edition is marked owned (e.g. from a prior emission)
            val remoteEdition = buildEdition(id = 30, owned = true)
            val remoteBook = buildBook(id = 3, editions = listOf(remoteEdition))
            // Local book no longer has any owned editions
            val localBook = buildBook(id = 3, editions = listOf(buildEdition(id = 30, owned = false)))
            stateFlow.value = BookDetailUiState(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(localBook))

            // ----- Assert -----
            stateFlow.value.book!!.editions.single().owned shouldBe false
            job.cancel()
        }

        @Test
        fun `sets owned correctly for each edition independently when multiple editions are present`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val ownedId = 41
            val unownedId = 42
            val remoteEditions = listOf(buildEdition(id = ownedId, owned = false), buildEdition(id = unownedId, owned = false))
            val remoteBook = buildBook(id = 4, editions = remoteEditions)
            val localEditions = listOf(buildEdition(id = ownedId, owned = true), buildEdition(id = unownedId, owned = false))
            val localBook = buildBook(id = 4, editions = localEditions)
            stateFlow.value = BookDetailUiState(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(localBook))

            // ----- Assert -----
            val editions = stateFlow.value.book!!.editions
            editions.first { it.id == ownedId }.owned shouldBe true
            editions.first { it.id == unownedId }.owned shouldBe false
            job.cancel()
        }

        @Test
        fun `does not change state when emitted book list does not contain a book matching state book id`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val remoteBook = buildBook(id = 1)
            val unrelatedBook = buildBook(id = 99)
            stateFlow.value = BookDetailUiState(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(unrelatedBook))

            // ----- Assert -----
            stateFlow.value.book shouldBe remoteBook
            job.cancel()
        }

        @Test
        fun `does not change state when emitted list is empty`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val remoteBook = buildBook(id = 5)
            stateFlow.value = BookDetailUiState(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(emptyList())

            // ----- Assert -----
            stateFlow.value.book shouldBe remoteBook
            job.cancel()
        }

        @Test
        fun `re-evaluates overlay when state book id changes to a different book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstBook = buildBook(id = 100)
            val secondLocalUserBook = buildUserBook()
            val secondBook = buildBook(id = 200)
            val localFirstBook = buildBook(id = 100)
            val localSecondBook = buildBook(id = 200, userBook = secondLocalUserBook)

            stateFlow.value = BookDetailUiState(book = firstBook)
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // Emit initial user books list containing both
            userBooksFlow.emit(listOf(localFirstBook, localSecondBook))

            // ----- Act -----
            // Simulate the state's book being changed to a different book (e.g. by InitializeBookWithIdAction)
            stateFlow.value = stateFlow.value.copy(book = secondBook)

            // Re-emit the same user books list to trigger re-evaluation via combine
            userBooksFlow.emit(listOf(localFirstBook, localSecondBook))

            // ----- Assert -----
            stateFlow.value.book!!.userBook shouldBe secondLocalUserBook
            job.cancel()
        }

        @Test
        fun `preserves all other UiState fields when performing overlay`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val remoteBook = buildBook(id = 4)
            val localBook = buildBook(id = 4, userBook = buildUserBook())
            stateFlow.value = BookDetailUiState(
                book = remoteBook,
                loadingBookDetails = false,
                fabMenuExpanded = true,
                showEditEditionSheet = true,
            )
            val collector = UserBooksFlowCollector()
            val job = launch { collector.onLaunch(scope = scope, dependencies = dependencies) }

            // ----- Act -----
            userBooksFlow.emit(listOf(localBook))

            // ----- Assert -----
            stateFlow.value.loadingBookDetails shouldBe false
            stateFlow.value.fabMenuExpanded shouldBe true
            stateFlow.value.showEditEditionSheet shouldBe true
            job.cancel()
        }
    }
}
