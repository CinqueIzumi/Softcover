package nl.rhaydus.softcover.feature.widget.settings.randompick

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.lists.domain.usecase.GetAllUserListsUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.feature.widget.randompick.RandomPickStore
import nl.rhaydus.toad.ToadScreenModel

internal class RandomPickSettingsScreenModel(
    store: RandomPickStore,
    getThemeConfiguration: GetThemeConfigurationUseCase,
    getAllUserLists: GetAllUserListsUseCase,
    appDispatchers: AppDispatchers,
    flows: List<RandomPickSettingsCollector>,
) : ToadScreenModel<
    RandomPickSettingsUiState,
    RandomPickSettingsEvent,
    RandomPickSettingsDependencies,
    RandomPickSettingsCollector,
    RandomPickSettingsLocalVariables,
    >(
    initialState = RandomPickSettingsUiState(),
    initialLocalVariables = RandomPickSettingsLocalVariables(),
    initializers = flows,
) {
    override val dependencies = RandomPickSettingsDependencies(
        store = store,
        getThemeConfiguration = getThemeConfiguration,
        getAllUserLists = getAllUserLists,
        mainDispatcher = appDispatchers.main,
        coroutineScope = screenModelScope,
    )

    init {
        startInitializers()
    }

    fun runAction(action: RandomPickSettingsAction) = dispatch(action = action)
}
