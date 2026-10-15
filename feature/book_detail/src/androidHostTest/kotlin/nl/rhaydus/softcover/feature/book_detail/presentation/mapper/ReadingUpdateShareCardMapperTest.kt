package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.richtext.RichTextParagraph
import nl.rhaydus.softcover.core.component.richtext.RichTextRun
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextVariant
import nl.rhaydus.softcover.core.component.share.ReadingUpdateShareCardUiModel
import nl.rhaydus.softcover.core.component.share.ShareCardFormat
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.ReviewDocument
import nl.rhaydus.softcover.core.domain.model.ReviewParagraph
import nl.rhaydus.softcover.core.domain.model.ReviewRun
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ReadingUpdateShareCardMapperTest {
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
        reads = emptyList(),
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
        readingFormat = ReadingFormat.Audio,
    )

    private fun buildBook(
        title: String = "Test Book",
        coverUrl: String = "https://book.cover/img.jpg",
        authors: List<Author> = listOf(
            Author(
                id = 1,
                name = "Jane Austen",
            ),
        ),
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
        userBook = userBook?.copy(reads = listOfNotNull(userBookRead)),
    )

    // ----- username guards -----

    @Nested
    inner class UsernameGuards {
        @Test
        fun `returns null when username is null`() {
            // ----- Arrange -----
            val book = buildBook()

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
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
            val result = book.toReadingUpdateShareCardUiModel(
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
            val result = book.toReadingUpdateShareCardUiModel(
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
            val result = book.toReadingUpdateShareCardUiModel(
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
            val result = book.toReadingUpdateShareCardUiModel(
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
        fun `ratingStars carries rating 4-5 unchanged — not halved`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(rating = 4.5))

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().ratingStars shouldBe 4.5
        }

        @Test
        fun `ratingStars is null when rating is 0-0`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(rating = 0.0))

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().ratingStars shouldBe null
        }

        @Test
        fun `ratingStars is null when rating is null`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(rating = null))

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().ratingStars shouldBe null
        }
    }

    // ----- kind = READING (status Reading) -----

    @Nested
    inner class ReadingBook {
        @Test
        fun `progressLabel includes page and percentage when currentPage and edition pages are present`() {
            // ----- Arrange -----
            val edition = buildEdition(pages = 400)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(
                    currentPage = 100,
                    progress = 25f,
                ),
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Reading>().progressLabel shouldBe "p. 100 of 400 · 25%"
        }

        @Test
        fun `progressLabel falls back to percentage-only when currentPage is null`() {
            // ----- Arrange -----
            val edition = buildEdition(pages = 400)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(
                    currentPage = null,
                    progress = 31f,
                ),
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Reading>().progressLabel shouldBe "31%"
        }

        @Test
        fun `progressLabel falls back to percentage-only when edition pages is null`() {
            // ----- Arrange -----
            val edition = buildEdition(pages = null)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(
                    currentPage = 50,
                    progress = 40f,
                ),
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Reading>().progressLabel shouldBe "40%"
        }

        @Test
        fun `progressLabel falls back to percentage-only when no edition is provided`() {
            // ----- Arrange -----
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(
                    currentPage = 10,
                    progress = 10f,
                ),
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Reading>().progressLabel shouldBe "10%"
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
            val result = book.toReadingUpdateShareCardUiModel(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Reading>().progressLabel shouldBe "1h 0m of 2h 0m · 50%"
        }

        @Test
        fun `progressLabel falls back to percentage-only for audiobook when currentSeconds is null`() {
            // ----- Arrange -----
            val edition = buildAudiobookEdition(audioSeconds = 7200)
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = buildUserBookRead(
                    currentSeconds = null,
                    progress = 40f,
                ),
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Reading>().progressLabel shouldBe "40%"
        }

        @Test
        fun `progressLabel is null when userBookRead is null`() {
            // ----- Arrange -----
            val book = buildBook(
                userBook = buildUserBook(status = BookStatus.Reading),
                userBookRead = null,
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Reading>().progressLabel shouldBe null
        }
    }

    // ----- review -----

    @Nested
    inner class Review {
        @Test
        fun `inline spoiler runs are preserved in the rich text — masking happens at render time`() {
            // ----- Arrange -----
            val reviewDoc = ReviewDocument(
                listOf(
                    ReviewParagraph(
                        listOf(
                            ReviewRun("Great story"),
                            ReviewRun(
                                " — spoiler here",
                                spoiler = true,
                            ),
                            ReviewRun(" with a lovely ending"),
                        ),
                    ),
                ),
            )
            val book = buildBook(
                userBook = buildUserBook(
                    reviewDocument = reviewDoc,
                    reviewHasSpoilers = false,
                ),
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().review shouldBe RichTextUiModel(
                paragraphs = persistentListOf(
                    RichTextParagraph(
                        runs = persistentListOf(
                            RichTextRun(text = "Great story"),
                            RichTextRun(
                                text = " — spoiler here",
                                spoiler = true,
                            ),
                            RichTextRun(text = " with a lovely ending"),
                        ),
                    ),
                ),
                variant = RichTextVariant.ShareCard,
            )
        }

        @Test
        fun `review is non-null when reviewHasSpoilers is true — whole-review spoiler flag does not null the rich text`() {
            // ----- Arrange -----
            val reviewDoc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("A great read")))))
            val book = buildBook(
                userBook = buildUserBook(
                    reviewDocument = reviewDoc,
                    reviewHasSpoilers = true,
                ),
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().review shouldBe RichTextUiModel(
                paragraphs = persistentListOf(
                    RichTextParagraph(runs = persistentListOf(RichTextRun(text = "A great read"))),
                ),
                variant = RichTextVariant.ShareCard,
            )
        }

        @Test
        fun `review is null when reviewDocument is null`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(reviewDocument = null))

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().review shouldBe null
        }

        @Test
        fun `review is null when reviewDocument is blank`() {
            // ----- Arrange -----
            val reviewDoc = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("   ")))))
            val book = buildBook(userBook = buildUserBook(reviewDocument = reviewDoc))

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().review shouldBe null
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
            val result = book.toReadingUpdateShareCardUiModel(
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
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = "   ",
            )

            // ----- Assert -----
            result?.avatarUrl shouldBe null
        }
    }

    // ----- cover resolution -----

    @Nested
    inner class CoverResolution {
        @TempDir
        lateinit var tempDir: File

        @Test
        fun `cover prefers the edition's local file over its url`() {
            // ----- Arrange -----
            val localFile = File(
                tempDir,
                "cover.jpg",
            )
            localFile.writeText("img")
            val edition = buildEdition(
                localImagePath = localFile.path,
                url = "https://remote.cover/img.jpg",
            )
            val book = buildBook(coverUrl = "https://book.cover/img.jpg")

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.cover?.source shouldBe CoverSource.Local(
                path = localFile.path,
                cacheKey = "https://remote.cover/img.jpg",
            )
        }

        @Test
        fun `cover falls back to the edition url when it has no local file`() {
            // ----- Arrange -----
            val edition = buildEdition(url = "https://remote.cover/img.jpg")
            val book = buildBook(coverUrl = "https://book.cover/img.jpg")

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.cover?.source shouldBe CoverSource.Remote(
                url = "https://remote.cover/img.jpg",
                persistEditionId = 1,
            )
        }

        @Test
        fun `cover falls back to book coverUrl when no edition has an image`() {
            // ----- Arrange -----
            val edition = buildEdition()
            val book = buildBook(coverUrl = "https://book.cover/img.jpg")

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = edition,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.cover?.source shouldBe CoverSource.Remote(
                url = "https://book.cover/img.jpg",
                persistEditionId = null,
            )
        }

        @Test
        fun `cover uses the default edition's image when the chosen edition has none`() {
            // ----- Arrange -----
            val defaultEdition = buildEdition(url = "https://default.cover/img.jpg")
            val book = buildBook(
                coverUrl = "https://book.cover/img.jpg",
                defaultEdition = defaultEdition,
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = buildEdition(),
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.cover?.source shouldBe CoverSource.Remote(
                url = "https://default.cover/img.jpg",
                persistEditionId = 1,
            )
        }

        @Test
        fun `cover carries the book title as its coverless title`() {
            // ----- Arrange -----
            val book = buildBook(title = "Test Book")

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.cover?.coverlessTitle shouldBe "Test Book"
        }

        @Test
        fun `cover is coverless when the book cover url is blank and no edition has an image`() {
            // ----- Arrange -----
            val book = buildBook(
                title = "Test Book",
                coverUrl = "",
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = buildEdition(),
                username = "reader",
                avatarUrl = null,
            )

            // ----- Assert -----
            result?.cover?.source.shouldBeNull()
            result?.cover?.coverlessTitle shouldBe "Test Book"
        }
    }

    // ----- tags -----

    @Nested
    inner class Tags {
        @Test
        fun `tags are over-media chips and spoiler tags are excluded`() {
            // ----- Arrange -----
            val book = buildBook()
            val tags = listOf(
                UserTag(
                    name = "Cozy",
                    category = TagCategory.MOOD,
                ),
                UserTag(
                    name = "Twist",
                    category = TagCategory.MOOD,
                    spoiler = true,
                ),
            )

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
                userTags = tags,
            )

            // ----- Assert -----
            result?.tags?.map { it.label to it.tone } shouldBe listOf("Cozy" to ChipTone.OverMedia)
        }
    }

    @Nested
    inner class Format {
        private val reviewDocument = ReviewDocument(listOf(ReviewParagraph(listOf(ReviewRun("A great read")))))

        @Test
        fun `a finished book carries the requested format`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(status = BookStatus.Read))

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
                format = ShareCardFormat.FullText,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().format shouldBe ShareCardFormat.FullText
        }

        @Test
        fun `a finished review uses the capped card variant in the story format`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(reviewDocument = reviewDocument))

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
                format = ShareCardFormat.Story,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().review.shouldNotBeNull().variant shouldBe
                RichTextVariant.ShareCard
        }

        @Test
        fun `a finished review uses the uncapped card variant in the full text format`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(reviewDocument = reviewDocument))

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
                format = ShareCardFormat.FullText,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Finished>().review.shouldNotBeNull().variant shouldBe
                RichTextVariant.ShareCardFullText
        }

        @Test
        fun `a reading book stays in the story format whatever format is requested`() {
            // ----- Arrange -----
            val book = buildBook(userBook = buildUserBook(status = BookStatus.Reading))

            // ----- Act -----
            val result = book.toReadingUpdateShareCardUiModel(
                edition = null,
                username = "reader",
                avatarUrl = null,
                format = ShareCardFormat.FullText,
            )

            // ----- Assert -----
            result.shouldBeInstanceOf<ReadingUpdateShareCardUiModel.Reading>().format shouldBe ShareCardFormat.Story
        }
    }
}
