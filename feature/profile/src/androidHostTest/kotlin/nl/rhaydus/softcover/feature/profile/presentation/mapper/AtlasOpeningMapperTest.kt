package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.profile.domain.model.PagesRead
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasOpeningUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AtlasOpeningMapperTest {
    private val today = LocalDate(
        year = 2026,
        month = 10,
        day = 23,
    )

    private fun buildStats(booksRead: Int = 42): ReadingStats = ReadingStats(booksRead = booksRead)

    private fun buildPagesRead(
        total: Int = 12_000,
        finished: Int = 0,
        stopped: Int = 0,
        inProgress: Boolean = false,
        pagesPerDay: Int? = null,
    ): PagesRead = PagesRead(
        total = total,
        finishedBookCount = finished,
        stoppedBookCount = stopped,
        hasInProgressPages = inProgress,
        pagesPerDay = pagesPerDay,
    )

    private fun AtlasOpeningUiModel.caption(): String = (hero.variant as StatTileVariant.Hero).caption

    private fun captionFor(
        pagesRead: PagesRead,
        scope: StatsScope = StatsScope.AllTime,
    ): String = atlasOpeningUiModel(
        stats = buildStats(),
        pagesRead = pagesRead,
        scope = scope,
        today = today,
    ).caption()

    @Nested
    inner class AtlasOpeningUiModelMapping {
        @Test
        fun `null stats yields two zero-valued loading stats`() {
            // ----- Arrange -----
            val stats: ReadingStats? = null

            // ----- Act -----
            val result = atlasOpeningUiModel(stats)

            // ----- Assert -----
            val tiles = listOf(
                result.hero.stat.shouldNotBeNull(),
                result.books.stat.shouldNotBeNull(),
            )

            tiles.map { it.value } shouldBe listOf(0.0, 0.0)
            tiles.map { it.isLoading } shouldBe listOf(true, true)
        }

        @Test
        fun `loading pages read gives the hero a caption that names no count`() {
            // ----- Arrange -----
            val stats = buildStats()

            // ----- Act -----
            val result = atlasOpeningUiModel(
                stats = stats,
                pagesRead = null,
            )

            // ----- Assert -----
            result.caption() shouldBe "From your reading."
            result.hero.stat.shouldNotBeNull().isLoading shouldBe true
            result.books.stat.shouldNotBeNull().isLoading shouldBe false
        }

        @Test
        fun `the hero shows the pages read total and the books tile the books finished`() {
            // ----- Arrange -----
            val stats = buildStats(booksRead = 42)
            val pagesRead = buildPagesRead(total = 12_000)

            // ----- Act -----
            val result = atlasOpeningUiModel(
                stats = stats,
                pagesRead = pagesRead,
            )

            // ----- Assert -----
            result.hero.stat.shouldNotBeNull().value shouldBe 12_000.0
            result.books.stat.shouldNotBeNull().value shouldBe 42.0

            listOf(
                result.hero.stat.shouldNotBeNull(),
                result.books.stat.shouldNotBeNull(),
            ).map { it.isLoading } shouldBe listOf(false, false)
        }

        @Test
        fun `names finished books, books you did not finish and the ones in progress`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(
                finished = 38,
                stopped = 4,
                inProgress = true,
            )

            // ----- Act -----
            val caption = captionFor(pagesRead)

            // ----- Assert -----
            caption shouldBe "From 38 finished books, 4 you didn't finish and the ones you're reading."
        }

        @Test
        fun `drops the finished part when no book was finished`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(
                stopped = 2,
                inProgress = true,
            )

            // ----- Act -----
            val caption = captionFor(pagesRead)

            // ----- Assert -----
            caption shouldBe "From 2 books you didn't finish and the ones you're reading."
        }

        @Test
        fun `drops the stopped and in-progress parts when there are none`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(finished = 5)

            // ----- Act -----
            val caption = captionFor(pagesRead)

            // ----- Assert -----
            caption shouldBe "From 5 finished books."
        }

        @Test
        fun `uses the singular for a single finished book`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(finished = 1)

            // ----- Act -----
            val caption = captionFor(pagesRead)

            // ----- Assert -----
            caption shouldBe "From 1 finished book."
        }

        @Test
        fun `uses the singular for a single stopped book when nothing was finished`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(stopped = 1)

            // ----- Act -----
            val caption = captionFor(pagesRead)

            // ----- Assert -----
            caption shouldBe "From 1 book you didn't finish."
        }

        @Test
        fun `names the books being read when only unfinished reading earned pages`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(inProgress = true)

            // ----- Act -----
            val caption = captionFor(pagesRead)

            // ----- Assert -----
            caption shouldBe "From the books you're reading."
        }

        @Test
        fun `falls back to your reading when pages exist but no part applies`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead()

            // ----- Act -----
            val caption = captionFor(pagesRead)

            // ----- Assert -----
            caption shouldBe "From your reading."
        }

        @Test
        fun `says were reading for a calendar year that is already over`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(
                finished = 3,
                inProgress = true,
            )

            // ----- Act -----
            val caption = captionFor(
                pagesRead = pagesRead,
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            caption shouldBe "From 3 finished books and the ones you were reading in 2025."
        }

        @Test
        fun `says are reading for the current calendar year`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(
                finished = 3,
                inProgress = true,
            )

            // ----- Act -----
            val caption = captionFor(
                pagesRead = pagesRead,
                scope = StatsScope.CalendarYear(2026),
            )

            // ----- Assert -----
            caption shouldBe "From 3 finished books and the ones you're reading in 2026."
        }

        @Test
        fun `a rolling window keeps the present tense`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(inProgress = true)

            // ----- Act -----
            val caption = captionFor(
                pagesRead = pagesRead,
                scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS),
            )

            // ----- Assert -----
            caption shouldBe "From the books you're reading in the last 30 days."
        }

        @Test
        fun `places the scope phrase before the pace`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(
                finished = 5,
                pagesPerDay = 12,
            )

            // ----- Act -----
            val caption = captionFor(
                pagesRead = pagesRead,
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            caption shouldBe "From 5 finished books in 2025, about 12 pages a day."
        }

        @Test
        fun `adds the pace only when there is one`() {
            // ----- Arrange -----
            val withPace = buildPagesRead(
                finished = 5,
                pagesPerDay = 1,
            )

            // ----- Act -----
            val caption = captionFor(withPace)

            // ----- Assert -----
            caption shouldBe "From 5 finished books, about 1 page a day."
        }

        @Test
        fun `groups thousands in the counts and the pace`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(
                finished = 1_200,
                pagesPerDay = 1_500,
            )

            // ----- Act -----
            val caption = captionFor(pagesRead)

            // ----- Assert -----
            caption shouldBe "From ${formatGroupedNumber(1_200)} finished books, about ${formatGroupedNumber(1_500)} pages a day."
        }

        @Test
        fun `says no pages read yet for a zero total across all time`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(total = 0)

            // ----- Act -----
            val caption = captionFor(pagesRead)

            // ----- Assert -----
            caption shouldBe "No pages read yet."
        }

        @Test
        fun `names the scope for a zero total inside a narrower scope`() {
            // ----- Arrange -----
            val pagesRead = buildPagesRead(total = 0)

            // ----- Act -----
            val caption = captionFor(
                pagesRead = pagesRead,
                scope = StatsScope.CalendarYear(2025),
            )

            // ----- Assert -----
            caption shouldBe "No pages read in 2025."
        }
    }
}
