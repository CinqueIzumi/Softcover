package nl.rhaydus.softcover.feature.settings.presentation.action

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.presentation.FakeHomeScreenWidgets
import nl.rhaydus.softcover.feature.settings.presentation.event.WidgetsSettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.WidgetsSettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OnWidgetsResumedActionTest {
    private lateinit var widgets: FakeHomeScreenWidgets
    private lateinit var dependencies: WidgetsSettingsDependencies
    private lateinit var stateFlow: MutableStateFlow<WidgetsSettingsUiState>
    private lateinit var scope: ActionScope<WidgetsSettingsUiState, WidgetsSettingsEvent, WidgetsSettingsLocalVariables>

    @BeforeEach
    fun setUp() {
        widgets = FakeHomeScreenWidgets()
        stateFlow = MutableStateFlow(WidgetsSettingsUiState())
        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(WidgetsSettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )
        dependencies = mockk<WidgetsSettingsDependencies>(relaxed = true).also { mock ->
            every {
                mock.homeScreenWidgets
            } returns widgets
        }
    }

    @Nested
    inner class Execute {
        @Test
        fun `turns canPin off when the launcher stopped supporting pinning`() = runTest {
            // ----- Arrange -----
            stateFlow.value = stateFlow.value.copy(canPin = true)
            widgets.canPinNow = false

            // ----- Act -----
            OnWidgetsResumedAction().execute(
                dependencies = dependencies,
                scope = scope,
            )

            // ----- Assert -----
            stateFlow.value.canPin shouldBe false
        }
    }
}
