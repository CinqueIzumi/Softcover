package nl.rhaydus.softcover.feature.settings.presentation.viewmodel

import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadViewModel
import nl.rhaydus.softcover.feature.caching.domain.usecase.InitializeUserBooksUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserIdUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenEvent
import nl.rhaydus.softcover.feature.settings.presentation.flows.SettingsInitializer
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsLocalVariables
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import timber.log.Timber

class SettingsScreenViewModel(
    private val updateApiKeyUseCase: UpdateApiKeyUseCase,
    private val getApiKeyUseCase: GetApiKeyUseCase,
    private val initializeUserIdUseCase: InitializeUserIdUseCase,
    private val resetUserDataUseCase: ResetUserDataUseCase,
    private val initializeUserBooksUseCase: InitializeUserBooksUseCase,
    appDispatchers: AppDispatchers,
    flows: List<SettingsInitializer>,
) : ToadViewModel<SettingsScreenUiState, SettingsScreenEvent, SettingsScreenDependencies, SettingsInitializer, SettingsLocalVariables>(
    initialState = SettingsScreenUiState(),
    initialLocalVariables = SettingsLocalVariables(),
    initializers = flows,
) {
    init {
        screenModelScope.launch(appDispatchers.main) {
            val apiKey = getApiKeyUseCase()
                .onFailure { Timber.e("-=- $it") }
                .getOrDefault(defaultValue = "")

            scope.setState {
                it.copy(apiKey = apiKey)
            }
        }
    }

    override val dependencies = SettingsScreenDependencies(
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        updateApiKeyUseCase = updateApiKeyUseCase,
        getApiKeyUseCase = getApiKeyUseCase,
        initializeUserIdUseCase = initializeUserIdUseCase,
        resetUserDataUseCase = resetUserDataUseCase,
        initializeUserBooksUseCase = initializeUserBooksUseCase,
    )

    fun runAction(action: SettingsAction) = dispatch(action)
}