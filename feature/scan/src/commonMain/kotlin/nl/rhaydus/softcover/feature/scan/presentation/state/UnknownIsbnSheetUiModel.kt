package nl.rhaydus.softcover.feature.scan.presentation.state

import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal data class UnknownIsbnSheetUiModel(
    val isbn: String,
    val scaffold: SheetScaffoldUiModel,
)
