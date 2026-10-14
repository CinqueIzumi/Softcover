package nl.rhaydus.softcover.orchestration.navigation

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ExternalNavigationCodecTest {
    @Nested
    inner class RoundTrip {
        @TestFactory
        fun `every tab destination survives a round trip`(): List<DynamicTest> =
            TabDestination.entries.map { destination ->
                DynamicTest.dynamicTest(destination.name) {
                    // ----- Arrange -----
                    val request = ExternalNavigationRequest.OpenTab(destination = destination)

                    // ----- Act -----
                    val parsed = parseExternalNavigationRequest(uri = request.toUriString())

                    // ----- Assert -----
                    parsed shouldBe request
                }
            }

        @Test
        fun `the focus mode request survives a round trip`() {
            // ----- Arrange -----
            val request = ExternalNavigationRequest.OpenFocusMode

            // ----- Act -----
            val parsed = parseExternalNavigationRequest(uri = request.toUriString())

            // ----- Assert -----
            parsed shouldBe request
        }
    }

    @Nested
    inner class WireFormat {
        @Test
        fun `a tab request keeps its persisted uri`() {
            // ----- Arrange -----
            val request = ExternalNavigationRequest.OpenTab(destination = TabDestination.LIBRARY)

            // ----- Act -----
            val uri = request.toUriString()

            // ----- Assert -----
            uri shouldBe "softcover://open/tab/library"
        }

        @Test
        fun `a book request keeps its persisted uri`() {
            // ----- Arrange -----
            val request = ExternalNavigationRequest.OpenBook(bookId = 42)

            // ----- Act -----
            val uri = request.toUriString()

            // ----- Assert -----
            uri shouldBe "softcover://open/book/42"
        }

        @Test
        fun `a persisted book uri still parses to the same request`() {
            // ----- Arrange -----
            val uri = "softcover://open/book/42"

            // ----- Act -----
            val parsed = parseExternalNavigationRequest(uri = uri)

            // ----- Assert -----
            parsed shouldBe ExternalNavigationRequest.OpenBook(bookId = 42)
        }
    }

    @Nested
    inner class Rejection {
        @TestFactory
        fun `malformed uris are rejected`(): List<DynamicTest> =
            mapOf(
                "wrong scheme" to "https://open/tab/library",
                "wrong host" to "softcover://close/tab/library",
                "no prefix" to "tab/library",
                "unknown tab name" to "softcover://open/tab/nowhere",
                "non-integer book id" to "softcover://open/book/abc",
                "tab with trailing segment" to "softcover://open/tab/library/extra",
                "book with trailing segment" to "softcover://open/book/42/extra",
                "focus with trailing segment" to "softcover://open/focus/extra",
                "tab without name" to "softcover://open/tab",
                "book without id" to "softcover://open/book",
                "no segments" to "softcover://open/",
                "unknown kind" to "softcover://open/shelf/1",
            ).map { (label, input) ->
                DynamicTest.dynamicTest(label) {
                    // ----- Arrange -----
                    val uri = input

                    // ----- Act -----
                    val parsed = parseExternalNavigationRequest(uri = uri)

                    // ----- Assert -----
                    parsed.shouldBeNull()
                }
            }
    }
}
