package nl.rhaydus.softcover.feature.settings.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadScreenModel
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.SetBottomBarStyleUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.flows.SettingsInitializer
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

class SettingsScreenScreenModel(
    private val setBottomBarStyleUseCase: SetBottomBarStyleUseCase,
    private val getThemeConfigurationUseCase: GetThemeConfigurationUseCase,
    appDispatchers: AppDispatchers,
    flows: List<SettingsInitializer>,
) : ToadScreenModel<SettingsScreenUiState, SettingsScreenEvent, SettingsScreenDependencies, SettingsInitializer, SettingsLocalVariables>(
    initialState = SettingsScreenUiState(),
    initialLocalVariables = SettingsLocalVariables(),
    initializers = flows,
) {
    override val dependencies = SettingsScreenDependencies(
        getThemeConfigurationUseCase = getThemeConfigurationUseCase,
        setBottomBarStyleUseCase = setBottomBarStyleUseCase,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(action: SettingsAction) = dispatch(action)
}