package nl.rhaydus.softcover.showcase.live.chip.mapper

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.showcase.live.chip.model.ChipSectionUiModel
import nl.rhaydus.softcover.showcase.live.chip.model.ChipSelectionMode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChipMapperTest {
    private fun section(
        mode: ChipSelectionMode,
        selectedKeys: Set<String> = emptySet(),
    ): ChipSectionUiModel = ChipSectionUiModel(
        key = "section",
        header = SectionHeaderUiModel.Section(eyebrow = "Eyebrow"),
        mode = mode,
        set = listOf("a", "b", "c")
            .map {
            ChipUiModel(
                key = it,
                label = it,
                selected = it in selectedKeys,
            ) to it.uppercase()
        }
            .toChipSet(),
    )

    private fun ChipSectionUiModel.selectedKeys(): List<String> = set.chips.filter { it.selected }.map { it.key }

    @Nested
    inner class AfterClick {
        @Test
        fun `multiple mode selects an unselected chip and keeps the others`() {
            // ----- Arrange -----
            val section = section(
                mode = ChipSelectionMode.Multiple,
                selectedKeys = setOf("a"),
            )

            // ----- Act -----
            val result = section.afterClick(chipKey = "b")

            // ----- Assert -----
            result.selectedKeys() shouldBe listOf("a", "b")
        }

        @Test
        fun `multiple mode deselects a selected chip`() {
            // ----- Arrange -----
            val section = section(
                mode = ChipSelectionMode.Multiple,
                selectedKeys = setOf("a", "b"),
            )

            // ----- Act -----
            val result = section.afterClick(chipKey = "a")

            // ----- Assert -----
            result.selectedKeys() shouldBe listOf("b")
        }

        @Test
        fun `single mode moves the selection to the clicked chip`() {
            // ----- Arrange -----
            val section = section(
                mode = ChipSelectionMode.Single,
                selectedKeys = setOf("a"),
            )

            // ----- Act -----
            val result = section.afterClick(chipKey = "c")

            // ----- Assert -----
            result.selectedKeys() shouldBe listOf("c")
        }

        @Test
        fun `single mode keeps the chip selected when it is clicked again`() {
            // ----- Arrange -----
            val section = section(
                mode = ChipSelectionMode.Single,
                selectedKeys = setOf("a"),
            )

            // ----- Act -----
            val result = section.afterClick(chipKey = "a")

            // ----- Assert -----
            result.selectedKeys() shouldBe listOf("a")
        }

        @Test
        fun `removable mode ignores the click`() {
            // ----- Arrange -----
            val section = section(
                mode = ChipSelectionMode.Removable,
                selectedKeys = setOf("a"),
            )

            // ----- Act -----
            val result = section.afterClick(chipKey = "b")

            // ----- Assert -----
            result shouldBe section
        }

        @Test
        fun `an unknown chip key leaves a multiple section unchanged`() {
            // ----- Arrange -----
            val section = section(
                mode = ChipSelectionMode.Multiple,
                selectedKeys = setOf("a"),
            )

            // ----- Act -----
            val result = section.afterClick(chipKey = "missing")

            // ----- Assert -----
            result shouldBe section
        }
    }

    @Nested
    inner class AfterDismiss {
        @Test
        fun `removes the dismissed chip and its payload`() {
            // ----- Arrange -----
            val section = section(mode = ChipSelectionMode.Removable)

            // ----- Act -----
            val result = section.afterDismiss(chipKey = "b")

            // ----- Assert -----
            result.set.chips.map { it.key } shouldBe listOf("a", "c")
            result.set["b"] shouldBe null
            result.set["a"] shouldBe "A"
        }

        @Test
        fun `an unknown chip key leaves the chips unchanged`() {
            // ----- Arrange -----
            val section = section(mode = ChipSelectionMode.Removable)

            // ----- Act -----
            val result = section.afterDismiss(chipKey = "missing")

            // ----- Assert -----
            result.set.chips shouldBe section.set.chips
            result.set.payloadByKey shouldBe section.set.payloadByKey
        }

        @Test
        fun `dismissing the last chip leaves an empty set`() {
            // ----- Arrange -----
            val section = section(mode = ChipSelectionMode.Removable).copy(
                set = persistentListOf(
                    ChipUiModel(
                        key = "a",
                        label = "a",
                    ) to "A",
                ).toChipSet(),
            )

            // ----- Act -----
            val result = section.afterDismiss(chipKey = "a")

            // ----- Assert -----
            result.set.isEmpty shouldBe true
        }
    }
}
