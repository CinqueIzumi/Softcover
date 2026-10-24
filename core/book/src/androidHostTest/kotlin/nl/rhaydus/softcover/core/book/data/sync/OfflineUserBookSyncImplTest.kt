package nl.rhaydus.softcover.core.book.data.sync

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import nl.rhaydus.platform.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.database.mapper.toJson
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OfflineUserBookSyncImplTest {
    private lateinit var userBookWriteQueue: UserBookWriteQueue
    private lateinit var userBookWriteDrainer: UserBookWriteDrainer
    private lateinit var booksLocalDataSource: BooksLocalDataSource
    private lateinit var userBookCacheGate: UserBookCacheGate
    private lateinit var networkAvailability: NetworkAvailabilityProvider
    private lateinit var isOnline: MutableStateFlow<Boolean>
    private lateinit var syncImpl: OfflineUserBookSyncImpl

    @BeforeEach
    fun setUp() {
        userBookWriteQueue = mockk(relaxed = true)
        userBookWriteDrainer = mockk(relaxed = true)
        booksLocalDataSource = mockk(relaxed = true)
        userBookCacheGate = UserBookCacheGate(booksLocalDataSource = booksLocalDataSource)
        networkAvailability = mockk()
        isOnline = MutableStateFlow(true)

        every {
            networkAvailability.isOnline
        } returns isOnline

        coEvery {
            userBookWriteQueue.hasPending()
        } returns true

        // A scope on a scheduler nobody advances: the retry job is scheduled but never runs, so the
        // tests that are not about retries are unaffected by it.
        syncImpl = createSyncImpl(scope = CoroutineScope(StandardTestDispatcher()))
    }

    private fun createSyncImpl(scope: CoroutineScope): OfflineUserBookSyncImpl = OfflineUserBookSyncImpl(
        userBookWriteQueue = userBookWriteQueue,
        userBookWriteDrainer = userBookWriteDrainer,
        booksLocalDataSource = booksLocalDataSource,
        userBookCacheGate = userBookCacheGate,
        networkAvailability = networkAvailability,
        applicationScope = ApplicationScope(scope = scope),
    )

    private suspend fun OfflineUserBookSyncImpl.drainAndReconcile(
        fetchFromServer: suspend () -> List<Book>,
    ): List<Book> = drainReconcileAndCache(
        fetchFromServer = fetchFromServer,
        selectBooksToCache = { it },
    )

    private fun TestScope.advanceBy(millis: Long) {
        advanceTimeBy(millis)
        runCurrent()
    }

    // ---------------------------------------------------------------------------
    // Fixture helpers
    // ---------------------------------------------------------------------------

    private fun stubUserBook(
        id: Int,
        editionId: Int? = null,
    ): UserBook = UserBook(
        id = id,
        status = BookStatus.Reading,
        dateAdded = "2026-01-01",
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = editionId,
        lastReadDate = null,
        rating = null,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = emptyList(),
        reads = emptyList(),
    )

    private fun stubUserBookRead(
        id: Int,
        currentPage: Int = 50,
        currentSeconds: Int? = null,
    ): UserBookRead = UserBookRead(
        id = id,
        currentPage = currentPage,
        currentSeconds = currentSeconds,
        progress = 0.5f,
        startedAt = "2026-01-01",
        finishedAt = null,
    )

    private fun stubBook(
        userBook: UserBook? = null,
        userBookRead: UserBookRead? = null,
    ): Book = Book(
        id = 100,
        title = "Test Book",
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = userBook?.copy(reads = userBook.reads + listOfNotNull(userBookRead)),
    )

    // ---------------------------------------------------------------------------
    // enqueueProgressUpdate
    // ---------------------------------------------------------------------------

    @Nested
    inner class EnqueueProgressUpdate {
        @Test
        fun `happy path — enqueue called with UPDATE_PROGRESS kind and correct fields`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 10)
            val userBookRead = stubUserBookRead(
                id = 20,
                currentPage = 100,
            )
            val book = stubBook(
                userBook = userBook.copy(reads = listOf(userBookRead)),
            )

            // ----- Act -----
            syncImpl.enqueueProgressUpdate(
                book = book,
                newPage = 150,
                newSeconds = 120,
                actionAt = "2026-07-21T21:00:00Z",
            )

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_PROGRESS &&
                            write.userBookId == 10 &&
                            write.userBookReadId == 20 &&
                            write.progressPages == 150 &&
                            write.progressSeconds == 120 &&
                            write.actionAt == "2026-07-21T21:00:00Z"
                    },
                )
            }
        }

        @Test
        fun `progressHistoryId is carried into the enqueued payload`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                userBook = stubUserBook(id = 10),
                userBookRead = stubUserBookRead(id = 20),
            )

            // ----- Act -----
            syncImpl.enqueueProgressUpdate(
                book = book,
                newPage = 150,
                newSeconds = null,
                actionAt = "2026-07-21T21:00:00Z",
                progressHistoryId = 77L,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_PROGRESS &&
                            write.progressHistoryId == 77L
                    },
                )
            }
        }

        @Test
        fun `payload has no progressHistoryId when none is passed`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                userBook = stubUserBook(id = 10),
                userBookRead = stubUserBookRead(id = 20),
            )

            // ----- Act -----
            syncImpl.enqueueProgressUpdate(
                book = book,
                newPage = 150,
                newSeconds = null,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                userBookWriteQueue.enqueue(
                    payload = match { write -> write.progressHistoryId == null },
                )
            }
        }

        @Test
        fun `book with no userBook — throws IllegalStateException and enqueues nothing`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null

                every {
                    this@mockk.currentRead
                } returns stubUserBookRead(id = 1)
            }

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                syncImpl.enqueueProgressUpdate(
                    book = book,
                    newPage = 10,
                    newSeconds = null,
                )
            }

            coVerify(exactly = 0) { userBookWriteQueue.enqueue(payload = any()) }
        }

        @Test
        fun `book with no userBookRead — throws IllegalStateException and enqueues nothing`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns stubUserBook(id = 5)

                every {
                    this@mockk.currentRead
                } returns null
            }

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                syncImpl.enqueueProgressUpdate(
                    book = book,
                    newPage = 10,
                    newSeconds = null,
                )
            }

            coVerify(exactly = 0) { userBookWriteQueue.enqueue(payload = any()) }
        }

        @Test
        fun `placeholder read — enqueues the negative read id with null startedAt and finishedAt`() = runTest {
            // ----- Arrange -----
            val placeholderRead = UserBookRead(
                id = -10,
                currentPage = null,
                currentSeconds = null,
                progress = 0f,
                startedAt = "2026-07-21",
                finishedAt = "2026-07-22",
            )
            val book = stubBook(
                userBook = stubUserBook(id = 10),
                userBookRead = placeholderRead,
            )

            // ----- Act -----
            syncImpl.enqueueProgressUpdate(
                book = book,
                newPage = 30,
                newSeconds = null,
                actionAt = "2026-07-21T21:00:00Z",
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_PROGRESS &&
                            write.userBookReadId == -10 &&
                            write.startedAt == null &&
                            write.finishedAt == null &&
                            write.progressPages == 30
                    },
                )
            }
        }

        @Test
        fun `real read — enqueues the read dates unchanged`() = runTest {
            // ----- Arrange -----
            val realRead = UserBookRead(
                id = 20,
                currentPage = 10,
                currentSeconds = null,
                progress = 0.1f,
                startedAt = "2026-07-21",
                finishedAt = null,
            )
            val book = stubBook(
                userBook = stubUserBook(id = 10),
                userBookRead = realRead,
            )

            // ----- Act -----
            syncImpl.enqueueProgressUpdate(
                book = book,
                newPage = 30,
                newSeconds = null,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.userBookReadId == 20 && write.startedAt == "2026-07-21"
                    },
                )
            }
        }
    }

    // ---------------------------------------------------------------------------
    // enqueueMarkAsRead
    // ---------------------------------------------------------------------------

    @Nested
    inner class EnqueueMarkAsRead {
        @Test
        fun `happy path — enqueue called with MARK_AS_READ kind and progress fields from userBookRead`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 10)
            val userBookRead = stubUserBookRead(
                id = 20,
                currentPage = 300,
            )
            val book = stubBook(
                userBook = userBook.copy(reads = listOf(userBookRead)),
            )

            // ----- Act -----
            syncImpl.enqueueMarkAsRead(
                book = book,
                actionAt = "2026-07-21T21:00:00Z",
            )

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.MARK_AS_READ &&
                            write.userBookId == 10 &&
                            write.userBookReadId == 20 &&
                            write.progressPages == 300 &&
                            write.progressSeconds == userBookRead.currentSeconds &&
                            write.actionAt == "2026-07-21T21:00:00Z"
                    },
                )
            }
        }

        @Test
        fun `book with no userBook — throws IllegalStateException and enqueues nothing`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null

                every {
                    this@mockk.currentRead
                } returns stubUserBookRead(id = 1)
            }

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                syncImpl.enqueueMarkAsRead(book = book)
            }

            coVerify(exactly = 0) { userBookWriteQueue.enqueue(payload = any()) }
        }

        @Test
        fun `book with no userBookRead — enqueues MARK_AS_READ with read id 0 and null read fields`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns stubUserBook(id = 5)

                every {
                    this@mockk.currentRead
                } returns null
            }

            // ----- Act -----
            syncImpl.enqueueMarkAsRead(book = book)

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.MARK_AS_READ &&
                            write.userBookId == 5 &&
                            write.userBookReadId == 0 &&
                            write.progressPages == null &&
                            write.progressSeconds == null &&
                            write.startedAt == null &&
                            write.finishedAt == null
                    },
                )
            }
        }
    }

    // ---------------------------------------------------------------------------
    // enqueueRatingUpdate
    // ---------------------------------------------------------------------------

    @Nested
    inner class EnqueueStatusUpdate {
        @Test
        fun `happy path — enqueue called with SET_STATUS kind, user book id and status code`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = stubUserBook(id = 10))

            // ----- Act -----
            syncImpl.enqueueStatusUpdate(
                book = book,
                status = UserBookStatus.PAUSED,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.SET_STATUS &&
                            write.userBookId == 10 &&
                            write.bookId == book.id &&
                            write.statusCode == UserBookStatus.PAUSED.code
                    },
                )
            }
        }

        @Test
        fun `book without a userBookRead — still enqueues with the placeholder read id`() = runTest {
            // ----- Arrange -----
            val book = stubBook(
                userBook = stubUserBook(id = 10),
            )

            // ----- Act -----
            syncImpl.enqueueStatusUpdate(
                book = book,
                status = UserBookStatus.WANT_TO_READ,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.SET_STATUS &&
                            write.userBookReadId == 0 &&
                            write.statusCode == UserBookStatus.WANT_TO_READ.code
                    },
                )
            }
        }

        @Test
        fun `book with no userBook — throws IllegalStateException and enqueues nothing`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBook = null)

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                syncImpl.enqueueStatusUpdate(
                    book = book,
                    status = UserBookStatus.CURRENTLY_READING,
                )
            }

            coVerify(exactly = 0) { userBookWriteQueue.enqueue(payload = any()) }
        }
    }

    @Nested
    inner class EnqueueRatingUpdate {
        @Test
        fun `happy path with userBookRead present — enqueue called with UPDATE_RATING and userBookRead id`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 10)
            val userBookRead = stubUserBookRead(id = 20)
            val book = stubBook(
                userBook = userBook.copy(reads = listOf(userBookRead)),
            )

            // ----- Act -----
            syncImpl.enqueueRatingUpdate(
                book = book,
                rating = 4.5,
            )

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_RATING &&
                            write.userBookId == 10 &&
                            write.userBookReadId == 20 &&
                            write.rating == 4.5
                    },
                )
            }
        }

        @Test
        fun `userBookRead is null — enqueue still called with placeholder userBookReadId of 0`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 10)
            val book = stubBook(
                userBook = userBook,
            )

            // ----- Act -----
            syncImpl.enqueueRatingUpdate(
                book = book,
                rating = 3.0,
            )

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_RATING &&
                            write.userBookReadId == 0 &&
                            write.rating == 3.0
                    },
                )
            }
        }

        @Test
        fun `book with no userBook — throws IllegalStateException and enqueues nothing`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null
            }

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                syncImpl.enqueueRatingUpdate(
                    book = book,
                    rating = 5.0,
                )
            }

            coVerify(exactly = 0) { userBookWriteQueue.enqueue(payload = any()) }
        }
    }

    // ---------------------------------------------------------------------------
    // enqueueReviewUpdate
    // ---------------------------------------------------------------------------

    @Nested
    inner class EnqueueReviewUpdate {
        private val review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great read")))))
        private val hasSpoilers = true

        @Test
        fun `happy path with userBookRead present — enqueue called with UPDATE_REVIEW, correct json and hasSpoilers`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 10)
            val userBookRead = stubUserBookRead(id = 20)
            val book = stubBook(
                userBook = userBook.copy(reads = listOf(userBookRead)),
            )

            // ----- Act -----
            syncImpl.enqueueReviewUpdate(
                book = book,
                review = review,
                hasSpoilers = hasSpoilers,
            )

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_REVIEW &&
                            write.userBookId == 10 &&
                            write.userBookReadId == 20 &&
                            write.reviewSlateJson == review.toJson() &&
                            write.reviewHasSpoilers == hasSpoilers
                    },
                )
            }
        }

        @Test
        fun `userBookRead is null — enqueue still called with placeholder userBookReadId of 0`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 10)
            val book = stubBook(
                userBook = userBook,
            )

            // ----- Act -----
            syncImpl.enqueueReviewUpdate(
                book = book,
                review = review,
                hasSpoilers = false,
            )

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    payload = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_REVIEW &&
                            write.userBookReadId == 0 &&
                            write.reviewSlateJson == review.toJson() &&
                            write.reviewHasSpoilers == false
                    },
                )
            }
        }

        @Test
        fun `book with no userBook — throws IllegalStateException and enqueues nothing`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null
            }

            // ----- Act & Assert -----
            shouldThrow<IllegalStateException> {
                syncImpl.enqueueReviewUpdate(
                    book = book,
                    review = review,
                    hasSpoilers = false,
                )
            }

            coVerify(exactly = 0) { userBookWriteQueue.enqueue(payload = any()) }
        }
    }

    // ---------------------------------------------------------------------------
    // drainAndReconcile
    // ---------------------------------------------------------------------------

    @Nested
    inner class DrainAndReconcile {
        @Test
        fun `SET_STATUS — status preserved from local snapshot, other fields flow from server`() = runTest {
            // ----- Arrange -----
            val syncedUserBookId = 61
            val localUserBook = stubUserBook(id = syncedUserBookId).copy(status = BookStatus.Paused)
            val serverUserBook = stubUserBook(id = syncedUserBookId).copy(
                status = BookStatus.Reading,
                rating = 4.5,
            )
            val fetchedBook = stubBook(userBook = serverUserBook)
            val localBook = fetchedBook.copy(userBook = localUserBook)

            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(
                syncedUserBookId to setOf(PendingUserBookWriteKind.SET_STATUS),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            val reconciled = result.first().userBook.shouldNotBeNull()
            reconciled.status shouldBe BookStatus.Paused
            reconciled.rating shouldBe 4.5
        }

        @Test
        fun `SET_STATUS — fetched book without a userBook is returned unchanged`() = runTest {
            // ----- Arrange -----
            val syncedUserBookId = 62
            val fetchedBook = stubBook(userBook = null)
            val localBook = fetchedBook.copy(
                userBook = stubUserBook(id = syncedUserBookId).copy(status = BookStatus.Paused),
            )

            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(
                syncedUserBookId to setOf(PendingUserBookWriteKind.SET_STATUS),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            result shouldBe listOf(fetchedBook)
        }

        @Test
        fun `drain is called before the fetchFromServer lambda`() = runTest {
            // ----- Arrange -----
            val order = mutableListOf<String>()

            coEvery {
                userBookWriteDrainer.drain()
            } coAnswers {
                order += "drain"
                emptyMap()
            }

            // ----- Act -----
            syncImpl.drainAndReconcile {
                order += "fetch"
                emptyList()
            }

            // ----- Assert -----
            order shouldBe listOf("drain", "fetch")
        }

        @Test
        fun `empty syncedWrites — returns fetched list as-is and never accesses booksLocalDataSource`() = runTest {
            // ----- Arrange -----
            val fetchedBook = stubBook(userBook = stubUserBook(id = 1))
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            result shouldBe listOf(fetchedBook)

            coVerify(exactly = 0) { booksLocalDataSource.allUserBooks }
        }

        @Test
        fun `non-empty syncedWrites but no matching local snapshot — returns fetched list unchanged`() = runTest {
            // ----- Arrange -----
            val syncedUserBookId = 99
            val fetchedBook = stubBook(userBook = stubUserBook(id = 10)) // different userBookId

            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(
                syncedUserBookId to setOf(PendingUserBookWriteKind.UPDATE_RATING),
            )

            // Local cache has a book with a userBookId NOT in syncedWrites
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(fetchedBook))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            result shouldBe listOf(fetchedBook)
        }

        @Test
        fun `UPDATE_PROGRESS — owned fields come from local snapshot, non-owned rating flows from server`() = runTest {
            // ----- Arrange -----
            val syncedUserBookId = 42

            val localUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Reading,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = 2.0, // NOT owned by UPDATE_PROGRESS — server wins
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )
            val localRead = UserBookRead(
                id = 1,
                currentPage = 200,
                currentSeconds = null,
                progress = 80f,
                startedAt = "2026-01-01",
                finishedAt = null,
            )

            val serverUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Reading,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = 4.5, // server-of-record rating
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )
            val serverRead = UserBookRead(
                id = 1,
                currentPage = 50, // stale — local (200) should win
                currentSeconds = null,
                progress = 20f, // stale — local (80f) should win
                startedAt = null,
                finishedAt = null,
            )

            val fetchedBook = stubBook(
                userBook = serverUserBook.copy(reads = listOf(serverRead)),
            )
            val localBook = fetchedBook.copy(
                userBook = localUserBook.copy(reads = listOf(localRead)),
            )

            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(
                syncedUserBookId to setOf(PendingUserBookWriteKind.UPDATE_PROGRESS),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            val cached = result.first()
            cached.currentRead?.currentPage shouldBe 200
            cached.currentRead?.progress shouldBe 80f
            cached.userBook?.rating shouldBe 4.5
        }

        @Test
        fun `UPDATE_PROGRESS — fetched book has null userBookRead but snapshot has one — snapshot row reinstated`() = runTest {
            // ----- Arrange -----
            val syncedUserBookId = 43

            val localUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Reading,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = null,
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )
            val localRead = UserBookRead(
                id = 5,
                currentPage = 200,
                currentSeconds = null,
                progress = 80f,
                startedAt = "2026-01-01",
                finishedAt = null,
            )

            val serverUserBook = localUserBook.copy()

            // Server fetch has no userBookRead row yet (lag)
            val fetchedBook = stubBook(
                userBook = serverUserBook,
            )
            val localBook = fetchedBook.copy(
                userBook = localUserBook.copy(reads = listOf(localRead)),
            )

            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(
                syncedUserBookId to setOf(PendingUserBookWriteKind.UPDATE_PROGRESS),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            val cached = result.first()
            cached.currentRead?.currentPage shouldBe 200
            cached.currentRead?.progress shouldBe 80f
        }

        @Test
        fun `UPDATE_RATING — rating preserved from local, progress fields flow from server`() = runTest {
            // ----- Arrange -----
            val syncedUserBookId = 55

            val localUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Read,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = 5.0, // owned — should be preserved
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )
            val localRead = UserBookRead(
                id = 1,
                currentPage = 100,
                currentSeconds = null,
                progress = 50f,
                startedAt = null,
                finishedAt = null,
            )

            val serverUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Read,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = 1.0, // stale — local (5.0) should win
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )
            val serverRead = UserBookRead(
                id = 1,
                currentPage = 350, // server progress — should flow through
                currentSeconds = null,
                progress = 100f,
                startedAt = null,
                finishedAt = null,
            )

            val fetchedBook = stubBook(
                userBook = serverUserBook.copy(reads = listOf(serverRead)),
            )
            val localBook = fetchedBook.copy(
                userBook = localUserBook.copy(reads = listOf(localRead)),
            )

            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(
                syncedUserBookId to setOf(PendingUserBookWriteKind.UPDATE_RATING),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            val cached = result.first()
            cached.userBook?.rating shouldBe 5.0
            cached.currentRead?.currentPage shouldBe 350
            cached.currentRead?.progress shouldBe 100f
        }

        @Test
        fun `MARK_AS_READ — status and finishedAt preserved, non-owned rating flows from server`() = runTest {
            // ----- Arrange -----
            val syncedUserBookId = 66

            val localUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Read, // owned — should be preserved
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = 3.0, // NOT owned — server wins
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )
            val localRead = UserBookRead(
                id = 1,
                currentPage = 300,
                currentSeconds = null,
                progress = 100f, // owned — preserved
                startedAt = null,
                finishedAt = "2026-05-01", // owned — preserved
            )

            val serverUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.WantToRead, // stale — local (Read) should win
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = 4.8, // server-of-record
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )
            val serverRead = UserBookRead(
                id = 1,
                currentPage = 0,
                currentSeconds = null,
                progress = 0f,
                startedAt = null,
                finishedAt = null, // stale — local should win
            )

            val fetchedBook = stubBook(
                userBook = serverUserBook.copy(reads = listOf(serverRead)),
            )
            val localBook = fetchedBook.copy(
                userBook = localUserBook.copy(reads = listOf(localRead)),
            )

            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(
                syncedUserBookId to setOf(PendingUserBookWriteKind.MARK_AS_READ),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            val cached = result.first()
            cached.userBook?.status shouldBe BookStatus.Read
            cached.currentRead?.finishedAt shouldBe "2026-05-01"
            cached.currentRead?.progress shouldBe 100f
            cached.userBook?.rating shouldBe 4.8
        }

        @Test
        fun `UPDATE_REVIEW — reviewDocument and reviewHasSpoilers preserved, non-owned rating flows from server`() = runTest {
            // ----- Arrange -----
            val syncedUserBookId = 77
            val localReview = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Local review")))))

            val localUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Read,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = true, // owned — preserved
                editionId = null,
                lastReadDate = null,
                rating = 3.5, // NOT owned — server wins
                reviewDocument = localReview, // owned — preserved
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )

            val serverUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Read,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false, // stale
                editionId = null,
                lastReadDate = null,
                rating = 4.0, // server-of-record
                reviewDocument = null, // stale
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )

            val fetchedBook = stubBook(
                userBook = serverUserBook,
            )
            val localBook = fetchedBook.copy(userBook = localUserBook)

            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(
                syncedUserBookId to setOf(PendingUserBookWriteKind.UPDATE_REVIEW),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            val cached = result.first()
            cached.userBook?.reviewDocument shouldBe localReview
            cached.userBook?.reviewHasSpoilers shouldBe true
            cached.userBook?.rating shouldBe 4.0
        }

        @Test
        fun `DC1 discriminator — book not in syncedWrites passes through unchanged even when local has different values`() = runTest {
            // ----- Arrange -----
            val syncedUserBookId = 10
            val otherUserBookId = 99 // not in syncedWrites

            val localUserBookForSynced = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Reading,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = 2.0,
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )

            val serverUserBookForOther = UserBook(
                id = otherUserBookId,
                status = BookStatus.Read,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = 4.0, // server value — should NOT be overwritten
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )

            val syncedFetchedBook = stubBook(
                userBook = UserBook(
                    id = syncedUserBookId,
                    status = BookStatus.Reading,
                    dateAdded = "2026-01-01",
                    createdAt = null,
                    privacySettingId = 1,
                    reviewHasSpoilers = false,
                    editionId = null,
                    lastReadDate = null,
                    rating = 3.5,
                    referrerUserId = null,
                    reviewedAt = null,
                    updatedAt = null,
                    journals = emptyList(),
                    reads = emptyList(),
                ),
            )
            val otherFetchedBook = stubBook(userBook = serverUserBookForOther)

            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(
                syncedUserBookId to setOf(PendingUserBookWriteKind.UPDATE_RATING),
            )

            val localBookForSynced = syncedFetchedBook.copy(userBook = localUserBookForSynced)
            // Local also has a different-rated entry for otherUserBookId, but it should be ignored
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBookForSynced))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(
                fetchFromServer = { listOf(syncedFetchedBook, otherFetchedBook) },
            )

            // ----- Assert -----
            val other = result.first { it.userBook?.id == otherUserBookId }
            other.userBook?.rating shouldBe 4.0 // unchanged from server
        }

        @Test
        fun `multiple kinds on same book — both UPDATE_RATING and UPDATE_REVIEW applied`() = runTest {
            // ----- Arrange -----
            val syncedUserBookId = 88
            val localReview = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Local review")))))

            val localUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Read,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = true,
                editionId = null,
                lastReadDate = null,
                rating = 5.0, // owned by UPDATE_RATING
                reviewDocument = localReview, // owned by UPDATE_REVIEW
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )

            val serverUserBook = UserBook(
                id = syncedUserBookId,
                status = BookStatus.Read,
                dateAdded = "2026-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = 1.0, // stale
                reviewDocument = null, // stale
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )

            val fetchedBook = stubBook(
                userBook = serverUserBook,
            )
            val localBook = fetchedBook.copy(userBook = localUserBook)

            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(
                syncedUserBookId to setOf(
                    PendingUserBookWriteKind.UPDATE_RATING,
                    PendingUserBookWriteKind.UPDATE_REVIEW,
                ),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            val cached = result.first()
            cached.userBook?.rating shouldBe 5.0
            cached.userBook?.reviewDocument shouldBe localReview
            cached.userBook?.reviewHasSpoilers shouldBe true
        }
    }

    @Nested
    inner class ReconcileReads {
        private val userBookId = 55

        private fun read(
            id: Int,
            page: Int? = null,
            progress: Float = 0f,
            startedAt: String? = null,
            finishedAt: String? = null,
        ): UserBookRead = UserBookRead(
            id = id,
            currentPage = page,
            currentSeconds = null,
            progress = progress,
            startedAt = startedAt,
            finishedAt = finishedAt,
        )

        private fun bookWith(
            status: BookStatus,
            vararg reads: UserBookRead,
        ): Book = stubBook(
            userBook = stubUserBook(id = userBookId).copy(
                status = status,
                reads = reads.toList(),
            ),
        )

        private suspend fun reconcile(
            kind: PendingUserBookWriteKind,
            snapshot: Book,
            fetched: Book,
        ): Book {
            coEvery {
                userBookWriteDrainer.drain()
            } returns mapOf(userBookId to setOf(kind))
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(snapshot))

            return syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetched) }).first()
        }

        @Test
        fun `UPDATE_PROGRESS — snapshot values apply only to the fetched read with the same id`() = runTest {
            // ----- Arrange -----
            val snapshot = bookWith(
                BookStatus.Reading,
                read(
                    id = 10,
                    page = 80,
                    progress = 80f,
                    startedAt = "2026-02-01",
                ),
                read(
                    id = 5,
                    page = 100,
                    progress = 100f,
                    finishedAt = "2025-01-01",
                ),
            )
            val staleCurrent = read(
                id = 10,
                page = 20,
                progress = 20f,
                startedAt = "2026-02-02",
            )
            val olderRead = read(
                id = 5,
                page = 90,
                progress = 90f,
                finishedAt = "2025-01-02",
            )
            val fetched = bookWith(
                BookStatus.Reading,
                staleCurrent,
                olderRead,
            )

            // ----- Act -----
            val result = reconcile(
                PendingUserBookWriteKind.UPDATE_PROGRESS,
                snapshot,
                fetched,
            )

            // ----- Assert -----
            val reads = result.userBook.shouldNotBeNull().reads
            reads.first { it.id == 10 } shouldBe read(
                id = 10,
                page = 80,
                progress = 80f,
                startedAt = "2026-02-01",
            )
            reads.first { it.id == 5 } shouldBe olderRead
        }

        @Test
        fun `UPDATE_PROGRESS — a genuine snapshot read missing from the fetch is appended`() = runTest {
            // ----- Arrange -----
            val snapshotCurrent = read(
                id = 10,
                page = 80,
                progress = 80f,
                startedAt = "2026-02-01",
            )
            val snapshot = bookWith(
                BookStatus.Reading,
                snapshotCurrent,
            )
            val olderRead = read(
                id = 5,
                progress = 100f,
                finishedAt = "2025-01-01",
            )
            val fetched = bookWith(
                BookStatus.Reading,
                olderRead,
            )

            // ----- Act -----
            val result = reconcile(
                PendingUserBookWriteKind.UPDATE_PROGRESS,
                snapshot,
                fetched,
            )

            // ----- Assert -----
            result.userBook.shouldNotBeNull().reads shouldBe listOf(olderRead, snapshotCurrent)
        }

        @Test
        fun `UPDATE_PROGRESS — a placeholder snapshot read missing from the fetch is not appended`() = runTest {
            // ----- Arrange -----
            val snapshot = bookWith(
                BookStatus.Reading,
                read(
                    id = -userBookId,
                    page = 30,
                    progress = 30f,
                    startedAt = "2026-02-01",
                ),
            )
            val olderRead = read(
                id = 5,
                progress = 100f,
                finishedAt = "2025-01-01",
            )
            val serverCurrent = read(
                id = 9,
                startedAt = "2026-02-01",
            )
            val fetched = bookWith(
                BookStatus.Reading,
                olderRead,
                serverCurrent,
            )

            // ----- Act -----
            val result = reconcile(
                PendingUserBookWriteKind.UPDATE_PROGRESS,
                snapshot,
                fetched,
            )

            // ----- Assert -----
            result.userBook.shouldNotBeNull().reads shouldBe listOf(olderRead, serverCurrent)
        }

        @Test
        fun `UPDATE_PROGRESS — a fetched read with a different id is never overwritten`() = runTest {
            // ----- Arrange -----
            val snapshotCurrent = read(
                id = 10,
                page = 80,
                progress = 80f,
                startedAt = "2026-02-01",
            )
            val snapshot = bookWith(
                BookStatus.Reading,
                snapshotCurrent,
            )
            val otherRead = read(
                id = 11,
                page = 5,
                progress = 5f,
                startedAt = "2026-03-01",
            )
            val fetched = bookWith(
                BookStatus.Reading,
                otherRead,
            )

            // ----- Act -----
            val result = reconcile(
                PendingUserBookWriteKind.UPDATE_PROGRESS,
                snapshot,
                fetched,
            )

            // ----- Assert -----
            val reads = result.userBook.shouldNotBeNull().reads
            reads.first { it.id == 11 } shouldBe otherRead
        }

        @Test
        fun `MARK_AS_READ — progress and finishedAt apply only to the fetched read with the same id`() = runTest {
            // ----- Arrange -----
            val snapshot = bookWith(
                BookStatus.Read,
                read(
                    id = 10,
                    page = 100,
                    progress = 100f,
                    startedAt = "2026-02-01",
                    finishedAt = "2026-05-01",
                ),
            )
            val staleCurrent = read(
                id = 10,
                page = 40,
                progress = 40f,
                startedAt = "2026-02-01",
            )
            val otherRead = read(
                id = 9,
                page = 7,
                progress = 7f,
                startedAt = "2025-02-01",
            )
            val fetched = bookWith(
                BookStatus.Reading,
                staleCurrent,
                otherRead,
            )

            // ----- Act -----
            val result = reconcile(
                PendingUserBookWriteKind.MARK_AS_READ,
                snapshot,
                fetched,
            )

            // ----- Assert -----
            val userBook = result.userBook.shouldNotBeNull()
            userBook.status shouldBe BookStatus.Read
            userBook.reads.first { it.id == 10 } shouldBe staleCurrent.copy(
                progress = 100f,
                finishedAt = "2026-05-01",
            )
            userBook.reads.first { it.id == 9 } shouldBe otherRead
        }

        @Test
        fun `MARK_AS_READ — a fetched read with a different id is never overwritten`() = runTest {
            // ----- Arrange -----
            val snapshot = bookWith(
                BookStatus.Read,
                read(
                    id = 10,
                    progress = 100f,
                    finishedAt = "2026-05-01",
                ),
            )
            val otherRead = read(
                id = 11,
                page = 5,
                progress = 5f,
                startedAt = "2026-03-01",
            )
            val fetched = bookWith(
                BookStatus.Reading,
                otherRead,
            )

            // ----- Act -----
            val result = reconcile(
                PendingUserBookWriteKind.MARK_AS_READ,
                snapshot,
                fetched,
            )

            // ----- Assert -----
            result.userBook.shouldNotBeNull().reads.first { it.id == 11 } shouldBe otherRead
        }

        @Test
        fun `SET_STATUS — a local placeholder read is kept when the fetched Reading book has only finished reads`() =
            runTest {
                // ----- Arrange -----
                val placeholder = read(
                    id = -1,
                    startedAt = "2026-06-01",
                )
                val finished = read(
                    id = 5,
                    progress = 100f,
                    finishedAt = "2025-01-01",
                )
                val snapshot = bookWith(
                    BookStatus.Reading,
                    placeholder,
                    finished,
                )
                val fetched = bookWith(
                    BookStatus.Reading,
                    finished,
                )

                // ----- Act -----
                val result = reconcile(
                    PendingUserBookWriteKind.SET_STATUS,
                    snapshot,
                    fetched,
                )

                // ----- Assert -----
                val userBook = result.userBook.shouldNotBeNull()
                userBook.reads shouldBe listOf(finished, placeholder)
                userBook.currentRead shouldBe placeholder
            }

        @Test
        fun `SET_STATUS — a local placeholder read is kept when the fetched book is Paused`() = runTest {
            // ----- Arrange -----
            val placeholder = read(
                id = -1,
                startedAt = "2026-06-01",
            )
            val finished = read(
                id = 5,
                progress = 100f,
                finishedAt = "2025-01-01",
            )
            val snapshot = bookWith(
                BookStatus.Paused,
                placeholder,
            )
            val fetched = bookWith(
                BookStatus.Paused,
                finished,
            )

            // ----- Act -----
            val result = reconcile(
                PendingUserBookWriteKind.SET_STATUS,
                snapshot,
                fetched,
            )

            // ----- Assert -----
            result.userBook.shouldNotBeNull().reads shouldBe listOf(finished, placeholder)
        }

        @Test
        fun `SET_STATUS — the placeholder is not added when the fetched book already has an unfinished read`() =
            runTest {
                // ----- Arrange -----
                val placeholder = read(
                    id = -1,
                    startedAt = "2026-06-01",
                )
                val snapshot = bookWith(
                    BookStatus.Reading,
                    placeholder,
                )
                val serverCurrent = read(
                    id = 9,
                    startedAt = "2026-06-01",
                )
                val fetched = bookWith(
                    BookStatus.Reading,
                    serverCurrent,
                )

                // ----- Act -----
                val result = reconcile(
                    PendingUserBookWriteKind.SET_STATUS,
                    snapshot,
                    fetched,
                )

                // ----- Assert -----
                result.userBook.shouldNotBeNull().reads shouldBe listOf(serverCurrent)
            }

        @Test
        fun `SET_STATUS — the placeholder is not added when the reconciled status is Read`() = runTest {
            // ----- Arrange -----
            val placeholder = read(
                id = -1,
                startedAt = "2026-06-01",
            )
            val snapshot = bookWith(
                BookStatus.Read,
                placeholder,
            )
            val finished = read(
                id = 5,
                progress = 100f,
                finishedAt = "2025-01-01",
            )
            val fetched = bookWith(
                BookStatus.Reading,
                finished,
            )

            // ----- Act -----
            val result = reconcile(
                PendingUserBookWriteKind.SET_STATUS,
                snapshot,
                fetched,
            )

            // ----- Assert -----
            val userBook = result.userBook.shouldNotBeNull()
            userBook.status shouldBe BookStatus.Read
            userBook.reads shouldBe listOf(finished)
        }
    }

    @Nested
    inner class RetryDrain {
        private val retryHintsUserBookId = 7

        private fun bookWithUserBook(): Book = stubBook(userBook = stubUserBook(id = retryHintsUserBookId))

        @Test
        fun `an enqueue while online drains once after the first delay and not before`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            advanceBy(millis = 29_999)

            // ----- Assert -----
            coVerify(exactly = 0) { userBookWriteDrainer.drain() }

            advanceBy(millis = 1)

            coVerify(exactly = 1) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `an enqueue while offline schedules no retry`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            isOnline.value = false

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            advanceBy(millis = 1_000_000)

            // ----- Assert -----
            coVerify(exactly = 0) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `a tick that lands while offline ends the schedule and leaves replay to the reconnect trigger`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            isOnline.value = false
            advanceBy(millis = 30_000)

            // ----- Assert -----
            coVerify(exactly = 0) { userBookWriteDrainer.drain() }

            isOnline.value = true
            advanceBy(millis = 1_000_000)

            coVerify(exactly = 0) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `the loop drains three times in total and then stops`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            advanceBy(millis = 1_000_000)

            // ----- Assert -----
            coVerify(exactly = 3) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `a second enqueue restarts the schedule so the first retry waits a full first delay from it`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            advanceBy(millis = 20_000)

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 5.0,
            )
            advanceBy(millis = 10_000)

            // ----- Assert -----
            coVerify(exactly = 0) { userBookWriteDrainer.drain() }

            advanceBy(millis = 19_999)

            coVerify(exactly = 0) { userBookWriteDrainer.drain() }

            advanceBy(millis = 1)

            coVerify(exactly = 1) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `a restarted schedule still drains three times in total`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            advanceBy(millis = 20_000)

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 5.0,
            )
            advanceBy(millis = 1_000_000)

            // ----- Assert -----
            coVerify(exactly = 3) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `a restart during an in-flight drain lets that drain finish`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            var completedDrains = 0
            coEvery {
                userBookWriteDrainer.drain()
            } coAnswers {
                delay(5_000)
                completedDrains++
                emptyMap()
            }
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            advanceBy(millis = 32_000)

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 5.0,
            )
            advanceBy(millis = 3_000)

            // ----- Assert -----
            completedDrains shouldBe 1
        }

        @Test
        fun `the loop stops when no write is pending after a drain`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()
            coEvery {
                userBookWriteQueue.hasPending()
            } returns false

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            advanceBy(millis = 1_000_000)

            // ----- Assert -----
            coVerify(exactly = 1) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `the loop continues while writes remain pending and stops once they are gone`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()
            coEvery {
                userBookWriteQueue.hasPending()
            } returnsMany listOf(true, false)

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            advanceBy(millis = 1_000_000)

            // ----- Assert -----
            coVerify(exactly = 2) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `concurrent enqueues produce a single schedule`() = runTest(StandardTestDispatcher()) {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()

            // ----- Act -----
            launch {
                impl.enqueueRatingUpdate(
                    book = bookWithUserBook(),
                    rating = 4.0,
                )
            }
            launch {
                impl.enqueueRatingUpdate(
                    book = bookWithUserBook(),
                    rating = 5.0,
                )
            }
            runCurrent()
            advanceBy(millis = 30_000)

            // ----- Assert -----
            coVerify(exactly = 1) { userBookWriteDrainer.drain() }

            advanceBy(millis = 1_000_000)

            coVerify(exactly = 3) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `an enqueue after the loop has finished starts a fresh loop`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            advanceBy(millis = 1_000_000)

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 5.0,
            )
            advanceBy(millis = 30_000)

            // ----- Assert -----
            coVerify(exactly = 4) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `a drain that throws does not stop the remaining retries`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            coEvery {
                userBookWriteDrainer.drain()
            } throws IllegalStateException("boom")

            // ----- Act -----
            impl.enqueueRatingUpdate(
                book = bookWithUserBook(),
                rating = 4.0,
            )
            advanceBy(millis = 1_000_000)

            // ----- Assert -----
            coVerify(exactly = 3) { userBookWriteDrainer.drain() }
        }

        @Test
        fun `hints from a retry drain are applied by the next drainAndReconcile and then cleared`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            val fetchedBook = stubBook(
                userBook = stubUserBook(id = retryHintsUserBookId).copy(status = BookStatus.Reading),
            )
            val localBook = stubBook(
                userBook = stubUserBook(id = retryHintsUserBookId).copy(status = BookStatus.Paused),
            )

            coEvery {
                userBookWriteDrainer.drain()
            } returnsMany listOf(
                mapOf(retryHintsUserBookId to setOf(PendingUserBookWriteKind.SET_STATUS)),
                emptyMap(),
                emptyMap(),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            impl.enqueueStatusUpdate(
                book = localBook,
                status = UserBookStatus.PAUSED,
            )
            advanceBy(millis = 30_000)

            // ----- Act -----
            val first = impl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })
            val second = impl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            first.first().userBook.shouldNotBeNull().status shouldBe BookStatus.Paused
            second.first().userBook.shouldNotBeNull().status shouldBe BookStatus.Reading
        }

        @Test
        fun `retry hints and the refresh drain hints for the same user book are merged`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            val fetchedBook = stubBook(
                userBook = stubUserBook(id = retryHintsUserBookId).copy(
                    status = BookStatus.Reading,
                    rating = 1.0,
                ),
            )
            val localBook = stubBook(
                userBook = stubUserBook(id = retryHintsUserBookId).copy(
                    status = BookStatus.Paused,
                    rating = 5.0,
                ),
            )

            coEvery {
                userBookWriteDrainer.drain()
            } returnsMany listOf(
                mapOf(retryHintsUserBookId to setOf(PendingUserBookWriteKind.SET_STATUS)),
                mapOf(retryHintsUserBookId to setOf(PendingUserBookWriteKind.UPDATE_RATING)),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            impl.enqueueStatusUpdate(
                book = localBook,
                status = UserBookStatus.PAUSED,
            )
            advanceBy(millis = 30_000)

            // ----- Act -----
            val result = impl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            val reconciled = result.first().userBook.shouldNotBeNull()
            reconciled.status shouldBe BookStatus.Paused
            reconciled.rating shouldBe 5.0
        }

        @Test
        fun `hints from consecutive retry drains accumulate until the next reconcile`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            val fetchedBook = stubBook(
                userBook = stubUserBook(id = retryHintsUserBookId).copy(
                    status = BookStatus.Reading,
                    rating = 1.0,
                ),
            )
            val localBook = stubBook(
                userBook = stubUserBook(id = retryHintsUserBookId).copy(
                    status = BookStatus.Paused,
                    rating = 5.0,
                ),
            )

            coEvery {
                userBookWriteDrainer.drain()
            } returnsMany listOf(
                mapOf(retryHintsUserBookId to setOf(PendingUserBookWriteKind.SET_STATUS)),
                mapOf(retryHintsUserBookId to setOf(PendingUserBookWriteKind.UPDATE_RATING)),
                emptyMap(),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            impl.enqueueStatusUpdate(
                book = localBook,
                status = UserBookStatus.PAUSED,
            )
            advanceBy(millis = 120_000)

            // ----- Act -----
            val result = impl.drainAndReconcile(fetchFromServer = { listOf(fetchedBook) })

            // ----- Assert -----
            val reconciled = result.first().userBook.shouldNotBeNull()
            reconciled.status shouldBe BookStatus.Paused
            reconciled.rating shouldBe 5.0
        }

        @Test
        fun `hints for different user books from the retry and the refresh drain are all applied`() = runTest {
            // ----- Arrange -----
            val impl = createSyncImpl(scope = backgroundScope)
            val otherUserBookId = 8
            val fetchedRetryBook = stubBook(
                userBook = stubUserBook(id = retryHintsUserBookId).copy(status = BookStatus.Reading),
            )
            val fetchedDrainBook = stubBook(
                userBook = stubUserBook(id = otherUserBookId).copy(status = BookStatus.Reading),
            )
            val localRetryBook = stubBook(
                userBook = stubUserBook(id = retryHintsUserBookId).copy(status = BookStatus.Paused),
            )
            val localDrainBook = stubBook(
                userBook = stubUserBook(id = otherUserBookId).copy(status = BookStatus.Paused),
            )

            coEvery {
                userBookWriteDrainer.drain()
            } returnsMany listOf(
                mapOf(retryHintsUserBookId to setOf(PendingUserBookWriteKind.SET_STATUS)),
                mapOf(otherUserBookId to setOf(PendingUserBookWriteKind.SET_STATUS)),
            )
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localRetryBook, localDrainBook))

            impl.enqueueStatusUpdate(
                book = localRetryBook,
                status = UserBookStatus.PAUSED,
            )
            advanceBy(millis = 30_000)

            // ----- Act -----
            val result = impl.drainAndReconcile(fetchFromServer = { listOf(fetchedRetryBook, fetchedDrainBook) })

            // ----- Assert -----
            result.map { it.userBook.shouldNotBeNull().status } shouldBe listOf(BookStatus.Paused, BookStatus.Paused)
        }
    }

    @Nested
    inner class WritesDuringFetch {
        private val userBookId = 77

        private fun read(
            id: Int,
            page: Int? = null,
            finishedAt: String? = null,
        ): UserBookRead = UserBookRead(
            id = id,
            currentPage = page,
            currentSeconds = null,
            progress = if (finishedAt != null) 1f else 0.1f,
            startedAt = "2026-01-01",
            finishedAt = finishedAt,
        )

        private fun bookWith(
            status: BookStatus,
            reads: List<UserBookRead> = emptyList(),
        ): Book = stubBook(
            userBook = stubUserBook(id = userBookId).copy(
                status = status,
                reads = reads,
            ),
        )

        private fun Book.withRating(rating: Double?): Book = copy(
            userBook = userBook.shouldNotBeNull().copy(rating = rating),
        )

        private suspend fun TestScope.reconcileWithWrite(
            local: Book,
            kind: PendingUserBookWriteKind,
            fetched: List<Book>,
        ): List<Book> {
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(local))

            val fetchGate = CompletableDeferred<List<Book>>()
            val refresh = async {
                syncImpl.drainAndReconcile(fetchFromServer = { fetchGate.await() })
            }
            runCurrent()

            userBookCacheGate.cacheBook(
                book = local,
                kind = kind,
            )
            fetchGate.complete(fetched)

            return refresh.await()
        }

        @Test
        fun `progress written during the fetch keeps the local page while another server change flows through`() = runTest {
            // ----- Arrange -----
            val local = bookWith(
                status = BookStatus.Reading,
                reads = listOf(
                    read(
                        id = 7,
                        page = 80,
                    ),
                ),
            )
            val fetched = bookWith(
                status = BookStatus.Reading,
                reads = listOf(
                    read(
                        id = 7,
                        page = 10,
                    ),
                ),
            ).withRating(rating = 4.5)

            // ----- Act -----
            val result = reconcileWithWrite(
                local = local,
                kind = PendingUserBookWriteKind.UPDATE_PROGRESS,
                fetched = listOf(fetched),
            )

            // ----- Assert -----
            val reconciled = result.single().userBook.shouldNotBeNull()
            reconciled.reads.single().currentPage shouldBe 80
            reconciled.rating shouldBe 4.5
        }

        @Test
        fun `without a recorded write the fetched page wins over the local one`() = runTest {
            // ----- Arrange -----
            val local = bookWith(
                status = BookStatus.Reading,
                reads = listOf(
                    read(
                        id = 7,
                        page = 80,
                    ),
                ),
            )
            val fetched = bookWith(
                status = BookStatus.Reading,
                reads = listOf(
                    read(
                        id = 7,
                        page = 10,
                    ),
                ),
            )
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()
            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(local))

            // ----- Act -----
            val result = syncImpl.drainAndReconcile(fetchFromServer = { listOf(fetched) })

            // ----- Assert -----
            result.single().userBook.shouldNotBeNull().reads.single().currentPage shouldBe 10
        }

        @Test
        fun `a book removed during the fetch is dropped from the result and not cached`() = runTest {
            // ----- Arrange -----
            val keptBook = bookWith(status = BookStatus.Reading)
            val removedBook = keptBook.copy(
                id = 200,
                userBook = keptBook.userBook.shouldNotBeNull().copy(id = 88),
            )
            coEvery {
                userBookWriteDrainer.drain()
            } returns emptyMap()

            val fetchGate = CompletableDeferred<List<Book>>()
            val refresh = async {
                syncImpl.drainAndReconcile(fetchFromServer = { fetchGate.await() })
            }
            runCurrent()

            // ----- Act -----
            userBookCacheGate.removeUserBooks(ids = listOf(88))
            fetchGate.complete(listOf(keptBook, removedBook))
            val result = refresh.await()

            // ----- Assert -----
            result shouldBe listOf(keptBook)
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBooks(books = listOf(keptBook))
            }
        }

        @Test
        fun `status set to Reading during a fetch with only a finished read keeps the snapshot's unfinished read as current`() = runTest {
            // ----- Arrange -----
            val local = bookWith(
                status = BookStatus.Reading,
                reads = listOf(
                    read(
                        id = 10,
                        finishedAt = "2025-05-05",
                    ),
                    read(id = 20),
                ),
            )
            val fetched = bookWith(
                status = BookStatus.WantToRead,
                reads = listOf(
                    read(
                        id = 10,
                        finishedAt = "2025-05-05",
                    ),
                ),
            )

            // ----- Act -----
            val result = reconcileWithWrite(
                local = local,
                kind = PendingUserBookWriteKind.SET_STATUS,
                fetched = listOf(fetched),
            )

            // ----- Assert -----
            val reconciled = result.single().userBook.shouldNotBeNull()
            reconciled.status shouldBe BookStatus.Reading
            reconciled.currentRead.shouldNotBeNull().id shouldBe 20
        }

        @Test
        fun `mark as read during a fetch lacking the snapshot's current read keeps that read with its finishedAt`() = runTest {
            // ----- Arrange -----
            val local = bookWith(
                status = BookStatus.Read,
                reads = listOf(
                    read(
                        id = 30,
                        finishedAt = "2026-02-02",
                    ),
                ),
            )
            val fetched = bookWith(
                status = BookStatus.Reading,
                reads = listOf(
                    read(
                        id = 10,
                        finishedAt = "2025-05-05",
                    ),
                ),
            )

            // ----- Act -----
            val result = reconcileWithWrite(
                local = local,
                kind = PendingUserBookWriteKind.MARK_AS_READ,
                fetched = listOf(fetched),
            )

            // ----- Assert -----
            val reconciled = result.single().userBook.shouldNotBeNull()
            reconciled.status shouldBe BookStatus.Read
            reconciled.reads.first { it.id == 30 }.finishedAt shouldBe "2026-02-02"
        }
    }
}
