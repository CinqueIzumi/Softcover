package nl.rhaydus.softcover.core.presentation.navigation

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.TagCategory
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFilterRequestsTest {
    private val requests = LibraryFilterRequests()

    @Nested
    inner class Consume {
        @Test
        fun `consuming the pending request clears it`() {
            // ----- Arrange -----
            val request = LibraryTagFilterRequest(
                category = TagCategory.GENRE,
                name = "Fantasy",
            )

            requests.request(request = request)

            // ----- Act -----
            requests.consume(request = request)

            // ----- Assert -----
            requests.pending.value.shouldBeNull()
        }

        @Test
        fun `consuming an older request keeps the newer pending one`() {
            // ----- Arrange -----
            val older = LibraryTagFilterRequest(
                category = TagCategory.GENRE,
                name = "Fantasy",
            )

            val newer = LibraryTagFilterRequest(
                category = TagCategory.MOOD,
                name = "Cozy",
            )

            requests.request(request = older)
            requests.request(request = newer)

            // ----- Act -----
            requests.consume(request = older)

            // ----- Assert -----
            requests.pending.value shouldBe newer
        }
    }

    @Nested
    inner class Clear {
        @Test
        fun `clearing empties the pending request`() {
            // ----- Arrange -----
            requests.request(
                request = LibraryTagFilterRequest(
                    category = TagCategory.TAG,
                    name = "Favourite",
                ),
            )

            // ----- Act -----
            requests.clear()

            // ----- Assert -----
            requests.pending.value.shouldBeNull()
        }
    }
}
