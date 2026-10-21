package nl.rhaydus.softcover.showcase.live.chip.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

@Immutable
internal data class ChipSectionUiModel(
    val key: String,
    val header: SectionHeaderUiModel,
    val mode: ChipSelectionMode,
    val set: ChipSet<String>,
)
