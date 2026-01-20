package nl.rhaydus.softcover.feature.settings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserIdUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUSeCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val updateApiKeyUseCase: UpdateApiKeyUseCase,
    private val getApiKeyUseCase: GetApiKeyUseCase,
    private val initializeUserIdUseCase: InitializeUserIdUseCase,
    private val resetUserDataUseCase: ResetUserDataUSeCase,
    @param:Named("mainDispatcher") private val mainDispatcher: CoroutineDispatcher,
) : ToadViewModel<SettingsScreenUiState, SettingsScreenEvent>(
    initialState = SettingsScreenUiState()
) {
    // TODO: Ideally I'd want to be able to remove/add these observers in the same way actions are added...
    init {
        viewModelScope.launch(mainDispatcher) {
            val apiKey = getApiKeyUseCase().getOrDefault(defaultValue = "")

            scope.setState { copy(apiKey = apiKey) }
        }
    }

    override val dependencies = SettingsScreenDependencies(
        coroutineScope = viewModelScope,
        mainDispatcher = mainDispatcher,
        updateApiKeyUseCase = updateApiKeyUseCase,
        getApiKeyUseCase = getApiKeyUseCase,
        initializeUserIdUseCase = initializeUserIdUseCase,
        resetUserDataUseCase = resetUserDataUseCase,
    )

    fun runAction(action: SettingsAction) = dispatch(action)
}