package nl.rhaydus.softcover.feature.reading.presentation.screen

import io.kotest.matchers.shouldBe
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit

class PlanTodayNudgeTest {
    private fun buildProgress(
        daysRemaining: Long = 5L,
        unitsRemaining: Int = 50,
        requiredPerDay: Float = 10f,
        isExpired: Boolean = false,
        unit: DeadlineUnit = DeadlineUnit.PAGES,
    ) = DeadlineProgress(
        deadline = LocalDate(
            2026,
            6,
            1,
        ).plus(
            daysRemaining.toInt(),
            DateTimeUnit.DAY,
        ),
        daysRemaining = daysRemaining,
        unitsRemaining = unitsRemaining,
        requiredPerDay = requiredPerDay,
        initialPerDay = 10f,
        isExpired = isExpired,
        isOnTrack = true,
        unitsBehindSchedule = 0,
        unit = unit,
    )

    @Nested
    inner class PlanTodayNudgeFor {
        @Test
        fun `returns null when progress is null`() {
            // ----- Arrange -----

            // ----- Act -----
            val result = planTodayNudgeFor(progress = null)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when deadline is expired`() {
            // ----- Arrange -----
            val progress = buildProgress(isExpired = true)

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when unitsRemaining is 0`() {
            // ----- Arrange -----
            val progress = buildProgress(
                unitsRemaining = 0,
                requiredPerDay = 0f,
            )

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when daysRemaining is 0`() {
            // ----- Arrange -----
            val progress = buildProgress(daysRemaining = 0L)

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when daysRemaining is negative`() {
            // ----- Arrange -----
            val progress = buildProgress(daysRemaining = -1L)

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when required-per-day is 0`() {
            // ----- Arrange -----
            val progress = buildProgress(requiredPerDay = 0f)

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns pages message for PAGES unit`() {
            // ----- Arrange -----
            val progress = buildProgress(
                requiredPerDay = 15f,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe "Read 15 pages today to stay on pace."
        }

        @Test
        fun `returns pages message and ceils fractional requiredPerDay for PAGES unit`() {
            // ----- Arrange -----
            val progress = buildProgress(
                requiredPerDay = 14.1f,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe "Read 15 pages today to stay on pace."
        }

        @Test
        fun `returns listen message for SECONDS unit with correct minute conversion`() {
            // ----- Arrange -----
            val progress = buildProgress(
                requiredPerDay = 1800f,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe "Listen 30 min today to stay on pace."
        }

        @Test
        fun `ceils seconds to minutes for SECONDS unit when not exact multiple of 60`() {
            // ----- Arrange -----
            val progress = buildProgress(
                requiredPerDay = 61f,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe "Listen 2 min today to stay on pace."
        }

        @Test
        fun `ceils 1 second to 1 minute for SECONDS unit`() {
            // ----- Arrange -----
            val progress = buildProgress(
                requiredPerDay = 1f,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe "Listen 1 min today to stay on pace."
        }

        @Test
        fun `ceils fractional seconds requiredPerDay before minute conversion`() {
            // ----- Arrange -----
            val progress = buildProgress(
                requiredPerDay = 59.5f,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Act -----
            val result = planTodayNudgeFor(progress = progress)

            // ----- Assert -----
            result shouldBe "Listen 1 min today to stay on pace."
        }
    }
}
