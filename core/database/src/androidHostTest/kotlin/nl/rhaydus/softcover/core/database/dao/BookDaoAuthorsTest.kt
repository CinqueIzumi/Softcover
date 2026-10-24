package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.mapper.toModel
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.Gender
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDaoAuthorsTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: SoftcoverDatabase
    private lateinit var dao: BookDao

    @BeforeEach
    fun setUp() {
        val context: Context = mockk(relaxed = true)

        database = SoftcoverDatabase.build(
            builder = Room.inMemoryDatabaseBuilder(context = context),
            queryContext = testDispatcher,
        )
        dao = database.bookDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    private fun author(
        id: Int,
        name: String,
        isBipoc: Boolean? = null,
        isLgbtq: Boolean? = null,
        gender: Gender = Gender.Unknown,
    ) = Author(
        id = id,
        name = name,
        isBipoc = isBipoc,
        isLgbtq = isLgbtq,
        gender = gender,
    )

    private fun edition(
        id: Int,
        bookId: Int,
        authors: List<Author>,
    ) = BookEdition(
        id = id,
        canonicalId = null,
        bookId = bookId,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = authors,
        releaseYear = 2020,
        format = "Paperback",
        owned = false,
    )

    private fun book(
        id: Int,
        authors: List<Author> = emptyList(),
        editions: List<BookEdition> = emptyList(),
    ) = Book(
        id = id,
        title = "Book $id",
        editions = editions,
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = authors,
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
    )

    private suspend fun cachedAuthors(bookId: Int): List<Author> =
        dao.observeBookById(id = bookId).first()
            .shouldNotBeNull()
            .bookAuthors
            .map { it.toModel() }

    private suspend fun editionAuthors(editionId: Int): List<Author> =
        dao.getAuthorsForEdition(editionId = editionId).map { it.toModel() }

    @Nested
    inner class CacheBook {
        @Test
        fun `two authors sharing a name stay two rows with their own demographics`() = runTest(testDispatcher) {
            // ----- Arrange -----
            val first = author(
                id = 1,
                name = "Alex Smith",
                isBipoc = true,
                gender = Gender.Female,
            )
            val second = author(
                id = 2,
                name = "Alex Smith",
                isLgbtq = true,
                gender = Gender.Male,
            )

            // ----- Act -----
            dao.cacheBook(
                book = book(
                    id = 100,
                    authors = listOf(first, second),
                ),
            )

            // ----- Assert -----
            cachedAuthors(bookId = 100) shouldContainExactlyInAnyOrder listOf(first, second)
        }

        @Test
        fun `an author on the book and on its edition is stored once with the book-level values`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                val bookLevel = author(
                    id = 1,
                    name = "Octavia Butler",
                    isBipoc = true,
                    gender = Gender.Female,
                )
                val editionLevel = author(
                    id = 1,
                    name = "Octavia Butler",
                )

                // ----- Act -----
                dao.cacheBook(
                    book = book(
                        id = 100,
                        authors = listOf(bookLevel),
                        editions = listOf(
                            edition(
                                id = 10,
                                bookId = 100,
                                authors = listOf(editionLevel),
                            ),
                        ),
                    ),
                )

                // ----- Assert -----
                cachedAuthors(bookId = 100) shouldBe listOf(bookLevel)
                editionAuthors(editionId = 10) shouldBe listOf(bookLevel)
            }
    }

    @Nested
    inner class CacheEditions {
        @Test
        fun `keeps the demographics of edition authors and separates same-named authors`() =
            runTest(testDispatcher) {
                // ----- Arrange -----
                dao.cacheBook(book = book(id = 100))
                val first = author(
                    id = 1,
                    name = "Alex Smith",
                    isLgbtq = true,
                    gender = Gender.Other,
                )
                val second = author(
                    id = 2,
                    name = "Alex Smith",
                )

                // ----- Act -----
                dao.cacheEditions(
                    editions = listOf(
                        edition(
                            id = 10,
                            bookId = 100,
                            authors = listOf(first),
                        ),
                        edition(
                            id = 11,
                            bookId = 100,
                            authors = listOf(second),
                        ),
                    ),
                )

                // ----- Assert -----
                editionAuthors(editionId = 10) shouldBe listOf(first)
                editionAuthors(editionId = 11) shouldBe listOf(second)
            }
    }
}
