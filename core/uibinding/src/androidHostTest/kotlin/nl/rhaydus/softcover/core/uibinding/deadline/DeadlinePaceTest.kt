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
            // ----- Arrange -----
            val amount = 1
            val unit = DeadlineUnit.PAGES

            // ----- Act -----
            val result = formatUnitAmount(
                amount = amount,
                unit = unit,
            )

            // ----- Assert -----
            result shouldBe "1 page"
        }

        @Test
        fun `zero and many pages are plural`() {
            // ----- Arrange -----
            val noPages = 0
            val manyPages = 35

            // ----- Act -----
            val zero = formatUnitAmount(
                amount = noPages,
                unit = DeadlineUnit.PAGES,
            )
            val many = formatUnitAmount(
                amount = manyPages,
                unit = DeadlineUnit.PAGES,
            )

            // ----- Assert -----
            zero shouldBe "0 pages"
            many shouldBe "35 pages"
        }

        @Test
        fun `seconds read as hours and minutes`() {
            // ----- Arrange -----
            val amount = 3_900
            val unit = DeadlineUnit.SECONDS

            // ----- Act -----
            val result = formatUnitAmount(
                amount = amount,
                unit = unit,
            )

            // ----- Assert -----
            result shouldBe "1h 5m"
        }
    }
}
