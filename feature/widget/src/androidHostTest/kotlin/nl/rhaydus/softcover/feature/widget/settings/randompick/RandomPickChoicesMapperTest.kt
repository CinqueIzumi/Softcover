package nl.rhaydus.softcover.feature.widget.settings.randompick

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickSource
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
            result.chips.map { it.label } shouldContainExactly listOf("Want to Read", "Owned", "Apple", "banana")
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
        fun `a selected list that no longer exists marks want to read`() {
            // ----- Arrange -----
            val lists = emptyList<BookList>()

            // ----- Act -----
            val result = sourceChipsFor(
                selected = RandomPickSource.UserList(listId = 99),
                lists = lists,
            )

            // ----- Assert -----
            result.chips.filter { it.selected }.map { it.label } shouldContainExactly listOf("Want to Read")
            result.chips.size shouldBe 2
        }
    }
}
