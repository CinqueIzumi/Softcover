package nl.rhaydus.softcover.feature.widget.currentlyreading

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrentlyReadingSelectionTest {
    @Nested
    inner class ResolveShownBookId {
        @Test
        fun `shows the stored book when it is on the shelf but not first`() {
            // ----- Arrange -----
            val shelf = listOf(1, 2, 3)

            // ----- Act -----
            val result = resolveShownBookId(
                storedBookId = 3,
                orderedBookIds = shelf,
            )

            // ----- Assert -----
            result shouldBe 3
        }

        @Test
        fun `falls back to the first book when the stored book left the shelf`() {
            // ----- Arrange -----
            val shelf = listOf(1, 2, 3)

            // ----- Act -----
            val result = resolveShownBookId(
                storedBookId = 99,
                orderedBookIds = shelf,
            )

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `falls back to the first book when nothing is stored`() {
            // ----- Arrange -----
            val shelf = listOf(4, 5)

            // ----- Act -----
            val result = resolveShownBookId(
                storedBookId = null,
                orderedBookIds = shelf,
            )

            // ----- Assert -----
            result shouldBe 4
        }

        @Test
        fun `shows nothing for an empty shelf`() {
            // ----- Arrange -----
            val shelf = emptyList<Int>()

            // ----- Act -----
            val result = resolveShownBookId(
                storedBookId = 1,
                orderedBookIds = shelf,
            )

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class NextBookId {
        @Test
        fun `steps forward to the following book`() {
            // ----- Arrange -----
            val shelf = listOf(1, 2, 3)

            // ----- Act -----
            val result = nextBookId(
                storedBookId = 1,
                orderedBookIds = shelf,
                step = 1,
            )

            // ----- Assert -----
            result shouldBe 2
        }

        @Test
        fun `wraps from the last book to the first when stepping forward`() {
            // ----- Arrange -----
            val shelf = listOf(1, 2, 3)

            // ----- Act -----
            val result = nextBookId(
                storedBookId = 3,
                orderedBookIds = shelf,
                step = 1,
            )

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `wraps from the first book to the last when stepping back`() {
            // ----- Arrange -----
            val shelf = listOf(1, 2, 3)

            // ----- Act -----
            val result = nextBookId(
                storedBookId = 1,
                orderedBookIds = shelf,
                step = -1,
            )

            // ----- Assert -----
            result shouldBe 3
        }

        @Test
        fun `starts cycling from the first book when the stored one left the shelf`() {
            // ----- Arrange -----
            val shelf = listOf(1, 2, 3)

            // ----- Act -----
            val result = nextBookId(
                storedBookId = 99,
                orderedBookIds = shelf,
                step = 1,
            )

            // ----- Assert -----
            result shouldBe 2
        }

        @Test
        fun `stays on the only book of a single book shelf`() {
            // ----- Arrange -----
            val shelf = listOf(7)

            // ----- Act -----
            val result = nextBookId(
                storedBookId = 7,
                orderedBookIds = shelf,
                step = 1,
            )

            // ----- Assert -----
            result shouldBe 7
        }

        @Test
        fun `returns nothing for an empty shelf`() {
            // ----- Arrange -----
            val shelf = emptyList<Int>()

            // ----- Act -----
            val result = nextBookId(
                storedBookId = null,
                orderedBookIds = shelf,
                step = 1,
            )

            // ----- Assert -----
            result shouldBe null
        }
    }
}
