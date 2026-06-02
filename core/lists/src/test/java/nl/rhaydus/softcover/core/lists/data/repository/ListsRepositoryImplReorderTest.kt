package nl.rhaydus.softcover.core.lists.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsRepositoryImplReorderTest {
    private lateinit var listsRemoteDataSource: ListsRemoteDataSource
    private lateinit var listsLocalDataSource: ListsLocalDataSource
    private val booksRepository = mockk<BooksRepository>()
    private lateinit var listWriteQueue: ListWriteQueue
    private lateinit var listWriteDrainer: ListWriteDrainer
    private lateinit var repository: ListsRepositoryImpl

    @BeforeEach
    fun setUp() {
        listsRemoteDataSource = mockk(relaxed = true)
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

    @Nested
    inner class ReorderListBooks {
        @Test
        fun `does nothing when orderedListBookIds is empty`() = runTest {
            // ----- Arrange -----
            // No stubs needed — neither data source should be called.

            // ----- Act -----
            repository.reorderListBooks(
                listId = 1,
                startPosition = 0,
                orderedListBookIds = emptyList(),
            )

            // ----- Assert -----
            coVerify(exactly = 0) {
                listsLocalDataSource.applyListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            }

            coVerify(exactly = 0) {
                listsRemoteDataSource.updateListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            }
        }

        @Test
        fun `calls local data source before remote data source`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val startPosition = 2
            val orderedIds = listOf(10, 20, 30)

            coJustRun {
                listsLocalDataSource.applyListBookPositions(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }

            coJustRun {
                listsRemoteDataSource.updateListBookPositions(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }

            // ----- Act -----
            repository.reorderListBooks(
                listId = listId,
                startPosition = startPosition,
                orderedListBookIds = orderedIds,
            )

            // ----- Assert -----
            coVerifyOrder {
                listsLocalDataSource.applyListBookPositions(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )

                listsRemoteDataSource.updateListBookPositions(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }
        }

        @Test
        fun `propagates remote failure after local write has already been applied`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val startPosition = 0
            val orderedIds = listOf(1, 2)
            val remoteError = RuntimeException("network failure")

            coJustRun {
                listsLocalDataSource.applyListBookPositions(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }

            coEvery {
                listsRemoteDataSource.updateListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } throws remoteError

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                repository.reorderListBooks(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }

            coVerify(exactly = 1) {
                listsLocalDataSource.applyListBookPositions(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }
        }

        @Test
        fun `on remote failure local positions are not rolled back and REORDER_LIST_BOOKS is enqueued`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val startPosition = 2
            val orderedIds = listOf(10, 20, 30)
            val remoteError = RuntimeException("network failure")

            coEvery {
                listsRemoteDataSource.updateListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } throws remoteError

            val slot = mutableListOf<PendingListWrite>()

            coJustRun {
                listWriteQueue.enqueue(capture(slot))
            }

            // ----- Act -----
            val thrown = shouldThrow<RuntimeException> {
                repository.reorderListBooks(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }

            // ----- Assert -----
            thrown shouldBe remoteError

            coVerify(exactly = 1) {
                listsLocalDataSource.applyListBookPositions(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }

            coVerify(exactly = 1) {
                listWriteQueue.enqueue(write = any())
            }

            val enqueued = slot.first()

            enqueued.kind shouldBe PendingListWriteKind.REORDER_LIST_BOOKS
            enqueued.listId shouldBe listId
            enqueued.startPosition shouldBe startPosition
            enqueued.orderedListBookIds shouldBe orderedIds
        }
    }
}
