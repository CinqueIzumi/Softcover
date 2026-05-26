package nl.rhaydus.softcover.feature.lists.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteDrainer
import nl.rhaydus.softcover.core.domain.connectivity.ListWriteQueue
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsRepositoryImplSetListRankedTest {

    private lateinit var listsRemoteDataSource: ListsRemoteDataSource
    private lateinit var listsLocalDataSource: ListsLocalDataSource
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
            applicationScope = ApplicationScope(scope = CoroutineScope(UnconfinedTestDispatcher())),
            listWriteQueue = listWriteQueue,
            listWriteDrainer = listWriteDrainer,
        )
    }

    private fun stubBookList(): BookList = mockk {
        every { this@mockk.id } returns 1
    }

    @Nested
    inner class SetListRanked {

        @Test
        fun `calls local then remote then caches the refreshed list on success`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val ranked = true
            val refreshed = stubBookList()

            coJustRun {
                listsLocalDataSource.setListRanked(
                    listId = listId,
                    ranked = ranked,
                )
            }

            coEvery {
                listsRemoteDataSource.setListRanked(
                    listId = listId,
                    ranked = ranked,
                )
            } returns refreshed

            coJustRun {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(refreshed))
            }

            // ----- Act -----
            repository.setListRanked(
                listId = listId,
                ranked = ranked,
            )

            // ----- Assert -----
            coVerifyOrder {
                listsLocalDataSource.setListRanked(
                    listId = listId,
                    ranked = ranked,
                )

                listsRemoteDataSource.setListRanked(
                    listId = listId,
                    ranked = ranked,
                )

                listsLocalDataSource.cacheUserBookLists(lists = listOf(refreshed))
            }
        }

        @Test
        fun `rolls back local with ranked inverted and rethrows when remote fails`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val ranked = true
            val remoteError = RuntimeException("network error")

            coJustRun {
                listsLocalDataSource.setListRanked(
                    listId = any(),
                    ranked = any(),
                )
            }

            coEvery {
                listsRemoteDataSource.setListRanked(
                    listId = listId,
                    ranked = ranked,
                )
            } throws remoteError

            // ----- Act & Assert -----
            shouldThrow<RuntimeException> {
                repository.setListRanked(
                    listId = listId,
                    ranked = ranked,
                )
            }

            coVerifyOrder {
                listsLocalDataSource.setListRanked(
                    listId = listId,
                    ranked = ranked,
                )

                listsLocalDataSource.setListRanked(
                    listId = listId,
                    ranked = ranked.not(),
                )
            }

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheUserBookLists(lists = any())
            }
        }
    }
}
