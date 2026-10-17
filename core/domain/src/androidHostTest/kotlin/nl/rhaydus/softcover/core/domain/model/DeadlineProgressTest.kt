package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeadlineProgressTest {
    private fun buildDeadline(
        bookId: Int = 1,
        deadlineDate: LocalDate = LocalDate(
            2026,
            5,
            1,
        ),
        setAt: LocalDate = LocalDate(
            2026,
            4,
            1,
        ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    5,
                    20,
                ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    5,
                    20,
                ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    4,
                    19,
                ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    4,
                    20,
                ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    5,
                    20,
                ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            // Initial was 10/day; now behind — need more than 10/day
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    5,
                    20,
                ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    4,
                    20,
                ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    5,
                    20,
                ),
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
    }

    @Nested
    inner class UnitsBehindSchedule {
        @Test
        fun `reader is exactly on schedule — unitsBehindSchedule is zero`() {
            // ----- Arrange -----
            // 20 days remaining, initialPerDay=10 → expected=200; reader has exactly 200 units left
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    5,
                    10,
                ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    4,
                    30,
                ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    4,
                    30,
                ),
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
            val today = LocalDate(
                2026,
                4,
                20,
            )
            val deadline = buildDeadline(
                deadlineDate = LocalDate(
                    2026,
                    4,
                    15,
                ),
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
    }
}
