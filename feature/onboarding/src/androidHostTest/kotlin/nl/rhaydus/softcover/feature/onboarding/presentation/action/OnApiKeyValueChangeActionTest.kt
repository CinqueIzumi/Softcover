package nl.rhaydus.softcover.feature.onboarding.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
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
        fun `updates apiKeyField text in state to the new value`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "new-api-key")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.apiKeyField.text shouldBe "new-api-key"
        }

        @Test
        fun `updates apiKeyField text to an empty string when newValue is empty`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "existing-key")
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.apiKeyField.text shouldBe ""
        }

        @Test
        fun `preserves other state fields when updating apiKeyField text`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "old-key").copy(
                saveApiKeyButtonEnabled = false,
                loadingSheet = OnboardingUiState().loadingSheet.copy(isLoading = true),
            )
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "new-key")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.apiKeyField.text shouldBe "new-key"
            stateFlow.value.saveApiKeyButtonEnabled shouldBe false
            stateFlow.value.loadingSheet.isLoading shouldBe true
            stateFlow.value.loadingSheet.scaffold.header.section shouldBe
                SectionHeaderUiModel.Section(
                    eyebrow = "Setting up",
                    headline = "Pulling your library together.",
                    description = "Depending on its size, this might take a moment.",
                )
        }

        @Test
        fun `updates apiKeyField text when called multiple times sequentially`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)

            // ----- Act -----
            OnApiKeyValueChangeAction(newValue = "first").execute(
                dependencies = dependencies,
                scope = scope,
            )
            OnApiKeyValueChangeAction(newValue = "second").execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.apiKeyField.text shouldBe "second"
        }

        @Test
        fun `keeps the other api key field properties when the text changes`() = runTest {
            // ----- Arrange -----
            val initial = OnboardingUiState().apiKeyField
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "new-key")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.apiKeyField shouldBe initial.copy(text = "new-key")
        }

        @Test
        fun `clears submissionError when apiKeyField text changes`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateWithApiKey(text = "old-key").copy(
                submissionError = "That API key wasn't accepted. Double-check it and try again.",
            )
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "new-key")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.submissionError shouldBe null
        }
    }
}
