package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveUserBookUseCase
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserBooksFlowCollectorTest {
    private lateinit var observeUserBookUseCase: ObserveUserBookUseCase
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var stateFlow: MutableStateFlow<BookDetailUiState>
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>
    private lateinit var userBooksFlow: MutableSharedFlow<Book?>

    @BeforeEach
    fun setUp() {
        userBooksFlow = MutableSharedFlow()
        observeUserBookUseCase = mockk()
        stateFlow = MutableStateFlow(buildBookDetailUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(BookDetailLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            observeUserBookUseCase(id = any())
        } returns userBooksFlow

        dependencies = mockk<BookDetailDependencies>(relaxed = true).also { mock ->
            every {
                mock.observeUserBookUseCase
            } returns observeUserBookUseCase
        }
    }

    // -------------------------------------------------------------------------
    // Builder helpers — use real data-class instances so overlay/.copy() works
    // -------------------------------------------------------------------------

    private fun buildEdition(
        id: Int,
        owned: Boolean = false,
    ): BookEdition = BookEdition(
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

    private fun buildUserBook(reads: List<UserBookRead> = emptyList()): UserBook = UserBook(
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
        reads = reads,
    )

    private fun buildUserBookRead(): UserBookRead = UserBookRead(
        id = 1,
        currentPage = null,
        currentSeconds = null,
        progress = 0f,
        startedAt = null,
        finishedAt = null,
        finishedAtPrecision = null,
        editionPages = null,
    )

    private fun buildBook(
        id: Int,
        title: String = "Remote Title",
        rating: Double = 4.5,
        description: String = "Remote description",
        editions: List<BookEdition> = emptyList(),
        userBook: UserBook? = null,
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
    )

    // -------------------------------------------------------------------------

    @Nested
    inner class OnLaunch {
        @Test
        fun `does not change state when book in state is null and flow emits a book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val emittedBook = buildBook(id = 3)
            stateFlow.value = buildBookDetailUiState().copy(book = null)
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            userBooksFlow.emit(emittedBook)

            // ----- Assert -----
            stateFlow.value.book shouldBe null
            job.cancel()
        }

        @Test
        fun `overlays userBook from local book onto state book when ids match`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val localUserBook = buildUserBook()
            val remoteBook = buildBook(
                id = 10,
                userBook = null,
            )
            val localBook = buildBook(
                id = 10,
                userBook = localUserBook,
            )
            stateFlow.value = buildBookDetailUiState().copy(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            userBooksFlow.emit(localBook)

            // ----- Assert -----
            stateFlow.value.book.shouldNotBeNull().userBook shouldBe localUserBook
            job.cancel()
        }

        @Test
        fun `overlays the reads carried by the local user book onto state book when ids match`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val localUserBookRead = buildUserBookRead()
            val remoteBook = buildBook(
                id = 10,
                userBook = null,
            )
            val localBook = buildBook(
                id = 10,
                userBook = buildUserBook(reads = listOf(localUserBookRead)),
            )
            stateFlow.value = buildBookDetailUiState().copy(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            userBooksFlow.emit(localBook)

            // ----- Assert -----
            stateFlow.value.book.shouldNotBeNull().userBook.shouldNotBeNull().reads shouldBe listOf(localUserBookRead)
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
            stateFlow.value = buildBookDetailUiState().copy(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            userBooksFlow.emit(localBook)

            // ----- Assert -----
            val resultBook = stateFlow.value.book.shouldNotBeNull()
            resultBook.title shouldBe "Remote Title"
            resultBook.rating shouldBe 4.5
            resultBook.description shouldBe "Remote description"
            job.cancel()
        }

        @Test
        fun `leaves owned false on a remote edition whose id is not in local owned editions`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val remoteEdition = buildEdition(
                id = 20,
                owned = false,
            )
            val remoteBook = buildBook(
                id = 2,
                editions = persistentListOf(remoteEdition),
            )
            val localEdition = buildEdition(
                id = 99,
                owned = true,
            )
            val localBook = buildBook(
                id = 2,
                editions = persistentListOf(localEdition),
            )
            stateFlow.value = buildBookDetailUiState().copy(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            userBooksFlow.emit(localBook)

            // ----- Assert -----
            stateFlow.value.book.shouldNotBeNull().editions.single().owned shouldBe false
            job.cancel()
        }

        @Test
        fun `overlay is authoritative - flips a previously-owned edition to false when it is no longer in local owned set`() =
            runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            // Start: remote edition is marked owned (e.g. from a prior emission)
            val remoteEdition = buildEdition(
                id = 30,
                owned = true,
            )
            val remoteBook = buildBook(
                id = 3,
                editions = persistentListOf(remoteEdition),
            )
            // Local book no longer has any owned editions
            val localBook = buildBook(
                id = 3,
                editions = persistentListOf(
                    buildEdition(
                        id = 30,
                        owned = false,
                    ),
                ),
            )
            stateFlow.value = buildBookDetailUiState().copy(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            userBooksFlow.emit(localBook)

            // ----- Assert -----
            stateFlow.value.book.shouldNotBeNull().editions.single().owned shouldBe false
            job.cancel()
        }

        @Test
        fun `sets owned correctly for each edition independently when multiple editions are present`() =
            runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val ownedId = 41
            val unownedId = 42
            val remoteEditions = listOf(
                buildEdition(
                    id = ownedId,
                    owned = false,
                ),
                buildEdition(
                    id = unownedId,
                    owned = false,
                ),
            )
            val remoteBook = buildBook(
                id = 4,
                editions = remoteEditions,
            )
            val localEditions = listOf(
                buildEdition(
                    id = ownedId,
                    owned = true,
                ),
                buildEdition(
                    id = unownedId,
                    owned = false,
                ),
            )
            val localBook = buildBook(
                id = 4,
                editions = localEditions,
            )
            stateFlow.value = buildBookDetailUiState().copy(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            userBooksFlow.emit(localBook)

            // ----- Assert -----
            val editions = stateFlow.value.book.shouldNotBeNull().editions
            editions.first { it.id == ownedId }.owned shouldBe true
            editions.first { it.id == unownedId }.owned shouldBe false
            job.cancel()
        }

        @Test
        fun `does not change state when the book is not stored locally`() =
            runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val remoteBook = buildBook(id = 1)
            stateFlow.value = buildBookDetailUiState().copy(book = remoteBook)
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            userBooksFlow.emit(null)

            // ----- Assert -----
            stateFlow.value.book shouldBe remoteBook
            job.cancel()
        }

        @Test
        fun `observes the new book when state book id changes to a different book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val firstBook = buildBook(id = 100)
            val secondLocalUserBook = buildUserBook()
            val secondBook = buildBook(id = 200)
            val localSecondBook = buildBook(
                id = 200,
                userBook = secondLocalUserBook,
            )
            stateFlow.value = buildBookDetailUiState().copy(book = firstBook)
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            stateFlow.value = stateFlow.value.copy(book = secondBook)
            userBooksFlow.emit(localSecondBook)

            // ----- Assert -----
            verify {
                observeUserBookUseCase(id = 200)
            }
            stateFlow.value.book.shouldNotBeNull().userBook shouldBe secondLocalUserBook
            job.cancel()
        }

        @Test
        fun `skips setState when the overlaid book equals the current state book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val localUserBook = buildUserBook()
            val currentBook = buildBook(
                id = 10,
                userBook = localUserBook,
            )
            val localBook = buildBook(
                id = 10,
                userBook = localUserBook,
            )
            stateFlow.value = buildBookDetailUiState().copy(
                book = currentBook,
                pendingShelfStatus = BookStatus.Reading,
            )
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            userBooksFlow.emit(localBook)

            // ----- Assert -----
            stateFlow.value.pendingShelfStatus shouldBe BookStatus.Reading
            job.cancel()
        }

        @Test
        fun `retires pendingShelfStatus when the overlay brings in a user book`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val currentBook = buildBook(id = 10)
            val localBook = buildBook(
                id = 10,
                userBook = buildUserBook(),
            )
            stateFlow.value = buildBookDetailUiState().copy(
                book = currentBook,
                pendingShelfStatus = BookStatus.Reading,
            )
            val collector = UserBooksFlowCollector()
            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            userBooksFlow.emit(localBook)

            // ----- Assert -----
            stateFlow.value.pendingShelfStatus shouldBe null
            job.cancel()
        }
    }
}
