package nl.rhaydus.softcover.core.designsystem.presentation.transition

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SharedElementScopesTest {
    @Nested
    inner class Elements {
        @Test
        fun `the three elements of one book get three distinct keys`() {
            // ----- Arrange -----
            val elements = BookTransitionElement.entries

            // ----- Act -----
            val keys = elements.map { element ->
                bookTransitionKey(
                    element = element,
                    editionId = 3,
                    bookId = 42,
                )
            }

            // ----- Assert -----
            keys.forEach { it.shouldNotBeNull() }
            keys.toSet().size shouldBe elements.size
        }

        @Test
        fun `the same element of the same book gets the same key every time`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Title

            // ----- Act -----
            val first = bookTransitionKey(
                element = element,
                editionId = 3,
                bookId = 42,
            )
            val second = bookTransitionKey(
                element = element,
                editionId = 3,
                bookId = 42,
            )

            // ----- Assert -----
            first.shouldNotBeNull()
            first shouldBe second
        }

        @Test
        fun `different books get different keys for the same element`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Rating

            // ----- Act -----
            val first = bookTransitionKey(
                element = element,
                editionId = null,
                bookId = 1,
            )
            val second = bookTransitionKey(
                element = element,
                editionId = null,
                bookId = 2,
            )

            // ----- Assert -----
            first.shouldNotBeNull()
            first shouldNotBe second
        }
    }

    @Nested
    inner class Identity {
        @Test
        fun `the bookId wins so the key is stable when the edition changes`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Title

            // ----- Act -----
            val withFirstEdition = bookTransitionKey(
                element = element,
                editionId = 1,
                bookId = 42,
            )
            val withSecondEdition = bookTransitionKey(
                element = element,
                editionId = 2,
                bookId = 42,
            )
            val withoutEdition = bookTransitionKey(
                element = element,
                editionId = null,
                bookId = 42,
            )

            // ----- Assert -----
            withFirstEdition.shouldNotBeNull()
            withFirstEdition shouldBe withSecondEdition
            withFirstEdition shouldBe withoutEdition
        }

        @Test
        fun `the editionId is the fallback when the bookId is null`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Cover

            // ----- Act -----
            val first = bookTransitionKey(
                element = element,
                editionId = 1,
                bookId = null,
            )
            val second = bookTransitionKey(
                element = element,
                editionId = 2,
                bookId = null,
            )

            // ----- Assert -----
            first.shouldNotBeNull()
            first shouldNotBe second
        }

        @Test
        fun `no ids yields no key`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Cover

            // ----- Act -----
            val key = bookTransitionKey(
                element = element,
                editionId = null,
                bookId = null,
                surface = "home",
            )

            // ----- Assert -----
            key.shouldBeNull()
        }
    }

    @Nested
    inner class Surface {
        @Test
        fun `different surfaces keep one book's key distinct`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Title

            // ----- Act -----
            val rail = bookTransitionKey(
                element = element,
                editionId = null,
                bookId = 42,
                surface = "rail",
            )
            val grid = bookTransitionKey(
                element = element,
                editionId = null,
                bookId = 42,
                surface = "grid",
            )

            // ----- Assert -----
            rail.shouldNotBeNull()
            rail shouldNotBe grid
        }

        @Test
        fun `a null surface differs from a non-null surface`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Rating

            // ----- Act -----
            val bare = bookTransitionKey(
                element = element,
                editionId = null,
                bookId = 42,
                surface = null,
            )
            val surfaced = bookTransitionKey(
                element = element,
                editionId = null,
                bookId = 42,
                surface = "rail",
            )

            // ----- Assert -----
            bare.shouldNotBeNull()
            bare shouldNotBe surfaced
        }

        @Test
        fun `the surface also applies to the edition fallback`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Cover

            // ----- Act -----
            val rail = bookTransitionKey(
                element = element,
                editionId = 5,
                bookId = null,
                surface = "rail",
            )
            val grid = bookTransitionKey(
                element = element,
                editionId = 5,
                bookId = null,
                surface = "grid",
            )

            // ----- Assert -----
            rail.shouldNotBeNull()
            rail shouldNotBe grid
        }
    }
}
