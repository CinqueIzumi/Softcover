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
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingListWriteKind
import nl.rhaydus.softcover.core.domain.exception.OfflineException
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.coroutines.cancellation.CancellationException

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
        fun `applies the new order locally and sends it to Hardcover`() = runTest {
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
            coVerify(exactly = 1) {
                listsLocalDataSource.applyListBookPositions(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }

            coVerify(exactly = 1) {
                listsRemoteDataSource.updateListBookPositions(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }
        }

        @Test
        fun `on a retryable failure returns normally, keeps the local order and enqueues REORDER_LIST_BOOKS`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val startPosition = 2
            val orderedIds = listOf(10, 20, 30)

            coEvery {
                listsLocalDataSource.getAffectedListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } returns mapOf(
                10 to 4,
                20 to null,
            )

            coEvery {
                listsRemoteDataSource.updateListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } throws OfflineException()

            val slot = mutableListOf<PendingListWrite>()

            coJustRun {
                listWriteQueue.enqueue(capture(slot))
            }

            // ----- Act -----
            repository.reorderListBooks(
                listId = listId,
                startPosition = startPosition,
                orderedListBookIds = orderedIds,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                listsLocalDataSource.applyListBookPositions(
                    listId = listId,
                    startPosition = startPosition,
                    orderedListBookIds = orderedIds,
                )
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.restoreListBookPositions(positions = any())
            }

            coVerify(exactly = 1) {
                listWriteQueue.enqueue(payload = any())
            }

            val enqueued = slot.first()

            enqueued.kind shouldBe PendingListWriteKind.REORDER_LIST_BOOKS
            enqueued.listId shouldBe listId
            enqueued.startPosition shouldBe startPosition
            enqueued.orderedListBookIds shouldBe orderedIds
        }

        @Test
        fun `on a rejected failure rethrows and does not enqueue`() = runTest {
            // ----- Arrange -----
            val remoteError = RuntimeException("rejected")

            coEvery {
                listsLocalDataSource.getAffectedListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } returns mapOf(
                10 to 4,
                20 to null,
            )

            coEvery {
                listsRemoteDataSource.updateListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } throws remoteError

            // ----- Act -----
            val thrown = shouldThrow<RuntimeException> {
                repository.reorderListBooks(
                    listId = 5,
                    startPosition = 2,
                    orderedListBookIds = listOf(10, 20, 30),
                )
            }

            // ----- Assert -----
            thrown shouldBe remoteError

            coVerify(exactly = 1) {
                listsLocalDataSource.restoreListBookPositions(
                    positions = mapOf(
                        10 to 4,
                        20 to null,
                    ),
                )
            }

            coVerify(exactly = 0) {
                listWriteQueue.enqueue(payload = any())
            }
        }

        @Test
        fun `cancellation — rethrows CancellationException without enqueuing`() = runTest {
            // ----- Arrange -----
            coEvery {
                listsLocalDataSource.getAffectedListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } returns mapOf(
                10 to 4,
                20 to null,
            )

            coEvery {
                listsRemoteDataSource.updateListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } throws CancellationException("cancelled")

            // ----- Act & Assert -----
            shouldThrow<CancellationException> {
                repository.reorderListBooks(
                    listId = 5,
                    startPosition = 2,
                    orderedListBookIds = listOf(10, 20, 30),
                )
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.restoreListBookPositions(positions = any())
            }

            coVerify(exactly = 0) {
                listWriteQueue.enqueue(payload = any())
            }
        }

        @Test
        fun `when enqueueing a retryable failure also fails, restores the snapshotted positions and rethrows the queue error`() = runTest {
            // ----- Arrange -----
            val queueError = IllegalStateException("queue full")

            coEvery {
                listsLocalDataSource.getAffectedListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } returns mapOf(
                10 to 4,
                20 to null,
            )

            coEvery {
                listsRemoteDataSource.updateListBookPositions(
                    listId = any(),
                    startPosition = any(),
                    orderedListBookIds = any(),
                )
            } throws OfflineException()

            coEvery {
                listWriteQueue.enqueue(payload = any())
            } throws queueError

            // ----- Act -----
            val thrown = shouldThrow<IllegalStateException> {
                repository.reorderListBooks(
                    listId = 5,
                    startPosition = 2,
                    orderedListBookIds = listOf(10, 20, 30),
                )
            }

            // ----- Assert -----
            thrown shouldBe queueError

            coVerify(exactly = 1) {
                listsLocalDataSource.restoreListBookPositions(
                    positions = mapOf(
                        10 to 4,
                        20 to null,
                    ),
                )
            }
        }
    }
}
