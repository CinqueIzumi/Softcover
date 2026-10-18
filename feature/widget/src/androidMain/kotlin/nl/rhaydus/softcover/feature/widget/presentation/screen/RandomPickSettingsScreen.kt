package nl.rhaydus.softcover.feature.widget.presentation.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.koin.koinScreenModel
import nl.rhaydus.designsystem.util.ObserveAsEvents
import nl.rhaydus.softcover.core.component.button.PillButton
import nl.rhaydus.softcover.core.component.chip.ChipEvent
import nl.rhaydus.softcover.core.component.chip.ChipFlow
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.feature.widget.presentation.action.LoadRandomPickSettingsAction
import nl.rhaydus.softcover.feature.widget.presentation.action.OnRandomPickBackClickAction
import nl.rhaydus.softcover.feature.widget.presentation.action.OnRandomPickCadenceSelectedAction
import nl.rhaydus.softcover.feature.widget.presentation.action.OnRandomPickDoneClickAction
import nl.rhaydus.softcover.feature.widget.presentation.action.OnRandomPickSourceSelectedAction
import nl.rhaydus.softcover.feature.widget.presentation.event.RandomPickSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.RandomPickSettingsScreenModel

internal class RandomPickSettingsScreen(
    private val appWidgetId: Int,
) : Screen {
    /** Voyager keys the screen model by this, and the class name alone would hand one widget's model to the next. */
    override val key: ScreenKey = "${RandomPickSettingsScreen::class.qualifiedName}-$appWidgetId"

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<RandomPickSettingsScreenModel>()
        val activity = LocalActivity.current

        val state by screenModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(appWidgetId) {
            screenModel.runAction(LoadRandomPickSettingsAction(appWidgetId = appWidgetId))
        }

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                RandomPickSettingsEvent.Finished -> activity?.finish()
            }
        }

        WidgetSettingsLayout(
            themeConfiguration = state.themeConfiguration,
            topBar = state.topBar,
            onBack = { screenModel.runAction(OnRandomPickBackClickAction) },
        ) {
            SectionHeader(model = state.titleHeader)

            SectionHeader(model = state.sourceHeader)

            ChipFlow(
                set = state.sourceChips,
                onEvent = { event, source ->
                    if (event is ChipEvent.Clicked) screenModel.runAction(OnRandomPickSourceSelectedAction(source = source))
                },
            )

            SectionHeader(model = state.cadenceHeader)

            ChipFlow(
                set = state.cadenceChips,
                onEvent = { event, cadence ->
                    if (event is ChipEvent.Clicked) screenModel.runAction(OnRandomPickCadenceSelectedAction(cadence = cadence))
                },
            )

            PillButton(
                model = state.done,
                onEvent = { screenModel.runAction(OnRandomPickDoneClickAction) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
