package nl.rhaydus.softcover.core.component.progress

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WhenReadRowTest {
    private val now = LocalDateTime(
        2024,
        3,
        1,
        10,
        0,
    )

    @Nested
    inner class WhenReadChipModel {
        @Test
        fun `picked null defaults to Just now, unselected, with no dismiss label`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = whenReadChipModel(
                picked = null,
                now = now,
            )

            // ----- Assert -----
            result.label shouldBe "Just now"
            result.variant shouldBe ChipVariant.Tonal(selected = false)
            result.dismissLabel shouldBe null
            result.leadingIcon shouldBe SoftcoverIcon.DateRange
            result.interaction shouldBe ChipInteraction.Clickable
        }

        @Test
        fun `picked non-null formats the label, selects the chip, and offers a reset`() {
            // ----- Arrange -----
            val picked = LocalDateTime(
                2024,
                3,
                1,
                14,
                30,
            )

            // ----- Act -----
            val result = whenReadChipModel(
                picked = picked,
                now = now,
            )

            // ----- Assert -----
            result.label shouldBe "Today, 14:30"
            result.variant shouldBe ChipVariant.Tonal(selected = true)
            result.dismissLabel shouldBe "Reset to just now"
            result.leadingIcon shouldBe SoftcoverIcon.DateRange
        }
    }
}
