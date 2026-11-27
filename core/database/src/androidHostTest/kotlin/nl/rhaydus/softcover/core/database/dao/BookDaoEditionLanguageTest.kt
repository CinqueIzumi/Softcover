package nl.rhaydus.softcover.core.database.dao

import android.content.Context
import androidx.room.Room
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.database.SoftcoverDatabase
import nl.rhaydus.softcover.core.database.model.BookEditionEntity
import nl.rhaydus.softcover.core.database.model.BookEntity
import nl.rhaydus.softcover.core.database.model.EditionImageRef
import nl.rhaydus.softcover.core.domain.model.BookEdition
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDaoEditionLanguageTest {
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

    private fun edition(
        id: Int,
        isLanguageFetched: Boolean,
    ): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList(),
        releaseYear = 2020,
        format = "Paperback",
        owned = false,
        language = if (isLanguageFetched) "Dutch" else null,
        isLanguageFetched = isLanguageFetched,
    )

    @Nested
    inner class GetEditionIdsWithoutFetchedLanguage {
        @Test
        fun `returns only the requested editions whose language was never fetched`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertBook(
                book = BookEntity(
                    id = 1,
                    title = "Book",
                    defaultEditionId = null,
                    rating = 0.0,
                    headline = "",
                    description = "",
                    releaseYear = 2020,
                    releaseDate = null,
                    pages = null,
                    coverUrl = "",
                    usersCount = 0,
                    ratingsCount = 0,
                    positionsInSeries = "",
                    isCompilation = false,
                    seriesId = null,
                ),
            )

            dao.cacheEditions(
                editions = listOf(
                    edition(
                        id = 10,
                        isLanguageFetched = false,
                    ),
                    edition(
                        id = 11,
                        isLanguageFetched = true,
                    ),
                    edition(
                        id = 12,
                        isLanguageFetched = false,
                    ),
                ),
            )

            // ----- Act -----
            val result = dao.getEditionIdsWithoutFetchedLanguage(editionIds = listOf(10, 11, 99))

            // ----- Assert -----
            result shouldContainExactlyInAnyOrder listOf(10)
        }
    }

    @Nested
    inner class FillEditionLanguages {
        private fun editionEntity(id: Int) = BookEditionEntity(
            id = id,
            canonicalId = null,
            bookId = 1,
            publisher = null,
            title = null,
            url = null,
            localImagePath = "covers/$id.jpg",
            localImageUrl = "https://example.com/$id.jpg",
            isbn10 = null,
            isbn13 = null,
            pages = null,
            audioSeconds = null,
            releaseYear = 2020,
            releaseDate = null,
            format = "Paperback",
            readingFormatId = null,
        )

        @Test
        fun `sets the language and marks it fetched while leaving bookId and cover columns untouched`() = runTest(testDispatcher) {
            // ----- Arrange -----
            dao.insertBook(
                book = BookEntity(
                    id = 1,
                    title = "Book",
                    defaultEditionId = null,
                    rating = 0.0,
                    headline = "",
                    description = "",
                    releaseYear = 2020,
                    releaseDate = null,
                    pages = null,
                    coverUrl = "",
                    usersCount = 0,
                    ratingsCount = 0,
                    positionsInSeries = "",
                    isCompilation = false,
                    seriesId = null,
                ),
            )

            dao.insertEditions(editions = listOf(editionEntity(id = 10), editionEntity(id = 11)))

            // ----- Act -----
            dao.fillEditionLanguages(languagesById = mapOf(10 to "Dutch", 11 to null))

            // ----- Assert -----
            dao.getEditionIdsWithoutFetchedLanguage(editionIds = listOf(10, 11)) shouldBe emptyList()

            dao.getEditionImageRefsByBookId(bookId = 1) shouldContainExactlyInAnyOrder listOf(
                EditionImageRef(
                    id = 10,
                    localImagePath = "covers/10.jpg",
                    localImageUrl = "https://example.com/10.jpg",
                ),
                EditionImageRef(
                    id = 11,
                    localImagePath = "covers/11.jpg",
                    localImageUrl = "https://example.com/11.jpg",
                ),
            )
        }
    }
}
