package nl.rhaydus.softcover.feature.settings.presentation.event

sealed class SettingsScreenUiEvent {
    data object OnSaveApiKeyClick : SettingsScreenUiEvent()

    data class OnApiKeyValueChanged(val newValue: String) : SettingsScreenUiEvent()
}