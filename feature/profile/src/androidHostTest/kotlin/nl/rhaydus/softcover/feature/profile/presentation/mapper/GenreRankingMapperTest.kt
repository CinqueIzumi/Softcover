package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.StatsScope
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

    private fun SectionHeaderUiModel.Section.descriptionOrEmpty(): String =
        description.orEmpty().joinToString(separator = "") { it.text }

    private fun slices(count: Int): List<GenreSlice> = List(count) { index ->
        slice(
            name = "Genre $index",
            fraction = 0.8 - index * 0.05,
        )
    }

    @Nested
    inner class GenreHeaderTests {
        @Test
        fun `headlines the top genre in lowercase with its share`() {
            // ----- Arrange -----
            val genres = listOf(
                slice(
                    name = "Science Fiction",
                    fraction = 0.62,
                ),
                slice(
                    name = "Fantasy",
                    fraction = 0.4,
                ),
            )

            // ----- Act -----
            val result = genreHeader(
                slices = genres,
                emptyCaption = "empty",
            )

            // ----- Assert -----
            result.headlineText shouldBe "Mostly science fiction"
            result.descriptionOrEmpty() shouldContain "62% of your books were science fiction."
        }

        @Test
        fun `keeps an all-caps word in the headline`() {
            // ----- Arrange -----
            val genres = listOf(
                slice(
                    name = "LGBTQ+ Fiction",
                    fraction = 0.3,
                ),
            )

            // ----- Act -----
            val result = genreHeader(
                slices = genres,
                emptyCaption = "empty",
            )

            // ----- Assert -----
            result.headlineText shouldBe "Mostly LGBTQ+ fiction"
        }

        @Test
        fun `a tie at the top picks the first genre of the ranking`() {
            // ----- Arrange -----
            val genres = listOf(
                slice(
                    name = "Mystery",
                    fraction = 0.5,
                ),
                slice(
                    name = "Romance",
                    fraction = 0.5,
                ),
            )

            // ----- Act -----
            val result = genreHeader(
                slices = genres,
                emptyCaption = "empty",
            )

            // ----- Assert -----
            result.headlineText shouldBe "Mostly mystery"
        }

        @Test
        fun `an empty ranking says the empty caption under the generic headline`() {
            // ----- Arrange -----
            val caption = "No finished books in 2025."

            // ----- Act -----
            val result = genreHeader(
                slices = emptyList(),
                emptyCaption = caption,
            )

            // ----- Assert -----
            result.headlineText shouldBe "The genres you read most"
            result.descriptionOrEmpty() shouldBe caption
            result.isLoading shouldBe false
        }

        @Test
        fun `null slices yield the masked header`() {
            // ----- Arrange -----
            val genres = null

            // ----- Act -----
            val result = genreHeader(
                slices = genres,
                emptyCaption = "empty",
            )

            // ----- Assert -----
            result.isLoading shouldBe true
        }
    }

    @Nested
    inner class GenreRankingUiModelTests {
        @Test
        fun `null slices yield three masked loading rows, all visible`() {
            // ----- Arrange -----
            val genres = null

            // ----- Act -----
            val result = genreRankingUiModel(
                slices = genres,
                shownCount = 5,
            )

            // ----- Assert -----
            result.isLoading shouldBe true
            result.rows shouldHaveSize 3
            result.visibleCount shouldBe 3
        }

        @Test
        fun `no slices yield no rows and no loading`() {
            // ----- Arrange -----
            val genres = emptyList<GenreSlice>()

            // ----- Act -----
            val result = genreRankingUiModel(
                slices = genres,
                shownCount = 5,
            )

            // ----- Assert -----
            result.isLoading shouldBe false
            result.rows shouldHaveSize 0
            result.visibleCount shouldBe 0
        }

        @Test
        fun `every genre is a row while only the shown count is visible`() {
            // ----- Arrange -----
            val genres = slices(8)

            // ----- Act -----
            val result = genreRankingUiModel(
                slices = genres,
                shownCount = 5,
            )

            // ----- Assert -----
            result.rows.map { it.label } shouldBe genres.map { it.name }
            result.visibleCount shouldBe 5
        }

        @Test
        fun `a shown count above the ranking size shows every genre`() {
            // ----- Arrange -----
            val genres = slices(8)

            // ----- Act -----
            val result = genreRankingUiModel(
                slices = genres,
                shownCount = 15,
            )

            // ----- Assert -----
            result.visibleCount shouldBe 8
        }

        @Test
        fun `fills are relative to the lead and the share is rounded`() {
            // ----- Arrange -----
            val genres = listOf(
                slice(
                    name = "Fantasy",
                    fraction = 0.625,
                ),
                slice(
                    name = "Romance",
                    fraction = 0.3125,
                ),
            )

            // ----- Act -----
            val result = genreRankingUiModel(
                slices = genres,
                shownCount = 5,
            )

            // ----- Assert -----
            result.rows.map { it.fill } shouldBe listOf(1f, 0.5f)
            result.rows.first().share shouldBe "63%"
        }
    }

    @Nested
    inner class GenreRankingShowMoreTests {
        @Test
        fun `is offered while genres remain hidden`() {
            // ----- Arrange -----
            val genres = slices(9)

            // ----- Act -----
            val result = genreRankingShowMore(
                slices = genres,
                shownCount = 5,
            )

            // ----- Assert -----
            result.shouldNotBeNull()
        }

        @Test
        fun `is gone once every genre is shown`() {
            // ----- Arrange -----
            val genres = slices(9)

            // ----- Act -----
            val exact = genreRankingShowMore(
                slices = genres,
                shownCount = 9,
            )
            val beyond = genreRankingShowMore(
                slices = genres,
                shownCount = 15,
            )

            // ----- Assert -----
            exact.shouldBeNull()
            beyond.shouldBeNull()
        }

        @Test
        fun `null slices have none`() {
            // ----- Arrange -----
            val genres = null

            // ----- Act -----
            val result = genreRankingShowMore(
                slices = genres,
                shownCount = 5,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class GenreRankingShowLessTests {
        @Test
        fun `is offered once more than five genres are shown`() {
            // ----- Arrange -----
            val genres = slices(20)

            // ----- Act -----
            val result = genreRankingShowLess(
                slices = genres,
                shownCount = 15,
            )

            // ----- Assert -----
            result.shouldNotBeNull()
        }

        @Test
        fun `is gone at the collapsed five`() {
            // ----- Arrange -----
            val genres = slices(20)

            // ----- Act -----
            val result = genreRankingShowLess(
                slices = genres,
                shownCount = 5,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `a ranking of five or fewer has none even with a larger shown count`() {
            // ----- Arrange -----
            val genres = slices(4)

            // ----- Act -----
            val result = genreRankingShowLess(
                slices = genres,
                shownCount = 15,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `a shown count above a longer ranking still offers it`() {
            // ----- Arrange -----
            val genres = slices(8)

            // ----- Act -----
            val result = genreRankingShowLess(
                slices = genres,
                shownCount = 25,
            )

            // ----- Assert -----
            result.shouldNotBeNull()
        }

        @Test
        fun `null slices have none`() {
            // ----- Arrange -----
            val genres = null

            // ----- Act -----
            val result = genreRankingShowLess(
                slices = genres,
                shownCount = 15,
            )

            // ----- Assert -----
            result.shouldBeNull()
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
