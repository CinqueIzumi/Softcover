package nl.rhaydus.softcover.feature.library.presentation.mapper

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel

/** The transition surface of the shelf cover for edition [editionId], shared with the book it opens. */
internal fun editionTransitionSurface(editionId: Int): String = "edition-$editionId"

internal fun BookEdition.toShelfEditionCover(): CoverUiModel = toCoverUiModel(
    defaultEdition = this,
    coverlessTitle = title.orEmpty(),
    variant = CoverVariant.Shelf,
    sharedTransitionKey = bookTransitionKey(
        element = BookTransitionElement.Cover,
        bookId = bookId,
        surface = editionTransitionSurface(editionId = id),
    ),
)
