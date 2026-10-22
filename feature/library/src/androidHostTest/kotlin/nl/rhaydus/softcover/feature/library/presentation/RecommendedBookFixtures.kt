package nl.rhaydus.softcover.feature.library.presentation

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.library.presentation.mapper.recommendedMenuSheetFor
import nl.rhaydus.softcover.feature.library.presentation.state.PendingRecommendedHide
import nl.rhaydus.softcover.feature.library.presentation.state.RecommendedMenuSheetUiModel

internal fun recommendedBook(
    id: Int,
    title: String = "Book $id",
    authors: List<Author> = emptyList(),
) = Book(
    id = id,
    canonicalId = null,
    title = title,
    editions = emptyList(),
    defaultEdition = null,
    rating = 0.0,
    description = "",
    releaseYear = 2020,
    releaseDate = null,
    coverUrl = "cover-$id.jpg",
    authors = authors,
    usersCount = 0,
    ratingsCount = 0,
    bookSeries = null,
    positionsInSeries = emptyList(),
    isCompilation = false,
    userBook = null,
)

internal fun openRecommendedMenuSession(
    book: Book,
    eyebrow: String = "Not on your shelf yet",
    pending: PendingRecommendedHide? = null,
): SheetSession<RecommendedMenuSheetUiModel, PendingRecommendedHide> = SheetSession.Open(
    model = recommendedMenuSheetFor(
        book = book,
        eyebrow = eyebrow,
    ),
    pending = pending,
)
