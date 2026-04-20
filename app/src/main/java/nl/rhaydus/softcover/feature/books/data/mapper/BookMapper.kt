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
import nl.rhaydus.softcover.fragment.BookDetailFragment
import nl.rhaydus.softcover.fragment.BookDetailFragment.Default_physical_edition.Companion.editionFragment
import nl.rhaydus.softcover.fragment.BookListFragment
import nl.rhaydus.softcover.fragment.BookListFragment.Book_series.Companion.bookSeriesFragment
import nl.rhaydus.softcover.fragment.BookSeriesFragment
import nl.rhaydus.softcover.fragment.EditionDetailFragment
import nl.rhaydus.softcover.fragment.EditionFragment
import nl.rhaydus.softcover.fragment.ListBookFragment
import nl.rhaydus.softcover.fragment.ListFragment
import nl.rhaydus.softcover.fragment.ListFragment.List_book.Companion.listBookFragment
import nl.rhaydus.softcover.fragment.ReadingJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Book.Companion.bookListFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Edition.Companion.editionFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.Reading_journal.Companion.readingJournalFragment
import nl.rhaydus.softcover.fragment.UserBookFragment.User_book_read.Companion.userBookReadFragment
import nl.rhaydus.softcover.fragment.UserBookReadFragment
import kotlin.math.roundToInt

// region DTO -> UI mappers
fun EditionFragment.toBookEdition(
    authors: List<Author> = emptyList(),
): BookEdition = BookEdition(
    id = id,
    canonicalId = canonical_id,
    title = title,
    url = image?.url ?: fallbackImages.firstOrNull()?.url,
    localImagePath = null,
    publisher = publisher?.name,
    pages = pages,
    authors = authors,
    isbn10 = isbn_10,
    releaseYear = release_year ?: -1,
    format = edition_format ?: "",
    bookId = book_id,
    owned = false,
)

fun EditionDetailFragment.toBookEdition(): BookEdition {
    val authors = contributions.mapNotNull { contribution ->
        val author = contribution.author ?: return@mapNotNull null

        Author(name = author.name, id = author.id)
    }
    return (this as EditionFragment).toBookEdition(authors = authors)
}

fun ListFragment.toBookList(): BookList {
    val listBooks = list_books.mapNotNull { it.listBookFragment()?.toListBook() }

    return BookList(
        id = id,
        name = name,
        slug = slug ?: "",
        books = listBooks
    )
}

fun ListBookFragment.toListBook(): ListBook? {
    val editionId = edition_id ?: return null

    return ListBook(
        listBookId = id,
        listId = list_id,
        bookId = book_id,
        editionId = editionId,
    )
}

fun ReadingJournalFragment.toReadingJournal(): ReadingJournal = ReadingJournal(
    updatedAt = updated_at,
    event = event,
)

