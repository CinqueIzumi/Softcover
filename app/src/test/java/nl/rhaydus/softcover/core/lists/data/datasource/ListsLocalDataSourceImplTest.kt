package nl.rhaydus.softcover.core.lists.data.datasource

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.data.database.dao.BookDao
import nl.rhaydus.softcover.core.data.database.model.BookListEntity
import nl.rhaydus.softcover.core.data.database.model.BookListWithBooks
import nl.rhaydus.softcover.core.data.database.mapper.toModel
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
        fun `emits empty list when DAO emits empty list`() = runTest {
            // ----- Arrange -----
            every {
                dao.observeBookLists()
            } returns flowOf(emptyList())

            // ----- Act & Assert -----
            dataSource.allUserLists.test {
                awaitItem() shouldBe emptyList<BookList>()
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
            } returns flowOf(list, list)

            // ----- Act & Assert -----
            dataSource.allUserLists.test {
                awaitItem() shouldBe listOf(entity.toModel())
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ApplyListBookPositions {

        @Test
        fun `forwards to dao applyListBookPositionRange with correct arguments`() = runTest {
            // ----- Arrange -----
            val listId = 5
            val startPosition = 2
            val orderedIds = listOf(10, 20, 30)

            coJustRun {
                dao.applyListBookPositionRange(
                    listId = listId,
                    startPosition = startPosition,
                    listBookIds = orderedIds,
                )
            }

            // ----- Act -----
            dataSource.applyListBookPositions(
                listId = listId,
                startPosition = startPosition,
                orderedListBookIds = orderedIds,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                dao.applyListBookPositionRange(
                    listId = listId,
                    startPosition = startPosition,
                    listBookIds = orderedIds,
                )
            }
        }
    }

    @Nested
    inner class SetListRanked {

        @Test
        fun `forwards to dao setBookListRanked with correct arguments`() = runTest {
            // ----- Arrange -----
            val listId = 7
            val ranked = true

            // ----- Act -----
            dataSource.setListRanked(
                listId = listId,
                ranked = ranked,
            )

            // ----- Assert -----
            coVerify(exactly = 1) {
                dao.setBookListRanked(
                    listId = listId,
                    ranked = ranked,
                )
            }
        }
    }
}
