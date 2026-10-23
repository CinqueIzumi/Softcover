package nl.rhaydus.softcover.showcase.live.sheet.mapper

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import nl.rhaydus.softcover.showcase.live.sheet.model.SheetDemo
import nl.rhaydus.softcover.showcase.live.sheet.model.SheetDemoUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SheetLiveMapperTest {
    private fun selectedKeys(model: SheetDemoUiModel): List<String> = model
        .shouldBeInstanceOf<SheetDemoUiModel.Pinned>()
        .rows.rows
        .filter { it.selected }
        .map { it.key }

    @Nested
    inner class Selecting {
        @Test
        fun `selects only the row with the given key`() {
            // ----- Arrange -----
            val model = sheetDemoModel(demo = SheetDemo.LONG)

            // ----- Act -----
            val result = model.selecting(key = "Mystery")

            // ----- Assert -----
            selectedKeys(result) shouldContainExactly listOf("Mystery")
        }

        @Test
        fun `moves the selection away from the previously selected row`() {
            // ----- Arrange -----
            val model = sheetDemoModel(demo = SheetDemo.LONG).selecting(key = "Gothic")

            // ----- Act -----
            val result = model.selecting(key = "Poetry")

            // ----- Assert -----
            selectedKeys(result) shouldContainExactly listOf("Poetry")
        }

        @Test
        fun `selects nothing when no row has the key`() {
            // ----- Arrange -----
            val model = sheetDemoModel(demo = SheetDemo.LONG)

            // ----- Act -----
            val result = model.selecting(key = "Not a genre")

            // ----- Assert -----
            selectedKeys(result) shouldBe emptyList()
        }

        @Test
        fun `leaves a sheet without rows untouched`() {
            // ----- Arrange -----
            val model = sheetDemoModel(demo = SheetDemo.SHORT)

            // ----- Act -----
            val result = model.selecting(key = "Mystery")

            // ----- Assert -----
            result shouldBeSameInstanceAs model
        }
    }

    @Nested
    inner class Cleared {
        @Test
        fun `deselects every row`() {
            // ----- Arrange -----
            val model = sheetDemoModel(demo = SheetDemo.LONG).selecting(key = "Mystery")

            // ----- Act -----
            val result = model.cleared()

            // ----- Assert -----
            selectedKeys(result) shouldBe emptyList()
        }

        @Test
        fun `leaves a sheet without rows untouched`() {
            // ----- Arrange -----
            val model = sheetDemoModel(demo = SheetDemo.ERROR)

            // ----- Act -----
            val result = model.cleared()

            // ----- Assert -----
            result shouldBeSameInstanceAs model
        }
    }
}
