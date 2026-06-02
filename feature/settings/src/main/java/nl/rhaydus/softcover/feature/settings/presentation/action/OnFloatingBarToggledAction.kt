package nl.rhaydus.softcover.feature.settings.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.screenmodel.SettingsScreenDependencies
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import timber.log.Timber

internal class OnFloatingBarToggledAction(val newValue: Boolean) : SettingsAction {
    override suspend fun execute(
        dependencies: SettingsScreenDependencies,
        scope: ActionScope<SettingsScreenUiState, SettingsScreenEvent, SettingsLocalVariables>,
    ) {
        val newStyle: BottomBarStyle = when {
            newValue -> BottomBarStyle.FLOATING
            else -> BottomBarStyle.DOCKED
        }

        dependencies.setBottomBarStyleUseCase(newStyle = newStyle).onFailure {
            Timber.e("$it")
        }
    }
}