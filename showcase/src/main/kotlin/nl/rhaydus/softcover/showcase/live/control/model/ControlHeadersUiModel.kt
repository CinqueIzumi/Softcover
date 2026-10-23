package nl.rhaydus.softcover.showcase.live.control.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

@Immutable
internal data class ControlHeadersUiModel(
    val toggles: SectionHeaderUiModel,
    val rows: SectionHeaderUiModel,
    val segments: SectionHeaderUiModel,
    val themes: SectionHeaderUiModel,
    val palettes: SectionHeaderUiModel,
)
