package nl.rhaydus.softcover.feature.settings.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetAskForMoodAfterProgressAsFlowUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodPromptToggleRowCollectorTest {
    private lateinit var getAskForMoodAfterProgressAsFlowUseCase: GetAskForMoodAfterProgressAsFlowUseCase
    private lateinit var dependencies: SettingsScreenDependencies
    private lateinit var stateFlow: MutableStateFlow<SettingsScreenUiState>
    private lateinit var scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>
    private lateinit var askForMoodFlow: MutableSharedFlow<Boolean>

    @BeforeEach
    fun setUp() {
        askForMoodFlow = MutableSharedFlow()
        getAskForMoodAfterProgressAsFlowUseCase = mockk()
        stateFlow = MutableStateFlow(SettingsScreenUiState())

        scope = ActionScope(
            stateFlow = stateFlow,
            localVariablesFlow = MutableStateFlow(SettingsLocalVariables()),
            eventChannel = Channel(Channel.BUFFERED),
        )

        every {
            getAskForMoodAfterProgressAsFlowUseCase()
        } returns askForMoodFlow

        dependencies = mockk<SettingsScreenDependencies>(relaxed = true).also { mock ->
            every {
                mock.getAskForMoodAfterProgressAsFlowUseCase
            } returns getAskForMoodAfterProgressAsFlowUseCase
        }
    }

    @Nested
    inner class OnLaunch {
        @Test
        fun `follows the preference as it is switched off and back on`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val collector = MoodPromptToggleRowCollector()

            val job = launch {
                collector.onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            askForMoodFlow.emit(false)
            val afterOff = stateFlow.value.moodPromptToggleRow.checked
            askForMoodFlow.emit(true)

            // ----- Assert -----
            afterOff shouldBe false
            stateFlow.value.moodPromptToggleRow.checked shouldBe true
            job.cancel()
        }
    }
}
