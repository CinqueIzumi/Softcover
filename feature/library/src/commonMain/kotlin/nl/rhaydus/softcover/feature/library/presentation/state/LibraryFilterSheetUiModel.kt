package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal data class LibraryFilterSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val selection: LibraryFilterSheetSelection?,
)
