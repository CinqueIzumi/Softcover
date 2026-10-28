package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentSetOf
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

class LibraryFiltersTest {
    // region Fixtures
    private val tagFiction = Tag(
        id = 1,
        name = "Fiction",
    )
    private val tagScifi = Tag(
        id = 2,
        name = "Sci-Fi",
    )
    private val keyFiction = HardcoverTagKey(
        id = 1,
        name = "Fiction",
    )
    private val keyScifi = HardcoverTagKey(
        id = 2,
        name = "Sci-Fi",
    )
    private val userCozy = UserTag(
        name = "cozy",
        category = TagCategory.MOOD,
    )
    private val userSlow = UserTag(
        name = "slow",
        category = TagCategory.MOOD,
    )
    private val userCozyGenre = UserTag(
        name = "cozy",
        category = TagCategory.GENRE,
    )
    private val keyCozy = UserTagKey(
        category = TagCategory.MOOD,
        name = "cozy",
    )
    private val keySlow = UserTagKey(
        category = TagCategory.MOOD,
        name = "slow",
    )

    private fun buildEdition(
        id: Int = 1,
        bookId: Int = 1,
        format: String = "paperback",
        owned: Boolean = false,
        releaseYear: Int = 2020,
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
        authors = emptyList(),
        releaseYear = releaseYear,
        releaseDate = null,
        format = format,
        owned = owned,
    )

