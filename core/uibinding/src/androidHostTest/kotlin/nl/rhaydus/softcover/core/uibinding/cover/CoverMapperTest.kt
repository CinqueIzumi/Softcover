package nl.rhaydus.softcover.core.uibinding.cover

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CoverMapperTest {
    private fun edition(
        id: Int = 1,
        url: String? = null,
    ): BookEdition = PreviewData.baseEdition.copy(
        id = id,
        url = url,
        localImagePath = null,
    )

    private fun book(
        defaultEdition: BookEdition?,
        coverUrl: String = "",
    ): Book = PreviewData.baseBook.copy(
        title = "Earthsea",
        editions = emptyList(),
        defaultEdition = defaultEdition,
        coverUrl = coverUrl,
        userBook = null,
    )

    @Nested
    inner class BookOverload {
        @Test
        fun `resolves the source from the book's current edition`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = edition(
                    id = 7,
                    url = "https://example.com/edition.jpg",
                ),
                coverUrl = "https://example.com/book.jpg",
            )

            // ----- Act -----
            val result = subject.toCoverUiModel(variant = CoverVariant.Shelf)

            // ----- Assert -----
            result.source shouldBe CoverSource.Remote(
                url = "https://example.com/edition.jpg",
                persistEditionId = 7,
            )
        }

        @Test
        fun `falls back to the book's coverUrl when no edition has a cover`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = edition(url = null),
                coverUrl = "https://example.com/book.jpg",
            )

            // ----- Act -----
            val result = subject.toCoverUiModel(variant = CoverVariant.Shelf)

            // ----- Assert -----
            result.source shouldBe CoverSource.Remote(
                url = "https://example.com/book.jpg",
                persistEditionId = null,
            )
        }

        @Test
        fun `titles the cover with the book's title for the coverless fallback`() {
            // ----- Arrange -----
            val subject = book(defaultEdition = null)

            // ----- Act -----
            val result = subject.toCoverUiModel(variant = CoverVariant.Shelf)

            // ----- Assert -----
            result.coverlessTitle shouldBe "Earthsea"
        }

        @Test
        fun `shareTransition gives the cover a key that is stable per book`() {
            // ----- Arrange -----
            val subject = book(defaultEdition = null)

            // ----- Act -----
            val first = subject.toCoverUiModel(
                variant = CoverVariant.Shelf,
                shareTransition = true,
            )
            val second = subject.toCoverUiModel(
                variant = CoverVariant.Shelf,
                shareTransition = true,
            )

            // ----- Assert -----
            first.sharedTransitionKey.shouldNotBeNull()
            first.sharedTransitionKey shouldBe second.sharedTransitionKey
        }

        @Test
        fun `the transition surface keeps one book on two rails distinct`() {
            // ----- Arrange -----
            val subject = book(defaultEdition = null)

            // ----- Act -----
            val rail = subject.toCoverUiModel(
                variant = CoverVariant.Shelf,
                shareTransition = true,
                transitionSurface = "rail",
            )
            val grid = subject.toCoverUiModel(
                variant = CoverVariant.Shelf,
                shareTransition = true,
                transitionSurface = "grid",
            )

            // ----- Assert -----
            rail.sharedTransitionKey.shouldNotBeNull()
            rail.sharedTransitionKey shouldNotBe grid.sharedTransitionKey
        }

        @Test
        fun `a transition surface without shareTransition adds no key`() {
            // ----- Arrange -----
            val subject = book(defaultEdition = null)

            // ----- Act -----
            val result = subject.toCoverUiModel(
                variant = CoverVariant.Shelf,
                transitionSurface = "rail",
            )

            // ----- Assert -----
            result.sharedTransitionKey.shouldBeNull()
        }
    }

    @Nested
    inner class UrlOverload {
        @Test
        fun `maps the url to a remote source titled and varianted as given`() {
            // ----- Act -----
            val result = coverUiModelFromUrl(
                url = "https://example.com/c.jpg",
                title = "Dune",
                variant = CoverVariant.Raised,
            )

            // ----- Assert -----
            result.source shouldBe CoverSource.Remote(
                url = "https://example.com/c.jpg",
                persistEditionId = null,
            )
            result.coverlessTitle shouldBe "Dune"
            result.variant shouldBe CoverVariant.Raised
        }

        @Test
        fun `a null url has no source but keeps the title for the coverless fallback`() {
            // ----- Act -----
            val result = coverUiModelFromUrl(
                url = null,
                title = "Dune",
                variant = CoverVariant.Shelf,
            )

            // ----- Assert -----
            result.source.shouldBeNull()
            result.coverlessTitle shouldBe "Dune"
        }
    }

    @Nested
    inner class EditionOverload {
        @Test
        fun `a null edition resolves from the default edition`() {
            // ----- Arrange -----
            val subject: BookEdition? = null
            val defaultEdition = edition(
                id = 3,
                url = "https://example.com/default.jpg",
            )

            // ----- Act -----
            val result = subject.toCoverUiModel(
                defaultEdition = defaultEdition,
                coverlessTitle = "Title",
                variant = CoverVariant.Shelf,
            )

            // ----- Assert -----
            result.source shouldBe CoverSource.Remote(
                url = "https://example.com/default.jpg",
                persistEditionId = 3,
            )
        }
    }
}
