package nl.rhaydus.softcover.feature.explore.data.datasource

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.explore.data.datastore.SearchHistoryDataStore
import nl.rhaydus.softcover.feature.explore.data.datastore.serializer.SearchHistoryEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SearchLocalDataSourceImplTest {
    private lateinit var storeDouble: DataStore<SearchHistoryEntity>
    private lateinit var dataSource: SearchLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        storeDouble = mockk(relaxed = true)
        dataSource = SearchLocalDataSourceImpl(dataStore = SearchHistoryDataStore(store = storeDouble))
    }

    private fun stubDataStore(entity: SearchHistoryEntity) {
        every {
            storeDouble.data
        } returns flowOf(entity)
    }

    @Nested
    inner class PreviousSearchQueries {
        @Test
        fun `emits list of previous queries from datastore`() = runTest {
            // ----- Arrange -----
            val queries = listOf("kotlin", "android")
            stubDataStore(SearchHistoryEntity(previousQueries = queries))
            val freshDataSource = SearchLocalDataSourceImpl(dataStore = SearchHistoryDataStore(store = storeDouble))

            // ----- Act & Assert -----
            freshDataSource.previousSearchQueries.test {
                awaitItem() shouldBe queries
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SaveSearchQuery {
        @Test
        fun `prepends new query to front of list`() = runTest {
            // ----- Arrange -----
            val transformSlot = slot<suspend (SearchHistoryEntity) -> SearchHistoryEntity>()

            coEvery {
                storeDouble.updateData(capture(transformSlot))
            } coAnswers {
                transformSlot.captured(SearchHistoryEntity(previousQueries = listOf("existing")))
            }

            // ----- Act -----
            dataSource.saveSearchQuery(name = "new")

            // ----- Assert -----
            val result = transformSlot.captured(SearchHistoryEntity(previousQueries = listOf("existing")))
            result.previousQueries.first() shouldBe "new"
        }

        @Test
        fun `moves duplicate query to front of list instead of adding a second entry`() = runTest {
            // ----- Arrange -----
            val transformSlot = slot<suspend (SearchHistoryEntity) -> SearchHistoryEntity>()

            coEvery {
                storeDouble.updateData(capture(transformSlot))
            } coAnswers {
                transformSlot.captured(SearchHistoryEntity(previousQueries = listOf("kotlin", "android")))
            }

            // ----- Act -----
            dataSource.saveSearchQuery(name = "android")

            // ----- Assert -----
            val result = transformSlot.captured(SearchHistoryEntity(previousQueries = listOf("kotlin", "android")))
            result.previousQueries shouldBe listOf("android", "kotlin")
        }

        @Test
        fun `caps list at 10 entries when exceeding max size`() = runTest {
            // ----- Arrange -----
            val tenQueries = (1..10).map { "query$it" }
            val transformSlot = slot<suspend (SearchHistoryEntity) -> SearchHistoryEntity>()

            coEvery {
                storeDouble.updateData(capture(transformSlot))
            } coAnswers {
                transformSlot.captured(SearchHistoryEntity(previousQueries = tenQueries))
            }

            // ----- Act -----
            dataSource.saveSearchQuery(name = "queryNew")

            // ----- Assert -----
            val result = transformSlot.captured(SearchHistoryEntity(previousQueries = tenQueries))
            result.previousQueries.size shouldBe 10
            result.previousQueries.first() shouldBe "queryNew"
        }
    }

    @Nested
    inner class RemoveSearchQuery {
        @Test
        fun `removes the specified query from the list`() = runTest {
            // ----- Arrange -----
            val transformSlot = slot<suspend (SearchHistoryEntity) -> SearchHistoryEntity>()

            coEvery {
                storeDouble.updateData(capture(transformSlot))
            } coAnswers {
                transformSlot.captured(SearchHistoryEntity(previousQueries = listOf("kotlin", "android", "compose")))
            }

            // ----- Act -----
            dataSource.removeSearchQuery(name = "android")

            // ----- Assert -----
            val result = transformSlot.captured(
                SearchHistoryEntity(previousQueries = listOf("kotlin", "android", "compose")),
            )
            result.previousQueries shouldBe listOf("kotlin", "compose")
        }
    }

    @Nested
    inner class RemoveAllSearchQueries {
        @Test
        fun `clears all queries from the list`() = runTest {
            // ----- Arrange -----
            val transformSlot = slot<suspend (SearchHistoryEntity) -> SearchHistoryEntity>()

            coEvery {
                storeDouble.updateData(capture(transformSlot))
            } coAnswers {
                transformSlot.captured(SearchHistoryEntity(previousQueries = listOf("a", "b", "c")))
            }

            // ----- Act -----
            dataSource.removeAllSearchQueries()

            // ----- Assert -----
            val result = transformSlot.captured(SearchHistoryEntity(previousQueries = listOf("a", "b", "c")))
            result.previousQueries shouldBe emptyList()
        }
    }
}
