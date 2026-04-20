package nl.rhaydus.softcover.feature.deadlines.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DeadlineProgressTest {

    private fun buildDeadline(
        bookId: Int = 1,
        deadlineDate: LocalDate = LocalDate.of(2026, 5, 1),
        setAt: LocalDate = LocalDate.of(2026, 4, 1),
        initialPerDay: Float = 10f,
        unit: DeadlineUnit = DeadlineUnit.PAGES,
    ) = BookDeadline(
        bookId = bookId,
        deadlineDate = deadlineDate,
        setAt = setAt,
        initialPerDay = initialPerDay,
        unit = unit,
    )

    @Nested
    inner class Compute {

        @Test
        fun `no units read — requiredPerDay equals total divided by daysRemaining`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 300
            result.daysRemaining shouldBe 30L
            result.requiredPerDay shouldBe 10f
            result.isExpired shouldBe false
            result.unitsBehindSchedule shouldBe 0
        }

        @Test
        fun `fully read — unitsRemaining is zero and requiredPerDay is zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 300,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 0
            result.requiredPerDay shouldBe 0f
            result.isExpired shouldBe false
            result.isOnTrack shouldBe true
            result.unitsBehindSchedule shouldBe 0
        }

        @Test
        fun `expired past — deadline was yesterday and units still remaining`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 19),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 100,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.isExpired shouldBe true
            result.daysRemaining shouldBe -1L
            result.unitsRemaining shouldBe 200
            // daysRemaining < 0 → expectedRemaining = 0 → unitsBehindSchedule = unitsRemaining = 200
            result.unitsBehindSchedule shouldBe 200
        }

        @Test
        fun `expired same-day — deadline is today and units remaining`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 20),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 50,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.isExpired shouldBe true
            result.daysRemaining shouldBe 0L
            result.unitsRemaining shouldBe 250
            result.requiredPerDay shouldBe 250f
            // daysRemaining == 0 → expectedRemaining = 0 → unitsBehindSchedule = unitsRemaining = 250
            result.unitsBehindSchedule shouldBe 250
        }

        @Test
        fun `on track — requiredPerDay is less than or equal to initialPerDay`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.isOnTrack shouldBe true
            result.requiredPerDay shouldBe 10f
            result.unitsBehindSchedule shouldBe 0
        }

        @Test
        fun `behind — requiredPerDay is greater than initialPerDay`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            // Initial was 10/day; now behind — need more than 10/day
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 600,  // double units → 20/day needed, initial was 10
                today = today,
            )

            // ----- Assert -----
            result.isOnTrack shouldBe false
            result.requiredPerDay shouldBe 20f
            // expected = 10 * 30 = 300; unitsRemaining = 600 → 600 - 300 = 300
            result.unitsBehindSchedule shouldBe 300
        }

        @Test
        fun `deadline today with zero units remaining — not expired and requiredPerDay is zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 20),
                initialPerDay = 5f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 300,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 0
            result.requiredPerDay shouldBe 0f
            result.isExpired shouldBe false
            result.unitsBehindSchedule shouldBe 0
        }

        @Test
        fun `total zero — unitsRemaining clamps to zero and requiredPerDay is zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPerDay = 0f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 0,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 0
            result.requiredPerDay shouldBe 0f
            result.isExpired shouldBe false
            result.unitsBehindSchedule shouldBe 0
        }

        @Test
        fun `current exceeds total — unitsRemaining clamps to zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPerDay = 5f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 350,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 0
            result.requiredPerDay shouldBe 0f
            result.unitsBehindSchedule shouldBe 0
        }

        @Test
        fun `output carries the deadline date from the BookDeadline`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadlineDate = LocalDate.of(2026, 6, 15)
            val deadline = buildDeadline(deadlineDate = deadlineDate)

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 100,
                today = today,
            )

            // ----- Assert -----
            result.deadline shouldBe deadlineDate
        }

        @Test
        fun `output carries the initialPerDay from the BookDeadline`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPerDay = 7.5f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 100,
                today = today,
            )

            // ----- Assert -----
            result.initialPerDay shouldBe 7.5f
        }

        @Test
        fun `unit PAGES is carried from BookDeadline to DeadlineProgress`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                unit = DeadlineUnit.PAGES,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 100,
                today = today,
            )

            // ----- Assert -----
            result.unit shouldBe DeadlineUnit.PAGES
        }

        @Test
        fun `unit SECONDS is carried from BookDeadline to DeadlineProgress`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 3600,
                today = today,
            )

            // ----- Assert -----
            result.unit shouldBe DeadlineUnit.SECONDS
        }
    }

    @Nested
    inner class ComputeWithSecondsUnit {

        // Mirrors Compute tests to verify SECONDS and PAGES behave symmetrically.

        private fun buildAudioDeadline(
            deadlineDate: LocalDate = LocalDate.of(2026, 5, 20),
            initialPerDay: Float = 600f,
        ) = buildDeadline(
            deadlineDate = deadlineDate,
            initialPerDay = initialPerDay,
            unit = DeadlineUnit.SECONDS,
        )

        @Test
        fun `no seconds listened — requiredPerDay equals totalSeconds divided by daysRemaining`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildAudioDeadline(initialPerDay = 600f)

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 18000,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 18000
            result.daysRemaining shouldBe 30L
            result.requiredPerDay shouldBe 600f
            result.isExpired shouldBe false
            result.unitsBehindSchedule shouldBe 0
            result.unit shouldBe DeadlineUnit.SECONDS
        }

        @Test
        fun `fully listened — unitsRemaining is zero and requiredPerDay is zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildAudioDeadline(initialPerDay = 600f)

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 18000,
                total = 18000,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 0
            result.requiredPerDay shouldBe 0f
            result.isExpired shouldBe false
            result.isOnTrack shouldBe true
            result.unitsBehindSchedule shouldBe 0
        }

        @Test
        fun `expired past — deadline was yesterday and seconds still remaining`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildAudioDeadline(
                deadlineDate = LocalDate.of(2026, 4, 19),
                initialPerDay = 600f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 3600,
                total = 18000,
                today = today,
            )

            // ----- Assert -----
            result.isExpired shouldBe true
            result.daysRemaining shouldBe -1L
            result.unitsRemaining shouldBe 14400
            result.unitsBehindSchedule shouldBe 14400
        }

        @Test
        fun `behind — requiredPerDay is greater than initialPerDay for seconds`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildAudioDeadline(initialPerDay = 600f)

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 36000,  // double → 1200/day needed, initial was 600
                today = today,
            )

            // ----- Assert -----
            result.isOnTrack shouldBe false
            result.requiredPerDay shouldBe 1200f
            // expected = 600 * 30 = 18000; unitsRemaining = 36000 → behind by 18000
            result.unitsBehindSchedule shouldBe 18000
        }

        @Test
        fun `current exceeds total seconds — unitsRemaining clamps to zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildAudioDeadline(initialPerDay = 600f)

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 20000,
                total = 18000,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 0
            result.requiredPerDay shouldBe 0f
            result.unitsBehindSchedule shouldBe 0
        }
    }

    @Nested
    inner class UnitsBehindSchedule {

        @Test
        fun `reader is exactly on schedule — unitsBehindSchedule is zero`() {
            // ----- Arrange -----
            // 20 days remaining, initialPerDay=10 → expected=200; reader has exactly 200 units left
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 10),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 100,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 200
            result.unitsBehindSchedule shouldBe 0
        }

        @Test
        fun `reader is behind schedule — unitsBehindSchedule matches the arithmetic`() {
            // ----- Arrange -----
            // 10 days remaining, initialPerDay=10 → expected=100; reader still has 150 units left
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 30),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 150,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 150
            // expected = 10 * 10 = 100; behind by 150 - 100 = 50
            result.unitsBehindSchedule shouldBe 50
        }

        @Test
        fun `reader is ahead of schedule — unitsBehindSchedule is zero not negative`() {
            // ----- Arrange -----
            // 10 days remaining, initialPerDay=10 → expected=100; reader only has 40 units left (ahead)
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 30),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 260,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 40
            // expected = 100; ahead by 60 → max(0, 40-100) = 0
            result.unitsBehindSchedule shouldBe 0
        }

        @Test
        fun `deadline in the past — unitsBehindSchedule equals unitsRemaining`() {
            // ----- Arrange -----
            // daysRemaining < 0 → expectedRemaining collapses to 0
            // unitsBehindSchedule = max(0, unitsRemaining - 0) = unitsRemaining
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 15),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 200,
                total = 300,
                today = today,
            )

            // ----- Assert -----
            result.isExpired shouldBe true
            result.unitsRemaining shouldBe 100
            result.unitsBehindSchedule shouldBe 100
        }

        @Test
        fun `total zero — unitsBehindSchedule is zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                current = 0,
                total = 0,
                today = today,
            )

            // ----- Assert -----
            result.unitsRemaining shouldBe 0
            result.unitsBehindSchedule shouldBe 0
        }
    }
}
