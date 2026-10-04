package nl.rhaydus.softcover.feature.reading.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FeaturedProgressMapperTest {
    private fun edition(
        pages: Int? = null,
        audioSeconds: Int? = null,
        readingFormat: ReadingFormat? = null,
    ): BookEdition = BookEdition(
        id = 50,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = pages,
        audioSeconds = audioSeconds,
        authors = emptyList(),
        releaseYear = 2020,
        format = "Paperback",
        readingFormat = readingFormat,
        owned = false,
    )

    private fun read(
        progress: Float = 0f,
        currentPage: Int? = null,
        currentSeconds: Int? = null,
    ): UserBookRead = UserBookRead(
        id = 1,
        currentPage = currentPage,
        currentSeconds = currentSeconds,
        progress = progress,
        startedAt = null,
        finishedAt = null,
    )

    private fun book(
        defaultEdition: BookEdition? = null,
        editions: List<BookEdition> = emptyList(),
        userBookRead: UserBookRead? = null,
    ): Book = Book(
        id = 1,
        title = "Title",
        editions = editions,
        defaultEdition = defaultEdition,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
        userBookRead = userBookRead,
    )

    @Nested
    inner class Percent {
        @Test
        fun `rounds the progress to a whole percent`() {
            // ----- Arrange -----
            val subject = book(userBookRead = read(progress = 42.6f))

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.percent shouldBe "43%"
        }

        @Test
        fun `reads 0 percent when there is no read`() {
            // ----- Arrange -----
            val subject = book(userBookRead = null)

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.percent shouldBe "0%"
        }
    }

    @Nested
    inner class Bar {
        @Test
        fun `fraction is the progress as a share of one`() {
            // ----- Arrange -----
            val subject = book(userBookRead = read(progress = 25f))

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.bar.fraction shouldBe 0.25f
        }

        @Test
        fun `fraction is clamped to one above 100 percent`() {
            // ----- Arrange -----
            val subject = book(userBookRead = read(progress = 140f))

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.bar.fraction shouldBe 1f
        }

        @Test
        fun `fraction is clamped to zero below 0 percent`() {
            // ----- Arrange -----
            val subject = book(userBookRead = read(progress = -10f))

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.bar.fraction shouldBe 0f
        }

        @Test
        fun `fraction is zero when there is no read`() {
            // ----- Arrange -----
            val subject = book(userBookRead = null)

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.bar.fraction shouldBe 0f
        }
    }

    @Nested
    inner class PaperBook {
        @Test
        fun `lead names the current page and tail the edition page count`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = edition(pages = 320),
                userBookRead = read(currentPage = 120),
            )

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.pageLead shouldBe "Page 120"
            result.pageTail shouldBe " of 320"
        }

        @Test
        fun `lead falls back to page 0 when the read has no current page`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = edition(pages = 320),
                userBookRead = read(currentPage = null),
            )

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.pageLead shouldBe "Page 0"
        }

        @Test
        fun `tail is omitted when the total page count is unknown`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = edition(pages = null),
                userBookRead = read(currentPage = 12),
            )

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.pageLead shouldBe "Page 12"
            result.pageTail.shouldBeNull()
        }

        @Test
        fun `tail is omitted when the book has no edition at all`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = null,
                userBookRead = read(currentPage = 12),
            )

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.pageTail.shouldBeNull()
        }

        @Test
        fun `tail uses the current edition page count over the default edition`() {
            // ----- Arrange -----
            val owned = edition(pages = 500).copy(
                id = 77,
                owned = true,
            )
            val subject = book(
                defaultEdition = edition(pages = 320),
                editions = listOf(owned),
                userBookRead = read(currentPage = 10),
            )

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.pageTail shouldBe " of 500"
        }

        @Test
        fun `tail falls back to the default edition when the current edition has no page count`() {
            // ----- Arrange -----
            val owned = edition(pages = null).copy(
                id = 77,
                owned = true,
            )
            val subject = book(
                defaultEdition = edition(pages = 320),
                editions = listOf(owned),
                userBookRead = read(currentPage = 10),
            )

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.pageTail shouldBe " of 320"
        }
    }

    @Nested
    inner class Audiobook {
        @Test
        fun `lead and tail are the listened and total time`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = edition(
                    audioSeconds = 36_000,
                    readingFormat = ReadingFormat.Audio,
                ),
                userBookRead = read(currentSeconds = 5_400),
            )

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.pageLead shouldBe secondsToHm(5_400)
            result.pageTail shouldBe " of ${secondsToHm(36_000)}"
        }

        @Test
        fun `lead falls back to zero time when the read has no current seconds`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = edition(
                    audioSeconds = 36_000,
                    readingFormat = ReadingFormat.Audio,
                ),
                userBookRead = read(currentSeconds = null),
            )

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.pageLead shouldBe secondsToHm(0)
        }

        @Test
        fun `tail is omitted when the total audio length is unknown`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = edition(
                    audioSeconds = null,
                    readingFormat = ReadingFormat.Audio,
                ),
                userBookRead = read(currentSeconds = 5_400),
            )

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.pageTail.shouldBeNull()
        }

        @Test
        fun `does not use the page wording`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = edition(
                    pages = 320,
                    audioSeconds = 36_000,
                    readingFormat = ReadingFormat.Audio,
                ),
                userBookRead = read(
                    currentPage = 120,
                    currentSeconds = 5_400,
                ),
            )

            // ----- Act -----
            val result = subject.toFeaturedProgress()

            // ----- Assert -----
            result.pageLead shouldBe secondsToHm(5_400)
        }
    }
}
