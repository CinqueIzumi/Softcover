package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryPillButtonsTest {
    @Nested
    inner class FilterPillButton {
        @Test
        fun `shows the active indicator when the tab has filters`() {
            // ----- Arrange -----
            val active = true

            // ----- Act -----
            val pill = filterPillButton(active = active)

            // ----- Assert -----
            pill.content.shouldBeInstanceOf<PillButtonContent.Label>().indicator shouldBe true
        }

        @Test
        fun `hides the active indicator when the tab has no filters`() {
            // ----- Arrange -----
            val active = false

            // ----- Act -----
            val pill = filterPillButton(active = active)

            // ----- Assert -----
            pill.content.shouldBeInstanceOf<PillButtonContent.Label>().indicator shouldBe false
        }

        @Test
        fun `keeps the same key whether or not the indicator is shown`() {
            // ----- Arrange -----
            val activePill = filterPillButton(active = true)

            // ----- Act -----
            val inactivePill = filterPillButton(active = false)

            // ----- Assert -----
            inactivePill.key shouldBe activePill.key
        }
    }
}
