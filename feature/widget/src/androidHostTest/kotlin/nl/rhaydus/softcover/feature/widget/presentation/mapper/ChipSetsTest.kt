package nl.rhaydus.softcover.feature.widget.presentation.mapper

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChipSetsTest {
    @Nested
    inner class ToChipSet {
        @Test
        fun `each choice gets its own key that maps back to its payload`() {
            // ----- Arrange -----
            val choices = listOf(
                "One" to 1,
                "Two" to 2,
                "Three" to 3,
            )

            // ----- Act -----
            val result = choices.toChipSet(selected = 2)

            // ----- Assert -----
            result.chips.map { it.label } shouldContainExactly listOf("One", "Two", "Three")
            result.chips.map { result.payloadByKey[it.key] } shouldContainExactly listOf(1, 2, 3)
            result.chips.map { it.key }.distinct().size shouldBe 3
        }

        @Test
        fun `only the chip of the selected payload is selected`() {
            // ----- Arrange -----
            val choices = listOf(
                "One" to 1,
                "Two" to 2,
            )

            // ----- Act -----
            val result = choices.toChipSet(selected = 2)

            // ----- Assert -----
            result.chips.filter { it.selected }.map { it.label } shouldContainExactly listOf("Two")
        }
    }
}
