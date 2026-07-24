package nl.rhaydus.softcover.core.network.helper

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class PaginationTest {
    @Test
    fun `returns all rows and stops after a single short page`() = runTest {
        // ----- Arrange -----
        val calls = mutableListOf<Pair<Int, Int>>()
        val shortPage = listOf("a", "b")

        // ----- Act -----
        val result = fetchAllPages(pageSize = 3) { limit, offset ->
            calls += limit to offset
            shortPage
        }

        // ----- Assert -----
        calls shouldBe listOf(3 to 0)
        result shouldBe shortPage
    }

    @Test
    fun `confirms end of pages with a trailing empty call when total is an exact multiple of pageSize`() = runTest {
        // ----- Arrange -----
        val calls = mutableListOf<Pair<Int, Int>>()
        val rows = (1..6).map { "row$it" }
        val pages = listOf(rows.subList(
            0,
            3,
        ), rows.subList(
            3,
            6,
        ), emptyList(),)
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
        val pages = listOf(rows.subList(
            0,
            3,
        ), rows.subList(
            3,
            6,
        ), rows.subList(
            6,
            7,
        ),)
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
}
