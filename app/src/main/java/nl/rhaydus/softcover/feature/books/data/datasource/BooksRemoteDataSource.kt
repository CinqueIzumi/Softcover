package nl.rhaydus.softcover.feature.books.data.datasource

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.GetBookByIdQuery
import nl.rhaydus.softcover.GetBookByIdQuery.Data.Book.Companion.bookFragment
import nl.rhaydus.softcover.GetUserBookListsQuery
import nl.rhaydus.softcover.GetUserBookListsQuery.Data.Me.List.Companion.listFragment
import nl.rhaydus.softcover.GetUserBooksQuery
import nl.rhaydus.softcover.GetUserBooksQuery.Data.Me.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkBookAsReadMutation
import nl.rhaydus.softcover.MarkBookAsReadMutation.Data.Insert_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkBookAsReadingMutation
import nl.rhaydus.softcover.MarkBookAsReadingMutation.Data.Update_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation
import nl.rhaydus.softcover.MarkBookAsWantToReadMutation.Data.Insert_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.MarkEditionAsOwnedMutation
import nl.rhaydus.softcover.MarkEditionAsOwnedMutation.Data.Edition_owned.List_book.Companion.listBookFragment
import nl.rhaydus.softcover.RemoveListBookMutation
import nl.rhaydus.softcover.RemoveListBookMutation.Data.Delete_list_book.List.Companion.listFragment
import nl.rhaydus.softcover.RemoveUserBookMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation
import nl.rhaydus.softcover.UpdateBookEditionMutation.Data.Update_user_book.User_book.Companion.userBookFragment
import nl.rhaydus.softcover.UpdateReadingProgressMutation
import nl.rhaydus.softcover.UpdateReadingProgressMutation.Data.Update_user_book_read.User_book_read.Companion.userBookReadFragment
import nl.rhaydus.softcover.core.data.network.helper.safeMutation
import nl.rhaydus.softcover.core.data.network.helper.safeQuery
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.ReadingJournal
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.books.data.mapper.toBook
import nl.rhaydus.softcover.feature.books.data.mapper.toBookList
import nl.rhaydus.softcover.feature.books.data.mapper.toListBook
import nl.rhaydus.softcover.type.DatesReadInput
import nl.rhaydus.softcover.type.UserBookCreateInput
import nl.rhaydus.softcover.type.UserBookUpdateInput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface BooksRemoteDataSource {
    suspend fun fetchBookById(id: Int): Book

    suspend fun markBookAsWantToRead(bookId: Int): Book

    suspend fun markBookAsReading(book: Book): Book

    suspend fun removeBookFromLibrary(book: Book)

    suspend fun initializeBooks(userId: Int): List<Book>

    suspend fun fetchUserLists(userId: Int): List<BookList>

    suspend fun updateBookProgress(
        book: Book,
        newPage: Int,
    ): Book

    suspend fun markBookAsRead(book: Book): Book

    suspend fun updateBookEdition(
        userBook: UserBook,
        newEditionId: Int,
    ): Book

    suspend fun markEditionAsOwned(edition: BookEdition): ListBook

    suspend fun removeListBook(book: ListBook): BookList
}

