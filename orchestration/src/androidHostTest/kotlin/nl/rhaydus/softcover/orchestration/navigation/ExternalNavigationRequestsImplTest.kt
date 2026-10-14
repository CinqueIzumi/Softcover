package nl.rhaydus.softcover.orchestration.navigation

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.presentation.navigation.ExternalNavigationRequest
import nl.rhaydus.softcover.core.presentation.navigation.TabDestination
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExternalNavigationRequestsImplTest {
    private val requests = ExternalNavigationRequestsImpl()

    @Nested
    inner class Request {
        @Test
        fun `a newer request replaces the pending one`() {
            // ----- Arrange -----
            val newer = ExternalNavigationRequest.OpenBook(bookId = 3)
            requests.request(request = ExternalNavigationRequest.OpenTab(destination = TabDestination.READING))

            // ----- Act -----
            requests.request(request = newer)

            // ----- Assert -----
            requests.pending.value shouldBe newer
        }
    }

    @Nested
    inner class Consume {
        @Test
        fun `consuming the pending request clears it`() {
            // ----- Arrange -----
            val request = ExternalNavigationRequest.OpenBook(bookId = 3)
            requests.request(request = request)

            // ----- Act -----
            requests.consume(request = request)

            // ----- Assert -----
            requests.pending.value.shouldBeNull()
        }

        @Test
        fun `consuming a stale request keeps the newer one`() {
            // ----- Arrange -----
            val stale = ExternalNavigationRequest.OpenBook(bookId = 3)
            val newer = ExternalNavigationRequest.OpenFocusMode
            requests.request(request = stale)
            requests.request(request = newer)

            // ----- Act -----
            requests.consume(request = stale)

            // ----- Assert -----
            requests.pending.value shouldBe newer
        }
    }
}
