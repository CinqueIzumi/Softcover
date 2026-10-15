package nl.rhaydus.softcover.feature.widget.settings.trending

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.feature.widget.settings.WidgetSettingsLayout

internal class TrendingSettingsScreen(
    private val appWidgetId: Int,
    private val onFinished: () -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<TrendingSettingsScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(appWidgetId) {
            screenModel.runAction(LoadTrendingSettingsAction(appWidgetId = appWidgetId))
        }

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                TrendingSettingsEvent.Finished -> onFinished()
            }
        }

        WidgetSettingsLayout(themeConfiguration = state.themeConfiguration) {
            SectionHeader(model = state.titleHeader)

            ChipFlow(
                set = state.cadenceChips,
                onEvent = { event, cadence ->
                    if (event is ChipEvent.Clicked) {
                        screenModel.runAction(OnTrendingCadenceSelectedAction(cadence = cadence))
                    }
                },
            )

            PillButton(
                model = state.done,
                onEvent = { screenModel.runAction(OnTrendingDoneClickAction) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
