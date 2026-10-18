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
import nl.rhaydus.softcover.feature.widget.presentation.action.LoadTrendingSettingsAction
import nl.rhaydus.softcover.feature.widget.presentation.action.OnTrendingBackClickAction
import nl.rhaydus.softcover.feature.widget.presentation.action.OnTrendingCadenceSelectedAction
import nl.rhaydus.softcover.feature.widget.presentation.action.OnTrendingDoneClickAction
import nl.rhaydus.softcover.feature.widget.presentation.action.OnTrendingFeedSelectedAction
import nl.rhaydus.softcover.feature.widget.presentation.action.OnTrendingKindSelectedAction
import nl.rhaydus.softcover.feature.widget.presentation.event.TrendingSettingsEvent
import nl.rhaydus.softcover.feature.widget.presentation.screenmodel.TrendingSettingsScreenModel

internal class TrendingSettingsScreen(
    private val appWidgetId: Int,
) : Screen {
    /** Voyager keys the screen model by this, and the class name alone would hand one widget's model to the next. */
    override val key: ScreenKey = "${TrendingSettingsScreen::class.qualifiedName}-$appWidgetId"

    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<TrendingSettingsScreenModel>()
        val activity = LocalActivity.current

        val state by screenModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(appWidgetId) {
            screenModel.runAction(LoadTrendingSettingsAction(appWidgetId = appWidgetId))
        }

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                TrendingSettingsEvent.Finished -> activity?.finish()
            }
        }

        WidgetSettingsLayout(
            themeConfiguration = state.themeConfiguration,
            topBar = state.topBar,
            onBack = { screenModel.runAction(OnTrendingBackClickAction) },
        ) {
            SectionHeader(model = state.showHeader)

            ChipFlow(
                set = state.kindChips,
                onEvent = { event, kind ->
                    if (event is ChipEvent.Clicked) {
                        screenModel.runAction(OnTrendingKindSelectedAction(kind = kind))
                    }
                },
            )

            SectionHeader(model = state.periodHeader)

            ChipFlow(
                set = state.periodChips,
                onEvent = { event, feed ->
                    if (event is ChipEvent.Clicked) {
                        screenModel.runAction(OnTrendingFeedSelectedAction(feed = feed))
                    }
                },
            )

            SectionHeader(model = state.cadenceHeader)

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
