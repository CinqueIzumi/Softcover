package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingSummaryMapperTest {
    private fun buildReadingHistory(
        books: List<YearCount> = emptyList(),
        pages: List<YearCount> = emptyList(),
    ): ReadingHistory = ReadingHistory(
        booksByYear = books,
        pagesByYear = pages,
    )

    @Nested
    inner class ReadingSummaryUiModelMapping {
        @Test
        fun `null reading history yields two zero-valued loading stats`() {
            // ----- Arrange -----
            val history: ReadingHistory? = null

            // ----- Act -----
            val result = readingSummaryUiModel(
                history = history,
                currentYear = 2025,
            )

            // ----- Assert -----
            result.stats.map { it.stat.value } shouldBe listOf(0.0, 0.0)
            result.stats.map { it.stat.isLoading } shouldBe listOf(true, true)
        }

        @Test
        fun `picks the current year's books and pages and is not loading`() {
            // ----- Arrange -----
            val history = buildReadingHistory(
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
                history = history,
                currentYear = 2025,
            )

            // ----- Assert -----
            result.stats.map { it.stat.value } shouldBe listOf(4.0, 1_200.0)
            result.stats.map { it.stat.isLoading } shouldBe listOf(false, false)
        }

        @Test
        fun `a current year with no entry counts as zero, not as another year`() {
            // ----- Arrange -----
            val history = buildReadingHistory(
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
                history = history,
                currentYear = 2025,
            )

            // ----- Assert -----
            result.stats.map { it.stat.value } shouldBe listOf(0.0, 0.0)
            result.stats.map { it.stat.isLoading } shouldBe listOf(false, false)
        }
    }
}
