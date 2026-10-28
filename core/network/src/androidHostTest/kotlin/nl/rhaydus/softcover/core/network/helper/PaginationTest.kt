package nl.rhaydus.softcover.core.network.helper

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PaginationTest {
    @Test
    fun `confirms end of pages with a trailing empty call when total is an exact multiple of pageSize`() = runTest {
        // ----- Arrange -----
        val calls = mutableListOf<Pair<Int, Int>>()
        val rows = (1..6).map { "row$it" }
        val pages = rows.chunked(3) + listOf(emptyList())
        var callIndex = 0

        // ----- Act -----
        val result = fetchAllPages(pageSize = 3) { limit, offset ->
            calls += limit to offset
            pages[callIndex++]
        }

        // ----- Assert -----
        calls shouldBe listOf(3 to 0, 3 to 3, 3 to 6)
        result shouldBe rows
    }

    @Test
    fun `stops immediately on a short final page without a confirming empty call`() = runTest {
        // ----- Arrange -----
        val calls = mutableListOf<Pair<Int, Int>>()
        val rows = (1..7).map { "row$it" }
        val pages = rows.chunked(3)
        var callIndex = 0

        // ----- Act -----
        val result = fetchAllPages(pageSize = 3) { limit, offset ->
            calls += limit to offset
            pages[callIndex++]
        }

        // ----- Assert -----
        calls shouldBe listOf(3 to 0, 3 to 3, 3 to 6)
        result shouldBe rows
    }

    @Test
    fun `terminates via the maxPages guard when every page is full`() = runTest {
        // ----- Arrange -----
        var callCount = 0

        // ----- Act -----
        val result = fetchAllPages<String>(pageSize = 3, maxPages = 4) { _, _ ->
            callCount++
            listOf("x", "y", "z")
        }

        // ----- Assert -----
        callCount shouldBe 4
        result.size shouldBe 12
    }

    @Test
    fun `returns an empty list when the first page is empty`() = runTest {
        // ----- Arrange -----
        var callCount = 0

        // ----- Act -----
        val result = fetchAllPages<String>(pageSize = 3) { _, _ ->
            callCount++
            emptyList()
        }

        // ----- Assert -----
        callCount shouldBe 1
        result shouldBe emptyList<String>()
    }

    @Nested
    inner class FetchAllPagesOrThrow {
        @Test
        fun `accumulates every page and stops on the short one`() = runTest {
            // ----- Arrange -----
            val calls = mutableListOf<Pair<Int, Int>>()
            val rows = (1..7).map { "row$it" }
            val pages = rows.chunked(3)
            var callIndex = 0

            // ----- Act -----
            val result = fetchAllPagesOrThrow(pageSize = 3, maxPages = 10) { limit, offset ->
                calls += limit to offset
                pages[callIndex++]
            }

            // ----- Assert -----
            calls shouldBe listOf(3 to 0, 3 to 3, 3 to 6)
            result shouldBe rows
        }

        @Test
        fun `returns the rows when the confirming empty page lands exactly on the last allowed page`() = runTest {
            // ----- Arrange -----
            val pages = listOf(listOf("a", "b"), listOf("c", "d"), emptyList())
            var callIndex = 0

            // ----- Act -----
            val result = fetchAllPagesOrThrow(pageSize = 2, maxPages = 3) { _, _ ->
                pages[callIndex++]
            }

            // ----- Assert -----
            result shouldBe listOf("a", "b", "c", "d")
        }

        @Test
        fun `throws UnexpectedApiException when maxPages full pages never reach a short page`() = runTest {
            // ----- Arrange -----
            var callCount = 0

            // ----- Act & Assert -----
            shouldThrow<UnexpectedApiException> {
                fetchAllPagesOrThrow<String>(pageSize = 3, maxPages = 4) { _, _ ->
                    callCount++
                    listOf("x", "y", "z")
                }
            }
        }
    }

    @Nested
    inner class FetchAllPagesCounted {
        @Test
        fun `stops once the unique rows reach the total without an extra request`() = runTest {
            // ----- Arrange -----
            val calls = mutableListOf<Pair<Int, Int>>()
            val rows = (1..6).map { "row$it" }
            val pages = rows.chunked(3)
            var callIndex = 0

            // ----- Act -----
            val result = fetchAllPagesCounted(
                pageSize = 3,
                key = { row: String -> row },
            ) { limit, offset ->
                calls += limit to offset
                CountedPage(
                    rows = pages[callIndex++],
                    total = 6,
                )
            }

            // ----- Assert -----
            calls shouldBe listOf(3 to 0, 3 to 3)
            result shouldBe rows
        }

        @Test
        fun `fetches one more page when the total rises mid-fetch`() = runTest {
            // ----- Arrange -----
            val calls = mutableListOf<Pair<Int, Int>>()
            val pages = listOf(
                CountedPage(
                    rows = listOf("a", "b"),
                    total = 4,
                ),
                CountedPage(
                    rows = listOf("c", "d"),
                    total = 6,
                ),
                CountedPage(
                    rows = listOf("e", "f"),
                    total = 6,
                ),
            )
            var callIndex = 0

            // ----- Act -----
            val result = fetchAllPagesCounted(
                pageSize = 2,
                key = { row: String -> row },
            ) { limit, offset ->
                calls += limit to offset
                pages[callIndex++]
            }

            // ----- Assert -----
            calls shouldBe listOf(2 to 0, 2 to 2, 2 to 4)
            result shouldBe listOf("a", "b", "c", "d", "e", "f")
        }

        @Test
        fun `ends sooner when the total falls mid-fetch`() = runTest {
            // ----- Arrange -----
            val calls = mutableListOf<Pair<Int, Int>>()
            val pages = listOf(
                CountedPage(
                    rows = listOf("a", "b"),
                    total = 6,
                ),
                CountedPage(
                    rows = listOf("c", "d"),
                    total = 4,
                ),
            )
            var callIndex = 0

            // ----- Act -----
            val result = fetchAllPagesCounted(
                pageSize = 2,
                key = { row: String -> row },
            ) { limit, offset ->
                calls += limit to offset
                pages[callIndex++]
            }

            // ----- Assert -----
            calls shouldBe listOf(2 to 0, 2 to 2)
            result shouldBe listOf("a", "b", "c", "d")
        }

        @Test
        fun `throws UnexpectedApiException when a short page leaves unique rows below the total`() = runTest {
            // ----- Arrange -----
            val pages = listOf(
                CountedPage(
                    rows = listOf("a", "b"),
                    total = 5,
                ),
                CountedPage(
                    rows = listOf("c"),
                    total = 5,
                ),
            )
            var callIndex = 0

            // ----- Act & Assert -----
            shouldThrow<UnexpectedApiException> {
                fetchAllPagesCounted(
                    pageSize = 2,
                    key = { row: String -> row },
                ) { _, _ ->
                    pages[callIndex++]
                }
            }
        }

        @Test
        fun `throws UnexpectedApiException when a full page adds no new key`() = runTest {
            // ----- Arrange -----
            var callCount = 0

            // ----- Act & Assert -----
            shouldThrow<UnexpectedApiException> {
                fetchAllPagesCounted(
                    pageSize = 2,
                    key = { row: String -> row },
                ) { _, _ ->
                    callCount++
                    CountedPage(
                    rows = listOf("a", "b"),
                    total = 5,
                )
                }
            }
        }

        @Test
        fun `counts a key repeated across pages once and returns it once`() = runTest {
            // ----- Arrange -----
            val calls = mutableListOf<Pair<Int, Int>>()
            val pages = listOf(
                CountedPage(
                    rows = listOf("a", "b"),
                    total = 3,
                ),
                CountedPage(
                    rows = listOf("b", "c"),
                    total = 3,
                ),
            )
            var callIndex = 0

            // ----- Act -----
            val result = fetchAllPagesCounted(
                pageSize = 2,
                key = { row: String -> row },
            ) { limit, offset ->
                calls += limit to offset
                pages[callIndex++]
            }

            // ----- Assert -----
            calls shouldBe listOf(2 to 0, 2 to 2)
            result shouldBe listOf("a", "b", "c")
        }
    }
}
