package nl.rhaydus.softcover.feature.onboarding.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.onboarding.presentation.event.OnboardingEvent
import nl.rhaydus.softcover.feature.onboarding.presentation.screenmodel.OnboardingDependencies
import nl.rhaydus.softcover.feature.onboarding.presentation.state.LocalOnboardingVariables
import nl.rhaydus.softcover.feature.onboarding.presentation.state.OnboardingUiState
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

    private fun stubDependencies(testScope: kotlinx.coroutines.test.TestScope): OnboardingDependencies {
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
        fun `updates apiKeyValue in state to the new value`() = runTest {
            // ----- Arrange -----
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "new-api-key")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.apiKeyValue shouldBe "new-api-key"
        }

        @Test
        fun `updates apiKeyValue to an empty string when newValue is empty`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(apiKeyValue = "existing-key")
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.apiKeyValue shouldBe ""
        }

        @Test
        fun `preserves other state fields when updating apiKeyValue`() = runTest {
            // ----- Arrange -----
            stateFlow.value = OnboardingUiState(
                apiKeyValue = "old-key",
                saveApiKeyButtonEnabled = false,
                isLoading = true,
            )
            dependencies = stubDependencies(this)
            val action = OnApiKeyValueChangeAction(newValue = "new-key")

            // ----- Act -----
            action.execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.apiKeyValue shouldBe "new-key"
            stateFlow.value.saveApiKeyButtonEnabled shouldBe false
            stateFlow.value.isLoading shouldBe true
        }

        @Test
        fun `updates apiKeyValue when called multiple times sequentially`() = runTest {
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
            stateFlow.value.apiKeyValue shouldBe "second"
        }
    }
}
