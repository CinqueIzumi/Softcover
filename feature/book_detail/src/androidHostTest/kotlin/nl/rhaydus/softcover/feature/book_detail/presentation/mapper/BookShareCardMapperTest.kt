package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.share.ShareCardFormat
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BookShareCardMapperTest {
    private fun stubEdition(
        localImagePath: String? = null,
        url: String? = null,
        releaseYear: Int = 2021,
        pages: Int? = 320,
    ): BookEdition = mockk {
        every {
            this@mockk.id
        } returns 7

        every {
            this@mockk.localImagePath
        } returns localImagePath

        every {
            this@mockk.url
        } returns url

        every {
            this@mockk.releaseYear
        } returns releaseYear

        every {
            this@mockk.pages
        } returns pages
    }

    private fun stubUserBook(rating: Double?): UserBook = mockk {
        every {
            this@mockk.rating
        } returns rating
    }

    private fun stubBook(
        title: String = "Test Book",
        authors: List<Author> = listOf(
            Author(
                id = 1,
                name = "Jane Austen",
            ),
        ),
        rating: Double = 4.0,
        description: String = "A description",
        releaseYear: Int = 2020,
        coverUrl: String = "https://book.cover/img.jpg",
        defaultEdition: BookEdition? = null,
        userBook: UserBook? = null,
        tags: List<Tag> = emptyList(),
    ): Book = mockk {
        every {
            this@mockk.title
        } returns title

        every {
            this@mockk.authors
        } returns authors

        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.description
        } returns description

        every {
            this@mockk.releaseYear
        } returns releaseYear

        every {
            this@mockk.coverUrl
        } returns coverUrl

        every {
            this@mockk.defaultEdition
        } returns defaultEdition

        every {
            this@mockk.userBook
        } returns userBook

        every {
            this@mockk.tags
        } returns tags
    }

    @Nested
    inner class UserRating {
        @Test
        fun `doubles the star rating into the verdict score`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
                userBook = stubUserBook(rating = 4.5),
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe "Your 9/10"
        }

        @Test
        fun `is left out when the rating is zero`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
                userBook = stubUserBook(rating = 0.0),
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe null
        }

        @Test
        fun `is left out when the rating is null`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
                userBook = stubUserBook(rating = null),
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe null
        }

        @Test
        fun `is left out when the book is not on a shelf`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
                userBook = null,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe null
        }
    }

    @Nested
    inner class CommunityRating {
        @Test
        fun `rounds the community rating to one decimal`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 4.25,
                releaseYear = -1,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe "★ 4.3"
        }

        @Test
        fun `is left out when the community rating is zero`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe null
        }
    }

    @Nested
    inner class StatsLine {
        @Test
        fun `joins every stat in reading order`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 4.0,
                userBook = stubUserBook(rating = 3.0),
            )
            val edition = stubEdition(
                releaseYear = 2019,
                pages = 640,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = edition,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe "★ 4.0 · Your 6/10 · 2019 · 640 pages"
        }

        @Test
        fun `leaves out a non-positive page count`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
            )
            val edition = stubEdition(
                releaseYear = -1,
                pages = 0,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = edition,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe null
        }
    }

    @Nested
    inner class Identity {
        @Test
        fun `carries the title and the first author`() {
            // ----- Arrange -----
            val book = stubBook(
                title = "Emma",
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Jane Austen",
                    ),
                    Author(
                        id = 2,
                        name = "Someone Else",
                    ),
                ),
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.title shouldBe "Emma"
            result.author shouldBe "Jane Austen"
        }

        @Test
        fun `author is empty when the book has no authors`() {
            // ----- Arrange -----
            val book = stubBook(authors = emptyList())

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.author shouldBe ""
        }

        @Test
        fun `description is null when blank`() {
            // ----- Arrange -----
            val book = stubBook(description = "  ")

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.description shouldBe null
        }
    }

    @Nested
    inner class EditionResolution {
        @Test
        fun `prefers the given edition over the default edition`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                defaultEdition = stubEdition(pages = 100),
            )
            val edition = stubEdition(
                pages = 640,
                releaseYear = 2019,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = edition,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe "2019 · 640 pages"
        }

        @Test
        fun `falls back to the default edition when none is given`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                defaultEdition = stubEdition(pages = 100),
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe "2021 · 100 pages"
        }

        @Test
        fun `the release year falls back to the book when the edition has the unknown sentinel`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = 2020,
            )
            val edition = stubEdition(
                releaseYear = -1,
                pages = null,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = edition,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe "2020"
        }

        @Test
        fun `the year is left out when both the edition and the book have the unknown sentinel`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
            )
            val edition = stubEdition(
                releaseYear = -1,
                pages = null,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = edition,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.statsLine shouldBe null
        }
    }

    @Nested
    inner class CoverResolution {
        @TempDir
        lateinit var tempDir: File

        @Test
        fun `the chosen edition's local file wins over its url`() {
            // ----- Arrange -----
            val localFile = File(
                tempDir,
                "cover.jpg",
            )
            localFile.writeText("img")
            val book = stubBook(coverUrl = "https://book.cover/img.jpg")
            val edition = stubEdition(
                localImagePath = localFile.path,
                url = "https://edition.cover/img.jpg",
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = edition,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.cover.source shouldBe CoverSource.Local(
                path = localFile.path,
                cacheKey = "https://edition.cover/img.jpg",
            )
        }

        @Test
        fun `the chosen edition's url is used when it has no local file`() {
            // ----- Arrange -----
            val book = stubBook(coverUrl = "https://book.cover/img.jpg")
            val edition = stubEdition(url = "https://edition.cover/img.jpg")

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = edition,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.cover.source shouldBe CoverSource.Remote(
                url = "https://edition.cover/img.jpg",
                persistEditionId = 7,
            )
        }

        @Test
        fun `an imageless chosen edition falls back to the default edition's image`() {
            // ----- Arrange -----
            val defaultEdition = stubEdition(url = "https://default.cover/img.jpg")
            val book = stubBook(
                coverUrl = "https://book.cover/img.jpg",
                defaultEdition = defaultEdition,
            )
            val edition = stubEdition()

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = edition,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.cover.source shouldBe CoverSource.Remote(
                url = "https://default.cover/img.jpg",
                persistEditionId = 7,
            )
        }

        @Test
        fun `the book cover url is used when no edition has an image`() {
            // ----- Arrange -----
            val book = stubBook(coverUrl = "https://book.cover/img.jpg")
            val edition = stubEdition()

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = edition,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.cover.source shouldBe CoverSource.Remote(
                url = "https://book.cover/img.jpg",
                persistEditionId = null,
            )
        }

        @Test
        fun `the cover carries the book title as its coverless title`() {
            // ----- Arrange -----
            val book = stubBook(title = "Test Book")

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.cover.coverlessTitle shouldBe "Test Book"
        }

        @Test
        fun `a blank book cover url without any edition image leaves the cover coverless`() {
            // ----- Arrange -----
            val book = stubBook(
                title = "Test Book",
                coverUrl = "",
            )
            val edition = stubEdition()

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = edition,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.cover.source.shouldBeNull()
            result.cover.coverlessTitle shouldBe "Test Book"
        }
    }

    @Nested
    inner class Format {
        @Test
        fun `carries the requested story format onto the card`() {
            // ----- Arrange -----
            val book = stubBook()

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.format shouldBe ShareCardFormat.Story
        }

        @Test
        fun `carries the requested full text format onto the card`() {
            // ----- Arrange -----
            val book = stubBook()

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.FullText,
            )

            // ----- Assert -----
            result.format shouldBe ShareCardFormat.FullText
        }
    }


    @Nested
    inner class Tags {
        private val cozy = Tag(
            id = 1,
            name = "Cozy",
            category = TagCategory.MOOD,
            count = 9,
        )
        private val fantasy = Tag(
            id = 2,
            name = "Fantasy",
            category = TagCategory.GENRE,
            count = 4,
        )
        private val slowBurn = Tag(
            id = 3,
            name = "Slow burn",
            category = TagCategory.TAG,
            count = 7,
        )

        @Test
        fun `carries no tags when none are selected`() {
            // ----- Arrange -----
            val book = stubBook(tags = listOf(cozy, fantasy))

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.tags shouldBe emptyList()
        }

        @Test
        fun `keeps only the selected tags in category order`() {
            // ----- Arrange -----
            val book = stubBook(tags = listOf(slowBurn, cozy, fantasy))

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
                selectedTags = setOf("Slow burn", "Fantasy"),
            )

            // ----- Assert -----
            result.tags.map { it.label } shouldBe listOf("Fantasy", "Slow burn")
        }

        @Test
        fun `ignores a selected tag the book does not offer`() {
            // ----- Arrange -----
            val book = stubBook(tags = listOf(cozy))

            // ----- Act -----
            val result = book.toBookShareCardUiModel(
                edition = null,
                format = ShareCardFormat.Story,
                selectedTags = setOf("Cozy", "Not a tag"),
            )

            // ----- Assert -----
            result.tags.map { it.label } shouldBe listOf("Cozy")
        }
    }
}
