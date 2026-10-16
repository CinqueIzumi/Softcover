package nl.rhaydus.softcover.feature.settings.presentation.collector

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgetKind
import nl.rhaydus.softcover.core.presentation.widget.PlacedHomeScreenWidget
import nl.rhaydus.softcover.feature.settings.presentation.FakeHomeScreenWidgets
import nl.rhaydus.softcover.feature.settings.presentation.event.WidgetsSettingsEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.WidgetsSettingsDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.WidgetsSettingsUiState
import nl.rhaydus.toad.ActionScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PlacedWidgetsCollectorTest {
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
    inner class OnLaunch {
        @Test
        fun `places the latest emission in state as rows`() = runTest(UnconfinedTestDispatcher()) {
            // ----- Arrange -----
            val job = launch {
                PlacedWidgetsCollector().onLaunch(
                    scope = scope,
                    dependencies = dependencies,
                )
            }

            // ----- Act -----
            widgets.placed.emit(
                listOf(
                    PlacedHomeScreenWidget(
                        id = 1,
                        kind = HomeScreenWidgetKind.Trending,
                        hasSettings = false,
                    ),
                ),
            )
            widgets.placed.emit(
                listOf(
                    PlacedHomeScreenWidget(
                        id = 4,
                        kind = HomeScreenWidgetKind.RandomPick,
                        hasSettings = true,
                    ),
                    PlacedHomeScreenWidget(
                        id = 5,
                        kind = HomeScreenWidgetKind.Trending,
                        hasSettings = false,
                    ),
                ),
            )

            // ----- Assert -----
            stateFlow.value.placedRows.map { it.widgetId } shouldBe listOf(4, 5)
            job.cancel()
        }
    }
}
