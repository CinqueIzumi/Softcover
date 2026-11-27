package nl.rhaydus.softcover.core.presentation.model

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.BookList
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryTabOrderTest {
    @Nested
    inner class GenericOrder {
        @Test
        fun `returns the items unchanged when the saved order is empty`() {
            // ----- Arrange -----
            val items = listOf("c", "a", "b")

            // ----- Act -----
            val result = items.inLibraryTabOrder(savedOrder = emptyList()) { it }

            // ----- Assert -----
            result shouldBe listOf("c", "a", "b")
        }

        @Test
        fun `places saved ids first in saved order`() {
            // ----- Arrange -----
            val items = listOf("a", "b", "c")

            // ----- Act -----
            val result = items.inLibraryTabOrder(savedOrder = listOf("c", "a", "b")) { it }

            // ----- Assert -----
            result shouldBe listOf("c", "a", "b")
        }

        @Test
        fun `keeps unsaved items in incoming order after the saved ones`() {
            // ----- Arrange -----
            val items = listOf("d", "a", "c", "b")

            // ----- Act -----
            val result = items.inLibraryTabOrder(savedOrder = listOf("b")) { it }

            // ----- Assert -----
            result shouldBe listOf("b", "d", "a", "c")
        }

        @Test
        fun `places an item once when the saved order repeats its id`() {
            // ----- Arrange -----
            val items = listOf("a", "b")

            // ----- Act -----
            val result = items.inLibraryTabOrder(savedOrder = listOf("b", "b", "a", "b")) { it }

            // ----- Assert -----
            result shouldBe listOf("b", "a")
        }

        @Test
        fun `skips saved ids that match no item`() {
            // ----- Arrange -----
            val items = listOf("a", "b")

            // ----- Act -----
            val result = items.inLibraryTabOrder(savedOrder = listOf("gone", "b", "missing")) { it }

            // ----- Assert -----
            result shouldBe listOf("b", "a")
        }
    }

    @Nested
    inner class BookListOrder {
        @Test
        fun `sorts lists absent from the saved order alphabetically ignoring case`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    1,
                    "cherry",
                ),
                list(
                    2,
                    "Banana",
                ),
                list(
                    3,
                    "apple",
                ),
            )

            // ----- Act -----
            val result = lists.inLibraryTabOrder(savedOrder = listOf("status-x"))

            // ----- Assert -----
            result.map { it.name } shouldBe listOf("apple", "Banana", "cherry")
        }

        @Test
        fun `lets the saved order win over the alphabetical one`() {
            // ----- Arrange -----
            val lists = listOf(
                list(
                    1,
                    "apple",
                ),
                list(
                    2,
                    "Banana",
                ),
                list(
                    3,
                    "cherry",
                ),
            )

            val saved = listOf(LibraryTab.CustomList.idFor(listId = 3))

            // ----- Act -----
            val result = lists.inLibraryTabOrder(savedOrder = saved)

            // ----- Assert -----
            result.map { it.name } shouldBe listOf("cherry", "apple", "Banana")
        }
    }

    private fun list(
        id: Int,
        name: String,
    ) = BookList(
        id = id,
        name = name,
        slug = name,
        books = emptyList(),
    )
}
