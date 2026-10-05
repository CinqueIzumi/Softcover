package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.badge.BadgeTone
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
            result.badge.label shouldBe "3 days"
            result.badge.tone shouldBe BadgeTone.Primary
            result.grayscale shouldBe false
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
            result.badge.label shouldBe "Expired"
            result.badge.tone shouldBe BadgeTone.Neutral
            result.grayscale shouldBe true
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
    }
}
