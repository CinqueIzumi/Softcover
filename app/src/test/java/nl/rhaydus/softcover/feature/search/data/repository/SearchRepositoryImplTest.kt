package nl.rhaydus.softcover.feature.search.data.repository

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.search.data.datasource.SearchLocalDataSource
import nl.rhaydus.softcover.feature.search.data.datasource.SearchRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SearchRepositoryImplTest {

    private lateinit var searchRemoteDataSource: SearchRemoteDataSource
    private lateinit var searchLocalDataSource: SearchLocalDataSource
    private lateinit var repository: SearchRepositoryImpl

    @BeforeEach
    fun setUp() {
        searchRemoteDataSource = mockk(relaxed = true)
        searchLocalDataSource = mockk(relaxed = true)
        repository = SearchRepositoryImpl(
            searchRemoteDataSource = searchRemoteDataSource,
            searchLocalDataSource = searchLocalDataSource,
        )
    }

    private fun stubBook(): Book = mockk()

    @Nested
    inner class PreviousSearchQueries {

        @Test
        fun `previousSearchQueries is wired to local data source`() = runTest {
            // ----- Arrange -----
            val expectedQueries = listOf("kotlin", "android")

            every {
                searchLocalDataSource.previousSearchQueries
            } returns flowOf(expectedQueries)

            val freshRepository = SearchRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.previousSearchQueries.test {
                awaitItem() shouldBe expectedQueries
                awaitComplete()
            }
        }

        @Test
        fun `previousSearchQueries emits empty list when local data source emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                searchLocalDataSource.previousSearchQueries
            } returns flowOf(emptyList())

            val freshRepository = SearchRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.previousSearchQueries.test {
                awaitItem() shouldBe emptyList()
                awaitComplete()
            }
        }
    }

    @Nested
    inner class QueriedBooks {

        @Test
        fun `queriedBooks is wired to remote data source`() = runTest {
            // ----- Arrange -----
            val expectedBooks = listOf(stubBook())

            every {
                searchRemoteDataSource.queriedBooks
            } returns flowOf(expectedBooks)

            val freshRepository = SearchRepositoryImpl(
                searchRemoteDataSource = searchRemoteDataSource,
                searchLocalDataSource = searchLocalDataSource,
            )

            // ----- Act & Assert -----
            freshRepository.queriedBooks.test {
                awaitItem() shouldBe expectedBooks
                awaitComplete()
            }
        }
    }

    @Nested
    inner class SearchForName {

        @Test
        fun `delegates to remote data source with correct arguments`() = runTest {
            // ----- Arrange -----
            val name = "dune"
            val userId = 5

            // ----- Act -----
            repository.searchForName(name = name, userId = userId)

            // ----- Assert -----
            coVerify {
                searchRemoteDataSource.searchForName(name = name, userId = userId)
            }
        }
    }

    @Nested
    inner class SaveSearchQuery {

        @Test
        fun `delegates to local data source with correct name`() = runTest {
            // ----- Arrange -----
            val name = "foundation"

            // ----- Act -----
            repository.saveSearchQuery(name = name)

            // ----- Assert -----
            coVerify {
                searchLocalDataSource.saveSearchQuery(name = name)
            }
        }
    }

    @Nested
    inner class RemoveSearchQuery {

        @Test
        fun `delegates to local data source with correct name`() = runTest {
            // ----- Arrange -----
            val name = "neuromancer"

            // ----- Act -----
            repository.removeSearchQuery(name = name)

            // ----- Assert -----
            coVerify {
                searchLocalDataSource.removeSearchQuery(name = name)
            }
        }
    }

    @Nested
    inner class RemoveAllSearchQueries {

        @Test
        fun `delegates to local data source`() = runTest {
            // ----- Arrange -----
            // (searchLocalDataSource is relaxed — no additional setup needed)

            // ----- Act -----
            repository.removeAllSearchQueries()

            // ----- Assert -----
            coVerify {
                searchLocalDataSource.removeAllSearchQueries()
            }
        }
    }
}
