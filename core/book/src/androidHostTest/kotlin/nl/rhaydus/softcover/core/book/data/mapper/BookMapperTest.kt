package nl.rhaydus.softcover.core.book.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.fragment.BookDetailFragment
import nl.rhaydus.softcover.fragment.BookListFragment
import nl.rhaydus.softcover.fragment.EditionDetailFragment
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.ReadingJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Progress_updated_journal.Companion.readingJournalFragment as progressUpdatedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Status_stopped_journal.Companion.readingJournalFragment as statusStoppedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read_finished_journal.Companion.readingJournalFragment as userBookReadFinishedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read_started_journal.Companion.readingJournalFragment as userBookReadStartedJournalFragment
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookMapperTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun mockSharedCompanions() {
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.Progress_updated_journal.Companion)
            mockkObject(UserBookFragment.Status_stopped_journal.Companion)
            mockkObject(UserBookFragment.User_book_read_finished_journal.Companion)
            mockkObject(UserBookFragment.User_book_read_started_journal.Companion)
        }

        @JvmStatic
        @AfterAll
        fun unmockSharedCompanions() {
            unmockkAll()
        }
    }

    private fun EditionFragment.stubEditionFields(
        id: Int = 10,
        canonicalId: Int? = null,
        title: String? = null,
        bookId: Int = 1,
        isbn10: String? = null,
        isbn13: String? = null,
        pages: Int? = null,
        publisher: EditionFragment.Publisher? = null,
        image: EditionFragment.Image? = null,
        fallbackImages: List<EditionFragment.FallbackImage> = emptyList(),
        releaseYear: Int? = null,
        releaseDate: String? = null,
        editionFormat: String? = null,
        audioSeconds: Int? = null,
        readingFormatId: Int = 0,
    ) {
        every {
            this@stubEditionFields.id
        } returns id
        every {
            canonical_id
        } returns canonicalId
        every {
            this@stubEditionFields.title
        } returns title
        every {
            book_id
        } returns bookId
        every {
            isbn_10
        } returns isbn10
        every {
            isbn_13
        } returns isbn13
        every {
            this@stubEditionFields.pages
        } returns pages
        every {
            this@stubEditionFields.publisher
        } returns publisher
        every {
            this@stubEditionFields.image
        } returns image
        every {
            this@stubEditionFields.fallbackImages
        } returns fallbackImages
        every {
            release_year
        } returns releaseYear
        every {
            release_date
        } returns releaseDate
        every {
            edition_format
        } returns editionFormat
        every {
            audio_seconds
        } returns audioSeconds
        every {
            reading_format_id
        } returns readingFormatId
    }

    private fun stubEditionFragment(
        id: Int = 10,
        bookId: Int = 1,
        image: EditionFragment.Image? = null,
        fallbackImages: List<EditionFragment.FallbackImage> = emptyList(),
        releaseYear: Int? = null,
        releaseDate: String? = null,
        editionFormat: String? = null,
        readingFormatId: Int = 0,
    ): EditionFragment = mockk<EditionFragment>().also {
        it.stubEditionFields(
            id = id,
            bookId = bookId,
            image = image,
            fallbackImages = fallbackImages,
            releaseYear = releaseYear,
            releaseDate = releaseDate,
            editionFormat = editionFormat,
            readingFormatId = readingFormatId,
        )
    }

    private fun stubBookListFragment(
        id: Int = 100,
        canonicalId: Int? = null,
        title: String? = null,
        rating: Double? = null,
        imageUrl: String? = null,
        releaseYear: Int? = null,
        releaseDate: String? = null,
        contributions: List<BookListFragment.Contribution> = emptyList(),
        usersCount: Int = 0,
        ratingsCount: Int = 0,
        compilation: Boolean = false,
        description: String? = null,
        headline: String? = null,
        taggableCounts: List<BookListFragment.Taggable_count> = emptyList(),
    ): BookListFragment {
        val canonical = canonicalId?.let { canonicalValue ->
            mockk<BookListFragment.Canonical> {
                every {
                    this@mockk.id
                } returns canonicalValue
            }
        }
        val image = imageUrl?.let { urlValue ->
            mockk<BookListFragment.Image> {
                every {
                    url
                } returns urlValue
            }
        }

        return mockk {
            every {
                this@mockk.id
            } returns id
            every {
                this@mockk.canonical
            } returns canonical
            every {
                this@mockk.title
            } returns title
            every {
                this@mockk.rating
            } returns rating
            every {
                this@mockk.image
            } returns image
            every {
                release_year
            } returns releaseYear
            every {
                release_date
            } returns releaseDate
            every {
                book_series
            } returns emptyList()
            every {
                this@mockk.compilation
            } returns compilation
            every {
                this@mockk.contributions
            } returns contributions
            every {
                users_count
            } returns usersCount
            every {
                ratings_count
            } returns ratingsCount
            every {
                this@mockk.description
            } returns description
            every {
                this@mockk.headline
            } returns headline
            every {
                taggable_counts
            } returns taggableCounts
        }
    }

    private fun stubUserBookFragment(
        bookListFragment: BookListFragment? = stubBookListFragment(),
        editionFragment: EditionFragment? = null,
        statusId: Int = 1,
        editionId: Int? = null,
        lastReadDate: String? = null,
        privacySettingId: Int = 1,
        rating: Double? = null,
        referrerUserId: Int? = null,
        reviewHasSpoilers: Boolean = false,
        reviewSlate: List<Map<String, Any?>> = emptyList(),
        reviewedAt: String? = null,
        updatedAt: String? = null,
        createdAt: String = "2024-01-01",
        progressUpdated: List<UserBookFragment.Progress_updated_journal> = emptyList(),
        userBookReadStarted: List<UserBookFragment.User_book_read_started_journal> = emptyList(),
        userBookReadFinished: List<UserBookFragment.User_book_read_finished_journal> = emptyList(),
        statusStopped: List<UserBookFragment.Status_stopped_journal> = emptyList(),
        userBookReads: List<UserBookFragment.User_book_read> = emptyList(),
    ): UserBookFragment {
        val bookInner = mockk<UserBookFragment.Book>()
        val editionInner = editionFragment?.let { editionValue ->
            mockk<UserBookFragment.Edition>().also { inner ->
                every {
                    with(UserBookFragment.Edition.Companion) { inner.editionFragment() }
                } returns editionValue
            }
        }

        every {
            with(UserBookFragment.Book.Companion) { bookInner.bookListFragment() }
        } returns bookListFragment

        return mockk {
            every {
                book
            } returns bookInner
            every {
                edition
            } returns editionInner
            every {
                progress_updated_journal
            } returns progressUpdated
            every {
                user_book_read_started_journal
            } returns userBookReadStarted
            every {
                user_book_read_finished_journal
            } returns userBookReadFinished
            every {
                status_stopped_journal
            } returns statusStopped
            every {
                user_book_reads
            } returns userBookReads
            every {
                id
            } returns 1
            every {
                status_id
            } returns statusId
            every {
                edition_id
            } returns editionId
            every {
                last_read_date
            } returns lastReadDate
            every {
                date_added
            } returns "2024-01-01"
            every {
                privacy_setting_id
            } returns privacySettingId
            every {
                this@mockk.rating
            } returns rating
            every {
                referrer_user_id
            } returns referrerUserId
            every {
                review_has_spoilers
            } returns reviewHasSpoilers
            every {
                review_slate
            } returns reviewSlate
            every {
                reviewed_at
            } returns reviewedAt
            every {
                updated_at
            } returns updatedAt
            every {
                created_at
            } returns createdAt
        }
    }

    private fun stubBookDetailFragment(
        defaultCoverEdition: EditionFragment? = null,
        description: String? = null,
        usersCount: Int = 0,
        bookSeries: List<BookDetailFragment.Book_series> = emptyList(),
    ): BookDetailFragment {
        val defaultEditionInner = defaultCoverEdition?.let { editionValue ->
            mockk<BookDetailFragment.Default_cover_edition>().also { inner ->
                every {
                    with(BookDetailFragment.Default_cover_edition.Companion) { inner.editionFragment() }
                } returns editionValue
            }
        }

        return mockk {
            every {
                id
            } returns 1
            every {
                canonical
            } returns null
            every {
                title
            } returns "My Book"
            every {
                rating
            } returns null
            every {
                image
            } returns null
            every {
                release_year
            } returns null
            every {
                release_date
            } returns null
            every {
                book_series
            } returns bookSeries
            every {
                compilation
            } returns false
            every {
                contributions
            } returns emptyList()
            every {
                this@mockk.description
            } returns description
            every {
                headline
            } returns null
            every {
                users_count
            } returns usersCount
            every {
                ratings_count
            } returns 0
            every {
                default_cover_edition
            } returns defaultEditionInner
            every {
                taggable_counts
            } returns emptyList()
        }
    }

    @Nested
    inner class EditionFragmentToBookEdition {
        @Test
        fun `maps every populated field of the fragment onto the BookEdition`() {
            // ----- Arrange -----
            val publisher = mockk<EditionFragment.Publisher> {
                every {
                    name
                } returns "Penguin"
            }
            val image = mockk<EditionFragment.Image> {
                every {
                    url
                } returns "https://example.com/cover.jpg"
            }
            val fragment = mockk<EditionFragment>()

            fragment.stubEditionFields(
                id = 10,
                canonicalId = 11,
                title = "Edition Title",
                bookId = 1,
                isbn10 = "1234567890",
                isbn13 = "9781234567897",
                pages = 300,
                publisher = publisher,
                image = image,
                releaseYear = 2020,
                releaseDate = "2020-03-04",
                editionFormat = "Audiobook",
                audioSeconds = 3_600,
                readingFormatId = 2,
            )

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.id shouldBe 10
            result.canonicalId shouldBe 11
            result.title shouldBe "Edition Title"
            result.bookId shouldBe 1
            result.isbn10 shouldBe "1234567890"
            result.isbn13 shouldBe "9781234567897"
            result.pages shouldBe 300
            result.publisher shouldBe "Penguin"
            result.url shouldBe "https://example.com/cover.jpg"
            result.releaseYear shouldBe 2020
            result.releaseDate shouldBe LocalDate(
                year = 2020,
                month = 3,
                day = 4,
            )
            result.format shouldBe "Audiobook"
            result.audioSeconds shouldBe 3_600
            result.readingFormat shouldBe ReadingFormat.Audio
            result.owned shouldBe false
            result.localImagePath.shouldBeNull()
        }

        @Test
        fun `falls back to placeholders when optional fields are missing`() {
            // ----- Arrange -----
            val fragment = stubEditionFragment()

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.publisher.shouldBeNull()
            result.releaseYear shouldBe -1
            result.releaseDate.shouldBeNull()
            result.format shouldBe ""
            result.readingFormat.shouldBeNull()
        }

        @Test
        fun `attaches the authors passed in by the caller`() {
            // ----- Arrange -----
            val author = Author(
                id = 7,
                name = "Jane Austen",
            )
            val fragment = stubEditionFragment()

            // ----- Act -----
            val result = fragment.toBookEdition(authors = listOf(author))

            // ----- Assert -----
            result.authors shouldBe listOf(author)
        }

        @Test
        fun `url prefers the image over the fallback images`() {
            // ----- Arrange -----
            val image = mockk<EditionFragment.Image> {
                every {
                    url
                } returns "https://example.com/primary.jpg"
            }
            val fallback = mockk<EditionFragment.FallbackImage> {
                every {
                    url
                } returns "https://example.com/fallback.jpg"
            }
            val fragment = stubEditionFragment(
                image = image,
                fallbackImages = listOf(fallback),
            )

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url shouldBe "https://example.com/primary.jpg"
        }

        @Test
        fun `url falls back to the first fallback image when there is no image`() {
            // ----- Arrange -----
            val fallback = mockk<EditionFragment.FallbackImage> {
                every {
                    url
                } returns "https://example.com/fallback.jpg"
            }
            val fragment = stubEditionFragment(fallbackImages = listOf(fallback))

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url shouldBe "https://example.com/fallback.jpg"
        }

        @Test
        fun `url is null when there is no image and the fallback image has no url`() {
            // ----- Arrange -----
            val fallback = mockk<EditionFragment.FallbackImage> {
                every {
                    url
                } returns null
            }
            val fragment = stubEditionFragment(fallbackImages = listOf(fallback))

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url.shouldBeNull()
        }

        @Test
        fun `a malformed release_date maps to a null releaseDate`() {
            // ----- Arrange -----
            val fragment = stubEditionFragment(releaseDate = "not-a-date")

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.releaseDate.shouldBeNull()
        }
    }

    @Nested
    inner class EditionDetailFragmentToBookEdition {
        @Test
        fun `maps contribution authors and skips contributions without an author`() {
            // ----- Arrange -----
            val author = mockk<EditionDetailFragment.Contribution.Author> {
                every {
                    id
                } returns 5
                every {
                    name
                } returns "George Orwell"
            }
            val withAuthor = mockk<EditionDetailFragment.Contribution> {
                every {
                    this@mockk.author
                } returns author
            }
            val withoutAuthor = mockk<EditionDetailFragment.Contribution> {
                every {
                    this@mockk.author
                } returns null
            }
            val fragment = mockk<EditionDetailFragment>()

            fragment.stubEditionFields()

            every {
                fragment.contributions
            } returns listOf(
                withAuthor,
                withoutAuthor,
            )

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.authors shouldBe listOf(
                Author(
                    id = 5,
                    name = "George Orwell",
                ),
            )
        }
    }

    @Nested
    inner class UserBookFragmentToBook {
        @Test
        fun `returns null when the book list fragment is missing`() {
            // ----- Arrange -----
            val fragment = stubUserBookFragment(bookListFragment = null)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `maps the book fields from the book list fragment`() {
            // ----- Arrange -----
            val author = mockk<BookListFragment.Contribution.Author> {
                every {
                    id
                } returns 3
                every {
                    name
                } returns "Ursula Le Guin"
            }
            val withAuthor = mockk<BookListFragment.Contribution> {
                every {
                    this@mockk.author
                } returns author
            }
            val withoutAuthor = mockk<BookListFragment.Contribution> {
                every {
                    this@mockk.author
                } returns null
            }
            val fragment = stubUserBookFragment(
                bookListFragment = stubBookListFragment(
                    id = 100,
                    title = "My Book",
                    rating = 4.26,
                    imageUrl = "https://example.com/cover.jpg",
                    releaseYear = 2020,
                    releaseDate = "2020-03-04",
                    contributions = listOf(
                        withAuthor,
                        withoutAuthor,
                    ),
                    usersCount = 250,
                    ratingsCount = 77,
                    compilation = true,
                    description = "A description",
                    headline = "A headline",
                ),
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.id shouldBe 100
            result.title shouldBe "My Book"
            result.rating shouldBe 4.3
            result.coverUrl shouldBe "https://example.com/cover.jpg"
            result.releaseYear shouldBe 2020
            result.releaseDate shouldBe LocalDate(
                year = 2020,
                month = 3,
                day = 4,
            )
            result.authors shouldBe listOf(
                Author(
                    id = 3,
                    name = "Ursula Le Guin",
                ),
            )
            result.usersCount shouldBe 250
            result.ratingsCount shouldBe 77
            result.isCompilation shouldBe true
            result.description shouldBe "A description"
            result.headline shouldBe "A headline"
        }

        @Test
        fun `falls back to placeholders when the book list fragment has missing fields`() {
            // ----- Arrange -----
            val fragment = stubUserBookFragment()

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.title shouldBe ""
            result.rating shouldBe 0.0
            result.description shouldBe ""
            result.headline shouldBe ""
            result.releaseYear shouldBe -1
            result.releaseDate.shouldBeNull()
            result.coverUrl shouldBe ""
            result.canonicalId.shouldBeNull()
        }

        @Test
        fun `has no editions when the user book has no edition`() {
            // ----- Arrange -----
            val fragment = stubUserBookFragment(editionFragment = null)

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.editions shouldBe emptyList()
            result.defaultEdition.shouldBeNull()
        }

        @Test
        fun `maps the edition with the book authors and re-keys it to the parent book id`() {
            // Canonicalized editions can carry a book_id that points at a different book than the
            // parent list entry. The mapper must use the parent's id so that Room's relation join on
            // book_editions.bookId = books.id keeps the edition attached to the right book.
            // ----- Arrange -----
            val author = mockk<BookListFragment.Contribution.Author> {
                every {
                    id
                } returns 3
                every {
                    name
                } returns "Ursula Le Guin"
            }
            val contribution = mockk<BookListFragment.Contribution> {
                every {
                    this@mockk.author
                } returns author
            }
            val fragment = stubUserBookFragment(
                bookListFragment = stubBookListFragment(
                    id = 100,
                    contributions = listOf(contribution),
                ),
                editionFragment = stubEditionFragment(
                    id = 10,
                    bookId = 999,
                ),
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            val edition = result.editions.single()

            edition.id shouldBe 10
            edition.bookId shouldBe 100
            edition.authors shouldBe result.authors
            result.defaultEdition.shouldBeNull()
        }

        @Test
        fun `sets canonicalId when the canonical id differs from the book id`() {
            // ----- Arrange -----
            val fragment = stubUserBookFragment(
                bookListFragment = stubBookListFragment(
                    id = 100,
                    canonicalId = 999,
                ),
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.canonicalId shouldBe 999
        }

        @Test
        fun `clears canonicalId when the canonical id equals the book id`() {
            // ----- Arrange -----
            val fragment = stubUserBookFragment(
                bookListFragment = stubBookListFragment(
                    id = 100,
                    canonicalId = 100,
                ),
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.canonicalId.shouldBeNull()
        }

        @Test
        fun `maps the user book fields from the fragment`() {
            // ----- Arrange -----
            val fragment = stubUserBookFragment(
                statusId = 3,
                editionId = 10,
                lastReadDate = "2024-02-02",
                privacySettingId = 2,
                rating = 4.5,
                referrerUserId = 77,
                reviewHasSpoilers = true,
                reviewedAt = "2024-03-03",
                updatedAt = "2024-04-04",
                createdAt = "2024-05-20",
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            val userBook = result.userBook.shouldNotBeNull()

            userBook.id shouldBe 1
            userBook.status shouldBe BookStatus.Read
            userBook.editionId shouldBe 10
            userBook.lastReadDate shouldBe "2024-02-02"
            userBook.dateAdded shouldBe "2024-01-01"
            userBook.privacySettingId shouldBe 2
            userBook.rating shouldBe 4.5
            userBook.referrerUserId shouldBe 77
            userBook.reviewHasSpoilers shouldBe true
            userBook.reviewedAt shouldBe "2024-03-03"
            userBook.updatedAt shouldBe "2024-04-04"
            userBook.createdAt shouldBe "2024-05-20"
        }

        @Test
        fun `collects journals from all four aliased lists in order`() {
            // ----- Arrange -----
            val progress = mockk<UserBookFragment.Progress_updated_journal> {
                every {
                    with(UserBookFragment.Progress_updated_journal.Companion) { progressUpdatedJournalFragment() }
                } returns stubReadingJournalFragment(event = "progress_updated")
            }
            val started = mockk<UserBookFragment.User_book_read_started_journal> {
                every {
                    with(UserBookFragment.User_book_read_started_journal.Companion) {
                        userBookReadStartedJournalFragment()
                    }
                } returns stubReadingJournalFragment(event = "user_book_read_started")
            }
            val finished = mockk<UserBookFragment.User_book_read_finished_journal> {
                every {
                    with(UserBookFragment.User_book_read_finished_journal.Companion) {
                        userBookReadFinishedJournalFragment()
                    }
                } returns stubReadingJournalFragment(event = "user_book_read_finished")
            }
            val stopped = mockk<UserBookFragment.Status_stopped_journal> {
                every {
                    with(UserBookFragment.Status_stopped_journal.Companion) { statusStoppedJournalFragment() }
                } returns stubReadingJournalFragment(event = "status_stopped")
            }
            val fragment = stubUserBookFragment(
                progressUpdated = listOf(progress),
                userBookReadStarted = listOf(started),
                userBookReadFinished = listOf(finished),
                statusStopped = listOf(stopped),
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.userBook.shouldNotBeNull().journals.map { it.event } shouldBe listOf(
                "progress_updated",
                "user_book_read_started",
                "user_book_read_finished",
                "status_stopped",
            )
        }

        @Test
        fun `turns a non-empty review_slate into a review document`() {
            // ----- Arrange -----
            val slate = listOf(
                mapOf(
                    "data" to emptyMap<String, Any?>(),
                    "type" to "paragraph",
                    "object" to "block",
                    "children" to listOf(
                        mapOf(
                            "object" to "text",
                            "text" to "Loved it",
                        ),
                    ),
                ),
            )
            val fragment = stubUserBookFragment(reviewSlate = slate)

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            val review = result.userBook.shouldNotBeNull().reviewDocument.shouldNotBeNull()

            review.paragraphs.single().runs.single().text shouldBe "Loved it"
        }

        @Test
        fun `leaves the review document null when the review_slate is empty`() {
            // ----- Arrange -----
            val fragment = stubUserBookFragment(reviewSlate = emptyList())

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.userBook.shouldNotBeNull().reviewDocument.shouldBeNull()
        }

        @Test
        fun `maps taggable counts to tags, skipping missing tags and defaulting a missing category to OTHER`() {
            // ----- Arrange -----
            val genreTag = stubTaggableCount(
                tagId = 7L,
                tagName = "Science Fiction",
                category = "Genre",
                count = 15,
            )
            val uncategorisedTag = stubTaggableCount(
                tagId = 8L,
                tagName = "Cozy",
                category = null,
                count = 4,
            )
            val missingTag = mockk<BookListFragment.Taggable_count> {
                every {
                    count
                } returns 1
                every {
                    tag
                } returns null
            }
            val fragment = stubUserBookFragment(
                bookListFragment = stubBookListFragment(
                    taggableCounts = listOf(
                        genreTag,
                        missingTag,
                        uncategorisedTag,
                    ),
                ),
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.tags.map { it.id } shouldBe listOf(7, 8)
            result.tags.map { it.name } shouldBe listOf("Science Fiction", "Cozy")
            result.tags.map { it.category } shouldBe listOf(TagCategory.GENRE, TagCategory.OTHER)
            result.tags.map { it.count } shouldBe listOf(15, 4)
        }

        private fun stubReadingJournalFragment(event: String): ReadingJournalFragment = mockk {
            every {
                this@mockk.event
            } returns event
            every {
                updated_at
            } returns "2024-01-01"
        }

        private fun stubTaggableCount(
            tagId: Long,
            tagName: String,
            category: String?,
            count: Int,
        ): BookListFragment.Taggable_count {
            val tagCategory = mockk<BookListFragment.Taggable_count.Tag.Tag_category> {
                every {
                    this@mockk.category
                } returns category
            }
            val tag = mockk<BookListFragment.Taggable_count.Tag> {
                every {
                    id
                } returns tagId
                every {
                    tag
                } returns tagName
                every {
                    tag_category
                } returns tagCategory
            }

            return mockk {
                every {
                    this@mockk.count
                } returns count
                every {
                    this@mockk.tag
                } returns tag
            }
        }
    }

    @Nested
    inner class BookDetailFragmentToBook {
        @Test
        fun `has no editions when there is no default cover edition`() {
            // ----- Arrange -----
            val fragment = stubBookDetailFragment(defaultCoverEdition = null)

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.editions shouldBe emptyList()
            result.defaultEdition.shouldBeNull()
            result.description shouldBe ""
            result.userBook.shouldBeNull()
            result.userBookRead.shouldBeNull()
        }

        @Test
        fun `uses the default cover edition as the only edition, re-keyed to the parent book id`() {
            // ----- Arrange -----
            val fragment = stubBookDetailFragment(
                defaultCoverEdition = stubEditionFragment(
                    id = 10,
                    bookId = 999,
                ),
                description = "A great book.",
                usersCount = 250,
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            val edition = result.editions.single()

            edition.id shouldBe 10
            edition.bookId shouldBe 1
            result.defaultEdition shouldBe edition
            result.description shouldBe "A great book."
            result.usersCount shouldBe 250
        }
    }

    @Nested
    inner class PositionDetailsParsing {
        private fun bookWithSeries(
            details: String?,
            position: Double?,
        ): BookDetailFragment {
            val series = mockk<BookDetailFragment.Book_series> {
                every {
                    this@mockk.details
                } returns details
                every {
                    this@mockk.position
                } returns position
                every {
                    this@mockk.series
                } returns null
                every {
                    __typename
                } returns "book_series"
            }

            return stubBookDetailFragment(bookSeries = listOf(series))
        }

        @Test
        fun `details "1" yields single-element list`() {
            // ----- Arrange -----
            val fragment = bookWithSeries(
                details = "1",
                position = null,
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.positionsInSeries shouldBe listOf(1.0)
        }

        @Test
        fun `details "1-3" expands to 1, 2, 3`() {
            // ----- Arrange -----
            val fragment = bookWithSeries(
                details = "1-3",
                position = null,
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.positionsInSeries shouldBe listOf(1.0, 2.0, 3.0)
        }

        @Test
        fun `details 1point5 yields single-element list`() {
            // ----- Arrange -----
            val fragment = bookWithSeries(
                details = "1.5",
                position = null,
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.positionsInSeries shouldBe listOf(1.5)
        }

        @Test
        fun `details "1point5-2point5" yields endpoints without fan-out`() {
            // ----- Arrange -----
            val fragment = bookWithSeries(
                details = "1.5-2.5",
                position = null,
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.positionsInSeries shouldBe listOf(1.5, 2.5)
        }

        @Test
        fun `unparseable details falls back to position`() {
            // ----- Arrange -----
            val fragment = bookWithSeries(
                details = "abc",
                position = 3.0,
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.positionsInSeries shouldBe listOf(3.0)
        }

        @Test
        fun `null details falls back to position`() {
            // ----- Arrange -----
            val fragment = bookWithSeries(
                details = null,
                position = 2.0,
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.positionsInSeries shouldBe listOf(2.0)
        }

        @Test
        fun `empty book_series list yields empty positions`() {
            // ----- Arrange -----
            val fragment = stubBookDetailFragment(bookSeries = emptyList())

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.positionsInSeries shouldBe emptyList()
        }

        @Test
        fun `inverted range "3-1" falls back to position`() {
            // ----- Arrange -----
            val fragment = bookWithSeries(
                details = "3-1",
                position = 1.0,
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.positionsInSeries shouldBe listOf(1.0)
        }
    }

    @Nested
    inner class UserBookReadMapping {
        private fun stubRead(
            id: Int,
            progress: Double?,
            progressPages: Int? = null,
            progressSeconds: Int? = null,
            startedAt: String? = null,
            finishedAt: String? = null,
        ): UserBookFragment.User_book_read = mockk {
            every {
                this@mockk.id
            } returns id
            every {
                this@mockk.progress
            } returns progress
            every {
                progress_pages
            } returns progressPages
            every {
                progress_seconds
            } returns progressSeconds
            every {
                started_at
            } returns startedAt
            every {
                finished_at
            } returns finishedAt
        }

        @Test
        fun `has no userBookRead when the user book has no reads`() {
            // ----- Arrange -----
            val fragment = stubUserBookFragment(userBookReads = emptyList())

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.userBookRead.shouldBeNull()
        }

        @Test
        fun `maps the first read and ignores later ones`() {
            // ----- Arrange -----
            val firstRead = stubRead(
                id = 7,
                progress = 42.5,
                progressPages = 120,
                progressSeconds = 3600,
                startedAt = "2024-01-02",
                finishedAt = "2024-02-03",
            )
            val laterRead = stubRead(
                id = 8,
                progress = 100.0,
            )
            val fragment = stubUserBookFragment(userBookReads = listOf(firstRead, laterRead))

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            val userBookRead = result.userBookRead.shouldNotBeNull()

            userBookRead.id shouldBe 7
            userBookRead.progress shouldBe 42.5f
            userBookRead.currentPage shouldBe 120
            userBookRead.currentSeconds shouldBe 3600
            userBookRead.startedAt shouldBe "2024-01-02"
            userBookRead.finishedAt shouldBe "2024-02-03"
        }

        @Test
        fun `a missing progress maps to zero`() {
            // ----- Arrange -----
            val fragment = stubUserBookFragment(
                userBookReads = listOf(
                    stubRead(
                        id = 7,
                        progress = null,
                    ),
                ),
            )

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.userBookRead.shouldNotBeNull().progress shouldBe 0f
        }
    }

    @Nested
    inner class SeriesMapping {
        private fun bookWithSeries(series: BookDetailFragment.Book_series.Series?): BookDetailFragment {
            val bookSeries = mockk<BookDetailFragment.Book_series> {
                every {
                    details
                } returns null
                every {
                    position
                } returns 1.0
                every {
                    this@mockk.series
                } returns series
                every {
                    __typename
                } returns "book_series"
            }

            return stubBookDetailFragment(bookSeries = listOf(bookSeries))
        }

        private fun stubSeries(primaryBooksCount: Int?): BookDetailFragment.Book_series.Series = mockk {
            every {
                id
            } returns 9
            every {
                name
            } returns "The Expanse"
            every {
                primary_books_count
            } returns primaryBooksCount
        }

        @Test
        fun `maps the series id, name and primary book count`() {
            // ----- Arrange -----
            val fragment = bookWithSeries(series = stubSeries(primaryBooksCount = 9))

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            val bookSeries = result.bookSeries.shouldNotBeNull()

            bookSeries.id shouldBe 9
            bookSeries.name shouldBe "The Expanse"
            bookSeries.amountOfBooks shouldBe 9
        }

        @Test
        fun `a missing primary book count maps to zero books`() {
            // ----- Arrange -----
            val fragment = bookWithSeries(series = stubSeries(primaryBooksCount = null))

            // ----- Act -----
            val result = fragment.toBook().shouldNotBeNull()

            // ----- Assert -----
            result.bookSeries.shouldNotBeNull().amountOfBooks shouldBe 0
        }

        @Test
        fun `has no bookSeries when the series relation is missing or the list is empty`() {
            // ----- Arrange -----
            val withoutRelation = bookWithSeries(series = null)
            val withoutEntries = stubBookDetailFragment(bookSeries = emptyList())

            // ----- Act -----
            val relationResult = withoutRelation.toBook().shouldNotBeNull()
            val entriesResult = withoutEntries.toBook().shouldNotBeNull()

            // ----- Assert -----
            relationResult.bookSeries.shouldBeNull()
            entriesResult.bookSeries.shouldBeNull()
        }
    }
}
