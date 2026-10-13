package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookCardSemanticsTest {
    private fun multiple(selected: Boolean) = BookCardSelection.Multiple(selected = selected)

    @Nested
    inner class Interactive {
        @Test
        fun `multiple selection announces a checkbox toggled on when selected`() {
            // ----- Arrange -----
            val selection = multiple(selected = true)

            // ----- Act -----
            val result = bookCardSemantics(
                interactive = true,
                selection = selection,
            )

            // ----- Assert -----
            result shouldBe BookCardSemantics(
                role = Role.Checkbox,
                selected = null,
                toggleableState = ToggleableState.On,
            )
        }

        @Test
        fun `multiple selection announces a checkbox toggled off when unselected`() {
            // ----- Arrange -----
            val selection = multiple(selected = false)

            // ----- Act -----
            val result = bookCardSemantics(
                interactive = true,
                selection = selection,
            )

            // ----- Assert -----
            result shouldBe BookCardSemantics(
                role = Role.Checkbox,
                selected = null,
                toggleableState = ToggleableState.Off,
            )
        }

        @Test
        fun `single selection announces a selected radio button`() {
            // ----- Arrange -----
            val selection = BookCardSelection.Single(selected = true)

            // ----- Act -----
            val result = bookCardSemantics(
                interactive = true,
                selection = selection,
            )

            // ----- Assert -----
            result shouldBe BookCardSemantics(
                role = Role.RadioButton,
                selected = true,
                toggleableState = null,
            )
        }

        @Test
        fun `single selection announces an unselected radio button`() {
            // ----- Arrange -----
            val selection = BookCardSelection.Single(selected = false)

            // ----- Act -----
            val result = bookCardSemantics(
                interactive = true,
                selection = selection,
            )

            // ----- Assert -----
            result shouldBe BookCardSemantics(
                role = Role.RadioButton,
                selected = false,
                toggleableState = null,
            )
        }

        @Test
        fun `no selection announces a button`() {
            // ----- Arrange -----
            val selection: BookCardSelection? = null

            // ----- Act -----
            val result = bookCardSemantics(
                interactive = true,
                selection = selection,
            )

            // ----- Assert -----
            result shouldBe BookCardSemantics(
                role = Role.Button,
                selected = null,
                toggleableState = null,
            )
        }
    }

    @Nested
    inner class NonInteractive {
        @Test
        fun `multiple selection carries only the selected flag`() {
            // ----- Arrange -----
            val selection = multiple(selected = true)

            // ----- Act -----
            val result = bookCardSemantics(
                interactive = false,
                selection = selection,
            )

            // ----- Assert -----
            result shouldBe BookCardSemantics(
                role = null,
                selected = true,
                toggleableState = null,
            )
        }

        @Test
        fun `single selection carries only the unselected flag`() {
            // ----- Arrange -----
            val selection = BookCardSelection.Single(selected = false)

            // ----- Act -----
            val result = bookCardSemantics(
                interactive = false,
                selection = selection,
            )

            // ----- Assert -----
            result shouldBe BookCardSemantics(
                role = null,
                selected = false,
                toggleableState = null,
            )
        }

        @Test
        fun `no selection carries nothing`() {
            // ----- Arrange -----
            val selection: BookCardSelection? = null

            // ----- Act -----
            val result = bookCardSemantics(
                interactive = false,
                selection = selection,
            )

            // ----- Assert -----
            result shouldBe BookCardSemantics(
                role = null,
                selected = null,
                toggleableState = null,
            )
        }
    }
}
