package nl.rhaydus.softcover.feature.onboarding.presentation.action

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.component.sheet.isClosing
import nl.rhaydus.softcover.core.component.sheet.isOpen
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
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

class OnLoaderFinishedActionTest {
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
        fun `requests the sheet to close, records the sign-in and sends no event`() = runTest {
            // ----- Arrange -----
            val model = onboardingLoadingSheet(progress = 1f)
            stateFlow.value = stateFlow.value.copy(loadingSheet = SheetSession.Open(model = model))

            // ----- Act -----
            OnLoaderFinishedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingSheet.isClosing shouldBe true
            stateFlow.value.loadingSheet.modelOrNull.shouldNotBeNull() shouldBe model.requestingClose()
            stateFlow.value.loadingSheet.dismissed() shouldBe SignInCompleted
            eventChannel.tryReceive().getOrNull() shouldBe null
        }

        @Test
        fun `leaves a closed sheet closed`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(loadingSheet = SheetSession.Closed)

            // ----- Act -----
            OnLoaderFinishedAction.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.loadingSheet.isOpen shouldBe false
            stateFlow.value.loadingSheet.dismissed() shouldBe null
        }
    }
}
