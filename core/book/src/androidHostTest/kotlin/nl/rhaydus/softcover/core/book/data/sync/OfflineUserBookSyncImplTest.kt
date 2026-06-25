package nl.rhaydus.softcover.core.book.data.sync

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.database.mapper.toJson
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.UserBookWriteQueue
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OfflineUserBookSyncImplTest {
    private lateinit var userBookWriteQueue: UserBookWriteQueue
    private lateinit var userBookWriteDrainer: UserBookWriteDrainer
    private lateinit var booksLocalDataSource: BooksLocalDataSource
    private lateinit var syncImpl: OfflineUserBookSyncImpl

    @BeforeEach
    fun setUp() {
        userBookWriteQueue = mockk(relaxed = true)
        userBookWriteDrainer = mockk(relaxed = true)
        booksLocalDataSource = mockk(relaxed = true)

        syncImpl = OfflineUserBookSyncImpl(
            userBookWriteQueue = userBookWriteQueue,
            userBookWriteDrainer = userBookWriteDrainer,
            booksLocalDataSource = booksLocalDataSource,
        )
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
        userBook = userBook,
        userBookRead = userBookRead,
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
                userBook = userBook,
                userBookRead = userBookRead,
            )

            // ----- Act -----
            syncImpl.enqueueProgressUpdate(
                book = book,
                newPage = 150,
                newSeconds = null,
            )

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    update = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_PROGRESS &&
                            write.userBookId == 10 &&
                            write.userBookReadId == 20 &&
                            write.progressPages == 150 &&
                            write.progressSeconds == null
                    },
                )
            }
        }

        @Test
        fun `non-null newSeconds — progressSeconds maps through to the queued write`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 10)
            val userBookRead = stubUserBookRead(
                id = 20,
                currentPage = 100,
            )
            val book = stubBook(
                userBook = userBook,
                userBookRead = userBookRead,
            )

            // ----- Act -----
            syncImpl.enqueueProgressUpdate(
                book = book,
                newPage = 100,
                newSeconds = 120,
            )

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    update = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_PROGRESS &&
                            write.progressSeconds == 120
                    },
                )
            }
        }

        @Test
        fun `book with no userBook — enqueue is not called`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null

                every {
                    this@mockk.userBookRead
                } returns stubUserBookRead(id = 1)
            }

            // ----- Act -----
            syncImpl.enqueueProgressUpdate(
                book = book,
                newPage = 10,
                newSeconds = null,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { userBookWriteQueue.enqueue(update = any()) }
        }

        @Test
        fun `book with no userBookRead — enqueue is not called`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns stubUserBook(id = 5)

                every {
                    this@mockk.userBookRead
                } returns null
            }

            // ----- Act -----
            syncImpl.enqueueProgressUpdate(
                book = book,
                newPage = 10,
                newSeconds = null,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { userBookWriteQueue.enqueue(update = any()) }
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
                userBook = userBook,
                userBookRead = userBookRead,
            )

            // ----- Act -----
            syncImpl.enqueueMarkAsRead(book = book)

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    update = match { write ->
                        write.kind == PendingUserBookWriteKind.MARK_AS_READ &&
                            write.userBookId == 10 &&
                            write.userBookReadId == 20 &&
                            write.progressPages == 300 &&
                            write.progressSeconds == userBookRead.currentSeconds
                    },
                )
            }
        }

        @Test
        fun `book with no userBook — enqueue is not called`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null

                every {
                    this@mockk.userBookRead
                } returns stubUserBookRead(id = 1)
            }

            // ----- Act -----
            syncImpl.enqueueMarkAsRead(book = book)

            // ----- Assert -----
            coVerify(exactly = 0) { userBookWriteQueue.enqueue(update = any()) }
        }

        @Test
        fun `book with no userBookRead — enqueue is not called`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns stubUserBook(id = 5)

                every {
                    this@mockk.userBookRead
                } returns null
            }

            // ----- Act -----
            syncImpl.enqueueMarkAsRead(book = book)

            // ----- Assert -----
            coVerify(exactly = 0) { userBookWriteQueue.enqueue(update = any()) }
        }
    }

    // ---------------------------------------------------------------------------
    // enqueueRatingUpdate
    // ---------------------------------------------------------------------------

    @Nested
    inner class EnqueueRatingUpdate {
        @Test
        fun `happy path with userBookRead present — enqueue called with UPDATE_RATING and userBookRead id`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 10)
            val userBookRead = stubUserBookRead(id = 20)
            val book = stubBook(
                userBook = userBook,
                userBookRead = userBookRead,
            )

            // ----- Act -----
            syncImpl.enqueueRatingUpdate(
                book = book,
                rating = 4.5,
            )

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    update = match { write ->
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
                userBookRead = null,
            )

            // ----- Act -----
            syncImpl.enqueueRatingUpdate(
                book = book,
                rating = 3.0,
            )

            // ----- Assert -----
            coVerify {
                userBookWriteQueue.enqueue(
                    update = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_RATING &&
                            write.userBookReadId == 0 &&
                            write.rating == 3.0
                    },
                )
            }
        }

        @Test
        fun `book with no userBook — enqueue is not called`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null
            }

            // ----- Act -----
            syncImpl.enqueueRatingUpdate(
                book = book,
                rating = 5.0,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { userBookWriteQueue.enqueue(update = any()) }
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
                userBook = userBook,
                userBookRead = userBookRead,
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
                    update = match { write ->
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
                userBookRead = null,
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
                    update = match { write ->
                        write.kind == PendingUserBookWriteKind.UPDATE_REVIEW &&
                            write.userBookReadId == 0 &&
                            write.reviewSlateJson == review.toJson() &&
                            write.reviewHasSpoilers == false
                    },
                )
            }
        }

        @Test
        fun `book with no userBook — enqueue is not called`() = runTest {
            // ----- Arrange -----
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null
            }

            // ----- Act -----
            syncImpl.enqueueReviewUpdate(
                book = book,
                review = review,
                hasSpoilers = false,
            )

            // ----- Assert -----
            coVerify(exactly = 0) { userBookWriteQueue.enqueue(update = any()) }
        }
    }

    // ---------------------------------------------------------------------------
    // drainAndReconcile
    // ---------------------------------------------------------------------------

    @Nested
    inner class DrainAndReconcile {
        @Test
        fun `drain is called before the fetchFromServer lambda`() = runTest {
            // ----- Arrange -----
            val order = mutableListOf<String>()

            coEvery {
                userBookWriteDrainer.drainPendingUpdates()
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
                userBookWriteDrainer.drainPendingUpdates()
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
                userBookWriteDrainer.drainPendingUpdates()
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
                userBook = serverUserBook,
                userBookRead = serverRead,
            )
            val localBook = fetchedBook.copy(
                userBook = localUserBook,
                userBookRead = localRead,
            )

            coEvery {
                userBookWriteDrainer.drainPendingUpdates()
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
            cached.userBookRead?.currentPage shouldBe 200
            cached.userBookRead?.progress shouldBe 80f
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
                userBookRead = null,
            )
            val localBook = fetchedBook.copy(
                userBook = localUserBook,
                userBookRead = localRead,
            )

            coEvery {
                userBookWriteDrainer.drainPendingUpdates()
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
            cached.userBookRead?.currentPage shouldBe 200
            cached.userBookRead?.progress shouldBe 80f
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
                userBook = serverUserBook,
                userBookRead = serverRead,
            )
            val localBook = fetchedBook.copy(
                userBook = localUserBook,
                userBookRead = localRead,
            )

            coEvery {
                userBookWriteDrainer.drainPendingUpdates()
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
            cached.userBookRead?.currentPage shouldBe 350
            cached.userBookRead?.progress shouldBe 100f
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
                userBook = serverUserBook,
                userBookRead = serverRead,
            )
            val localBook = fetchedBook.copy(
                userBook = localUserBook,
                userBookRead = localRead,
            )

            coEvery {
                userBookWriteDrainer.drainPendingUpdates()
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
            cached.userBookRead?.finishedAt shouldBe "2026-05-01"
            cached.userBookRead?.progress shouldBe 100f
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
            )

            val fetchedBook = stubBook(
                userBook = serverUserBook,
                userBookRead = null,
            )
            val localBook = fetchedBook.copy(userBook = localUserBook)

            coEvery {
                userBookWriteDrainer.drainPendingUpdates()
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
                ),
            )
            val otherFetchedBook = stubBook(userBook = serverUserBookForOther)

            coEvery {
                userBookWriteDrainer.drainPendingUpdates()
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
            )

            val fetchedBook = stubBook(
                userBook = serverUserBook,
                userBookRead = null,
            )
            val localBook = fetchedBook.copy(userBook = localUserBook)

            coEvery {
                userBookWriteDrainer.drainPendingUpdates()
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
}
