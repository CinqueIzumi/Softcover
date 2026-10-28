package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.Tag
import nl.rhaydus.softcover.core.domain.model.TagCategory
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryFilterOptionsBuilderTest {
    private val tagFiction = Tag(
        id = 1,
        name = "Fiction",
        category = TagCategory.GENRE,
    )
    private val tagScifi = Tag(
        id = 2,
        name = "Sci-Fi",
        category = TagCategory.GENRE,
    )

    private val userCozy = UserTag(
        name = "cozy",
        category = TagCategory.MOOD,
    )
    private val userSlow = UserTag(
        name = "slow",
        category = TagCategory.MOOD,
    )

    @Nested
    inner class BuildBookFilterOptions {
        @Test
        fun `lists each community tag once with the number of books carrying it`() {
            // ----- Arrange -----
            val book1 = buildBook(
                id = 1,
                tags = listOf(tagScifi, tagFiction),
            )
            val book2 = buildBook(
                id = 2,
                tags = listOf(tagFiction),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book1, book2))

            // ----- Assert -----
            result.hardcoverTags.map { it.key.id to it.count } shouldBe listOf(
                tagFiction.id to 2,
                tagScifi.id to 1,
            )
        }

        @Test
        fun `returns distinct non-blank formats sorted alphabetically`() {
            // ----- Arrange -----
            val book1 = buildBook(
                id = 1,
                editions = listOf(
                    buildEdition(
                        id = 1,
                        format = "paperback",
                    ),
                    buildEdition(
                        id = 2,
                        format = "ebook",
                    ),
                ),
            )
            val book2 = buildBook(
                id = 2,
                editions = listOf(
                    buildEdition(
                        id = 3,
                        format = "paperback",
                    ),
                ),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book1, book2))

            // ----- Assert -----
            result.formats shouldBe listOf("ebook", "paperback")
        }

        @Test
        fun `excludes blank formats`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                editions = listOf(
                    buildEdition(
                        id = 1,
                        format = "",
                    ),
                    buildEdition(
                        id = 2,
                        format = "hardcover",
                    ),
                ),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.formats shouldBe listOf("hardcover")
        }

        @Test
        fun `returns distinct positive release years sorted descending`() {
            // ----- Arrange -----
            val book1 = buildBook(
                id = 1,
                releaseYear = 2019,
            )
            val book2 = buildBook(
                id = 2,
                releaseYear = 2021,
            )
            val book3 = buildBook(
                id = 3,
                releaseYear = 2019,
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book1, book2, book3))

            // ----- Assert -----
            result.releaseYears shouldBe listOf(2021, 2019)
        }

        @Test
        fun `excludes release years that are zero or negative`() {
            // ----- Arrange -----
            val book1 = buildBook(
                id = 1,
                releaseYear = 2021,
            )
            val book2 = buildBook(
                id = 2,
                releaseYear = -1,
            )
            val book3 = buildBook(
                id = 3,
                releaseYear = 0,
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book1, book2, book3))

            // ----- Assert -----
            result.releaseYears shouldBe listOf(2021)
        }

        @Test
        fun `supportsOwnedFilter is true when a book has an owned edition`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                editions = listOf(
                    buildEdition(
                        id = 1,
                        owned = true,
                    ),
                ),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.supportsOwnedFilter shouldBe true
        }

        @Test
        fun `supportsOwnedFilter is false when no book has an owned edition`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                editions = listOf(
                    buildEdition(
                        id = 1,
                        owned = false,
                    ),
                ),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.supportsOwnedFilter shouldBe false
        }

        @Test
        fun `ratingBuckets only contains buckets at or below max rating`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                rating = 4.0,
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.ratingBuckets shouldBe listOf(4.0, 3.5, 3.0)
        }

        @Test
        fun `ratingBuckets is empty when no book has a positive rating`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                rating = 0.0,
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.ratingBuckets shouldBe emptyList()
        }

        @Test
        fun `lists community tags of every category`() {
            // ----- Arrange -----
            val tagMood = Tag(
                id = 3,
                name = "Hopeful",
                category = TagCategory.MOOD,
            )
            val tagWarning = Tag(
                id = 4,
                name = "Violence",
                category = TagCategory.CONTENT_WARNING,
            )
            val book = buildBook(
                id = 1,
                tags = listOf(tagFiction, tagMood, tagWarning),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.hardcoverTags.map { it.category }.toSet() shouldBe setOf(
                TagCategory.GENRE,
                TagCategory.MOOD,
                TagCategory.CONTENT_WARNING,
            )
        }

        @Test
        fun `counts the user's own tags per book and leaves books without any out`() {
            // ----- Arrange -----
            val book1 = buildBook(id = 1)
            val book2 = buildBook(id = 2)
            val book3 = buildBook(id = 3)
            val userTags = mapOf(
                1 to listOf(userCozy, userSlow),
                2 to listOf(userCozy),
            )

            // ----- Act -----
            val result = buildBookFilterOptions(
                books = listOf(book1, book2, book3),
                userTagsByBookId = userTags,
            )

            // ----- Assert -----
            result.yourTags.map { it.key.name to it.count } shouldBe listOf(
                "cozy" to 2,
                "slow" to 1,
            )
        }

        @Test
        fun `offers no Yours options when no book has own tags`() {
            // ----- Arrange -----
            val book = buildBook(id = 1)

            // ----- Act -----
            val result = buildBookFilterOptions(books = listOf(book))

            // ----- Assert -----
            result.yourTags shouldBe emptyList()
        }

        @Nested
        inner class ReadYears {
            @Test
            fun `is empty when isReadTab is false even though books have finishedYear`() {
                // ----- Arrange -----
                val book = buildBook(
                    id = 1,
                    finishedYear = 2021,
                )

                // ----- Act -----
                val result = buildBookFilterOptions(
                    books = listOf(book),
                    isReadTab = false,
                )

                // ----- Assert -----
                result.readYears shouldBe emptyList()
            }

            @Test
            fun `is empty when isReadTab is true but no book has a finishedYear`() {
                // ----- Arrange -----
                val book = buildBook(
                    id = 1,
                    finishedYear = null,
                )

                // ----- Act -----
                val result = buildBookFilterOptions(
                    books = listOf(book),
                    isReadTab = true,
                )

                // ----- Assert -----
                result.readYears shouldBe emptyList()
            }

            @Test
            fun `returns distinct finishedYears sorted descending when isReadTab is true`() {
                // ----- Arrange -----
                val book1 = buildBook(
                    id = 1,
                    finishedYear = 2019,
                )
                val book2 = buildBook(
                    id = 2,
                    finishedYear = 2021,
                )
                val book3 = buildBook(
                    id = 3,
                    finishedYear = 2019,
                )

                // ----- Act -----
                val result = buildBookFilterOptions(
                    books = listOf(book1, book2, book3),
                    isReadTab = true,
                )

                // ----- Assert -----
                result.readYears shouldBe listOf(2021, 2019)
            }
        }
    }

    @Nested
    inner class BuildEditionFilterOptions {
        @Test
        fun `community tags sourced from parent books via bookByBookId`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 1,
                bookId = 10,
            )
            val book = buildBook(
                id = 10,
                tags = listOf(tagFiction),
            )

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition),
                bookByBookId = mapOf(10 to book),
            )

            // ----- Assert -----
            result.hardcoverTags.map { it.key.id } shouldBe listOf(tagFiction.id)
        }

        @Test
        fun `counts a parent book once when several of its editions are on the tab`() {
            // ----- Arrange -----
            val edition1 = buildEdition(
                id = 1,
                bookId = 10,
            )
            val edition2 = buildEdition(
                id = 2,
                bookId = 10,
            )
            val book = buildBook(
                id = 10,
                tags = listOf(tagFiction),
            )

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition1, edition2),
                bookByBookId = mapOf(10 to book),
                userTagsByBookId = mapOf(10 to listOf(userCozy)),
            )

            // ----- Assert -----
            result.hardcoverTags.single().count shouldBe 1
            result.yourTags.single().count shouldBe 1
        }

        @Test
        fun `own tags are resolved through the edition's parent book id`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 1,
                bookId = 10,
            )
            val book = buildBook(id = 10)

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition),
                bookByBookId = mapOf(10 to book),
                userTagsByBookId = mapOf(10 to listOf(userSlow)),
            )

            // ----- Assert -----
            result.yourTags.map { it.key.name } shouldBe listOf("slow")
        }

        @Test
        fun `formats sourced from editions themselves`() {
            // ----- Arrange -----
            val edition1 = buildEdition(
                id = 1,
                bookId = 10,
                format = "ebook",
            )
            val edition2 = buildEdition(
                id = 2,
                bookId = 10,
                format = "audiobook",
            )

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition1, edition2),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.formats shouldBe listOf("audiobook", "ebook")
        }

        @Test
        fun `supportsOwnedFilter is true when any edition is owned`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 1,
                owned = true,
            )

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition),
                bookByBookId = emptyMap(),
            )

            // ----- Assert -----
            result.supportsOwnedFilter shouldBe true
        }

        @Test
        fun `release years sourced from parent books when present`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 1,
                bookId = 10,
            )
            val book = buildBook(
                id = 10,
                releaseYear = 2022,
            )

            // ----- Act -----
            val result = buildEditionFilterOptions(
                editions = listOf(edition),
                bookByBookId = mapOf(10 to book),
            )

            // ----- Assert -----
            result.releaseYears shouldBe listOf(2022)
        }
    }

    private fun buildBook(
        id: Int,
        tags: List<Tag> = emptyList(),
        editions: List<BookEdition> = listOf(
            buildEdition(
                id = id,
                bookId = id,
            ),
        ),
        releaseYear: Int = 2020,
        rating: Double = 0.0,
        finishedYear: Int? = null,
    ): Book = Book(
        id = id,
        title = "Book $id",
        editions = editions,
        defaultEdition = editions.firstOrNull(),
        rating = rating,
        description = "",
        releaseYear = releaseYear,
        coverUrl = "",
        authors = listOf(
            Author(
                id = id,
                name = "Author $id",
            ),
        ),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        tags = tags,
        userBook = finishedYear?.let {
            UserBook(
                id = id,
                status = BookStatus.Read,
                dateAdded = "2020-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = null,
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = listOf(
                    UserBookRead(
                        id = id,
                        currentPage = null,
                        currentSeconds = null,
                        progress = 1f,
                        startedAt = null,
                        finishedAt = "$it-06-15",
                        finishedAtPrecision = null,
                        editionPages = null,
                    ),
                ),
            )
        },
    )

    private fun buildEdition(
        id: Int,
        bookId: Int = id,
        format: String = "",
        owned: Boolean = false,
    ): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = bookId,
        publisher = null,
        title = "Edition $id",
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = emptyList(),
        releaseYear = 2020,
        format = format,
        owned = owned,
    )
}
