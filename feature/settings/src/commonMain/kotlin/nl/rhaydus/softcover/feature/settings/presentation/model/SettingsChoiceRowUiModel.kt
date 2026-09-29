package nl.rhaydus.softcover.feature.settings.presentation.model

/** One entry in a single-choice row list (date notation, UI scale); [example] is `null` when the row has none. */
internal data class SettingsChoiceRowUiModel<K>(
    val key: K,
    val label: String,
    val example: String?,
    val selected: Boolean,
)
