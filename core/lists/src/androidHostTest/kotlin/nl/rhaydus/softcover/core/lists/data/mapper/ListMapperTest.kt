package nl.rhaydus.softcover.core.lists.data.mapper

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.fragment.ListBookFragment
import nl.rhaydus.softcover.fragment.ListFragment
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ListMapperTest {
    private fun stubAggregate(
        count: Int = 0,
        maxUpdatedAt: String? = null,
    ): ListFragment.List_books_aggregate {
        val max: ListFragment.List_books_aggregate.Aggregate.Max? = if (maxUpdatedAt != null) {
            mockk {
                every {
                    updated_at
                } returns maxUpdatedAt
            }
        } else {
            null
        }

        val inner: ListFragment.List_books_aggregate.Aggregate = mockk {
            every {
                this@mockk.count
            } returns count
            every {
                this@mockk.max
            } returns max
        }

        return mockk {
            every {
                aggregate
            } returns inner
        }
    }

    private fun stubListFragment(
        id: Int = 20,
        name: String = "My List",
        slug: String? = "my-list",
        ranked: Boolean = false,
        privacySettingId: Int = PrivacySetting.PUBLIC.code,
        listBooks: List<ListFragment.List_book> = emptyList(),
        updatedAt: String? = null,
        listBooksAggregate: ListFragment.List_books_aggregate = stubAggregate(),
    ): ListFragment = mockk {
        every {
            this@mockk.id
        } returns id
        every {
            this@mockk.name
        } returns name
        every {
            this@mockk.slug
        } returns slug
        every {
            this@mockk.ranked
        } returns ranked
        every {
            privacy_setting_id
        } returns privacySettingId
        every {
            list_books
        } returns listBooks
        every {
            updated_at
        } returns updatedAt
        every {
            list_books_aggregate
        } returns listBooksAggregate
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
            every {
                this@mockk.id
            } returns id
            every {
                list_id
            } returns listId
            every {
                book_id
            } returns bookId
            every {
                edition_id
            } returns editionId
            every {
                this@mockk.position
            } returns position
            every {
                created_at
            } returns createdAt
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
        fun `maps ids, position and created_at, leaving book and edition unresolved`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(
                id = 99,
                listId = 20,
                bookId = 1,
                editionId = 10,
                position = 3,
                createdAt = "2024-03-15",
            )

            // ----- Act -----
            val result = fragment.toListBook().shouldNotBeNull()

            // ----- Assert -----
            result.listBookId shouldBe 99
            result.listId shouldBe 20
            result.bookId shouldBe 1
            result.editionId shouldBe 10
            result.position shouldBe 3
            result.addedAt shouldBe "2024-03-15"
            result.book shouldBe null
            result.edition shouldBe null
        }

        @Test
        fun `maps absent position and created_at to null`() {
            // ----- Arrange -----
            val fragment = stubListBookFragment(
                editionId = 10,
                position = null,
                createdAt = null,
            )

            // ----- Act -----
            val result = fragment.toListBook().shouldNotBeNull()

            // ----- Assert -----
            result.position shouldBe null
            result.addedAt shouldBe null
        }
    }

    @Nested
    inner class ListFragmentToBookList {
        @Test
        fun `populates signature from updatedAt count and maxListBookUpdatedAt`() {
            // ----- Arrange -----
            val fragment = stubListFragment(
                updatedAt = "2024-05-01",
                listBooksAggregate = stubAggregate(
                    count = 4,
                    maxUpdatedAt = "2024-04-30",
                ),
            )

            // ----- Act -----
            val result = fragment.toBookList()

            // ----- Assert -----
            result.signature shouldBe "2024-05-01|4|2024-04-30"
        }

        @Test
        fun `populates signature with count 0 when aggregate is absent`() {
            // ----- Arrange -----
            val noAggregate: ListFragment.List_books_aggregate = mockk {
                every {
                    aggregate
                } returns null
            }
            val fragment = stubListFragment(
                updatedAt = "2024-06-01",
                listBooksAggregate = noAggregate,
            )

            // ----- Act -----
            val result = fragment.toBookList()

            // ----- Assert -----
            result.signature shouldBe "2024-06-01|0|"
        }

        @Test
        fun `maps PRIVATE privacy_setting_id to PrivacySetting PRIVATE`() {
            // ----- Arrange -----
            val fragment = stubListFragment(privacySettingId = PrivacySetting.PRIVATE.code)

            // ----- Act -----
            val result = fragment.toBookList()

            // ----- Assert -----
            result.privacy shouldBe PrivacySetting.PRIVATE
        }

        @Test
        fun `degrades an unrecognized privacy_setting_id to PUBLIC`() {
            // ----- Arrange -----
            // privacy_setting_id is a non-null Int on the fragment, so an unmapped code (rather
            // than null) is the only way this branch can be reached.
            val fragment = stubListFragment(privacySettingId = -1)

            // ----- Act -----
            val result = fragment.toBookList()

            // ----- Assert -----
            result.privacy shouldBe PrivacySetting.PUBLIC
        }
    }

    // =========================================================
    // buildListSignature
    // =========================================================

    @Nested
    inner class BuildListSignature {
        @Test
        fun `formats all non-null values as pipe-separated string`() {
            // ----- Act -----
            val result = buildListSignature(
                updatedAt = "2024-01-01",
                count = 7,
                maxListBookUpdatedAt = "2023-12-31",
            )

            // ----- Assert -----
            result shouldBe "2024-01-01|7|2023-12-31"
        }

        @Test
        fun `uses empty string for null updatedAt`() {
            // ----- Act -----
            val result = buildListSignature(
                updatedAt = null,
                count = 3,
                maxListBookUpdatedAt = "2024-01-01",
            )

            // ----- Assert -----
            result shouldBe "|3|2024-01-01"
        }

        @Test
        fun `uses empty string for null maxListBookUpdatedAt`() {
            // ----- Act -----
            val result = buildListSignature(
                updatedAt = "2024-01-01",
                count = 3,
                maxListBookUpdatedAt = null,
            )

            // ----- Assert -----
            result shouldBe "2024-01-01|3|"
        }
    }
}
