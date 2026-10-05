package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
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
            val state = BookDetailUiState(
                bookId = BOOK_ID,
                transitionSurface = SURFACE,
                initialCover = initialCover(COVER_URL),
            )

            // ----- Act -----
            val result = state.withCoverModels()

            // ----- Assert -----
            val hero = result.heroCover.shouldNotBeNull()
            val backdrop = result.heroBackdropCover.shouldNotBeNull()

            hero.sharedTransitionKey shouldBe bookCoverTransitionKey(
                editionId = null,
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
            val state = BookDetailUiState(
                bookId = BOOK_ID,
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
            val state = BookDetailUiState(
                bookId = BOOK_ID,
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
            val state = BookDetailUiState(
                bookId = BOOK_ID,
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
            val state = BookDetailUiState(
                bookId = BOOK_ID,
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
        fun `without a bookId or edition the hero has no transition key`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                bookId = null,
                transitionSurface = SURFACE,
                initialCover = initialCover(COVER_URL),
            )

            // ----- Act -----
            val result = state.withCoverModels()

            // ----- Assert -----
            result.heroCover.shouldNotBeNull().sharedTransitionKey.shouldBeNull()
        }
    }

    @Nested
    inner class ToCoverModels {
        private fun snapshot(
            book: Book?,
            loadingBookDetails: Boolean,
        ): CoverModelsSnapshot = CoverModelsSnapshot(
            book = book,
            displayedEdition = null,
            editions = emptyList(),
            initialCover = null,
            loadingBookDetails = loadingBookDetails,
            bookId = BOOK_ID,
            transitionSurface = null,
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
    }
}
