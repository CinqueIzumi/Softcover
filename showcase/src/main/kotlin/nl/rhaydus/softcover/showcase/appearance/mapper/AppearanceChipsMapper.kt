package nl.rhaydus.softcover.showcase.appearance.mapper

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette

internal fun paletteChips(selected: SpinePalette): ChipSet<SpinePalette> = choiceChips(
    options = SpinePalette.entries,
    label = { it.label },
    selected = selected,
)

internal fun brightnessChips(selected: SchemeBrightness): ChipSet<SchemeBrightness> = choiceChips(
    options = SchemeBrightness.entries,
    label = { it.name.lowercase().replaceFirstChar(Char::titlecase) },
    selected = selected,
)

private fun <T : Any> choiceChips(
    options: List<T>,
    label: (T) -> String,
    selected: T?,
): ChipSet<T> = options
    .mapIndexed { index, option ->
        ChipUiModel(
            key = index.toString(),
            label = label(option),
            selected = option == selected,
        ) to option
    }
    .toChipSet()
