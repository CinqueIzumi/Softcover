package nl.rhaydus.softcover.core.component.sheet

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SheetScaffoldUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every SheetLayout value appears in at least one fixture`() {
            // ----- Act -----
            val coveredLayouts = SheetScaffoldUiModel.previews.values.map { it.layout }.toSet()

            // ----- Assert -----
            coveredLayouts shouldBe SheetLayout.entries.toSet()
        }

        @Test
        fun `every header trailing kind appears in at least one fixture`() {
            // ----- Act -----
            val trailingKinds = SheetScaffoldUiModel.previews.values
                .mapNotNull { it.header.trailing }
                .map { it::class }
                .toSet()

            // ----- Assert -----
            trailingKinds shouldBe setOf(
                SheetHeaderTrailing.Jacket::class,
                SheetHeaderTrailing.JacketStack::class,
                SheetHeaderTrailing.Action::class,
            )
        }

        @Test
        fun `fixtures cover a dismissible and a non-dismissible sheet`() {
            // ----- Act -----
            val dismissibleValues = SheetScaffoldUiModel.previews.values.map { it.dismissible }.toSet()

            // ----- Assert -----
            dismissibleValues shouldBe setOf(true, false)
        }

        @Test
        fun `fixtures cover an idle and a busy sheet`() {
            // ----- Act -----
            val busyValues = SheetScaffoldUiModel.previews.values.map { it.busy }.toSet()

            // ----- Assert -----
            busyValues shouldBe setOf(true, false)
        }

        @Test
        fun `fixtures cover a sheet with and without a footer`() {
            // ----- Act -----
            val footerPresence = SheetScaffoldUiModel.previews.values.map { it.footer != null }.toSet()

            // ----- Assert -----
            footerPresence shouldBe setOf(true, false)
        }
    }
}
