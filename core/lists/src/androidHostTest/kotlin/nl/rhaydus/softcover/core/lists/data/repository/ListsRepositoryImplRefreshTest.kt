package nl.rhaydus.softcover.core.lists.data.repository

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource
import nl.rhaydus.softcover.core.lists.domain.model.ListSignature
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsRepositoryImplRefreshTest {
    private lateinit var listsRemoteDataSource: ListsRemoteDataSource
    private lateinit var listsLocalDataSource: ListsLocalDataSource
    private val booksRepository = mockk<BooksRepository>()
    private lateinit var listWriteQueue: ListWriteQueue
    private lateinit var listWriteDrainer: ListWriteDrainer
    private lateinit var repository: ListsRepositoryImpl

    @BeforeEach
    fun setUp() {
        listsRemoteDataSource = mockk()
        listsLocalDataSource = mockk(relaxed = true)
        listWriteQueue = mockk(relaxed = true)
        listWriteDrainer = mockk(relaxed = true)

        repository = ListsRepositoryImpl(
            listsRemoteDataSource = listsRemoteDataSource,
            listsLocalDataSource = listsLocalDataSource,
            booksRepository = booksRepository,
            applicationScope = ApplicationScope(scope = CoroutineScope(UnconfinedTestDispatcher())),
            listWriteQueue = listWriteQueue,
            listWriteDrainer = listWriteDrainer,
        )
    }

    private fun stubBookList(id: Int = 1): BookList = mockk {
        io.mockk.every { this@mockk.id } returns id
    }

    @Nested
    inner class RefreshUserLists {
        @Test
        fun `drains pending writes before fetching signatures`() = runTest {
            // ----- Arrange -----
            val userId = 10

            coJustRun { listWriteDrainer.drainPendingWrites() }

            coEvery {
                listsRemoteDataSource.fetchListSignatures()
            } returns emptyList()

            coEvery {
                listsLocalDataSource.getCachedListSignatures()
            } returns emptyMap()

            // ----- Act -----
            repository.refreshUserLists(userId = userId)

            // ----- Assert -----
            coVerify(exactly = 1) {
                listWriteDrainer.drainPendingWrites()
            }
        }

        @Test
        fun `when all signatures match cache fetchUserLists is never called`() = runTest {
            // ----- Arrange -----
            val userId = 5
            val signatures = listOf(
                ListSignature(
                    listId = 1,
                    signature = "sig-a",
                ),
                ListSignature(
                    listId = 2,
                    signature = "sig-b",
                ),
            )

            coEvery {
                listsRemoteDataSource.fetchListSignatures()
            } returns signatures

            coEvery {
                listsLocalDataSource.getCachedListSignatures()
            } returns mapOf(1 to "sig-a", 2 to "sig-b")

            // ----- Act -----
            val result = repository.refreshUserLists(userId = userId)

            // ----- Assert -----
            coVerify(exactly = 0) {
                listsRemoteDataSource.fetchUserLists(
                    userId = any(),
                    listIds = any(),
                )
            }

            result.changedLists shouldBe emptyList()
            result.serverListIds shouldBe setOf(1, 2)
        }

        @Test
        fun `when some signatures differ fetchUserLists is called with exactly the stale ids`() = runTest {
            // ----- Arrange -----
            val userId = 7
            val changedList = stubBookList(id = 2)
            val signatures = listOf(
                ListSignature(
                    listId = 1,
                    signature = "sig-a",
                ),
                ListSignature(
                    listId = 2,
                    signature = "sig-b-new",
                ),
            )

            coEvery {
                listsRemoteDataSource.fetchListSignatures()
            } returns signatures

            coEvery {
                listsLocalDataSource.getCachedListSignatures()
            } returns mapOf(1 to "sig-a", 2 to "sig-b-old")

            coEvery {
                listsRemoteDataSource.fetchUserLists(
                    userId = userId,
                    listIds = setOf(2),
                )
            } returns listOf(changedList)

            // ----- Act -----
            val result = repository.refreshUserLists(userId = userId)

            // ----- Assert -----
            coVerify(exactly = 1) {
                listsRemoteDataSource.fetchUserLists(
                    userId = userId,
                    listIds = setOf(2),
                )
            }

            result.changedLists shouldBe listOf(changedList)
            result.serverListIds shouldBe setOf(1, 2)
        }

        @Test
        fun `a list with different cached signature is treated as stale`() = runTest {
            // ----- Arrange -----
            val userId = 3
            val changedList = stubBookList(id = 1)
            val signatures = listOf(
                ListSignature(
                    listId = 1,
                    signature = "new-sig",
                ),
            )

            coEvery {
                listsRemoteDataSource.fetchListSignatures()
            } returns signatures

            coEvery {
                listsLocalDataSource.getCachedListSignatures()
            } returns mapOf(1 to "old-sig")

            coEvery {
                listsRemoteDataSource.fetchUserLists(
                    userId = userId,
                    listIds = setOf(1),
                )
            } returns listOf(changedList)

            // ----- Act -----
            val result = repository.refreshUserLists(userId = userId)

            // ----- Assert -----
            result.changedLists shouldBe listOf(changedList)
        }

        @Test
        fun `a list absent from cache is treated as stale`() = runTest {
            // ----- Arrange -----
            val userId = 4
            val newList = stubBookList(id = 99)
            val signatures = listOf(
                ListSignature(
                    listId = 99,
                    signature = "sig-x",
                ),
            )

            coEvery {
                listsRemoteDataSource.fetchListSignatures()
            } returns signatures

            coEvery {
                listsLocalDataSource.getCachedListSignatures()
            } returns emptyMap()

            coEvery {
                listsRemoteDataSource.fetchUserLists(
                    userId = userId,
                    listIds = setOf(99),
                )
            } returns listOf(newList)

            // ----- Act -----
            val result = repository.refreshUserLists(userId = userId)

            // ----- Assert -----
            result.changedLists shouldBe listOf(newList)
            result.serverListIds shouldBe setOf(99)
        }

        @Test
        fun `a list with null cached signature is treated as stale`() = runTest {
            // ----- Arrange -----
            val userId = 6
            val list = stubBookList(id = 5)
            val signatures = listOf(
                ListSignature(
                    listId = 5,
                    signature = "sig-new",
                ),
            )

            coEvery {
                listsRemoteDataSource.fetchListSignatures()
            } returns signatures

            coEvery {
                listsLocalDataSource.getCachedListSignatures()
            } returns mapOf(5 to null)

            coEvery {
                listsRemoteDataSource.fetchUserLists(
                    userId = userId,
                    listIds = setOf(5),
                )
            } returns listOf(list)

            // ----- Act -----
            val result = repository.refreshUserLists(userId = userId)

            // ----- Assert -----
            result.changedLists shouldBe listOf(list)
        }

        @Test
        fun `a list whose signature equals cached value is not stale`() = runTest {
            // ----- Arrange -----
            val userId = 8
            val signatures = listOf(
                ListSignature(
                    listId = 10,
                    signature = "stable-sig",
                ),
                ListSignature(
                    listId = 11,
                    signature = "changed-sig",
                ),
            )

            coEvery {
                listsRemoteDataSource.fetchListSignatures()
            } returns signatures

            coEvery {
                listsLocalDataSource.getCachedListSignatures()
            } returns mapOf(10 to "stable-sig", 11 to "old-sig")

            coEvery {
                listsRemoteDataSource.fetchUserLists(
                    userId = userId,
                    listIds = setOf(11),
                )
            } returns emptyList()

            // ----- Act -----
            repository.refreshUserLists(userId = userId)

            // ----- Assert -----
            coVerify(exactly = 1) {
                listsRemoteDataSource.fetchUserLists(
                    userId = userId,
                    listIds = setOf(11),
                )
            }
        }

        @Test
        fun `serverListIds equals the set of all list ids from signatures`() = runTest {
            // ----- Arrange -----
            val userId = 9
            val signatures = listOf(
                ListSignature(
                    listId = 1,
                    signature = "a",
                ),
                ListSignature(
                    listId = 2,
                    signature = "b",
                ),
                ListSignature(
                    listId = 3,
                    signature = "c",
                ),
            )

            coEvery {
                listsRemoteDataSource.fetchListSignatures()
            } returns signatures

            coEvery {
                listsLocalDataSource.getCachedListSignatures()
            } returns mapOf(1 to "a", 2 to "b", 3 to "c")

            // ----- Act -----
            val result = repository.refreshUserLists(userId = userId)

            // ----- Assert -----
            result.serverListIds shouldBe setOf(1, 2, 3)
        }
    }
}
