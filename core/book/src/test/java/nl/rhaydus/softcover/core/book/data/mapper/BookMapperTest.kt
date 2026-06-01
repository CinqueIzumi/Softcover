package nl.rhaydus.softcover.core.book.data.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import java.time.LocalDate
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.fragment.BookDetailFragment
import nl.rhaydus.softcover.fragment.BookListFragment
import nl.rhaydus.softcover.fragment.BookListFragment.Taggable_count as BookListTaggableCount
import nl.rhaydus.softcover.fragment.BookListFragment.Taggable_count.Tag as BookListTaggableTag
import nl.rhaydus.softcover.fragment.BookListFragment.Taggable_count.Tag.Tag_category as BookListTagCategory
import nl.rhaydus.softcover.fragment.EditionDetailFragment
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.ReadingJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Progress_updated_journal.Companion.readingJournalFragment as progressUpdatedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Status_stopped_journal.Companion.readingJournalFragment as statusStoppedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read_finished_journal.Companion.readingJournalFragment as userBookReadFinishedJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read_started_journal.Companion.readingJournalFragment as userBookReadStartedJournalFragment
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookMapperTest {

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Nested
    inner class EditionFragmentToBookEdition {

        @Test
        fun `maps all scalar fields from EditionFragment to BookEdition`() {
            // ----- Arrange -----
            val publisher = mockk<EditionFragment.Publisher> {
                every { name } returns "Penguin"
            }

            val image = mockk<EditionFragment.Image> {
                every { url } returns "https://example.com/cover.jpg"
            }

            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns "Edition Title"
                every { book_id } returns 1
                every { isbn_10 } returns "1234567890"
                every { isbn_13 } returns null
                every { pages } returns 300
                every { this@mockk.publisher } returns publisher
                every { this@mockk.image } returns image
                every { release_year } returns 2020
                every { release_date } returns null
                every { edition_format } returns "Paperback"
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.id shouldBe 10
            result.title shouldBe "Edition Title"
            result.bookId shouldBe 1
            result.isbn10 shouldBe "1234567890"
            result.pages shouldBe 300
            result.publisher shouldBe "Penguin"
            result.url shouldBe "https://example.com/cover.jpg"
            result.releaseYear shouldBe 2020
            result.format shouldBe "Paperback"
            result.owned shouldBe false
        }

        @Test
        fun `maps authors from provided list parameter`() {
            // ----- Arrange -----
            val author = Author(id = 7, name = "Jane Austen")

            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition(authors = listOf(author))

            // ----- Assert -----
            result.authors.size shouldBe 1
            result.authors[0].id shouldBe 7
            result.authors[0].name shouldBe "Jane Austen"
        }

        @Test
        fun `maps null publisher name as null`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.publisher shouldBe null
        }

        @Test
        fun `url uses image url when image is present`() {
            // ----- Arrange -----
            val image = mockk<EditionFragment.Image> {
                every { url } returns "https://example.com/primary.jpg"
            }

            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { this@mockk.image } returns image
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url shouldBe "https://example.com/primary.jpg"
        }

        @Test
        fun `url falls back to first fallbackImages url when image is null`() {
            // ----- Arrange -----
            val fallback = mockk<EditionFragment.FallbackImage> {
                every { url } returns "https://example.com/fallback.jpg"
            }

            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns listOf(fallback)
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url shouldBe "https://example.com/fallback.jpg"
        }

        @Test
        fun `url is null when image is null and fallbackImages is empty`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url shouldBe null
        }

        @Test
        fun `url is null when image is null and all fallbackImages have null url`() {
            // ----- Arrange -----
            val fallback = mockk<EditionFragment.FallbackImage> {
                every { url } returns null
            }

            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns listOf(fallback)
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.url shouldBe null
        }

        @Test
        fun `maps null release_year as -1`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.releaseYear shouldBe -1
        }

        @Test
        fun `maps null edition_format as empty string`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.format shouldBe ""
        }

        @Test
        fun `defaults authors to empty list when not supplied`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.authors shouldBe emptyList()
        }

        @Test
        fun `maps non-null canonical_id on fragment to canonicalId on BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns 55
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.canonicalId shouldBe 55
        }

        @Test
        fun `maps null canonical_id on fragment to null canonicalId on BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.canonicalId shouldBe null
        }

        @Test
        fun `maps non-null audio_seconds from fragment to audioSeconds on BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns 3600
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.audioSeconds shouldBe 3600
        }

        @Test
        fun `maps null audio_seconds from fragment to null audioSeconds on BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.audioSeconds shouldBe null
        }

        @Test
        fun `release_date in EditionFragment is parsed into BookEdition releaseDate`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns "2026-05-05"
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.releaseDate shouldBe LocalDate.of(2026, 5, 5)
        }

        @Test
        fun `null release_date maps to null releaseDate`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.releaseDate shouldBe null
        }

        @Test
        fun `malformed release_date maps to null releaseDate`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns "not-a-date"
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.releaseDate shouldBe null
        }

        @Test
        fun `maps non-null isbn_13 from EditionFragment to BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns "9780451524935"
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.isbn13 shouldBe "9780451524935"
        }
    }

    @Nested
    inner class EditionDetailFragmentToBookEdition {

        @Test
        fun `extracts authors from contributions and maps them`() {
            // ----- Arrange -----
            val authorInner = mockk<EditionDetailFragment.Contribution.Author> {
                every { id } returns 5
                every { name } returns "George Orwell"
            }

            val contribution = mockk<EditionDetailFragment.Contribution> {
                every { author } returns authorInner
            }

            val fragment = mockk<EditionDetailFragment> {
                every { id } returns 20
                every { canonical_id } returns null
                every { title } returns "Animal Farm"
                every { book_id } returns 2
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns 112
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns 1945
                every { release_date } returns null
                every { edition_format } returns "Hardcover"
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
                every { contributions } returns listOf(contribution)
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.authors.size shouldBe 1
            result.authors[0].id shouldBe 5
            result.authors[0].name shouldBe "George Orwell"
        }

        @Test
        fun `skips contributions whose author is null`() {
            // ----- Arrange -----
            val contribution = mockk<EditionDetailFragment.Contribution> {
                every { author } returns null
            }

            val fragment = mockk<EditionDetailFragment> {
                every { id } returns 20
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 2
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
                every { contributions } returns listOf(contribution)
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.authors shouldBe emptyList()
        }

        @Test
        fun `produces empty authors list when contributions is empty`() {
            // ----- Arrange -----
            val fragment = mockk<EditionDetailFragment> {
                every { id } returns 20
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 2
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
                every { contributions } returns emptyList()
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.authors shouldBe emptyList()
        }

        @Test
        fun `maps scalar edition fields correctly`() {
            // ----- Arrange -----
            val fragment = mockk<EditionDetailFragment> {
                every { id } returns 77
                every { canonical_id } returns null
                every { title } returns "1984"
                every { book_id } returns 3
                every { isbn_10 } returns "0451524934"
                every { isbn_13 } returns null
                every { pages } returns 328
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns 1949
                every { release_date } returns null
                every { edition_format } returns "Paperback"
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
                every { contributions } returns emptyList()
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.id shouldBe 77
            result.title shouldBe "1984"
            result.bookId shouldBe 3
            result.isbn10 shouldBe "0451524934"
            result.pages shouldBe 328
            result.releaseYear shouldBe 1949
            result.format shouldBe "Paperback"
        }

        @Test
        fun `maps non-null audio_seconds from EditionDetailFragment to audioSeconds on BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionDetailFragment> {
                every { id } returns 20
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 2
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns 7200
                every { reading_format_id } returns 0
                every { contributions } returns emptyList()
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.audioSeconds shouldBe 7200
        }

        @Test
        fun `always sets localImagePath to null on the resulting BookEdition`() {
            // ----- Arrange -----
            val fragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            // ----- Act -----
            val result = fragment.toBookEdition()

            // ----- Assert -----
            result.localImagePath shouldBe null
        }
    }

    @Nested
    inner class UserBookFragmentToBook {

        @Test
        fun `returns null when bookListFragment companion extension returns null`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)

            val bookInner = mockk<UserBookFragment.Book>()
            val fragment = mockk<UserBookFragment> {
                every { book } returns bookInner
            }

            every {
                with(UserBookFragment.Book.Companion) { bookInner.bookListFragment() }
            } returns null

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `maps to Book with no editions when edition is null`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns null
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns null
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns mockk {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "Test"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns null
                every { headline } returns null
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { taggable_counts } returns emptyList()
            }

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result shouldNotBe null
            result!!.editions shouldBe emptyList()
            result.defaultEdition shouldBe null
        }

        @Test
        fun `returns non-null Book when edition is present and all fields map correctly`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns "My Edition"
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns 4.0
                every { image } returns null
                every { release_year } returns 2020
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 250
                every { ratings_count } returns 0
                every { description } returns null
                every { headline } returns null
                every { taggable_counts } returns emptyList()
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.id shouldBe 100
            result?.title shouldBe "My Book"
            result?.defaultEdition shouldBe null
            result?.description shouldBe ""
            result?.usersCount shouldBe 250
            result?.editions?.size shouldBe 1
            result?.editions?.get(0)?.id shouldBe 10
        }

        @Test
        fun `maps description from BookListFragment when non-null`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { description } returns "some description"
                every { headline } returns null
                every { taggable_counts } returns emptyList()
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.description shouldBe "some description"
        }

        @Test
        fun `maps headline from BookListFragment when non-null`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()

            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { description } returns null
                every { headline } returns "A short tagline"
                every { taggable_counts } returns emptyList()
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.headline shouldBe "A short tagline"
        }

        @Test
        fun `maps headline as empty string when null in BookListFragment`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()

            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { description } returns null
                every { headline } returns null
                every { taggable_counts } returns emptyList()
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.headline shouldBe ""
        }

        @Test
        fun `maps usersCount from BookListFragment users_count when non-null`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 1234
                every { ratings_count } returns 0
                every { description } returns null
                every { headline } returns null
                every { taggable_counts } returns emptyList()
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.usersCount shouldBe 1234
        }

        @Test
        fun `sets canonicalId when canonical id differs from book id`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val canonical = mockk<nl.rhaydus.softcover.fragment.BookListFragment.Canonical> {
                every { id } returns 999
            }

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { this@mockk.canonical } returns canonical
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { description } returns null
                every { headline } returns null
                every { taggable_counts } returns emptyList()
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.canonicalId shouldBe 999
        }

        @Test
        fun `sets canonicalId to null when canonical id equals book id`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val canonical = mockk<nl.rhaydus.softcover.fragment.BookListFragment.Canonical> {
                every { id } returns 100
            }

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { this@mockk.canonical } returns canonical
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { description } returns null
                every { headline } returns null
                every { taggable_counts } returns emptyList()
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.canonicalId shouldBe null
        }

        @Test
        fun `overrides edition bookId with parent book id when edition book_id differs`() {
            // ----- Arrange -----
            // Canonicalized editions can have a book_id that points to a different book than the
            // parent list entry. The mapper must copy the parent's id so that Room's @Relation join
            // on book_editions.bookId = books.id keeps the edition attached to the correct book.
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 999 // differs from parent book id
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100 // parent book id
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { description } returns null
                every { headline } returns null
                every { taggable_counts } returns emptyList()
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.editions?.get(0)?.bookId shouldBe 100
        }

        // ---- journal alias tests ----

        private fun stubMinimalUserBookFragment(
            progressUpdated: List<UserBookFragment.Progress_updated_journal> = emptyList(),
            userBookReadStarted: List<UserBookFragment.User_book_read_started_journal> = emptyList(),
            userBookReadFinished: List<UserBookFragment.User_book_read_finished_journal> = emptyList(),
            statusStopped: List<UserBookFragment.Status_stopped_journal> = emptyList(),
            createdAt: String = "2024-01-01",
            ratingsCount: Int = 0,
            reviewSlate: Any = emptyList<Map<String, Any?>>(),
        ): UserBookFragment {
            val bookInner = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }
            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "Test Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 0
                every { ratings_count } returns ratingsCount
                every { description } returns null
                every { headline } returns null
                every { taggable_counts } returns emptyList()
            }

            every {
                with(UserBookFragment.Book.Companion) { bookInner.bookListFragment() }
            } returns bookListFragmentModel
            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            return mockk<UserBookFragment> {
                every { book } returns bookInner
                every { edition } returns editionInner
                every { progress_updated_journal } returns progressUpdated
                every { user_book_read_started_journal } returns userBookReadStarted
                every { user_book_read_finished_journal } returns userBookReadFinished
                every { status_stopped_journal } returns statusStopped
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns reviewSlate
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns createdAt
            }
        }

        private fun stubReadingJournalFragment(event: String, updatedAt: String = "2024-01-01"): ReadingJournalFragment = mockk {
            every { this@mockk.event } returns event
            every { this@mockk.updated_at } returns updatedAt
        }

        @Test
        fun `collects journals from progress_updated_journal alias`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.Progress_updated_journal.Companion)

            val journalFragment = stubReadingJournalFragment(event = "progress_updated")
            val journalEntry = mockk<UserBookFragment.Progress_updated_journal> {
                every { with(UserBookFragment.Progress_updated_journal.Companion) { progressUpdatedJournalFragment() } } returns journalFragment
            }
            val fragment = stubMinimalUserBookFragment(progressUpdated = listOf(journalEntry))

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.journals?.size shouldBe 1
            result?.userBook?.journals?.get(0)?.event shouldBe "progress_updated"
        }

        @Test
        fun `collects journals from user_book_read_started_journal alias`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read_started_journal.Companion)

            val journalFragment = stubReadingJournalFragment(event = "user_book_read_started")
            val journalEntry = mockk<UserBookFragment.User_book_read_started_journal> {
                every { with(UserBookFragment.User_book_read_started_journal.Companion) { userBookReadStartedJournalFragment() } } returns journalFragment
            }
            val fragment = stubMinimalUserBookFragment(userBookReadStarted = listOf(journalEntry))

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.journals?.size shouldBe 1
            result?.userBook?.journals?.get(0)?.event shouldBe "user_book_read_started"
        }

        @Test
        fun `collects journals from user_book_read_finished_journal alias`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read_finished_journal.Companion)

            val journalFragment = stubReadingJournalFragment(event = "user_book_read_finished")
            val journalEntry = mockk<UserBookFragment.User_book_read_finished_journal> {
                every { with(UserBookFragment.User_book_read_finished_journal.Companion) { userBookReadFinishedJournalFragment() } } returns journalFragment
            }
            val fragment = stubMinimalUserBookFragment(userBookReadFinished = listOf(journalEntry))

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.journals?.size shouldBe 1
            result?.userBook?.journals?.get(0)?.event shouldBe "user_book_read_finished"
        }

        @Test
        fun `collects journals from status_stopped_journal alias`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.Status_stopped_journal.Companion)

            val journalFragment = stubReadingJournalFragment(event = "status_stopped")
            val journalEntry = mockk<UserBookFragment.Status_stopped_journal> {
                every { with(UserBookFragment.Status_stopped_journal.Companion) { statusStoppedJournalFragment() } } returns journalFragment
            }
            val fragment = stubMinimalUserBookFragment(statusStopped = listOf(journalEntry))

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.journals?.size shouldBe 1
            result?.userBook?.journals?.get(0)?.event shouldBe "status_stopped"
        }

        @Test
        fun `combines journals from all four aliased lists`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.Progress_updated_journal.Companion)
            mockkObject(UserBookFragment.User_book_read_started_journal.Companion)
            mockkObject(UserBookFragment.User_book_read_finished_journal.Companion)
            mockkObject(UserBookFragment.Status_stopped_journal.Companion)

            val progressEntry = mockk<UserBookFragment.Progress_updated_journal> {
                every { with(UserBookFragment.Progress_updated_journal.Companion) { progressUpdatedJournalFragment() } } returns stubReadingJournalFragment("progress_updated")
            }
            val currentlyReadingEntry = mockk<UserBookFragment.User_book_read_started_journal> {
                every { with(UserBookFragment.User_book_read_started_journal.Companion) { userBookReadStartedJournalFragment() } } returns stubReadingJournalFragment("user_book_read_started")
            }
            val readEntry = mockk<UserBookFragment.User_book_read_finished_journal> {
                every { with(UserBookFragment.User_book_read_finished_journal.Companion) { userBookReadFinishedJournalFragment() } } returns stubReadingJournalFragment("user_book_read_finished")
            }
            val stoppedEntry = mockk<UserBookFragment.Status_stopped_journal> {
                every { with(UserBookFragment.Status_stopped_journal.Companion) { statusStoppedJournalFragment() } } returns stubReadingJournalFragment("status_stopped")
            }
            val fragment = stubMinimalUserBookFragment(
                progressUpdated = listOf(progressEntry),
                userBookReadStarted = listOf(currentlyReadingEntry),
                userBookReadFinished = listOf(readEntry),
                statusStopped = listOf(stoppedEntry),
            )

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            val events = result?.userBook?.journals?.map { it.event }
            events?.size shouldBe 4
            events shouldBe listOf(
                "progress_updated",
                "user_book_read_started",
                "user_book_read_finished",
                "status_stopped",
            )
        }

        @Test
        fun `propagates created_at from fragment to UserBook createdAt`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            val fragment = stubMinimalUserBookFragment(createdAt = "2024-05-20")

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.createdAt shouldBe "2024-05-20"
        }

        @Test
        fun `propagates a different created_at value from fragment to UserBook createdAt`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            val fragment = stubMinimalUserBookFragment(createdAt = "2023-11-01")

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.createdAt shouldBe "2023-11-01"
        }

        @Test
        fun `maps ratings_count from BookListFragment to ratingsCount`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            val fragment = stubMinimalUserBookFragment(ratingsCount = 77)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.ratingsCount shouldBe 77
        }

        @Test
        fun `propagates non-empty review_slate from fragment to UserBook as ReviewDocument`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)

            val slate = listOf(
                mapOf(
                    "data" to emptyMap<String, Any?>(),
                    "type" to "paragraph",
                    "object" to "block",
                    "children" to listOf(mapOf("object" to "text", "text" to "Loved it")),
                ),
            )

            val fragment = stubMinimalUserBookFragment(reviewSlate = slate)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.reviewDocument?.paragraphs?.first()?.runs?.first()?.text shouldBe "Loved it"
        }

        @Test
        fun `propagates empty review_slate from fragment to UserBook as null reviewDocument`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)

            val fragment = stubMinimalUserBookFragment(reviewSlate = emptyList<Map<String, Any?>>())

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.userBook?.reviewDocument shouldBe null
        }

        @Test
        fun `taggable_counts entries map category and count onto Book tags`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val tagCategory = mockk<BookListTagCategory> {
                every { this@mockk.category } returns "Genre"
            }

            val tag = mockk<BookListTaggableTag> {
                every { id } returns 7L
                every { tag } returns "Science Fiction"
                every { tag_category } returns tagCategory
            }

            val taggableCountGenre = mockk<BookListTaggableCount> {
                every { count } returns 15
                every { this@mockk.tag } returns tag
            }

            val nullTagEntry = mockk<BookListTaggableCount> {
                every { count } returns 1
                every { this@mockk.tag } returns null
            }

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()

            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { description } returns null
                every { headline } returns null
                every { taggable_counts } returns listOf(taggableCountGenre, nullTagEntry)
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.tags?.size shouldBe 1
            result?.tags?.get(0)?.id shouldBe 7
            result?.tags?.get(0)?.name shouldBe "Science Fiction"
            result?.tags?.get(0)?.category shouldBe TagCategory.GENRE
            result?.tags?.get(0)?.count shouldBe 15
        }

        @Test
        fun `taggable_counts entry with null tag_category category maps to TagCategory OTHER`() {
            // ----- Arrange -----
            mockkObject(UserBookFragment.Book.Companion)
            mockkObject(UserBookFragment.Edition.Companion)
            mockkObject(UserBookFragment.User_book_read.Companion)

            val tagCategory = mockk<BookListTagCategory> {
                every { this@mockk.category } returns null
            }

            val tag = mockk<BookListTaggableTag> {
                every { id } returns 3L
                every { tag } returns "Dark"
                every { tag_category } returns tagCategory
            }

            val taggableCount = mockk<BookListTaggableCount> {
                every { count } returns 2
                every { this@mockk.tag } returns tag
            }

            val bookListFragment = mockk<UserBookFragment.Book>()
            val editionInner = mockk<UserBookFragment.Edition>()

            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 100
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val bookListFragmentModel = mockk<nl.rhaydus.softcover.fragment.BookListFragment> {
                every { id } returns 100
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { description } returns null
                every { headline } returns null
                every { taggable_counts } returns listOf(taggableCount)
            }

            val fragment = mockk<UserBookFragment> {
                every { book } returns bookListFragment
                every { edition } returns editionInner
                every { progress_updated_journal } returns emptyList()
                every { user_book_read_started_journal } returns emptyList()
                every { user_book_read_finished_journal } returns emptyList()
                every { status_stopped_journal } returns emptyList()
                every { user_book_reads } returns emptyList()
                every { id } returns 1
                every { status_id } returns 1
                every { edition_id } returns 10
                every { last_read_date } returns null
                every { date_added } returns "2024-01-01"
                every { privacy_setting_id } returns 1
                every { rating } returns null
                every { referrer_user_id } returns null
                every { review_has_spoilers } returns false
                every { review_slate } returns emptyList<Map<String, Any?>>()
                every { reviewed_at } returns null
                every { updated_at } returns null
                every { created_at } returns "2024-01-01"
            }

            every {
                with(UserBookFragment.Book.Companion) { bookListFragment.bookListFragment() }
            } returns bookListFragmentModel

            every {
                with(UserBookFragment.Edition.Companion) { editionInner.editionFragment() }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.tags?.get(0)?.category shouldBe TagCategory.OTHER
        }
    }

    @Nested
    inner class BookDetailFragmentToBook {

        @Test
        fun `maps to Book with no editions when default_cover_edition is null`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns null
                every { headline } returns null
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { default_cover_edition } returns null
                every { taggable_counts } returns emptyList()
            }

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result shouldNotBe null
            result!!.editions shouldBe emptyList()
            result.defaultEdition shouldBe null
        }

        @Test
        fun `returns non-null Book when default_cover_edition is present`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)

            val defaultEditionInner = mockk<BookDetailFragment.Default_cover_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns "Hardcover Edition"
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns 400
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns 2021
                every { release_date } returns null
                every { edition_format } returns "Hardcover"
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns 4.5
                every { image } returns null
                every { release_year } returns 2021
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns "A great book."
                every { headline } returns null
                every { users_count } returns 500
                every { ratings_count } returns 0
                every { default_cover_edition } returns defaultEditionInner
                every { taggable_counts } returns emptyList()
            }

            every {
                with(BookDetailFragment.Default_cover_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.id shouldBe 1
            result?.title shouldBe "My Book"
            result?.description shouldBe "A great book."
            result?.usersCount shouldBe 500
            result?.userBook shouldBe null
            result?.userBookRead shouldBe null
            result?.defaultEdition?.id shouldBe 10
            result?.editions?.size shouldBe 1
            result?.editions?.get(0)?.id shouldBe 10
        }

        @Test
        fun `maps description as empty string when null`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)

            val defaultEditionInner = mockk<BookDetailFragment.Default_cover_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { canonical } returns null
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns null
                every { headline } returns null
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { default_cover_edition } returns defaultEditionInner
                every { taggable_counts } returns emptyList()
            }

            every {
                with(BookDetailFragment.Default_cover_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.description shouldBe ""
        }

        @Test
        fun `maps headline from BookDetailFragment when non-null`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)

            val defaultEditionInner = mockk<BookDetailFragment.Default_cover_edition>()

            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { canonical } returns null
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns null
                every { headline } returns "A gripping thriller"
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { default_cover_edition } returns defaultEditionInner
                every { taggable_counts } returns emptyList()
            }

            every {
                with(BookDetailFragment.Default_cover_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.headline shouldBe "A gripping thriller"
        }

        @Test
        fun `maps headline as empty string when null in BookDetailFragment`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)

            val defaultEditionInner = mockk<BookDetailFragment.Default_cover_edition>()

            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { canonical } returns null
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns null
                every { headline } returns null
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { default_cover_edition } returns defaultEditionInner
                every { taggable_counts } returns emptyList()
            }

            every {
                with(BookDetailFragment.Default_cover_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.headline shouldBe ""
        }

        @Test
        fun `sets canonicalId when canonical id differs from book id`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)

            val canonical = mockk<BookDetailFragment.Canonical> {
                every { id } returns 999
            }

            val defaultEditionInner = mockk<BookDetailFragment.Default_cover_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { this@mockk.canonical } returns canonical
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns null
                every { headline } returns null
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { default_cover_edition } returns defaultEditionInner
                every { taggable_counts } returns emptyList()
            }

            every {
                with(BookDetailFragment.Default_cover_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.canonicalId shouldBe 999
        }

        @Test
        fun `sets canonicalId to null when canonical id equals book id`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)

            val canonical = mockk<BookDetailFragment.Canonical> {
                every { id } returns 1
            }

            val defaultEditionInner = mockk<BookDetailFragment.Default_cover_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { this@mockk.canonical } returns canonical
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns null
                every { headline } returns null
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { default_cover_edition } returns defaultEditionInner
                every { taggable_counts } returns emptyList()
            }

            every {
                with(BookDetailFragment.Default_cover_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.canonicalId shouldBe null
        }

        @Test
        fun `overrides edition bookId with parent book id when edition book_id differs`() {
            // ----- Arrange -----
            // Canonicalized editions can have a book_id that points to a different book than the
            // parent list entry. The mapper must copy the parent's id so that Room's @Relation join
            // on book_editions.bookId = books.id keeps the edition attached to the correct book.
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)

            val defaultEditionInner = mockk<BookDetailFragment.Default_cover_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 999 // differs from parent book id
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1 // parent book id
                every { canonical } returns null
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns null
                every { headline } returns null
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { default_cover_edition } returns defaultEditionInner
                every { taggable_counts } returns emptyList()
            }

            every {
                with(BookDetailFragment.Default_cover_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.defaultEdition?.bookId shouldBe 1
            result?.editions?.get(0)?.bookId shouldBe 1
        }

        @Test
        fun `maps ratings_count from BookDetailFragment to ratingsCount`() {
            // ----- Arrange -----
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)

            val defaultEditionInner = mockk<BookDetailFragment.Default_cover_edition>()
            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns null
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { canonical } returns null
                every { title } returns null
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns emptyList()
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns null
                every { headline } returns null
                every { users_count } returns 0
                every { ratings_count } returns 312
                every { default_cover_edition } returns defaultEditionInner
                every { taggable_counts } returns emptyList()
            }

            every {
                with(BookDetailFragment.Default_cover_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.ratingsCount shouldBe 312
        }
    }

    @Nested
    inner class BookEditionIsAudiobook {

        @Test
        fun `returns true when audioSeconds is positive`() {
            // ----- Arrange -----
            val edition = BookEdition(
                id = 1,
                canonicalId = null,
                bookId = 1,
                publisher = null,
                title = null,
                url = null,
                localImagePath = null,
                isbn10 = null,
                isbn13 = null,
                pages = null,
                audioSeconds = 3600,
                authors = emptyList(),
                releaseYear = 2020,
                format = "Audiobook",
                owned = false,
            )

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe true
        }

        @Test
        fun `returns false when audioSeconds is zero`() {
            // ----- Arrange -----
            val edition = BookEdition(
                id = 1,
                canonicalId = null,
                bookId = 1,
                publisher = null,
                title = null,
                url = null,
                localImagePath = null,
                isbn10 = null,
                isbn13 = null,
                pages = null,
                audioSeconds = 0,
                authors = emptyList(),
                releaseYear = 2020,
                format = "Audiobook",
                owned = false,
            )

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe false
        }

        @Test
        fun `returns false when audioSeconds is null`() {
            // ----- Arrange -----
            val edition = BookEdition(
                id = 1,
                canonicalId = null,
                bookId = 1,
                publisher = null,
                title = null,
                url = null,
                localImagePath = null,
                isbn10 = null,
                isbn13 = null,
                pages = null,
                audioSeconds = null,
                authors = emptyList(),
                releaseYear = 2020,
                format = "Paperback",
                owned = false,
            )

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe false
        }
    }

    @Nested
    inner class PositionDetailsParsing {

        private fun buildBookDetailFragment(
            bookSeriesEntry: BookDetailFragment.Book_series?,
        ): BookDetailFragment {
            mockkObject(BookDetailFragment.Default_cover_edition.Companion)

            val defaultEditionInner = mockk<BookDetailFragment.Default_cover_edition>()

            val editionFragment = mockk<EditionFragment> {
                every { id } returns 10
                every { canonical_id } returns null
                every { title } returns "Title"
                every { book_id } returns 1
                every { isbn_10 } returns null
                every { isbn_13 } returns null
                every { pages } returns null
                every { publisher } returns null
                every { image } returns null
                every { fallbackImages } returns emptyList()
                every { release_year } returns null
                every { release_date } returns null
                every { edition_format } returns null
                every { audio_seconds } returns null
                every { reading_format_id } returns 0
            }

            val seriesList = listOfNotNull(bookSeriesEntry)

            val fragment = mockk<BookDetailFragment> {
                every { id } returns 1
                every { canonical } returns null
                every { title } returns "My Book"
                every { rating } returns null
                every { image } returns null
                every { release_year } returns null
                every { release_date } returns null
                every { book_series } returns seriesList
                every { compilation } returns false
                every { contributions } returns emptyList()
                every { description } returns null
                every { headline } returns null
                every { users_count } returns 0
                every { ratings_count } returns 0
                every { default_cover_edition } returns defaultEditionInner
                every { taggable_counts } returns emptyList()
            }

            every {
                with(BookDetailFragment.Default_cover_edition.Companion) {
                    defaultEditionInner.editionFragment()
                }
            } returns editionFragment

            return fragment
        }

        private fun bookSeriesEntry(
            details: String?,
            position: Double?,
        ): BookDetailFragment.Book_series = mockk<BookDetailFragment.Book_series> {
            every { this@mockk.details } returns details
            every { this@mockk.position } returns position
            every { this@mockk.series } returns null
            every { this@mockk.__typename } returns "book_series"
        }

        @Test
        fun `details "1" yields single-element list`() {
            // ----- Arrange -----
            val entry = bookSeriesEntry(details = "1", position = null)
            val fragment = buildBookDetailFragment(bookSeriesEntry = entry)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.positionsInSeries shouldBe listOf(1.0)
        }

        @Test
        fun `details "1-3" expands to 1, 2, 3`() {
            // ----- Arrange -----
            val entry = bookSeriesEntry(details = "1-3", position = null)
            val fragment = buildBookDetailFragment(bookSeriesEntry = entry)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.positionsInSeries shouldBe listOf(1.0, 2.0, 3.0)
        }

        @Test
        fun `details 1point5 yields single-element list`() {
            // ----- Arrange -----
            val entry = bookSeriesEntry(details = "1.5", position = null)
            val fragment = buildBookDetailFragment(bookSeriesEntry = entry)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.positionsInSeries shouldBe listOf(1.5)
        }

        @Test
        fun `details "1point5-2point5" yields endpoints without fan-out`() {
            // ----- Arrange -----
            val entry = bookSeriesEntry(details = "1.5-2.5", position = null)
            val fragment = buildBookDetailFragment(bookSeriesEntry = entry)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.positionsInSeries shouldBe listOf(1.5, 2.5)
        }

        @Test
        fun `unparseable details falls back to position`() {
            // ----- Arrange -----
            val entry = bookSeriesEntry(details = "abc", position = 3.0)
            val fragment = buildBookDetailFragment(bookSeriesEntry = entry)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.positionsInSeries shouldBe listOf(3.0)
        }

        @Test
        fun `null details falls back to position`() {
            // ----- Arrange -----
            val entry = bookSeriesEntry(details = null, position = 2.0)
            val fragment = buildBookDetailFragment(bookSeriesEntry = entry)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.positionsInSeries shouldBe listOf(2.0)
        }

        @Test
        fun `empty book_series list yields empty positions`() {
            // ----- Arrange -----
            val fragment = buildBookDetailFragment(bookSeriesEntry = null)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.positionsInSeries shouldBe emptyList()
        }

        @Test
        fun `inverted range "3-1" falls back to position`() {
            // ----- Arrange -----
            val entry = bookSeriesEntry(details = "3-1", position = 1.0)
            val fragment = buildBookDetailFragment(bookSeriesEntry = entry)

            // ----- Act -----
            val result = fragment.toBook()

            // ----- Assert -----
            result?.positionsInSeries shouldBe listOf(1.0)
        }
    }
}
