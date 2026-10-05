package nl.rhaydus.softcover.core.lists.data.datasource

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.dao.BookDao
import nl.rhaydus.softcover.core.database.mapper.toModel
import nl.rhaydus.softcover.core.database.model.BookListEntity
import nl.rhaydus.softcover.core.database.model.BookListWithBooks
import nl.rhaydus.softcover.core.database.model.ListSignatureRow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListsLocalDataSourceImplTest {
    private lateinit var dao: BookDao
    private lateinit var dataSource: ListsLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dao = mockk(relaxed = true)
        dataSource = ListsLocalDataSourceImpl(dao = dao)
    }

    private fun stubBookListWithBooks(id: Int = 1): BookListWithBooks = BookListWithBooks(
        bookList = BookListEntity(
            id = id,
            name = "My List $id",
            slug = "my-list-$id",
        ),
        listBooks = emptyList(),
    )

    @Nested
    inner class AllUserLists {
        @Test
        fun `emits mapped domain book lists from DAO observeBookLists`() = runTest {
            // ----- Arrange -----
            val entity = stubBookListWithBooks(id = 1)

            every {
                dao.observeBookLists()
            } returns flowOf(listOf(entity))

            // ----- Act & Assert -----
            dataSource.allUserLists.test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }

        @Test
        fun `suppresses consecutive duplicate emissions via distinctUntilChanged`() = runTest {
            // ----- Arrange -----
            val entity = stubBookListWithBooks(id = 1)
            val list = listOf(entity)

            every {
                dao.observeBookLists()
            } returns flowOf(
                list,
                list,
            )

            // ----- Act & Assert -----
            dataSource.allUserLists.test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class GetCachedListSignatures {
        @Test
        fun `maps rows to id-to-signature map`() = runTest {
            // ----- Arrange -----
            val rows = listOf(
                ListSignatureRow(
                    id = 1,
                    signature = "sig-a",
                ),
                ListSignatureRow(
                    id = 2,
                    signature = "sig-b",
                ),
            )

            coEvery {
                dao.getCachedListSignatures()
            } returns rows

            // ----- Act -----
            val result = dataSource.getCachedListSignatures()

            // ----- Assert -----
            result shouldBe mapOf(1 to "sig-a", 2 to "sig-b")
        }

        @Test
        fun `includes entries with null signature`() = runTest {
            // ----- Arrange -----
            val rows = listOf(
                ListSignatureRow(
                    id = 3,
                    signature = null,
                ),
            )

            coEvery {
                dao.getCachedListSignatures()
            } returns rows

            // ----- Act -----
            val result = dataSource.getCachedListSignatures()

            // ----- Assert -----
            result shouldBe mapOf(3 to null)
        }
    }
}
