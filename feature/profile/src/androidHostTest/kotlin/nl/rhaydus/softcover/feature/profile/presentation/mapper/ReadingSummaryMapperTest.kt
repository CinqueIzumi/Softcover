package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingSummaryMapperTest {
    private fun buildReadingLife(
        books: List<YearCount> = emptyList(),
        pages: List<YearCount> = emptyList(),
    ): ReadingLife = ReadingLife(
        booksByYear = books,
        pagesByYear = pages,
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(),
        recentlyLoved = emptyList(),
    )

    @Nested
    inner class ReadingSummaryUiModelMapping {
        @Test
        fun `null reading life yields two zero-valued loading stats`() {
            // ----- Arrange -----
            val readingLife: ReadingLife? = null

            // ----- Act -----
            val result = readingSummaryUiModel(
                readingLife = readingLife,
                currentYear = 2025,
            )

            // ----- Assert -----
            result.stats.map { it.stat.value } shouldBe listOf(0.0, 0.0)
            result.stats.map { it.stat.isLoading } shouldBe listOf(true, true)
        }

        @Test
        fun `picks the current year's books and pages and is not loading`() {
            // ----- Arrange -----
            val readingLife = buildReadingLife(
                books = listOf(
                    YearCount(
                        year = 2024,
                        count = 9,
                    ),
                    YearCount(
                        year = 2025,
                        count = 4,
                    ),
                ),
                pages = listOf(
                    YearCount(
                        year = 2024,
                        count = 800,
                    ),
                    YearCount(
                        year = 2025,
                        count = 1_200,
                    ),
                ),
            )

            // ----- Act -----
            val result = readingSummaryUiModel(
                readingLife = readingLife,
                currentYear = 2025,
            )

            // ----- Assert -----
            result.stats.map { it.stat.value } shouldBe listOf(4.0, 1_200.0)
            result.stats.map { it.stat.isLoading } shouldBe listOf(false, false)
        }

        @Test
        fun `a current year with no entry counts as zero, not as another year`() {
            // ----- Arrange -----
            val readingLife = buildReadingLife(
                books = listOf(
                    YearCount(
                        year = 2024,
                        count = 9,
                    ),
                ),
                pages = listOf(
                    YearCount(
                        year = 2024,
                        count = 800,
                    ),
                ),
            )

            // ----- Act -----
            val result = readingSummaryUiModel(
                readingLife = readingLife,
                currentYear = 2025,
            )

            // ----- Assert -----
            result.stats.map { it.stat.value } shouldBe listOf(0.0, 0.0)
            result.stats.map { it.stat.isLoading } shouldBe listOf(false, false)
        }
    }
}
