package nl.rhaydus.softcover.core.uibinding.cover

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
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
        fun `is never loading`() {
            // ----- Arrange -----
            val subject = book(defaultEdition = null)

            // ----- Act -----
            val result = subject.toCoverUiModel(variant = CoverVariant.Shelf)

            // ----- Assert -----
            result.isLoading shouldBe false
        }

        @Test
        fun `carries the variant and the shared transition key through`() {
            // ----- Arrange -----
            val subject = book(defaultEdition = null)

            // ----- Act -----
            val result = subject.toCoverUiModel(
                variant = CoverVariant.Raised,
                sharedTransitionKey = "cover-1",
            )

            // ----- Assert -----
            result.variant shouldBe CoverVariant.Raised
            result.sharedTransitionKey shouldBe "cover-1"
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

        @Test
        fun `a null edition with no default and no fallback has no source`() {
            // ----- Arrange -----
            val subject: BookEdition? = null

            // ----- Act -----
            val result = subject.toCoverUiModel(
                defaultEdition = null,
                coverlessTitle = null,
                variant = CoverVariant.Shelf,
            )

            // ----- Assert -----
            result.source.shouldBeNull()
            result.coverlessTitle.shouldBeNull()
        }

        @Test
        fun `passes isLoading through`() {
            // ----- Arrange -----
            val subject: BookEdition? = null

            // ----- Act -----
            val loading = subject.toCoverUiModel(
                defaultEdition = null,
                coverlessTitle = null,
                variant = CoverVariant.Shelf,
                isLoading = true,
            )
            val idle = subject.toCoverUiModel(
                defaultEdition = null,
                coverlessTitle = null,
                variant = CoverVariant.Shelf,
            )

            // ----- Assert -----
            loading.isLoading shouldBe true
            idle.isLoading shouldBe false
        }
    }
}
