package nl.rhaydus.softcover.feature.reading.presentation.state

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingSheetScaffoldBuilderTest {
    @Nested
    inner class StreakSheetScaffoldFor {
        @Test
        fun `uses the no-reading headline for a full window without reading`() {
            // ----- Arrange -----
            val readDays = 0
            val dayCount = 21

            // ----- Act -----
            val result = streakSheetScaffoldFor(
                readDays = readDays,
                dayCount = dayCount,
            )

            // ----- Assert -----
            result.header.shouldNotBeNull().section.headlineText shouldBe "No reading logged yet"
            result.header.shouldNotBeNull().section.eyebrow shouldBe "Last 21 days"
        }

        @Test
        fun `uses the headline for exactly one read day`() {
            // ----- Arrange -----
            val readDays = 1
            val dayCount = 3

            // ----- Act -----
            val result = streakSheetScaffoldFor(
                readDays = readDays,
                dayCount = dayCount,
            )

            // ----- Assert -----
            result.header.shouldNotBeNull().section.headlineText shouldBe "You read on 1 of the last 3 days"
        }

        @Test
        fun `counts every read day in the headline when several were read`() {
            // ----- Arrange -----
            val readDays = 3
            val dayCount = 5

            // ----- Act -----
            val result = streakSheetScaffoldFor(
                readDays = readDays,
                dayCount = dayCount,
            )

            // ----- Assert -----
            result.header.shouldNotBeNull().section.headlineText shouldBe "You read on 3 of the last 5 days"
            result.header.shouldNotBeNull().section.eyebrow shouldBe "Last 5 days"
        }
    }
}
