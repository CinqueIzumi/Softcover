package nl.rhaydus.softcover.feature.onboarding.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.field.TextFieldError
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnApiKeyValueChangeActionTest {
    private lateinit var dependencies: OnboardingDependencies
    private lateinit var stateFlow: MutableStateFlow<OnboardingUiState>
    private lateinit var scope: ActionScope<OnboardingUiState, OnboardingEvent, LocalOnboardingVariables>

    @BeforeEach
    fun setUp() {
        stateFlow = MutableStateFlow(OnboardingUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(LocalOnboardingVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
    }

    private fun stateWithApiKey(text: String): OnboardingUiState = OnboardingUiState().let {
        it.copy(apiKeyField = it.apiKeyField.copy(text = text))
    }

    private fun stubDependencies(testScope: TestScope): OnboardingDependencies {
        val dispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        return mockk<OnboardingDependencies>(relaxed = true).also { mock ->
            every {
                mock.coroutineScope
            } returns testScope

            every {
                mock.mainDispatcher
            } returns dispatcher
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `stores the typed text and clears a previous submissionError`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "old-key").copy(submissionError = "previous error")
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "new-key")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.apiKeyField.text shouldBe "new-key"
            stateFlow.value.submissionError shouldBe null
        }

        @Test
        fun `enables the save button when the typed text is not blank`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "new-key")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe true
        }

        @Test
        fun `disables the save button when the typed text is blank`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "old-key").copy(saveApiKeyButtonEnabled = true)
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "   ")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe false
        }

        @Test
        fun `keeps the save button disabled for a non-blank value while loading`() = runTest {
            // ----- Arrange -----
            val base = stateWithApiKey(text = "old-key")
            stateFlow.value = base.copy(loadingSheet = base.loadingSheet.copy(isLoading = true))
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "new-key")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.saveApiKeyButtonEnabled shouldBe false
        }

        @Test
        fun `clears a previous field error when the text changes`() = runTest {
            // ----- Arrange -----
            val base = stateWithApiKey(text = "old-key")
            stateFlow.value = base.copy(
                apiKeyField = base.apiKeyField.copy(error = TextFieldError(message = "rejected")),
            )
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "new-key")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.apiKeyField.error shouldBe null
        }
    }
}
