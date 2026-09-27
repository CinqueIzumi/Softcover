package nl.rhaydus.softcover.core.component.chip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/** What [Chip] paints for a [ChipTone] × [ChipUiModel.selected] — the R2 lookup every chip resolves once. */
internal data class ChipStyle(
    val container: Color,
    val ink: Color,
    val weight: FontWeight,
    val border: ChipBorder = ChipBorder.None,
    val mutedInk: Color = ink,
)
