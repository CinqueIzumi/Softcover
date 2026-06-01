package nl.rhaydus.softcover.core.lists.data.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.fragment.ListBookFragment
import nl.rhaydus.softcover.fragment.ListFragment
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListMapperTest {

    private fun stubListFragment(
        id: Int = 20,
        name: String = "My List",
        slug: String? = "my-list",
        ranked: Boolean = false,
        listBooks: List<ListFragment.List_book> = emptyList(),
    ): ListFragment = mockk {
        every { this@mockk.id } returns id
        every { this@mockk.name } returns name
        every { this@mockk.slug } returns slug
        every { this@mockk.ranked } returns ranked
        every { list_books } returns listBooks
    }

    // =========================================================
    // GraphQL Fragment -> Model mappers
    // =========================================================

    @Nested
    inner class ListBookFragmentToListBook {

        private fun stubListBookFragment(
            id: Int = 99,
            listId: Int = 20,
            bookId: Int = 1,
            editionId: Int? = 10,
            position: Int? = null,
            createdAt: String? = null,
        ): ListBookFragment = mockk {
            every { this@mockk.id } returns id
            every { list_id } returns listId
            every { book_id } returns bookId
            every { edition_id } returns editionId
            every { this@mockk.position } returns position
            every { created_at } returns createdAt
        }

        @Test
        fun `returns null when edition_id is null`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(editionId = null)

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns ids-only ListBook when edition_id is present`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(
                id = 99,
                listId = 20,
                bookId = 1,
                editionId = 10,
            )

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result?.listBookId shouldBe 99
            result?.listId shouldBe 20
            result?.bookId shouldBe 1
            result?.editionId shouldBe 10
        }

        @Test
        fun `book and edition are null on the GraphQL path`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(editionId = 10)

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result?.book shouldBe null
            result?.edition shouldBe null
        }

        @Test
        fun `propagates created_at into addedAt`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(editionId = 10, createdAt = "2024-03-15")

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result?.addedAt shouldBe "2024-03-15"
        }

        @Test
        fun `propagates null created_at as null addedAt`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(editionId = 10, createdAt = null)

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result?.addedAt shouldBe null
        }

        @Test
        fun `propagates position when present`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(editionId = 10, position = 3)

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result?.position shouldBe 3
        }

        @Test
        fun `propagates null position as null`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(editionId = 10, position = null)

            // ----- Act -----
            val result = fragment.toListBook()

            // ----- Assert -----
            result?.position shouldBe null
        }
    }

    @Nested
    inner class ListFragmentToBookList {

        @Test
        fun `reads ranked=true from fragment`() {
            // ----- Arrange -----
            val fragment = stubListFragment(ranked = true)

            // ----- Act -----
            val result = fragment.toBookList()

            // ----- Assert -----
            result.ranked shouldBe true
        }

        @Test
        fun `reads ranked=false from fragment`() {
            // ----- Arrange -----
            val fragment = stubListFragment(ranked = false)

            // ----- Act -----
            val result = fragment.toBookList()

            // ----- Assert -----
            result.ranked shouldBe false
        }
    }
}
