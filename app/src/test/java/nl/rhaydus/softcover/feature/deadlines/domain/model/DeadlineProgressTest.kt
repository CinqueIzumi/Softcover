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
        initialPagesPerDay: Float = 10f,
    ) = BookDeadline(
        bookId = bookId,
        deadlineDate = deadlineDate,
        setAt = setAt,
        initialPagesPerDay = initialPagesPerDay,
    )

    @Nested
    inner class Compute {

        @Test
        fun `no pages read — requiredPagesPerDay equals totalPages divided by daysRemaining`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 0,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.pagesRemaining shouldBe 300
            result.daysRemaining shouldBe 30L
            result.requiredPagesPerDay shouldBe 10f
            result.isExpired shouldBe false
            result.pagesBehindSchedule shouldBe 0
        }

        @Test
        fun `fully read — pagesRemaining is zero and requiredPagesPerDay is zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 300,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.pagesRemaining shouldBe 0
            result.requiredPagesPerDay shouldBe 0f
            result.isExpired shouldBe false
            result.isOnTrack shouldBe true
            result.pagesBehindSchedule shouldBe 0
        }

        @Test
        fun `expired past — deadline was yesterday and pages still remaining`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 19),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 100,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.isExpired shouldBe true
            result.daysRemaining shouldBe -1L
            result.pagesRemaining shouldBe 200
            // daysRemaining < 0 → expectedPagesRemaining = 0 → pagesBehindSchedule = pagesRemaining = 200
            result.pagesBehindSchedule shouldBe 200
        }

        @Test
        fun `expired same-day — deadline is today and pages remaining`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 20),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 50,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.isExpired shouldBe true
            result.daysRemaining shouldBe 0L
            result.pagesRemaining shouldBe 250
            result.requiredPagesPerDay shouldBe 250f
            // daysRemaining == 0 → expectedPagesRemaining = 0 → pagesBehindSchedule = pagesRemaining = 250
            result.pagesBehindSchedule shouldBe 250
        }

        @Test
        fun `on track — requiredPagesPerDay is less than or equal to initialPagesPerDay`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 0,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.isOnTrack shouldBe true
            result.requiredPagesPerDay shouldBe 10f
            result.pagesBehindSchedule shouldBe 0
        }

        @Test
        fun `behind — requiredPagesPerDay is greater than initialPagesPerDay`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            // Initial was 10 p/day; now behind — need more than 10 p/day
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 0,
                totalPages = 600,  // double pages → 20 p/day needed, initial was 10
                today = today,
            )

            // ----- Assert -----
            result.isOnTrack shouldBe false
            result.requiredPagesPerDay shouldBe 20f
            // expected = 10 * 30 = 300; pagesRemaining = 600 → 600 - 300 = 300
            result.pagesBehindSchedule shouldBe 300
        }

        @Test
        fun `deadline today with zero pages remaining — not expired and requiredPagesPerDay is zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 20),
                initialPagesPerDay = 5f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 300,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.pagesRemaining shouldBe 0
            result.requiredPagesPerDay shouldBe 0f
            result.isExpired shouldBe false
            result.pagesBehindSchedule shouldBe 0
        }

        @Test
        fun `totalPages zero — pagesRemaining clamps to zero and requiredPagesPerDay is zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPagesPerDay = 0f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 0,
                totalPages = 0,
                today = today,
            )

            // ----- Assert -----
            result.pagesRemaining shouldBe 0
            result.requiredPagesPerDay shouldBe 0f
            result.isExpired shouldBe false
            result.pagesBehindSchedule shouldBe 0
        }

        @Test
        fun `currentPage exceeds totalPages — pagesRemaining clamps to zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPagesPerDay = 5f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 350,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.pagesRemaining shouldBe 0
            result.requiredPagesPerDay shouldBe 0f
            result.pagesBehindSchedule shouldBe 0
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
                currentPage = 0,
                totalPages = 100,
                today = today,
            )

            // ----- Assert -----
            result.deadline shouldBe deadlineDate
        }

        @Test
        fun `output carries the initialPagesPerDay from the BookDeadline`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPagesPerDay = 7.5f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 0,
                totalPages = 100,
                today = today,
            )

            // ----- Assert -----
            result.initialPagesPerDay shouldBe 7.5f
        }
    }

    @Nested
    inner class PagesBehindSchedule {

        @Test
        fun `reader is exactly on schedule — pagesBehindSchedule is zero`() {
            // ----- Arrange -----
            // 20 days remaining, initialPagesPerDay=10 → expected=200; reader has exactly 200 pages left
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 10),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 100,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.pagesRemaining shouldBe 200
            result.pagesBehindSchedule shouldBe 0
        }

        @Test
        fun `reader is behind schedule — pagesBehindSchedule matches the arithmetic`() {
            // ----- Arrange -----
            // 10 days remaining, initialPagesPerDay=10 → expected=100; reader still has 150 pages left
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 30),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 150,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.pagesRemaining shouldBe 150
            // expected = 10 * 10 = 100; behind by 150 - 100 = 50
            result.pagesBehindSchedule shouldBe 50
        }

        @Test
        fun `reader is ahead of schedule — pagesBehindSchedule is zero not negative`() {
            // ----- Arrange -----
            // 10 days remaining, initialPagesPerDay=10 → expected=100; reader only has 40 pages left (ahead)
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 30),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 260,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.pagesRemaining shouldBe 40
            // expected = 100; ahead by 60 → max(0, 40-100) = 0
            result.pagesBehindSchedule shouldBe 0
        }

        @Test
        fun `deadline in the past — pagesBehindSchedule equals pagesRemaining`() {
            // ----- Arrange -----
            // daysRemaining < 0 → expectedPagesRemaining collapses to 0
            // pagesBehindSchedule = max(0, pagesRemaining - 0) = pagesRemaining
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 4, 15),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 200,
                totalPages = 300,
                today = today,
            )

            // ----- Assert -----
            result.isExpired shouldBe true
            result.pagesRemaining shouldBe 100
            result.pagesBehindSchedule shouldBe 100
        }

        @Test
        fun `totalPages zero — pagesBehindSchedule is zero`() {
            // ----- Arrange -----
            val today = LocalDate.of(2026, 4, 20)
            val deadline = buildDeadline(
                deadlineDate = LocalDate.of(2026, 5, 20),
                initialPagesPerDay = 10f,
            )

            // ----- Act -----
            val result = DeadlineProgress.compute(
                deadline = deadline,
                currentPage = 0,
                totalPages = 0,
                today = today,
            )

            // ----- Assert -----
            result.pagesRemaining shouldBe 0
            result.pagesBehindSchedule shouldBe 0
        }
    }
}
