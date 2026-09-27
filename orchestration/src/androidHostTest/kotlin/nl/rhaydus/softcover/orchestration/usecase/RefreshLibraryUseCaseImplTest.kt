package nl.rhaydus.softcover.orchestration.usecase

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.lists.domain.model.ListsRefreshResult
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RefreshLibraryUseCaseImplTest {
    private lateinit var getUserIdUseCase: GetUserIdUseCase
    private lateinit var booksRepository: BooksRepository
    private lateinit var listsRepository: ListsRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: RefreshLibraryUseCaseImpl

    @BeforeEach
    fun setUp() {
        getUserIdUseCase = mockk()
        booksRepository = mockk(relaxed = true)
        listsRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)

        every {
            settingsRepository.listDefaultsSeeded
        } returns flowOf(true)

        val dispatcher = UnconfinedTestDispatcher()

        useCase = RefreshLibraryUseCaseImpl(
            getUserIdUseCase = getUserIdUseCase,
            booksRepository = booksRepository,
            listsRepository = listsRepository,
            settingsRepository = settingsRepository,
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
        fun `returns success and calls both booksRepository and listsRepository`() = runTest {
            // ----- Arrange -----
            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                serverListIds = emptySet(),
                changedLists = emptyList(),
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
            }

            coVerify {
                listsRepository.refreshUserLists(userId = 55)
            }
        }

        @Test
        fun `calls hydrateReferencedBooks with union of all list book and edition ids`() = runTest {
            // ----- Arrange -----
            val listBook1 = stubListBook(
                bookId = 10,
                editionId = 100,
            )
            val listBook2 = stubListBook(
                bookId = 20,
                editionId = 200,
            )
            val changedList = stubBookList(books = listOf(listBook1, listBook2))

            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                serverListIds = emptySet(),
                changedLists = listOf(changedList),
            )

            // ----- Act -----
            useCase(scope = RefreshScope.All)

            // ----- Assert -----
            coVerify {
                booksRepository.hydrateReferencedBooks(
                    bookIds = match { it.containsAll(listOf(10, 20)) },
                    editionIds = match { it.containsAll(listOf(100, 200)) },
                    forceNetwork = true,
                )
            }
        }

        @Test
        fun `calls syncBookListMetadata with the full server list ids from the result — not just changed list ids`() = runTest {
            // ----- Arrange -----
            // changedList has id=7; server also knows about ids 8 and 9 (unchanged lists)
            val changedList = stubBookList(id = 7)

            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                serverListIds = setOf(7, 8, 9),
                changedLists = listOf(changedList),
            )

            // ----- Act -----
            useCase(scope = RefreshScope.All)

            // ----- Assert -----
            coVerify {
                listsRepository.syncBookListMetadata(serverListIds = setOf(7, 8, 9))
            }
        }

        @Test
        fun `calls cacheUserBookLists with the changed lists`() = runTest {
            // ----- Arrange -----
            val changedList = stubBookList(id = 3)

            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                serverListIds = setOf(3),
                changedLists = listOf(changedList),
            )

            // ----- Act -----
            useCase(scope = RefreshScope.All)

            // ----- Assert -----
            coVerify {
                listsRepository.cacheUserBookLists(lists = listOf(changedList))
            }
        }

        @Test
        fun `calls deleteOrphanBooks after cacheUserBookLists`() = runTest {
            // ----- Arrange -----
            val changedList = stubBookList()

            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                serverListIds = emptySet(),
                changedLists = listOf(changedList),
            )

            // ----- Act -----
            useCase(scope = RefreshScope.All)

            // ----- Assert -----
            coVerifyOrder {
                listsRepository.cacheUserBookLists(lists = any())
                booksRepository.deleteOrphanBooks()
            }
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

        @Test
        fun `syncs full server id set and runs delete even when no lists changed`() = runTest {
            // ----- Arrange -----
            coEvery {
                listsRepository.refreshUserLists(userId = 55)
            } returns ListsRefreshResult(
                serverListIds = setOf(1, 2),
                changedLists = emptyList(),
            )

            // ----- Act -----
            useCase(scope = RefreshScope.All)

            // ----- Assert -----
            coVerify {
                listsRepository.cacheUserBookLists(lists = emptyList())
            }

            coVerify {
                booksRepository.hydrateReferencedBooks(
                    bookIds = emptyList(),
                    editionIds = emptyList(),
                    forceNetwork = true,
                )
            }

            coVerify {
                listsRepository.syncBookListMetadata(serverListIds = setOf(1, 2))
            }

            coVerify {
                booksRepository.deleteOrphanBooks()
            }
        }
    }

    @Nested
    inner class ByStatusScope {
        @Test
        fun `calls refreshUserBooks with the given statusFilter and never touches lists`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.WANT_TO_READ

            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)

            // ----- Act -----
            val result = useCase(scope = RefreshScope.ByStatus(status = status))

            // ----- Assert -----
            result.isSuccess shouldBe true

            coVerify {
                booksRepository.refreshUserBooks(
                    userId = 55,
                    statusFilter = status,
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

        @Test
        fun `returns failure when booksRepository throws`() = runTest {
            // ----- Arrange -----
            val status = UserBookStatus.READ
            val expectedError = RuntimeException("books error")

            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)

            coEvery {
                booksRepository.refreshUserBooks(
                    userId = 55,
                    statusFilter = status,
                )
            } throws expectedError

            // ----- Act -----
            val result = useCase(scope = RefreshScope.ByStatus(status = status))

            // ----- Assert -----
            result.isFailure shouldBe true
            result.exceptionOrNull() shouldBe expectedError
        }
    }

    @Nested
    inner class ByListScope {
        @Test
        fun `fetches the single list by id and caches it without touching books refresh`() = runTest {
            // ----- Arrange -----
            val listId = 7
            val fetchedList = stubBookList(id = listId)

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
                listsRepository.fetchUserLists(
                    userId = 55,
                    listIds = setOf(listId),
                )
            }

            coVerify {
                listsRepository.cacheUserBookLists(lists = listOf(fetchedList))
            }

            coVerify(exactly = 0) {
                booksRepository.refreshUserBooks(
                    userId = any(),
                    statusFilter = any(),
                )
            }
        }

        @Test
        fun `calls hydrateReferencedBooks with ids from the fetched list`() = runTest {
            // ----- Arrange -----
            val listId = 7
            val listBook = stubListBook(
                bookId = 10,
                editionId = 100,
            )
            val fetchedList = stubBookList(
                id = listId,
                books = listOf(listBook),
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
            useCase(scope = RefreshScope.ByList(listId = listId))

            // ----- Assert -----
            coVerify {
                booksRepository.hydrateReferencedBooks(
                    bookIds = listOf(10),
                    editionIds = listOf(100),
                    forceNetwork = true,
                )
            }
        }

        @Test
        fun `does not call syncBookListMetadata or deleteOrphanBooks`() = runTest {
            // ----- Arrange -----
            val listId = 7

            coEvery {
                getUserIdUseCase()
            } returns Result.success(55)

            coEvery {
                listsRepository.fetchUserLists(
                    userId = 55,
                    listIds = setOf(listId),
                )
            } returns emptyList()

            // ----- Act -----
            useCase(scope = RefreshScope.ByList(listId = listId))

            // ----- Assert -----
            coVerify(exactly = 0) {
                listsRepository.syncBookListMetadata(serverListIds = any())
            }

            coVerify(exactly = 0) {
                booksRepository.deleteOrphanBooks()
            }
        }
    }
}
