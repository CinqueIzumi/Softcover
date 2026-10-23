package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.library.presentation.state.PendingRecommendedHide
import nl.rhaydus.softcover.feature.library.presentation.state.RecommendedMenuSheetUiModel
import nl.rhaydus.softcover.feature.library.presentation.state.recommendedMenuSheetScaffoldFor

internal fun recommendedMenuSheetFor(
    book: Book,
    eyebrow: String,
): RecommendedMenuSheetUiModel = RecommendedMenuSheetUiModel(
    scaffold = recommendedMenuSheetScaffoldFor(
        eyebrow = eyebrow,
        title = book.title,
    ),
    hideOption = SheetActionRowUiModel(
        key = PendingRecommendedHide(bookId = book.id),
        label = "Not for me",
        description = "Stop recommending this book",
        icon = SoftcoverIcon.VisibilityOff,
    ),
)
