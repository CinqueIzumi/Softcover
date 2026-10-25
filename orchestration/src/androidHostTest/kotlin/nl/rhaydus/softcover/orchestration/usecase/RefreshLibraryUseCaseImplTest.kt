package nl.rhaydus.softcover.orchestration.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.lists.domain.model.ListsRefreshResult
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import nl.rhaydus.softcover.core.personal.domain.usecase.RefreshAllReadingJournalHistoryUseCase
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RefreshLibraryUseCaseImplTest {
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var booksRepository: BooksRepository
    private lateinit var listsRepository: ListsRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var refreshAllReadingJournalHistoryUseCase: RefreshAllReadingJournalHistoryUseCase
    private lateinit var useCase: RefreshLibraryUseCaseImpl

    @BeforeEach
    fun setUp() {
        getUserIdUseCase = mockk()
        booksRepository = mockk(relaxed = true)
        listsRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        refreshAllReadingJournalHistoryUseCase = mockk()

        coEvery {
            refreshAllReadingJournalHistoryUseCase()
        } returns Result.success(Unit)

        every {
            settingsRepository.listDefaultsSeeded
        } returns flowOf(true)

        val dispatcher = UnconfinedTestDispatcher()

        useCase = RefreshLibraryUseCaseImpl(
            getUserIdUseCase = getUserIdUseCase,
            booksRepository = booksRepository,
            listsRepository = listsRepository,
            settingsRepository = settingsRepository,
            refreshAllReadingJournalHistoryUseCase = refreshAllReadingJournalHistoryUseCase,
            applicationScope = ApplicationScope(scope = CoroutineScope(dispatcher)),
            dispatchers = AppDispatchers(
                main = dispatcher,
                io = dispatcher,
                default = dispatcher,
            ),
        )
    }

    private fun stubBookList(
        id: Int = 1,
        slug: String = "some-list",
        books: List<ListBook> = emptyList(),
    ): BookList = BookList(
        id = id,
        name = slug,
        slug = slug,
        books = books,
    )

    private fun stubListBook(
        bookId: Int = 1,
        editionId: Int = 100,
    ): ListBook = ListBook(
        listBookId = 1,
        listId = 0,
        bookId = bookId,
        editionId = editionId,
    )

    @Nested
    inner class GetUserIdFailure {
        @Test
        fun `returns failure when getUserIdUseCase returns failure — no repo calls made`() = runTest {
            // ----- Arrange -----
            val expectedError = IllegalStateException("no user id")

            coEvery {
                getUserIdUseCase()
            } returns Result.failure(expectedError)

            // ----- Act -----
            val result = useCase()

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError

            coVerify(exactly = 0) {
                booksRepository.refreshUserBooks(
                    userId = any(),
                    statusFilter = any(),
                )
            }

            coVerify(exactly = 0) {
                listsRepository.refreshUserLists(userId = any())
            }
        }
    }

    @Nested
    inner class AllScope {
        @BeforeEach
        fun setUpUserId() {
            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)
        }

        @Test
        fun `refreshes books and lists, hydrates, syncs server ids, caches changes, drops orphans`() = runTest {
            // ----- Arrange -----
            // changedList has id=7; the server also knows lists 8 and 9, which are unchanged
            val changedList = stubBookList(
                id = 7,
                books = listOf(
                    stubListBook(
                        bookId = 10,
                        editionId = 100,
                    ),
                ),
            )

            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                serverListIds = setOf(7, 8, 9),
                changedLists = listOf(changedList),
            )

            // ----- Act -----
            val result = useCase(scope = RefreshScope.All)

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                booksRepository.refreshUserBooks(
                    userId = 55,
                    statusFilter = null,
                )
                booksRepository.hydrateReferencedBooks(
                    bookIds = listOf(10),
                    editionIds = listOf(100),
                    forceNetwork = true,
                )
                listsRepository.syncBookListMetadata(serverListIds = setOf(7, 8, 9))
                listsRepository.cacheUserBookLists(lists = listOf(changedList))
                booksRepository.deleteOrphanBooks()
            }
        }

        @Test
        fun `refreshing the books throws — returns failure`() = runTest {
            // ----- Arrange -----
            val expectedError = IllegalStateException("books refresh failed")

            coEvery {
                booksRepository.refreshUserBooks(
                    userId = 55,
                    statusFilter = null,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(scope = RefreshScope.All)

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }

        @Test
        fun `seeds enabledListIds with owned list id when listDefaultsSeeded is false`() = runTest {
            // ----- Arrange -----
            val ownedList = stubBookList(
                id = 42,
                slug = "owned",
            )

            every {
                settingsRepository.listDefaultsSeeded
            } returns flowOf(false)

            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                serverListIds = emptySet(),
                changedLists = listOf(ownedList),
            )

            // ----- Act -----
            useCase(scope = RefreshScope.All)

            // ----- Assert -----
            coVerify {
                settingsRepository.seedEnabledListIds(ids = setOf(42))
            }
        }

        @Test
        fun `does not seed enabledListIds when listDefaultsSeeded is true`() = runTest {
            // ----- Arrange -----
            val ownedList = stubBookList(
                id = 42,
                slug = "owned",
            )

            every {
                settingsRepository.listDefaultsSeeded
            } returns flowOf(true)

            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                serverListIds = emptySet(),
                changedLists = listOf(ownedList),
            )

            // ----- Act -----
            useCase(scope = RefreshScope.All)

            // ----- Assert -----
            coVerify(exactly = 0) {
                settingsRepository.seedEnabledListIds(ids = any())
            }
        }

        @Test
        fun `seeds with empty set when no owned list is found and listDefaultsSeeded is false`() = runTest {
            // ----- Arrange -----
            val nonOwnedList = stubBookList(
                id = 5,
                slug = "other",
            )

            every {
                settingsRepository.listDefaultsSeeded
            } returns flowOf(false)

            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                serverListIds = emptySet(),
                changedLists = listOf(nonOwnedList),
            )

            // ----- Act -----
            useCase(scope = RefreshScope.All)

            // ----- Assert -----
            coVerify {
                settingsRepository.seedEnabledListIds(ids = emptySet())
            }
        }
    }

    @Nested
    inner class ByStatusesScope {
        @Test
        fun `calls refreshUserBooks with the given statusFilter and never touches lists`() = runTest {
            // ----- Arrange -----
            val statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.PAUSED)

            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)

            // ----- Act -----
            val result = useCase(scope = RefreshScope.ByStatuses(statuses = statuses))

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                booksRepository.refreshUserBooks(
                    userId = 55,
                    statusFilter = statuses,
                )
            }

            coVerify(exactly = 0) {
                listsRepository.fetchUserLists(
                    userId = any(),
                    listIds = any(),
                )
            }

            coVerify(exactly = 0) {
                listsRepository.cacheUserBookLists(lists = any())
            }

            coVerify(exactly = 0) {
                listsRepository.syncBookListMetadata(serverListIds = any())
            }
        }
    }

    @Nested
    inner class ByListScope {
        @Test
        fun `fetches the single list by id, hydrates and caches it, and leaves books and list metadata alone`() = runTest {
            // ----- Arrange -----
            val listId = 7
            val fetchedList = stubBookList(
                id = listId,
                books = listOf(
                    stubListBook(
                        bookId = 10,
                        editionId = 100,
                    ),
                ),
            )

            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)

            coEvery {
                listsRepository.fetchUserLists(
                    userId = 55,
                    listIds = setOf(listId),
                )
            } returns listOf(fetchedList)

            // ----- Act -----
            val result = useCase(scope = RefreshScope.ByList(listId = listId))

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                booksRepository.hydrateReferencedBooks(
                    bookIds = listOf(10),
                    editionIds = listOf(100),
                    forceNetwork = true,
                )
                listsRepository.cacheUserBookLists(lists = listOf(fetchedList))
            }

            coVerify(exactly = 0) {
                booksRepository.refreshUserBooks(
                    userId = any(),
                    statusFilter = any(),
                )
                listsRepository.syncBookListMetadata(serverListIds = any())
                booksRepository.deleteOrphanBooks()
            }
        }
    }

    @Nested
    inner class ReadingJournalRefresh {
        @Test
        fun `a full refresh also refreshes the reading journal for every book`() = runTest {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)

            // ----- Act -----
            val result = useCase(scope = RefreshScope.All)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { refreshAllReadingJournalHistoryUseCase() }
        }

        @Test
        fun `a status refresh also refreshes the reading journal`() = runTest {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)

            // ----- Act -----
            val result = useCase(scope = RefreshScope.ByStatuses(statuses = setOf(UserBookStatus.READ)))

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { refreshAllReadingJournalHistoryUseCase() }
        }

        @Test
        fun `a single-list refresh leaves the reading journal alone`() = runTest {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)

            // ----- Act -----
            val result = useCase(scope = RefreshScope.ByList(listId = 7))

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) { refreshAllReadingJournalHistoryUseCase() }
        }

        @Test
        fun `a failing journal refresh does not fail the library refresh`() = runTest {
            // ----- Arrange -----
            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)

            coEvery {
                refreshAllReadingJournalHistoryUseCase()
            } returns Result.failure(IllegalStateException("journal down"))

            // ----- Act -----
            val result = useCase(scope = RefreshScope.All)

            // ----- Assert -----
            result.isSuccess shouldBe true
        }
    }

    @Nested
    inner class AuthorDemographicsBackfill {
        @BeforeEach
        fun setUpUserId() {
            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)
            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                changedLists = emptyList(),
                serverListIds = emptySet(),
            )
        }

        @Test
        fun `an all-scope refresh marks the backfill`() = runTest {
            // ----- Arrange -----

            // ----- Act -----
            val result = useCase(scope = RefreshScope.All)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { settingsRepository.markAuthorDemographicsBackfilled() }
        }

        @Test
        fun `a statuses refresh containing read marks the backfill`() = runTest {
            // ----- Arrange -----
            val scope = RefreshScope.ByStatuses(
                statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.READ),
            )

            // ----- Act -----
            val result = useCase(scope = scope)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 1) { settingsRepository.markAuthorDemographicsBackfilled() }
        }

        @Test
        fun `a statuses refresh without read does not mark the backfill`() = runTest {
            // ----- Arrange -----
            val scope = RefreshScope.ByStatuses(
                statuses = setOf(UserBookStatus.CURRENTLY_READING, UserBookStatus.PAUSED),
            )

            // ----- Act -----
            val result = useCase(scope = scope)

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) { settingsRepository.markAuthorDemographicsBackfilled() }
        }

        @Test
        fun `a single-list refresh does not mark the backfill`() = runTest {
            // ----- Arrange -----
            coEvery {
                listsRepository.fetchUserLists(
                    userId = 55,
                    listIds = setOf(7),
                )
            } returns emptyList()

            // ----- Act -----
            val result = useCase(scope = RefreshScope.ByList(listId = 7))

            // ----- Assert -----
            result.isSuccess shouldBe true
            coVerify(exactly = 0) { settingsRepository.markAuthorDemographicsBackfilled() }
        }

        @Test
        fun `a failed read refresh does not mark the backfill`() = runTest {
            // ----- Arrange -----
            val scope = RefreshScope.ByStatuses(statuses = setOf(UserBookStatus.READ))
            coEvery {
                booksRepository.refreshUserBooks(
                    userId = 55,
                    statusFilter = setOf(UserBookStatus.READ),
                )
            } throws IllegalStateException("books refresh failed")

            // ----- Act -----
            val result = useCase(scope = scope)

            // ----- Assert -----
            result.isFailure shouldBe true
            coVerify(exactly = 0) { settingsRepository.markAuthorDemographicsBackfilled() }
        }

        @Test
        fun `a failed all-scope refresh does not mark the backfill`() = runTest {
            // ----- Arrange -----
            coEvery {
                booksRepository.refreshUserBooks(
                    userId = 55,
                    statusFilter = null,
                )
            } throws IllegalStateException("books refresh failed")

            // ----- Act -----
            val result = useCase(scope = RefreshScope.All)

            // ----- Assert -----
            result.isFailure shouldBe true
            coVerify(exactly = 0) { settingsRepository.markAuthorDemographicsBackfilled() }
        }
    }
}
