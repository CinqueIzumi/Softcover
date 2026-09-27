package nl.rhaydus.softcover.core.component.chip

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChipSetTest {
    @Nested
    inner class ToChipSet {
        @Test
        fun `chips keep the order of the source pairs`() {
            // ----- Arrange -----
            val first = ChipUiModel(
                key = "first",
                label = "First",
            )
            val second = ChipUiModel(
                key = "second",
                label = "Second",
            )
            val pairs = listOf(first to "a", second to "b")

            // ----- Act -----
            val chipSet = pairs.toChipSet()

            // ----- Assert -----
            chipSet.chips shouldBe listOf(first, second)
        }

        @Test
        fun `each key maps to its payload`() {
            // ----- Arrange -----
            val first = ChipUiModel(
                key = "first",
                label = "First",
            )
            val second = ChipUiModel(
                key = "second",
                label = "Second",
            )
            val pairs = listOf(first to "a", second to "b")

            // ----- Act -----
            val chipSet = pairs.toChipSet()

            // ----- Assert -----
            chipSet["first"] shouldBe "a"
            chipSet["second"] shouldBe "b"
        }

        @Test
        fun `an empty source produces an empty chip set`() {
            // ----- Arrange -----
            val pairs = emptyList<Pair<ChipUiModel, String>>()

            // ----- Act -----
            val chipSet = pairs.toChipSet()

            // ----- Assert -----
            chipSet.chips shouldBe emptyList()
            chipSet.payloadByKey shouldBe emptyMap()
        }
    }

    @Nested
    inner class Get {
        @Test
        fun `an unknown key returns null`() {
            // ----- Arrange -----
            val chip = ChipUiModel(
                key = "known",
                label = "Known",
            )
            val chipSet = listOf(chip to "payload").toChipSet()

            // ----- Act -----
            val result = chipSet["unknown"]

            // ----- Assert -----
            result shouldBe null
        }
    }

    @Nested
    inner class IsEmpty {
        @Test
        fun `a default chip set is empty`() {
            // ----- Arrange & Act -----
            val chipSet = ChipSet<String>()

            // ----- Assert -----
            chipSet.isEmpty shouldBe true
        }

        @Test
        fun `a chip set built from one pair is not empty`() {
            // ----- Arrange -----
            val chip = ChipUiModel(
                key = "known",
                label = "Known",
            )

            // ----- Act -----
            val chipSet = listOf(chip to "payload").toChipSet()

            // ----- Assert -----
            chipSet.isEmpty shouldBe false
        }
    }

    @Nested
    inner class Defaults {
        @Test
        fun `chips defaults to empty`() {
            // ----- Arrange & Act -----
            val chipSet = ChipSet<String>()

            // ----- Assert -----
            chipSet.chips shouldBe emptyList()
        }

        @Test
        fun `payloadByKey defaults to empty`() {
            // ----- Arrange & Act -----
            val chipSet = ChipSet<String>()

            // ----- Assert -----
            chipSet.payloadByKey shouldBe emptyMap()
        }
    }
}
