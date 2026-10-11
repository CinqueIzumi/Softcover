package nl.rhaydus.softcover.feature.reading.presentation.state

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingSheetScaffoldBuilderTest {
    private fun day(
        dayOfMonth: Int,
        didRead: Boolean,
    ) = ReadingDayActivity(
        date = LocalDate(
            year = 2026,
            monthNumber = 1,
            dayOfMonth = dayOfMonth,
        ),
        didRead = didRead,
    )

    private fun days(vararg read: Boolean) = read.mapIndexed { index, didRead ->
        day(
            dayOfMonth = index + 1,
            didRead = didRead,
        )
    }

    @Nested
    inner class StreakSheetScaffoldFor {
        @Test
        fun `uses the no-reading headline for an empty activity list`() {
            // ----- Arrange -----
            val activity = emptyList<ReadingDayActivity>()

            // ----- Act -----
            val result = streakSheetScaffoldFor(activity = activity)

            // ----- Assert -----
            result.header.shouldNotBeNull().section.headlineText shouldBe "No reading logged yet"
            result.header.shouldNotBeNull().section.eyebrow shouldBe "Last 0 days"
        }

        @Test
        fun `uses the headline for exactly one read day`() {
            // ----- Arrange -----
            val activity = days(
                true,
                false,
                false,
            )

            // ----- Act -----
            val result = streakSheetScaffoldFor(activity = activity)

            // ----- Assert -----
            result.header.shouldNotBeNull().section.headlineText shouldBe "You read on 1 of the last 3 days"
        }

        @Test
        fun `counts every read day in the headline when several were read`() {
            // ----- Arrange -----
            val activity = days(
                true,
                false,
                true,
                true,
                false,
            )

            // ----- Act -----
            val result = streakSheetScaffoldFor(activity = activity)

            // ----- Assert -----
            result.header.shouldNotBeNull().section.headlineText shouldBe "You read on 3 of the last 5 days"
            result.header.shouldNotBeNull().section.eyebrow shouldBe "Last 5 days"
        }
    }
}
