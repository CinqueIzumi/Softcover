package nl.rhaydus.softcover.feature.explore.presentation.mapper

import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.model.RecommendedMenuSheetUiModel
import nl.rhaydus.softcover.feature.explore.presentation.state.recommendedMenuSheetScaffoldFor

internal fun recommendedMenuSheetFor(
    book: Book,
    eyebrow: String,
): RecommendedMenuSheetUiModel {
    return RecommendedMenuSheetUiModel(
        scaffold = recommendedMenuSheetScaffoldFor(
            book = book,
            eyebrow = eyebrow,
        ),
        hideOption = SheetActionRowUiModel(
            icon = SoftcoverIcon.VisibilityOff,
            label = "Not for me",
            description = "Stop recommending this book",
            key = book.id,
        ),
    )
}