private fun UserBookFragment.toUserBook(): UserBook {
    val journals = reading_journals.mapNotNull {
        it.readingJournalFragment()?.toReadingJournal()
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

private fun UserBookReadFragment.toUserBookRead(): UserBookRead = UserBookRead(
    currentPage = progress_pages,
    progress = progress?.toFloat(),
    id = id,
    startedAt = started_at,
    finishedAt = finished_at,
)

private fun BookListFragment.authors(): List<Author> = contributions.mapNotNull { contribution ->
    val author = contribution.author ?: return@mapNotNull null

    Author(name = author.name, id = author.id)
}

private fun BookListFragment.bookSeries(): BookSeries? =
    book_series.firstOrNull()?.bookSeriesFragment()?.toBookSeries()

private fun BookListFragment.positionInSeries(): Int? =
    book_series.firstOrNull()?.bookSeriesFragment()?.position?.toInt()

private fun BookListFragment.canonicalIdOrNull(): Int? {
    val canonicalId = canonical?.id ?: return null

    return canonicalId.takeIf { it != id }
}

private fun BookListFragment.roundedRating(): Double =
    ((rating ?: 0.0) * 10).roundToInt() / 10.0

fun UserBookFragment.toBook(): Book? {
    val listFragment = book.bookListFragment() ?: return null

    val bookAuthors = listFragment.authors()
    val selectedEdition = edition?.editionFragment()
        ?.toBookEdition(authors = bookAuthors)
        ?.copy(bookId = listFragment.id)

    val editions = listOfNotNull(selectedEdition)

    if (editions.isEmpty()) return null

    val userBookReadFragment = user_book_reads.firstOrNull()?.userBookReadFragment()

    return Book(
        id = listFragment.id,
        canonicalId = listFragment.canonicalIdOrNull(),
        title = listFragment.title ?: "",
        editions = editions,
        defaultEdition = null,
        rating = listFragment.roundedRating(),
        description = "",
        releaseYear = listFragment.release_year ?: -1,
        coverUrl = listFragment.image?.url ?: "",
        authors = bookAuthors,
        usersCount = listFragment.users_count ?: 0,
        bookSeries = listFragment.bookSeries(),
        positionInSeries = listFragment.positionInSeries(),
        userBook = toUserBook(),
        userBookRead = userBookReadFragment?.toUserBookRead(),
    )
}

fun BookDetailFragment.toBook(): Book? {
    val listFragment: BookListFragment = this
    val bookAuthors = listFragment.authors()
    val defaultEdition = default_physical_edition?.editionFragment()
        ?.toBookEdition(authors = bookAuthors)
        ?.copy(bookId = listFragment.id)
    val editions = listOfNotNull(defaultEdition)

    if (editions.isEmpty()) return null

    return Book(
        id = listFragment.id,
        canonicalId = listFragment.canonicalIdOrNull(),
        title = listFragment.title ?: "",
        editions = editions,
        defaultEdition = defaultEdition,
        rating = listFragment.roundedRating(),
        description = description ?: "",
        releaseYear = listFragment.release_year ?: -1,
        coverUrl = listFragment.image?.url ?: "",
        authors = bookAuthors,
        usersCount = users_count,
        bookSeries = listFragment.bookSeries(),
        positionInSeries = listFragment.positionInSeries(),
        userBook = null,
        userBookRead = null,
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

fun BookSeries.toEntity(): BookSeriesEntity = BookSeriesEntity(
    id = id,
    name = name,
    amountOfBooks = amountOfBooks,
)

fun ListBook.toEntity(): ListBookEntity = ListBookEntity(
    listId = listId,
    bookId = bookId,
    editionId = editionId,
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

fun UserBookRead.toEntity(userBookId: Int): UserBookReadEntity = UserBookReadEntity(
    id = id,
    currentPage = currentPage,
    progress = progress,
    startedAt = startedAt,
    finishedAt = finishedAt,
    userBookId = userBookId
)

fun UserBook.toEntity(bookId: Int): UserBookEntity = UserBookEntity(
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

fun ReadingJournal.toEntity(userBookId: Int): ReadingJournalEntity = ReadingJournalEntity(
    event = event ?: "",
    updatedAt = updatedAt,
    userBookId = userBookId
)

fun BookEdition.toEntity(): BookEditionEntity = BookEditionEntity(
    id = id,
    canonicalId = canonicalId,
    bookId = bookId,
    publisher = publisher,
    title = title,
    url = url,
    localImagePath = localImagePath,
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
    canonicalId = canonicalId,
    publisher = publisher,
    title = title,
    url = url,
    localImagePath = localImagePath,
    isbn10 = isbn10,
    pages = pages,
    releaseYear = releaseYear,
    authors = authors.map { it.toModel() },
    format = format,
    bookId = bookId,
    owned = owned,
)

fun UserBookReadEntity.toModel(): UserBookRead = UserBookRead(
    id = id,
    currentPage = currentPage,
    progress = progress,
    startedAt = startedAt,
    finishedAt = finishedAt
)

fun UserBookEntity.toModel(journals: List<ReadingJournal>): UserBook = UserBook(
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

fun ReadingJournalEntity.toModel(): ReadingJournal = ReadingJournal(
    updatedAt = updatedAt,
    event = event,
)

fun ListBookFull.toModel(): ListBook = ListBook(
    listBookId = listBook.listBookId,
    listId = listBook.listId,
    bookId = listBook.bookId,
    editionId = listBook.editionId,
    book = book.toModel(),
    edition = edition.edition.edition.toModel(
        authors = edition.authors,
        owned = edition.edition.isOwned
    ),
)

fun BookListWithBooks.toModel(): BookList = BookList(
    id = bookList.id,
    name = bookList.name,
    slug = bookList.slug,
    books = listBooks.map { it.toModel() }
)

fun BookFullEntity.toModel(): Book {
    val uiEditions = editions.map { editionWithAuthors ->
        val editionAuthors = editionWithAuthors.authors.ifEmpty { bookAuthors }
        editionWithAuthors.edition.edition.toModel(
            authors = editionAuthors,
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

fun BookSeriesEntity.toModel(): BookSeries = BookSeries(
    id = id,
    name = name,
    amountOfBooks = amountOfBooks,
)
// endregion
