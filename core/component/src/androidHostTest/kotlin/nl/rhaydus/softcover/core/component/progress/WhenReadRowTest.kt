package nl.rhaydus.softcover.core.component.progress

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
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
        fun `picked null defaults to Just now, unselected, with no dismiss trailing`() {
            // ----- Arrange -----
            // ----- Act -----
            val result = whenReadChipModel(
                picked = null,
                now = now,
            )

            // ----- Assert -----
            result.label shouldBe "Just now"
            result.tone shouldBe ChipTone.Tonal
            result.selected shouldBe false
            result.trailing shouldBe null
            result.leading shouldBe ChipLeading.Icon(icon = SoftcoverIcon.DateRange)
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
            result.tone shouldBe ChipTone.Tonal
            result.selected shouldBe true
            result.trailing shouldBe ChipTrailing.Dismiss(label = "Reset to just now")
            result.leading shouldBe ChipLeading.Icon(icon = SoftcoverIcon.DateRange)
        }
    }
}
