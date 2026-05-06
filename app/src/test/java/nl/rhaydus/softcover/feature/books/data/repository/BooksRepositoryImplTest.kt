package nl.rhaydus.softcover.feature.books.data.repository

import app.cash.turbine.test
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
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
        listBookId = 0,
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
                positionInSeries = null,
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
    }

    @Nested
    inner class MarkBookAsWantToRead {

        @Test
        fun `markBookAsWantToRead delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val bookId = 11
            val expectedBook = stubBook(userBookId = null)

            coEvery {
                booksRemoteDataSource.markBookAsWantToRead(bookId = bookId)
            } returns expectedBook

            // ----- Act -----
            val result = repository.markBookAsWantToRead(bookId = bookId)

            // ----- Assert -----
            result shouldBe expectedBook
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
                positionInSeries = null,
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
    }

    @Nested
    inner class RemoveBookFromLibrary {

        @Test
        fun `removeBookFromLibrary delegates to remote data source`() = runTest {
            // ----- Arrange -----
            val book = stubBook(userBookId = 1)

            coEvery {
                booksRemoteDataSource.removeBookFromLibrary(book = book)
            } returns Unit

            // ----- Act -----
            repository.removeBookFromLibrary(book = book)

            // ----- Assert -----
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
                positionInSeries = null,
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
                positionInSeries = null,
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
        fun `markEditionAsOwned delegates to remote data source and returns result`() = runTest {
            // ----- Arrange -----
            val edition = stubBookEdition()
            val expectedListBook = stubListBook()

            coEvery {
                booksRemoteDataSource.markEditionAsOwned(edition = edition)
            } returns expectedListBook

            // ----- Act -----
            val result = repository.markEditionAsOwned(edition = edition)

            // ----- Assert -----
            result shouldBe expectedListBook
        }
    }

    @Nested
    inner class GetListBookByEditionId {

        @Test
        fun `getListBookByEditionId delegates to local data source and returns result`() = runTest {
            // ----- Arrange -----
            val editionId = 55
            val expectedListBook = stubListBook()

            coEvery {
                booksLocalDataSource.getOwnedListBookByEditionId(editionId = editionId)
            } returns expectedListBook

            // ----- Act -----
            val result = repository.getListBookByEditionId(editionId = editionId)

            // ----- Assert -----
            result shouldBe expectedListBook
        }
    }

    @Nested
    inner class RemoveListBook {

        @Test
        fun `removeListBook fetches updated list from remote and caches it locally`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook()
            val updatedBookList = stubBookList()

            coEvery {
                booksRemoteDataSource.removeListBook(book = listBook)
            } returns updatedBookList

            // ----- Act -----
            repository.removeListBook(book = listBook)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheUserBookLists(lists = listOf(updatedBookList))
            }
        }

        @Test
        fun `removeListBook calls remote before caching locally`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook()
            val updatedBookList = stubBookList()

            coEvery {
                booksRemoteDataSource.removeListBook(book = listBook)
            } returns updatedBookList

            // ----- Act -----
            repository.removeListBook(book = listBook)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.removeListBook(book = listBook)
            }
            coVerify {
                booksLocalDataSource.cacheUserBookLists(lists = listOf(updatedBookList))
            }
        }
    }

    @Nested
    inner class CacheListBook {

        @Test
        fun `cacheListBook delegates to local data source`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook()

            // ----- Act -----
            repository.cacheListBook(book = listBook)

            // ----- Assert -----
            coVerify {
                booksLocalDataSource.cacheListBook(book = listBook)
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
                booksRemoteDataSource.fetchBooksByIds(ids = ids)
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
                booksRemoteDataSource.fetchBooksByIds(ids = ids)
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
                booksRemoteDataSource.fetchEditionsByIds(ids = ids)
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
                booksRemoteDataSource.fetchEditionsByIds(ids = ids)
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
                booksRemoteDataSource.fetchEditionsByIds(ids = any())
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
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            }
        }

        @Test
        fun `does not call fetchBooksByIds when all referenced bookIds are already cached`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(bookId = 5)
            val bookList = stubBookList(listBooks = listOf(listBook))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = listOf(5))
            } returns listOf(5)

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
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
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(7))
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
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(8))
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
        fun `only fetches the ids that are missing from the local cache`() = runTest {
            // ----- Arrange -----
            val listBook1 = stubListBook(bookId = 10)
            val listBook2 = stubListBook(bookId = 11)
            val bookList = stubBookList(listBooks = listOf(listBook1, listBook2))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = listOf(10, 11))
            } returns listOf(10)

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(11))
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(11))
            }
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(10))
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
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(9))
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
        fun `does not call fetchEditionsByIds when all referenced editionIds are already cached`() = runTest {
            // ----- Arrange -----
            val listBook = stubListBook(bookId = 5, editionId = 20)
            val bookList = stubBookList(listBooks = listOf(listBook))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = listOf(5))
            } returns listOf(5)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = listOf(20))
            } returns listOf(20)

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchEditionsByIds(ids = any())
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
                booksLocalDataSource.getExistingBookIds(ids = listOf(5))
            } returns listOf(5)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = listOf(30))
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(30))
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
                booksLocalDataSource.getExistingBookIds(ids = listOf(5))
            } returns listOf(5)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = listOf(40))
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(40))
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
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(50))
            } returns listOf(orphanBook)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = listOf(50))
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(50))
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
        fun `only fetches the editionIds that are missing from the local cache`() = runTest {
            // ----- Arrange -----
            val listBook1 = stubListBook(bookId = 10, editionId = 10)
            val listBook2 = stubListBook(bookId = 11, editionId = 11)
            val bookList = stubBookList(listBooks = listOf(listBook1, listBook2))

            setupFetchAndCache(fetchedLists = listOf(bookList))

            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = listOf(10, 11))
            } returns listOf(10, 11)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = listOf(10, 11))
            } returns listOf(10)

            coEvery {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(11))
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(11))
            }
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchEditionsByIds(ids = listOf(10))
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

            // The owned book is not yet in the local cache → hydrateOrphanOwnedBooks must fetch it
            coEvery {
                booksLocalDataSource.getExistingBookIds(ids = listOf(ownedBookId))
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(ownedBookId))
            } returns listOf(orphanBook)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = listOf(ownedEditionId))
            } returns listOf(ownedEditionId)

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(ownedBookId))
            }
        }

        @Test
        fun `does not call fetchBooksByIds or fetchEditionsByIds when owned list books and editions are already cached`() = runTest {
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
                booksLocalDataSource.getExistingBookIds(ids = listOf(ownedBookId))
            } returns listOf(ownedBookId)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = listOf(ownedEditionId))
            } returns listOf(ownedEditionId)

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchBooksByIds(ids = any())
            }
            coVerify(exactly = 0) {
                booksRemoteDataSource.fetchEditionsByIds(ids = any())
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
                booksLocalDataSource.getExistingBookIds(ids = listOf(10))
            } returns listOf(10)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = listOf(100))
            } returns listOf(100)

            // ----- Act -----
            repository.refreshUserBooks(userId = 1)

            // ----- Assert -----
            // The disabled list's bookId (20) must never reach getExistingBookIds
            coVerify(exactly = 0) {
                booksLocalDataSource.getExistingBookIds(ids = match { it.contains(20) })
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
                booksLocalDataSource.getExistingBookIds(ids = listOf(60))
            } returns emptyList()

            coEvery {
                booksRemoteDataSource.fetchBooksByIds(ids = listOf(60))
            } returns listOf(orphanBook)

            coEvery {
                booksLocalDataSource.getExistingEditionIds(ids = listOf(60))
            } returns listOf(60)

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
            positionInSeries = null,
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
