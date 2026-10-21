package nl.rhaydus.softcover.showcase.appearance.state

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SchemeBrightness
import nl.rhaydus.softcover.core.designsystem.presentation.theme.SpinePalette
import nl.rhaydus.softcover.showcase.appearance.mapper.brightnessChips
import nl.rhaydus.softcover.showcase.appearance.mapper.paletteChips

/** The Appearance sheet's slice of a screen's state; every screen that offers the sheet embeds one. */
@Immutable
internal data class AppearanceState(
    val sheet: SheetSession<SheetScaffoldUiModel, Nothing> = SheetSession.Closed,
    val paletteChips: ChipSet<SpinePalette> = paletteChips(selected = SpinePalette.DEFAULT),
    val brightnessChips: ChipSet<SchemeBrightness> = brightnessChips(selected = SchemeBrightness.LIGHT),
)
