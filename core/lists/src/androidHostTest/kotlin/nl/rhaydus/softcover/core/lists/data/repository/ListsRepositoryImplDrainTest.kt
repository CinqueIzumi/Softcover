package nl.rhaydus.softcover.core.lists.data.repository

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
import nl.rhaydus.softcover.core.lists.data.datasource.ListsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsRepositoryImplDrainTest {
    private lateinit var listsRemoteDataSource: ListsRemoteDataSource
    private lateinit var listWriteDrainer: ListWriteDrainer
    private lateinit var repository: ListsRepositoryImpl

    @BeforeEach
    fun setUp() {
        listsRemoteDataSource = mockk()
        listWriteDrainer = mockk()

        repository = ListsRepositoryImpl(
            listsRemoteDataSource = listsRemoteDataSource,
            listsLocalDataSource = mockk(relaxed = true),
            booksRepository = mockk<BooksRepository>(),
            applicationScope = ApplicationScope(scope = CoroutineScope(UnconfinedTestDispatcher())),
            listWriteQueue = mockk<ListWriteQueue>(relaxed = true),
            listWriteDrainer = listWriteDrainer,
        )
    }

    @Nested
    inner class DrainPendingListWrites {
        @Test
        fun `drains the queue without fetching anything from the remote`() = runTest {
            // ----- Arrange -----
            coJustRun { listWriteDrainer.drain() }

            // ----- Act -----
            repository.drainPendingListWrites()

            // ----- Assert -----
            coVerify(exactly = 1) {
                listWriteDrainer.drain()
            }

            coVerify(exactly = 0) {
                listsRemoteDataSource.fetchListSignatures()
            }

            coVerify(exactly = 0) {
                listsRemoteDataSource.fetchUserLists(
                    userId = any(),
                    listIds = any(),
                )
            }
        }
    }
}
