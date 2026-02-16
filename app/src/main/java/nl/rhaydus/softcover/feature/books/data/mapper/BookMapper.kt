package nl.rhaydus.softcover.feature.books.data.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.books.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.books.data.model.BookAuthorCrossRef
import nl.rhaydus.softcover.feature.books.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.books.data.model.BookEntity
import nl.rhaydus.softcover.feature.books.data.model.BookFullEntity
import nl.rhaydus.softcover.feature.books.data.model.EditionAuthorCrossRef
import nl.rhaydus.softcover.feature.books.data.model.ReadingJournalEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookReadEntity
import nl.rhaydus.softcover.fragment.BookFragment
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.ReadingJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookReadFragment
import kotlin.math.roundToInt

// region DTO -> UI mappers
private fun EditionFragment.toBookEdition(): BookEdition {
    return BookEdition(
        id = id,
        title = title,
        url = image?.url,
        publisher = publisher?.name,
        pages = pages,
        authors = contributions.mapNotNull { contribution ->
            val author = contribution.author ?: return@mapNotNull null
            val id = author.id

            Author(
                name = author.name,
                id = id,
            )
        },
        isbn10 = isbn_10,
        releaseYear = release_year ?: -1,
        format = edition_format ?: "",
    )
}

fun UserBookFragment.toBook(): Book {
    return book.bookFragment.toBook(userBookFragment = this)
}

fun ReadingJournalFragment.toReadingJournal(): ReadingJournal {
    return ReadingJournal(
        updatedAt = updated_at,
        event = event,
    )
}

private fun UserBookFragment?.toUserBook(): UserBook? {
    if (this == null) return null

    val journals = reading_journals.map {
        it.readingJournalFragment.toReadingJournal()
    }

    return UserBook(
        id = id,
        status = BookStatus.getFromCode(code = status_id),
        editionId = edition_id,
        lastReadDate = last_read_date,
        dateAdded = date_added,
        privacySettingId = privacy_setting_id,
        rating = rating,
        referrerUserId = referrer_user_id,
        reviewHasSpoilers = review_has_spoilers,
        reviewedAt = reviewed_at,
        updatedAt = updated_at,
        journals = journals
    )
}

private fun UserBookReadFragment?.toUserBookRead(): UserBookRead? {
    if (this == null) return null

    return UserBookRead(
        currentPage = progress_pages,
        progress = progress?.toFloat(),
        id = id,
        startedAt = started_at,
        finishedAt = finished_at,
    )
}

fun BookFragment.toBook(
    userBookFragment: UserBookFragment? = null,
): Book {
    val bookContent = canonical?.bookContentFragment ?: this.bookContentFragment

    val rating = ((bookContent.rating ?: 0.0) * 10).roundToInt() / 10.0
    val userBookReadFragment = userBookFragment
        ?.user_book_reads
        ?.firstOrNull()
        ?.userBookReadFragment

    return Book(
        id = bookContent.id,
        title = bookContent.title ?: "",
        editions = bookContent.editions.map { userBookEdition ->
            userBookEdition.editionFragment.toBookEdition()
        },
        description = bookContent.description ?: "",
        rating = rating,
        releaseYear = bookContent.release_year ?: -1,
        coverUrl = bookContent.image?.url ?: "",
        authors = bookContent.contributions.mapNotNull { contribution ->
            val author = contribution.author ?: return@mapNotNull null
            val id = author.id

            Author(
                name = author.name,
                id = id,
            )
        },
        defaultEdition = bookContent.default_physical_edition?.editionFragment?.toBookEdition(),
        userBook = userBookFragment.toUserBook(),
        userBookRead = userBookReadFragment.toUserBookRead(),
        usersCount = bookContent.users_count,
    )
}
// endregion

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
        updatedAt = updatedAt,
    )
}

fun ReadingJournal.toEntity(userBookId: Int): ReadingJournalEntity {
    return ReadingJournalEntity(
        event = event ?: "",
        updatedAt = updatedAt,
        userBookId = userBookId
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
    releaseYear = releaseYear,
    format = format,
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
    authors = authors.map { it.toModel() },
    format = format,
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

fun UserBookEntity.toModel(journals: List<ReadingJournal>): UserBook {
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
        updatedAt = updatedAt,
        journals = journals,
    )
}

fun ReadingJournalEntity.toModel(): ReadingJournal {
    return ReadingJournal(
        updatedAt = updatedAt,
        event = event,
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

    val journals = journals.map { it.toModel() }

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
        userBook = book.userBook?.toModel(journals = journals),
        userBookRead = book.userBookReadEntity?.toModel()
    )
}
// endregion