package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AtlasOpeningMapperTest {
    private fun buildStats(booksRead: Int = 42): ReadingStats = ReadingStats(
        booksRead = booksRead,
        totalPages = 12000,
    )

    @Nested
    inner class AtlasOpeningUiModelMapping {
        @Test
        fun `null stats yields two zero-valued loading stats`() {
            // ----- Arrange -----
            val stats: ReadingStats? = null

            // ----- Act -----
            val result = atlasOpeningUiModel(stats)

            // ----- Assert -----
            val tiles = listOf(result.hero.stat, result.books.stat)
            tiles.map { it.value } shouldBe listOf(0.0, 0.0)
            tiles.map { it.isLoading } shouldBe listOf(true, true)
        }

        @Test
        fun `null stats gives the hero a caption that names no count`() {
            // ----- Arrange -----
            val stats: ReadingStats? = null

            // ----- Act -----
            val result = atlasOpeningUiModel(stats)

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "From your finished books."
        }

        @Test
        fun `stats fill the pages and books tiles and is not loading`() {
            // ----- Arrange -----
            val stats = buildStats()

            // ----- Act -----
            val result = atlasOpeningUiModel(stats)

            // ----- Assert -----
            result.hero.stat.value shouldBe 12000.0
            result.books.stat.value shouldBe 42.0
            listOf(result.hero.stat, result.books.stat).map { it.isLoading } shouldBe listOf(false, false)
        }

        @Test
        fun `a single finished book uses the singular caption`() {
            // ----- Arrange -----
            val stats = buildStats(booksRead = 1)

            // ----- Act -----
            val result = atlasOpeningUiModel(stats)

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "From 1 finished book."
        }

        @Test
        fun `several finished books use the plural caption with the count`() {
            // ----- Arrange -----
            val stats = buildStats(booksRead = 42)

            // ----- Act -----
            val result = atlasOpeningUiModel(stats)

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "From 42 finished books."
        }

        @Test
        fun `zero finished books state that none are finished yet`() {
            // ----- Arrange -----
            val stats = buildStats(booksRead = 0)

            // ----- Act -----
            val result = atlasOpeningUiModel(stats)

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "No finished books yet."
        }
    }


    @Nested
    inner class ScopedHeroCaption {
        @Test
        fun `a scope with several finished books names the scope after the count`() {
            // ----- Arrange -----
            val stats = buildStats(booksRead = 5)

            // ----- Act -----
            val result = atlasOpeningUiModel(
                stats = stats,
                scope = StatsScope.CalendarYear(2026),
            )

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "From 5 finished books in 2026."
        }

        @Test
        fun `a scope with a single finished book uses the singular count`() {
            // ----- Arrange -----
            val stats = buildStats(booksRead = 1)

            // ----- Act -----
            val result = atlasOpeningUiModel(
                stats = stats,
                scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS),
            )

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "From 1 finished book in the last 30 days."
        }

        @Test
        fun `a scope with no finished books says so instead of counting zero`() {
            // ----- Arrange -----
            val stats = buildStats(booksRead = 0)

            // ----- Act -----
            val result = atlasOpeningUiModel(
                stats = stats,
                scope = StatsScope.Rolling(StatsWindow.LAST_6_MONTHS),
            )

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "No books finished in the last 6 months."
        }

        @Test
        fun `loading stats keep the scope-free caption whatever the scope`() {
            // ----- Arrange -----
            val stats: ReadingStats? = null

            // ----- Act -----
            val result = atlasOpeningUiModel(
                stats = stats,
                scope = StatsScope.CalendarYear(2026),
            )

            // ----- Assert -----
            (result.hero.variant as StatTileVariant.Hero).caption shouldBe "From your finished books."
        }
    }
}
