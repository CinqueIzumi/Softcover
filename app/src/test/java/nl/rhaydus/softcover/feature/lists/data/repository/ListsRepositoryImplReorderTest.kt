package nl.rhaydus.softcover.feature.lists.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsRepositoryImplReorderTest {

    private lateinit var listsRemoteDataSource: ListsRemoteDataSource
    private lateinit var listsLocalDataSource: ListsLocalDataSource
    private lateinit var repository: ListsRepositoryImpl

    @BeforeEach
    fun setUp() {
        listsRemoteDataSource = mockk(relaxed = true)
        listsLocalDataSource = mockk(relaxed = true)

        repository = ListsRepositoryImpl(
            listsRemoteDataSource = listsRemoteDataSource,
            listsLocalDataSource = listsLocalDataSource,
            applicationScope = ApplicationScope(scope = CoroutineScope(UnconfinedTestDispatcher())),
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
    }
}
