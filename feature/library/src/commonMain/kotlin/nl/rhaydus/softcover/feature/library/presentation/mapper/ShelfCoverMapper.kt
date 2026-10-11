package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.designsystem.presentation.transition.BookTransitionElement
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel

internal fun BookEdition.toShelfEditionCover(): CoverUiModel = toCoverUiModel(
    defaultEdition = this,
    coverlessTitle = title.orEmpty(),
    variant = CoverVariant.Shelf,
    sharedTransitionKey = bookTransitionKey(
        element = BookTransitionElement.Cover,
        editionId = id,
        bookId = bookId,
        surface = "edition-$id",
    ),
)
