package nl.rhaydus.softcover.feature.settings.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.presentation.model.SettingsChoiceRowUiModel

/**
 * One row per [DateStyle], off the composition (`component-contract.md` R9); a style with no example
 * yet renders `null` rather than an empty line.
 */
internal fun dateStyleRowsFor(
    examples: Map<DateStyle, String>,
    selected: DateStyle,
): ImmutableList<SettingsChoiceRowUiModel<DateStyle>> = DateStyle.entries
    .map { style ->
        SettingsChoiceRowUiModel(
            key = style,
            label = style.label,
            example = examples[style],
            selected = style == selected,
        )
    }
    .toImmutableList()
