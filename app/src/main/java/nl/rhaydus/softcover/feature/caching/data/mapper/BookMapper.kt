package nl.rhaydus.softcover.feature.caching.data.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.caching.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookAuthorCrossRef
import nl.rhaydus.softcover.feature.caching.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookFullEntity
import nl.rhaydus.softcover.feature.caching.data.model.EditionAuthorCrossRef

fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    statusCode = userStatus.code,
    title = title,
    rating = rating,
    description = description,
    releaseYear = releaseYear,
    coverUrl = coverUrl,
    defaultEditionId = defaultEdition?.id,
    currentEditionId = userEditionId,
    currentPage = currentPage,
    progress = progress,
    userBookId = userBookId,
    userBookReadId = userBookReadId,
    startedAt = startedAt,
    finishedAt = finishedAt,
    userLastReadDate = userLastReadDate,
    userDateAdded = userDateAdded,
    userPrivacySettingId = userPrivacySettingId,
    userRating = userRating,
    userReferrerUserId = userReferrerUserId,
    userReviewHasSpoilers = userReviewHasSpoilers,
    userReviewedAt = userReviewedAt,
    userUpdatedAt = userUpdatedAt
)

fun BookEdition.toEntity(bookId: Int): BookEditionEntity = BookEditionEntity(
    id = id,
    bookId = bookId,
    publisher = publisher,
    title = title,
    url = url,
    isbn10 = isbn10,
    pages = pages,
    releaseYear = releaseYear
)

fun Author.toEntity(): AuthorEntity = AuthorEntity(name = name, id = id)

fun Book.toBookAuthorRefs(authorIdsByName: Map<String, Int>): List<BookAuthorCrossRef> =
    authors.map { BookAuthorCrossRef(bookId = id, authorId = authorIdsByName.getValue(it.name)) }

fun Book.toEditionAuthorRefs(authorIdsByName: Map<String, Int>): List<EditionAuthorCrossRef> =
    editions.flatMap { edition ->
        edition.authors.map {
            EditionAuthorCrossRef(
                editionId = edition.id,
                authorId = authorIdsByName.getValue(it.name)
            )
        }
    }


// ----------
fun AuthorEntity.toUi(): Author = Author(name = name, id = id)

fun BookEditionEntity.toUi(authors: List<AuthorEntity>): BookEdition =
    BookEdition(
        id = id,
        publisher = publisher,
        title = title,
        url = url,
        isbn10 = isbn10,
        pages = pages,
        releaseYear = releaseYear,
        authors = authors.map { it.toUi() }
    )

fun BookFullEntity.toUi(): Book {
    val uiEditions = editions.map { editionWithAuthors ->
        editionWithAuthors.edition.toUi(
            authors = editionWithAuthors.authors
        )
    }

    val defaultEdition = book.defaultEditionId?.let { id ->
        uiEditions.firstOrNull { it.id == id }
    }

    return Book(
        id = book.id,
        userStatus = BookStatus.getFromCode(book.statusCode),
        title = book.title,
        editions = uiEditions,
        defaultEdition = defaultEdition,
        rating = book.rating,
        description = book.description,
        releaseYear = book.releaseYear,
        coverUrl = book.coverUrl,
        authors = bookAuthors.map { it.toUi() },
        currentPage = book.currentPage,
        progress = book.progress,
        userEditionId = book.currentEditionId,
        userBookId = book.userBookId,
        userBookReadId = book.userBookReadId,
        startedAt = book.startedAt,
        finishedAt = book.finishedAt,
        userLastReadDate = book.userLastReadDate,
        userDateAdded = book.userDateAdded,
        userPrivacySettingId = book.userPrivacySettingId,
        userRating = book.userRating,
        userReferrerUserId = book.userReferrerUserId,
        userReviewHasSpoilers = book.userReviewHasSpoilers,
        userReviewedAt = book.userReviewedAt,
        userUpdatedAt = book.userUpdatedAt
    )
}

