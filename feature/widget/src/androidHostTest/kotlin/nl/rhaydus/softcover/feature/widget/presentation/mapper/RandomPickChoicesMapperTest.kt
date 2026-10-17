package nl.rhaydus.softcover.feature.widget.presentation.mapper

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.domain.model.RandomPickSource
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RandomPickChoicesMapperTest {
    private fun list(
        id: Int,
        name: String,
        slug: String = "list-$id",
    ): BookList = BookList(
        id = id,
        name = name,
        slug = slug,
        books = emptyList(),
    )

    @Nested
    inner class SourceChipsFor {
        @Test
        fun `the owned list is not repeated among the user list chips`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    id = 1,
                    name = "My owned books",
                    slug = "owned",
                ),
                list(
                    id = 2,
                    name = "Favourites",
                ),
            )

            // ----- Act -----
            val result = sourceChipsFor(
                selected = RandomPickSource.WantToRead,
                lists = lists,
            )

            // ----- Assert -----
            result.chips.map { it.label } shouldContainExactly listOf("Want to Read", "Owned", "Favourites")
            result.payloadByKey.values.toList() shouldContainExactly listOf(
                RandomPickSource.WantToRead,
                RandomPickSource.Owned,
                RandomPickSource.UserList(listId = 2),
            )
        }

        @Test
        fun `the owned chip is left out when there is no owned list`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    id = 2,
                    name = "Favourites",
                ),
            )

            // ----- Act -----
            val result = sourceChipsFor(
                selected = RandomPickSource.WantToRead,
                lists = lists,
            )

            // ----- Assert -----
            result.chips.map { it.label } shouldContainExactly listOf("Want to Read", "Favourites")
        }

        @Test
        fun `user lists are ordered by name ignoring case`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    id = 1,
                    name = "banana",
                ),
                list(
                    id = 2,
                    name = "Apple",
                ),
            )

            // ----- Act -----
            val result = sourceChipsFor(
                selected = RandomPickSource.WantToRead,
                lists = lists,
            )

            // ----- Assert -----
            result.chips.map { it.label } shouldContainExactly listOf("Want to Read", "Apple", "banana")
        }

        @Test
        fun `only the selected source is marked selected`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    id = 2,
                    name = "Favourites",
                ),
            )

            // ----- Act -----
            val result = sourceChipsFor(
                selected = RandomPickSource.UserList(listId = 2),
                lists = lists,
            )

            // ----- Assert -----
            result.chips.filter { it.selected }.map { it.label } shouldContainExactly listOf("Favourites")
        }

        @Test
        fun `a selected list that no longer exists marks no chip`() {
            // ----- Arrange -----
            val lists = emptyList<BookList>()

            // ----- Act -----
            val result = sourceChipsFor(
                selected = RandomPickSource.UserList(listId = 99),
                lists = lists,
            )

            // ----- Assert -----
            result.chips.filter { it.selected } shouldBe emptyList()
            result.chips.size shouldBe 1
        }
    }

    @Nested
    inner class SourceLabel {
        @Test
        fun `a user list that no longer exists reads as not on the device`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    id = 1,
                    name = "Favourites",
                ),
            )

            // ----- Act -----
            val result = RandomPickSource.UserList(listId = 99).label(lists)

            // ----- Assert -----
            result shouldBe "List not on your device"
        }

        @Test
        fun `a user list that exists reads as its own name`() {
            // ----- Arrange -----
            val favourites = list(
                id = 2,
                name = "Favourites",
            )
            val lists = listOf(
                list(
                    id = 1,
                    name = "Classics",
                ),
                favourites,
            )

            // ----- Act -----
            val result = RandomPickSource.UserList(listId = favourites.id).label(lists)

            // ----- Assert -----
            result shouldBe favourites.name
        }
    }
}
