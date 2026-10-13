package nl.rhaydus.softcover.feature.settings.presentation.mapper

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.uibinding.theme.toSpinePalette

/**
 * The gallery's override rows as chip sets, `selected` marking the override in effect (`null` when the
 * gallery follows the app's own setting).
 */
internal fun galleryThemeModeChips(selected: ThemeMode?): ChipSet<ThemeMode> = choiceChips(
    options = ThemeMode.entries,
    label = { it.label },
    selected = selected,
)

internal fun galleryFamilyChips(selected: GalleryFamily?): ChipSet<GalleryFamily> = choiceChips(
    options = GalleryFamily.entries,
    label = { it.label },
    selected = selected,
)

internal fun galleryPaletteChips(selected: ColorPalette?): ChipSet<ColorPalette> = choiceChips(
    options = ColorPalette.entries,
    label = { it.toSpinePalette().label },
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
