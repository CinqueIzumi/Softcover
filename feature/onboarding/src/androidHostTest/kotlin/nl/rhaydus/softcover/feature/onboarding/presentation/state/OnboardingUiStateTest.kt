package nl.rhaydus.softcover.feature.onboarding.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.divider.DividerCenter
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.divider.HorizontalDividerSurface
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnboardingUiStateTest {
    @Nested
    inner class Defaults {
        @Test
        fun `orTypeItDivider defaults to the OnboardingOrTypeIt surface labelled Or type it`() {
            // ----- Arrange & Act -----
            val state = OnboardingUiState()

            // ----- Assert -----
            state.orTypeItDivider shouldBe DividerUiModel.Horizontal(
                surface = HorizontalDividerSurface.OnboardingOrTypeIt,
                center = DividerCenter.Label(text = "Or type it"),
            )
        }
    }
}
