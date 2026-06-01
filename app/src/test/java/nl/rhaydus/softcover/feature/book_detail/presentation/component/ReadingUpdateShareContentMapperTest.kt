package nl.rhaydus.softcover.feature.book_detail.presentation.component

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.core.presentation.share.ReadingUpdateKind
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingUpdateShareContentMapperTest {

    // ----- Fixtures -----

    private fun buildEdition(
        pages: Int? = null,
        url: String? = null,
        localImagePath: String? = null,
    ): BookEdition = BookEdition(
        id = 1,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = null,
        url = url,
        localImagePath = localImagePath,
        isbn10 = null,
        isbn13 = null,
        pages = pages,
        audioSeconds = null,
        authors = emptyList(),
        releaseYear = 2020,
        releaseDate = null,
        format = "paperback",
        owned = false,
    )

    private fun buildUserBook(
        status: BookStatus = BookStatus.Read,
        rating: Double? = null,
        reviewDocument: ReviewDocument? = null,
        reviewHasSpoilers: Boolean = false,
    ): UserBook = UserBook(
        id = 1,
        status = status,
        dateAdded = "2024-01-01",
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = reviewHasSpoilers,
        editionId = null,
        lastReadDate = null,
        rating = rating,
        referrerUserId = null,
        reviewDocument = reviewDocument,
        reviewedAt = null,
        updatedAt = null,
        journals = emptyList(),
    )

    private fun buildUserBookRead(
        currentPage: Int? = null,
        currentSeconds: Int? = null,
        progress: Float = 0f,
    ): UserBookRead = UserBookRead(
        id = 1,
        currentPage = currentPage,
        currentSeconds = currentSeconds,
        progress = progress,
        startedAt = null,
        finishedAt = null,
    )

    private fun buildAudiobookEdition(
        audioSeconds: Int? = null,
    ): BookEdition = BookEdition(
        id = 2,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = audioSeconds,
        authors = emptyList(),
        releaseYear = 2020,
        releaseDate = null,
        format = "audio",
        owned = false,
    )

    private fun buildBook(
        title: String = "Test Book",
        coverUrl: String = "https://book.cover/img.jpg",
        authors: List<Author> = listOf(Author(id = 1, name = "Jane Austen")),
        defaultEdition: BookEdition? = null,
        userBook: UserBook? = buildUserBook(),
        userBookRead: UserBookRead? = null,
    ): Book = Book(
        id = 1,
        canonicalId = null,
        title = title,
        editions = emptyList(),
        defaultEdition = defaultEdition,
        rating = 4.0,
        description = "",
        releaseYear = 2020,
        releaseDate = null,
        coverUrl = coverUrl,
        authors = authors,
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = userBook,
        userBookRead = userBookRead,
    )

    // ----- username guards -----

    @Nested
    inner class UsernameGuards {

        @Test
        fun `returns null when username is null`() {
            // ----- Arrange -----
            val book = buildBook()

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = null,
                avatarUrl = "https://avatar.url",
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when username is blank`() {
            // ----- Arrange -----
            val book = buildBook()

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "   ",
                avatarUrl = "https://avatar.url",
            )

            // ----- Assert -----
            result shouldBe null
        }
    }

    // ----- userBook status guards -----

    @Nested
    inner class UserBookStatusGuards {

        @Test
        fun `returns null when userBook is null`() {
            // ----- Arrange -----
            val book = buildBook(userBook = null)

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when status is WantToRead`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(status = BookStatus.WantToRead))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when status is None`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(status = BookStatus.None))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when status is DidNotFinish`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(status = BookStatus.DidNotFinish))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result shouldBe null
        }
    }

    // ----- kind = FINISHED (status Read) -----

    @Nested
    inner class FinishedBook {

        @Test
        fun `kind is FINISHED when status is Read`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(status = BookStatus.Read))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.kind shouldBe ReadingUpdateKind.FINISHED
        }

        @Test
        fun `ratingStars carries rating 4-5 unchanged — not halved`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(rating = 4.5))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.ratingStars shouldBe 4.5
        }

        @Test
        fun `ratingStars is null when rating is 0-0`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(rating = 0.0))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.ratingStars shouldBe null
        }

        @Test
        fun `ratingStars is null when rating is null`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(rating = null))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.ratingStars shouldBe null
        }

        @Test
        fun `progressLabel is null for finished book`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(status = BookStatus.Read))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe null
        }
    }

    // ----- kind = READING (status Reading) -----

    @Nested
    inner class ReadingBook {

        @Test
        fun `kind is READING when status is Reading`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(status = BookStatus.Reading))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.kind shouldBe ReadingUpdateKind.READING
        }

        @Test
        fun `ratingStars is null for reading book`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(status = BookStatus.Reading, rating = 3.0))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.ratingStars shouldBe null
        }

        @Test
        fun `review is null for reading book`() {
            // ----- Arrange -----
            val review = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("Great so far")))))
            val book = buildBook(userBook = buildUserBook(status = BookStatus.Reading, reviewDocument = review))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.review shouldBe null
        }

        @Test
        fun `progressLabel includes page and percentage when currentPage and edition pages are present`() {
            // ----- Arrange -----
            val edition = buildEdition(pages = 400)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(currentPage = 100, progress = 25f),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe "page 100 / 400 · 25%"
        }

        @Test
        fun `progressLabel uses middle dot U+00B7 as separator`() {
            // ----- Arrange -----
            val edition = buildEdition(pages = 300)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(currentPage = 150, progress = 50f),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe "page 150 / 300 · 50%"
        }

        @Test
        fun `progressLabel concrete case — page 61 of 195 rounds to 31 percent`() {
            // ----- Arrange -----
            val edition = buildEdition(pages = 195)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(currentPage = 61, progress = 31f),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe "page 61 / 195 · 31%"
        }

        @Test
        fun `progressLabel falls back to percentage-only when currentPage is null`() {
            // ----- Arrange -----
            val edition = buildEdition(pages = 400)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(currentPage = null, progress = 31f),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe "31%"
        }

        @Test
        fun `progressLabel falls back to percentage-only when edition pages is null`() {
            // ----- Arrange -----
            val edition = buildEdition(pages = null)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(currentPage = 50, progress = 40f),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe "40%"
        }

        @Test
        fun `progressLabel falls back to percentage-only when no edition is provided`() {
            // ----- Arrange -----
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(currentPage = 10, progress = 10f),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe "10%"
        }

        @Test
        fun `progressLabel falls back to percentage-only when progress is 100`() {
            // ----- Arrange -----
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(currentPage = null, progress = 100f),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe "100%"
        }

        @Test
        fun `progressLabel percentage is rounded`() {
            // ----- Arrange -----
            val edition = buildEdition(pages = 100)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(currentPage = 33, progress = 33f),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe "page 33 / 100 · 33%"
        }

        @Test
        fun `progressLabel shows time and percentage for audiobook when currentSeconds and audioSeconds are present`() {
            // ----- Arrange -----
            val edition = buildAudiobookEdition(audioSeconds = 7200)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(currentSeconds = 3600),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe "1h 0m / 2h 0m · 50%"
        }

        @Test
        fun `progressLabel falls back to percentage-only for audiobook when currentSeconds is null`() {
            // ----- Arrange -----
            val edition = buildAudiobookEdition(audioSeconds = 7200)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(currentSeconds = null, progress = 40f),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe "40%"
        }

        @Test
        fun `progressLabel is null when userBookRead is null`() {
            // ----- Arrange -----
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = null,
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.progressLabel shouldBe null
        }
    }

    // ----- review -----

    @Nested
    inner class Review {

        @Test
        fun `review equals the ReviewDocument unchanged for finished book`() {
            // ----- Arrange -----
            val reviewDoc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("A great read")))))
            val book = buildBook(userBook = buildUserBook(reviewDocument = reviewDoc))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.review shouldBe reviewDoc
        }

        @Test
        fun `inline spoiler runs are preserved in the document — masking happens at render time`() {
            // ----- Arrange -----
            val reviewDoc = ReviewDocument(listOf(
                ReviewParagraph(listOf(
                    ReviewRun("Great story"),
                    ReviewRun(" — spoiler here", spoiler = true),
                    ReviewRun(" with a lovely ending"),
                )),
            ))
            val book = buildBook(
                userBook = buildUserBook(
                    reviewDocument = reviewDoc,
                    reviewHasSpoilers = false,
                ),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.review shouldBe reviewDoc
        }

        @Test
        fun `review is non-null when reviewHasSpoilers is true — whole-review spoiler flag does not null the document`() {
            // ----- Arrange -----
            val reviewDoc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("A great read")))))
            val book = buildBook(
                userBook = buildUserBook(
                    reviewDocument = reviewDoc,
                    reviewHasSpoilers = true,
                ),
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.review shouldBe reviewDoc
        }

        @Test
        fun `review is null when reviewDocument is null`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(reviewDocument = null))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.review shouldBe null
        }

        @Test
        fun `review is null when reviewDocument is blank`() {
            // ----- Arrange -----
            val reviewDoc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("   ")))))
            val book = buildBook(userBook = buildUserBook(reviewDocument = reviewDoc))

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.review shouldBe null
        }
    }

    // ----- avatarUrl -----

    @Nested
    inner class AvatarUrl {

        @Test
        fun `avatarUrl is passed through when non-blank`() {
            // ----- Arrange -----
            val book = buildBook()

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = "https://avatar.example.com/me.jpg",
            )

            // ----- Assert -----
            result?.avatarUrl shouldBe "https://avatar.example.com/me.jpg"
        }

        @Test
        fun `avatarUrl is null when blank`() {
            // ----- Arrange -----
            val book = buildBook()

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = "   ",
            )

            // ----- Assert -----
            result?.avatarUrl shouldBe null
        }

        @Test
        fun `avatarUrl is null when null`() {
            // ----- Arrange -----
            val book = buildBook()

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.avatarUrl shouldBe null
        }
    }

    // ----- cover resolution -----

    @Nested
    inner class CoverResolution {

        @Test
        fun `cover prefers edition localImagePath over edition url`() {
            // ----- Arrange -----
            val edition = buildEdition(
                localImagePath = "file:///local/cover.jpg",
                url = "https://remote.cover/img.jpg",
            )
            val book = buildBook(coverUrl = "https://book.cover/img.jpg")

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.coverUrl shouldBe "file:///local/cover.jpg"
        }

        @Test
        fun `cover falls back to edition url when localImagePath is null`() {
            // ----- Arrange -----
            val edition = buildEdition(
                localImagePath = null,
                url = "https://remote.cover/img.jpg",
            )
            val book = buildBook(coverUrl = "https://book.cover/img.jpg")

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.coverUrl shouldBe "https://remote.cover/img.jpg"
        }

        @Test
        fun `cover falls back to book coverUrl when edition provides neither localImagePath nor url`() {
            // ----- Arrange -----
            val edition = buildEdition(
                localImagePath = null,
                url = null,
            )
            val book = buildBook(coverUrl = "https://book.cover/img.jpg")

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.coverUrl shouldBe "https://book.cover/img.jpg"
        }

        @Test
        fun `cover falls back to book coverUrl when no edition is provided and defaultEdition is null`() {
            // ----- Arrange -----
            val book = buildBook(
                coverUrl = "https://book.cover/img.jpg",
                defaultEdition = null,
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.coverUrl shouldBe "https://book.cover/img.jpg"
        }

        @Test
        fun `cover uses defaultEdition localImagePath when no edition arg is passed`() {
            // ----- Arrange -----
            val defaultEdition = buildEdition(localImagePath = "file:///default/cover.jpg")
            val book = buildBook(
                coverUrl = "https://book.cover/img.jpg",
                defaultEdition = defaultEdition,
            )

            // ----- Act -----
            val result = book.toReadingUpdateContent(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.coverUrl shouldBe "file:///default/cover.jpg"
        }
    }
}
