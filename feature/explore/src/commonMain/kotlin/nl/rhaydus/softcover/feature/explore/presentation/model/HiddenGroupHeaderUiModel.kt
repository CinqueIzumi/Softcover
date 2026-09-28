package nl.rhaydus.softcover.feature.explore.presentation.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

@Immutable
internal data class HiddenGroupHeaderUiModel(
    val header: SectionHeaderUiModel,
    val count: String,
    val subline: String,
)
