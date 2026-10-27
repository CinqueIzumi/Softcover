package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.domain.model.StatsWindow
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearAtAGlanceMapperTest {
    private val today = LocalDate(
        year = 2026,
        month = 10,
        day = 23,
    )

    @Nested
    inner class AtlasYearResolution {
        @Test
        fun `uses the selected calendar year`() {
            // ----- Arrange -----
            val scope = StatsScope.CalendarYear(2023)

            // ----- Act -----
            val result = atlasYear(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result shouldBe 2023
        }

        @Test
        fun `uses the year of today for a rolling window`() {
            // ----- Arrange -----
            val scope = StatsScope.Rolling(StatsWindow.LAST_12_MONTHS)

            // ----- Act -----
            val result = atlasYear(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result shouldBe 2026
        }

        @Test
        fun `uses the year of today for all time`() {
            // ----- Arrange -----
            val scope = StatsScope.AllTime

            // ----- Act -----
            val result = atlasYear(
                scope = scope,
                today = today,
            )

            // ----- Assert -----
            result shouldBe 2026
        }
    }

    @Nested
    inner class YearAtAGlanceHeaderMapping {
        @Test
        fun `describes the days of the given year`() {
            // ----- Arrange -----
            val year = 2023

            // ----- Act -----
            val result = yearAtAGlanceHeader(year = year)

            // ----- Assert -----
            val text = result.description.shouldNotBeNull().joinToString(separator = "") { it.text }
            text shouldContain "a day of 2023"
        }
    }
}
