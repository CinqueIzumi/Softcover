package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.domain.model.BookEdition

internal fun selectedEditionFor(
    editions: List<BookEdition>,
    selectedEditionId: Int?,
    currentEdition: BookEdition,
): BookEdition = editions.firstOrNull { it.id == selectedEditionId } ?: currentEdition
