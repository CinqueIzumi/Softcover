package nl.rhaydus.softcover.orchestration.presentation

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

@Immutable
internal data class ReAuthSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val apiKey: String,
    val errorMessage: String?,
    val fieldEnabled: Boolean,
)
