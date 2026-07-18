package nl.rhaydus.softcover.feature.reading.presentation.component

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity

class StreakStripTest {
    private fun date(day: Int): LocalDate = LocalDate(
        2026,
        1,
        day,
    )

    private fun buildActivity(vararg didRead: Boolean): List<ReadingDayActivity> =
        didRead.mapIndexed { index, read ->
            ReadingDayActivity(
                date = date(1 + index),
                didRead = read,
            )
        }

    @Nested
    inner class CurrentStreakLength {
        @Test
        fun `returns the full length for an unbroken run of read days`() {
            // ----- Arrange -----
            val activity = buildActivity(
                true,
                true,
                true,
                true,
                true,
            )

            // ----- Act -----
            val streak = currentStreakLength(activity)

            // ----- Assert -----
            streak shouldBe 5
        }

        @Test
        fun `counts only the trailing run after a gap breaks the streak`() {
            // ----- Arrange -----
            val activity = buildActivity(
                true,
                true,
                false,
                true,
                true,
            )

            // ----- Act -----
            val streak = currentStreakLength(activity)

            // ----- Assert -----
            streak shouldBe 2
        }

        @Test
        fun `returns zero when no days were read`() {
            // ----- Arrange -----
            val activity = buildActivity(
                false,
                false,
                false,
            )

            // ----- Act -----
            val streak = currentStreakLength(activity)

            // ----- Assert -----
            streak shouldBe 0
        }

        @Test
        fun `returns zero for an empty activity list`() {
            // ----- Arrange -----
            val activity = emptyList<ReadingDayActivity>()

            // ----- Act -----
            val streak = currentStreakLength(activity)

            // ----- Assert -----
            streak shouldBe 0
        }

        @Test
        fun `returns zero when today is unread even though earlier days were read`() {
            // ----- Arrange -----
            val activity = buildActivity(
                true,
                true,
                true,
                false,
            )

            // ----- Act -----
            val streak = currentStreakLength(activity)

            // ----- Assert -----
            streak shouldBe 0
        }

        @Test
        fun `returns the full length for a 21 day unbroken run`() {
            // ----- Arrange -----
            val activity = buildActivity(*BooleanArray(21) { true })

            // ----- Act -----
            val streak = currentStreakLength(activity)

            // ----- Assert -----
            streak shouldBe 21
        }
    }
}
