package nl.rhaydus.softcover.feature.caching.data.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.caching.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookAuthorCrossRef
import nl.rhaydus.softcover.feature.caching.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookEntity
import nl.rhaydus.softcover.feature.caching.data.model.BookFullEntity
import nl.rhaydus.softcover.feature.caching.data.model.EditionAuthorCrossRef
import nl.rhaydus.softcover.feature.caching.data.model.UserBookEntity
import nl.rhaydus.softcover.feature.caching.data.model.UserBookReadEntity

// region UI -> Entity mappers
fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    rating = rating,
    description = description,
    releaseYear = releaseYear,
    coverUrl = coverUrl,
    defaultEditionId = defaultEdition?.id,
    usersCount = usersCount,
    userBook = userBook?.toEntity(),
    userBookReadEntity = userBookRead?.toEntity(),
)

fun UserBookRead.toEntity(): UserBookReadEntity {
    return UserBookReadEntity(
        id = id,
        currentPage = currentPage,
        progress = progress,
        startedAt = startedAt,
        finishedAt = finishedAt
    )
}

fun UserBook.toEntity(): UserBookEntity {
    return UserBookEntity(
        id = id,
        statusCode = status.code,
        dateAdded = dateAdded,
        privacySettingId = privacySettingId,
        reviewHasSpoilers = reviewHasSpoilers,
        editionId = editionId,
        lastReadDate = lastReadDate,
        rating = rating,
        referrerUserId = referrerUserId,
        reviewedAt = reviewedAt,
        updatedAt = updatedAt
    )
}

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

// endregion

// region Entity -> UI mappers
fun AuthorEntity.toModel(): Author = Author(name = name, id = id)

fun BookEditionEntity.toModel(authors: List<AuthorEntity>): BookEdition = BookEdition(
    id = id,
    publisher = publisher,
    title = title,
    url = url,
    isbn10 = isbn10,
    pages = pages,
    releaseYear = releaseYear,
    authors = authors.map { it.toModel() }
)

fun UserBookReadEntity.toModel(): UserBookRead {
    return UserBookRead(
        id = id,
        currentPage = currentPage,
        progress = progress,
        startedAt = startedAt,
        finishedAt = finishedAt
    )
}

fun UserBookEntity.toModel(): UserBook {
    return UserBook(
        id = id,
        status = BookStatus.getFromCode(statusCode),
        dateAdded = dateAdded,
        privacySettingId = privacySettingId,
        reviewHasSpoilers = reviewHasSpoilers,
        editionId = editionId,
        lastReadDate = lastReadDate,
        rating = rating,
        referrerUserId = referrerUserId,
        reviewedAt = reviewedAt,
        updatedAt = updatedAt
    )
}

fun BookFullEntity.toModel(): Book {
    val uiEditions = editions.map { editionWithAuthors ->
        editionWithAuthors.edition.toModel(
            authors = editionWithAuthors.authors
        )
    }

    val defaultEdition = book.defaultEditionId?.let { id ->
        uiEditions.firstOrNull { it.id == id }
    }

    return Book(
        id = book.id,
        title = book.title,
        editions = uiEditions,
        defaultEdition = defaultEdition,
        rating = book.rating,
        description = book.description,
        releaseYear = book.releaseYear,
        coverUrl = book.coverUrl,
        authors = bookAuthors.map { it.toModel() },
        usersCount = book.usersCount,
        userBook = book.userBook?.toModel(),
        userBookRead = book.userBookReadEntity?.toModel()
    )
}
// endregion