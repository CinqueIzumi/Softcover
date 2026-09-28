package nl.rhaydus.softcover.feature.onboarding.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.divider.DividerUiModel
import nl.rhaydus.softcover.core.component.divider.DividerVariant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnboardingUiStateTest {
    @Nested
    inner class Defaults {
        @Test
        fun `orTypeItDivider defaults to the OnboardingOrTypeIt variant labelled Or type it`() {
            // ----- Arrange & Act -----
            val state = OnboardingUiState()

            // ----- Assert -----
            state.orTypeItDivider shouldBe DividerUiModel(DividerVariant.OnboardingOrTypeIt(label = "Or type it"))
        }
    }
}
