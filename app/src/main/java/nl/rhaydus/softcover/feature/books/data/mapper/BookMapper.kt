package nl.rhaydus.softcover.feature.books.data.mapper

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.books.data.model.AuthorEntity
import nl.rhaydus.softcover.feature.books.data.model.BookAuthorCrossRef
import nl.rhaydus.softcover.feature.books.data.model.BookEditionEntity
import nl.rhaydus.softcover.feature.books.data.model.BookEntity
import nl.rhaydus.softcover.feature.books.data.model.BookFullEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListEntity
import nl.rhaydus.softcover.feature.books.data.model.BookListWithBooks
import nl.rhaydus.softcover.feature.books.data.model.BookSeriesEntity
import nl.rhaydus.softcover.feature.books.data.model.EditionAuthorCrossRef
import nl.rhaydus.softcover.feature.books.data.model.ListBookEntity
import nl.rhaydus.softcover.feature.books.data.model.ListBookFull
import nl.rhaydus.softcover.feature.books.data.model.ReadingJournalEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookEntity
import nl.rhaydus.softcover.feature.books.data.model.UserBookReadEntity
import nl.rhaydus.softcover.fragment.BookFragment
import nl.rhaydus.softcover.fragment.BookSeriesFragment
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.ListBookFragment
import nl.rhaydus.softcover.fragment.ListFragment
import nl.rhaydus.softcover.fragment.ReadingJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookReadFragment
import kotlin.math.roundToInt

// region DTO -> UI mappers
fun EditionFragment.toBookEdition(): BookEdition {
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
        bookId = book_id,
        owned = false,
    )
}

fun ListFragment.toBookList(): BookList {
    val listBooks = list_books.mapNotNull { it.listBookFragment.toListBook() }

    return BookList(
        id = id,
        name = name,
        slug = slug ?: "",
        books = listBooks
    )
}

fun ListBookFragment.toListBook(): ListBook? {
    val edition = edition?.editionFragment?.toBookEdition() ?: return null

    return ListBook(
        book = book.bookFragment.toBook(),
        edition = edition,
        listId = list_id,
        listBookId = id
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
        bookSeries = bookContent.book_series.firstOrNull()?.bookSeriesFragment?.toBookSeries(),
        positionInSeries = bookContent.book_series.firstOrNull()?.bookSeriesFragment?.position?.toInt(),
    )
}

private fun BookSeriesFragment.toBookSeries(): BookSeries? {
    val series = series ?: return null

    return BookSeries(
        id = series.id,
        name = series.name,
        amountOfBooks = series.primary_books_count ?: 0
    )
}
// endregion

// region UI -> Entity mappers
fun BookList.toEntity(): BookListEntity = BookListEntity(
    id = id,
    name = name,
    slug = slug,
)

fun BookSeries.toEntity(): BookSeriesEntity {
    return BookSeriesEntity(
        id = id,
        name = name,
        amountOfBooks = amountOfBooks,
    )
}

fun ListBook.toEntity(): ListBookEntity = ListBookEntity(
    listId = listId,
    bookId = book.id,
    editionId = edition.id,
    listBookId = listBookId,
)

fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    rating = rating,
    description = description,
    releaseYear = releaseYear,
    coverUrl = coverUrl,
    defaultEditionId = defaultEdition?.id,
    usersCount = usersCount,
    positionInSeries = positionInSeries,
    seriesId = bookSeries?.id,
)

fun UserBookRead.toEntity(userBookId: Int): UserBookReadEntity {
    return UserBookReadEntity(
        id = id,
        currentPage = currentPage,
        progress = progress,
        startedAt = startedAt,
        finishedAt = finishedAt,
        userBookId = userBookId
    )
}

fun UserBook.toEntity(bookId: Int): UserBookEntity {
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
        bookId = bookId,
    )
}

fun ReadingJournal.toEntity(userBookId: Int): ReadingJournalEntity {
    return ReadingJournalEntity(
        event = event ?: "",
        updatedAt = updatedAt,
        userBookId = userBookId
    )
}

fun BookEdition.toEntity(): BookEditionEntity = BookEditionEntity(
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
    editions.flatMap { it.toEditionAuthorRefs(authorIdsByName) }

fun BookEdition.toEditionAuthorRefs(authorIdsByName: Map<String, Int>): List<EditionAuthorCrossRef> =
    authors.map {
        EditionAuthorCrossRef(
            editionId = id,
            authorId = authorIdsByName.getValue(it.name)
        )
    }

// endregion

// region Entity -> UI mappers
fun AuthorEntity.toModel(): Author = Author(name = name, id = id)

fun BookEditionEntity.toModel(
    authors: List<AuthorEntity>,
    owned: Boolean,
): BookEdition = BookEdition(
    id = id,
    publisher = publisher,
    title = title,
    url = url,
    isbn10 = isbn10,
    pages = pages,
    releaseYear = releaseYear,
    authors = authors.map { it.toModel() },
    format = format,
    bookId = bookId,
    owned = owned,
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

fun ListBookFull.toModel(): ListBook = ListBook(
    book = book.toModel(),
    edition = edition.edition.edition.toModel(
        authors = edition.authors,
        owned = edition.edition.isOwned
    ),
    listId = listBook.listId,
    listBookId = listBook.listBookId
)

fun BookListWithBooks.toModel(): BookList = BookList(
    id = bookList.id,
    name = bookList.name,
    slug = bookList.slug,
    books = listBooks.map { it.toModel() }
)

fun BookFullEntity.toModel(): Book {
    val uiEditions = editions.map { editionWithAuthors ->
        editionWithAuthors.edition.edition.toModel(
            authors = editionWithAuthors.authors,
            owned = editionWithAuthors.edition.isOwned,
        )
    }

    val defaultEdition = book.defaultEditionId?.let { id ->
        uiEditions.firstOrNull { it.id == id }
    }

    val journals = userBookWithJournals?.journals?.map { it.toModel() } ?: emptyList()

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
        userBook = userBookWithJournals?.userBook?.toModel(journals = journals),
        userBookRead = userBookWithJournals?.userBookRead?.toModel(),
        positionInSeries = book.positionInSeries,
        bookSeries = series?.toModel()
    )
}

fun BookSeriesEntity.toModel(): BookSeries {
    return BookSeries(
        id = id,
        name = name,
        amountOfBooks = amountOfBooks,
    )
}
// endregion
