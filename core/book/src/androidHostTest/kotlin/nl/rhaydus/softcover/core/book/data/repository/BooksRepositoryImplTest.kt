package nl.rhaydus.softcover.core.book.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.platform.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.book.baseBook
import nl.rhaydus.softcover.core.book.data.datasource.BookNotFoundException
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.book.data.datasource.FakeUserTagsLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.ProgressHistoryLocalDataSource
import nl.rhaydus.softcover.core.book.data.sync.OfflineUserBookSyncImpl
import nl.rhaydus.softcover.core.book.data.sync.UserBookCacheGate
import nl.rhaydus.softcover.core.book.domain.model.StagedBookRemoval
import nl.rhaydus.softcover.core.book.domain.model.UserBooksSnapshot
import nl.rhaydus.softcover.core.book.domain.sync.OfflineUserBookSync
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.JournalEventType
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.RecentWindow
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import java.util.TimeZone as JavaTimeZone
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class BooksRepositoryImplTest {
    private lateinit var booksRemoteDataSource: BooksRemoteDataSource
    private lateinit var booksLocalDataSource: BooksLocalDataSource
    private lateinit var networkAvailability: NetworkAvailabilityProvider
    private lateinit var offlineSync: OfflineUserBookSync
    private lateinit var progressHistoryLocalDataSource: ProgressHistoryLocalDataSource
    private lateinit var appDispatchers: AppDispatchers
    private lateinit var testScope: TestScope
    private lateinit var repository: BooksRepositoryImpl

    @BeforeEach
    fun setUp() {
        booksRemoteDataSource = mockk()
        booksLocalDataSource = mockk(relaxed = true)
        networkAvailability = mockk()
        offlineSync = mockk(relaxed = true)
        progressHistoryLocalDataSource = mockk(relaxed = true)

        val dispatcher = UnconfinedTestDispatcher()
        appDispatchers = AppDispatchers(
            main = dispatcher,
            io = dispatcher,
            default = dispatcher,
        )

        testScope = TestScope(UnconfinedTestDispatcher())

        every {
            networkAvailability.isOnline
        } returns MutableStateFlow(true)

        coEvery {
            booksRemoteDataSource.fetchExistingUserBookIds(ids = any())
        } returns emptySet()

        repository = BooksRepositoryImpl(
            booksRemoteDataSource = booksRemoteDataSource,
            booksLocalDataSource = booksLocalDataSource,
            userBookCacheGate = UserBookCacheGate(
                booksLocalDataSource = booksLocalDataSource,
                userTagsLocalDataSource = FakeUserTagsLocalDataSource(),
            ),
            networkAvailability = networkAvailability,
            offlineSync = offlineSync,
            applicationScope = ApplicationScope(scope = testScope),
            appDispatchers = appDispatchers,
            progressHistoryLocalDataSource = progressHistoryLocalDataSource,
        )
    }

    @AfterEach
    fun tearDown() {
        testScope.cancel()
    }

    private fun snapshotOf(books: List<Book>): UserBooksSnapshot = UserBooksSnapshot(
        books = books,
        userTagsByBookId = emptyMap(),
    )

    private fun stubBook(userBookId: Int?): Book = mockk(relaxed = true) {
        every {
            this@mockk.userBook
        } returns if (userBookId != null) stubUserBook(userBookId) else null

        every {
            this@mockk.currentRead
        } returns null
    }

    private fun stubUserBook(id: Int): UserBook = mockk(relaxed = true) {
        every {
            this@mockk.id
        } returns id
    }

    private fun stubBookEdition(): BookEdition = mockk()

    private fun stagedBook(
        bookId: Int,
        userBookId: Int?,
    ): Book = mockk(relaxed = true) {
        every {
            this@mockk.id
        } returns bookId

        every {
            this@mockk.userBook
        } returns if (userBookId != null) stubUserBook(userBookId) else null
    }

    @Nested
    inner class RefreshUserBooks {
        @Test
        fun `refreshUserBooks fetches remote books and caches them`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val fetchedBooks = listOf(stubBook(userBookId = 3))

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                )
            } returns snapshotOf(fetchedBooks)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = fetchedBooks)
            }
        }

        @Test
        fun `refreshUserBooks delegates reconciliation to offlineSync and caches whatever it returns`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val reconciledBook = stubBook(userBookId = 42)

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                )
            } returns snapshotOf(listOf(stubBook(userBookId = 42)))

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val reconciled = listOf(reconciledBook)
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(reconciled))
                reconciled
            }

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = listOf(reconciledBook))
            }
        }

        @Test
        fun `null statusFilter passes all status codes to remote`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(
                        UserBookStatus.WANT_TO_READ.code,
                        UserBookStatus.CURRENTLY_READING.code,
                        UserBookStatus.READ.code,
                        UserBookStatus.PAUSED.code,
                        UserBookStatus.DID_NOT_FINISH.code,
                    ),
                )
            } returns snapshotOf(emptyList())

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = null,
            )

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(
                        UserBookStatus.WANT_TO_READ.code,
                        UserBookStatus.CURRENTLY_READING.code,
                        UserBookStatus.READ.code,
                        UserBookStatus.PAUSED.code,
                        UserBookStatus.DID_NOT_FINISH.code,
                    ),
                )
            }
        }

        @Test
        fun `non-null statusFilter fetches only the given status and performs status-scoped delete-diff`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val status = UserBookStatus.WANT_TO_READ
            val fetchedBook = stubBook(userBookId = 10)
            val staleLocalId = 55

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(status.code),
                    userBookIds = setOf(10, staleLocalId),
                )
            } returns snapshotOf(listOf(fetchedBook))

            coEvery {
                booksLocalDataSource.getUserBookIdsByStatuses(statuses = setOf(status))
            } returns listOf(10, staleLocalId)

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = setOf(status),
            )

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(status.code),
                    userBookIds = setOf(10, staleLocalId),
                )
            }

            coVerify {
                booksLocalDataSource.getUserBookIdsByStatuses(statuses = setOf(status))
            }

            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = listOf(staleLocalId))
            }
        }

        @Test
        fun `status-scoped refresh sends the ids stored locally under that status as userBookIds`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val status = UserBookStatus.CURRENTLY_READING

            coEvery {
                booksLocalDataSource.getUserBookIdsByStatuses(statuses = setOf(status))
            } returns listOf(4, 8)

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = any(),
                    statusIds = any(),
                    userBookIds = any(),
                )
            } returns snapshotOf(emptyList())

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = setOf(status),
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(status.code),
                    userBookIds = setOf(4, 8),
                )
            }
        }

        @Test
        fun `a book that moved to another status on the server is cached and not removed`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val movedUserBookId = 7
            val status = UserBookStatus.CURRENTLY_READING
            val movedBook: Book = mockk(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns mockk<UserBook>(relaxed = true) {
                    every {
                        this@mockk.id
                    } returns movedUserBookId

                    every {
                        this@mockk.status
                    } returns BookStatus.Read
                }
            }

            coEvery {
                booksLocalDataSource.getUserBookIdsByStatuses(statuses = setOf(status))
            } returns listOf(movedUserBookId)

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(status.code),
                    userBookIds = setOf(movedUserBookId),
                )
            } returns snapshotOf(listOf(movedBook))

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = setOf(status),
            )

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = listOf(movedBook))
            }

            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = emptyList())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.removeUserBooksById(
                    ids = match { movedUserBookId in it },
                )
            }
        }

        @Test
        fun `a locally stored id under the filtered status that the server no longer returns is removed`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val status = UserBookStatus.READ
            val keptId = 1
            val deletedId = 2

            coEvery {
                booksLocalDataSource.getUserBookIdsByStatuses(statuses = setOf(status))
            } returns listOf(keptId, deletedId)

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(status.code),
                    userBookIds = setOf(keptId, deletedId),
                )
            } returns snapshotOf(listOf(stubBook(userBookId = keptId)))

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = setOf(status),
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(deletedId))
            }
        }

        @Test
        fun `multi-status filter requests exactly those codes and re-fetches local rows under any of them`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.PAUSED)
            val readingId = 4
            val pausedId = 8

            coEvery {
                booksLocalDataSource.getUserBookIdsByStatuses(statuses = statuses)
            } returns listOf(readingId, pausedId)

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = any(),
                    statusIds = any(),
                    userBookIds = any(),
                )
            } returns snapshotOf(emptyList())

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = statuses,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(
                        UserBookStatus.CURRENTLY_READING.code,
                        UserBookStatus.PAUSED.code,
                    ),
                    userBookIds = setOf(readingId, pausedId),
                )
            }
        }

        @Test
        fun `multi-status filter removes only filtered rows the server no longer returns`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.PAUSED)
            val keptId = 1
            val deletedPausedId = 2

            coEvery {
                booksLocalDataSource.getUserBookIdsByStatuses(statuses = statuses)
            } returns listOf(keptId, deletedPausedId)

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = any(),
                    statusIds = any(),
                    userBookIds = any(),
                )
            } returns snapshotOf(listOf(stubBook(userBookId = keptId)))

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = statuses,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(deletedPausedId))
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.getAllUserBookIds()
            }
        }

        @Test
        fun `null statusFilter sends an empty userBookIds and diffs against all local ids`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns listOf(1, 2)

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = any(),
                    statusIds = any(),
                    userBookIds = any(),
                )
            } returns snapshotOf(listOf(stubBook(userBookId = 1)))

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = null,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                    userBookIds = emptySet(),
                )
            }

            coVerify(exactly = 1) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(2))
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.getUserBookIdsByStatuses(statuses = any())
            }
        }

        @Test
        fun `overlapping refreshes with the same filter share one remote fetch`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val gate = CompletableDeferred<Unit>()

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                )
            } coAnswers {
                gate.await()
                snapshotOf(emptyList())
            }

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            val first = launch { repository.refreshUserBooks(userId = userId) }
            val second = launch { repository.refreshUserBooks(userId = userId) }
            runCurrent()

            // ----- Act -----
            gate.complete(Unit)
            joinAll(
                first,
                second,
            )

            // ----- Assert -----
            first.isCompleted shouldBe true
            second.isCompleted shouldBe true

            coVerify(exactly = 1) {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                )
            }
        }

        @Test
        fun `a refresh started after the previous one finished fetches again`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                )
            } returns snapshotOf(emptyList())

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify(exactly = 2) {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                )
            }
        }
    }

    @Nested
    inner class PruneConfirmation {
        @Test
        fun `keeps a missing local user book that the re-check finds on the server`() = runTest {
            // ----- Arrange -----
            stubRefresh(
                fetchedUserBookId = 10,
                localIds = listOf(10, 55, 56),
            )

            coEvery {
                booksRemoteDataSource.fetchExistingUserBookIds(ids = listOf(55, 56))
            } returns setOf(55)

            // ----- Act -----
            repository.refreshUserBooks(
                userId = 20,
                statusFilter = null,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(56))
            }
        }

        @Test
        fun `removes nothing when every missing user book is confirmed present`() = runTest {
            // ----- Arrange -----
            stubRefresh(
                fetchedUserBookId = 10,
                localIds = listOf(10, 55),
            )

            coEvery {
                booksRemoteDataSource.fetchExistingUserBookIds(ids = listOf(55))
            } returns setOf(55)

            // ----- Act -----
            repository.refreshUserBooks(
                userId = 20,
                statusFilter = null,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(55))
            }
        }

        @Test
        fun `removes nothing and does not fail the refresh when the re-check throws`() = runTest {
            // ----- Arrange -----
            stubRefresh(
                fetchedUserBookId = 10,
                localIds = listOf(10, 55),
            )

            coEvery {
                booksRemoteDataSource.fetchExistingUserBookIds(ids = any())
            } throws RuntimeException("offline")

            // ----- Act -----
            repository.refreshUserBooks(
                userId = 20,
                statusFilter = null,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(55))
            }
        }

        @Test
        fun `skips the re-check when no local user book is missing from the fetch`() = runTest {
            // ----- Arrange -----
            stubRefresh(
                fetchedUserBookId = 10,
                localIds = listOf(10),
            )

            // ----- Act -----
            repository.refreshUserBooks(
                userId = 20,
                statusFilter = null,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchExistingUserBookIds(ids = any())
            }
        }

        private fun stubRefresh(
            fetchedUserBookId: Int,
            localIds: List<Int>,
        ) {
            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns localIds

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = any(),
                    statusIds = any(),
                    userBookIds = any(),
                )
            } returns snapshotOf(listOf(stubBook(userBookId = fetchedUserBookId)))

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val fetched = firstArg<suspend () -> UserBooksSnapshot>().invoke().books
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(fetched))
                fetched
            }
        }
    }

    @Nested
    inner class HydrateReferencedBooks {
        @Test
        fun `forceNetwork true fetches all bookIds regardless of what is cached`() = runTest {
            // ----- Arrange -----
            val bookIds = listOf(1, 2)
            val editionIds = emptyList<Int>()
            val fetchedBooks = listOf(stubBook(userBookId = null), stubBook(userBookId = null))

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(
                    ids = bookIds,
                    forceNetwork = true,
                )
            } returns fetchedBooks

            // ----- Act -----
            repository.hydrateReferencedBooks(
                bookIds = bookIds,
                editionIds = editionIds,
                forceNetwork = true,
            )

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchBooksByIds(
                    ids = bookIds,
                    forceNetwork = true,
                )
            }

            coVerify {
                booksLocalDataSource.cacheBooks(books = fetchedBooks)
            }
        }

        @Test
        fun `forceNetwork false only fetches bookIds not already in cache`() = runTest {
            // ----- Arrange -----
            val bookIds = listOf(10, 20, 30)
            val editionIds = emptyList<Int>()
            val cachedBookIds = listOf(10)
            val missingBookIds = listOf(20, 30)
            val fetchedBooks = listOf(stubBook(userBookId = null))

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = bookIds)
            } returns cachedBookIds

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(
                    ids = missingBookIds,
                    forceNetwork = false,
                )
            } returns fetchedBooks

            // ----- Act -----
            repository.hydrateReferencedBooks(
                bookIds = bookIds,
                editionIds = editionIds,
                forceNetwork = false,
            )

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchBooksByIds(
                    ids = missingBookIds,
                    forceNetwork = false,
                )
            }

            coVerify {
                booksLocalDataSource.cacheBooks(books = fetchedBooks)
            }
        }

        @Test
        fun `forceNetwork true fetches all editionIds regardless of what is cached`() = runTest {
            // ----- Arrange -----
            val bookIds = emptyList<Int>()
            val editionIds = listOf(100, 200)
            val fetchedEditions = listOf(stubBookEdition(), stubBookEdition())

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(
                    ids = editionIds,
                    forceNetwork = true,
                )
            } returns fetchedEditions

            // ----- Act -----
            repository.hydrateReferencedBooks(
                bookIds = bookIds,
                editionIds = editionIds,
                forceNetwork = true,
            )

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchEditionsByIds(
                    ids = editionIds,
                    forceNetwork = true,
                )
            }

            coVerify {
                booksLocalDataSource.cacheEditions(editions = fetchedEditions)
            }
        }

        @Test
        fun `forceNetwork false only fetches editionIds not already in cache`() = runTest {
            // ----- Arrange -----
            val bookIds = emptyList<Int>()
            val editionIds = listOf(100, 200, 300)
            val cachedEditionIds = listOf(100)
            val missingEditionIds = listOf(200, 300)
            val fetchedEditions = listOf(stubBookEdition())

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = editionIds)
            } returns cachedEditionIds

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(
                    ids = missingEditionIds,
                    forceNetwork = false,
                )
            } returns fetchedEditions

            // ----- Act -----
            repository.hydrateReferencedBooks(
                bookIds = bookIds,
                editionIds = editionIds,
                forceNetwork = false,
            )

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchEditionsByIds(
                    ids = missingEditionIds,
                    forceNetwork = false,
                )
            }

            coVerify {
                booksLocalDataSource.cacheEditions(editions = fetchedEditions)
            }
        }

        @Test
        fun `empty bookIds and editionIds is a no-op`() = runTest {
            // ----- Arrange -----
            // (no stubs needed — method should return immediately)

            // ----- Act -----
            repository.hydrateReferencedBooks(
                bookIds = emptyList(),
                editionIds = emptyList(),
                forceNetwork = true,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBooksByIds(
                    ids = any(),
                    forceNetwork = any(),
                )
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchEditionsByIds(
                    ids = any(),
                    forceNetwork = any(),
                )
            }
        }

        @Test
        fun `does not call cacheBooks when remote returns empty list for bookIds`() = runTest {
            // ----- Arrange -----
            val bookIds = listOf(1)
            val editionIds = emptyList<Int>()

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(
                    ids = bookIds,
                    forceNetwork = true,
                )
            } returns emptyList()

            // ----- Act -----
            repository.hydrateReferencedBooks(
                bookIds = bookIds,
                editionIds = editionIds,
                forceNetwork = true,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBooks(books = any())
            }
        }

        @Test
        fun `does not call cacheEditions when remote returns empty list for editionIds`() = runTest {
            // ----- Arrange -----
            val bookIds = emptyList<Int>()
            val editionIds = listOf(100)

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(
                    ids = editionIds,
                    forceNetwork = true,
                )
            } returns emptyList()

            // ----- Act -----
            repository.hydrateReferencedBooks(
                bookIds = bookIds,
                editionIds = editionIds,
                forceNetwork = true,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksLocalDataSource.cacheEditions(editions = any())
            }
        }
    }

    @Nested
    inner class RemoveBook {
        @Test
        fun `removeBook removes the user book id via local data source`() = runTest {
            // ----- Arrange -----
            val userBookId = 42
            val book = stubBook(userBookId = userBookId)

            // ----- Act -----
            repository.removeBook(book = book)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = listOf(userBookId))
            }
        }

        @Test
        fun `removeBook throws when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)

            // ----- Act -----
            val error = shouldThrow<Exception> {
                repository.removeBook(book = book)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<Exception>()
        }
    }

    @Nested
    inner class GetEditionsByBookId {
        @Test
        fun `getEditionsByBookId when offline returns cached book editions and skips remote`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            val bookId = 7
            val cachedEditions = listOf(mockk<BookEdition>(relaxed = true), mockk<BookEdition>(relaxed = true))
            val localBook = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.editions
                } returns cachedEditions
            }

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns localBook

            // ----- Act -----
            val result = repository.getEditionsByBookId(bookId = bookId)

            // ----- Assert -----
            result shouldBe cachedEditions

            coVerify(exactly = 0) {
                booksRemoteDataSource.getEditionsByBookId(bookId = any())
            }
        }

        @Test
        fun `getEditionsByBookId when offline and cache miss returns empty list`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            val bookId = 7

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns null

            // ----- Act -----
            val result = repository.getEditionsByBookId(bookId = bookId)

            // ----- Assert -----
            result shouldBe emptyList()

            coVerify(exactly = 0) {
                booksRemoteDataSource.getEditionsByBookId(bookId = any())
            }
        }
    }

    @Nested
    inner class FetchBookById {
        @Test
        fun `fetchBookById when offline returns cached book and skips remote`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            val bookId = 7
            val cachedBook = mockk<Book>(relaxed = true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns cachedBook

            // ----- Act -----
            val result = repository.fetchBookById(id = bookId)

            // ----- Assert -----
            result shouldBe cachedBook

            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBookById(id = any())
            }
        }

        @Test
        fun `fetchBookById when offline and cache miss throws OfflineException`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            val bookId = 7

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns null

            // ----- Act -----
            val error = shouldThrow<OfflineException> {
                repository.fetchBookById(id = bookId)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<OfflineException>()

            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBookById(id = any())
            }
        }

        @Test
        fun `recovery happy path — returns the canonical book and persists, redirects and cleans up`() = runTest {
            // ----- Arrange -----
            val missingId = 10
            val editionId = 55
            val canonicalId = 99

            val userBook = mockk<UserBook>(relaxed = true) {
                every {
                    this@mockk.editionId
                } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns userBook
            }

            val canonicalBook = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null
                every {
                    this@mockk.id
                } returns canonicalId
            }

            coEvery {
                booksRemoteDataSource.fetchBookById(id = missingId)
            } throws BookNotFoundException(bookId = missingId)

            coEvery {
                booksLocalDataSource.getBookById(id = missingId)
            } returns localBook

            coEvery {
                booksRemoteDataSource.fetchBookIdForEdition(editionId = editionId)
            } returns canonicalId

            coEvery {
                booksRemoteDataSource.fetchBookById(id = canonicalId)
            } returns canonicalBook

            // ----- Act -----
            val result = repository.fetchBookById(id = missingId)

            // ----- Assert -----
            result shouldBe canonicalBook

            coVerifyOrder {
                booksLocalDataSource.cacheBook(book = canonicalBook)
                booksLocalDataSource.redirectBookId(
                    oldId = missingId,
                    newId = canonicalId,
                )
                booksLocalDataSource.deleteOrphanBooks()
            }
        }

        @Test
        fun `recovery canonical also missing — no persist calls are made`() = runTest {
            // ----- Arrange -----
            val missingId = 10
            val editionId = 55
            val canonicalId = 99

            val userBook = mockk<UserBook>(relaxed = true) {
                every {
                    this@mockk.editionId
                } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns userBook
            }

            coEvery {
                booksRemoteDataSource.fetchBookById(id = missingId)
            } throws BookNotFoundException(bookId = missingId)

            coEvery {
                booksLocalDataSource.getBookById(id = missingId)
            } returns localBook

            coEvery {
                booksRemoteDataSource.fetchBookIdForEdition(editionId = editionId)
            } returns canonicalId

            coEvery {
                booksRemoteDataSource.fetchBookById(id = canonicalId)
            } throws BookNotFoundException(bookId = canonicalId)

            // ----- Act -----
            val error = shouldThrow<BookNotFoundException> {
                repository.fetchBookById(id = missingId)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<BookNotFoundException>()

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.redirectBookId(
                    oldId = any(),
                    newId = any(),
                )
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.deleteOrphanBooks()
            }
        }

        @Test
        fun `recovery falls back to currentEdition id when userBook is null`() = runTest {
            // ----- Arrange -----
            val missingId = 10
            val editionId = 66
            val canonicalId = 100

            val currentEdition = mockk<BookEdition>(relaxed = true) {
                every {
                    this@mockk.id
                } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null
                every {
                    this@mockk.currentEdition
                } returns currentEdition
            }

            val canonicalBook = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.fetchBookById(id = missingId)
            } throws BookNotFoundException(bookId = missingId)

            coEvery {
                booksLocalDataSource.getBookById(id = missingId)
            } returns localBook

            coEvery {
                booksRemoteDataSource.fetchBookIdForEdition(editionId = editionId)
            } returns canonicalId

            coEvery {
                booksRemoteDataSource.fetchBookById(id = canonicalId)
            } returns canonicalBook

            // ----- Act -----
            val result = repository.fetchBookById(id = missingId)

            // ----- Assert -----
            result shouldBe canonicalBook
        }

        @Test
        fun `recovery falls back to first edition id when userBook and currentEdition are both null`() = runTest {
            // ----- Arrange -----
            val missingId = 10
            val editionId = 77
            val canonicalId = 101

            val firstEdition = mockk<BookEdition>(relaxed = true) {
                every {
                    this@mockk.id
                } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null
                every {
                    this@mockk.currentEdition
                } returns null
                every {
                    this@mockk.editions
                } returns listOf(firstEdition)
            }

            val canonicalBook = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.fetchBookById(id = missingId)
            } throws BookNotFoundException(bookId = missingId)

            coEvery {
                booksLocalDataSource.getBookById(id = missingId)
            } returns localBook

            coEvery {
                booksRemoteDataSource.fetchBookIdForEdition(editionId = editionId)
            } returns canonicalId

            coEvery {
                booksRemoteDataSource.fetchBookById(id = canonicalId)
            } returns canonicalBook

            // ----- Act -----
            val result = repository.fetchBookById(id = missingId)

            // ----- Assert -----
            result shouldBe canonicalBook
        }

        @Test
        fun `recovery rethrows BookNotFoundException when local cache has no entry`() = runTest {
            // ----- Arrange -----
            val missingId = 10
            val notFound = BookNotFoundException(bookId = missingId)

            coEvery {
                booksRemoteDataSource.fetchBookById(id = missingId)
            } throws notFound

            coEvery {
                booksLocalDataSource.getBookById(id = missingId)
            } returns null

            // ----- Act -----
            val error = shouldThrow<BookNotFoundException> {
                repository.fetchBookById(id = missingId)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<BookNotFoundException>()

            coVerify(exactly = 0) {
                booksLocalDataSource.redirectBookId(
                    oldId = any(),
                    newId = any(),
                )
            }
        }

        @Test
        fun `recovery rethrows BookNotFoundException when local book has no resolvable edition id`() = runTest {
            // ----- Arrange -----
            val missingId = 10
            val notFound = BookNotFoundException(bookId = missingId)

            val localBook = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns null
                every {
                    this@mockk.currentEdition
                } returns null
                every {
                    this@mockk.editions
                } returns emptyList()
            }

            coEvery {
                booksRemoteDataSource.fetchBookById(id = missingId)
            } throws notFound

            coEvery {
                booksLocalDataSource.getBookById(id = missingId)
            } returns localBook

            // ----- Act -----
            val error = shouldThrow<BookNotFoundException> {
                repository.fetchBookById(id = missingId)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<BookNotFoundException>()
        }

        @Test
        fun `recovery rethrows BookNotFoundException when remote fetchBookIdForEdition returns null`() = runTest {
            // ----- Arrange -----
            val missingId = 10
            val editionId = 55
            val notFound = BookNotFoundException(bookId = missingId)

            val userBook = mockk<UserBook>(relaxed = true) {
                every {
                    this@mockk.editionId
                } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns userBook
            }

            coEvery {
                booksRemoteDataSource.fetchBookById(id = missingId)
            } throws notFound

            coEvery {
                booksLocalDataSource.getBookById(id = missingId)
            } returns localBook

            coEvery {
                booksRemoteDataSource.fetchBookIdForEdition(editionId = editionId)
            } returns null

            // ----- Act -----
            val error = shouldThrow<BookNotFoundException> {
                repository.fetchBookById(id = missingId)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<BookNotFoundException>()
        }

        @Test
        fun `recovery rethrows BookNotFoundException when remote returns the same id as the missing book`() = runTest {
            // ----- Arrange -----
            val missingId = 10
            val editionId = 55
            val notFound = BookNotFoundException(bookId = missingId)

            val userBook = mockk<UserBook>(relaxed = true) {
                every {
                    this@mockk.editionId
                } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.userBook
                } returns userBook
            }

            coEvery {
                booksRemoteDataSource.fetchBookById(id = missingId)
            } throws notFound

            coEvery {
                booksLocalDataSource.getBookById(id = missingId)
            } returns localBook

            coEvery {
                booksRemoteDataSource.fetchBookIdForEdition(editionId = editionId)
            } returns missingId

            // ----- Act -----
            val error = shouldThrow<BookNotFoundException> {
                repository.fetchBookById(id = missingId)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<BookNotFoundException>()
        }
    }

    @Nested
    inner class MarkBookAsWantToRead {
        private fun stubUserBookWithStatus(
            id: Int,
            status: BookStatus,
        ): UserBook = UserBook(
            id = id,
            status = status,
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

        @Test
        fun `happy path — writes optimistic snapshot, calls remote with book id, caches result and returns it`() = runTest {
            // ----- Arrange -----
            val bookId = 11
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns stubUserBookWithStatus(
                    id = 5,
                    status = BookStatus.Reading,
                )
            }
            val remoteBook = stubBook(userBookId = 5)

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(
                    bookId = bookId,
                    editionId = any(),
                )
            } returns remoteBook

            // ----- Act -----
            val result = repository.markBookAsWantToRead(book = book)

            // ----- Assert -----
            result shouldBe remoteBook

            coVerify {
                booksRemoteDataSource.markBookAsWantToRead(
                    bookId = bookId,
                    editionId = any(),
                )
            }

            coVerify {
                booksLocalDataSource.cacheBook(book = remoteBook)
            }
        }

        @Test
        fun `forwards non-null editionId to remote data source`() = runTest {
            // ----- Arrange -----
            val bookId = 11
            val editionId = 55
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns stubUserBookWithStatus(
                    id = 5,
                    status = BookStatus.Reading,
                )
            }
            val remoteBook = stubBook(userBookId = 5)

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(
                    bookId = bookId,
                    editionId = editionId,
                )
            } returns remoteBook

            // ----- Act -----
            repository.markBookAsWantToRead(
                book = book,
                editionId = editionId,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.markBookAsWantToRead(
                    bookId = bookId,
                    editionId = editionId,
                )
            }
        }

        @Test
        fun `optimistic write is skipped when book has no userBook`() = runTest {
            // ----- Arrange -----
            val bookId = 11
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns null
            }
            val remoteBook = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(
                    bookId = bookId,
                    editionId = any(),
                )
            } returns remoteBook

            // ----- Act -----
            repository.markBookAsWantToRead(book = book)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }
        }

        @Test
        fun `failure path — rolls back via cacheBook(snapshot) and rethrows`() = runTest {
            // ----- Arrange -----
            val bookId = 11
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns stubUserBookWithStatus(
                    id = 5,
                    status = BookStatus.Reading,
                )
            }
            val snapshot = stubBook(userBookId = 5)
            val remoteError = RuntimeException("network error")

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(
                    bookId = bookId,
                    editionId = any(),
                )
            } throws remoteError

            // ----- Act -----
            val caught = runCatching { repository.markBookAsWantToRead(book = book) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without invoking rollback`() = runTest {
            // ----- Arrange -----
            val bookId = 11
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns stubUserBookWithStatus(
                    id = 5,
                    status = BookStatus.Reading,
                )
            }
            val snapshot = stubBook(userBookId = 5)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(
                    bookId = bookId,
                    editionId = any(),
                )
            } throws CancellationException("cancelled")

            // ----- Act -----
            val error = shouldThrow<CancellationException> {
                repository.markBookAsWantToRead(book = book)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<CancellationException>()

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }
    }

    @Nested
    inner class MarkBookAsReading {
        @Test
        fun `writes an optimistic Reading snapshot with a started journal and returns the remote book`() = runTest {
            // ----- Arrange -----
            val existingJournal = ReadingJournal(
                updatedAt = "2026-01-01T10:00:00",
                event = "some_existing_event",
            )
            val userBook = UserBook(
                id = 1,
                status = BookStatus.WantToRead,
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
                journals = listOf(existingJournal),
                reads = emptyList(),
            )
            val book = Book(
                id = 42,
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
            )
            val expectedBook = stubBook(userBookId = 1)
            val slot = slot<Book>()

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            } returns expectedBook

            coEvery {
                booksLocalDataSource.cacheBook(book = capture(slot))
            } returns Unit

            // ----- Act -----
            val result = repository.markBookAsReading(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
            val capturedUserBook = slot.captured.userBook.shouldNotBeNull()
            capturedUserBook.status shouldBe BookStatus.Reading
            val capturedJournals = capturedUserBook.journals
            capturedJournals.size shouldBe 2
            capturedJournals[0] shouldBe existingJournal
            capturedJournals[1].event shouldBe "user_book_read_started"
            capturedJournals[1].updatedAt.isNotEmpty() shouldBe true
        }

        @Test
        fun `when userBook is null no optimistic cacheBook happens but the remote write still runs`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)
            val expectedBook = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            } returns expectedBook

            // ----- Act -----
            val result = repository.markBookAsReading(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = book)
            }
            coVerify(exactly = 1) {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            }
        }

        @Test
        fun `markBookAsReading restores snapshot to local cache and rethrows when remote throws non-offline error`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val remoteError = RuntimeException("network failure")
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            } throws remoteError

            // ----- Act -----
            val caught = runCatching { repository.markBookAsReading(book = book) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `markBookAsReading does not call cacheBook with snapshot when no prior cached book exists`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns null

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            } throws RuntimeException("network failure")

            // ----- Act -----
            runCatching { repository.markBookAsReading(book = book) }

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }
        }

        @Test
        fun `markBookAsReading cancellation — optimistic write occurs but snapshot rollback is not invoked`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }
            val snapshot = stubBook(userBookId = 1)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            } throws CancellationException("cancelled")

            // ----- Act -----
            val error = shouldThrow<CancellationException> {
                repository.markBookAsReading(book = book)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<CancellationException>()

            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `forwards non-null editionId to remote data source`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val editionId = 99
            val expectedBook = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = editionId,
                )
            } returns expectedBook

            coEvery {
                booksLocalDataSource.cacheBook(book = any())
            } returns Unit

            // ----- Act -----
            repository.markBookAsReading(
                book = book,
                editionId = editionId,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = editionId,
                )
            }
        }
    }

    @Nested
    inner class StatusWrites {
        private val existingJournal = ReadingJournal(
            updatedAt = "2026-01-01T10:00:00",
            event = "some_existing_event",
        )

        private fun statusBook(
            status: BookStatus,
            isShelved: Boolean = true,
        ): Book = Book(
            id = 42,
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
            userBook = if (isShelved) {
                UserBook(
                    id = 7,
                    status = status,
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
                    journals = listOf(existingJournal),
                    reads = emptyList(),
                )
            } else {
                null
            },
        )

        private fun goOffline() {
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)
        }

        @Test
        fun `markBookAsPaused writes an optimistic Paused status without a journal event and returns the remote book`() =
            runTest {
                // ----- Arrange -----
                val book = statusBook(status = BookStatus.Reading)
                val remoteBook = stubBook(userBookId = 7)
                val slot = slot<Book>()

                coEvery {
                    booksRemoteDataSource.markBookAsPaused(
                        book = book,
                        editionId = any(),
                    )
                } returns remoteBook

                coEvery {
                    booksLocalDataSource.cacheBook(book = capture(slot))
                } returns Unit

                // ----- Act -----
                val result = repository.markBookAsPaused(book = book)

                // ----- Assert -----
                result shouldBe remoteBook
                val capturedUserBook = slot.captured.userBook.shouldNotBeNull()
                capturedUserBook.status shouldBe BookStatus.Paused
                capturedUserBook.journals shouldBe listOf(existingJournal)
            }

        @Test
        fun `markBookAsPaused skips the optimistic write for an already Paused book but still calls remote`() =
            runTest {
                // ----- Arrange -----
                val book = statusBook(status = BookStatus.Paused)
                val remoteBook = stubBook(userBookId = 7)

                coEvery {
                    booksRemoteDataSource.markBookAsPaused(
                        book = book,
                        editionId = any(),
                    )
                } returns remoteBook

                // ----- Act -----
                repository.markBookAsPaused(book = book)

                // ----- Assert -----
                coVerify(exactly = 0) {
                    booksLocalDataSource.cacheBook(book = any())
                }
                coVerify(exactly = 1) {
                    booksRemoteDataSource.markBookAsPaused(
                        book = book,
                        editionId = any(),
                    )
                }
            }

        @Test
        fun `markBookAsPaused forwards editionId to the remote data source`() = runTest {
            // ----- Arrange -----
            val book = statusBook(status = BookStatus.Reading)
            val editionId = 99

            coEvery {
                booksRemoteDataSource.markBookAsPaused(
                    book = book,
                    editionId = editionId,
                )
            } returns stubBook(userBookId = 7)

            // ----- Act -----
            repository.markBookAsPaused(
                book = book,
                editionId = editionId,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.markBookAsPaused(
                    book = book,
                    editionId = editionId,
                )
            }
        }

        @Test
        fun `markBookAsPaused restores the snapshot and rethrows on a non-transient failure without queueing`() =
            runTest {
                // ----- Arrange -----
                val book = statusBook(status = BookStatus.Reading)
                val snapshot = stubBook(userBookId = 7)
                val remoteError = RuntimeException("rejected")

                coEvery {
                    booksLocalDataSource.getBookById(id = book.id)
                } returns snapshot

                coEvery {
                    booksRemoteDataSource.markBookAsPaused(
                        book = book,
                        editionId = any(),
                    )
                } throws remoteError

                // ----- Act -----
                val caught = runCatching { repository.markBookAsPaused(book = book) }

                // ----- Assert -----
                caught.exceptionOrNull() shouldBe remoteError
                coVerify(exactly = 1) {
                    booksLocalDataSource.cacheBook(book = snapshot)
                }
                coVerify(exactly = 0) {
                    offlineSync.enqueueStatusUpdate(
                        book = any(),
                        status = any(),
                    )
                }
            }

        @Test
        fun `markBookAsPaused rethrows cancellation without rollback or queueing`() = runTest {
            // ----- Arrange -----
            val book = statusBook(status = BookStatus.Reading)
            val snapshot = stubBook(userBookId = 7)

            coEvery {
                booksLocalDataSource.getBookById(id = book.id)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsPaused(
                    book = book,
                    editionId = any(),
                )
            } throws CancellationException("cancelled")

            // ----- Act -----
            val error = shouldThrow<CancellationException> {
                repository.markBookAsPaused(book = book)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<CancellationException>()

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
            coVerify(exactly = 0) {
                offlineSync.enqueueStatusUpdate(
                    book = any(),
                    status = any(),
                )
            }
        }

        @Test
        fun `markBookAsPaused when offline queues SET_STATUS for a shelved book without calling remote`() = runTest {
            // ----- Arrange -----
            goOffline()

            val book = statusBook(status = BookStatus.Reading)
            val slot = slot<Book>()

            coEvery {
                booksLocalDataSource.cacheBook(book = capture(slot))
            } returns Unit

            // ----- Act -----
            val result = repository.markBookAsPaused(book = book)

            // ----- Assert -----
            result shouldBe slot.captured
            result.userBook.shouldNotBeNull().status shouldBe BookStatus.Paused
            coVerify(exactly = 1) {
                offlineSync.enqueueStatusUpdate(
                    book = result,
                    status = UserBookStatus.PAUSED,
                )
            }
            coVerify(exactly = 0) {
                booksRemoteDataSource.markBookAsPaused(
                    book = any(),
                    editionId = any(),
                )
            }
        }

        @Test
        fun `markBookAsPaused queues SET_STATUS and keeps the optimistic book when remote throws a transient error`() =
            runTest {
                // ----- Arrange -----
                val book = statusBook(status = BookStatus.Reading)
                val slot = slot<Book>()

                coEvery {
                    booksLocalDataSource.cacheBook(book = capture(slot))
                } returns Unit

                coEvery {
                    booksRemoteDataSource.markBookAsPaused(
                        book = book,
                        editionId = any(),
                    )
                } throws ServerUnavailableException(message = "502")

                // ----- Act -----
                val result = repository.markBookAsPaused(book = book)

                // ----- Assert -----
                result shouldBe slot.captured
                coVerify(exactly = 1) {
                    offlineSync.enqueueStatusUpdate(
                        book = result,
                        status = UserBookStatus.PAUSED,
                    )
                }
                coVerify(exactly = 1) {
                    booksLocalDataSource.cacheBook(book = any())
                }
            }

        @Test
        fun `markBookAsPaused for an off-shelf book stays online-only and rolls back when offline`() = runTest {
            // ----- Arrange -----
            goOffline()

            val book = statusBook(
                status = BookStatus.None,
                isShelved = false,
            )

            coEvery {
                booksRemoteDataSource.markBookAsPaused(
                    book = book,
                    editionId = any(),
                )
            } throws OfflineException()

            // ----- Act -----
            val error = shouldThrow<OfflineException> {
                repository.markBookAsPaused(book = book)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<OfflineException>()

            coVerify(exactly = 0) {
                offlineSync.enqueueStatusUpdate(
                    book = any(),
                    status = any(),
                )
            }
        }

        @Test
        fun `markBookAsPaused for an off-shelf book does not queue a transient failure`() = runTest {
            // ----- Arrange -----
            val book = statusBook(
                status = BookStatus.None,
                isShelved = false,
            )
            val error = ServerUnavailableException(message = "502")

            coEvery {
                booksRemoteDataSource.markBookAsPaused(
                    book = book,
                    editionId = any(),
                )
            } throws error

            // ----- Act -----
            val caught = runCatching { repository.markBookAsPaused(book = book) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe error
            coVerify(exactly = 0) {
                offlineSync.enqueueStatusUpdate(
                    book = any(),
                    status = any(),
                )
            }
        }

        @Test
        fun `markBookAsWantToRead when offline queues SET_STATUS for a shelved book without calling remote`() =
            runTest {
                // ----- Arrange -----
                goOffline()

                val book = statusBook(status = BookStatus.Reading)

                // ----- Act -----
                val result = repository.markBookAsWantToRead(book = book)

                // ----- Assert -----
                result.userBook.shouldNotBeNull().status shouldBe BookStatus.WantToRead
                coVerify(exactly = 1) {
                    offlineSync.enqueueStatusUpdate(
                        book = result,
                        status = UserBookStatus.WANT_TO_READ,
                    )
                }
                coVerify(exactly = 0) {
                    booksRemoteDataSource.markBookAsWantToRead(
                        bookId = any(),
                        editionId = any(),
                    )
                }
            }

        @Test
        fun `markBookAsWantToRead queues SET_STATUS when remote throws a transient error`() = runTest {
            // ----- Arrange -----
            val book = statusBook(status = BookStatus.Reading)

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(
                    bookId = book.id,
                    editionId = any(),
                )
            } throws OfflineException()

            // ----- Act -----
            val result = repository.markBookAsWantToRead(book = book)

            // ----- Assert -----
            result.userBook.shouldNotBeNull().status shouldBe BookStatus.WantToRead
            coVerify(exactly = 1) {
                offlineSync.enqueueStatusUpdate(
                    book = result,
                    status = UserBookStatus.WANT_TO_READ,
                )
            }
        }

        @Test
        fun `markBookAsWantToRead for an off-shelf book is not queued on failure`() = runTest {
            // ----- Arrange -----
            val book = statusBook(
                status = BookStatus.None,
                isShelved = false,
            )

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(
                    bookId = book.id,
                    editionId = any(),
                )
            } throws OfflineException()

            // ----- Act -----
            val caught = runCatching { repository.markBookAsWantToRead(book = book) }

            // ----- Assert -----
            caught.exceptionOrNull().shouldNotBeNull()
            coVerify(exactly = 0) {
                offlineSync.enqueueStatusUpdate(
                    book = any(),
                    status = any(),
                )
            }
        }

        @Test
        fun `markBookAsReading when offline queues SET_STATUS for a shelved book without calling remote`() =
            runTest {
                // ----- Arrange -----
                goOffline()

                val book = statusBook(status = BookStatus.Paused)

                // ----- Act -----
                val result = repository.markBookAsReading(book = book)

                // ----- Assert -----
                result.userBook.shouldNotBeNull().status shouldBe BookStatus.Reading
                coVerify(exactly = 1) {
                    offlineSync.enqueueStatusUpdate(
                        book = result,
                        status = UserBookStatus.CURRENTLY_READING,
                    )
                }
                coVerify(exactly = 0) {
                    booksRemoteDataSource.markBookAsReading(
                        book = any(),
                        editionId = any(),
                    )
                }
            }

        @Test
        fun `markBookAsReading queues SET_STATUS when remote throws a transient error`() = runTest {
            // ----- Arrange -----
            val book = statusBook(status = BookStatus.Paused)

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            } throws ServerUnavailableException(message = "503")

            // ----- Act -----
            val result = repository.markBookAsReading(book = book)

            // ----- Assert -----
            result.userBook.shouldNotBeNull().status shouldBe BookStatus.Reading
            coVerify(exactly = 1) {
                offlineSync.enqueueStatusUpdate(
                    book = result,
                    status = UserBookStatus.CURRENTLY_READING,
                )
            }
        }

        @Test
        fun `markBookAsReading for an off-shelf book is not queued when offline`() = runTest {
            // ----- Arrange -----
            goOffline()

            val book = statusBook(
                status = BookStatus.None,
                isShelved = false,
            )

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            } throws OfflineException()

            // ----- Act -----
            val caught = runCatching { repository.markBookAsReading(book = book) }

            // ----- Assert -----
            caught.exceptionOrNull().shouldNotBeNull()
            coVerify(exactly = 0) {
                offlineSync.enqueueStatusUpdate(
                    book = any(),
                    status = any(),
                )
            }
        }
    }

    @Nested
    inner class UpdateBookRating {
        private fun stubUserBookWithRating(
            id: Int,
            rating: Double?,
            status: BookStatus = BookStatus.Read,
        ): UserBook = UserBook(
            id = id,
            status = status,
            dateAdded = "2026-01-01",
            createdAt = null,
            privacySettingId = 1,
            reviewHasSpoilers = false,
            editionId = null,
            lastReadDate = null,
            rating = rating,
            referrerUserId = null,
            reviewedAt = null,
            updatedAt = null,
            journals = emptyList(),
            reads = emptyList(),
        )

        @Test
        fun `throws when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)

            // ----- Act -----
            val error = shouldThrow<Exception> {
                repository.updateBookRating(
                    book = book,
                    rating = 4.0,
                )
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<Exception>()
        }

        @Test
        fun `writes the optimistic rating to the local cache and returns the remote book`() = runTest {
            // ----- Arrange -----
            val newRating = 4.5
            val userBook = stubUserBookWithRating(
                id = 5,
                rating = 3.0,
            )

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            val book = Book(
                id = 10,
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
            )
            val remoteBook = stubBook(userBookId = 5)
            val capturedBooks = mutableListOf<Book>()

            coEvery {
                booksLocalDataSource.getBookById(id = 10)
            } returns null

            coEvery {
                booksRemoteDataSource.updateBookRating(
                    userBook = any(),
                    rating = newRating,
                )
            } returns remoteBook

            coEvery {
                booksLocalDataSource.cacheBook(book = capture(capturedBooks))
            } returns Unit

            // ----- Act -----
            val result = repository.updateBookRating(
                book = book,
                rating = newRating,
            )

            // ----- Assert -----
            result shouldBe remoteBook
            capturedBooks.first().userBook?.rating shouldBe newRating
        }

        @Test
        fun `restores snapshot via restoreOptimisticWrite when remote throws non-cancellation error`() = runTest {
            // ----- Arrange -----
            val bookId = 10
            val newRating = 4.5
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns stubUserBookWithRating(
                    id = 5,
                    rating = 3.0,
                )
            }
            val snapshot = stubBook(userBookId = 5)
            val remoteError = RuntimeException("network failure")

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.updateBookRating(
                    userBook = any(),
                    rating = newRating,
                )
            } throws remoteError

            // ----- Act -----
            val caught = runCatching {
                repository.updateBookRating(
                    book = book,
                    rating = newRating,
                )
            }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `rethrows CancellationException without invoking snapshot rollback`() = runTest {
            // ----- Arrange -----
            val bookId = 10
            val newRating = 4.5
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns stubUserBookWithRating(
                    id = 5,
                    rating = 3.0,
                )
            }
            val snapshot = stubBook(userBookId = 5)

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.updateBookRating(
                    userBook = any(),
                    rating = newRating,
                )
            } throws CancellationException("cancelled")

            // ----- Act -----
            val error = shouldThrow<CancellationException> {
                repository.updateBookRating(
                    book = book,
                    rating = newRating,
                )
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<CancellationException>()

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `when online and remote throws OfflineException enqueues UPDATE_RATING and returns optimistic book`() = runTest {
            // ----- Arrange -----
            val userBookId = 5
            val bookId = 10
            val newRating = 4.5
            val book = Book(
                id = bookId,
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
                userBook = stubUserBookWithRating(
                    id = userBookId,
                    rating = 3.0,
                ),
            )
            val snapshot = stubBook(userBookId = userBookId)

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.updateBookRating(
                    userBook = any(),
                    rating = newRating,
                )
            } throws OfflineException()

            // ----- Act -----
            repository.updateBookRating(
                book = book,
                rating = newRating,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }

            coVerify {
                offlineSync.enqueueRatingUpdate(
                    book = any(),
                    rating = newRating,
                )
            }
        }

        @Test
        fun `when online and remote throws ServerUnavailableException enqueues UPDATE_RATING and returns optimistic book`() = runTest {
            // ----- Arrange -----
            val userBookId = 5
            val bookId = 10
            val newRating = 4.5
            val book = Book(
                id = bookId,
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
                userBook = stubUserBookWithRating(
                    id = userBookId,
                    rating = 3.0,
                ),
            )
            val snapshot = stubBook(userBookId = userBookId)

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.updateBookRating(
                    userBook = any(),
                    rating = newRating,
                )
            } throws ServerUnavailableException("server error")

            // ----- Act -----
            repository.updateBookRating(
                book = book,
                rating = newRating,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }

            coVerify {
                offlineSync.enqueueRatingUpdate(
                    book = any(),
                    rating = newRating,
                )
            }
        }

        @Test
        fun `when offline caches optimistic book and enqueues UPDATE_RATING without calling remote`() = runTest {
            // ----- Arrange -----
            val userBookId = 5
            val bookId = 10
            val newRating = 4.5
            val book = Book(
                id = bookId,
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
                userBook = stubUserBookWithRating(
                    id = userBookId,
                    rating = 3.0,
                ),
            )

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns null

            // ----- Act -----
            repository.updateBookRating(
                book = book,
                rating = newRating,
            )

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.updateBookRating(
                    userBook = any(),
                    rating = any(),
                )
            }

            coVerify {
                offlineSync.enqueueRatingUpdate(
                    book = any(),
                    rating = newRating,
                )
            }
        }

        @Test
        fun `does not resurrect a finished book — optimistic write carries the cached Read status, not the passed-in Reading status`() =
            runTest {
                // ----- Arrange -----
                val userBookId = 5
                val bookId = 10
                val newRating = 4.5

                // The cached snapshot already reflects that the book was finished.
                val cachedBook = Book(
                    id = bookId,
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
                    userBook = stubUserBookWithRating(
                        id = userBookId,
                        rating = 3.0,
                        status = BookStatus.Read,
                    ),
                )

                // The caller passes a pre-finish snapshot still marked Reading (e.g. the Reading
                // screen's finish -> verdict prompt flow).
                val passedBook = Book(
                    id = bookId,
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
                    userBook = stubUserBookWithRating(
                        id = userBookId,
                        rating = 3.0,
                        status = BookStatus.Reading,
                    ),
                )
                val remoteBook = stubBook(userBookId = userBookId)
                val capturedBooks = mutableListOf<Book>()

                every {
                    networkAvailability.isOnline
                } returns MutableStateFlow(true)

                coEvery {
                    booksLocalDataSource.getBookById(id = bookId)
                } returns cachedBook

                coEvery {
                    booksRemoteDataSource.updateBookRating(
                        userBook = any(),
                        rating = newRating,
                    )
                } returns remoteBook

                coEvery {
                    booksLocalDataSource.cacheBook(book = capture(capturedBooks))
                } returns Unit

                // ----- Act -----
                repository.updateBookRating(
                    book = passedBook,
                    rating = newRating,
                )

                // ----- Assert -----
                val optimisticWrite = capturedBooks.first()
                optimisticWrite.userBook?.status shouldBe BookStatus.Read
                optimisticWrite.userBook?.rating shouldBe newRating
            }
    }

    @Nested
    inner class UpdateBookReview {
        private fun stubUserBookForReview(
            id: Int,
            status: BookStatus = BookStatus.Read,
        ): UserBook = UserBook(
            id = id,
            status = status,
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

        @Test
        fun `throws when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)

            // ----- Act -----
            val error = shouldThrow<Exception> {
                repository.updateBookReview(
                    book = book,
                    review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great read"))))),
                    hasSpoilers = false,
                )
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<Exception>()
        }

        @Test
        fun `on success returns the remote book and does not enqueue`() = runTest {
            // ----- Arrange -----
            val userBookId = 5
            val bookId = 10
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns stubUserBookForReview(id = userBookId)
            }
            val remoteBook = stubBook(userBookId = userBookId)

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns null

            coEvery {
                booksRemoteDataSource.updateBookReview(
                    userBook = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            } returns remoteBook

            coEvery {
                booksLocalDataSource.cacheBook(book = any())
            } returns Unit

            // ----- Act -----
            val result = repository.updateBookReview(
                book = book,
                review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great read"))))),
                hasSpoilers = false,
            )

            // ----- Assert -----
            result shouldBe remoteBook

            coVerify(exactly = 0) {
                offlineSync.enqueueReviewUpdate(
                    book = any(),
                    review = any(),
                    hasSpoilers = any(),
                )
            }
        }

        @Test
        fun `when offline caches optimistic book and enqueues UPDATE_REVIEW without calling remote`() = runTest {
            // ----- Arrange -----
            val userBookId = 5
            val body = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great offline read")))))
            val hasSpoilers = true
            val book = Book(
                id = 10,
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
                userBook = stubUserBookForReview(id = userBookId),
            )

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            coEvery {
                booksLocalDataSource.getBookById(id = 10)
            } returns null

            // ----- Act -----
            val result = repository.updateBookReview(
                book = book,
                review = body,
                hasSpoilers = hasSpoilers,
            )

            // ----- Assert -----
            result.userBook?.reviewDocument shouldBe body
            result.userBook?.reviewHasSpoilers shouldBe hasSpoilers

            coVerify {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.updateBookReview(
                    userBook = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            coVerify {
                offlineSync.enqueueReviewUpdate(
                    book = any(),
                    review = body,
                    hasSpoilers = hasSpoilers,
                )
            }
        }

        @Test
        fun `when online and remote throws OfflineException enqueues UPDATE_REVIEW and returns optimistic book`() = runTest {
            // ----- Arrange -----
            val userBookId = 5
            val bookId = 10
            val body = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Review written while falsely online")))))
            val hasSpoilers = false
            val book = Book(
                id = bookId,
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
                userBook = stubUserBookForReview(id = userBookId),
            )
            val snapshot = Book(
                id = bookId,
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
                userBook = stubUserBookForReview(id = userBookId),
            )

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.updateBookReview(
                    userBook = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            } throws OfflineException()

            coEvery {
                booksLocalDataSource.cacheBook(book = any())
            } returns Unit

            // ----- Act -----
            val result = repository.updateBookReview(
                book = book,
                review = body,
                hasSpoilers = hasSpoilers,
            )

            // ----- Assert -----
            result.userBook?.reviewDocument shouldBe body
            result.userBook?.reviewHasSpoilers shouldBe hasSpoilers

            coVerify(exactly = 1) {
                booksRemoteDataSource.updateBookReview(
                    userBook = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            coVerify {
                offlineSync.enqueueReviewUpdate(
                    book = any(),
                    review = body,
                    hasSpoilers = hasSpoilers,
                )
            }
        }

        @Test
        fun `when online and remote throws ServerUnavailableException enqueues UPDATE_REVIEW and returns optimistic book`() = runTest {
            // ----- Arrange -----
            val userBookId = 5
            val bookId = 10
            val body = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Review written while falsely online")))))
            val hasSpoilers = false
            val book = Book(
                id = bookId,
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
                userBook = stubUserBookForReview(id = userBookId),
            )
            val snapshot = Book(
                id = bookId,
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
                userBook = stubUserBookForReview(id = userBookId),
            )

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.updateBookReview(
                    userBook = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            } throws ServerUnavailableException("server error")

            coEvery {
                booksLocalDataSource.cacheBook(book = any())
            } returns Unit

            // ----- Act -----
            val result = repository.updateBookReview(
                book = book,
                review = body,
                hasSpoilers = hasSpoilers,
            )

            // ----- Assert -----
            result.userBook?.reviewDocument shouldBe body
            result.userBook?.reviewHasSpoilers shouldBe hasSpoilers

            coVerify(exactly = 1) {
                booksRemoteDataSource.updateBookReview(
                    userBook = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            }

            coVerify {
                offlineSync.enqueueReviewUpdate(
                    book = any(),
                    review = body,
                    hasSpoilers = hasSpoilers,
                )
            }
        }

        @Test
        fun `restores snapshot via restoreOptimisticWrite when remote throws non-cancellation error`() = runTest {
            // ----- Arrange -----
            val userBookId = 5
            val bookId = 10
            val body = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Review that will fail")))))
            val hasSpoilers = true
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns stubUserBookForReview(id = userBookId)
            }
            val snapshot = stubBook(userBookId = userBookId)
            val remoteError = RuntimeException("network failure")

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.updateBookReview(
                    userBook = any(),
                    review = any(),
                    hasSpoilers = any(),
                    reviewedAt = any(),
                )
            } throws remoteError

            // ----- Act -----
            val caught = runCatching {
                repository.updateBookReview(
                    book = book,
                    review = body,
                    hasSpoilers = hasSpoilers,
                )
            }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `does not resurrect a finished book — optimistic write carries the cached Read status, not the passed-in Reading status`() =
            runTest {
                // ----- Arrange -----
                val userBookId = 5
                val bookId = 10
                val body = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Finished it, loved it")))))
                val hasSpoilers = false

                // The cached snapshot already reflects that the book was finished.
                val cachedBook = Book(
                    id = bookId,
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
                    userBook = stubUserBookForReview(
                        id = userBookId,
                        status = BookStatus.Read,
                    ),
                )

                // The caller passes a pre-finish snapshot still marked Reading (e.g. the Reading
                // screen's finish -> verdict prompt flow).
                val passedBook = Book(
                    id = bookId,
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
                    userBook = stubUserBookForReview(
                        id = userBookId,
                        status = BookStatus.Reading,
                    ),
                )
                val remoteBook = stubBook(userBookId = userBookId)
                val capturedBooks = mutableListOf<Book>()

                every {
                    networkAvailability.isOnline
                } returns MutableStateFlow(true)

                coEvery {
                    booksLocalDataSource.getBookById(id = bookId)
                } returns cachedBook

                coEvery {
                    booksRemoteDataSource.updateBookReview(
                        userBook = any(),
                        review = any(),
                        hasSpoilers = any(),
                        reviewedAt = any(),
                    )
                } returns remoteBook

                coEvery {
                    booksLocalDataSource.cacheBook(book = capture(capturedBooks))
                } returns Unit

                // ----- Act -----
                repository.updateBookReview(
                    book = passedBook,
                    review = body,
                    hasSpoilers = hasSpoilers,
                )

                // ----- Assert -----
                val optimisticWrite = capturedBooks.first()
                optimisticWrite.userBook?.status shouldBe BookStatus.Read
                optimisticWrite.userBook?.reviewDocument shouldBe body
                optimisticWrite.userBook?.reviewHasSpoilers shouldBe hasSpoilers
            }
    }

    @Nested
    inner class StageBookRemoval {
        @Test
        fun `removes the user books locally and returns the cached snapshots`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val cached = stagedBook(
                bookId = 1,
                userBookId = 11,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns cached

            // ----- Act -----
            val staged = repository.stageBookRemoval(books = listOf(book))

            // ----- Assert -----
            staged.books shouldBe listOf(book)
            staged.snapshots shouldBe listOf(cached)

            coVerify(exactly = 1) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(11))
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.removeBookFromLibrary(book = any())
            }
        }

        @Test
        fun `falls back to the passed-in book when the cache has none`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            // ----- Act -----
            val staged = repository.stageBookRemoval(books = listOf(book))

            // ----- Assert -----
            staged.snapshots shouldBe listOf(book)
        }

        @Test
        fun `drops books that an earlier removal still holds staged`() = runTest {
            // ----- Arrange -----
            val first = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val second = stagedBook(
                bookId = 2,
                userBookId = 22,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = any())
            } returns null

            repository.stageBookRemoval(books = listOf(first))

            // ----- Act -----
            val staged = repository.stageBookRemoval(books = listOf(first, second))

            // ----- Assert -----
            staged.books shouldBe listOf(second)
            staged.snapshots shouldBe listOf(second)

            coVerify(exactly = 1) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(11))
            }

            coVerify(exactly = 1) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(22))
            }
        }

        @Test
        fun `skips the local delete when no staged book has a user book`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = null,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            // ----- Act -----
            val staged = repository.stageBookRemoval(books = listOf(book))

            // ----- Assert -----
            staged.books shouldBe listOf(book)

            coVerify(exactly = 0) {
                booksLocalDataSource.removeUserBooksById(ids = any())
            }
        }

        @Test
        fun `a local failure unstages the books and rethrows`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val localError = RuntimeException("disk full")

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            coEvery {
                booksLocalDataSource.removeUserBooksById(ids = listOf(11))
            } throws localError

            // ----- Act -----
            val caught = runCatching {
                repository.stageBookRemoval(books = listOf(book))
            }
            repository.cacheBook(book = book)

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe localError

            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = book)
            }
        }

        @Test
        fun `a failed snapshot read unstages the books and rethrows`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val localError = RuntimeException("read failed")

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } throws localError

            // ----- Act -----
            val caught = runCatching {
                repository.stageBookRemoval(books = listOf(book))
            }
            repository.cacheBook(book = book)

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe localError

            coVerify(exactly = 0) {
                booksLocalDataSource.removeUserBooksById(ids = any())
            }

            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = book)
            }
        }
    }

    @Nested
    inner class UndoBookRemoval {
        @Test
        fun `restores the snapshot when the cache has no user book for it`() = runTest {
            // ----- Arrange -----
            val snapshot = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val staged = StagedBookRemoval(
                books = listOf(snapshot),
                snapshots = listOf(snapshot),
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            // ----- Act -----
            repository.undoBookRemoval(staged = staged)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `restores the snapshot when the cached book has lost its user book`() = runTest {
            // ----- Arrange -----
            val snapshot = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val staged = StagedBookRemoval(
                books = listOf(snapshot),
                snapshots = listOf(snapshot),
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns stagedBook(
                bookId = 1,
                userBookId = null,
            )

            // ----- Act -----
            repository.undoBookRemoval(staged = staged)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `does not restore a snapshot the user re-added in the meantime`() = runTest {
            // ----- Arrange -----
            val snapshot = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val staged = StagedBookRemoval(
                books = listOf(snapshot),
                snapshots = listOf(snapshot),
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns stagedBook(
                bookId = 1,
                userBookId = 99,
            )

            // ----- Act -----
            repository.undoBookRemoval(staged = staged)

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = any())
            }
        }

        @Test
        fun `unstages the books even when a restore throws`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val restoreError = RuntimeException("restore failed")

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            val staged = repository.stageBookRemoval(books = listOf(book))

            coEvery {
                booksLocalDataSource.cacheBook(book = book)
            } throws restoreError

            // ----- Act -----
            val caught = runCatching {
                repository.undoBookRemoval(staged = staged)
            }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe restoreError

            val restaged = repository.stageBookRemoval(books = listOf(book))
            restaged.books shouldBe listOf(book)
        }
    }

    @Nested
    inner class CommitBookRemoval {
        @Test
        fun `a remote success re-runs the local delete and reports no failures`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val staged = StagedBookRemoval(
                books = listOf(book),
                snapshots = listOf(book),
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } returns Unit

            // ----- Act -----
            val failed = repository.commitBookRemoval(staged = staged)

            // ----- Assert -----
            failed shouldBe emptyList()

            coVerify(exactly = 1) {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            }

            coVerify(exactly = 1) {
                booksLocalDataSource.removeUserBooksById(ids = listOf(11))
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = any())
            }
        }

        @Test
        fun `a remote failure restores the snapshot and returns the book as failed`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val staged = StagedBookRemoval(
                books = listOf(book),
                snapshots = listOf(book),
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } throws RuntimeException("network error")

            // ----- Act -----
            val failed = repository.commitBookRemoval(staged = staged)

            // ----- Assert -----
            failed shouldBe listOf(book)

            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = book)
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.removeUserBooksById(ids = any())
            }
        }

        @Test
        fun `a remote failure does not restore a book re-added under a new user book`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val staged = StagedBookRemoval(
                books = listOf(book),
                snapshots = listOf(book),
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns stagedBook(
                bookId = 1,
                userBookId = 99,
            )

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } throws RuntimeException("network error")

            // ----- Act -----
            val failed = repository.commitBookRemoval(staged = staged)

            // ----- Assert -----
            failed shouldBe listOf(book)

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = any())
            }
        }

        @Test
        fun `one failure does not stop the remaining books from being removed`() = runTest {
            // ----- Arrange -----
            val failing = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val succeeding = stagedBook(
                bookId = 2,
                userBookId = 22,
            )
            val staged = StagedBookRemoval(
                books = listOf(failing, succeeding),
                snapshots = listOf(failing, succeeding),
            )

            coEvery {
                booksLocalDataSource.getBookById(id = any())
            } returns null

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = failing)
            } throws RuntimeException("network error")

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = succeeding)
            } returns Unit

            // ----- Act -----
            val failed = repository.commitBookRemoval(staged = staged)

            // ----- Assert -----
            failed shouldBe listOf(failing)

            coVerify(exactly = 1) {
                booksRemoteDataSource.removeBookFromLibrary(book = succeeding)
            }
        }

        @Test
        fun `a cancellation propagates and is not reported as a failed book`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val staged = StagedBookRemoval(
                books = listOf(book),
                snapshots = listOf(book),
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } throws CancellationException("cancelled")

            // ----- Act -----
            val error = shouldThrow<CancellationException> {
                repository.commitBookRemoval(staged = staged)
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<CancellationException>()

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = any())
            }
        }

        @Test
        fun `skips the remote call for a book back in the cache under the same user book`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val staged = StagedBookRemoval(
                books = listOf(book),
                snapshots = listOf(book),
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns stagedBook(
                bookId = 1,
                userBookId = 11,
            )

            // ----- Act -----
            val failed = repository.commitBookRemoval(staged = staged)

            // ----- Assert -----
            failed shouldBe emptyList()

            coVerify(exactly = 0) {
                booksRemoteDataSource.removeBookFromLibrary(book = any())
            }
        }

        @Test
        fun `still removes a book re-added under a different user book`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val staged = StagedBookRemoval(
                books = listOf(book),
                snapshots = listOf(book),
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns stagedBook(
                bookId = 1,
                userBookId = 99,
            )

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } returns Unit

            // ----- Act -----
            repository.commitBookRemoval(staged = staged)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            }
        }

        @Test
        fun `unstages the books afterwards so they can be cached again`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } returns Unit

            val staged = repository.stageBookRemoval(books = listOf(book))

            // ----- Act -----
            repository.commitBookRemoval(staged = staged)
            repository.cacheBook(book = book)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = book)
            }
        }
    }

    @Nested
    inner class StagedRemovalSyncGuard {
        @Test
        fun `cacheBook drops a staged book`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            repository.stageBookRemoval(books = listOf(book))

            // ----- Act -----
            repository.cacheBook(book = book)

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = book)
            }
        }

        @Test
        fun `cacheBook caches a book that is not staged`() = runTest {
            // ----- Arrange -----
            val staged = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val other = stagedBook(
                bookId = 2,
                userBookId = 22,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = any())
            } returns null

            repository.stageBookRemoval(books = listOf(staged))

            // ----- Act -----
            repository.cacheBook(book = other)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = other)
            }
        }

        @Test
        fun `cacheBook caches the book again after an undo`() = runTest {
            // ----- Arrange -----
            val book = stagedBook(
                bookId = 1,
                userBookId = 11,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            val staged = repository.stageBookRemoval(books = listOf(book))
            repository.undoBookRemoval(staged = staged)

            // ----- Act -----
            repository.cacheBook(book = book)

            // ----- Assert -----
            coVerify(atLeast = 2) {
                booksLocalDataSource.cacheBook(book = book)
            }
        }

        @Test
        fun `refreshUserBooks leaves a staged book out of the cached batch`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val stagedBook = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val otherBook = stagedBook(
                bookId = 2,
                userBookId = 22,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } coAnswers {
                val reconciled = listOf(stagedBook, otherBook)
                val selectBooksToCache = secondArg<suspend (List<Book>) -> List<Book>>()
                booksLocalDataSource.cacheBooks(books = selectBooksToCache(reconciled))
                reconciled
            }

            repository.stageBookRemoval(books = listOf(stagedBook))

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBooks(books = listOf(otherBook))
            }
        }

        @Test
        fun `hydrateReferencedBooks leaves a staged book out of the cached batch`() = runTest {
            // ----- Arrange -----
            val stagedBook = stagedBook(
                bookId = 1,
                userBookId = 11,
            )
            val otherBook = stagedBook(
                bookId = 2,
                userBookId = 22,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = 1)
            } returns null

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(
                    ids = listOf(1, 2),
                    forceNetwork = true,
                )
            } returns listOf(stagedBook, otherBook)

            repository.stageBookRemoval(books = listOf(stagedBook))

            // ----- Act -----
            repository.hydrateReferencedBooks(
                bookIds = listOf(1, 2),
                editionIds = emptyList(),
                forceNetwork = true,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBooks(books = listOf(otherBook))
            }
        }
    }

    @Nested
    inner class UpdateBookProgress {
        private fun historyBook(): Book = mockk(relaxed = true) {
            every {
                this@mockk.id
            } returns 42
        }

        private fun stubInsertPending(localId: Long) {
            coEvery {
                progressHistoryLocalDataSource.insertPending(
                    bookId = any(),
                    actionAtEpochMillis = any(),
                    pages = any(),
                    seconds = any(),
                )
            } returns localId
        }

        @Test
        fun `inserts a pending history row with the passed actionAt as epoch millis and the new pages and seconds`() = runTest {
            // ----- Arrange -----
            val book = historyBook()
            stubInsertPending(localId = 9L)

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } returns stubBook(userBookId = 1)

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 120,
                newSeconds = 3000,
                actionAt = "2026-07-21T21:00:00Z",
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.insertPending(
                    bookId = 42,
                    actionAtEpochMillis = 1784667600000L,
                    pages = 120,
                    seconds = 3000,
                )
            }
        }

        @Test
        fun `inserts the pending history row stamped with the current time when actionAt is null`() = runTest {
            // ----- Arrange -----
            val book = historyBook()
            val actionAtSlot = slot<Long>()
            val before = Clock.System.now()
            stubInsertPending(localId = 9L)

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } returns stubBook(userBookId = 1)

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 10,
                actionAt = null,
            )

            // ----- Assert -----
            coVerify {
                progressHistoryLocalDataSource.insertPending(
                    bookId = 42,
                    actionAtEpochMillis = capture(actionAtSlot),
                    pages = 10,
                    seconds = null,
                )
            }

            val stamped = Instant.fromEpochMilliseconds(actionAtSlot.captured)
            (stamped >= before) shouldBe true
            (stamped <= Clock.System.now()) shouldBe true
        }

        @Test
        fun `marks the inserted history row synced when the direct remote write succeeds`() = runTest {
            // ----- Arrange -----
            val book = historyBook()
            stubInsertPending(localId = 9L)

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } returns stubBook(userBookId = 1)

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 10,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.markSynced(localId = 9L)
            }

            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.delete(localId = any())
            }
        }

        @Test
        fun `leaves the history row pending when the remote write fails with a retryable error`() = runTest {
            // ----- Arrange -----
            val book = historyBook()
            stubInsertPending(localId = 9L)

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } throws ServerUnavailableException(message = "down")

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 10,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.markSynced(localId = any())
            }

            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.delete(localId = any())
            }

            coVerify(exactly = 1) {
                offlineSync.enqueueProgressUpdate(
                    book = any(),
                    newPage = 10,
                    newSeconds = null,
                    actionAt = any(),
                    progressHistoryId = 9L,
                )
            }
        }

        @Test
        fun `leaves the history row pending when offline`() = runTest {
            // ----- Arrange -----
            val book = historyBook()
            stubInsertPending(localId = 9L)

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 10,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.insertPending(
                    bookId = 42,
                    actionAtEpochMillis = any(),
                    pages = 10,
                    seconds = null,
                )
            }

            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.markSynced(localId = any())
            }

            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.delete(localId = any())
            }

            coVerify(exactly = 1) {
                offlineSync.enqueueProgressUpdate(
                    book = any(),
                    newPage = 10,
                    newSeconds = null,
                    actionAt = any(),
                    progressHistoryId = 9L,
                )
            }
        }

        @Test
        fun `rolls the history row back when the remote write fails with a non-retryable error`() = runTest {
            // ----- Arrange -----
            val book = historyBook()
            val remoteError = RuntimeException("rejected")
            stubInsertPending(localId = 9L)

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } throws remoteError

            // ----- Act -----
            val caught = runCatching {
                repository.updateBookProgress(
                    book = book,
                    newPage = 10,
                )
            }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.delete(localId = 9L)
            }

            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.markSynced(localId = any())
            }
        }

        @Test
        fun `throws and writes nothing when the book has no user book read`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)

            // ----- Act -----
            shouldThrow<Exception> {
                repository.updateBookProgress(
                    book = book,
                    newPage = 10,
                )
            }

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                progressHistoryLocalDataSource.insertPending(
                    bookId = any(),
                    actionAtEpochMillis = any(),
                    pages = any(),
                    seconds = any(),
                )
            }

            coVerify(exactly = 0) {
                offlineSync.enqueueProgressUpdate(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                    progressHistoryId = any(),
                )
            }
        }

        @Test
        fun `stamps the current time into the remote write, the history row and the queued write when actionAt is null`() = runTest {
            // ----- Arrange -----
            val book = historyBook()
            val remoteSlot = slot<String>()
            val millisSlot = slot<Long>()
            val enqueueSlot = slot<String>()
            val before = Clock.System.now()
            stubInsertPending(localId = 9L)

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = capture(remoteSlot),
                )
            } throws ServerUnavailableException(message = "down")

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 10,
                actionAt = null,
            )

            // ----- Assert -----
            coVerify {
                progressHistoryLocalDataSource.insertPending(
                    bookId = 42,
                    actionAtEpochMillis = capture(millisSlot),
                    pages = 10,
                    seconds = null,
                )
            }

            coVerify {
                offlineSync.enqueueProgressUpdate(
                    book = any(),
                    newPage = 10,
                    newSeconds = null,
                    actionAt = capture(enqueueSlot),
                    progressHistoryId = 9L,
                )
            }

            Instant.parse(remoteSlot.captured).toEpochMilliseconds() shouldBe millisSlot.captured
            Instant.parse(enqueueSlot.captured).toEpochMilliseconds() shouldBe millisSlot.captured
            (Instant.fromEpochMilliseconds(millisSlot.captured) >= before) shouldBe true
        }

        @Test
        fun `stamps the current time into the queued write and the history row when offline and actionAt is null`() = runTest {
            // ----- Arrange -----
            val book = historyBook()
            val millisSlot = slot<Long>()
            val enqueueSlot = slot<String>()
            stubInsertPending(localId = 9L)

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 10,
                actionAt = null,
            )

            // ----- Assert -----
            coVerify {
                progressHistoryLocalDataSource.insertPending(
                    bookId = 42,
                    actionAtEpochMillis = capture(millisSlot),
                    pages = 10,
                    seconds = null,
                )
            }

            coVerify {
                offlineSync.enqueueProgressUpdate(
                    book = any(),
                    newPage = 10,
                    newSeconds = null,
                    actionAt = capture(enqueueSlot),
                    progressHistoryId = 9L,
                )
            }

            Instant.parse(enqueueSlot.captured).toEpochMilliseconds() shouldBe millisSlot.captured
        }

        @Test
        fun `a passed actionAt reaches the remote write, the history row and the queued write as the same instant`() = runTest {
            // ----- Arrange -----
            val book = historyBook()
            val actionAt = "2026-07-21T21:00:00Z"
            stubInsertPending(localId = 9L)

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = actionAt,
                )
            } throws ServerUnavailableException(message = "down")

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 10,
                actionAt = actionAt,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = 10,
                    newSeconds = any(),
                    actionAt = actionAt,
                )
            }

            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.insertPending(
                    bookId = 42,
                    actionAtEpochMillis = 1784667600000L,
                    pages = 10,
                    seconds = null,
                )
            }

            coVerify(exactly = 1) {
                offlineSync.enqueueProgressUpdate(
                    book = any(),
                    newPage = 10,
                    newSeconds = null,
                    actionAt = actionAt,
                    progressHistoryId = 9L,
                )
            }
        }

        @Test
        fun `updateBookProgress forwards actionAt to remote data source`() = runTest {
            // ----- Arrange -----
            val book = historyBook()
            val newPage = 150
            val actionAt = "2026-07-21T21:00:00Z"
            val expectedBook = historyBook()

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = book,
                    newPage = newPage,
                    actionAt = actionAt,
                )
            } returns expectedBook

            // ----- Act -----
            val result = repository.updateBookProgress(
                book = book,
                newPage = newPage,
                actionAt = actionAt,
            )

            // ----- Assert -----
            result shouldBe expectedBook
        }

        @Test
        fun `appends a progress_updated journal entry to the optimistic local cache write`() = runTest {
            // ----- Arrange -----
            val existingJournal = ReadingJournal(
                updatedAt = "2026-01-01T10:00:00",
                event = "user_book_read_started",
            )
            val userBook = UserBook(
                id = 1,
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
                journals = listOf(existingJournal),
                reads = emptyList(),
            )
            val userBookRead = UserBookRead(
                id = 1,
                currentPage = 50,
                currentSeconds = null,
                progress = 0.5f,
                startedAt = null,
                finishedAt = null,
            )
            val book = Book(
                id = 42,
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
                userBook = userBook.copy(reads = listOf(userBookRead)),
            )
            val slot = slot<Book>()

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } returns stubBook(userBookId = 1)

            coEvery {
                booksLocalDataSource.cacheBook(book = capture(slot))
            } returns Unit

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 75,
            )

            // ----- Assert -----
            val capturedJournals = slot.captured.userBook.shouldNotBeNull().journals
            capturedJournals.size shouldBe 2
            capturedJournals[0] shouldBe existingJournal
            capturedJournals[1].event shouldBe "progress_updated"
            capturedJournals[1].updatedAt.isNotEmpty() shouldBe true
        }

        @Test
        fun `updateBookProgress when online and remote throws non-offline error restores snapshot to local cache and rethrows`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }
            val remoteError = RuntimeException("network failure")

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } throws remoteError

            // ----- Act -----
            val caught = runCatching {
                repository.updateBookProgress(
                    book = book,
                    newPage = 50,
                )
            }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `updateBookProgress when online and remote throws OfflineException does NOT restore snapshot`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } throws OfflineException()

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 50,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `updateBookProgress when online and remote throws ServerUnavailableException does NOT restore snapshot`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = book,
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } throws ServerUnavailableException("server error")

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = 50,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }

            coVerify {
                offlineSync.enqueueProgressUpdate(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                    progressHistoryId = any(),
                )
            }
        }

        private fun readingBook(read: UserBookRead): Book {
            val userBook = baseBook.userBook.shouldNotBeNull()

            return baseBook.copy(
                userBook = userBook.copy(
                    status = BookStatus.Reading,
                    reads = listOf(read),
                ),
            )
        }

        @Test
        fun `placeholder current read — caches optimistically, records history and enqueues without calling the remote`() = runTest {
            // ----- Arrange -----
            val placeholderRead = UserBookRead(
                id = -1,
                currentPage = null,
                currentSeconds = null,
                progress = 0f,
                startedAt = "2026-07-21",
                finishedAt = null,
            )
            val book = readingBook(read = placeholderRead)
            val cached = slot<Book>()
            stubInsertPending(localId = 9L)

            coEvery {
                booksLocalDataSource.cacheBook(book = capture(cached))
            } returns Unit

            // ----- Act -----
            val result = repository.updateBookProgress(
                book = book,
                newPage = 25,
                actionAt = "2026-07-21T21:00:00Z",
            )

            // ----- Assert -----
            cached.captured.currentRead.shouldNotBeNull().currentPage shouldBe 25
            result shouldBe cached.captured

            coVerify(exactly = 1) {
                progressHistoryLocalDataSource.insertPending(
                    bookId = book.id,
                    actionAtEpochMillis = any(),
                    pages = 25,
                    seconds = null,
                )
            }

            coVerify(exactly = 1) {
                offlineSync.enqueueProgressUpdate(
                    book = cached.captured,
                    newPage = 25,
                    newSeconds = null,
                    actionAt = "2026-07-21T21:00:00Z",
                    progressHistoryId = 9L,
                )
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `real current read — calls the remote with that read and does not enqueue`() = runTest {
            // ----- Arrange -----
            val realRead = UserBookRead(
                id = 7,
                currentPage = 10,
                currentSeconds = null,
                progress = 10f,
                startedAt = "2026-07-21",
                finishedAt = null,
            )
            val book = readingBook(read = realRead)
            val remoteBook = stubBook(userBookId = 1)
            stubInsertPending(localId = 9L)

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } returns remoteBook

            // ----- Act -----
            val result = repository.updateBookProgress(
                book = book,
                newPage = 25,
                actionAt = "2026-07-21T21:00:00Z",
            )

            // ----- Assert -----
            result shouldBe remoteBook

            coVerify(exactly = 1) {
                booksRemoteDataSource.updateBookProgress(
                    book = book,
                    newPage = 25,
                    newSeconds = null,
                    actionAt = "2026-07-21T21:00:00Z",
                )
            }

            coVerify(exactly = 0) {
                offlineSync.enqueueProgressUpdate(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                    progressHistoryId = any(),
                )
            }
        }
    }

    @Nested
    inner class MarkBookAsRead {
        @Test
        fun `forwards non-null editionId to remote data source`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val editionId = 77
            val expectedBook = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = editionId,
                )
            } returns expectedBook

            // ----- Act -----
            repository.markBookAsRead(
                book = book,
                editionId = editionId,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = editionId,
                )
            }
        }

        @Test
        fun `forwards a non-null actionAt to remote data source`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val actionAt = "2026-07-21T21:00:00Z"
            val expectedBook = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = null,
                    actionAt = actionAt,
                )
            } returns expectedBook

            // ----- Act -----
            repository.markBookAsRead(
                book = book,
                actionAt = actionAt,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = null,
                    actionAt = actionAt,
                )
            }
        }

        @Test
        fun `when userBook is null no optimistic cacheBook happens but the remote write still runs`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)
            val expectedBook = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = null,
                )
            } returns expectedBook

            // ----- Act -----
            val result = repository.markBookAsRead(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = any())
            }
            coVerify(exactly = 1) {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = null,
                )
            }
        }

        @Test
        fun `appends a user_book_read_finished journal entry to the optimistic local cache write`() = runTest {
            // ----- Arrange -----
            val existingJournal = ReadingJournal(
                updatedAt = "2026-01-01T10:00:00",
                event = "user_book_read_started",
            )
            val userBook = UserBook(
                id = 1,
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
                journals = listOf(existingJournal),
                reads = emptyList(),
            )
            val userBookRead = UserBookRead(
                id = 1,
                currentPage = 50,
                currentSeconds = null,
                progress = 0.5f,
                startedAt = null,
                finishedAt = null,
            )
            val book = Book(
                id = 42,
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
                userBook = userBook.copy(reads = listOf(userBookRead)),
            )
            val slot = slot<Book>()

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = any(),
                    editionId = any(),
                )
            } returns stubBook(userBookId = 1)

            coEvery {
                booksLocalDataSource.cacheBook(book = capture(slot))
            } returns Unit

            // ----- Act -----
            repository.markBookAsRead(book = book)

            // ----- Assert -----
            val capturedUserBook = slot.captured.userBook.shouldNotBeNull()
            val capturedJournals = capturedUserBook.journals
            capturedJournals.size shouldBe 2
            capturedJournals[0] shouldBe existingJournal
            capturedJournals[1].event shouldBe "user_book_read_finished"
            capturedJournals[1].updatedAt.isNotEmpty() shouldBe true
            capturedUserBook.status shouldBe BookStatus.Read
        }

        @Test
        fun `sets optimistic finishedAt to actionAt's local date on a backdated finish`() = runTest {
            // ----- Arrange -----
            val actionAt = "2026-07-21T21:00:00Z"
            val expectedFinishedAt = Instant.parse(actionAt).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val userBook = UserBook(
                id = 1,
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
            val userBookRead = UserBookRead(
                id = 1,
                currentPage = 50,
                currentSeconds = null,
                progress = 0.5f,
                startedAt = null,
                finishedAt = null,
            )
            val book = Book(
                id = 42,
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
                userBook = userBook.copy(reads = listOf(userBookRead)),
            )
            val slot = slot<Book>()

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = any(),
                    editionId = any(),
                    actionAt = any(),
                )
            } returns stubBook(userBookId = 1)

            coEvery {
                booksLocalDataSource.cacheBook(book = capture(slot))
            } returns Unit

            // ----- Act -----
            repository.markBookAsRead(
                book = book,
                actionAt = actionAt,
            )

            // ----- Assert -----
            slot.captured.currentRead?.finishedAt shouldBe expectedFinishedAt
        }

        @Test
        fun `markBookAsRead when online and remote throws non-offline error restores snapshot to local cache and rethrows`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }
            val remoteError = RuntimeException("network failure")

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = any(),
                )
            } throws remoteError

            // ----- Act -----
            val caught = runCatching { repository.markBookAsRead(book = book) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `markBookAsRead when online and remote throws OfflineException does NOT restore snapshot`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
            }

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = any(),
                )
            } throws OfflineException()

            // ----- Act -----
            repository.markBookAsRead(book = book)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `markBookAsRead when online and remote throws ServerUnavailableException enqueues MARK_AS_READ, does NOT restore snapshot`() =
            runTest {
                // ----- Arrange -----
                val bookId = 42
                val book = mockk<Book>(relaxed = true) {
                    every {
                        this@mockk.id
                    } returns bookId
                }
                val snapshot = mockk<Book>(relaxed = true) {
                    every {
                        this@mockk.id
                    } returns bookId
                }

                every {
                    networkAvailability.isOnline
                } returns MutableStateFlow(true)

                coEvery {
                    booksLocalDataSource.getBookById(id = bookId)
                } returns snapshot

                coEvery {
                    booksRemoteDataSource.markBookAsRead(
                        book = book,
                        editionId = any(),
                    )
                } throws ServerUnavailableException("server error")

                // ----- Act -----
                repository.markBookAsRead(book = book)

                // ----- Assert -----
                coVerify(exactly = 1) {
                    booksLocalDataSource.cacheBook(book = any())
                }

                coVerify(exactly = 0) {
                    booksLocalDataSource.cacheBook(book = snapshot)
                }

                coVerify { offlineSync.enqueueMarkAsRead(book = any()) }
            }

        private fun offShelfBook(): Book = baseBook.copy(userBook = null)

        private fun wantToReadBookWithoutReads(): Book {
            val userBook = baseBook.userBook.shouldNotBeNull()

            return baseBook.copy(
                userBook = userBook.copy(
                    status = BookStatus.WantToRead,
                    reads = emptyList(),
                ),
            )
        }

        @Test
        fun `markBookAsRead for an off-shelf book is not queued when offline`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            val book = offShelfBook()
            val remoteError = OfflineException()

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = any(),
                    actionAt = any(),
                )
            } throws remoteError

            // ----- Act -----
            val caught = runCatching { repository.markBookAsRead(book = book) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError
            coVerify(exactly = 0) {
                offlineSync.enqueueMarkAsRead(
                    book = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `markBookAsRead for an off-shelf book is not queued on a transient server error`() = runTest {
            // ----- Arrange -----
            val book = offShelfBook()
            val remoteError = ServerUnavailableException("503")

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = any(),
                    actionAt = any(),
                )
            } throws remoteError

            // ----- Act -----
            val caught = runCatching { repository.markBookAsRead(book = book) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError
            coVerify(exactly = 0) {
                offlineSync.enqueueMarkAsRead(
                    book = any(),
                    actionAt = any(),
                )
            }
        }

        @Test
        fun `markBookAsRead for an off-shelf book restores the snapshot and rethrows a non-transient error`() =
            runTest {
                // ----- Arrange -----
                val book = offShelfBook()
                val snapshot = baseBook
                val remoteError = RuntimeException("rejected")

                coEvery {
                    booksLocalDataSource.getBookById(id = book.id)
                } returns snapshot

                coEvery {
                    booksRemoteDataSource.markBookAsRead(
                        book = book,
                        editionId = any(),
                        actionAt = any(),
                    )
                } throws remoteError

                // ----- Act -----
                val caught = runCatching { repository.markBookAsRead(book = book) }

                // ----- Assert -----
                caught.exceptionOrNull() shouldBe remoteError
                coVerify(exactly = 1) { booksLocalDataSource.cacheBook(book = snapshot) }
                coVerify(exactly = 0) {
                    offlineSync.enqueueMarkAsRead(
                        book = any(),
                        actionAt = any(),
                    )
                }
            }

        @Test
        fun `markBookAsRead for a shelved Want to Read book with no reads queues MARK_AS_READ when offline`() =
            runTest {
                // ----- Arrange -----
                every {
                    networkAvailability.isOnline
                } returns MutableStateFlow(false)

                val book = wantToReadBookWithoutReads()

                // ----- Act -----
                val result = repository.markBookAsRead(book = book)

                // ----- Assert -----
                result.userBook.shouldNotBeNull().status shouldBe BookStatus.Read
                coVerify(exactly = 1) {
                    offlineSync.enqueueMarkAsRead(
                        book = result,
                        actionAt = null,
                    )
                }
                coVerify(exactly = 0) {
                    booksRemoteDataSource.markBookAsRead(
                        book = any(),
                        editionId = any(),
                        actionAt = any(),
                    )
                }
            }

        @Test
        fun `markBookAsRead records the confirmed server book as a MARK_AS_READ write`() = runTest {
            // ----- Arrange -----
            val gate = mockk<UserBookCacheGate>(relaxed = true)
            val gatedRepository = BooksRepositoryImpl(
                booksRemoteDataSource = booksRemoteDataSource,
                booksLocalDataSource = booksLocalDataSource,
                userBookCacheGate = gate,
                networkAvailability = networkAvailability,
                offlineSync = offlineSync,
                applicationScope = ApplicationScope(scope = testScope),
                appDispatchers = appDispatchers,
                progressHistoryLocalDataSource = progressHistoryLocalDataSource,
            )
            val book = offShelfBook()
            val confirmed = baseBook

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = any(),
                    actionAt = any(),
                )
            } returns confirmed

            // ----- Act -----
            val result = gatedRepository.markBookAsRead(book = book)

            // ----- Assert -----
            result shouldBe confirmed
            coVerify(exactly = 1) {
                gate.recordWrite(
                    book = confirmed,
                    kind = PendingUserBookWriteKind.MARK_AS_READ,
                )
            }
        }
    }

    @Nested
    inner class FetchEditionMatchesForIsbns {
        @Test
        fun `offline — throws OfflineException without calling remote`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            val isbn = "9780451524935"

            // ----- Act -----
            val error = shouldThrow<OfflineException> {
                repository.fetchEditionMatchesForIsbns(isbns = listOf(isbn))
            }

            // ----- Assert -----
            error.shouldBeInstanceOf<OfflineException>()

            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchEditionMatchesForIsbns(isbns = any())
            }
        }

        @Test
        fun `empty isbns list — returns emptyMap without calling remote, even offline`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            // ----- Act -----
            val result = repository.fetchEditionMatchesForIsbns(isbns = emptyList())

            // ----- Assert -----
            result shouldBe emptyMap()

            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchEditionMatchesForIsbns(isbns = any())
            }
        }
    }

    @Nested
    inner class OfflineBehavior {
        private fun stubUserBook(
            id: Int = 1,
            reads: List<UserBookRead> = emptyList(),
        ): UserBook = UserBook(
            id = id,
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
            reads = reads,
        )

        private fun stubUserBookRead(id: Int = 1): UserBookRead = UserBookRead(
            id = id,
            currentPage = 50,
            currentSeconds = null,
            progress = 0.5f,
            startedAt = null,
            finishedAt = null,
        )

        private fun stubBookWithUserBookRead(
            userBookId: Int = 1,
            userBookReadId: Int = 1,
        ): Book = Book(
            id = 42,
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
            userBook = stubUserBook(
                id = userBookId,
                reads = listOf(stubUserBookRead(id = userBookReadId)),
            ),
        )

        @Test
        fun `updateBookProgress when offline caches optimistic book and enqueues UPDATE_PROGRESS without calling remote`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            val book = stubBookWithUserBookRead()
            val newPage = 75
            val actionAt = "2026-07-21T21:00:00Z"

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = newPage,
                actionAt = actionAt,
            )

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify {
                offlineSync.enqueueProgressUpdate(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = actionAt,
                    progressHistoryId = any(),
                )
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                )
            }
        }

        @Test
        fun `markBookAsRead when offline caches optimistic book and enqueues MARK_AS_READ without calling remote`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            val book = stubBookWithUserBookRead()
            val actionAt = "2026-07-21T21:00:00Z"

            // ----- Act -----
            repository.markBookAsRead(
                book = book,
                actionAt = actionAt,
            )

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify {
                offlineSync.enqueueMarkAsRead(
                    book = any(),
                    actionAt = actionAt,
                )
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.markBookAsRead(
                    book = any(),
                    editionId = any(),
                )
            }
        }
    }

    @Nested
    inner class RefreshTrendingBooks {
        private fun trendingBook(id: Int): Book = mockk(relaxed = true) {
            every {
                this@mockk.id
            } returns id
        }

        private suspend fun refreshWeek(maxAge: Duration) = repository.refreshTrendingBooks(
            period = TrendingPeriod.Week,
            maxAge = maxAge,
        )

        @BeforeEach
        fun setUpStore() {
            coEvery {
                booksLocalDataSource.getTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns null

            coEvery {
                booksLocalDataSource.getExistingUserBookBookIds(ids = any())
            } returns emptyList()
        }

        @Test
        fun `skips the fetch when the last one is younger than maxAge`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns Clock.System.now() - 1.minutes

            // ----- Act -----
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Week,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            }
            coVerify(exactly = 0) {
                booksLocalDataSource.replaceTrendingBooks(
                    period = TrendingPeriod.Week,
                    requestedIds = any(),
                    books = any(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `fetches when the last fetch is older than maxAge`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns Clock.System.now() - 2.hours

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            } returns emptyList()

            // ----- Act -----
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Week,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            }
        }

        @Test
        fun `a zero maxAge always fetches even right after a fetch`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns Clock.System.now()

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            } returns emptyList()

            // ----- Act -----
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Week,
                maxAge = Duration.ZERO,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            }
        }

        @Test
        fun `a fetched-at in the future is stale and refreshes`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns Clock.System.now() + 1.hours

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            } returns emptyList()

            // ----- Act -----
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Week,
                maxAge = 2.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            }
        }

        @Test
        fun `an empty API result records the fetch time with an empty list`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            } returns emptyList()

            // ----- Act -----
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Week,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceTrendingBooks(
                    period = TrendingPeriod.Week,
                    requestedIds = emptyList(),
                    books = emptyList(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `an empty API result leaves the cached ranking alone when a fetch time exists`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns Clock.System.now() - 2.hours

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            } returns emptyList()

            // ----- Act -----
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Week,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksLocalDataSource.replaceTrendingBooks(
                    period = any(),
                    requestedIds = any(),
                    books = any(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `ignores duplicate ids from the API`() = runTest {
            // ----- Arrange -----
            val first = trendingBook(1)
            val second = trendingBook(2)

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            } returns listOf(1, 2, 1)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } returns listOf(first, second)

            // ----- Act -----
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Week,
                maxAge = Duration.ZERO,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(1, 2))
            }
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceTrendingBooks(
                    period = TrendingPeriod.Week,
                    requestedIds = listOf(1, 2),
                    books = listOf(first, second),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `does not re-fetch or re-cache library books but refreshes trending-only books`() = runTest {
            // ----- Arrange -----
            val trendingOnly = trendingBook(2)

            coEvery {
                booksLocalDataSource.getExistingUserBookBookIds(ids = any())
            } returns listOf(1)

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            } returns listOf(1, 2)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } returns listOf(trendingOnly)

            // ----- Act -----
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Week,
                maxAge = Duration.ZERO,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(2))
            }
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceTrendingBooks(
                    period = TrendingPeriod.Week,
                    requestedIds = listOf(1, 2),
                    books = listOf(trendingOnly),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `does not fetch books when every trending book is in the library`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getExistingUserBookBookIds(ids = any())
            } returns listOf(1, 2)

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            } returns listOf(2, 1)

            // ----- Act -----
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Week,
                maxAge = Duration.ZERO,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            }
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceTrendingBooks(
                    period = TrendingPeriod.Week,
                    requestedIds = listOf(2, 1),
                    books = emptyList(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `two concurrent refreshes with a stale fetch time fetch once`() = runTest {
            // ----- Arrange -----
            var storedFetchedAt: Instant? = Clock.System.now() - 2.hours
            val gate = CompletableDeferred<Unit>()

            coEvery {
                booksLocalDataSource.getTrendingFetchedAt(period = TrendingPeriod.Week)
            } answers {
                storedFetchedAt
            }

            coEvery {
                booksLocalDataSource.replaceTrendingBooks(
                    period = TrendingPeriod.Week,
                    requestedIds = any(),
                    books = any(),
                    fetchedAt = any(),
                )
            } answers {
                storedFetchedAt = arg<Instant>(3)
            }

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            } coAnswers {
                gate.await()
                listOf(1)
            }

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(1))
            } returns listOf(trendingBook(1))

            // ----- Act -----
            val first = launch { refreshWeek(maxAge = 1.hours) }
            val second = launch { refreshWeek(maxAge = 1.hours) }
            runCurrent()
            gate.complete(Unit)
            joinAll(
                first,
                second,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            }
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceTrendingBooks(
                    period = TrendingPeriod.Week,
                    requestedIds = any(),
                    books = any(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `checks maxAge against the requested period's fetch time only`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getTrendingFetchedAt(period = TrendingPeriod.Week)
            } returns Clock.System.now() - 1.minutes

            coEvery {
                booksLocalDataSource.getTrendingFetchedAt(period = TrendingPeriod.Month)
            } returns Clock.System.now() - 2.hours

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Month)
            } returns emptyList()

            // ----- Act -----
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Week,
                maxAge = 1.hours,
            )
            repository.refreshTrendingBooks(
                period = TrendingPeriod.Month,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            }
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Month)
            }
        }

        @Test
        fun `refreshes of different periods run concurrently`() = runTest {
            // ----- Arrange -----
            val gate = CompletableDeferred<Unit>()
            val started = mutableListOf<TrendingPeriod>()

            coEvery {
                booksLocalDataSource.getTrendingFetchedAt(period = any())
            } returns null

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = any())
            } coAnswers {
                started += firstArg<TrendingPeriod>()
                gate.await()
                emptyList()
            }

            // ----- Act -----
            val week = launch {
                repository.refreshTrendingBooks(
                    period = TrendingPeriod.Week,
                    maxAge = Duration.ZERO,
                )
            }
            val month = launch {
                repository.refreshTrendingBooks(
                    period = TrendingPeriod.Month,
                    maxAge = Duration.ZERO,
                )
            }
            runCurrent()
            val startedBeforeRelease = started.toList()
            gate.complete(Unit)
            joinAll(
                week,
                month,
            )

            // ----- Assert -----
            startedBeforeRelease shouldContainExactlyInAnyOrder listOf(TrendingPeriod.Week, TrendingPeriod.Month)
        }

        @Test
        fun `propagates a failing books fetch and writes nothing`() = runTest {
            // ----- Arrange -----
            val error = ServerUnavailableException(message = "down")

            coEvery {
                booksRemoteDataSource.fetchTrendingBookIds(period = TrendingPeriod.Week)
            } returns listOf(1)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } throws error

            // ----- Act -----
            val thrown = shouldThrow<ServerUnavailableException> {
                repository.refreshTrendingBooks(
                    period = TrendingPeriod.Week,
                    maxAge = Duration.ZERO,
                )
            }

            // ----- Assert -----
            thrown shouldBe error
            coVerify(exactly = 0) {
                booksLocalDataSource.replaceTrendingBooks(
                    period = TrendingPeriod.Week,
                    requestedIds = any(),
                    books = any(),
                    fetchedAt = any(),
                )
            }
        }
    }

    @Nested
    inner class RefreshUpcomingBooks {
        private val window = UpcomingWindow.ThreeMonths

        private fun upcomingBook(
            id: Int,
            releaseDate: LocalDate? = null,
        ): Book = mockk(relaxed = true) {
            every {
                this@mockk.id
            } returns id

            every {
                this@mockk.releaseDate
            } returns releaseDate
        }

        @BeforeEach
        fun setUpStore() {
            coEvery {
                booksLocalDataSource.getUpcomingFetchedAt(window = any())
            } returns null

            coEvery {
                booksLocalDataSource.getExistingUserBookBookIds(ids = any())
            } returns emptyList()
        }

        @Test
        fun `skips the fetch when the window's last fetch is younger than maxAge`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getUpcomingFetchedAt(window = window)
            } returns Clock.System.now() - 1.minutes

            // ----- Act -----
            repository.refreshUpcomingBooks(
                window = window,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchUpcomingBookIds(
                    window = any(),
                    limit = any(),
                )
            }
            coVerify(exactly = 0) {
                booksLocalDataSource.replaceUpcomingBooks(
                    window = any(),
                    requestedIds = any(),
                    books = any(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `fetches and replaces the window's list when the last fetch is older than maxAge`() = runTest {
            // ----- Arrange -----
            val first = upcomingBook(5)
            val second = upcomingBook(6)

            coEvery {
                booksLocalDataSource.getUpcomingFetchedAt(window = window)
            } returns Clock.System.now() - 2.hours

            coEvery {
                booksRemoteDataSource.fetchUpcomingBookIds(
                    window = window,
                    limit = any(),
                )
            } returns listOf(5, 6, 5)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } returns listOf(first, second)

            // ----- Act -----
            repository.refreshUpcomingBooks(
                window = window,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceUpcomingBooks(
                    window = window,
                    requestedIds = listOf(5, 6),
                    books = listOf(first, second),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `fetches when the window was never fetched`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRemoteDataSource.fetchUpcomingBookIds(
                    window = window,
                    limit = any(),
                )
            } returns listOf(9)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } returns listOf(upcomingBook(9))

            // ----- Act -----
            repository.refreshUpcomingBooks(
                window = window,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceUpcomingBooks(
                    window = window,
                    requestedIds = listOf(9),
                    books = any(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `an empty API result records the window's fetch time with an empty list`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRemoteDataSource.fetchUpcomingBookIds(
                    window = window,
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            repository.refreshUpcomingBooks(
                window = window,
                maxAge = Duration.ZERO,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceUpcomingBooks(
                    window = window,
                    requestedIds = emptyList(),
                    books = emptyList(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `an empty API result keeps only the cached books released after today and records the fetch time`() =
            runTest {
                // ----- Arrange -----
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val released = upcomingBook(
                    id = 1,
                    releaseDate = today.plus(DatePeriod(days = -1)),
                )
                val releasedToday = upcomingBook(
                    id = 2,
                    releaseDate = today,
                )
                val tomorrow = upcomingBook(
                    id = 3,
                    releaseDate = today.plus(DatePeriod(days = 1)),
                )
                val undated = upcomingBook(
                    id = 4,
                    releaseDate = null,
                )

                coEvery {
                    booksLocalDataSource.getUpcomingFetchedAt(window = window)
                } returns Clock.System.now() - 2.hours

                coEvery {
                    booksLocalDataSource.getUpcomingBooks(window = window)
                } returns listOf(
                    released,
                    releasedToday,
                    tomorrow,
                    undated,
                )

                coEvery {
                    booksRemoteDataSource.fetchUpcomingBookIds(
                        window = window,
                        limit = any(),
                    )
                } returns emptyList()

                // ----- Act -----
                repository.refreshUpcomingBooks(
                    window = window,
                    maxAge = 1.hours,
                )

                // ----- Assert -----
                coVerify(exactly = 1) {
                    booksLocalDataSource.replaceUpcomingBooks(
                        window = window,
                        requestedIds = listOf(3, 4),
                        books = emptyList(),
                        fetchedAt = any(),
                    )
                }
            }

        @Test
        fun `checks maxAge against the requested window's fetch time only`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getUpcomingFetchedAt(window = UpcomingWindow.Month)
            } returns Clock.System.now() - 1.minutes

            coEvery {
                booksLocalDataSource.getUpcomingFetchedAt(window = UpcomingWindow.Year)
            } returns Clock.System.now() - 2.hours

            coEvery {
                booksRemoteDataSource.fetchUpcomingBookIds(
                    window = UpcomingWindow.Year,
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            repository.refreshUpcomingBooks(
                window = UpcomingWindow.Month,
                maxAge = 1.hours,
            )
            repository.refreshUpcomingBooks(
                window = UpcomingWindow.Year,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchUpcomingBookIds(
                    window = UpcomingWindow.Month,
                    limit = any(),
                )
            }
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchUpcomingBookIds(
                    window = UpcomingWindow.Year,
                    limit = any(),
                )
            }
        }
    }

    @Nested
    inner class RefreshRecentBooks {
        private val window = RecentWindow.ThreeMonths

        private fun recentBook(
            id: Int,
            releaseDate: LocalDate? = null,
        ): Book = mockk(relaxed = true) {
            every {
                this@mockk.id
            } returns id

            every {
                this@mockk.releaseDate
            } returns releaseDate
        }

        @BeforeEach
        fun setUpStore() {
            coEvery {
                booksLocalDataSource.getRecentFetchedAt(window = any())
            } returns null

            coEvery {
                booksLocalDataSource.getExistingUserBookBookIds(ids = any())
            } returns emptyList()
        }

        @Test
        fun `skips the fetch when the window's last fetch is younger than maxAge`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getRecentFetchedAt(window = window)
            } returns Clock.System.now() - 1.minutes

            // ----- Act -----
            repository.refreshRecentBooks(
                window = window,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchRecentBookIds(
                    window = any(),
                    limit = any(),
                )
            }
            coVerify(exactly = 0) {
                booksLocalDataSource.replaceRecentBooks(
                    window = any(),
                    requestedIds = any(),
                    books = any(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `fetches and replaces the window's list when the last fetch is older than maxAge`() = runTest {
            // ----- Arrange -----
            val first = recentBook(5)
            val second = recentBook(6)

            coEvery {
                booksLocalDataSource.getRecentFetchedAt(window = window)
            } returns Clock.System.now() - 2.hours

            coEvery {
                booksRemoteDataSource.fetchRecentBookIds(
                    window = window,
                    limit = any(),
                )
            } returns listOf(5, 6, 5)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } returns listOf(first, second)

            // ----- Act -----
            repository.refreshRecentBooks(
                window = window,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceRecentBooks(
                    window = window,
                    requestedIds = listOf(5, 6),
                    books = listOf(first, second),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `an empty API result records the window's fetch time with an empty list`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRemoteDataSource.fetchRecentBookIds(
                    window = window,
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            repository.refreshRecentBooks(
                window = window,
                maxAge = Duration.ZERO,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceRecentBooks(
                    window = window,
                    requestedIds = emptyList(),
                    books = emptyList(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `an empty API result keeps only the cached books released after the window start`() = runTest {
            // ----- Arrange -----
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val windowStart = today.plus(DatePeriod(months = -3))
            val beforeWindow = recentBook(
                id = 1,
                releaseDate = windowStart.plus(DatePeriod(days = -1)),
            )
            val onWindowStart = recentBook(
                id = 2,
                releaseDate = windowStart,
            )
            val dayAfterWindowStart = recentBook(
                id = 3,
                releaseDate = windowStart.plus(DatePeriod(days = 1)),
            )
            val undated = recentBook(
                id = 4,
                releaseDate = null,
            )

            coEvery {
                booksLocalDataSource.getRecentFetchedAt(window = window)
            } returns Clock.System.now() - 2.hours

            coEvery {
                booksLocalDataSource.getRecentBooks(window = window)
            } returns listOf(
                beforeWindow,
                onWindowStart,
                dayAfterWindowStart,
                undated,
            )

            coEvery {
                booksRemoteDataSource.fetchRecentBookIds(
                    window = window,
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            repository.refreshRecentBooks(
                window = window,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceRecentBooks(
                    window = window,
                    requestedIds = listOf(3, 4),
                    books = emptyList(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `checks maxAge against the requested window's fetch time only`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getRecentFetchedAt(window = RecentWindow.Month)
            } returns Clock.System.now() - 1.minutes

            coEvery {
                booksLocalDataSource.getRecentFetchedAt(window = RecentWindow.SixMonths)
            } returns Clock.System.now() - 2.hours

            coEvery {
                booksRemoteDataSource.fetchRecentBookIds(
                    window = RecentWindow.SixMonths,
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            repository.refreshRecentBooks(
                window = RecentWindow.Month,
                maxAge = 1.hours,
            )
            repository.refreshRecentBooks(
                window = RecentWindow.SixMonths,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchRecentBookIds(
                    window = RecentWindow.Month,
                    limit = any(),
                )
            }
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchRecentBookIds(
                    window = RecentWindow.SixMonths,
                    limit = any(),
                )
            }
        }
    }

    @Nested
    inner class RefreshRecommendedBooks {
        private fun recommendedBook(id: Int): Book = mockk(relaxed = true) {
            every {
                this@mockk.id
            } returns id
        }

        @BeforeEach
        fun setUpStore() {
            coEvery {
                booksLocalDataSource.getSystemVibeFetchedAt(vibe = SystemVibe.Recommendations)
            } returns null

            coEvery {
                booksLocalDataSource.getSystemVibeBookIds(vibe = SystemVibe.Recommendations)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getExistingUserBookBookIds(ids = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchSystemVibeBookIds(
                    vibe = any(),
                    limit = any(),
                )
            } returns emptyList()
        }

        @Test
        fun `skips the fetch while the ranking is cached and younger than maxAge`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getSystemVibeFetchedAt(vibe = SystemVibe.Recommendations)
            } returns Clock.System.now() - 2.hours

            coEvery {
                booksLocalDataSource.getSystemVibeBookIds(vibe = SystemVibe.Recommendations)
            } returns listOf(1, 2)

            // ----- Act -----
            repository.refreshSystemVibeBooks(
                vibe = SystemVibe.Recommendations,
                maxAge = 24.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchSystemVibeBookIds(
                    vibe = any(),
                    limit = any(),
                )
            }
        }

        @Test
        fun `an unchanged ranking is re-stamped without hydrating the books again`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getSystemVibeFetchedAt(vibe = SystemVibe.Recommendations)
            } returns Clock.System.now() - 2.days

            coEvery {
                booksLocalDataSource.getSystemVibeBookIds(vibe = SystemVibe.Recommendations)
            } returns listOf(1, 2)

            coEvery {
                booksRemoteDataSource.fetchSystemVibeBookIds(
                    vibe = any(),
                    limit = any(),
                )
            } returns listOf(1, 2)

            // ----- Act -----
            repository.refreshSystemVibeBooks(
                vibe = SystemVibe.Recommendations,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            }
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceSystemVibeBooks(
                    vibe = SystemVibe.Recommendations,
                    requestedIds = listOf(1, 2),
                    books = emptyList(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `a reordered ranking is hydrated`() = runTest {
            // ----- Arrange -----
            val first = recommendedBook(id = 1)
            val second = recommendedBook(id = 2)

            coEvery {
                booksLocalDataSource.getSystemVibeFetchedAt(vibe = SystemVibe.Recommendations)
            } returns Clock.System.now() - 2.days

            coEvery {
                booksLocalDataSource.getSystemVibeBookIds(vibe = SystemVibe.Recommendations)
            } returns listOf(1, 2)

            coEvery {
                booksRemoteDataSource.fetchSystemVibeBookIds(
                    vibe = any(),
                    limit = any(),
                )
            } returns listOf(2, 1)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } returns listOf(second, first)

            // ----- Act -----
            repository.refreshSystemVibeBooks(
                vibe = SystemVibe.Recommendations,
                maxAge = 1.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceSystemVibeBooks(
                    vibe = SystemVibe.Recommendations,
                    requestedIds = listOf(2, 1),
                    books = listOf(second, first),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `a forced refresh hydrates even when the ranking is unchanged`() = runTest {
            // ----- Arrange -----
            val first = recommendedBook(id = 1)

            coEvery {
                booksLocalDataSource.getSystemVibeFetchedAt(vibe = SystemVibe.Recommendations)
            } returns Clock.System.now() - 2.days

            coEvery {
                booksLocalDataSource.getSystemVibeBookIds(vibe = SystemVibe.Recommendations)
            } returns listOf(1)

            coEvery {
                booksRemoteDataSource.fetchSystemVibeBookIds(
                    vibe = any(),
                    limit = any(),
                )
            } returns listOf(1)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } returns listOf(first)

            // ----- Act -----
            repository.refreshSystemVibeBooks(
                vibe = SystemVibe.Recommendations,
                maxAge = Duration.ZERO,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(1))
            }
        }

        @Test
        fun `a first-ever empty result stamps the fetch time with an empty ranking`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRemoteDataSource.fetchSystemVibeBookIds(
                    vibe = any(),
                    limit = any(),
                )
            } returns emptyList()

            // ----- Act -----
            repository.refreshSystemVibeBooks(
                vibe = SystemVibe.Recommendations,
                maxAge = 24.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceSystemVibeBooks(
                    vibe = SystemVibe.Recommendations,
                    requestedIds = emptyList(),
                    books = emptyList(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `a first-ever null result stamps the fetch time with an empty ranking`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRemoteDataSource.fetchSystemVibeBookIds(
                    vibe = any(),
                    limit = any(),
                )
            } returns null

            // ----- Act -----
            repository.refreshSystemVibeBooks(
                vibe = SystemVibe.Recommendations,
                maxAge = 24.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceSystemVibeBooks(
                    vibe = SystemVibe.Recommendations,
                    requestedIds = emptyList(),
                    books = emptyList(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `an empty response keeps the cached ids and re-stamps`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksLocalDataSource.getSystemVibeFetchedAt(vibe = SystemVibe.Recommendations)
            } returns Clock.System.now() - 2.days

            coEvery {
                booksLocalDataSource.getSystemVibeBookIds(vibe = SystemVibe.Recommendations)
            } returns listOf(5, 6)

            // ----- Act -----
            repository.refreshSystemVibeBooks(
                vibe = SystemVibe.Recommendations,
                maxAge = 24.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceSystemVibeBooks(
                    vibe = SystemVibe.Recommendations,
                    requestedIds = listOf(5, 6),
                    books = emptyList(),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `does not re-hydrate ids that already have a user_books row`() = runTest {
            // ----- Arrange -----
            val fresh = recommendedBook(id = 2)

            coEvery {
                booksRemoteDataSource.fetchSystemVibeBookIds(
                    vibe = any(),
                    limit = any(),
                )
            } returns listOf(1, 2)

            coEvery {
                booksLocalDataSource.getExistingUserBookBookIds(ids = any())
            } returns listOf(1)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } returns listOf(fresh)

            // ----- Act -----
            repository.refreshSystemVibeBooks(
                vibe = SystemVibe.Recommendations,
                maxAge = 24.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(2))
            }
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceSystemVibeBooks(
                    vibe = SystemVibe.Recommendations,
                    requestedIds = listOf(1, 2),
                    books = listOf(fresh),
                    fetchedAt = any(),
                )
            }
        }

        @Test
        fun `refreshing Top Picks stores under its own key and leaves Recommendations untouched`() = runTest {
            // ----- Arrange -----
            val pick = recommendedBook(id = 7)

            coEvery {
                booksLocalDataSource.getSystemVibeFetchedAt(vibe = SystemVibe.TopPicks)
            } returns null

            coEvery {
                booksLocalDataSource.getSystemVibeBookIds(vibe = SystemVibe.TopPicks)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchSystemVibeBookIds(
                    vibe = SystemVibe.TopPicks,
                    limit = any(),
                )
            } returns listOf(7)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } returns listOf(pick)

            // ----- Act -----
            repository.refreshSystemVibeBooks(
                vibe = SystemVibe.TopPicks,
                maxAge = 24.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceSystemVibeBooks(
                    vibe = SystemVibe.TopPicks,
                    requestedIds = listOf(7),
                    books = listOf(pick),
                    fetchedAt = any(),
                )
            }
            coVerify(exactly = 0) {
                booksLocalDataSource.replaceSystemVibeBooks(
                    vibe = SystemVibe.Recommendations,
                    requestedIds = any(),
                    books = any(),
                    fetchedAt = any(),
                )
            }
            coVerify(exactly = 0) {
                booksLocalDataSource.getSystemVibeFetchedAt(vibe = SystemVibe.Recommendations)
            }
        }

        @Test
        fun `refreshing Recommendations leaves the Top Picks ranking untouched`() = runTest {
            // ----- Arrange -----
            val recommended = recommendedBook(id = 3)

            coEvery {
                booksRemoteDataSource.fetchSystemVibeBookIds(
                    vibe = SystemVibe.Recommendations,
                    limit = any(),
                )
            } returns listOf(3)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            } returns listOf(recommended)

            // ----- Act -----
            repository.refreshSystemVibeBooks(
                vibe = SystemVibe.Recommendations,
                maxAge = 24.hours,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.replaceSystemVibeBooks(
                    vibe = SystemVibe.Recommendations,
                    requestedIds = listOf(3),
                    books = listOf(recommended),
                    fetchedAt = any(),
                )
            }
            coVerify(exactly = 0) {
                booksLocalDataSource.replaceSystemVibeBooks(
                    vibe = SystemVibe.TopPicks,
                    requestedIds = any(),
                    books = any(),
                    fetchedAt = any(),
                )
            }
            coVerify(exactly = 0) {
                booksLocalDataSource.getSystemVibeFetchedAt(vibe = SystemVibe.TopPicks)
            }
        }
    }

    @Nested
    inner class JournalTimestamps {
        private lateinit var originalZone: JavaTimeZone

        @BeforeEach
        fun pinZone() {
            originalZone = JavaTimeZone.getDefault()
        }

        @AfterEach
        fun restoreZone() {
            JavaTimeZone.setDefault(originalZone)
        }

        private fun readingBook(journals: List<ReadingJournal> = emptyList()): Book = Book(
            id = 42,
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
            userBook = UserBook(
                id = 1,
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
                journals = journals,
                reads = listOf(
                    UserBookRead(
                        id = 1,
                        currentPage = 50,
                        currentSeconds = null,
                        progress = 0.5f,
                        startedAt = null,
                        finishedAt = null,
                    ),
                ),
            ),
        )

        private suspend fun markReadAndCaptureJournals(
            book: Book,
            actionAt: String?,
        ): List<ReadingJournal> {
            val slot = slot<Book>()

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = any(),
                    editionId = any(),
                    actionAt = any(),
                )
            } returns stubBook(userBookId = 1)

            coEvery {
                booksLocalDataSource.cacheBook(book = capture(slot))
            } returns Unit

            repository.markBookAsRead(
                book = book,
                actionAt = actionAt,
            )

            return slot.captured.userBook.shouldNotBeNull().journals
        }

        @Test
        fun `an optimistic journal entry is stamped in naive UTC with microsecond precision`() = runTest {
            // ----- Arrange -----
            JavaTimeZone.setDefault(JavaTimeZone.getTimeZone("Pacific/Kiritimati"))
            val before = Clock.System.now()

            // ----- Act -----
            val journals = markReadAndCaptureJournals(
                book = readingBook(),
                actionAt = null,
            )

            // ----- Assert -----
            val after = Clock.System.now()
            val stamped = journals.single().updatedAt
            stamped.matches(Regex("""\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{6}""")) shouldBe true
            val stampedInstant = Instant.parse("${stamped}Z")
            (stampedInstant >= before - 1.seconds) shouldBe true
            (stampedInstant <= after + 1.seconds) shouldBe true
        }

        @Test
        fun `a backdated actionAt is converted to UTC whatever the device zone`() = runTest {
            // ----- Arrange -----
            JavaTimeZone.setDefault(JavaTimeZone.getTimeZone("Pacific/Pago_Pago"))

            // ----- Act -----
            val journals = markReadAndCaptureJournals(
                book = readingBook(),
                actionAt = "2026-07-21T21:00:00+02:00",
            )

            // ----- Assert -----
            journals.single().updatedAt shouldBe "2026-07-21T19:00:00.000000"
        }

        @Test
        fun `a locally stamped entry sorts after a server entry from earlier the same UTC evening`() = runTest {
            // ----- Arrange -----
            JavaTimeZone.setDefault(JavaTimeZone.getTimeZone("Pacific/Pago_Pago"))
            val serverEntry = ReadingJournal(
                updatedAt = "2026-07-21T20:59:59.999999",
                event = JournalEventType.StatusFinished.eventName,
            )

            // ----- Act -----
            val journals = markReadAndCaptureJournals(
                book = readingBook(journals = listOf(serverEntry)),
                actionAt = "2026-07-21T21:00:00Z",
            )

            // ----- Assert -----
            journals.sortedByDescending { it.updatedAt }.first() shouldBe journals.last()
            journals.last().updatedAt shouldBe "2026-07-21T21:00:00.000000"
        }

        @Test
        fun `a locally stamped entry sorts before a server entry from later the same UTC evening`() = runTest {
            // ----- Arrange -----
            JavaTimeZone.setDefault(JavaTimeZone.getTimeZone("Pacific/Kiritimati"))
            val serverEntry = ReadingJournal(
                updatedAt = "2026-07-21T21:00:00.500000",
                event = JournalEventType.StatusFinished.eventName,
            )

            // ----- Act -----
            val journals = markReadAndCaptureJournals(
                book = readingBook(journals = listOf(serverEntry)),
                actionAt = "2026-07-21T21:00:00Z",
            )

            // ----- Assert -----
            journals.sortedByDescending { it.updatedAt }.first() shouldBe serverEntry
        }
    }

    @Nested
    inner class WritesRacingARefresh {
        private val userId = 20
        private lateinit var gate: UserBookCacheGate
        private lateinit var racingRepository: BooksRepositoryImpl

        @BeforeEach
        fun setUpRacingRepository() {
            gate = UserBookCacheGate(
                booksLocalDataSource = booksLocalDataSource,
                userTagsLocalDataSource = FakeUserTagsLocalDataSource(),
            )

            racingRepository = BooksRepositoryImpl(
                booksRemoteDataSource = booksRemoteDataSource,
                booksLocalDataSource = booksLocalDataSource,
                userBookCacheGate = gate,
                networkAvailability = networkAvailability,
                offlineSync = OfflineUserBookSyncImpl(
                    userBookWriteQueue = mockk(relaxed = true),
                    userBookWriteDrainer = mockk(relaxed = true),
                    booksLocalDataSource = booksLocalDataSource,
                    userBookCacheGate = gate,
                    networkAvailability = networkAvailability,
                    applicationScope = ApplicationScope(scope = testScope),
                ),
                applicationScope = ApplicationScope(scope = testScope),
                appDispatchers = appDispatchers,
                progressHistoryLocalDataSource = progressHistoryLocalDataSource,
            )

            coEvery {
                booksLocalDataSource.getBookById(id = any())
            } returns null
        }

        private fun readingBook(page: Int): Book {
            val userBook = baseBook.userBook.shouldNotBeNull()

            return baseBook.copy(
                userBook = userBook.copy(
                    status = BookStatus.Reading,
                    reads = listOf(
                        UserBookRead(
                            id = 5,
                            currentPage = page,
                            currentSeconds = null,
                            progress = 0.1f,
                            startedAt = "2026-01-01",
                            finishedAt = null,
                        ),
                    ),
                ),
            )
        }

        @Test
        fun `a full refresh does not remove a user book created while the fetch was in flight`() = runTest {
            // ----- Arrange -----
            val fetchedBook = readingBook(page = 10)
            val localUserBookIds = mutableListOf(1)
            val removedIds = mutableListOf<Int>()
            val createdDuringFetch = 2

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } coAnswers {
                localUserBookIds.toList()
            }

            coEvery {
                booksLocalDataSource.removeUserBooksById(ids = any())
            } coAnswers {
                removedIds += firstArg<List<Int>>()
            }

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                    userBookIds = any(),
                )
            } coAnswers {
                localUserBookIds += createdDuringFetch
                snapshotOf(listOf(fetchedBook))
            }

            // ----- Act -----
            racingRepository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            removedIds.contains(createdDuringFetch) shouldBe false
        }

        @Test
        fun `an online progress update confirmed while a refresh fetch is suspended survives that refresh`() = runTest {
            // ----- Arrange -----
            val book = readingBook(page = 10)
            val staleFetched = readingBook(page = 10)
            val remoteGate = CompletableDeferred<Book>()
            val fetchGate = CompletableDeferred<List<Book>>()
            val cached = slot<List<Book>>()

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns listOf(1)

            coEvery {
                booksLocalDataSource.allUserBooks
            } answers {
                flowOf(listOf(book.withCurrentPage(page = 80)))
            }

            coEvery {
                booksLocalDataSource.cacheBooks(books = capture(cached))
            } returns Unit

            coEvery {
                progressHistoryLocalDataSource.insertPending(
                    bookId = any(),
                    actionAtEpochMillis = any(),
                    pages = any(),
                    seconds = any(),
                )
            } returns 9L

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = any(),
                )
            } coAnswers {
                remoteGate.await()
            }

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                    userBookIds = any(),
                )
            } coAnswers {
                snapshotOf(fetchGate.await())
            }

            val update = async {
                racingRepository.updateBookProgress(
                    book = book,
                    newPage = 80,
                )
            }
            runCurrent()
            val refresh = async { racingRepository.refreshUserBooks(userId = userId) }
            runCurrent()

            // ----- Act -----
            remoteGate.complete(book)
            update.await()
            fetchGate.complete(listOf(staleFetched))
            refresh.await()

            // ----- Assert -----
            cached.captured.single().currentRead.shouldNotBeNull().currentPage shouldBe 80
        }

        private fun Book.withCurrentPage(page: Int): Book {
            val userBook = userBook.shouldNotBeNull()

            return copy(
                userBook = userBook.copy(
                    reads = userBook.reads.map { it.copy(currentPage = page) },
                ),
            )
        }
    }

    @Nested
    inner class HasSyncedReadBooksThisSession {
        private val userId = 20

        private fun arrangeSuccessfulRefresh() {
            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } returns emptyList()
        }

        @Test
        fun `is set after a refresh of every status`() = runTest {
            // ----- Arrange -----
            arrangeSuccessfulRefresh()

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = null,
            )

            // ----- Assert -----
            repository.hasSyncedReadBooksThisSession shouldBe true
        }

        @Test
        fun `is set after a refresh whose filter contains Read`() = runTest {
            // ----- Arrange -----
            arrangeSuccessfulRefresh()

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.READ),
            )

            // ----- Assert -----
            repository.hasSyncedReadBooksThisSession shouldBe true
        }

        @Test
        fun `stays unset after a refresh whose filter leaves out Read`() = runTest {
            // ----- Arrange -----
            arrangeSuccessfulRefresh()

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = setOf(UserBookStatus.WANT_TO_READ, UserBookStatus.CURRENTLY_READING),
            )

            // ----- Assert -----
            repository.hasSyncedReadBooksThisSession shouldBe false
        }

        @Test
        fun `stays unset when the refresh fails`() = runTest {
            // ----- Arrange -----
            coEvery {
                offlineSync.drainReconcileAndCache(
                    fetchFromServer = any(),
                    selectBooksToCache = any(),
                )
            } throws RuntimeException("network error")

            // ----- Act -----
            val outcome = runCatching {
                repository.refreshUserBooks(
                    userId = userId,
                    statusFilter = null,
                )
            }

            // ----- Assert -----
            outcome.isFailure shouldBe true
            repository.hasSyncedReadBooksThisSession shouldBe false
        }

        @Test
        fun `is cleared by removeAllBooks`() = runTest {
            // ----- Arrange -----
            arrangeSuccessfulRefresh()
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = null,
            )

            // ----- Act -----
            repository.removeAllBooks()

            // ----- Assert -----
            repository.hasSyncedReadBooksThisSession shouldBe false
        }
    }
}
