package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileVariant
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StreakTilesMapperTest {
    private fun date(
        year: Int = 2025,
        month: Int,
        day: Int,
    ): LocalDate = LocalDate(
        year = year,
        month = month,
        day = day,
    )

    private val today = date(
        month = 6,
        day = 10,
    )

    private fun StatTileUiModel.caption(): String = variant.shouldBeInstanceOf<StatTileVariant.Standard>().caption

    @Nested
    inner class CurrentStreakTile {
        @Test
        fun `reads as no streak running when there is none`() {
            // ----- Arrange -----
            val streak: ReadingStreak? = null

            // ----- Act -----
            val result = currentStreakTile(
                streak = streak,
                today = today,
            )

            // ----- Assert -----
            result.stat.shouldNotBeNull().value shouldBe 0.0
            result.caption() shouldBe "No streak running. Read today to start one."
        }

        @Test
        fun `counts a run ending today without asking to read`() {
            // ----- Arrange -----
            val streak = ReadingStreak(
                start = date(
                    month = 6,
                    day = 6,
                ),
                end = today,
            )

            // ----- Act -----
            val result = currentStreakTile(
                streak = streak,
                today = today,
            )

            // ----- Assert -----
            result.stat.shouldNotBeNull().value shouldBe 5.0
            result.caption() shouldBe "days in a row"
        }

        @Test
        fun `uses the singular for a one day run ending today`() {
            // ----- Arrange -----
            val streak = ReadingStreak(
                start = today,
                end = today,
            )

            // ----- Act -----
            val result = currentStreakTile(
                streak = streak,
                today = today,
            )

            // ----- Assert -----
            result.stat.shouldNotBeNull().value shouldBe 1.0
            result.caption() shouldBe "day in a row"
        }

        @Test
        fun `asks to read today when the run ended yesterday`() {
            // ----- Arrange -----
            val streak = ReadingStreak(
                start = date(
                    month = 6,
                    day = 7,
                ),
                end = date(
                    month = 6,
                    day = 9,
                ),
            )

            // ----- Act -----
            val result = currentStreakTile(
                streak = streak,
                today = today,
            )

            // ----- Assert -----
            result.stat.shouldNotBeNull().value shouldBe 3.0
            result.caption() shouldBe "days in a row. Read today to keep it."
        }
    }

    @Nested
    inner class LongestStreakTile {
        @Test
        fun `names the year when there are no reading days`() {
            // ----- Arrange -----
            val streak: ReadingStreak? = null

            // ----- Act -----
            val result = longestStreakTile(
                streak = streak,
                year = 2025,
            )

            // ----- Assert -----
            result.stat.shouldNotBeNull().value shouldBe 0.0
            result.caption() shouldBe "No reading days in 2025."
        }

        @Test
        fun `gives the single date of a one day streak`() {
            // ----- Arrange -----
            val streak = ReadingStreak(
                start = date(
                    month = 4,
                    day = 14,
                ),
                end = date(
                    month = 4,
                    day = 14,
                ),
            )

            // ----- Act -----
            val result = longestStreakTile(
                streak = streak,
                year = 2025,
            )

            // ----- Assert -----
            result.stat.shouldNotBeNull().value shouldBe 1.0
            result.caption() shouldBe "day, 14 April"
        }

        @Test
        fun `gives both dates of a streak inside the year`() {
            // ----- Arrange -----
            val streak = ReadingStreak(
                start = date(
                    month = 3,
                    day = 3,
                ),
                end = date(
                    month = 3,
                    day = 10,
                ),
            )

            // ----- Act -----
            val result = longestStreakTile(
                streak = streak,
                year = 2025,
            )

            // ----- Assert -----
            result.stat.shouldNotBeNull().value shouldBe 8.0
            result.caption() shouldBe "days, 3 March to 10 March"
        }

        @Test
        fun `adds the year to the start of a streak that began before 1 January`() {
            // ----- Arrange -----
            val streak = ReadingStreak(
                start = date(
                    year = 2024,
                    month = 12,
                    day = 30,
                ),
                end = date(
                    month = 1,
                    day = 2,
                ),
            )

            // ----- Act -----
            val result = longestStreakTile(
                streak = streak,
                year = 2025,
            )

            // ----- Assert -----
            result.stat.shouldNotBeNull().value shouldBe 4.0
            result.caption() shouldBe "days, 30 December 2024 to 2 January"
        }

        @Test
        fun `adds the year to the end of a streak that runs past 31 December`() {
            // ----- Arrange -----
            val streak = ReadingStreak(
                start = date(
                    month = 12,
                    day = 30,
                ),
                end = date(
                    year = 2026,
                    month = 1,
                    day = 2,
                ),
            )

            // ----- Act -----
            val result = longestStreakTile(
                streak = streak,
                year = 2025,
            )

            // ----- Assert -----
            result.caption() shouldBe "days, 30 December to 2 January 2026"
        }
    }
}
