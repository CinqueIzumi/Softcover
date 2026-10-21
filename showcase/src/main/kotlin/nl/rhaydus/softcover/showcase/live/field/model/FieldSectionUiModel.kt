package nl.rhaydus.softcover.showcase.live.field.model

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.field.TextFieldUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel

@Immutable
internal data class FieldSectionUiModel(
    val header: SectionHeaderUiModel,
    val field: TextFieldUiModel,
)
