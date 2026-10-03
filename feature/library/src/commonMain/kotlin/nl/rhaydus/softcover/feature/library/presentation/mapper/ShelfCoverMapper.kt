package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel

internal fun Book.toShelfBookCover(): CoverUiModel = toCoverUiModel(
    variant = CoverVariant.LibraryShelfItem,
    sharedTransitionKey = bookCoverTransitionKey(
        editionId = currentEdition?.id,
        bookId = id,
    ),
)

internal fun BookEdition.toShelfEditionCover(): CoverUiModel = toCoverUiModel(
    defaultEdition = this,
    coverlessTitle = title.orEmpty(),
    variant = CoverVariant.LibraryShelfItem,
    sharedTransitionKey = bookCoverTransitionKey(
        editionId = id,
        bookId = bookId,
        surface = "edition-$id",
    ),
)
