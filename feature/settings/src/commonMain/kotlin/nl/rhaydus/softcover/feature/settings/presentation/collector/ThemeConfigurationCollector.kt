package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.paletteChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.themeChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Also maps the Appearance screen's theme and spine-colour pickers off the composition
 * (`component-contract.md` R9): [nl.rhaydus.softcover.feature.settings.presentation.screen.ThemeSection]
 * and [nl.rhaydus.softcover.feature.settings.presentation.screen.SpineColourSection] render
 * [SettingsScreenUiState.themeChoices] / [SettingsScreenUiState.paletteChoices] /
 * [SettingsScreenUiState.spinePalette] / [SettingsScreenUiState.paletteGloss] as-is rather than
 * calling `toSpinePalette()` themselves.
 */
internal class ThemeConfigurationCollector : SettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
        dependencies: SettingsScreenDependencies,
    ) {
        dependencies.getThemeConfigurationUseCase().collectLatest { configuration ->
            val spinePalette = configuration.colorPalette.toSpinePalette()

            scope.setState {
                it.copy(
                    useFloatingBarChecked = configuration.bottomBarStyle == BottomBarStyle.FLOATING,
                    themeMode = configuration.themeMode,
                    colorPalette = configuration.colorPalette,
                    useDynamicColorChecked = configuration.useDynamicColor,
                    spinePalette = spinePalette,
                    paletteGloss = spinePalette.gloss,
                    themeChoices = themeChoicesFor(
                        selected = configuration.themeMode,
                        palette = spinePalette,
                        dynamicColor = configuration.useDynamicColor,
                    ),
                    paletteChoices = paletteChoicesFor(selected = configuration.colorPalette),
                )
            }
        }
    }
}
