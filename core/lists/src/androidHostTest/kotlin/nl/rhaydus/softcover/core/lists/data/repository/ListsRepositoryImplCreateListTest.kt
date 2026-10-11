package nl.rhaydus.softcover.core.lists.data.repository

import io.kotest.assertions.throwables.shouldThrow
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
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource
import nl.rhaydus.softcover.core.lists.domain.exception.ListNameTakenException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsRepositoryImplCreateListTest {
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

    private fun stubBookList(
        id: Int = 1,
        name: String = "My List",
    ): BookList = BookList(
        id = id,
        name = name,
        slug = name.lowercase().replace(
            " ",
            "-",
        ),
        books = emptyList(),
    )

    @Nested
    inner class CreateList {
        @Test
        fun `returns the created BookList and caches it as a single-element list`() = runTest {
            // ----- Arrange -----
            val name = "My List"
            val created = stubBookList(
                id = 7,
                name = name,
            )

            coEvery {
                listsRemoteDataSource.createList(name = name)
            } returns created

            // ----- Act -----
            val result = repository.createList(name = name)

            // ----- Assert -----
            result shouldBe created

            coVerify(exactly = 1) {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(created))
            }
        }

        @Test
        fun `on a retryable failure rethrows without enqueuing or caching`() = runTest {
            // ----- Arrange -----
            val name = "Retry Me"
            val error = OfflineException()

            coEvery {
                listsRemoteDataSource.createList(name = name)
            } throws error

            // ----- Act -----
            val thrown = shouldThrow<OfflineException> {
                repository.createList(name = name)
            }

            // ----- Assert -----
            thrown shouldBe error

            coVerify(exactly = 0) {
                listWriteQueue.enqueue(payload = any())
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheUserBookLists(lists = any())
            }
        }

        @Test
        fun `on a rejected failure rethrows without enqueuing or caching`() = runTest {
            // ----- Arrange -----
            val name = "Rejected"
            val error = RuntimeException("rejected")

            coEvery {
                listsRemoteDataSource.createList(name = name)
            } throws error

            // ----- Act -----
            val thrown = shouldThrow<RuntimeException> {
                repository.createList(name = name)
            }

            // ----- Assert -----
            thrown shouldBe error

            coVerify(exactly = 0) {
                listWriteQueue.enqueue(payload = any())
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheUserBookLists(lists = any())
            }
        }

        @Test
        fun `on ListNameTakenException rethrows without enqueuing`() = runTest {
            // ----- Arrange -----
            val name = "Taken Name"

            coEvery {
                listsRemoteDataSource.createList(name = name)
            } throws ListNameTakenException(name = name)

            // ----- Act & Assert -----
            shouldThrow<ListNameTakenException> {
                repository.createList(name = name)
            }

            coVerify(exactly = 0) {
                listWriteQueue.enqueue(payload = any())
            }
        }
    }

    @Nested
    inner class FetchUserLists {
        @Test
        fun `drains pending writes exactly once before calling remote fetchUserLists`() = runTest {
            // ----- Arrange -----
            val userId = 42

            coJustRun {
                listWriteDrainer.drain()
            }

            coEvery {
                listsRemoteDataSource.fetchUserLists(
                    userId = userId,
                    listIds = null,
                )
            } returns emptyList()

            // ----- Act -----
            repository.fetchUserLists(
                userId = userId,
                listIds = null,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                listWriteDrainer.drain()
            }

            coVerify(exactly = 1) {
                listsRemoteDataSource.fetchUserLists(
                    userId = userId,
                    listIds = null,
                )
            }
        }
    }
}
