package nl.rhaydus.softcover.core.component.sheet

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SheetFooterUiModelTest {
    @Nested
    inner class StackedInvariant {
        @Test
        fun `a stacked footer with only a primary is accepted`() {
            // ----- Act -----
            val footer = SheetFooterUiModel.Stacked(
                primary = SheetFooterButton(label = "Save"),
                secondary = null,
            )

            // ----- Assert -----
            footer.secondary shouldBe null
        }

        @Test
        fun `a stacked footer with only a secondary is accepted`() {
            // ----- Act -----
            val footer = SheetFooterUiModel.Stacked(
                primary = null,
                secondary = SheetFooterButton(label = "Not now"),
            )

            // ----- Assert -----
            footer.primary shouldBe null
        }

        @Test
        fun `a stacked footer with neither button is rejected`() {
            // ----- Act & Assert -----
            shouldThrow<IllegalArgumentException> {
                SheetFooterUiModel.Stacked(
                    primary = null,
                    secondary = null,
                )
            }
        }
    }

    @Nested
    inner class Previews {
        @Test
        fun `every footer kind appears in the fixtures`() {
            // ----- Act -----
            val kinds = SheetFooterUiModel.previews.map { it::class }.toSet()

            // ----- Assert -----
            kinds shouldBe setOf(
                SheetFooterUiModel.Stacked::class,
                SheetFooterUiModel.Inline::class,
            )
        }

        @Test
        fun `fixtures cover a stacked footer with and without a secondary`() {
            // ----- Act -----
            val secondaryPresence = SheetFooterUiModel.previews
                .filterIsInstance<SheetFooterUiModel.Stacked>()
                .map { it.secondary != null }
                .toSet()

            // ----- Assert -----
            secondaryPresence shouldBe setOf(true, false)
        }

        @Test
        fun `fixtures cover an inline footer with a disabled primary`() {
            // ----- Act -----
            val primaryEnabledStates = SheetFooterUiModel.previews
                .filterIsInstance<SheetFooterUiModel.Inline>()
                .map { it.primary.enabled }
                .toSet()

            // ----- Assert -----
            primaryEnabledStates shouldBe setOf(true, false)
        }
    }
}
