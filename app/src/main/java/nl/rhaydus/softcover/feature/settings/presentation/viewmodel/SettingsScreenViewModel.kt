package nl.rhaydus.softcover.feature.settings.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserIdUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

class SettingsScreenViewModel(
    private val updateApiKeyUseCase: UpdateApiKeyUseCase,
    private val getApiKeyUseCase: GetApiKeyUseCase,
    private val initializeUserIdUseCase: InitializeUserIdUseCase,
    private val resetUserDataUseCase: ResetUserDataUseCase,
    private val appDispatchers: AppDispatchers,
) : ToadViewModel<SettingsScreenUiState, SettingsScreenEvent>(
    initialState = SettingsScreenUiState()
) {
    // TODO: Ideally I'd want to be able to remove/add these observers in the same way actions are added...
    init {
        screenModelScope.launch(appDispatchers.main) {
            val apiKey = getApiKeyUseCase().getOrDefault(defaultValue = "")

            scope.setState { copy(apiKey = apiKey) }
        }
    }

    override val dependencies = SettingsScreenDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        updateApiKeyUseCase = updateApiKeyUseCase,
        getApiKeyUseCase = getApiKeyUseCase,
        initializeUserIdUseCase = initializeUserIdUseCase,
        resetUserDataUseCase = resetUserDataUseCase,
    )

    fun runAction(action: SettingsAction) = dispatch(action)
}