package nl.rhaydus.softcover.feature.settings.presentation.model

/** A toggle row's rendered state, built off the composition so a section reads it as-is. */
internal data class SettingsToggleRowUiModel<K>(
    val key: K,
    val label: String,
    val gloss: String,
    val checked: Boolean,
)
