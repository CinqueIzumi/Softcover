package nl.rhaydus.softcover.core.uibinding.deadline

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.BadgeVariant
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryTone
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
        fun `OnTrack status maps to the OnTrack tone with the status label`() {
            // ----- Arrange -----
            val subject = progress(
                isExpired = false,
                isOnTrack = true,
            )

            // ----- Act -----
            val result = subject.toBadgeUiModel()

            // ----- Assert -----
            result shouldBe BadgeUiModel(
                label = "On track",
                tone = BadgeTone.OnTrack,
                variant = BadgeVariant.Standard,
            )
        }

        @Test
        fun `Behind status maps to the Behind tone with the status label`() {
            // ----- Arrange -----
            val subject = progress(
                isExpired = false,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toBadgeUiModel()

            // ----- Assert -----
            result shouldBe BadgeUiModel(
                label = "Behind",
                tone = BadgeTone.Behind,
                variant = BadgeVariant.Standard,
            )
        }

        @Test
        fun `Expired status maps to the Expired tone with the status label`() {
            // ----- Arrange -----
            val subject = progress(
                isExpired = true,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toBadgeUiModel()

            // ----- Assert -----
            result shouldBe BadgeUiModel(
                label = "Expired",
                tone = BadgeTone.Expired,
                variant = BadgeVariant.Standard,
            )
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
                grayscale = false,
            )
        }

        @Test
        fun `Behind status is not grayscale and nests the Behind badge`() {
            // ----- Arrange -----
            val subject = progress(
                isExpired = false,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toCoverOverlayUiModel()

            // ----- Assert -----
            result shouldBe CoverOverlayUiModel(
                badge = subject.toBadgeUiModel(),
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
                grayscale = true,
            )
        }
    }

    @Nested
    inner class ToDeadlineSummaryUiModelTest {
        @Test
        fun `dateText respects the passed DateStyle`() {
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
                tone = DeadlineSummaryTone.OnSurface,
            )
            val monthDayYear = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.MONTH_DAY_YEAR,
                tone = DeadlineSummaryTone.OnSurface,
            )

            // ----- Assert -----
            dayMonthYear.dateText shouldBe "02/09/2026"
            monthDayYear.dateText shouldBe "09/02/2026"
        }

        @Test
        fun `tone OnSurface passes through unchanged`() {
            // ----- Arrange -----
            val subject = progress()

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
                tone = DeadlineSummaryTone.OnSurface,
            )

            // ----- Assert -----
            result.tone shouldBe DeadlineSummaryTone.OnSurface
        }

        @Test
        fun `tone OnHeroBackdrop passes through unchanged`() {
            // ----- Arrange -----
            val subject = progress()

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
                tone = DeadlineSummaryTone.OnHeroBackdrop,
            )

            // ----- Assert -----
            result.tone shouldBe DeadlineSummaryTone.OnHeroBackdrop
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
                tone = DeadlineSummaryTone.OnSurface,
            )

            // ----- Assert -----
            result.paceText shouldBe "1 page/day"
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
                tone = DeadlineSummaryTone.OnSurface,
            )

            // ----- Assert -----
            result.paceText shouldBe "2 pages/day"
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
                tone = DeadlineSummaryTone.OnSurface,
            )

            // ----- Assert -----
            result.paceText shouldBe "18 pages/day"
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
                tone = DeadlineSummaryTone.OnSurface,
            )

            // ----- Assert -----
            result.paceText shouldBe "19 pages/day"
        }

        @Test
        fun `a zero pace stays zero`() {
            // ----- Arrange -----
            val subject = progress(
                requiredPerDay = 0f,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Act -----
            val result = subject.toDeadlineSummaryUiModel(
                dateStyle = DateStyle.DAY_MONTH_YEAR,
                tone = DeadlineSummaryTone.OnSurface,
            )

            // ----- Assert -----
            result.paceText shouldBe "0 pages/day"
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
                tone = DeadlineSummaryTone.OnSurface,
            )

            // ----- Assert -----
            result.paceText shouldBe "1h 30m/day"
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
                tone = DeadlineSummaryTone.OnSurface,
            )

            // ----- Assert -----
            result.paceText shouldBe "Expired"
        }
    }
}
