package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeadlineCountdownMapperTest {
    private fun progress(
        daysRemaining: Long = 3,
        unitsRemaining: Int = 100,
        requiredPerDay: Float = 10f,
        initialPerDay: Float = 10f,
        isExpired: Boolean = false,
        isOnTrack: Boolean = true,
        unitsBehindSchedule: Int = 0,
        unit: DeadlineUnit = DeadlineUnit.PAGES,
    ): DeadlineProgress = DeadlineProgress(
        deadline = LocalDate(
            year = 2026,
            monthNumber = 9,
            dayOfMonth = 2,
        ),
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
    inner class ToCountdownCoverOverlayUiModelTest {
        @Test
        fun `OnTrack status maps to the Primary tone with a plural days label`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = 3,
                isExpired = false,
                isOnTrack = true,
            )

            // ----- Act -----
            val result = subject.toCountdownCoverOverlayUiModel()

            // ----- Assert -----
            result shouldBe CoverOverlayUiModel(
                badge = BadgeUiModel(
                    label = "3 days",
                    tone = BadgeTone.Primary,
                    size = BadgeSize.Regular,
                    icon = SoftcoverIcon.DateRange,
                ),
                corner = CoverOverlayCorner.TopStart,
                grayscale = false,
            )
        }

        @Test
        fun `Behind status keeps the ErrorContainer tone rather than the countdown's own Primary`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = 3,
                isExpired = false,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toCountdownCoverOverlayUiModel()

            // ----- Assert -----
            result.badge.tone shouldBe BadgeTone.ErrorContainer
            result.grayscale shouldBe false
        }

        @Test
        fun `Expired status maps to the Neutral tone with the Expired label and is grayscale`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = -1,
                isExpired = true,
                isOnTrack = false,
            )

            // ----- Act -----
            val result = subject.toCountdownCoverOverlayUiModel()

            // ----- Assert -----
            result shouldBe CoverOverlayUiModel(
                badge = BadgeUiModel(
                    label = "Expired",
                    tone = BadgeTone.Neutral,
                    size = BadgeSize.Regular,
                    icon = SoftcoverIcon.DateRange,
                ),
                corner = CoverOverlayCorner.TopStart,
                grayscale = true,
            )
        }

        @Test
        fun `exactly one day remaining uses the singular day label`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = 1,
                isExpired = false,
                isOnTrack = true,
            )

            // ----- Act -----
            val result = subject.toCountdownCoverOverlayUiModel()

            // ----- Assert -----
            result.badge.label shouldBe "1 day"
        }

        @Test
        fun `zero days remaining without being expired still uses the plural days label`() {
            // ----- Arrange -----
            val subject = progress(
                daysRemaining = 0,
                unitsRemaining = 0,
                isExpired = false,
                isOnTrack = true,
            )

            // ----- Act -----
            val result = subject.toCountdownCoverOverlayUiModel()

            // ----- Assert -----
            result.badge.label shouldBe "0 days"
        }
    }
}
