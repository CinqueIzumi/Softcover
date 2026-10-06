package nl.rhaydus.softcover.core.component.sheet

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SheetFooterUiModelTest {
    @Nested
    inner class StackedInvariant {
        @Test
        fun `a stacked footer with just one of its two buttons is accepted`() {
            // ----- Arrange -----
            val saveButton = SheetFooterButton(label = "Save")
            val notNowButton = SheetFooterButton(label = "Not now")

            // ----- Act -----
            val primaryOnly = SheetFooterUiModel.Stacked(
                primary = saveButton,
                secondary = null,
            )
            val secondaryOnly = SheetFooterUiModel.Stacked(
                primary = null,
                secondary = notNowButton,
            )

            // ----- Assert -----
            primaryOnly.secondary shouldBe null
            secondaryOnly.primary shouldBe null
        }

        @Test
        fun `a stacked footer with neither button is rejected`() {
            // ----- Arrange -----
            val noPrimary: SheetFooterButton? = null
            val noSecondary: SheetFooterButton? = null

            // ----- Act & Assert -----
            shouldThrow<IllegalArgumentException> {
                SheetFooterUiModel.Stacked(
                    primary = noPrimary,
                    secondary = noSecondary,
                )
            }
        }
    }

    @Nested
    inner class Previews {
        @Test
        fun `every footer kind appears in the fixtures`() {
            // ----- Arrange -----
            val fixtures = SheetFooterUiModel.previews.values

            // ----- Act -----
            val kinds = fixtures.map { it::class }.toSet()

            // ----- Assert -----
            kinds shouldBe setOf(
                SheetFooterUiModel.Stacked::class,
                SheetFooterUiModel.Inline::class,
            )
        }

        @Test
        fun `fixtures cover a stacked footer with and without a secondary`() {
            // ----- Arrange -----
            val fixtures = SheetFooterUiModel.previews.values

            // ----- Act -----
            val secondaryPresence = fixtures
                .filterIsInstance<SheetFooterUiModel.Stacked>()
                .map { it.secondary != null }
                .toSet()

            // ----- Assert -----
            secondaryPresence shouldBe setOf(true, false)
        }

        @Test
        fun `fixtures cover an inline footer with a disabled primary`() {
            // ----- Arrange -----
            val fixtures = SheetFooterUiModel.previews.values

            // ----- Act -----
            val primaryEnabledStates = fixtures
                .filterIsInstance<SheetFooterUiModel.Inline>()
                .map { it.primary.enabled }
                .toSet()

            // ----- Assert -----
            primaryEnabledStates shouldBe setOf(true, false)
        }
    }
}
