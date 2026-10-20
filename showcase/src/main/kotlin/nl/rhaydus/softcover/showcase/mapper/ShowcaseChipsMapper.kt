package nl.rhaydus.softcover.showcase.mapper

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentMap
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.chip.toChipSet
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette

internal fun showcasePaletteChips(selected: SpinePalette): ChipSet<SpinePalette> = choiceChips(
    options = SpinePalette.entries,
    label = { it.label },
    selected = selected,
)

internal fun showcaseBrightnessChips(selected: SchemeBrightness): ChipSet<SchemeBrightness> = choiceChips(
    options = SchemeBrightness.entries,
    label = { it.name.lowercase().replaceFirstChar(Char::titlecase) },
    selected = selected,
)

internal fun showcaseFamilyChips(selected: GalleryFamily?): ChipSet<GalleryFamily> = choiceChips(
    options = GalleryFamily.entries,
    label = { it.label },
    selected = selected,
)

internal fun showcaseSectionHeaders(): ImmutableMap<GalleryFamily, SectionHeaderUiModel> = GalleryFamily.entries
    .associateWith { family ->
        SectionHeaderUiModel.Section(
            eyebrow = "Family",
            headline = family.label,
        )
    }
    .toPersistentMap()

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
