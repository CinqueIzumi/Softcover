package nl.rhaydus.softcover.feature.reading.presentation.state

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.callout.BannerVariant
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryTone
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.mapper.ALSO_READING_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.reading.presentation.mapper.FEATURED_STAND_IN_BOOK
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingScreenUiStateTest {
    private fun book(id: Int): Book = FEATURED_STAND_IN_BOOK.copy(
        id = id,
        title = "Title $id",
    )

    private fun loadedCard(): BookCardUiModel = ALSO_READING_STAND_IN_CARDS.first().copy(isLoading = false)

    private fun loadedAlsoReading(): BookCardSet<Book> = BookCardSet.standIns(persistentListOf(loadedCard()))

    private fun loadedHeader(): SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "Loaded eyebrow")

    private fun loadedProgress(): FeaturedProgressUiModel = FeaturedProgressUiModel(
        percent = "12%",
        pageLead = "Page 40",
        pageTail = " of 330",
        fraction = 0.12f,
    )

    private fun loadedDeadlineSummary(): DeadlineSummaryUiModel = DeadlineSummaryUiModel(
        text = "28 Aug • 18 pages/day",
        tone = DeadlineSummaryTone.OnHeroBackdrop,
    )

    private fun loadedNudge(): BannerUiModel = BannerUiModel(
        message = "Read 24 pages today to stay on pace.",
        variant = BannerVariant.Editorial,
        visible = true,
    )

    @Nested
    inner class FeaturedBook {
        @Test
        fun `is the loading stand-in while there are no books`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                books = emptyList(),
                isLoading = false,
            )

            // ----- Act & Assert -----
            state.featuredBook shouldBe FEATURED_STAND_IN_BOOK
        }

        @Test
        fun `is the loading stand-in while loading even when there are books`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                books = listOf(book(5)),
                isLoading = true,
            )

            // ----- Act & Assert -----
            state.featuredBook shouldBe FEATURED_STAND_IN_BOOK
        }

        @Test
        fun `is the first book once loaded`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                books = listOf(book(5), book(6)),
                isLoading = false,
            )

            // ----- Act & Assert -----
            state.featuredBook.id shouldBe 5
        }
    }

    @Nested
    inner class IsLoadingFeatured {
        @Test
        fun `is true while there are no books`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                books = emptyList(),
                isLoading = false,
            )

            // ----- Act & Assert -----
            state.isLoadingFeatured shouldBe true
        }

        @Test
        fun `is true while loading even when there is a book`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                books = listOf(book(1)),
                isLoading = true,
            )

            // ----- Act & Assert -----
            state.isLoadingFeatured shouldBe true
        }

        @Test
        fun `is false once loaded with a book`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                books = listOf(book(1)),
                isLoading = false,
            )

            // ----- Act & Assert -----
            state.isLoadingFeatured shouldBe false
        }
    }

    @Nested
    inner class VisibleMasthead {
        @Test
        fun `is a loading stand-in while loading even when built`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                isLoading = true,
                masthead = PageMastheadUiModel(title = "Built"),
            )

            // ----- Act & Assert -----
            state.visibleMasthead.isLoading shouldBe true
            state.visibleMasthead.title.shouldBeNull()
        }

        @Test
        fun `is a loading stand-in when not loading but not yet built`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                isLoading = false,
                masthead = null,
            )

            // ----- Act & Assert -----
            state.visibleMasthead.isLoading shouldBe true
            state.visibleMasthead.title.shouldBeNull()
        }

        @Test
        fun `is the built masthead once loaded`() {
            // ----- Arrange -----
            val built = PageMastheadUiModel(title = "Built")
            val state = ReadingScreenUiState(
                isLoading = false,
                masthead = built,
            )

            // ----- Act & Assert -----
            state.visibleMasthead shouldBe built
        }
    }

    @Nested
    inner class VisibleCompactMasthead {
        @Test
        fun `is a loading stand-in while loading even when built`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                isLoading = true,
                compactMasthead = PageMastheadUiModel(
                    title = "Built",
                    size = PageMastheadSize.Compact,
                ),
            )

            // ----- Act & Assert -----
            state.visibleCompactMasthead.isLoading shouldBe true
            state.visibleCompactMasthead.title.shouldBeNull()
        }

        @Test
        fun `is a loading stand-in when not loading but not yet built`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                isLoading = false,
                compactMasthead = null,
            )

            // ----- Act & Assert -----
            state.visibleCompactMasthead.isLoading shouldBe true
            state.visibleCompactMasthead.size shouldBe PageMastheadSize.Compact
        }

        @Test
        fun `is the built compact masthead once loaded`() {
            // ----- Arrange -----
            val built = PageMastheadUiModel(
                title = "Built",
                size = PageMastheadSize.Compact,
            )
            val state = ReadingScreenUiState(
                isLoading = false,
                compactMasthead = built,
            )

            // ----- Act & Assert -----
            state.visibleCompactMasthead shouldBe built
        }
    }

    @Nested
    inner class VisibleAlsoReading {
        @Test
        fun `is the loading stand-in set while loading even when cards exist`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                isLoading = true,
                alsoReading = loadedAlsoReading(),
            )

            // ----- Act & Assert -----
            state.visibleAlsoReading.cards.shouldNotBeEmpty()
            state.visibleAlsoReading.cards.forEach { it.isLoading shouldBe true }
        }

        @Test
        fun `is the real set once loaded`() {
            // ----- Arrange -----
            val alsoReading = loadedAlsoReading()
            val state = ReadingScreenUiState(
                isLoading = false,
                alsoReading = alsoReading,
            )

            // ----- Act & Assert -----
            state.visibleAlsoReading shouldBe alsoReading
        }
    }

    @Nested
    inner class VisibleAlsoReadingHeader {
        @Test
        fun `is a loading stand-in while loading even when set`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                isLoading = true,
                alsoReadingHeader = loadedHeader(),
            )

            // ----- Act & Assert -----
            state.visibleAlsoReadingHeader.shouldBeInstanceOf<SectionHeaderUiModel.Section>().isLoading shouldBe true
        }

        @Test
        fun `is the real header once loaded`() {
            // ----- Arrange -----
            val header = loadedHeader()
            val state = ReadingScreenUiState(
                isLoading = false,
                alsoReadingHeader = header,
            )

            // ----- Act & Assert -----
            state.visibleAlsoReadingHeader shouldBe header
        }
    }

    @Nested
    inner class VisibleFeaturedProgress {
        @Test
        fun `is the loading stand-in while loading even when set`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                isLoading = true,
                featuredProgress = loadedProgress(),
            )

            // ----- Act & Assert -----
            state.visibleFeaturedProgress shouldBe FeaturedProgressUiModel.Loading
        }

        @Test
        fun `is the real progress once loaded`() {
            // ----- Arrange -----
            val progress = loadedProgress()
            val state = ReadingScreenUiState(
                isLoading = false,
                featuredProgress = progress,
            )

            // ----- Act & Assert -----
            state.visibleFeaturedProgress shouldBe progress
        }
    }

    @Nested
    inner class VisibleFeaturedDeadlineSummary {
        @Test
        fun `is null while loading even when set`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                isLoading = true,
                featuredDeadlineSummary = loadedDeadlineSummary(),
            )

            // ----- Act & Assert -----
            state.visibleFeaturedDeadlineSummary.shouldBeNull()
        }

        @Test
        fun `is the real summary once loaded`() {
            // ----- Arrange -----
            val summary = loadedDeadlineSummary()
            val state = ReadingScreenUiState(
                isLoading = false,
                featuredDeadlineSummary = summary,
            )

            // ----- Act & Assert -----
            state.visibleFeaturedDeadlineSummary shouldBe summary
        }
    }

    @Nested
    inner class VisibleFeaturedPaceNudge {
        @Test
        fun `is null while loading even when set`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(
                isLoading = true,
                featuredPaceNudge = loadedNudge(),
            )

            // ----- Act & Assert -----
            state.visibleFeaturedPaceNudge.shouldBeNull()
        }

        @Test
        fun `is the real nudge once loaded`() {
            // ----- Arrange -----
            val nudge = loadedNudge()
            val state = ReadingScreenUiState(
                isLoading = false,
                featuredPaceNudge = nudge,
            )

            // ----- Act & Assert -----
            state.visibleFeaturedPaceNudge shouldBe nudge
        }
    }
}
