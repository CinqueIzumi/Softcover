package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val BOOK_ID = 42
private const val SURFACE = "home"
private const val COVER_URL = "https://example.test/cover.jpg"

class CoverModelsSnapshotTest {
    private fun initialCover(fallbackCoverUrl: String?): BookInitialCover = BookInitialCover(
        currentEdition = null,
        defaultEdition = null,
        fallbackCoverUrl = fallbackCoverUrl,
    )

    @Nested
    inner class WithCoverModels {
        @Test
        fun `seeds hero and backdrop from initialCover with the transition key on the hero only`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState(bookId = BOOK_ID).copy(
                transitionSurface = SURFACE,
                initialCover = initialCover(COVER_URL),
            )

            // ----- Act -----
            val result = state.withCoverModels()

            // ----- Assert -----
            val hero = result.heroCover.shouldNotBeNull()
            val backdrop = result.heroBackdropCover.shouldNotBeNull()

            hero.sharedTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Cover,
                bookId = BOOK_ID,
                surface = SURFACE,
            )
            backdrop.sharedTransitionKey.shouldBeNull()
            hero.source shouldBe CoverSource.Remote(
                url = COVER_URL,
                persistEditionId = null,
            )
            backdrop.source shouldBe hero.source
        }

        @Test
        fun `seeds the full-screen cover from the same initialCover as the hero`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState(bookId = BOOK_ID).copy(
                transitionSurface = SURFACE,
                initialCover = initialCover(COVER_URL),
            )

            // ----- Act -----
            val result = state.withCoverModels()

            // ----- Assert -----
            val fullScreen = result.fullScreenCover.shouldNotBeNull()

            fullScreen.source shouldBe result.heroCover.shouldNotBeNull().source
        }

        @Test
        fun `without initialCover or book the covers have no source and are loading while details load`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState(bookId = BOOK_ID).copy(
                loadingBookDetails = true,
                initialCover = null,
            )

            // ----- Act -----
            val result = state.withCoverModels()

            // ----- Assert -----
            val hero = result.heroCover.shouldNotBeNull()
            val backdrop = result.heroBackdropCover.shouldNotBeNull()

            hero.source.shouldBeNull()
            hero.isLoading shouldBe true
            backdrop.isLoading shouldBe true
        }

        @Test
        fun `without initialCover or book the covers are not loading once details finished loading`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState(bookId = BOOK_ID).copy(
                loadingBookDetails = false,
                initialCover = null,
            )

            // ----- Act -----
            val result = state.withCoverModels()

            // ----- Assert -----
            result.heroCover.shouldNotBeNull().isLoading shouldBe false
            result.heroBackdropCover.shouldNotBeNull().isLoading shouldBe false
        }

        @Test
        fun `a resolvable initialCover paints immediately even while details are loading`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState(bookId = BOOK_ID).copy(
                loadingBookDetails = true,
                initialCover = initialCover(COVER_URL),
            )

            // ----- Act -----
            val result = state.withCoverModels()

            // ----- Assert -----
            result.heroCover.shouldNotBeNull().isLoading shouldBe false
            result.heroBackdropCover.shouldNotBeNull().isLoading shouldBe false
        }

        @Test
        fun `seeds the hero title and rating keys on the first frame`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState(bookId = BOOK_ID).copy(
                transitionSurface = SURFACE,
                initialCover = initialCover(COVER_URL),
            )

            // ----- Act -----
            val result = state.withCoverModels()

            // ----- Assert -----
            result.heroTitleTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Title,
                bookId = BOOK_ID,
                surface = SURFACE,
            )
            result.heroRatingTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Rating,
                bookId = BOOK_ID,
                surface = SURFACE,
            )
            result.heroTitleTransitionKey.shouldNotBeNull()
        }
    }

    @Nested
    inner class ToCoverModels {
        private fun snapshot(
            book: Book?,
            loadingBookDetails: Boolean,
            bookId: Int = BOOK_ID,
            transitionSurface: String? = null,
            displayedEdition: BookEdition? = null,
        ): CoverModelsSnapshot = CoverModelsSnapshot(
            book = book,
            displayedEdition = displayedEdition,
            editions = emptyList(),
            initialCover = null,
            loadingBookDetails = loadingBookDetails,
            bookId = bookId,
            transitionSurface = transitionSurface,
        )

        private fun stubBook(): Book = mockk {
            every {
                title
            } returns "Piranesi"

            every {
                defaultEdition
            } returns null

            every {
                coverUrl
            } returns COVER_URL
        }

        @Test
        fun `hero is titled with the book while the backdrop stays untitled`() {
            // ----- Arrange -----
            val snapshot = snapshot(
                book = stubBook(),
                loadingBookDetails = false,
            )

            // ----- Act -----
            val covers = snapshot.toCoverModels()

            // ----- Assert -----
            covers.hero.coverlessTitle shouldBe "Piranesi"
            covers.backdrop.coverlessTitle.shouldBeNull()
        }

        @Test
        fun `a loaded book is not loading while its details refresh`() {
            // ----- Arrange -----
            val snapshot = snapshot(
                book = stubBook(),
                loadingBookDetails = true,
            )

            // ----- Act -----
            val covers = snapshot.toCoverModels()

            // ----- Assert -----
            covers.hero.isLoading shouldBe false
            covers.backdrop.isLoading shouldBe false
        }

        @Test
        fun `full-screen cover resolves from the same fallback as the hero and keeps the book title`() {
            // ----- Arrange -----
            val snapshot = snapshot(
                book = stubBook(),
                loadingBookDetails = false,
            )

            // ----- Act -----
            val covers = snapshot.toCoverModels()

            // ----- Assert -----
            covers.fullScreen.source shouldBe covers.hero.source
            covers.fullScreen.coverlessTitle shouldBe "Piranesi"
        }

        @Test
        fun `full-screen cover carries the same transition key as the hero`() {
            // ----- Arrange -----
            val snapshot = snapshot(
                book = stubBook(),
                loadingBookDetails = false,
            )

            // ----- Act -----
            val covers = snapshot.toCoverModels()

            // ----- Assert -----
            val heroKey = covers.hero.sharedTransitionKey.shouldNotBeNull()

            covers.fullScreen.sharedTransitionKey shouldBe heroKey
        }

        @Test
        fun `full-screen cover is loading while nothing has resolved and details load`() {
            // ----- Arrange -----
            val snapshot = snapshot(
                book = null,
                loadingBookDetails = true,
            )

            // ----- Act -----
            val covers = snapshot.toCoverModels()

            // ----- Assert -----
            covers.hero.isLoading shouldBe true
            covers.fullScreen.isLoading shouldBe true
        }

        @Test
        fun `full-screen cover is not loading once the book has loaded`() {
            // ----- Arrange -----
            val snapshot = snapshot(
                book = stubBook(),
                loadingBookDetails = true,
            )

            // ----- Act -----
            val covers = snapshot.toCoverModels()

            // ----- Assert -----
            covers.hero.isLoading shouldBe false
            covers.fullScreen.isLoading shouldBe false
        }

        @Test
        fun `hero title and rating keys equal the keys a library card computes for the book`() {
            // ----- Arrange -----
            val snapshot = snapshot(
                book = stubBook(),
                loadingBookDetails = false,
                transitionSurface = SURFACE,
            )

            // ----- Act -----
            val covers = snapshot.toCoverModels()

            // ----- Assert -----
            covers.heroTitleTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Title,
                bookId = BOOK_ID,
                surface = SURFACE,
            )
            covers.heroRatingTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Rating,
                bookId = BOOK_ID,
                surface = SURFACE,
            )
        }

        @Test
        fun `cover, title and rating keys are three distinct keys`() {
            // ----- Arrange -----
            val snapshot = snapshot(
                book = stubBook(),
                loadingBookDetails = false,
                transitionSurface = SURFACE,
            )

            // ----- Act -----
            val covers = snapshot.toCoverModels()

            // ----- Assert -----
            val keys = listOf(
                covers.hero.sharedTransitionKey,
                covers.heroTitleTransitionKey,
                covers.heroRatingTransitionKey,
            )

            keys.forEach { it.shouldNotBeNull() }
            keys.toSet().size shouldBe 3
        }

        @Test
        fun `the transition surface threads through to every key`() {
            // ----- Arrange -----
            val onHome = snapshot(
                book = stubBook(),
                loadingBookDetails = false,
                transitionSurface = SURFACE,
            )
            val bare = snapshot(
                book = stubBook(),
                loadingBookDetails = false,
                transitionSurface = null,
            )

            // ----- Act -----
            val surfaced = onHome.toCoverModels()
            val unsurfaced = bare.toCoverModels()

            // ----- Assert -----
            surfaced.hero.sharedTransitionKey shouldNotBe unsurfaced.hero.sharedTransitionKey
            surfaced.heroTitleTransitionKey shouldNotBe unsurfaced.heroTitleTransitionKey
            surfaced.heroRatingTransitionKey shouldNotBe unsurfaced.heroRatingTransitionKey
        }

        @Test
        fun `the bookId keeps the title key stable when the displayed edition changes`() {
            // ----- Arrange -----
            val first = snapshot(
                book = stubBook(),
                loadingBookDetails = false,
                displayedEdition = PreviewData.baseEdition.copy(id = 1),
            )
            val second = snapshot(
                book = stubBook(),
                loadingBookDetails = false,
                displayedEdition = PreviewData.baseEdition.copy(id = 2),
            )

            // ----- Act -----
            val firstCovers = first.toCoverModels()
            val secondCovers = second.toCoverModels()

            // ----- Assert -----
            firstCovers.heroTitleTransitionKey.shouldNotBeNull()
            firstCovers.heroTitleTransitionKey shouldBe secondCovers.heroTitleTransitionKey
            firstCovers.heroRatingTransitionKey shouldBe secondCovers.heroRatingTransitionKey
        }
    }
}
