package nl.rhaydus.softcover.feature.explore.domain.usecase

import app.cash.turbine.test
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveOwnedBookIdsUseCaseTest {
    private lateinit var booksFlow: MutableStateFlow<List<Book>>
    private lateinit var listsFlow: MutableStateFlow<List<BookList>>
    private lateinit var useCase: ObserveOwnedBookIdsUseCase

    @BeforeEach
    fun setUp() {
        booksFlow = MutableStateFlow(emptyList())
        listsFlow = MutableStateFlow(emptyList())
        val booksRepository = mockk<BooksRepository>()
        val listsRepository = mockk<ListsRepository>()

        every {
            booksRepository.books
        } returns booksFlow

        every {
            listsRepository.allUserLists
        } returns listsFlow

        useCase = ObserveOwnedBookIdsUseCase(
            booksRepository = booksRepository,
            listsRepository = listsRepository,
        )
    }

    private fun shelved(id: Int): Book = mockk {
        every {
            this@mockk.id
        } returns id
    }

    private fun listOf(
        listId: Int,
        vararg bookIds: Int,
    ): BookList = BookList(
        id = listId,
        name = "List $listId",
        slug = "list-$listId",
        books = bookIds.map { bookId ->
            ListBook(
                listBookId = listId * 100 + bookId,
                listId = listId,
                bookId = bookId,
                editionId = 0,
            )
        },
    )

    @Nested
    inner class Invoke {
        @Test
        fun `unions shelved ids with the ids of every list`() = runTest {
            // ----- Arrange -----
            booksFlow.value = listOf(shelved(1), shelved(2))
            listsFlow.value = listOf(
                listOf(10, 2, 3),
                listOf(11, 4),
            )

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe setOf(1, 2, 3, 4)
            }
        }

        @Test
        fun `is empty with no shelves and no lists`() = runTest {
            // ----- Arrange -----
            // Empty flows from setUp.

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe emptySet()
            }
        }

        @Test
        fun `re-emits when a book is shelved or listed`() = runTest {
            // ----- Arrange -----
            booksFlow.value = listOf(shelved(1))

            // ----- Act & Assert -----
            useCase().test {
                awaitItem() shouldBe setOf(1)

                booksFlow.value = listOf(shelved(1), shelved(2))
                awaitItem() shouldBe setOf(1, 2)

                listsFlow.value = listOf(listOf(10, 9))
                awaitItem() shouldBe setOf(1, 2, 9)
            }
        }
    }
}
