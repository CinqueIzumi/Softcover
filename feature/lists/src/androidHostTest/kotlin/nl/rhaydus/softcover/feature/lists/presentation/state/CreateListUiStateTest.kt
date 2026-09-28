package nl.rhaydus.softcover.feature.lists.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.divider.DividerCenter
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.divider.HorizontalDividerSurface
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CreateListUiStateTest {
    @Nested
    inner class Defaults {
        @Test
        fun `quoteDivider defaults to the CreateListQuote surface with a quote mark centre`() {
            // ----- Arrange & Act -----
            val state = CreateListUiState()

            // ----- Assert -----
            state.quoteDivider shouldBe DividerUiModel.Horizontal(
                surface = HorizontalDividerSurface.CreateListQuote,
                center = DividerCenter.QuoteMark,
            )
        }
    }
}
