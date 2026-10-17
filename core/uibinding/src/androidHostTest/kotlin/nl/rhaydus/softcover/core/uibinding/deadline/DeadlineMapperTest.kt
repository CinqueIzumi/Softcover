package nl.rhaydus.softcover.core.uibinding.deadline

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeadlineMapperTest {
    private fun progress(
        deadline: LocalDate = LocalDate(
            year = 2026,
            monthNumber = 9,
            dayOfMonth = 2,
        ),
        daysRemaining: Long = 10,
        unitsRemaining: Int = 100,
        requiredPerDay: Float = 10f,
        initialPerDay: Float = 10f,
        isExpired: Boolean = false,
        isOnTrack: Boolean = true,
        unitsBehindSchedule: Int = 0,
        unit: DeadlineUnit = DeadlineUnit.PAGES,
    ): DeadlineProgress = DeadlineProgress(
        deadline = deadline,
        daysRemaining = daysRemaining,
        unitsRemaining = unitsRemaining,
        requiredPerDay = requiredPerDay,
        initialPerDay = initialPerDay,
        isExpired = isExpired,
        isOnTrack = isOnTrack,
        unitsBehindSchedule = unitsBehindSchedule,
        unit = unit,
    )

    @Nested
    inner class ToBadgeUiModelTest {
        @Test
        fun `OnTrack status maps to the OnTrack tone`() {
            // ----- Arrange -----
            val subject = progress(
                isExpired = false,
                isOnTrack = true,
            )

            // ----- Act -----
            val result = subject.toBadgeUiModel()

            // ----- Assert -----
            result.tone shouldBe BadgeTone.PrimaryContainer
            result.size shouldBe BadgeSize.Compact
        }

        @Test
        fun `Behind status maps to the Behind tone`() {
            // ----- Arrange -----
            val subject = progress(
                isExpired = false,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toBadgeUiModel()

            // ----- Assert -----
            result.tone shouldBe BadgeTone.ErrorContainer
            result.size shouldBe BadgeSize.Compact
        }

        @Test
        fun `Expired status maps to the Expired tone`() {
            // ----- Arrange -----
            val subject = progress(
                isExpired = true,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toBadgeUiModel()

            // ----- Assert -----
            result.tone shouldBe BadgeTone.Neutral
            result.size shouldBe BadgeSize.Compact
        }
    }

    @Nested
    inner class ToCoverOverlayUiModelTest {
        @Test
        fun `OnTrack status is not grayscale and nests the OnTrack badge`() {
            // ----- Arrange -----
            val subject = progress(
                isExpired = false,
                isOnTrack = true,
            )

            // ----- Act -----
            val result = subject.toCoverOverlayUiModel()

            // ----- Assert -----
            result shouldBe CoverOverlayUiModel(
                badge = subject.toBadgeUiModel(),
                corner = CoverOverlayCorner.TopEnd,
                grayscale = false,
            )
        }

        @Test
        fun `Expired status is grayscale and nests the Expired badge`() {
            // ----- Arrange -----
            val subject = progress(
                isExpired = true,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toCoverOverlayUiModel()

            // ----- Assert -----
            result shouldBe CoverOverlayUiModel(
                badge = subject.toBadgeUiModel(),
                corner = CoverOverlayCorner.TopEnd,
                grayscale = true,
            )
        }
    }

    @Nested
    inner class ToDeadlineSummaryUiModelTest {
        @Test
        fun `text opens with the deadline date formatted in the passed DateStyle`() {
            // ----- Arrange -----
            val subject = progress(
                deadline = LocalDate(
                    year = 2026,
                    monthNumber = 9,
                    dayOfMonth = 2,
                ),
            )

            // ----- Act -----
            val dayMonthYear = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
            )
            val monthDayYear = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.MONTH_DAY_YEAR,
            )

            // ----- Assert -----
            dayMonthYear.text shouldBe "02/09/2026 • 10 pages/day"
            monthDayYear.text shouldBe "09/02/2026 • 10 pages/day"
        }

        @Test
        fun `pages pace ceiling to exactly 1 uses the singular unit label`() {
            // ----- Arrange -----
            val subject = progress(
                requiredPerDay = 0.5f,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
            )

            // ----- Assert -----
            result.text shouldBe "02/09/2026 • 1 page/day"
        }

        @Test
        fun `pages pace above 1 uses the plural unit label`() {
            // ----- Arrange -----
            val subject = progress(
                requiredPerDay = 2f,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
            )

            // ----- Assert -----
            result.text shouldBe "02/09/2026 • 2 pages/day"
        }

        @Test
        fun `an exact whole-number pace is not rounded up further`() {
            // ----- Arrange -----
            val subject = progress(
                requiredPerDay = 18.0f,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
            )

            // ----- Assert -----
            result.text shouldBe "02/09/2026 • 18 pages/day"
        }

        @Test
        fun `a fractional pace is rounded up to the next whole unit`() {
            // ----- Arrange -----
            val subject = progress(
                requiredPerDay = 18.2f,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
            )

            // ----- Assert -----
            result.text shouldBe "02/09/2026 • 19 pages/day"
        }

        @Test
        fun `seconds pace is rendered through secondsToHm`() {
            // ----- Arrange -----
            val subject = progress(
                requiredPerDay = 5_400f,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
            )

            // ----- Assert -----
            result.text shouldBe "02/09/2026 • 1h 30m/day"
        }

        @Test
        fun `an expired deadline reports the status label instead of a pace`() {
            // ----- Arrange -----
            val subject = progress(
                requiredPerDay = 100f,
                unit = DeadlineUnit.PAGES,
                isExpired = true,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
            )

            // ----- Assert -----
            result.text shouldBe "02/09/2026 • Expired"
        }

        @Test
        fun `a paused book reads On hold in place of the date and pace`() {
            // ----- Arrange -----
            val subject = progress()

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
                isPaused = true,
            )

            // ----- Assert -----
            result.text shouldBe "On hold"
        }

        @Test
        fun `a paused expired book still reads On hold`() {
            // ----- Arrange -----
            val subject = progress(
                isExpired = true,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
                isPaused = true,
            )

            // ----- Assert -----
            result.text shouldBe "On hold"
        }
    }

    @Nested
    inner class ToDeadlineRowUiModelTest {
        private fun rowOf(subject: DeadlineProgress) = subject.toDeadlineRowUiModel(dateStyle = DateStyle.DAY_MONTH_YEAR)

        @Test
        fun `title is Finish by the date in the passed DateStyle`() {
            // ----- Arrange -----
            val subject = progress()

            // ----- Act -----
            val result = rowOf(subject)

            // ----- Assert -----
            result.title shouldBe "Finish by 02/09/2026"
        }

        @Test
        fun `an on-track pages deadline states the pace and no behind prefix`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = 10,
                requiredPerDay = 9.2f,
            )

            // ----- Act -----
            val result = rowOf(subject)

            // ----- Assert -----
            result.goal shouldBe "Read 10 pages/day for the next 10 days to finish on time."
            result.isExpired shouldBe false
        }

        @Test
        fun `a single day remaining is singular`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = 1,
                requiredPerDay = 1f,
            )

            // ----- Act -----
            val result = rowOf(subject)

            // ----- Assert -----
            result.goal shouldBe "Read 1 page/day for the next 1 day to finish on time."
        }

        @Test
        fun `an audiobook deadline says Listen to with a time pace`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = 3,
                requiredPerDay = 3_900f,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Act -----
            val result = rowOf(subject)

            // ----- Assert -----
            result.goal shouldBe "Listen to 1h 5m/day for the next 3 days to finish on time."
        }

        @Test
        fun `a deadline that is behind prefixes how far behind schedule the reader is`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = 4,
                requiredPerDay = 20f,
                isOnTrack = false,
                unitsBehindSchedule = 30,
            )

            // ----- Act -----
            val result = rowOf(subject)

            // ----- Assert -----
            result.goal shouldBe "You're 30 pages behind schedule. Read 20 pages/day for the next 4 days to finish on time."
        }

        @Test
        fun `not on track with nothing behind schedule omits the prefix`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = 4,
                requiredPerDay = 20f,
                isOnTrack = false,
                unitsBehindSchedule = 0,
            )

            // ----- Act -----
            val result = rowOf(subject)

            // ----- Assert -----
            result.goal shouldBe "Read 20 pages/day for the next 4 days to finish on time."
        }

        @Test
        fun `an expired deadline says how many days ago it passed`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = -3,
                isExpired = true,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = rowOf(subject)

            // ----- Assert -----
            result.goal shouldBe "Deadline passed 3 days ago."
            result.isExpired shouldBe true
        }

        @Test
        fun `a deadline that passed yesterday is singular`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = -1,
                isExpired = true,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = rowOf(subject)

            // ----- Assert -----
            result.goal shouldBe "Deadline passed 1 day ago."
        }
 
        @Test
        fun `a paused book has no badge and a goal that asks to resume`() {
            // ----- Arrange -----
            val subject = progress()

            // ----- Act -----
            val result = subject.toDeadlineRowUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
                isPaused = true,
            )

            // ----- Assert -----
            result.badge.shouldBeNull()
            result.goal shouldBe "On hold. Resume to get back on pace."
            result.title shouldBe "Finish by 02/09/2026"
        }

        @Test
        fun `a book that is not paused keeps its badge`() {
            // ----- Arrange -----
            val subject = progress()

            // ----- Act -----
            val result = rowOf(subject)

            // ----- Assert -----
            result.badge.shouldNotBeNull()
        }

        @Test
        fun `a paused expired book keeps the expired flag but drops the badge`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = -3,
                isExpired = true,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toDeadlineRowUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
                isPaused = true,
            )

            // ----- Assert -----
            result.isExpired shouldBe true
            result.badge.shouldBeNull()
        }
    }
}