    private fun buildBook(
        id: Int = 1,
        tags: List<Tag> = emptyList(),
        editions: List<BookEdition> = listOf(buildEdition()),
        rating: Double = 4.0,
        releaseYear: Int = 2020,
        finishedYear: Int? = null,
    ) = Book(
        id = id,
        canonicalId = null,
        title = "A Book",
        editions = editions,
        defaultEdition = null,
        rating = rating,
        description = "",
        releaseYear = releaseYear,
        releaseDate = null,
        coverUrl = "",
        authors = emptyList(),
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
                    ),
                ),
            )
        },
    )
    // endregion
    @Nested
    inner class IsEmpty {
        @Test
        fun `returns true when all facets are at their defaults`() {
            // ----- Arrange -----
            val filters = LibraryFilters()

            // ----- Act -----
            val result = filters.isEmpty

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `returns false when only an excluded Hardcover tag is picked`() {
            // ----- Arrange -----
            val filters = LibraryFilters(hardcoverTags = TagSelection(excluded = persistentSetOf(keyFiction)))

            // ----- Act -----
            val result = filters.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `returns false when only an included Yours tag is picked`() {
            // ----- Arrange -----
            val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(keyCozy)))

            // ----- Act -----
            val result = filters.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `returns false when formats is non-empty`() {
            // ----- Arrange -----
            val filters = LibraryFilters(formats = persistentSetOf("ebook"))

            // ----- Act -----
            val result = filters.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `returns false when releaseYears is non-empty`() {
            // ----- Arrange -----
            val filters = LibraryFilters(releaseYears = persistentSetOf(2021))

            // ----- Act -----
            val result = filters.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `returns false when readYear is non-null`() {
            // ----- Arrange -----
            val filters = LibraryFilters(readYear = 2021)

            // ----- Act -----
            val result = filters.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `returns false when owned is non-null`() {
            // ----- Arrange -----
            val filters = LibraryFilters(owned = true)

            // ----- Act -----
            val result = filters.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `returns false when ratingMin is non-null`() {
            // ----- Arrange -----
            val filters = LibraryFilters(ratingMin = 3.0)

            // ----- Act -----
            val result = filters.isEmpty

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class MatchesBook {
        @Nested
        inner class HardcoverTagFacet {
            @Test
            fun `matches when book carries every included tag`() {
                // ----- Arrange -----
                val book = buildBook(tags = listOf(tagFiction, tagScifi))
                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction, keyScifi)),
                )

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when book carries only some of the included tags`() {
                // ----- Arrange -----
                val book = buildBook(tags = listOf(tagFiction))
                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction, keyScifi)),
                )

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `rejects when book carries an excluded tag`() {
                // ----- Arrange -----
                val book = buildBook(tags = listOf(tagFiction, tagScifi))
                val filters = LibraryFilters(hardcoverTags = TagSelection(excluded = persistentSetOf(keyScifi)))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `matches when book carries none of the excluded tags`() {
                // ----- Arrange -----
                val book = buildBook(tags = listOf(tagFiction))
                val filters = LibraryFilters(hardcoverTags = TagSelection(excluded = persistentSetOf(keyScifi)))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `matches an untagged book against an exclusion`() {
                // ----- Arrange -----
                val book = buildBook(tags = emptyList())
                val filters = LibraryFilters(hardcoverTags = TagSelection(excluded = persistentSetOf(keyScifi)))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `requires the included tag and the absence of the excluded tag together`() {
                // ----- Arrange -----
                val book = buildBook(tags = listOf(tagFiction, tagScifi))
                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(
                        included = persistentSetOf(keyFiction),
                        excluded = persistentSetOf(keyScifi),
                    ),
                )

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class YourTagFacet {
            @Test
            fun `matches when the user's tags include every included tag`() {
                // ----- Arrange -----
                val book = buildBook()
                val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(keyCozy, keySlow)))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = listOf(userCozy, userSlow),
                ) shouldBe true
            }

            @Test
            fun `rejects when the user's tags miss one of the included tags`() {
                // ----- Arrange -----
                val book = buildBook()
                val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(keyCozy, keySlow)))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = listOf(userCozy),
                ) shouldBe false
            }

            @Test
            fun `rejects a book with no own tags against an include`() {
                // ----- Arrange -----
                val book = buildBook()
                val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(keyCozy)))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `matches a book with no own tags against an exclusion`() {
                // ----- Arrange -----
                val book = buildBook()
                val filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(keyCozy)))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when the user's tags carry an excluded tag`() {
                // ----- Arrange -----
                val book = buildBook()
                val filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(keyCozy)))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = listOf(userCozy),
                ) shouldBe false
            }

            @Test
            fun `does not treat a tag of the same name in another category as the same tag`() {
                // ----- Arrange -----
                val book = buildBook()
                val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(keyCozy)))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = listOf(userCozyGenre),
                ) shouldBe false
            }

            @Test
            fun `ignores the book's community tags`() {
                // ----- Arrange -----
                val book = buildBook(tags = listOf(tagFiction))
                val filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(keyCozy)))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }
        }

        @Nested
        inner class TagSourcesCombined {
            @Test
            fun `rejects when the Hardcover selection passes but the Yours selection fails`() {
                // ----- Arrange -----
                val book = buildBook(tags = listOf(tagFiction))
                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction)),
                    yourTags = TagSelection(included = persistentSetOf(keyCozy)),
                )

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `rejects when the Yours selection passes but the Hardcover selection fails`() {
                // ----- Arrange -----
                val book = buildBook(tags = emptyList())
                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction)),
                    yourTags = TagSelection(included = persistentSetOf(keyCozy)),
                )

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = listOf(userCozy),
                ) shouldBe false
            }

            @Test
            fun `matches when both selections pass`() {
                // ----- Arrange -----
                val book = buildBook(tags = listOf(tagFiction))
                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction)),
                    yourTags = TagSelection(included = persistentSetOf(keyCozy)),
                )

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = listOf(userCozy),
                ) shouldBe true
            }
        }

        @Nested
        inner class FormatFacet {
            @Test
            fun `matches when any edition's format is in the filter set`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(format = "ebook")))
                val filters = LibraryFilters(formats = persistentSetOf("ebook"))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when no edition's format is in the filter set`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(format = "paperback")))
                val filters = LibraryFilters(formats = persistentSetOf("ebook"))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class ReleaseYearFacet {
            @Test
            fun `matches when book's releaseYear is in the filter set`() {
                // ----- Arrange -----
                val book = buildBook(releaseYear = 2021)
                val filters = LibraryFilters(releaseYears = persistentSetOf(2021))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when book's releaseYear is not in the filter set`() {
                // ----- Arrange -----
                val book = buildBook(releaseYear = 2019)
                val filters = LibraryFilters(releaseYears = persistentSetOf(2021))

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class ReadYearFacet {
            @Test
            fun `matches when finishedYear equals readYear`() {
                // ----- Arrange -----
                val book = buildBook(finishedYear = 2021)
                val filters = LibraryFilters(readYear = 2021)

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when finishedYear differs from readYear`() {
                // ----- Arrange -----
                val book = buildBook(finishedYear = 2020)
                val filters = LibraryFilters(readYear = 2021)

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `rejects when book has no finishedYear and readYear is active`() {
                // ----- Arrange -----
                val book = buildBook(finishedYear = null)
                val filters = LibraryFilters(readYear = 2021)

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class OwnedFacet {
            @Test
            fun `owned=true matches when at least one edition is owned`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(owned = true)))
                val filters = LibraryFilters(owned = true)

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `owned=true rejects when no edition is owned`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(owned = false)))
                val filters = LibraryFilters(owned = true)

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `owned=false matches when no edition is owned`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(owned = false)))
                val filters = LibraryFilters(owned = false)

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `owned=false rejects when any edition is owned`() {
                // ----- Arrange -----
                val book = buildBook(editions = listOf(buildEdition(owned = true)))
                val filters = LibraryFilters(owned = false)

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class RatingMinFacet {
            @Test
            fun `matches when book rating equals the threshold`() {
                // ----- Arrange -----
                val book = buildBook(rating = 4.0)
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when book rating is below the threshold`() {
                // ----- Arrange -----
                val book = buildBook(rating = 3.5)
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class CombinedFacets {
            @Test
            fun `all facets must pass — rejects when only one facet fails`() {
                // ----- Arrange -----
                val book = buildBook(
                    tags = listOf(tagFiction),
                    editions = listOf(
                        buildEdition(
                            format = "paperback",
                            owned = true,
                        ),
                    ),
                    rating = 4.0,
                    releaseYear = 2020,
                    finishedYear = 2021,
                )

                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction)),
                    formats = persistentSetOf("paperback"),
                    releaseYears = persistentSetOf(2020),
                    readYear = 2021,
                    owned = true,
                    ratingMin = 4.5,
                )

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `all facets must pass — matches when every facet is satisfied`() {
                // ----- Arrange -----
                val book = buildBook(
                    tags = listOf(tagFiction),
                    editions = listOf(
                        buildEdition(
                            format = "paperback",
                            owned = true,
                        ),
                    ),
                    rating = 4.5,
                    releaseYear = 2020,
                    finishedYear = 2021,
                )

                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction)),
                    formats = persistentSetOf("paperback"),
                    releaseYears = persistentSetOf(2020),
                    readYear = 2021,
                    owned = true,
                    ratingMin = 4.5,
                )

                // ----- Act & Assert -----
                filters.matchesBook(
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }
        }
    }

    @Nested
    inner class MatchesEdition {
        @Nested
        inner class HardcoverTagFacet {
            @Test
            fun `matches when parent book carries every included tag`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(tags = listOf(tagFiction, tagScifi))
                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction, keyScifi)),
                )

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when parent book carries only some of the included tags`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(tags = listOf(tagScifi))
                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction, keyScifi)),
                )

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `rejects when parent book carries an excluded tag`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(tags = listOf(tagScifi))
                val filters = LibraryFilters(hardcoverTags = TagSelection(excluded = persistentSetOf(keyScifi)))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `rejects when parent book is missing and an include is active`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val filters = LibraryFilters(hardcoverTags = TagSelection(included = persistentSetOf(keyFiction)))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `matches when parent book is missing and only an exclusion is active`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val filters = LibraryFilters(hardcoverTags = TagSelection(excluded = persistentSetOf(keyFiction)))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe true
            }
        }

        @Nested
        inner class YourTagFacet {
            @Test
            fun `matches when the parent book's own tags include every included tag`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook()
                val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(keyCozy)))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = listOf(userCozy),
                ) shouldBe true
            }

            @Test
            fun `rejects when the parent book carries an excluded own tag`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook()
                val filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(keyCozy)))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = listOf(userCozy),
                ) shouldBe false
            }

            @Test
            fun `rejects an edition with no own tags against an include`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val filters = LibraryFilters(yourTags = TagSelection(included = persistentSetOf(keyCozy)))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `matches an edition with no own tags against an exclusion`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val filters = LibraryFilters(yourTags = TagSelection(excluded = persistentSetOf(keyCozy)))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe true
            }
        }

        @Nested
        inner class FormatFacet {
            @Test
            fun `matches when edition format is in the filter set`() {
                // ----- Arrange -----
                val edition = buildEdition(format = "ebook")
                val filters = LibraryFilters(formats = persistentSetOf("ebook"))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when edition format is not in the filter set`() {
                // ----- Arrange -----
                val edition = buildEdition(format = "audiobook")
                val filters = LibraryFilters(formats = persistentSetOf("ebook"))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class ReleaseYearFacet {
            @Test
            fun `uses book releaseYear when book is supplied`() {
                // ----- Arrange -----
                val edition = buildEdition(releaseYear = 2019)
                val book = buildBook(releaseYear = 2021)
                val filters = LibraryFilters(releaseYears = persistentSetOf(2021))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `falls back to edition releaseYear when book is null`() {
                // ----- Arrange -----
                val edition = buildEdition(releaseYear = 2021)
                val filters = LibraryFilters(releaseYears = persistentSetOf(2021))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when edition releaseYear does not match filter and book is null`() {
                // ----- Arrange -----
                val edition = buildEdition(releaseYear = 2019)
                val filters = LibraryFilters(releaseYears = persistentSetOf(2021))

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class ReadYearFacet {
            @Test
            fun `matches when parent book's finishedYear equals readYear`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(finishedYear = 2021)
                val filters = LibraryFilters(readYear = 2021)

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when parent book's finishedYear differs from readYear`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(finishedYear = 2020)
                val filters = LibraryFilters(readYear = 2021)

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `rejects when book is null and readYear is active`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val filters = LibraryFilters(readYear = 2021)

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class OwnedFacet {
            @Test
            fun `owned=true matches when edition is owned`() {
                // ----- Arrange -----
                val edition = buildEdition(owned = true)
                val filters = LibraryFilters(owned = true)

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `owned=true rejects when edition is not owned`() {
                // ----- Arrange -----
                val edition = buildEdition(owned = false)
                val filters = LibraryFilters(owned = true)

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `owned=false rejects when edition is owned`() {
                // ----- Arrange -----
                val edition = buildEdition(owned = true)
                val filters = LibraryFilters(owned = false)

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class RatingMinFacet {
            @Test
            fun `matches when book rating meets threshold`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(rating = 4.5)
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }

            @Test
            fun `rejects when book rating is below threshold`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val book = buildBook(rating = 3.0)
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `rejects when book is null and ratingMin is active (treats rating as 0_0)`() {
                // ----- Arrange -----
                val edition = buildEdition()
                val filters = LibraryFilters(ratingMin = 4.0)

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = null,
                    userTags = emptyList(),
                ) shouldBe false
            }
        }

        @Nested
        inner class CombinedFacets {
            @Test
            fun `rejects when one facet fails out of multiple active facets`() {
                // ----- Arrange -----
                val edition = buildEdition(
                    format = "paperback",
                    owned = true,
                    releaseYear = 2020,
                )
                val book = buildBook(
                    tags = listOf(tagFiction),
                    rating = 3.5,
                    releaseYear = 2020,
                )

                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction)),
                    formats = persistentSetOf("paperback"),
                    releaseYears = persistentSetOf(2020),
                    owned = true,
                    ratingMin = 4.0,
                )

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = emptyList(),
                ) shouldBe false
            }

            @Test
            fun `matches when all facets are satisfied`() {
                // ----- Arrange -----
                val edition = buildEdition(
                    format = "paperback",
                    owned = true,
                    releaseYear = 2020,
                )
                val book = buildBook(
                    tags = listOf(tagFiction),
                    rating = 4.5,
                    releaseYear = 2020,
                )

                val filters = LibraryFilters(
                    hardcoverTags = TagSelection(included = persistentSetOf(keyFiction)),
                    formats = persistentSetOf("paperback"),
                    releaseYears = persistentSetOf(2020),
                    owned = true,
                    ratingMin = 4.0,
                )

                // ----- Act & Assert -----
                filters.matchesEdition(
                    edition = edition,
                    book = book,
                    userTags = emptyList(),
                ) shouldBe true
            }
        }
    }
}
