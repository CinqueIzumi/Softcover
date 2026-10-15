package nl.rhaydus.softcover.feature.widget.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SoftcoverTheme
import nl.rhaydus.softcover.core.designsystem.presentation.theme.Spacing
import nl.rhaydus.softcover.core.presentation.theme.isDark
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.feature.widget.settings.action.LoadSettingsAction
import nl.rhaydus.softcover.feature.widget.settings.action.OnCadenceSelectedAction
import nl.rhaydus.softcover.feature.widget.settings.action.OnDoneClickAction
import nl.rhaydus.softcover.feature.widget.settings.action.OnSourceSelectedAction
import nl.rhaydus.softcover.feature.widget.settings.action.RandomPickSettingsAction

internal class RandomPickSettingsScreen(
    private val appWidgetId: Int,
    private val onFinished: () -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<RandomPickSettingsScreenModel>()

        val state by screenModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(appWidgetId) {
            screenModel.runAction(LoadSettingsAction(appWidgetId = appWidgetId))
        }

        ObserveAsEvents(flow = screenModel.events) {
            when (it) {
                RandomPickSettingsEvent.Finished -> onFinished()
            }
        }

        SoftcoverTheme(
            palette = state.themeConfiguration.colorPalette.toSpinePalette(),
            darkTheme = state.themeConfiguration.themeMode.isDark(),
            dynamicColor = state.themeConfiguration.useDynamicColor,
        ) {
            RandomPickSettingsLayout(
                state = state,
                runAction = screenModel::runAction,
            )
        }
    }

    @Composable
    private fun RandomPickSettingsLayout(
        state: RandomPickSettingsUiState,
        runAction: (RandomPickSettingsAction) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = Spacing.gutter,
                    vertical = Spacing.lg,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            SectionHeader(model = state.titleHeader)

            SectionHeader(model = state.sourceHeader)

            ChipFlow(
                set = state.sourceChips,
                onEvent = { event, source ->
                    if (event is ChipEvent.Clicked) runAction(OnSourceSelectedAction(source = source))
                },
            )

            SectionHeader(model = state.cadenceHeader)

            ChipFlow(
                set = state.cadenceChips,
                onEvent = { event, cadence ->
                    if (event is ChipEvent.Clicked) runAction(OnCadenceSelectedAction(cadence = cadence))
                },
            )

            PillButton(
                model = state.done,
                onEvent = { runAction(OnDoneClickAction) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
