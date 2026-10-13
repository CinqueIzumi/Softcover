package nl.rhaydus.softcover.feature.book_detail.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal data class EditionSheetUiModel(
    val scaffold: SheetScaffoldUiModel,
    val cards: ImmutableList<BookCardUiModel>,
    val countHeader: SectionHeaderUiModel,
    val searchQuery: String,
    val searchPlaceholder: String,
    val emptyMessage: String,
)
