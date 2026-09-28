package nl.rhaydus.softcover.feature.lists.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.divider.DividerVariant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CreateListUiStateTest {
    @Nested
    inner class Defaults {
        @Test
        fun `quoteDivider defaults to the CreateListQuote variant`() {
            // ----- Arrange & Act -----
            val state = CreateListUiState()

            // ----- Assert -----
            state.quoteDivider.variant shouldBe DividerVariant.CreateListQuote
        }
    }
}
