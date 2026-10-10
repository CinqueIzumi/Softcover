package nl.rhaydus.softcover.feature.onboarding.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
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
    private lateinit var eventChannel: Channel<OnboardingEvent>
    private lateinit var scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>

    @BeforeEach
    fun setUp() {
        dependencies = mockk(relaxed = true)
        stateFlow = MutableStateFlow(OnboardingUiState())
        eventChannel = Channel(Channel.BUFFERED)
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalOnboardingVariables()),
            eventChannel = eventChannel,
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
        fun `sends SignInCompletedEvent and clears the pending flag when the sign-in was recorded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(signInPending = true)

            // ----- Act -----
            OnLoadingSheetDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.signInPending shouldBe false
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<OnboardingEvent.SignInCompletedEvent>()
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `sends no event when no sign-in was recorded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(signInPending = false)

            // ----- Act -----
            OnLoadingSheetDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe null
        }
    }
}
