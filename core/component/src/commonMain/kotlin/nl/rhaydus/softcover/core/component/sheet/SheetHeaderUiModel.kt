package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

@Immutable
data class SheetHeaderUiModel(
    val section: SectionHeaderUiModel.Section,
    val trailing: SheetHeaderTrailing? = null,
)
