package nl.rhaydus.softcover.feature.widget.presentation.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingPagingTest {
    @Nested
    inner class PageCount {
        @Test
        fun `an odd count rounds up to a partly filled last page`() {
            // ----- Arrange -----
            val itemCount = 5

            // ----- Act -----
            val result = pageCount(itemCount)

            // ----- Assert -----
            result shouldBe 3
        }

        @Test
        fun `books past the top ten do not add pages`() {
            // ----- Arrange -----
            val itemCount = 40

            // ----- Act -----
            val result = pageCount(itemCount)

            // ----- Assert -----
            result shouldBe 5
        }

        @Test
        fun `an empty list has no pages`() {
            // ----- Arrange -----
            val itemCount = 0

            // ----- Act -----
            val result = pageCount(itemCount)

            // ----- Assert -----
            result shouldBe 0
        }
    }

    @Nested
    inner class ResolveShownPage {
        @Test
        fun `a stored page past the end is pulled back to the last page`() {
            // ----- Arrange -----
            val stored = 7

            // ----- Act -----
            val result = resolveShownPage(
                stored = stored,
                pageCount = 3,
            )

            // ----- Assert -----
            result shouldBe 2
        }

        @Test
        fun `a stored page in range is kept`() {
            // ----- Arrange -----
            val stored = 1

            // ----- Act -----
            val result = resolveShownPage(
                stored = stored,
                pageCount = 3,
            )

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `an empty list resolves to page zero`() {
            // ----- Arrange -----
            val stored = 4

            // ----- Act -----
            val result = resolveShownPage(
                stored = stored,
                pageCount = 0,
            )

            // ----- Assert -----
            result shouldBe 0
        }
    }

    @Nested
    inner class NextPage {
        @Test
        fun `stepping forward from the last page wraps to the first`() {
            // ----- Arrange -----
            val stored = 2

            // ----- Act -----
            val result = nextPage(
                stored = stored,
                pageCount = 3,
                step = 1,
            )

            // ----- Assert -----
            result shouldBe 0
        }

        @Test
        fun `stepping back from the first page wraps to the last`() {
            // ----- Arrange -----
            val stored = 0

            // ----- Act -----
            val result = nextPage(
                stored = stored,
                pageCount = 3,
                step = -1,
            )

            // ----- Assert -----
            result shouldBe 2
        }

        @Test
        fun `a stale stored page steps from the clamped page`() {
            // ----- Arrange -----
            val stored = 9

            // ----- Act -----
            val result = nextPage(
                stored = stored,
                pageCount = 3,
                step = -1,
            )

            // ----- Assert -----
            result shouldBe 1
        }

        @Test
        fun `an empty list stays on page zero`() {
            // ----- Arrange -----
            val stored = 3

            // ----- Act -----
            val result = nextPage(
                stored = stored,
                pageCount = 0,
                step = 1,
            )

            // ----- Assert -----
            result shouldBe 0
        }
    }
}
