package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.ChartTone
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

        @Test
        fun `renders a zero fraction as 0 percent`() {
            // ----- Arrange -----
            val poetry = slice(
                name = "Poetry",
                fraction = 0.0,
            )
            val genres = GenreBreakdown(slices = listOf(poetry))

            // ----- Act -----
            val result = genreRankingUiModel(genres)

            // ----- Assert -----
            result.rows.single().share shouldBe "0%"
            result.rows.single().fraction shouldBe 0f
        }
    }
}