class BooksRemoteDataSourceImpl(
    private val apolloClient: ApolloClient,
) : BooksRemoteDataSource {
    override suspend fun fetchBookById(id: Int): Book {
        val result = apolloClient.safeQuery(query = GetBookByIdQuery(id = id))

        val book = result
            .books
            .firstOrNull()
            ?.bookFragment()
            ?.toBook() ?: throw Exception("Book could not be mapped")

        return book
    }

    override suspend fun markBookAsWantToRead(bookId: Int): Book {
        val userBookCreateInput = UserBookCreateInput(
            book_id = bookId,
            status_id = Optional.Present(UserBookStatus.WANT_TO_READ.code),
            privacy_setting_id = Optional.Present(PrivacySetting.PUBLIC.code),
        )

        val book = apolloClient
            .safeMutation(mutation = MarkBookAsWantToReadMutation(`object` = userBookCreateInput))
            .insert_user_book
            ?.user_book
            ?.userBookFragment()
            ?.toBook() ?: throw Exception("Book could not be mapped")

        return book
    }

    override suspend fun markBookAsReading(book: Book): Book {
        val userBook = book.userBook
            ?: throw Exception("User did not have a user book")

        val currentDate = LocalDate
            .now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)

        val input = UserBookUpdateInput(
            edition_id = Optional.Present(book.currentEdition.id),
            review_has_spoilers = Optional.Present(userBook.reviewHasSpoilers),
            status_id = Optional.present(UserBookStatus.CURRENTLY_READING.code),
            last_read_date = Optional.Present(userBook.lastReadDate),
            rating = Optional.Present(userBook.rating),
            privacy_setting_id = Optional.Present(PrivacySetting.PUBLIC.code),
            referrer_user_id = Optional.Present(userBook.referrerUserId),
            reviewed_at = Optional.Present(userBook.reviewedAt),
            date_added = Optional.Present(userBook.dateAdded),
            user_date = Optional.present(currentDate)
        )

        val book = apolloClient
            .safeMutation(
                mutation = MarkBookAsReadingMutation(
                    id = userBook.id,
                    `object` = input
                )
            )
            .update_user_book
            ?.user_book
            ?.userBookFragment()
            ?.toBook() ?: throw Exception("Book could not be mapped")

        return book
    }

    override suspend fun removeBookFromLibrary(book: Book) {
        val userBookId = book.userBook?.id
            ?: throw Exception("User did not have a user book")

        apolloClient.safeMutation(mutation = RemoveUserBookMutation(id = userBookId))
    }

    override suspend fun initializeBooks(userId: Int): List<Book> = withContext(Dispatchers.IO) {
        val result = apolloClient.safeQuery(query = GetUserBooksQuery())

        val userBooks = result.me.firstOrNull()?.user_books
            ?: throw Exception("No books were found")

        return@withContext userBooks.mapNotNull { it.userBookFragment()?.toBook() }
    }

    override suspend fun fetchUserLists(userId: Int): List<BookList> = withContext(Dispatchers.IO) {
        val result = apolloClient.safeQuery(GetUserBookListsQuery())

        val lists = result.me.firstOrNull()?.lists
            ?: throw Exception("No lists were found")

        return@withContext lists.mapNotNull { list ->
            list.listFragment()?.toBookList()
        }
    }

    override suspend fun updateBookProgress(
        book: Book,
        newPage: Int,
    ): Book {
        val userBook = book.userBook
            ?: throw Exception("Book did not contain a user book")

        val userBookRead =
            book.userBookRead ?: throw Exception("Book did not contain a user book read")

        val dataObject = DatesReadInput(
            progress_pages = Optional.present(newPage),
            started_at = Optional.present(userBookRead.startedAt),
            finished_at = Optional.present(userBookRead.finishedAt),
            edition_id = Optional.present(userBook.editionId),
        )

        val mutation = UpdateReadingProgressMutation(
            id = userBookRead.id,
            datesReadInput = dataObject
        )

        val userBookReadFragment = apolloClient
            .safeMutation(mutation = mutation)
            .update_user_book_read
            ?.user_book_read
            ?.userBookReadFragment()
            ?: throw Exception("Did not receive a new user book read fragment")

        val updatedUserBookRead = userBookRead.copy(
            currentPage = userBookReadFragment.progress_pages,
            progress = userBookReadFragment.progress?.toFloat(),
        )

        val formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")

        val currentTime = LocalDateTime.now().format(formatter)

        val updatedJournals = book.userBook.journals + ReadingJournal(
            updatedAt = currentTime,
            event = "progress_updated"
        )

        val updatedBook = book.copy(
            userBookRead = updatedUserBookRead,
            userBook = book.userBook.copy(journals = updatedJournals)
        )

        return updatedBook
    }

    override suspend fun markBookAsRead(book: Book): Book {
        val currentDate = LocalDate.now().toString()

        val dataObject = UserBookCreateInput(
            book_id = book.id,
            status_id = Optional.present(UserBookStatus.READ.code),
            user_date = Optional.present(currentDate),
            privacy_setting_id = Optional.present(PrivacySetting.PUBLIC.code),
        )

        val mutation = MarkBookAsReadMutation(userBookCreateInput = dataObject)

        val userBookFragment = apolloClient
            .safeMutation(mutation = mutation)
            .insert_user_book
            ?.user_book
            ?.userBookFragment()
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook() ?: throw Exception("Book was not mapped successfully")
    }

    override suspend fun updateBookEdition(
        userBook: UserBook,
        newEditionId: Int,
    ): Book {
        val mutation = UpdateBookEditionMutation(
            id = userBook.id,
            `object` = UserBookUpdateInput(
                date_added = Optional.Present(userBook.dateAdded),
                edition_id = Optional.present(newEditionId),
                last_read_date = Optional.Present(userBook.lastReadDate),
                privacy_setting_id = Optional.Present(PrivacySetting.PUBLIC.code),
                rating = Optional.Present(userBook.rating),
                referrer_user_id = Optional.Present(userBook.referrerUserId),
                review_has_spoilers = Optional.Present(userBook.reviewHasSpoilers),
                status_id = Optional.present(userBook.status.code),
                reviewed_at = Optional.Present(userBook.reviewedAt)
            )
        )

        val userBookFragment = apolloClient
            .safeMutation(mutation = mutation)
            .update_user_book
            ?.user_book
            ?.userBookFragment()
            ?: throw Exception("Did not receive a new user book fragment")

        return userBookFragment.toBook() ?: throw Exception("Book was not mapped successfully")
    }

    override suspend fun markEditionAsOwned(edition: BookEdition): ListBook {
        val mutation = MarkEditionAsOwnedMutation(id = edition.id)

        val listBookFragment = apolloClient
            .safeMutation(mutation = mutation)
            .edition_owned
            ?.list_book
            ?.listBookFragment()
            ?: throw Exception("Did not receive a list book fragment")

        return listBookFragment.toListBook()
            ?: throw Exception("List book mapping resulted in null")
    }

    override suspend fun removeListBook(book: ListBook): BookList {
        val mutation = RemoveListBookMutation(id = book.listBookId)

        val listFragment = apolloClient
            .safeMutation(mutation = mutation)
            .delete_list_book
            ?.list
            ?.listFragment()
            ?: throw Exception("Did not receive a list fragment")

        return listFragment.toBookList()
    }
}