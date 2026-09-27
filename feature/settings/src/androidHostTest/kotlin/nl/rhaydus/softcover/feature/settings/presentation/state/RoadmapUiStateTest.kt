package nl.rhaydus.softcover.feature.settings.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RoadmapUiStateTest {
    @Nested
    inner class Defaults {
        @Test
        fun `masthead defaults to the Regular Roadmap masthead`() {
            // ----- Arrange & Act -----
            val state = RoadmapUiState()

            // ----- Assert -----
            state.masthead shouldBe PageMastheadUiModel(
                eyebrow = "Roadmap",
                title = "Roadmap",
                subtitle = "What we're building next, and roughly when.",
            )
        }

        @Test
        fun `masthead size defaults to Regular`() {
            // ----- Arrange & Act -----
            val state = RoadmapUiState()

            // ----- Assert -----
            state.masthead.size shouldBe PageMastheadSize.Regular
        }
    }
}
