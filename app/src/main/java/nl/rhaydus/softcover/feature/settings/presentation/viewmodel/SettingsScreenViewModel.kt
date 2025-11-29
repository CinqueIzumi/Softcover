package nl.rhaydus.softcover.feature.settings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.rhaydus.softcover.core.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.InitializeUserIdUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.ResetUserDataUSeCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.UpdateApiKeyUseCase
import nl.rhaydus.softcover.feature.settings.presentation.event.SettingsScreenUiEvent
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SettingsScreenViewModel @Inject constructor(
    private val updateApiKeyUseCase: UpdateApiKeyUseCase,
    private val getApiKeyUseCase: GetApiKeyUseCase,
    private val initializeUserIdUseCase: InitializeUserIdUseCase,
    private val resetUserDataUseCase: ResetUserDataUSeCase,
) : ViewModel() {
    private val _apiKeyFlow = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SettingsScreenUiState> = _apiKeyFlow.mapLatest { key: String ->
        SettingsScreenUiState(apiKey = key)
    }.onStart {
        val apiKey = getApiKeyUseCase().getOrDefault(defaultValue = "")

        setApiKeyValue(newValue = apiKey)
    }.stateIn(
        scope = viewModelScope,
        initialValue = SettingsScreenUiState(),
        started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds)
    )

    fun onEvent(event: SettingsScreenUiEvent) {
        when (event) {
            SettingsScreenUiEvent.OnSaveApiKeyClick -> handleOnSaveApiKeyClick()
            is SettingsScreenUiEvent.OnApiKeyValueChanged -> handleOnApiKeyValueChanged(newValue = event.newValue)
        }
    }

    private fun handleOnApiKeyValueChanged(newValue: String) = setApiKeyValue(newValue = newValue)

    private fun handleOnSaveApiKeyClick() {
        val updatedKey = uiState.value.apiKey
            .removePrefix("Bearer")
            .trim()

        viewModelScope.launch {
            resetUserDataUseCase().onFailure { return@launch }

            updateApiKeyUseCase(key = updatedKey).onSuccess {
                attemptToInitializeUserId()
            }
        }
    }

    private suspend fun attemptToInitializeUserId() {
        val message: String = initializeUserIdUseCase().fold(
            onSuccess = { "Successfully initialized the user's profile." },
            onFailure = { "Something went wrong while trying to initialize the user's profile." }
        )

        SnackBarManager.showSnackbar(title = message)
    }

    private fun setApiKeyValue(newValue: String) {
        _apiKeyFlow.update { newValue }
    }
}