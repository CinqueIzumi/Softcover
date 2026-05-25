package nl.rhaydus.softcover.feature.lists.data.repository

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsLocalDataSource
import nl.rhaydus.softcover.feature.lists.data.datasource.ListsRemoteDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsRepositoryImplCreateListTest {

    private lateinit var listsRemoteDataSource: ListsRemoteDataSource
    private lateinit var listsLocalDataSource: ListsLocalDataSource
    private lateinit var repository: ListsRepositoryImpl

    @BeforeEach
    fun setUp() {
        listsRemoteDataSource = mockk()
        listsLocalDataSource = mockk(relaxed = true)

        repository = ListsRepositoryImpl(
            listsRemoteDataSource = listsRemoteDataSource,
            listsLocalDataSource = listsLocalDataSource,
            applicationScope = ApplicationScope(scope = CoroutineScope(UnconfinedTestDispatcher())),
        )
    }

    private fun stubBookList(id: Int = 1, name: String = "My List"): BookList = BookList(
        id = id,
        name = name,
        slug = name.lowercase().replace(" ", "-"),
        books = emptyList(),
    )

    @Nested
    inner class CreateList {

        @Test
        fun `returns the BookList produced by the remote data source`() = runTest {
            // ----- Arrange -----
            val name = "My List"
            val created = stubBookList(id = 7, name = name)

            coEvery {
                listsRemoteDataSource.createList(name = name)
            } returns created

            // ----- Act -----
            val result = repository.createList(name = name)

            // ----- Assert -----
            result shouldBe created
        }

        @Test
        fun `caches the created list as a single-element list`() = runTest {
            // ----- Arrange -----
            val name = "Favourites"
            val created = stubBookList(id = 3, name = name)

            coEvery {
                listsRemoteDataSource.createList(name = name)
            } returns created

            // ----- Act -----
            repository.createList(name = name)

            // ----- Assert -----
            coVerify(exactly = 1) {
                listsLocalDataSource.cacheUserBookLists(lists = listOf(created))
            }
        }

        @Test
        fun `propagates remote exception and does not cache`() = runTest {
            // ----- Arrange -----
            val name = "Bad List"
            val error = RuntimeException("API failure")

            coEvery {
                listsRemoteDataSource.createList(name = name)
            } throws error

            // ----- Act & Assert -----
            val thrown = shouldThrow<RuntimeException> {
                repository.createList(name = name)
            }

            thrown shouldBe error

            coVerify(exactly = 0) {
                listsLocalDataSource.cacheUserBookLists(lists = any())
            }
        }
    }
}
