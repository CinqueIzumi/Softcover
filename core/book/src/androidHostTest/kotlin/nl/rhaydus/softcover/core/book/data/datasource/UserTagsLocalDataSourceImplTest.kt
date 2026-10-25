package nl.rhaydus.softcover.core.book.data.datasource

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.dao.BookUserTagDao
import nl.rhaydus.softcover.core.database.model.BookUserTagEntity
import nl.rhaydus.softcover.core.domain.model.TagCategory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserTagsLocalDataSourceImplTest {
    private lateinit var dao: BookUserTagDao
    private lateinit var dataSource: UserTagsLocalDataSourceImpl

    @BeforeEach
    fun setUp() {
        dao = mockk(relaxed = true)
        dataSource = UserTagsLocalDataSourceImpl(dao = dao)
    }

    private fun row(
        bookId: Int,
        category: TagCategory,
        name: String,
    ): BookUserTagEntity = BookUserTagEntity(
        bookId = bookId,
        category = category.name,
        name = name,
        spoiler = false,
    )

    private val shuffledRows = listOf(
        row(
            bookId = 1,
            category = TagCategory.OTHER,
            name = "Zeta",
        ),
        row(
            bookId = 1,
            category = TagCategory.TAG,
            name = "beta",
        ),
        row(
            bookId = 1,
            category = TagCategory.GENRE,
            name = "Fantasy",
        ),
        row(
            bookId = 1,
            category = TagCategory.CONTENT_WARNING,
            name = "Gore",
        ),
        row(
            bookId = 1,
            category = TagCategory.TAG,
            name = "Alpha",
        ),
        row(
            bookId = 1,
            category = TagCategory.MOOD,
            name = "Cozy",
        ),
    )

    private val expectedOrder = listOf("Fantasy", "Cozy", "Alpha", "beta", "Gore", "Zeta")

    @Nested
    inner class GetForBook {
        @Test
        fun `orders by category then by name ignoring case`() = runTest {
            // ----- Arrange -----
            coEvery {
                dao.getForBook(bookId = 1)
            } returns shuffledRows

            // ----- Act -----
            val result = dataSource.getForBook(bookId = 1)

            // ----- Assert -----
            result.map { it.name } shouldBe expectedOrder
        }
    }

    @Nested
    inner class ObserveForBook {
        @Test
        fun `emits the rows in the same order`() = runTest {
            // ----- Arrange -----
            every {
                dao.observeForBook(bookId = 1)
            } returns flowOf(shuffledRows)

            // ----- Act & Assert -----
            dataSource.observeForBook(bookId = 1).test {
                awaitItem().map { it.name } shouldBe expectedOrder
                awaitComplete()
            }
        }
    }

    @Nested
    inner class ObserveAll {
        @Test
        fun `groups rows by book and orders each book's tags`() = runTest {
            // ----- Arrange -----
            val otherBookRow = row(
                bookId = 2,
                category = TagCategory.MOOD,
                name = "Dark",
            )
            every {
                dao.observeAll()
            } returns flowOf(shuffledRows + otherBookRow)

            // ----- Act & Assert -----
            dataSource.observeAll().test {
                val grouped = awaitItem()

                grouped.keys shouldBe setOf(1, 2)
                grouped[1]?.map { it.name } shouldBe expectedOrder
                grouped[2]?.map { it.name } shouldBe listOf("Dark")
                awaitComplete()
            }
        }
    }
}
