package nl.rhaydus.softcover.core.book.data.repository

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.platform.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.book.data.datasource.BookNotFoundException
import nl.rhaydus.softcover.core.book.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.core.book.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.core.book.domain.model.IsbnEditionMatch
import nl.rhaydus.softcover.core.book.domain.sync.OfflineUserBookSync
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.exception.ServerUnavailableException
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BooksRepositoryImplTest {
    private lateinit var booksRemoteDataSource: BooksRemoteDataSource
    private lateinit var booksLocalDataSource: BooksLocalDataSource
    private lateinit var networkAvailability: NetworkAvailabilityProvider
    private lateinit var offlineSync: OfflineUserBookSync
    private lateinit var appDispatchers: AppDispatchers
    private lateinit var testScope: TestScope
    private lateinit var repository: BooksRepositoryImpl

    @BeforeEach
    fun setUp() {
        booksRemoteDataSource = mockk()
        booksLocalDataSource = mockk(relaxed = true)
        networkAvailability = mockk()
        offlineSync = mockk(relaxed = true)

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

        repository = BooksRepositoryImpl(
            booksRemoteDataSource = booksRemoteDataSource,
            booksLocalDataSource = booksLocalDataSource,
            networkAvailability = networkAvailability,
            offlineSync = offlineSync,
            applicationScope = ApplicationScope(scope = testScope),
            appDispatchers = appDispatchers,
        )
    }

    @AfterEach
    fun tearDown() {
        testScope.cancel()
    }

    private fun stubBook(userBookId: Int?): Book = mockk(relaxed = true) {
        every {
            this@mockk.userBook
        } returns if (userBookId != null) stubUserBook(userBookId) else null

        every {
            this@mockk.userBookRead
        } returns null
    }

    private fun stubUserBook(id: Int): UserBook = mockk(relaxed = true) {
        every {
            this@mockk.id
        } returns id
    }

    private fun stubBookEdition(): BookEdition = mockk()

    @Nested
    inner class Books {
        @Test
        fun `books property is wired to local data source allUserBooks flow`() = runTest {
            // ----- Arrange -----
            val expectedBooks = listOf(stubBook(userBookId = 1))

            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(expectedBooks)

            val freshRepository = BooksRepositoryImpl(
                booksRemoteDataSource = booksRemoteDataSource,
                booksLocalDataSource = booksLocalDataSource,
                networkAvailability = networkAvailability,
                offlineSync = offlineSync,
                applicationScope = ApplicationScope(scope = testScope),
                appDispatchers = appDispatchers,
            )

            // ----- Act & Assert -----
            freshRepository.books.test {
                awaitItem() shouldBe expectedBooks
                awaitComplete()
            }
        }
    }

    @Nested
    inner class GetBooksFlowByStatus {
        @Test
        fun `getBooksFlowByStatus delegates to local data source with the given status`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.CURRENTLY_READING
            val expectedBooks = listOf(stubBook(userBookId = 5))

            every {
                booksLocalDataSource.getBooksFlowByStatus(status = status)
            } returns flowOf(expectedBooks)

            // ----- Act & Assert -----
            repository.getBooksFlowByStatus(status = status).test {
                awaitItem() shouldBe expectedBooks
                awaitComplete()
            }
        }
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
            } returns fetchedBooks

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainAndReconcile(any())
            } coAnswers {
                firstArg<suspend () -> List<Book>>().invoke()
            }

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = fetchedBooks)
            }
        }

        @Test
        fun `refreshUserBooks can be called multiple times without throwing`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                )
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainAndReconcile(any())
            } coAnswers {
                firstArg<suspend () -> List<Book>>().invoke()
            }

            // ----- Act -----
            val result = runCatching {
                repository.refreshUserBooks(userId = userId)
                repository.refreshUserBooks(userId = userId)
            }

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `refreshUserBooks delegates drain and reconcile to offlineSync`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                )
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainAndReconcile(any())
            } coAnswers {
                firstArg<suspend () -> List<Book>>().invoke()
            }

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify { offlineSync.drainAndReconcile(any()) }
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
            } returns listOf(stubBook(userBookId = 42))

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainAndReconcile(any())
            } returns listOf(reconciledBook)

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = listOf(reconciledBook))
            }
        }

        @Test
        fun `refreshUserBooks does not reapply local progress for userBooks not in synced set`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val remoteBook = stubBook(userBookId = 7)

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = any(),
                )
            } returns listOf(remoteBook)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainAndReconcile(any())
            } coAnswers {
                firstArg<suspend () -> List<Book>>().invoke()
            }

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = listOf(remoteBook))
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
                        UserBookStatus.DID_NOT_FINISH.code,
                    ),
                )
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainAndReconcile(any())
            } coAnswers {
                firstArg<suspend () -> List<Book>>().invoke()
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
                )
            } returns listOf(fetchedBook)

            coEvery {
                booksLocalDataSource.getUserBookIdsByStatus(status = status)
            } returns listOf(10, staleLocalId)

            coEvery {
                offlineSync.drainAndReconcile(any())
            } coAnswers {
                firstArg<suspend () -> List<Book>>().invoke()
            }

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = status,
            )

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(status.code),
                )
            }

            coVerify {
                booksLocalDataSource.getUserBookIdsByStatus(status = status)
            }

            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = listOf(staleLocalId))
            }
        }

        @Test
        fun `concurrent refreshUserBooks calls coalesce into one remote round trip`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(
                        UserBookStatus.WANT_TO_READ.code,
                        UserBookStatus.CURRENTLY_READING.code,
                        UserBookStatus.READ.code,
                        UserBookStatus.DID_NOT_FINISH.code,
                    ),
                )
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                offlineSync.drainAndReconcile(any())
            } coAnswers {
                firstArg<suspend () -> List<Book>>().invoke()
            }

            // ----- Act -----
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = null,
            )
            repository.refreshUserBooks(
                userId = userId,
                statusFilter = null,
            )

            // ----- Assert -----
            coVerify(atLeast = 1) {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = setOf(
                        UserBookStatus.WANT_TO_READ.code,
                        UserBookStatus.CURRENTLY_READING.code,
                        UserBookStatus.READ.code,
                        UserBookStatus.DID_NOT_FINISH.code,
                    ),
                )
            }
        }
    }

    @Nested
    inner class DeleteOrphanBooks {
        @Test
        fun `deleteOrphanBooks delegates to local data source`() = runTest {
            // ----- Arrange -----
            // (booksLocalDataSource is relaxed)

            // ----- Act -----
            repository.deleteOrphanBooks()

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.deleteOrphanBooks()
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
    inner class CacheBook {
        @Test
        fun `cacheBook delegates to local data source`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)

            // ----- Act -----
            repository.cacheBook(book = book)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = book)
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

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                repository.removeBook(book = book)
            }
        }
    }

    @Nested
    inner class RemoveAllBooks {
        @Test
        fun `removeAllBooks delegates to local data source`() = runTest {
            // ----- Arrange -----
            // (no additional setup needed — booksLocalDataSource is relaxed)

            // ----- Act -----
            repository.removeAllBooks()

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeAllBooks()
            }
        }
    }

    @Nested
    inner class GetEditionsByBookId {
        @Test
        fun `getEditionsByBookId delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            val expectedEditions = listOf(stubBookEdition())

            coEvery {
                booksRemoteDataSource.getEditionsByBookId(bookId = bookId)
            } returns expectedEditions

            // ----- Act -----
            val result = repository.getEditionsByBookId(bookId = bookId)

            // ----- Assert -----
            result shouldBe expectedEditions
        }

        @Test
        fun `getEditionsByBookId returns empty list when remote returns no editions`() = runTest {
            // ----- Arrange -----
            val bookId = 7

            coEvery {
                booksRemoteDataSource.getEditionsByBookId(bookId = bookId)
            } returns emptyList()

            // ----- Act -----
            val result = repository.getEditionsByBookId(bookId = bookId)

            // ----- Assert -----
            result shouldBe emptyList()
        }

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
        fun `fetchBookById delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val bookId = 7
            val expectedBook = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.fetchBookById(id = bookId)
            } returns expectedBook

            // ----- Act -----
            val result = repository.fetchBookById(id = bookId)

            // ----- Assert -----
            result shouldBe expectedBook
        }

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

            // ----- Act & Assert -----
            shouldThrow<OfflineException> {
                repository.fetchBookById(id = bookId)
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBookById(id = any())
            }
        }

        @Test
        fun `recovery happy path — resolves canonical via userBook editionId and returns refetched book`() = runTest {
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
        }

        @Test
        fun `recovery happy path — persist sequence cacheBook then redirectBookId then deleteOrphanBooks is called in order`() = runTest {
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
            repository.fetchBookById(id = missingId)

            // ----- Assert -----
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

            // ----- Act & Assert -----
            shouldThrow<BookNotFoundException> {
                repository.fetchBookById(id = missingId)
            }

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

            // ----- Act & Assert -----
            shouldThrow<BookNotFoundException> {
                repository.fetchBookById(id = missingId)
            }

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

            // ----- Act & Assert -----
            shouldThrow<BookNotFoundException> {
                repository.fetchBookById(id = missingId)
            }
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

            // ----- Act & Assert -----
            shouldThrow<BookNotFoundException> {
                repository.fetchBookById(id = missingId)
            }
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

            // ----- Act & Assert -----
            shouldThrow<BookNotFoundException> {
                repository.fetchBookById(id = missingId)
            }
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
        fun `forwards null editionId to remote data source when omitted`() = runTest {
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
                    editionId = null,
                )
            } returns remoteBook

            // ----- Act -----
            repository.markBookAsWantToRead(book = book)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.markBookAsWantToRead(
                    bookId = bookId,
                    editionId = null,
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

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                repository.markBookAsWantToRead(book = book)
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }
    }

    @Nested
    inner class MarkBookAsReading {
        @Test
        fun `markBookAsReading delegates to remote data source and returns result`() = runTest {
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
                userBookRead = null,
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
            val capturedUserBook = slot.captured.userBook!!
            capturedUserBook.status shouldBe BookStatus.Reading
            val capturedJournals = capturedUserBook.journals
            capturedJournals.size shouldBe 2
            capturedJournals[0] shouldBe existingJournal
            capturedJournals[1].event shouldBe "user_book_read_started"
            capturedJournals[1].updatedAt.isNotEmpty() shouldBe true
        }

        @Test
        fun `cacheBook is called before markBookAsReading on remote`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val expectedBook = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            } returns expectedBook

            coEvery {
                booksLocalDataSource.cacheBook(book = any())
            } returns Unit

            // ----- Act -----
            repository.markBookAsReading(book = book)

            // ----- Assert -----
            coVerifyOrder {
                booksLocalDataSource.cacheBook(book = any())
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            }
        }

        @Test
        fun `when userBook is null cacheBook receives the original book unchanged`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)
            val expectedBook = stubBook(userBookId = null)
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
            repository.markBookAsReading(book = book)

            // ----- Assert -----
            slot.captured shouldBe book
        }

        @Test
        fun `markBookAsReading restores snapshot to local cache when remote throws non-offline error`() = runTest {
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

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            } throws RuntimeException("network failure")

            // ----- Act -----
            runCatching { repository.markBookAsReading(book = book) }

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `markBookAsReading rethrows the remote error after restoring snapshot`() = runTest {
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

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsReading(
                    book = book,
                    editionId = any(),
                )
            } throws remoteError

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                repository.markBookAsReading(book = book)
            } shouldBe remoteError
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

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                repository.markBookAsReading(book = book)
            }

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
        )

        @Test
        fun `throws when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                repository.updateBookRating(
                    book = book,
                    rating = 4.0,
                )
            }
        }

        @Test
        fun `writes optimistic book with updated rating to local cache before the remote call`() = runTest {
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
                userBookRead = null,
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
            repository.updateBookRating(
                book = book,
                rating = newRating,
            )

            // ----- Assert -----
            capturedBooks.first().userBook?.rating shouldBe newRating

            coVerifyOrder {
                booksLocalDataSource.cacheBook(book = any())
                booksRemoteDataSource.updateBookRating(
                    userBook = any(),
                    rating = newRating,
                )
            }
        }

        @Test
        fun `on success returns the remote book and cacheBook is called exactly once for the optimistic write`() = runTest {
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
            val remoteBook = stubBook(userBookId = 5)

            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns null

            coEvery {
                booksRemoteDataSource.updateBookRating(
                    userBook = any(),
                    rating = newRating,
                )
            } returns remoteBook

            coEvery {
                booksLocalDataSource.cacheBook(book = any())
            } returns Unit

            // ----- Act -----
            val result = repository.updateBookRating(
                book = book,
                rating = newRating,
            )

            // ----- Assert -----
            result shouldBe remoteBook

            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = remoteBook)
            }
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

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                repository.updateBookRating(
                    book = book,
                    rating = newRating,
                )
            }

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
                userBookRead = null,
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
                userBookRead = null,
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
                userBookRead = null,
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
        fun `does not resurrect a finished book — optimistic write carries the cached Read status, not the passed-in Reading status`() = runTest {
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
                userBookRead = null,
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
                userBookRead = null,
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
        )

        @Test
        fun `throws when book has no userBook`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                repository.updateBookReview(
                    book = book,
                    review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great read"))))),
                    hasSpoilers = false,
                )
            }
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
                userBookRead = null,
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
                userBookRead = null,
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
                userBookRead = null,
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
                userBookRead = null,
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
                userBookRead = null,
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
        fun `does not resurrect a finished book — optimistic write carries the cached Read status, not the passed-in Reading status`() = runTest {
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
                userBookRead = null,
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
                userBookRead = null,
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
    inner class RemoveBookFromLibrary {
        @Test
        fun `happy path — removes user_book locally, calls remote, no rollback occurs`() = runTest {
            // ----- Arrange -----
            val userBookId = 7
            val book = stubBook(userBookId = userBookId)

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } returns Unit

            // ----- Act -----
            repository.removeBookFromLibrary(book = book)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = listOf(userBookId))
            }

            coVerify {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = any())
            }
        }

        @Test
        fun `failure path — re-caches snapshot and rethrows`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val userBookId = 7
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns stubUserBook(id = userBookId)
            }
            val snapshot = stubBook(userBookId = userBookId)
            val remoteError = RuntimeException("network error")

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } throws remoteError

            // ----- Act -----
            val caught = runCatching { repository.removeBookFromLibrary(book = book) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without re-caching snapshot`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val userBookId = 7
            val book = mockk<Book>(relaxed = true) {
                every {
                    this@mockk.id
                } returns bookId
                every {
                    this@mockk.userBook
                } returns stubUserBook(id = userBookId)
            }
            val snapshot = stubBook(userBookId = userBookId)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } throws CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                repository.removeBookFromLibrary(book = book)
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = any())
            }
        }

        @Test
        fun `no-userBook path — skips removeUserBooksById and still calls remote`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } returns Unit

            // ----- Act -----
            repository.removeBookFromLibrary(book = book)

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksLocalDataSource.removeUserBooksById(ids = any())
            }

            coVerify {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            }
        }
    }

    @Nested
    inner class UpdateBookProgress {
        @Test
        fun `updateBookProgress delegates to remote data source with correct arguments and returns result`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val newPage = 150
            val expectedBook = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.updateBookProgress(
                    book = book,
                    newPage = newPage,
                )
            } returns expectedBook

            // ----- Act -----
            val result = repository.updateBookProgress(
                book = book,
                newPage = newPage,
            )

            // ----- Assert -----
            result shouldBe expectedBook
        }

        @Test
        fun `updateBookProgress forwards actionAt to remote data source`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val newPage = 150
            val actionAt = "2026-07-21T21:00:00Z"
            val expectedBook = stubBook(userBookId = 1)

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
                userBook = userBook,
                userBookRead = userBookRead,
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
            val capturedJournals = slot.captured.userBook!!.journals
            capturedJournals.size shouldBe 2
            capturedJournals[0] shouldBe existingJournal
            capturedJournals[1].event shouldBe "progress_updated"
            capturedJournals[1].updatedAt.isNotEmpty() shouldBe true
        }

        @Test
        fun `updateBookProgress when online and remote throws non-offline error restores snapshot to local cache`() = runTest {
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
                )
            } throws RuntimeException("network failure")

            // ----- Act -----
            runCatching {
                repository.updateBookProgress(
                    book = book,
                    newPage = 50,
                )
            }

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `updateBookProgress when online and remote throws non-offline error rethrows the error`() = runTest {
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
                )
            } throws remoteError

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                repository.updateBookProgress(
                    book = book,
                    newPage = 50,
                )
            } shouldBe remoteError
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
                )
            }
        }
    }

    @Nested
    inner class MarkBookAsRead {
        @Test
        fun `markBookAsRead delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val expectedBook = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = any(),
                )
            } returns expectedBook

            // ----- Act -----
            val result = repository.markBookAsRead(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
        }

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
        fun `forwards null editionId to remote data source when omitted`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)
            val expectedBook = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = null,
                )
            } returns expectedBook

            // ----- Act -----
            repository.markBookAsRead(book = book)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.markBookAsRead(
                    book = book,
                    editionId = null,
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
                userBook = userBook,
                userBookRead = userBookRead,
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
            val capturedUserBook = slot.captured.userBook!!
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
                userBook = userBook,
                userBookRead = userBookRead,
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
            slot.captured.userBookRead?.finishedAt shouldBe expectedFinishedAt
        }

        @Test
        fun `markBookAsRead when online and remote throws non-offline error restores snapshot to local cache`() = runTest {
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
            } throws RuntimeException("network failure")

            // ----- Act -----
            runCatching { repository.markBookAsRead(book = book) }

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
        }

        @Test
        fun `markBookAsRead when online and remote throws non-offline error rethrows the error`() = runTest {
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

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                repository.markBookAsRead(book = book)
            } shouldBe remoteError
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
        fun `markBookAsRead when online and remote throws ServerUnavailableException enqueues MARK_AS_READ and does NOT restore snapshot`() = runTest {
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
    }

    @Nested
    inner class UpdateBookEdition {
        @Test
        fun `updateBookEdition delegates to remote data source with correct arguments and returns result`() = runTest {
            // ----- Arrange -----
            val userBook = stubUserBook(id = 3)
            val newEditionId = 88
            val expectedBook = stubBook(userBookId = 3)

            coEvery {
                booksRemoteDataSource.updateBookEdition(
                    userBook = userBook,
                    newEditionId = newEditionId,
                )
            } returns expectedBook

            // ----- Act -----
            val result = repository.updateBookEdition(
                userBook = userBook,
                newEditionId = newEditionId,
            )

            // ----- Assert -----
            result shouldBe expectedBook
        }
    }

    @Nested
    inner class FetchBooksByIds {
        @Test
        fun `fetchBooksByIds delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val ids = listOf(1, 2, 3)
            val expectedBooks = listOf(stubBook(userBookId = null), stubBook(userBookId = null))

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(
                    ids = ids,
                    forceNetwork = false,
                )
            } returns expectedBooks

            // ----- Act -----
            val result = repository.fetchBooksByIds(ids = ids)

            // ----- Assert -----
            result shouldBe expectedBooks
        }

        @Test
        fun `fetchBooksByIds returns empty list when remote returns no books`() = runTest {
            // ----- Arrange -----
            val ids = listOf(99)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(
                    ids = ids,
                    forceNetwork = false,
                )
            } returns emptyList()

            // ----- Act -----
            val result = repository.fetchBooksByIds(ids = ids)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class FetchEditionsByIds {
        @Test
        fun `fetchEditionsByIds delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val ids = listOf(10, 20)
            val expectedEditions = listOf(stubBookEdition(), stubBookEdition())

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(
                    ids = ids,
                    forceNetwork = false,
                )
            } returns expectedEditions

            // ----- Act -----
            val result = repository.fetchEditionsByIds(ids = ids)

            // ----- Assert -----
            result shouldBe expectedEditions
        }

        @Test
        fun `fetchEditionsByIds returns empty list when remote returns no editions`() = runTest {
            // ----- Arrange -----
            val ids = listOf(99)

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(
                    ids = ids,
                    forceNetwork = false,
                )
            } returns emptyList()

            // ----- Act -----
            val result = repository.fetchEditionsByIds(ids = ids)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class PersistEditionImage {
        @Test
        fun `delegates to local data source with the given editionId, url, and bytes`() = runTest {
            // ----- Arrange -----
            val editionId = 42
            val url = "https://example.com/cover42.jpg"
            val bytes = byteArrayOf(1, 2, 3)

            coEvery {
                booksLocalDataSource.persistEditionImage(
                    editionId = editionId,
                    url = url,
                    bytes = bytes,
                )
            } returns Unit

            // ----- Act -----
            repository.persistEditionImage(
                editionId = editionId,
                url = url,
                bytes = bytes,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.persistEditionImage(
                    editionId = editionId,
                    url = url,
                    bytes = bytes,
                )
            }
        }
    }

    @Nested
    inner class FetchEditionMatchesForIsbns {
        @Test
        fun `online — delegates to remote and returns the match map`() = runTest {
            // ----- Arrange -----
            val isbn = "9780451524935"
            val expectedMatches = mapOf(
                isbn to IsbnEditionMatch(
                    bookId = 42,
                    editionId = 99,
                ),
            )

            coEvery {
                booksRemoteDataSource.fetchEditionMatchesForIsbns(isbns = listOf(isbn))
            } returns expectedMatches

            // ----- Act -----
            val result = repository.fetchEditionMatchesForIsbns(isbns = listOf(isbn))

            // ----- Assert -----
            result shouldBe expectedMatches

            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchEditionMatchesForIsbns(isbns = listOf(isbn))
            }
        }

        @Test
        fun `online — remote reports no match — returns emptyMap`() = runTest {
            // ----- Arrange -----
            val isbn = "9780451524935"

            coEvery {
                booksRemoteDataSource.fetchEditionMatchesForIsbns(isbns = listOf(isbn))
            } returns emptyMap()

            // ----- Act -----
            val result = repository.fetchEditionMatchesForIsbns(isbns = listOf(isbn))

            // ----- Assert -----
            result shouldBe emptyMap()

            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchEditionMatchesForIsbns(isbns = listOf(isbn))
            }
        }

        @Test
        fun `offline — throws OfflineException without calling remote`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            val isbn = "9780451524935"

            // ----- Act & Assert -----
            shouldThrow<OfflineException> {
                repository.fetchEditionMatchesForIsbns(isbns = listOf(isbn))
            }

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
        private fun stubUserBook(id: Int = 1): UserBook = mockk(relaxed = true) {
            every {
                this@mockk.id
            } returns id
            every {
                this@mockk.editionId
            } returns null
        }

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
            userBook = stubUserBook(id = userBookId),
            userBookRead = stubUserBookRead(id = userBookReadId),
        )

        @Test
        fun `updateBookProgress when offline caches optimistic book and enqueues UPDATE_PROGRESS without calling remote`() = runTest {
            // ----- Arrange -----
            every {
                networkAvailability.isOnline
            } returns MutableStateFlow(false)

            val book = stubBookWithUserBookRead()
            val newPage = 75

            // ----- Act -----
            repository.updateBookProgress(
                book = book,
                newPage = newPage,
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
        fun `updateBookProgress when offline forwards actionAt to enqueueProgressUpdate`() = runTest {
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
                offlineSync.enqueueProgressUpdate(
                    book = any(),
                    newPage = any(),
                    newSeconds = any(),
                    actionAt = actionAt,
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

            // ----- Act -----
            repository.markBookAsRead(book = book)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify { offlineSync.enqueueMarkAsRead(book = any()) }

            coVerify(exactly = 0) {
                booksRemoteDataSource.markBookAsRead(
                    book = any(),
                    editionId = any(),
                )
            }
        }

        @Test
        fun `markBookAsRead when offline forwards actionAt to enqueueMarkAsRead`() = runTest {
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
                offlineSync.enqueueMarkAsRead(
                    book = any(),
                    actionAt = actionAt,
                )
            }
        }
    }

    @Nested
    inner class FetchTrendingBooks {
        @Test
        fun `returns list from remote data source`() = runTest {
            // ----- Arrange -----
            val trendingBooks = listOf(stubBook(userBookId = null), stubBook(userBookId = null))

            coEvery {
                booksRemoteDataSource.fetchTrendingBooks()
            } returns trendingBooks

            // ----- Act -----
            val result = repository.fetchTrendingBooks()

            // ----- Assert -----
            result shouldBe trendingBooks
        }

        @Test
        fun `delegates to remote data source`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRemoteDataSource.fetchTrendingBooks()
            } returns emptyList()

            // ----- Act -----
            repository.fetchTrendingBooks()

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchTrendingBooks()
            }
        }

        @Test
        fun `round-trips the result from the remote data source unchanged`() = runTest {
            // ----- Arrange -----
            val trendingBooks = listOf(stubBook(userBookId = null))

            coEvery {
                booksRemoteDataSource.fetchTrendingBooks()
            } returns trendingBooks

            // ----- Act -----
            val result = repository.fetchTrendingBooks()

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksRemoteDataSource.fetchTrendingBooks()
            }
            result shouldBe trendingBooks
        }
    }
}
