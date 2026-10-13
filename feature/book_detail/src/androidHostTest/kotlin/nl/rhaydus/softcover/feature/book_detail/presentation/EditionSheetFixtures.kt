package nl.rhaydus.softcover.feature.book_detail.presentation

import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EditionSheetUiModel

internal fun editionSheetWithQuery(query: String): EditionSheetUiModel = EditionSheetUiModel(
    scaffold = mockk<SheetScaffoldUiModel>(),
    cards = persistentListOf(),
    countHeader = SectionHeaderUiModel.Label(eyebrow = "0 EDITIONS"),
    searchQuery = query,
    searchPlaceholder = "Search",
    emptyMessage = "Nothing",
)
