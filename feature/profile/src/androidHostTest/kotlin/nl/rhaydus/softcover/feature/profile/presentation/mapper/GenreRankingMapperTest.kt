package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class GenreRankingMapperTest {
    private fun slice(
        name: String,
        fraction: Double,
    ): GenreSlice = GenreSlice(
        name = name,
        count = 1,
        fraction = fraction,
    )

    @Nested
    inner class GenreRankingUiModelTests {
        @Test
        fun `returns the loading model with three masked rows when genres is null`() {
            // ----- Arrange -----
            val genres: GenreBreakdown? = null

            // ----- Act -----
            val result = genreRankingUiModel(genres)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.rows shouldHaveSize 3
            result.rows.map { it.tone } shouldBe listOf(ChartTone.Lead, ChartTone.Second, ChartTone.Third)
            result.rows.map { it.label }.toSet() shouldBe setOf("")
        }

        @Test
        fun `returns no rows and is not loading when there are no slices`() {
            // ----- Arrange -----
            val genres = GenreBreakdown()

            // ----- Act -----
            val result = genreRankingUiModel(genres)

            // ----- Assert -----
            result.isLoading shouldBe false
            result.rows shouldHaveSize 0
        }

        @Test
        fun `maps a single slice to a Lead row with a rounded share`() {
            // ----- Arrange -----
            val fantasy = slice(
                name = "Fantasy",
                fraction = 0.625,
            )
            val genres = GenreBreakdown(slices = listOf(fantasy))

            // ----- Act -----
            val result = genreRankingUiModel(genres)

            // ----- Assert -----
            result.isLoading shouldBe false
            result.rows.single().label shouldBe "Fantasy"
            result.rows.single().share shouldBe "63%"
            result.rows.single().fraction shouldBe 0.625f
            result.rows.single().tone shouldBe ChartTone.Lead
        }

        @Test
        fun `assigns tones Lead to Fifth by position`() {
            // ----- Arrange -----
            val genres = GenreBreakdown(
                slices = listOf(
                    slice(
                        name = "A",
                        fraction = 0.5,
                    ),
                    slice(
                        name = "B",
                        fraction = 0.4,
                    ),
                    slice(
                        name = "C",
                        fraction = 0.3,
                    ),
                    slice(
                        name = "D",
                        fraction = 0.2,
                    ),
                    slice(
                        name = "E",
                        fraction = 0.1,
                    ),
                ),
            )

            // ----- Act -----
            val result = genreRankingUiModel(genres)

            // ----- Assert -----
            result.rows.map { it.label } shouldBe listOf("A", "B", "C", "D", "E")
            result.rows.map { it.tone } shouldBe listOf(
                ChartTone.Lead,
                ChartTone.Second,
                ChartTone.Third,
                ChartTone.Fourth,
                ChartTone.Fifth,
            )
        }

        @Test
        fun `keeps only the top five slices when there are more`() {
            // ----- Arrange -----
            val genres = GenreBreakdown(
                slices = listOf(
                    "A",
                    "B",
                    "C",
                    "D",
                    "E",
                    "F",
                    "G",
                ).map {
                    slice(
                        name = it,
                        fraction = 0.1,
                    )
                },
            )

            // ----- Act -----
            val result = genreRankingUiModel(genres)

            // ----- Assert -----
            result.rows.map { it.label } shouldBe listOf("A", "B", "C", "D", "E")
        }
    }

    @Nested
    inner class GenreScopeCaptionTests {
        @Test
        fun `falls back to the scope-free wording when the count is zero`() {
            // ----- Arrange -----
            val taggedBookCount = 0

            // ----- Act -----
            val result = genreScopeCaption(taggedBookCount)

            // ----- Assert -----
            result shouldContain "out of your finished books with genre tags"
        }

        @Test
        fun `uses the singular wording for a single book`() {
            // ----- Arrange -----
            val taggedBookCount = 1

            // ----- Act -----
            val result = genreScopeCaption(taggedBookCount)

            // ----- Assert -----
            result shouldContain "out of 1 finished book with genre tags"
        }

        @Test
        fun `names the count for several books`() {
            // ----- Arrange -----
            val taggedBookCount = 12

            // ----- Act -----
            val result = genreScopeCaption(taggedBookCount)

            // ----- Assert -----
            result shouldContain "out of 12 finished books with genre tags"
        }
    }


    @Nested
    inner class ScopedGenreScopeCaption {
        @Test
        fun `a scope with no tagged books names the books finished in it`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2026)

            // ----- Act -----
            val result = genreScopeCaption(
                taggedBookCount = 0,
                scope = scope,
            )

            // ----- Assert -----
            result shouldContain "out of the books you finished in 2026 with genre tags"
        }

        @Test
        fun `a scope with one tagged book uses the singular wording`() {
            // ----- Arrange -----
            val scope = StatsScope.Rolling(StatsWindow.LAST_12_MONTHS)

            // ----- Act -----
            val result = genreScopeCaption(
                taggedBookCount = 1,
                scope = scope,
            )

            // ----- Assert -----
            result shouldContain "out of the 1 book you finished in the last 12 months with genre tags"
        }

        @Test
        fun `a scope with several tagged books names the count`() {
            // ----- Arrange -----
            val scope = StatsScope.Rolling(StatsWindow.LAST_30_DAYS)

            // ----- Act -----
            val result = genreScopeCaption(
                taggedBookCount = 4,
                scope = scope,
            )

            // ----- Assert -----
            result shouldContain "out of the 4 books you finished in the last 30 days with genre tags"
        }
    }

    @Nested
    inner class GenreEmptyCaptionTests {
        @Test
        fun `a scope with no finished books says so`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2025)

            // ----- Act -----
            val result = genreEmptyCaption(
                booksRead = 0,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "No finished books in 2025."
        }

        @Test
        fun `a scope with finished books but no genre tags keeps the generic prompt`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2025)

            // ----- Act -----
            val result = genreEmptyCaption(
                booksRead = 3,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "Finish a few books and your top genres show up here."
        }

        @Test
        fun `all time keeps the generic prompt even with no finished books`() {
            // ----- Arrange -----
            val scope = StatsScope.AllTime

            // ----- Act -----
            val result = genreEmptyCaption(
                booksRead = 0,
                scope = scope,
            )

            // ----- Assert -----
            result shouldBe "Finish a few books and your top genres show up here."
        }
    }
}
