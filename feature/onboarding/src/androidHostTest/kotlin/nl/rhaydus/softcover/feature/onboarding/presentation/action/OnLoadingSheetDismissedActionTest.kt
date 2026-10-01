package nl.rhaydus.softcover.feature.onboarding.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnLoadingSheetDismissedActionTest {
    private lateinit var dependencies: OnboardingDependencies
    private lateinit var stateFlow: MutableStateFlow<OnboardingUiState>
    private lateinit var scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(OnboardingUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalOnboardingVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    @Nested
    inner class Execute {
        @Test
        fun `hides the sheet and clears the close request once its dismiss animation finished`() = runTest {
            // ----- Arrange -----
            val initial = stateFlow.value
            stateFlow.value = initial.copy(
                loadingSheet = initial.loadingSheet.copy(
                    isLoading = true,
                    scaffold = initial.loadingSheet.scaffold.copy(closeRequested = true),
                ),
            )

            // ----- Act -----
            OnLoadingSheetDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingSheet.isLoading shouldBe false
            stateFlow.value.loadingSheet.scaffold.closeRequested shouldBe false
        }

        @Test
        fun `keeps the error and the save button state untouched`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                saveApiKeyButtonEnabled = true,
                submissionError = "That API key wasn't accepted.",
            )

            // ----- Act -----
            OnLoadingSheetDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe true
            stateFlow.value.submissionError shouldBe "That API key wasn't accepted."
        }
    }
}
