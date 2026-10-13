package nl.rhaydus.softcover.feature.onboarding.presentation.action

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.LoadingSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.softcover.feature.onboarding.presentation.state.SignInCompleted
import nl.rhaydus.softcover.feature.onboarding.presentation.state.onboardingLoadingSheet
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

    private fun openSheet(): SheetSession<LoadingSheetUiModel, SignInCompleted> =
        SheetSession.Open(model = onboardingLoadingSheet(progress = 1f))

    @Nested
    inner class Execute {
        @Test
        fun `closes the session once the dismiss animation finished`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(loadingSheet = openSheet().requestClose())

            // ----- Act -----
            OnLoadingSheetDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingSheet shouldBe SheetSession.Closed
            stateFlow.value.loadingSheet.isOpen shouldBe false
        }

        @Test
        fun `sends SignInCompletedEvent exactly once when the sign-in was recorded`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(
                loadingSheet = openSheet().requestClose(pending = SignInCompleted),
            )

            // ----- Act -----
            OnLoadingSheetDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull().shouldBeInstanceOf<OnboardingEvent.SignInCompletedEvent>()
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `sends no event when the sheet closed after a failed sign-in`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(loadingSheet = openSheet().requestClose())

            // ----- Act -----
            OnLoadingSheetDismissedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `sends no event when the session is already closed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(loadingSheet = SheetSession.Closed)

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
