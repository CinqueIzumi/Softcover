package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.mapper.settingsSidebarSectionsFor
import nl.rhaydus.softcover.feature.settings.presentation.screen.SettingsCategory
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import nl.rhaydus.toad.ActionScope

internal class OnSettingsCategorySelectedAction(val category: SettingsCategory) : SettingsAction {
    override suspend fun execute(
        dependencies: SettingsScreenDependencies,
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                selectedCategory = category,
                settingsSidebarSections = settingsSidebarSectionsFor(selectedCategory = category),
            )
        }
    }
}
