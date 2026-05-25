package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.Tag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFiltersTest {

    // region Fixtures

    private val tagFiction = Tag(id = 1, name = "Fiction")
    private val tagScifi = Tag(id = 2, name = "Sci-Fi")

    private fun buildEdition(
        id: Int = 1,
        bookId: Int = 1,
        format: String = "paperback",
        owned: Boolean = false,
        releaseYear: Int = 2020,
    ) = BookEdition(
        id = id,
        canonicalId = null,
        bookId = bookId,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList(),
        releaseYear = releaseYear,
        releaseDate = null,
        format = format,
        owned = owned,
    )

    private fun buildBook(
        id: Int = 1,
        tags: List<Tag> = emptyList(),
        editions: List<BookEdition> = listOf(buildEdition()),
        rating: Double = 4.0,
        releaseYear: Int = 2020,
    ) = Book(
        id = id,
        canonicalId = null,
        title = "A Book",
        editions = editions,
        defaultEdition = null,
        rating = rating,
        description = "",
        releaseYear = releaseYear,
        releaseDate = null,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        tags = tags,
        userBook = null,
        userBookRead = null,
    )

    // endregion

    @Nested
    inner class IsEmpty {

        @Test
        fun `returns true when all facets are at their defaults`() {
            // ----- Arrange & Act -----
            val filters = LibraryFilters()

            // ----- Assert -----
            filters.isEmpty shouldBe true
        }

        @Test
        fun `returns false when tags is non-empty`() {
            // ----- Arrange & Act -----
            val filters = LibraryFilters(tags = setOf(tagFiction))

            // ----- Assert -----
            filters.isEmpty shouldBe false
        }

        @Test
        fun `returns false when formats is non-empty`() {
            // ----- Arrange & Act -----
            val filters = LibraryFilters(formats = setOf("ebook"))

            // ----- Assert -----
            filters.isEmpty shouldBe false
        }

        @Test
        fun `returns false when releaseYears is non-empty`() {
            // ----- Arrange & Act -----
            val filters = LibraryFilters(releaseYears = setOf(2021))

            // ----- Assert -----
            filters.isEmpty shouldBe false
        }

        @Test
        fun `returns false when owned is non-null`() {
            // ----- Arrange & Act -----
            val filters = LibraryFilters(owned = true)

            // ----- Assert -----
            filters.isEmpty shouldBe false
        }

        @Test
        fun `returns false when ratingMin is non-null`() {
            // ----- Arrange & Act -----
            val filters = LibraryFilters(ratingMin = 3.0)

            // ----- Assert -----
            filters.isEmpty shouldBe false
        }
    }

    @Nested
    inner class MatchesBook {

        @Nested
        inner class TagFacet {

            @Test
            fun `matches when book has at least one active tag`() {
                // ----- Arrange -----
                val book = buildBook(tags = listOf(tagFiction, tagScifi))
                val filters = LibraryFilters(tags = setOf(tagFiction))

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe true
            }

            @Test
            fun `rejects when book has none of the active tags`() {
                // ----- Arrange -----
                val book = buildBook(tags = listOf(tagScifi))
                val filters = LibraryFilters(tags = setOf(tagFiction))

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe false
            }

            @Test
            fun `passes when tags filter is empty`() {
                // ----- Arrange -----
                val book = buildBook(tags = emptyList())
                val filters = LibraryFilters(tags = emptySet())

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe true
            }
        }

        @Nested
        inner class FormatFacet {

            @Test
            fun `matches when any edition's format is in the filter set`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(format = "ebook")))
                val filters = LibraryFilters(formats = setOf("ebook"))

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe true
            }

            @Test
            fun `rejects when no edition's format is in the filter set`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(format = "paperback")))
                val filters = LibraryFilters(formats = setOf("ebook"))

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe false
            }
        }

        @Nested
        inner class ReleaseYearFacet {

            @Test
            fun `matches when book's releaseYear is in the filter set`() {
                // ----- Arrange -----
                val book = buildBook(releaseYear = 2021)
                val filters = LibraryFilters(releaseYears = setOf(2021))

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe true
            }

            @Test
            fun `rejects when book's releaseYear is not in the filter set`() {
                // ----- Arrange -----
                val book = buildBook(releaseYear = 2019)
                val filters = LibraryFilters(releaseYears = setOf(2021))

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe false
            }
        }

        @Nested
        inner class OwnedFacet {

            @Test
            fun `owned=true matches when at least one edition is owned`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(owned = true)))
                val filters = LibraryFilters(owned = true)

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe true
            }

            @Test
            fun `owned=true rejects when no edition is owned`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(owned = false)))
                val filters = LibraryFilters(owned = true)

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe false
            }

            @Test
            fun `owned=false matches when no edition is owned`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(owned = false)))
                val filters = LibraryFilters(owned = false)

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe true
            }

            @Test
            fun `owned=false rejects when any edition is owned`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(owned = true)))
                val filters = LibraryFilters(owned = false)

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe false
            }
        }

        @Nested
        inner class RatingMinFacet {

            @Test
            fun `matches when book rating equals the threshold`() {
                // ----- Arrange -----
                val book = buildBook(rating = 4.0)
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe true
            }

            @Test
            fun `matches when book rating exceeds the threshold`() {
                // ----- Arrange -----
                val book = buildBook(rating = 4.5)
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe true
            }

            @Test
            fun `rejects when book rating is below the threshold`() {
                // ----- Arrange -----
                val book = buildBook(rating = 3.5)
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe false
            }
        }

        @Nested
        inner class CombinedFacets {

            @Test
            fun `all facets must pass — rejects when only one facet fails`() {
                // ----- Arrange -----
                val book = buildBook(
                    tags = listOf(tagFiction),
                    editions = listOf(buildEdition(format = "paperback", owned = true)),
                    rating = 4.0,
                    releaseYear = 2020,
                )

                val filters = LibraryFilters(
                    tags = setOf(tagFiction),
                    formats = setOf("paperback"),
                    releaseYears = setOf(2020),
                    owned = true,
                    ratingMin = 4.5,
                )

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe false
            }

            @Test
            fun `all facets must pass — matches when every facet is satisfied`() {
                // ----- Arrange -----
                val book = buildBook(
                    tags = listOf(tagFiction),
                    editions = listOf(buildEdition(format = "paperback", owned = true)),
                    rating = 4.5,
                    releaseYear = 2020,
                )

                val filters = LibraryFilters(
                    tags = setOf(tagFiction),
                    formats = setOf("paperback"),
                    releaseYears = setOf(2020),
                    owned = true,
                    ratingMin = 4.5,
                )

                // ----- Act & Assert -----
                filters.matchesBook(book = book) shouldBe true
            }
        }
    }

    @Nested
    inner class MatchesEdition {

        @Nested
        inner class TagFacet {

            @Test
            fun `matches when parent book has at least one active tag`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(tags = listOf(tagFiction))
                val filters = LibraryFilters(tags = setOf(tagFiction))

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = book) shouldBe true
            }

            @Test
            fun `rejects when parent book has none of the active tags`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(tags = listOf(tagScifi))
                val filters = LibraryFilters(tags = setOf(tagFiction))

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = book) shouldBe false
            }

            @Test
            fun `rejects when book is null and tags filter is active`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val filters = LibraryFilters(tags = setOf(tagFiction))

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = null) shouldBe false
            }

            @Test
            fun `passes when tags filter is empty regardless of book`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val filters = LibraryFilters(tags = emptySet())

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = null) shouldBe true
            }
        }

        @Nested
        inner class FormatFacet {

            @Test
            fun `matches when edition format is in the filter set`() {
                // ----- Arrange -----
                val edition = buildEdition(format = "ebook")
                val filters = LibraryFilters(formats = setOf("ebook"))

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = null) shouldBe true
            }

            @Test
            fun `rejects when edition format is not in the filter set`() {
                // ----- Arrange -----
                val edition = buildEdition(format = "audiobook")
                val filters = LibraryFilters(formats = setOf("ebook"))

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = null) shouldBe false
            }
        }

        @Nested
        inner class ReleaseYearFacet {

            @Test
            fun `uses book releaseYear when book is supplied`() {
                // ----- Arrange -----
                val edition = buildEdition(releaseYear = 2019)
                val book = buildBook(releaseYear = 2021)
                val filters = LibraryFilters(releaseYears = setOf(2021))

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = book) shouldBe true
            }

            @Test
            fun `falls back to edition releaseYear when book is null`() {
                // ----- Arrange -----
                val edition = buildEdition(releaseYear = 2021)
                val filters = LibraryFilters(releaseYears = setOf(2021))

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = null) shouldBe true
            }

            @Test
            fun `rejects when edition releaseYear does not match filter and book is null`() {
                // ----- Arrange -----
                val edition = buildEdition(releaseYear = 2019)
                val filters = LibraryFilters(releaseYears = setOf(2021))

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = null) shouldBe false
            }
        }

        @Nested
        inner class OwnedFacet {

            @Test
            fun `owned=true matches when edition is owned`() {
                // ----- Arrange -----
                val edition = buildEdition(owned = true)
                val filters = LibraryFilters(owned = true)

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = null) shouldBe true
            }

            @Test
            fun `owned=true rejects when edition is not owned`() {
                // ----- Arrange -----
                val edition = buildEdition(owned = false)
                val filters = LibraryFilters(owned = true)

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = null) shouldBe false
            }

            @Test
            fun `owned=false rejects when edition is owned`() {
                // ----- Arrange -----
                val edition = buildEdition(owned = true)
                val filters = LibraryFilters(owned = false)

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = null) shouldBe false
            }
        }

        @Nested
        inner class RatingMinFacet {

            @Test
            fun `matches when book rating meets threshold`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(rating = 4.5)
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = book) shouldBe true
            }

            @Test
            fun `rejects when book rating is below threshold`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(rating = 3.0)
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = book) shouldBe false
            }

            @Test
            fun `rejects when book is null and ratingMin is active (treats rating as 0_0)`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = null) shouldBe false
            }
        }

        @Nested
        inner class CombinedFacets {

            @Test
            fun `rejects when one facet fails out of multiple active facets`() {
                // ----- Arrange -----
                val edition = buildEdition(
                    format = "paperback",
                    owned = true,
                    releaseYear = 2020,
                )
                val book = buildBook(
                    tags = listOf(tagFiction),
                    rating = 3.5,
                    releaseYear = 2020,
                )

                val filters = LibraryFilters(
                    tags = setOf(tagFiction),
                    formats = setOf("paperback"),
                    releaseYears = setOf(2020),
                    owned = true,
                    ratingMin = 4.0,
                )

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = book) shouldBe false
            }

            @Test
            fun `matches when all facets are satisfied`() {
                // ----- Arrange -----
                val edition = buildEdition(
                    format = "paperback",
                    owned = true,
                    releaseYear = 2020,
                )
                val book = buildBook(
                    tags = listOf(tagFiction),
                    rating = 4.5,
                    releaseYear = 2020,
                )

                val filters = LibraryFilters(
                    tags = setOf(tagFiction),
                    formats = setOf("paperback"),
                    releaseYears = setOf(2020),
                    owned = true,
                    ratingMin = 4.0,
                )

                // ----- Act & Assert -----
                filters.matchesEdition(edition = edition, book = book) shouldBe true
            }
        }
    }
}
