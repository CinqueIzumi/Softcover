package nl.rhaydus.softcover.showcase.state

import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.showcase.mapper.showcaseBrightnessChips
import nl.rhaydus.softcover.showcase.mapper.showcaseFamilyChips
import nl.rhaydus.softcover.showcase.mapper.showcasePaletteChips
import nl.rhaydus.softcover.showcase.mapper.showcaseSectionHeaders
import nl.rhaydus.toad.UiState

internal data class ShowcaseUiState(
    val selectedPalette: SpinePalette = SpinePalette.DEFAULT,
    val selectedBrightness: SchemeBrightness = SchemeBrightness.LIGHT,
    val selectedFamily: GalleryFamily? = null,
    val paletteChips: ChipSet<SpinePalette> = showcasePaletteChips(selected = SpinePalette.DEFAULT),
    val brightnessChips: ChipSet<SchemeBrightness> = showcaseBrightnessChips(selected = SchemeBrightness.LIGHT),
    val familyChips: ChipSet<GalleryFamily> = showcaseFamilyChips(selected = null),
    val sectionHeaders: ImmutableMap<GalleryFamily, SectionHeaderUiModel> = showcaseSectionHeaders(),
) : UiState
