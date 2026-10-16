package nl.rhaydus.softcover.feature.widget.settings.randompick

import androidx.activity.compose.LocalActivity
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

internal class RandomPickSettingsScreen(
    private val appWidgetId: Int,
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<RandomPickSettingsScreenModel>()
        val activity = LocalActivity.current

        val state by screenModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(appWidgetId) {
            screenModel.runAction(LoadSettingsAction(appWidgetId = appWidgetId))
        }

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                RandomPickSettingsEvent.Finished -> activity?.finish()
            }
        }

        WidgetSettingsLayout(themeConfiguration = state.themeConfiguration) {
            SectionHeader(model = state.titleHeader)

            SectionHeader(model = state.sourceHeader)

            ChipFlow(
                set = state.sourceChips,
                onEvent = { event, source ->
                    if (event is ChipEvent.Clicked) screenModel.runAction(OnSourceSelectedAction(source = source))
                },
            )

            SectionHeader(model = state.cadenceHeader)

            ChipFlow(
                set = state.cadenceChips,
                onEvent = { event, cadence ->
                    if (event is ChipEvent.Clicked) screenModel.runAction(OnCadenceSelectedAction(cadence = cadence))
                },
            )

            PillButton(
                model = state.done,
                onEvent = { screenModel.runAction(OnDoneClickAction) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
