package nl.rhaydus.softcover.feature.settings.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.dynamicColorSlotFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.paletteChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.spineColourSectionFor
import nl.rhaydus.softcover.feature.settings.presentation.mapper.themeChoicesFor
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.softcover.feature.settings.presentation.util.settingsDisplayToggleAvailability
import nl.rhaydus.toad.ActionScope

/**
 * Also maps the Appearance screen's theme and spine-colour pickers off the composition
 * (`component-contract.md` R9):
 * [nl.rhaydus.softcover.feature.settings.presentation.screen.section.ThemeSection]
 * and [nl.rhaydus.softcover.feature.settings.presentation.screen.section.SpineColourSection] render
 * [SettingsScreenUiState.themeChoices] / [SettingsScreenUiState.paletteChoices] /
 * [SettingsScreenUiState.spinePalette] / [SettingsScreenUiState.spineColourSection] as-is rather than
 * calling `toSpinePalette()` themselves. Also maps [SettingsScreenUiState.dynamicColorSlot], with the
 * platform's [settingsDisplayToggleAvailability] deciding whether dynamic colour is offered at all.
 */
internal class ThemeConfigurationCollector : SettingsCollector {
    override suspend fun onLaunch(
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
        dependencies: SettingsScreenDependencies,
    ) {
        val availability = settingsDisplayToggleAvailability()

        dependencies.getThemeConfigurationUseCase().collectLatest { configuration ->
            val spinePalette = configuration.colorPalette.toSpinePalette()
            val spineColourSection = spineColourSectionFor(
                themeMode = configuration.themeMode,
                useDynamicColor = configuration.useDynamicColor,
                paletteGloss = spinePalette.gloss,
            )

            scope.setState {
                it.copy(
                    themeMode = configuration.themeMode,
                    colorPalette = configuration.colorPalette,
                    dynamicColorSlot = dynamicColorSlotFor(
                        themeMode = configuration.themeMode,
                        supportsDynamicColor = availability.showsDynamicColorToggle,
                        useDynamicColor = configuration.useDynamicColor,
                    ),
                    spineColourSection = spineColourSection,
                    spinePalette = spinePalette,
                    themeChoices = themeChoicesFor(
                        selected = configuration.themeMode,
                        palette = spinePalette,
                        dynamicColor = configuration.useDynamicColor,
                    ),
                    paletteChoices = paletteChoicesFor(
                        selected = configuration.colorPalette,
                        enabled = spineColourSection.interactive,
                    ),
                )
            }
        }
    }
}
