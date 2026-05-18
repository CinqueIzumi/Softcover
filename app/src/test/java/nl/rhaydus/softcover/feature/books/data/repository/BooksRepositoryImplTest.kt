package nl.rhaydus.softcover.feature.books.data.repository

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.connectivity.NetworkAvailabilityProvider
import nl.rhaydus.softcover.core.domain.connectivity.OfflineProgressQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingProgressDrainer
import nl.rhaydus.softcover.core.domain.connectivity.PendingProgressUpdate
import nl.rhaydus.softcover.core.domain.connectivity.PendingProgressUpdateKind
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.books.data.datasource.BookNotFoundException
import nl.rhaydus.softcover.feature.books.data.datasource.BooksLocalDataSource
import nl.rhaydus.softcover.feature.books.data.datasource.BooksRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BooksRepositoryImplTest {

    private lateinit var booksRemoteDataSource: BooksRemoteDataSource
    private lateinit var booksLocalDataSource: BooksLocalDataSource
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var networkAvailability: NetworkAvailabilityProvider
    private lateinit var offlineProgressQueue: OfflineProgressQueue
    private lateinit var pendingProgressDrainer: PendingProgressDrainer
    private lateinit var repository: BooksRepositoryImpl

    @BeforeEach
    fun setUp() {
        booksRemoteDataSource = mockk()
        booksLocalDataSource = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        networkAvailability = mockk()
        offlineProgressQueue = mockk(relaxed = true)
        pendingProgressDrainer = mockk(relaxed = true)

        every {
            settingsRepository.enabledStatusCodes
        } returns flowOf(setOf(1, 3, 5))

        every {
            settingsRepository.enabledListIds
        } returns flowOf(emptySet())

        every {
            settingsRepository.listDefaultsSeeded
        } returns flowOf(true)

        every {
            networkAvailability.isOnline
        } returns MutableStateFlow(true)

        repository = BooksRepositoryImpl(
            booksRemoteDataSource = booksRemoteDataSource,
            booksLocalDataSource = booksLocalDataSource,
            settingsRepository = settingsRepository,
            networkAvailability = networkAvailability,
            offlineProgressQueue = offlineProgressQueue,
            pendingProgressDrainer = pendingProgressDrainer,
        )
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

    private fun stubBookList(
        listBooks: List<ListBook> = emptyList(),
        id: Int = 1,
        slug: String = "some-list",
    ): BookList = BookList(
        id = id,
        name = slug,
        slug = slug,
        books = listBooks,
    )

    private fun stubListBook(bookId: Int = 1, editionId: Int = 100): ListBook = ListBook(
        listBookId = 1,
        listId = 0,
        bookId = bookId,
        editionId = editionId,
    )

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
                settingsRepository = settingsRepository,
                networkAvailability = networkAvailability,
                offlineProgressQueue = offlineProgressQueue,
                pendingProgressDrainer = pendingProgressDrainer,
            )

            // ----- Act & Assert -----
            freshRepository.books.test {
                awaitItem() shouldBe expectedBooks
                awaitComplete()
            }
        }
    }

    @Nested
    inner class AllUserLists {

        @Test
        fun `allUserLists property is wired to local data source allUserLists flow`() = runTest {
            // ----- Arrange -----
            val expectedLists = listOf(stubBookList())

            every {
                booksLocalDataSource.allUserLists
            } returns flowOf(expectedLists)

            val freshRepository = BooksRepositoryImpl(
                booksRemoteDataSource = booksRemoteDataSource,
                booksLocalDataSource = booksLocalDataSource,
                settingsRepository = settingsRepository,
                networkAvailability = networkAvailability,
                offlineProgressQueue = offlineProgressQueue,
                pendingProgressDrainer = pendingProgressDrainer,
            )

            // ----- Act & Assert -----
            freshRepository.allUserLists.test {
                awaitItem() shouldBe expectedLists
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
    inner class InitializeBooks {

        @Test
        fun `initializeBooks fetches remote books and caches them`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val enabledStatuses = setOf(1, 3, 5)
            val fetchedBooks = listOf(
                stubBook(userBookId = 1),
                stubBook(userBookId = 2),
            )

            every {
                settingsRepository.enabledStatusCodes
            } returns flowOf(enabledStatuses)

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = fetchedBooks)
            }
        }

        @Test
        fun `initializeBooks caches all fetched lists with disabled ones having empty books`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val enabledListId = 1
            val disabledListId = 2
            val enabledList = stubBookList(id = enabledListId, slug = "owned")
            val disabledList = stubBookList(id = disabledListId, slug = "favorites")

            every {
                settingsRepository.enabledListIds
            } returns flowOf(setOf(enabledListId))

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns listOf(enabledList, disabledList)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheUserBookLists(
                    lists = match { lists ->
                        lists.any { it.id == enabledListId } &&
                            lists.any { it.id == disabledListId && it.books.isEmpty() }
                    },
                )
            }
        }

        @Test
        fun `initializeBooks always includes CURRENTLY_READING code in statusIds passed to remote`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val enabledStatuses = setOf(1, 3, 5)

            every {
                settingsRepository.enabledStatusCodes
            } returns flowOf(enabledStatuses)

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = match { ids -> UserBookStatus.CURRENTLY_READING.code in ids!! },
                )
            }
        }

        @Test
        fun `initializeBooks passes union of enabled codes, CR, and alwaysCachedCodes to remote`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val enabledStatuses = setOf(1, 3, 5)
            val expectedStatusIds =
                UserBookStatus.activeLibraryCodes(enabledCodes = enabledStatuses) +
                    UserBookStatus.alwaysCachedCodes

            every {
                settingsRepository.enabledStatusCodes
            } returns flowOf(enabledStatuses)

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = expectedStatusIds,
                )
            }
        }

        @Test
        fun `initializeBooks passes all alwaysCachedCodes to remote even when enabledStatusCodes is empty`() = runTest {
            // ----- Arrange -----
            val userId = 10

            every {
                settingsRepository.enabledStatusCodes
            } returns flowOf(emptySet())

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.initializeBooks(
                    userId = userId,
                    statusIds = match { ids -> ids.containsAll(UserBookStatus.alwaysCachedCodes) },
                )
            }
        }

        @Test
        fun `initializeBooks removes locally stored books absent from remote fetch`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val bookStillPresent = stubBook(userBookId = 1)
            val fetchedBooks = listOf(bookStillPresent)
            val fetchedLists = emptyList<BookList>()
            val staleLocalId = 99

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns fetchedLists

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns listOf(
                1,
                staleLocalId,
            )

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = listOf(staleLocalId))
            }
        }

        @Test
        fun `initializeBooks does not remove books whose ids are present in the remote fetch`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val fetchedBooks = listOf(stubBook(userBookId = 7))
            val fetchedLists = emptyList<BookList>()

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns fetchedLists

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns listOf(7)

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = emptyList())
            }
        }

        @Test
        fun `initializeBooks handles books without userBook when computing ids to remove`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val bookWithoutUserBook = stubBook(userBookId = null)
            val fetchedBooks = listOf(bookWithoutUserBook)
            val fetchedLists = emptyList<BookList>()

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns fetchedLists

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns listOf(3)

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeUserBooksById(ids = listOf(3))
            }
        }

        @Test
        fun `initializeBooks throws when called a second time in the same session`() = runTest {
            // ----- Arrange -----
            val userId = 10

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            repository.initializeBooks(userId = userId)

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                repository.initializeBooks(userId = userId)
            }
        }

        @Test
        fun `initializeBooks does not throw on first call`() = runTest {
            // ----- Arrange -----
            val userId = 1

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            val result = runCatching { repository.initializeBooks(userId = userId) }

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `initializeBooks calls syncBookListMetadata with server list ids after fetch`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val list1 = stubBookList(id = 3, slug = "one")
            val list2 = stubBookList(id = 7, slug = "two")

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns listOf(list1, list2)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.syncBookListMetadata(serverListIds = setOf(3, 7))
            }
        }

        @Test
        fun `initializeBooks calls deleteOrphanBooks after caching`() = runTest {
            // ----- Arrange -----
            val userId = 10

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.deleteOrphanBooks()
            }
        }

        @Test
        fun `initializeBooks always passes null listIds to fetchUserLists`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val enabledListIds = setOf(5, 8)

            every {
                settingsRepository.listDefaultsSeeded
            } returns flowOf(true)

            every {
                settingsRepository.enabledListIds
            } returns flowOf(enabledListIds)

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            }
        }

        @Test
        fun `initializeBooks on first run fetches all lists and seeds the owned list id`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val ownedListId = 42
            val ownedList = stubBookList(id = ownedListId, slug = "owned")
            val otherList = stubBookList(id = 99, slug = "favorites")

            every {
                settingsRepository.listDefaultsSeeded
            } returns flowOf(false)

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns listOf(ownedList, otherList)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                settingsRepository.seedEnabledListIds(ids = setOf(ownedListId))
            }
        }

        @Test
        fun `initializeBooks on first run caches all fetched lists`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val ownedListId = 42
            val ownedList = stubBookList(id = ownedListId, slug = "owned")
            val otherList = stubBookList(id = 99, slug = "favorites")

            every {
                settingsRepository.listDefaultsSeeded
            } returns flowOf(false)

            // After seeding, enabledListIds returns {ownedListId}
            every {
                settingsRepository.enabledListIds
            } returns flowOf(setOf(ownedListId))

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns listOf(ownedList, otherList)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            // Both lists are cached — the non-enabled one with empty books
            coVerify {
                booksLocalDataSource.cacheUserBookLists(
                    lists = match { lists -> lists.size == 2 },
                )
            }
        }

        @Test
        fun `initializeBooks caches owned list with its books even when owned id is not in enabledListIds`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val ownedListId = 55
            val disabledCustomListId = 99
            val ownedListBook = stubListBook(bookId = 1, editionId = 10)
            val disabledListBook = stubListBook(bookId = 2, editionId = 20)
            val ownedList = stubBookList(id = ownedListId, slug = "owned", listBooks = listOf(ownedListBook))
            val disabledCustomList = stubBookList(id = disabledCustomListId, slug = "favorites", listBooks = listOf(disabledListBook))

            // Neither the owned list nor the custom list is in enabledListIds
            every {
                settingsRepository.enabledListIds
            } returns flowOf(emptySet())

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns listOf(ownedList, disabledCustomList)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = any())
            } returns listOf(ownedListBook.bookId)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = any())
            } returns listOf(ownedListBook.editionId)

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheUserBookLists(
                    lists = match { lists ->
                        lists.any { it.id == ownedListId && it.books.isNotEmpty() } &&
                            lists.any { it.id == disabledCustomListId && it.books.isEmpty() }
                    },
                )
            }
        }

        @Test
        fun `initializeBooks on first run when no owned list exists seeds empty set`() = runTest {
            // ----- Arrange -----
            val userId = 10
            val someList = stubBookList(id = 10, slug = "favorites")

            every {
                settingsRepository.listDefaultsSeeded
            } returns flowOf(false)

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns listOf(someList)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.initializeBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                settingsRepository.seedEnabledListIds(ids = emptySet())
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
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = fetchedBooks)
            }
        }

        @Test
        fun `refreshUserBooks caches all fetched lists`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val enabledListId = 1
            val fetchedList = stubBookList(id = enabledListId)

            every {
                settingsRepository.enabledListIds
            } returns flowOf(setOf(enabledListId))

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns listOf(fetchedList)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheUserBookLists(lists = match { it.size == 1 })
            }
        }

        @Test
        fun `refreshUserBooks does not set initializedBooksThisSession so initializeBooks still throws after a prior init`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            repository.initializeBooks(userId = userId)
            repository.refreshUserBooks(userId = userId)

            // ----- Act & Assert -----
            shouldThrow<Exception> {
                repository.initializeBooks(userId = userId)
            }
        }

        @Test
        fun `refreshUserBooks can be called multiple times without throwing`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            val result = runCatching {
                repository.refreshUserBooks(userId = userId)
                repository.refreshUserBooks(userId = userId)
            }

            // ----- Assert -----
            result.isSuccess shouldBe true
        }

        @Test
        fun `refreshUserBooks drains pending updates before fetching from remote`() = runTest {
            // ----- Arrange -----
            val userId = 20

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerifyOrder {
                pendingProgressDrainer.drainPendingUpdates()
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            }
        }

        @Test
        fun `refreshUserBooks reapplies local progress for userBooks the drainer just synced`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val syncedUserBookId = 42

            val localUserBook = stubUserBook(id = syncedUserBookId)
            val localUserBookRead = UserBookRead(
                id = 1,
                currentPage = 200,
                currentSeconds = null,
                progress = 80f,
                startedAt = null,
                finishedAt = null,
            )

            val staleUserBook = stubUserBook(id = syncedUserBookId)
            val staleUserBookRead = UserBookRead(
                id = 1,
                currentPage = 50,
                currentSeconds = null,
                progress = 20f,
                startedAt = null,
                finishedAt = null,
            )

            val remoteBook = Book(
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
                userBook = staleUserBook,
                userBookRead = staleUserBookRead,
            )
            val localBook = remoteBook.copy(userBook = localUserBook, userBookRead = localUserBookRead)

            coEvery {
                pendingProgressDrainer.drainPendingUpdates()
            } returns setOf(syncedUserBookId)

            every {
                booksLocalDataSource.allUserBooks
            } returns flowOf(listOf(localBook))

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns listOf(remoteBook)

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(
                    books = match { books ->
                        books.any { it.userBook == localUserBook && it.userBookRead == localUserBookRead }
                    },
                )
            }
        }

        @Test
        fun `refreshUserBooks does not reapply local progress for userBooks not in synced set`() = runTest {
            // ----- Arrange -----
            val userId = 20
            val remoteBook = stubBook(userBookId = 7)

            coEvery {
                pendingProgressDrainer.drainPendingUpdates()
            } returns emptySet()

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns listOf(remoteBook)

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = listOf(remoteBook))
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

        @Test
        fun `removeAllBooks resets session flag so initializeBooks can be called again`() = runTest {
            // ----- Arrange -----
            val userId = 5

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            repository.initializeBooks(userId = userId)
            repository.removeAllBooks()

            // ----- Act -----
            val result = runCatching { repository.initializeBooks(userId = userId) }

            // ----- Assert -----
            result.isSuccess shouldBe true
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
            every { networkAvailability.isOnline } returns MutableStateFlow(false)

            val bookId = 7
            val cachedEditions = listOf(mockk<BookEdition>(relaxed = true), mockk<BookEdition>(relaxed = true))
            val localBook = mockk<Book>(relaxed = true) {
                every { this@mockk.editions } returns cachedEditions
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
            every { networkAvailability.isOnline } returns MutableStateFlow(false)

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
            every { networkAvailability.isOnline } returns MutableStateFlow(false)

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
            every { networkAvailability.isOnline } returns MutableStateFlow(false)

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
                every { this@mockk.editionId } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every { this@mockk.userBook } returns userBook
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
        fun `recovery falls back to currentEdition id when userBook is null`() = runTest {
            // ----- Arrange -----
            val missingId = 10
            val editionId = 66
            val canonicalId = 100

            val currentEdition = mockk<BookEdition>(relaxed = true) {
                every { this@mockk.id } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every { this@mockk.userBook } returns null
                every { this@mockk.currentEdition } returns currentEdition
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
                every { this@mockk.id } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every { this@mockk.userBook } returns null
                every { this@mockk.currentEdition } returns null
                every { this@mockk.editions } returns listOf(firstEdition)
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
        }

        @Test
        fun `recovery rethrows BookNotFoundException when local book has no resolvable edition id`() = runTest {
            // ----- Arrange -----
            val missingId = 10
            val notFound = BookNotFoundException(bookId = missingId)

            val localBook = mockk<Book>(relaxed = true) {
                every { this@mockk.userBook } returns null
                every { this@mockk.currentEdition } returns null
                every { this@mockk.editions } returns emptyList()
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
                every { this@mockk.editionId } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every { this@mockk.userBook } returns userBook
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
                every { this@mockk.editionId } returns editionId
            }

            val localBook = mockk<Book>(relaxed = true) {
                every { this@mockk.userBook } returns userBook
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

        private fun stubUserBookWithStatus(id: Int, status: BookStatus): UserBook = UserBook(
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
                every { this@mockk.id } returns bookId
                every { this@mockk.userBook } returns stubUserBookWithStatus(
                    id = 5,
                    status = BookStatus.Reading,
                )
            }
            val remoteBook = stubBook(userBookId = 5)

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(bookId = bookId)
            } returns remoteBook

            // ----- Act -----
            val result = repository.markBookAsWantToRead(book = book)

            // ----- Assert -----
            result shouldBe remoteBook

            coVerify {
                booksRemoteDataSource.markBookAsWantToRead(bookId = bookId)
            }

            coVerify {
                booksLocalDataSource.cacheBook(book = remoteBook)
            }
        }

        @Test
        fun `optimistic write is skipped when book has no userBook`() = runTest {
            // ----- Arrange -----
            val bookId = 11
            val book = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
                every { this@mockk.userBook } returns null
            }
            val remoteBook = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(bookId = bookId)
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
                every { this@mockk.id } returns bookId
                every { this@mockk.userBook } returns stubUserBookWithStatus(
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
                booksRemoteDataSource.markBookAsWantToRead(bookId = bookId)
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
                every { this@mockk.id } returns bookId
                every { this@mockk.userBook } returns stubUserBookWithStatus(
                    id = 5,
                    status = BookStatus.Reading,
                )
            }
            val snapshot = stubBook(userBookId = 5)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(bookId = bookId)
            } throws kotlinx.coroutines.CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<kotlinx.coroutines.CancellationException> {
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
                booksRemoteDataSource.markBookAsReading(book)
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
                booksRemoteDataSource.markBookAsReading(book)
            } returns expectedBook

            coEvery {
                booksLocalDataSource.cacheBook(book = any())
            } returns Unit

            // ----- Act -----
            repository.markBookAsReading(book = book)

            // ----- Assert -----
            coVerifyOrder {
                booksLocalDataSource.cacheBook(book = any())
                booksRemoteDataSource.markBookAsReading(book)
            }
        }

        @Test
        fun `when userBook is null cacheBook receives the original book unchanged`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = null)
            val expectedBook = stubBook(userBookId = null)
            val slot = slot<Book>()

            coEvery {
                booksRemoteDataSource.markBookAsReading(book)
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
                every { this@mockk.id } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
            }

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsReading(book)
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
                every { this@mockk.id } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
            }
            val remoteError = RuntimeException("network failure")

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsReading(book)
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
                every { this@mockk.id } returns bookId
            }

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns null

            coEvery {
                booksRemoteDataSource.markBookAsReading(book)
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
                every { this@mockk.id } returns bookId
            }
            val snapshot = stubBook(userBookId = 1)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsReading(book)
            } throws kotlinx.coroutines.CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<kotlinx.coroutines.CancellationException> {
                repository.markBookAsReading(book = book)
            }

            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
            }
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
                every { this@mockk.id } returns bookId
                every { this@mockk.userBook } returns stubUserBook(id = userBookId)
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
                every { this@mockk.id } returns bookId
                every { this@mockk.userBook } returns stubUserBook(id = userBookId)
            }
            val snapshot = stubBook(userBookId = userBookId)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } throws kotlinx.coroutines.CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<kotlinx.coroutines.CancellationException> {
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
                booksRemoteDataSource.updateBookProgress(book = book, newPage = newPage)
            } returns expectedBook

            // ----- Act -----
            val result = repository.updateBookProgress(book = book, newPage = newPage)

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

            every { networkAvailability.isOnline } returns MutableStateFlow(true)

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
            repository.updateBookProgress(book = book, newPage = 75)

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
                every { this@mockk.id } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
            }

            every { networkAvailability.isOnline } returns MutableStateFlow(true)

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
            runCatching { repository.updateBookProgress(book = book, newPage = 50) }

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
                every { this@mockk.id } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
            }
            val remoteError = RuntimeException("network failure")

            every { networkAvailability.isOnline } returns MutableStateFlow(true)

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
                repository.updateBookProgress(book = book, newPage = 50)
            } shouldBe remoteError
        }

        @Test
        fun `updateBookProgress when online and remote throws OfflineException does NOT restore snapshot`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
            }

            every { networkAvailability.isOnline } returns MutableStateFlow(true)

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
            repository.updateBookProgress(book = book, newPage = 50)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBook(book = any())
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheBook(book = snapshot)
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
                booksRemoteDataSource.markBookAsRead(book = book)
            } returns expectedBook

            // ----- Act -----
            val result = repository.markBookAsRead(book = book)

            // ----- Assert -----
            result shouldBe expectedBook
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

            every { networkAvailability.isOnline } returns MutableStateFlow(true)

            coEvery {
                booksRemoteDataSource.markBookAsRead(book = any())
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
        fun `markBookAsRead when online and remote throws non-offline error restores snapshot to local cache`() = runTest {
            // ----- Arrange -----
            val bookId = 42
            val book = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
            }

            every { networkAvailability.isOnline } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsRead(book = book)
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
                every { this@mockk.id } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
            }
            val remoteError = RuntimeException("network failure")

            every { networkAvailability.isOnline } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsRead(book = book)
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
                every { this@mockk.id } returns bookId
            }
            val snapshot = mockk<Book>(relaxed = true) {
                every { this@mockk.id } returns bookId
            }

            every { networkAvailability.isOnline } returns MutableStateFlow(true)

            coEvery {
                booksLocalDataSource.getBookById(id = bookId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.markBookAsRead(book = book)
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
                booksRemoteDataSource.updateBookEdition(userBook = userBook, newEditionId = newEditionId)
            } returns expectedBook

            // ----- Act -----
            val result = repository.updateBookEdition(userBook = userBook, newEditionId = newEditionId)

            // ----- Assert -----
            result shouldBe expectedBook
        }
    }

    @Nested
    inner class MarkEditionAsOwned {

        @Test
        fun `happy path — inserts optimistic placeholder, calls remote, replaces with real ListBook`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val bookId = 5
            val ownedListId = 99
            val edition = mockk<BookEdition> {
                every { this@mockk.id } returns editionId
                every { this@mockk.bookId } returns bookId
            }
            val realListBook = stubListBook(bookId = bookId, editionId = editionId)

            coEvery {
                booksLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns null

            coEvery {
                booksLocalDataSource.getOwnedListId()
            } returns ownedListId

            coEvery {
                booksRemoteDataSource.markEditionAsOwned(edition = edition)
            } returns realListBook

            // ----- Act -----
            repository.markEditionAsOwned(edition = edition)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheListBook(
                    book = ListBook(
                        listBookId = 0,
                        listId = ownedListId,
                        bookId = bookId,
                        editionId = editionId,
                    ),
                )
            }

            coVerify {
                booksRemoteDataSource.markEditionAsOwned(edition = edition)
            }

            coVerify {
                booksLocalDataSource.removeOwnedListBookByEditionId(editionId = editionId)
            }

            coVerify {
                booksLocalDataSource.cacheListBook(book = realListBook)
            }
        }

        @Test
        fun `when getOwnedListId returns null — skips optimistic insert but still calls remote and caches real result`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val bookId = 5
            val edition = mockk<BookEdition> {
                every { this@mockk.id } returns editionId
                every { this@mockk.bookId } returns bookId
            }
            val realListBook = stubListBook(bookId = bookId, editionId = editionId)

            coEvery {
                booksLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns null

            coEvery {
                booksLocalDataSource.getOwnedListId()
            } returns null

            coEvery {
                booksRemoteDataSource.markEditionAsOwned(edition = edition)
            } returns realListBook

            // ----- Act -----
            repository.markEditionAsOwned(edition = edition)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheListBook(book = any())
            }

            coVerify {
                booksRemoteDataSource.markEditionAsOwned(edition = edition)
            }

            coVerify {
                booksLocalDataSource.cacheListBook(book = realListBook)
            }
        }

        @Test
        fun `failure path — restores snapshot and rethrows on non-cancellation exception`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val bookId = 5
            val edition = mockk<BookEdition> {
                every { this@mockk.id } returns editionId
                every { this@mockk.bookId } returns bookId
            }
            val snapshot = stubListBook(bookId = bookId, editionId = editionId)
            val remoteError = RuntimeException("network error")

            coEvery {
                booksLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns snapshot

            coEvery {
                booksLocalDataSource.getOwnedListId()
            } returns null

            coEvery {
                booksRemoteDataSource.markEditionAsOwned(edition = edition)
            } throws remoteError

            // ----- Act -----
            val caught = runCatching { repository.markEditionAsOwned(edition = edition) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify {
                booksLocalDataSource.removeOwnedListBookByEditionId(editionId = editionId)
            }

            coVerify {
                booksLocalDataSource.cacheListBook(book = snapshot)
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without touching local cache for rollback`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val bookId = 5
            val edition = mockk<BookEdition> {
                every { this@mockk.id } returns editionId
                every { this@mockk.bookId } returns bookId
            }

            coEvery {
                booksLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns null

            coEvery {
                booksLocalDataSource.getOwnedListId()
            } returns null

            coEvery {
                booksRemoteDataSource.markEditionAsOwned(edition = edition)
            } throws kotlinx.coroutines.CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<kotlinx.coroutines.CancellationException> {
                repository.markEditionAsOwned(edition = edition)
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.removeOwnedListBookByEditionId(editionId = any())
            }
        }
    }

    @Nested
    inner class RemoveOwnedEdition {

        @Test
        fun `happy path — removes locally, calls remote, caches updated BookList`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val snapshot = stubListBook(bookId = 5, editionId = editionId)
            val updatedBookList = stubBookList()

            coEvery {
                booksLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.removeListBook(book = snapshot)
            } returns updatedBookList

            // ----- Act -----
            repository.removeOwnedEdition(editionId = editionId)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.removeOwnedListBookByEditionId(editionId = editionId)
            }

            coVerify {
                booksRemoteDataSource.removeListBook(book = snapshot)
            }

            coVerify {
                booksLocalDataSource.cacheUserBookLists(lists = listOf(updatedBookList))
            }
        }

        @Test
        fun `when snapshot is null — returns early without calling remote`() = runTest {
            // ----- Arrange -----
            val editionId = 10

            coEvery {
                booksLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns null

            // ----- Act -----
            repository.removeOwnedEdition(editionId = editionId)

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.removeListBook(book = any())
            }
        }

        @Test
        fun `failure path — re-caches snapshot and rethrows on non-cancellation exception`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val snapshot = stubListBook(bookId = 5, editionId = editionId)
            val remoteError = RuntimeException("network error")

            coEvery {
                booksLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.removeListBook(book = snapshot)
            } throws remoteError

            // ----- Act -----
            val caught = runCatching { repository.removeOwnedEdition(editionId = editionId) }

            // ----- Assert -----
            caught.exceptionOrNull() shouldBe remoteError

            coVerify {
                booksLocalDataSource.cacheListBook(book = snapshot)
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without rollback`() = runTest {
            // ----- Arrange -----
            val editionId = 10
            val snapshot = stubListBook(bookId = 5, editionId = editionId)

            coEvery {
                booksLocalDataSource.findOwnedListBookByEditionId(editionId = editionId)
            } returns snapshot

            coEvery {
                booksRemoteDataSource.removeListBook(book = snapshot)
            } throws kotlinx.coroutines.CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<kotlinx.coroutines.CancellationException> {
                repository.removeOwnedEdition(editionId = editionId)
            }

            coVerify(exactly = 0) {
                booksLocalDataSource.cacheListBook(book = any())
            }
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
                booksRemoteDataSource.fetchBooksByIds(ids = ids, forceNetwork = false)
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
                booksRemoteDataSource.fetchBooksByIds(ids = ids, forceNetwork = false)
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
                booksRemoteDataSource.fetchEditionsByIds(ids = ids, forceNetwork = false)
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
                booksRemoteDataSource.fetchEditionsByIds(ids = ids, forceNetwork = false)
            } returns emptyList()

            // ----- Act -----
            val result = repository.fetchEditionsByIds(ids = ids)

            // ----- Assert -----
            result shouldBe emptyList()
        }
    }

    @Nested
    inner class HydrateOrphanOwnedBooks {

        // The default stubBookList id is 1. We set enabledListIds = {1} so the list is
        // treated as enabled and its books are passed into hydrateOrphanOwnedBooks.
        private fun setupFetchAndCache(
            fetchedBooks: List<Book> = emptyList(),
            fetchedLists: List<BookList> = emptyList(),
            localUserBookIds: List<Int> = emptyList(),
            enabledListId: Int = 1,
        ) {
            every {
                settingsRepository.enabledListIds
            } returns flowOf(setOf(enabledListId))

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = any(), statusIds = any())
            } returns fetchedBooks

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = any(), listIds = null)
            } returns fetchedLists

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns localUserBookIds

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = any(), forceNetwork = any())
            } returns emptyList()
        }

        @Test
        fun `does not call fetchBooksByIds when lists have no referenced bookIds`() = runTest {
            // ----- Arrange -----
            setupFetchAndCache(fetchedLists = emptyList())

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBooksByIds(ids = any(), forceNetwork = any())
            }
        }

        @Test
        fun `refreshUserBooks fetches all referenced bookIds with forceNetwork regardless of cache`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(bookId = 5)
            val bookList = stubBookList(listBooks = listOf(listBook))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(5), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(5), forceNetwork = true)
            }
        }

        @Test
        fun `fetches and caches missing bookIds when orphans are present`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(bookId = 7)
            val bookList = stubBookList(listBooks = listOf(listBook))
            val orphanBook = stubBook(userBookId = null)

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = listOf(7))
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(7), forceNetwork = true)
            } returns listOf(orphanBook)

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBooks(books = listOf(orphanBook))
            }
        }

        @Test
        fun `cacheBooks is called before cacheUserBookLists for orphan books`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(bookId = 8)
            val bookList = stubBookList(listBooks = listOf(listBook))
            val orphanBook = stubBook(userBookId = null)

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = listOf(8))
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(8), forceNetwork = true)
            } returns listOf(orphanBook)

            val callOrder = mutableListOf<String>()

            coEvery {
                booksLocalDataSource.cacheBooks(books = listOf(orphanBook))
            } answers {
                callOrder += "cacheBooks(orphan)"
            }

            coEvery {
                booksLocalDataSource.cacheUserBookLists(lists = any())
            } answers {
                callOrder += "cacheUserBookLists"
            }

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            val orphanCacheIdx = callOrder.indexOf("cacheBooks(orphan)")
            val listCacheIdx = callOrder.indexOf("cacheUserBookLists")
            (orphanCacheIdx < listCacheIdx) shouldBe true
        }

        @Test
        fun `refreshUserBooks fetches all referenced bookIds in a single batch with forceNetwork`() = runTest {
            // ----- Arrange -----
            val listBook1 = stubListBook(bookId = 10)
            val listBook2 = stubListBook(bookId = 11)
            val bookList = stubBookList(listBooks = listOf(listBook1, listBook2))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(10, 11), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(10, 11), forceNetwork = true)
            }
        }

        @Test
        fun `does not cache orphan books when fetchBooksByIds returns empty list`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(bookId = 9)
            val bookList = stubBookList(listBooks = listOf(listBook))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = listOf(9))
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(9), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            // cacheBooks is called once for user books (emptyList), not a second time for orphans
            coVerify(exactly = 1) {
                booksLocalDataSource.cacheBooks(books = any())
            }
        }

        @Test
        fun `refreshUserBooks fetches all referenced editionIds with forceNetwork regardless of cache`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(bookId = 5, editionId = 20)
            val bookList = stubBookList(listBooks = listOf(listBook))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(5), forceNetwork = true)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(20), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(20), forceNetwork = true)
            }
        }

        @Test
        fun `fetches and caches missing editionIds when orphan editions are present`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(bookId = 5, editionId = 30)
            val bookList = stubBookList(listBooks = listOf(listBook))
            val orphanEdition = stubBookEdition()

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(5), forceNetwork = true)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(30), forceNetwork = true)
            } returns listOf(orphanEdition)

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheEditions(editions = listOf(orphanEdition))
            }
        }

        @Test
        fun `does not call cacheEditions when fetchEditionsByIds returns empty list`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(bookId = 5, editionId = 40)
            val bookList = stubBookList(listBooks = listOf(listBook))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(5), forceNetwork = true)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(40), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksLocalDataSource.cacheEditions(editions = any())
            }
        }

        @Test
        fun `book hydration runs before edition hydration`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(bookId = 50, editionId = 50)
            val bookList = stubBookList(listBooks = listOf(listBook))
            val orphanBook = stubBook(userBookId = null)
            val orphanEdition = stubBookEdition()

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = listOf(50))
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(50), forceNetwork = true)
            } returns listOf(orphanBook)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = listOf(50))
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(50), forceNetwork = true)
            } returns listOf(orphanEdition)

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerifyOrder {
                booksLocalDataSource.cacheBooks(books = listOf(orphanBook))
                booksLocalDataSource.cacheEditions(editions = listOf(orphanEdition))
            }
        }

        @Test
        fun `refreshUserBooks fetches all referenced editionIds in a single batch with forceNetwork`() = runTest {
            // ----- Arrange -----
            val listBook1 = stubListBook(bookId = 10, editionId = 10)
            val listBook2 = stubListBook(bookId = 11, editionId = 11)
            val bookList = stubBookList(listBooks = listOf(listBook1, listBook2))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(10, 11), forceNetwork = true)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(10, 11), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(10, 11), forceNetwork = true)
            }
        }

        @Test
        fun `hydrateOrphanOwnedBooks still runs for the owned list even when it is not in enabledListIds`() = runTest {
            // ----- Arrange -----
            val ownedListId = 77
            val ownedBookId = 200
            val ownedEditionId = 300
            val ownedListBook = stubListBook(bookId = ownedBookId, editionId = ownedEditionId)
            val ownedList = stubBookList(id = ownedListId, slug = "owned", listBooks = listOf(ownedListBook))
            val orphanBook = stubBook(userBookId = null)

            // Owned list is NOT in enabledListIds
            every {
                settingsRepository.enabledListIds
            } returns flowOf(emptySet())

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = any(), statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = any(), listIds = null)
            } returns listOf(ownedList)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            // forceRefreshAll = true: hydrateOrphanOwnedBooks fetches ALL referenced IDs, no cache check
            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(ownedBookId), forceNetwork = true)
            } returns listOf(orphanBook)

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(ownedEditionId), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(ownedBookId), forceNetwork = true)
            }
        }

        @Test
        fun `refreshUserBooks fetches all owned list books and editions with forceNetwork even when cached`() = runTest {
            // ----- Arrange -----
            val ownedListId = 88
            val ownedBookId = 400
            val ownedEditionId = 500
            val ownedListBook = stubListBook(bookId = ownedBookId, editionId = ownedEditionId)
            val ownedList = stubBookList(id = ownedListId, slug = "owned", listBooks = listOf(ownedListBook))

            every {
                settingsRepository.enabledListIds
            } returns flowOf(emptySet())

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = any(), statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = any(), listIds = null)
            } returns listOf(ownedList)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(ownedBookId), forceNetwork = true)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(ownedEditionId), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(ownedBookId), forceNetwork = true)
            }
            coVerify {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(ownedEditionId), forceNetwork = true)
            }
        }

        @Test
        fun `refreshUserBooks re-fetches bookIds already in cache with forceNetwork true`() = runTest {
            // ----- Arrange -----
            val cachedBookId = 70
            val listBook = stubListBook(bookId = cachedBookId, editionId = 700)
            val bookList = stubBookList(listBooks = listOf(listBook))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            // The bookId is already cached — getExistingBookIds returns it
            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = any())
            } returns listOf(cachedBookId)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(cachedBookId), forceNetwork = true)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(700), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(cachedBookId), forceNetwork = true)
            }
        }

        @Test
        fun `refreshUserBooks re-fetches editionIds already in cache with forceNetwork true`() = runTest {
            // ----- Arrange -----
            val cachedEditionId = 800
            val listBook = stubListBook(bookId = 80, editionId = cachedEditionId)
            val bookList = stubBookList(listBooks = listOf(listBook))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            // The editionId is already cached — getExistingEditionIds returns it
            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = any())
            } returns listOf(cachedEditionId)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(80), forceNetwork = true)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(cachedEditionId), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(cachedEditionId), forceNetwork = true)
            }
        }

        @Test
        fun `books from non-hydrated lists do not contribute to orphan resolution`() = runTest {
            // ----- Arrange -----
            val enabledListId = 1
            val disabledListId = 2
            val enabledListBook = stubListBook(bookId = 10, editionId = 100)
            val disabledListBook = stubListBook(bookId = 20, editionId = 200)
            val enabledList = stubBookList(id = enabledListId, listBooks = listOf(enabledListBook))
            val disabledList = stubBookList(id = disabledListId, slug = "some-disabled-list", listBooks = listOf(disabledListBook))

            every {
                settingsRepository.enabledListIds
            } returns flowOf(setOf(enabledListId))

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = any(), statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = any(), listIds = null)
            } returns listOf(enabledList, disabledList)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(10), forceNetwork = true)
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(100), forceNetwork = true)
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            // The disabled list's bookId (20) must never be fetched
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBooksByIds(ids = match { it.contains(20) }, forceNetwork = any())
            }
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchEditionsByIds(ids = match { it.contains(200) }, forceNetwork = any())
            }
        }
    }

    @Nested
    inner class InitializeBooksHydration {

        private fun setupInitializeFetchAndCache(
            fetchedLists: List<BookList> = emptyList(),
            localUserBookIds: List<Int> = emptyList(),
            enabledListId: Int = 1,
        ) {
            every {
                settingsRepository.enabledListIds
            } returns flowOf(setOf(enabledListId))

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = any(), statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = any(), listIds = null)
            } returns fetchedLists

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns localUserBookIds

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = any(), forceNetwork = any())
            } returns emptyList()
        }

        @Test
        fun `initializeBooks fetches only missing bookIds with forceNetwork false`() = runTest {
            // ----- Arrange -----
            val cachedBookId = 10
            val missingBookId = 11
            val listBook1 = stubListBook(bookId = cachedBookId)
            val listBook2 = stubListBook(bookId = missingBookId)
            val bookList = stubBookList(listBooks = listOf(listBook1, listBook2))
            val missingBook = stubBook(userBookId = null)

            setupInitializeFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksLocalDataSource.getExistingBookIds(
                    ids = match { it.containsAll(listOf(cachedBookId, missingBookId)) },
                )
            } returns listOf(cachedBookId)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(missingBookId), forceNetwork = false)
            } returns listOf(missingBook)

            // ----- Act -----
            repository.initializeBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(missingBookId), forceNetwork = false)
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBooksByIds(
                    ids = match { it.contains(cachedBookId) },
                    forceNetwork = any(),
                )
            }
        }

        @Test
        fun `initializeBooks fetches only missing editionIds with forceNetwork false`() = runTest {
            // ----- Arrange -----
            val cachedEditionId = 20
            val missingEditionId = 21
            val listBook1 = stubListBook(bookId = 1, editionId = cachedEditionId)
            val listBook2 = stubListBook(bookId = 2, editionId = missingEditionId)
            val bookList = stubBookList(listBooks = listOf(listBook1, listBook2))
            val missingEdition = stubBookEdition()

            setupInitializeFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = any())
            } returns listOf(1, 2)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(
                    ids = match { it.containsAll(listOf(cachedEditionId, missingEditionId)) },
                )
            } returns listOf(cachedEditionId)

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(missingEditionId), forceNetwork = false)
            } returns listOf(missingEdition)

            // ----- Act -----
            repository.initializeBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(missingEditionId), forceNetwork = false)
            }

            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchEditionsByIds(
                    ids = match { it.contains(cachedEditionId) },
                    forceNetwork = any(),
                )
            }
        }
    }

    @Nested
    inner class FetchAndCacheBooksOrdering {

        private fun setupMinimalFetchAndCache(userId: Int) {
            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns emptyList()

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()
        }

        @Test
        fun `cacheBooks runs before removeUserBooksById`() = runTest {
            // ----- Arrange -----
            val userId = 30

            setupMinimalFetchAndCache(userId = userId)

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerifyOrder {
                booksLocalDataSource.cacheBooks(books = any())
                booksLocalDataSource.removeUserBooksById(ids = any())
            }
        }

        @Test
        fun `removeUserBooksById runs before syncBookListMetadata`() = runTest {
            // ----- Arrange -----
            val userId = 31

            setupMinimalFetchAndCache(userId = userId)

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerifyOrder {
                booksLocalDataSource.removeUserBooksById(ids = any())
                booksLocalDataSource.syncBookListMetadata(serverListIds = any())
            }
        }

        @Test
        fun `hydrateOrphanOwnedBooks runs before cacheUserBookLists`() = runTest {
            // ----- Arrange -----
            val userId = 32
            val listId = 1
            val listBook = stubListBook(bookId = 60, editionId = 60)
            val bookList = stubBookList(id = listId, listBooks = listOf(listBook))
            val orphanBook = stubBook(userBookId = null)

            every {
                settingsRepository.enabledListIds
            } returns flowOf(setOf(listId))

            coEvery {
                booksRemoteDataSource.initializeBooks(userId = userId, statusIds = any())
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchUserLists(userId = userId, listIds = null)
            } returns listOf(bookList)

            coEvery {
                booksLocalDataSource.getAllUserBookIds()
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(60), forceNetwork = true)
            } returns listOf(orphanBook)

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(60), forceNetwork = true)
            } returns emptyList()

            val callOrder = mutableListOf<String>()

            coEvery {
                booksLocalDataSource.cacheBooks(books = listOf(orphanBook))
            } answers {
                callOrder += "cacheBooks(orphan)"
            }

            coEvery {
                booksLocalDataSource.cacheUserBookLists(lists = any())
            } answers {
                callOrder += "cacheUserBookLists"
            }

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            val orphanCacheIdx = callOrder.indexOf("cacheBooks(orphan)")
            val listCacheIdx = callOrder.indexOf("cacheUserBookLists")
            (orphanCacheIdx < listCacheIdx) shouldBe true
        }

        @Test
        fun `cacheUserBookLists runs before deleteOrphanBooks`() = runTest {
            // ----- Arrange -----
            val userId = 33

            setupMinimalFetchAndCache(userId = userId)

            // ----- Act -----
            repository.refreshUserBooks(userId = userId)

            // ----- Assert -----
            coVerifyOrder {
                booksLocalDataSource.cacheUserBookLists(lists = any())
                booksLocalDataSource.deleteOrphanBooks()
            }
        }
    }

    @Nested
    inner class PersistEditionImage {

        @Test
        fun `delegates to local data source with the given editionId and source`() = runTest {
            // ----- Arrange -----
            val editionId = 42
            val source: java.io.File = mockk()

            coEvery {
                booksLocalDataSource.persistEditionImage(editionId = editionId, source = source)
            } returns Unit

            // ----- Act -----
            repository.persistEditionImage(editionId = editionId, source = source)

            // ----- Assert -----
            coVerify(exactly = 1) {
                booksLocalDataSource.persistEditionImage(editionId = editionId, source = source)
            }
        }
    }

    @Nested
    inner class OfflineBehavior {

        private fun stubUserBook(id: Int = 1): UserBook = mockk(relaxed = true) {
            every { this@mockk.id } returns id
            every { this@mockk.editionId } returns null
        }

        private fun stubUserBookRead(id: Int = 1): UserBookRead = UserBookRead(
            id = id,
            currentPage = 50,
            currentSeconds = null,
            progress = 0.5f,
            startedAt = null,
            finishedAt = null,
        )

        private fun stubBookWithUserBookRead(userBookId: Int = 1, userBookReadId: Int = 1): Book = Book(
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
            every { networkAvailability.isOnline } returns MutableStateFlow(false)

            val book = stubBookWithUserBookRead()
            val newPage = 75

            // ----- Act -----
            repository.updateBookProgress(book = book, newPage = newPage)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = any())
            }
            coVerify {
                offlineProgressQueue.enqueue(
                    update = match { it.kind == PendingProgressUpdateKind.UPDATE_PROGRESS },
                )
            }
            coVerify(exactly = 0) {
                booksRemoteDataSource.updateBookProgress(book = any(), newPage = any(), newSeconds = any())
            }
        }

        @Test
        fun `markBookAsRead when offline caches optimistic book and enqueues MARK_AS_READ without calling remote`() = runTest {
            // ----- Arrange -----
            every { networkAvailability.isOnline } returns MutableStateFlow(false)

            val book = stubBookWithUserBookRead()

            // ----- Act -----
            repository.markBookAsRead(book = book)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheBook(book = any())
            }
            coVerify {
                offlineProgressQueue.enqueue(
                    update = match { it.kind == PendingProgressUpdateKind.MARK_AS_READ },
                )
            }
            coVerify(exactly = 0) {
                booksRemoteDataSource.markBookAsRead(book = any())
            }
        }
    }
}
