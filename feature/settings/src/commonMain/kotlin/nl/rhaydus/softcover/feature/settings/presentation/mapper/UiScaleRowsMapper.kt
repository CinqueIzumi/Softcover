package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsChoiceRowUiModel

/** One row per [UiScale], off the composition (`component-contract.md` R9); no scale has an example. */
internal fun uiScaleRowsFor(selected: UiScale): ImmutableList<SettingsChoiceRowUiModel<UiScale>> = UiScale.entries
    .map { scale ->
        SettingsChoiceRowUiModel(
            key = scale,
            label = scale.label,
            example = null,
            selected = scale == selected,
        )
    }
    .toImmutableList()
