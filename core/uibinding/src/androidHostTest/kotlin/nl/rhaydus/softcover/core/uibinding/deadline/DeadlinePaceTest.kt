package nl.rhaydus.softcover.core.uibinding.deadline

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DeadlinePaceTest {
    @Nested
    inner class FormatUnitAmount {
        @Test
        fun `one page is singular`() {
            // ----- Act -----
            val result = formatUnitAmount(
                amount = 1,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result shouldBe "1 page"
        }

        @Test
        fun `zero and many pages are plural`() {
            // ----- Act -----
            val zero = formatUnitAmount(
                amount = 0,
                unit = DeadlineUnit.PAGES,
            )
            val many = formatUnitAmount(
                amount = 35,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            zero shouldBe "0 pages"
            many shouldBe "35 pages"
        }

        @Test
        fun `seconds read as hours and minutes`() {
            // ----- Act -----
            val result = formatUnitAmount(
                amount = 3_900,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Assert -----
            result shouldBe "1h 5m"
        }
    }

    @Nested
    inner class FormatPacePerDay {
        @Test
        fun `pages pace carries the per-day suffix`() {
            // ----- Act -----
            val result = formatPacePerDay(
                amount = 35,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            result shouldBe "35 pages/day"
        }

        @Test
        fun `seconds pace carries the per-day suffix`() {
            // ----- Act -----
            val result = formatPacePerDay(
                amount = 3_900,
                unit = DeadlineUnit.SECONDS,
            )

            // ----- Assert -----
            result shouldBe "1h 5m/day"
        }
    }
}
