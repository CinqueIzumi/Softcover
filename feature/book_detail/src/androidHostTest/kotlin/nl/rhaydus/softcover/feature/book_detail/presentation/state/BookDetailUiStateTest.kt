package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.component.callout.CalloutUiModel
import nl.rhaydus.softcover.core.component.callout.CalloutVariant
import nl.rhaydus.softcover.core.component.topbar.TopBarSurface
import nl.rhaydus.softcover.core.component.verdict.VerdictBlockUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import nl.rhaydus.softcover.feature.book_detail.presentation.buildBookDetailUiState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDetailUiStateTest {
    private fun stubEdition(
        id: Int = 1,
        releaseYear: Int = 0,
    ): BookEdition = mockk<BookEdition>().also { mock ->
        every {
            mock.id
        } returns id
        every {
            mock.releaseYear
        } returns releaseYear
    }

    private fun stubBook(
        userBook: UserBook?,
        currentEdition: BookEdition?,
        editions: List<BookEdition> = emptyList(),
        releaseYear: Int = 0,
    ): Book = mockk<Book>().also { mock ->
        every {
            mock.releaseYear
        } returns releaseYear
        every {
            mock.userBook
        } returns userBook
        every {
            mock.currentEdition
        } returns currentEdition
        every {
            mock.editions
        } returns editions
    }

    private fun stubUserBook(editionId: Int? = null): UserBook = mockk<UserBook>().also { mock ->
        every {
            mock.editionId
        } returns editionId
    }

    @Nested
    inner class DisplayedEdition {
        @Test
        fun `returns previewEdition when book has no user book and previewEdition is set`() {
            // ----- Arrange -----
            val preview = stubEdition(id = 5)
            val bookEdition = stubEdition(id = 10)
            val book = stubBook(
                userBook = null,
                currentEdition = bookEdition,
            )
            val state = buildBookDetailUiState().copy(
                book = book,
                previewEdition = preview,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe preview
        }

        @Test
        fun `falls back to book currentEdition when off-shelf and no preview`() {
            // ----- Arrange -----
            val bookEdition = stubEdition(id = 10)
            val book = stubBook(
                userBook = null,
                currentEdition = bookEdition,
            )
            val state = buildBookDetailUiState().copy(
                book = book,
                previewEdition = null,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe bookEdition
        }

        @Test
        fun `falls back to initialCover currentEdition when off-shelf, no preview, and no book`() {
            // ----- Arrange -----
            val coverEdition = stubEdition(id = 20)
            val initialCover = BookInitialCover(
                currentEdition = coverEdition,
                defaultEdition = null,
                fallbackCoverUrl = null,
            )
            val state = buildBookDetailUiState().copy(
                book = null,
                previewEdition = null,
                initialCover = initialCover,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe coverEdition
        }

        @Test
        fun `ignores previewEdition and returns book currentEdition when user book is present`() {
            // ----- Arrange -----
            val preview = stubEdition(id = 5)
            val bookEdition = stubEdition(id = 10)
            val userBook = mockk<UserBook>()
            val book = stubBook(
                userBook = userBook,
                currentEdition = bookEdition,
            )
            val state = buildBookDetailUiState().copy(
                book = book,
                previewEdition = preview,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe bookEdition
        }

        @Test
        fun `returns scanned edition from book editions even when userBook has a different editionId`() {
            // ----- Arrange -----
            val scannedEdition = stubEdition(id = 42)
            val shelvedEdition = stubEdition(id = 99)
            val userBook = stubUserBook(editionId = 99)
            val book = stubBook(
                userBook = userBook,
                currentEdition = shelvedEdition,
                editions = persistentListOf(scannedEdition, shelvedEdition),
            )
            val state = buildBookDetailUiState().copy(
                book = book,
                scannedEditionId = 42,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe scannedEdition
        }

        @Test
        fun `resolves scanned edition from editions when the book does not carry it`() {
            // ----- Arrange -----
            val scannedEdition = stubEdition(id = 42)
            val book = stubBook(
                userBook = null,
                currentEdition = null,
                editions = persistentListOf(stubEdition(id = 1)),
            )
            val state = buildBookDetailUiState().copy(
                book = book,
                editions = persistentListOf(scannedEdition),
                scannedEditionId = 42,
            )

            // ----- Act -----
            val result = state.scannedEdition

            // ----- Assert -----
            result shouldBe scannedEdition
        }

        @Test
        fun `falls back to book currentEdition when scannedEditionId is null and userBook is present`() {
            // ----- Arrange -----
            val bookEdition = stubEdition(id = 10)
            val userBook = stubUserBook(editionId = 10)
            val book = stubBook(
                userBook = userBook,
                currentEdition = bookEdition,
            )
            val state = buildBookDetailUiState().copy(
                book = book,
                scannedEditionId = null,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe bookEdition
        }

        @Test
        fun `resolves scanned edition from initialCover currentEdition when editions list is empty`() {
            // ----- Arrange -----
            val coverEdition = stubEdition(id = 7)
            val initialCover = BookInitialCover(
                currentEdition = coverEdition,
                defaultEdition = null,
                fallbackCoverUrl = null,
            )
            val state = buildBookDetailUiState().copy(
                book = null,
                editions = persistentListOf(),
                initialCover = initialCover,
                scannedEditionId = 7,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe coverEdition
        }
    }

    @Nested
    inner class FilteredEditions {
        private val editionWithIsbn = PreviewData.baseEdition.copy(
            id = 1,
            isbn10 = "0385333498",
            publisher = "Doubleday",
        )

        private val editionWithoutIsbn = PreviewData.baseEdition.copy(
            id = 2,
            isbn10 = null,
            publisher = "Penguin Random House",
        )

        private val editionNullBoth = PreviewData.baseEdition.copy(
            id = 3,
            isbn10 = null,
            publisher = null,
        )

        @Test
        fun `returns all editions when query is empty`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                editions = persistentListOf(editionWithIsbn, editionWithoutIsbn),
                editionSearchQuery = "",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldContainExactly listOf(editionWithIsbn, editionWithoutIsbn)
        }

        @Test
        fun `filters by publisher substring match`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                editions = persistentListOf(editionWithIsbn, editionWithoutIsbn),
                editionSearchQuery = "Penguin",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldContainExactly listOf(editionWithoutIsbn)
        }

        @Test
        fun `edition with both isbn10 and publisher null does not crash and is excluded`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                editions = persistentListOf(editionNullBoth),
                editionSearchQuery = "anything",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result.shouldBeEmpty()
        }
    }

    @Nested
    inner class IsEditionOwned {
        private fun listBook(editionId: Int): ListBook = ListBook(
            listBookId = editionId * 10,
            listId = 1,
            bookId = 100,
            editionId = editionId,
        )

        private fun ownedList(vararg editionIds: Int): BookList = BookList(
            id = 1,
            name = "Owned",
            slug = "owned",
            books = editionIds.map { listBook(it) },
        )

        @Test
        fun `isEditionOwned returns true when edition id is in the owned list`() {
            // ----- Arrange -----
            val edition = stubEdition(id = 77)
            val state = buildBookDetailUiState().copy(
                userLists = persistentListOf(ownedList(77)),
            )

            // ----- Act -----
            val result = state.isEditionOwned(edition)

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `isEditionOwned returns false when edition id is not in the owned list`() {
            // ----- Arrange -----
            val edition = stubEdition(id = 99)
            val state = buildBookDetailUiState().copy(
                userLists = persistentListOf(ownedList(77)),
            )

            // ----- Act -----
            val result = state.isEditionOwned(edition)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `isEditionOwned returns false when no list has slug owned`() {
            // ----- Arrange -----
            val edition = stubEdition(id = 77)
            val nonOwnedList = BookList(
                id = 2,
                name = "Favorites",
                slug = "favorites",
                books = listOf(listBook(77)),
            )
            val state = buildBookDetailUiState().copy(
                userLists = persistentListOf(nonOwnedList),
            )

            // ----- Act -----
            val result = state.isEditionOwned(edition)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `isEditionOwned returns false when edition is null`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                userLists = persistentListOf(ownedList(77)),
            )

            // ----- Act -----
            val result = state.isEditionOwned(null)

            // ----- Assert -----
            result shouldBe false
        }
    }

    private fun stubHeroBook(
        title: String = "Dune",
        rating: Double = 4.5,
        seriesText: String? = null,
    ): Book = mockk<Book>().also { mock ->
        every {
            mock.title
        } returns title
        every {
            mock.rating
        } returns rating
        every {
            mock.seriesText
        } returns seriesText
    }

    private fun heroInitialCover(
        title: String? = "Seed title",
        seriesText: String? = "Seed series #1",
        rating: Double? = 3.2,
    ): BookInitialCover = BookInitialCover(
        currentEdition = null,
        defaultEdition = null,
        fallbackCoverUrl = null,
        title = title,
        seriesText = seriesText,
        rating = rating,
    )

    @Nested
    inner class HeroFields {
        @Test
        fun `falls back to initialCover values when book is null`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                book = null,
                initialCover = heroInitialCover(),
            )

            // ----- Act -----
            val title = state.heroTitle
            val series = state.heroSeriesText
            val rating = state.heroRating

            // ----- Assert -----
            title shouldBe "Seed title"
            series shouldBe "Seed series #1"
            rating shouldBe 3.2
        }

        @Test
        fun `loaded book title and rating win over initialCover`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                book = stubHeroBook(
                    title = "Dune",
                    rating = 4.5,
                ),
                initialCover = heroInitialCover(),
            )

            // ----- Act -----
            val title = state.heroTitle
            val rating = state.heroRating

            // ----- Assert -----
            title shouldBe "Dune"
            rating shouldBe 4.5
        }

        @Test
        fun `loaded book without series gives empty heroSeriesText instead of initialCover value`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                book = stubHeroBook(seriesText = null),
                initialCover = heroInitialCover(seriesText = "Seed series #1"),
            )

            // ----- Act -----
            val result = state.heroSeriesText

            // ----- Assert -----
            result.shouldNotBeNull() shouldBe ""
        }

        @Test
        fun `loaded book with series gives its seriesText`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                book = stubHeroBook(seriesText = "Dune #1"),
                initialCover = heroInitialCover(),
            )

            // ----- Act -----
            val result = state.heroSeriesText

            // ----- Assert -----
            result shouldBe "Dune #1"
        }

        @Test
        fun `all hero fields are null without a book or initialCover`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                book = null,
                initialCover = null,
            )

            // ----- Act & Assert -----
            state.heroTitle.shouldBeNull()
            state.heroSeriesText.shouldBeNull()
            state.heroRating.shouldBeNull()
        }
    }

    @Nested
    inner class HeroReleaseYear {
        @Test
        fun `uses the displayed edition year when it is set`() {
            // ----- Arrange -----
            val edition = stubEdition(releaseYear = 2011)
            val book = stubBook(
                userBook = null,
                currentEdition = edition,
                releaseYear = 1999,
            )
            val state = buildBookDetailUiState().copy(book = book)

            // ----- Act -----
            val result = state.heroReleaseYear

            // ----- Assert -----
            result shouldBe 2011
        }

        @Test
        fun `falls back to the book year when the edition year is -1`() {
            // ----- Arrange -----
            val edition = stubEdition(releaseYear = -1)
            val book = stubBook(
                userBook = null,
                currentEdition = edition,
                releaseYear = 1999,
            )
            val state = buildBookDetailUiState().copy(book = book)

            // ----- Act -----
            val result = state.heroReleaseYear

            // ----- Assert -----
            result shouldBe 1999
        }

        @Test
        fun `falls back to the book year when there is no edition`() {
            // ----- Arrange -----
            val book = stubBook(
                userBook = null,
                currentEdition = null,
                releaseYear = 1999,
            )
            val state = buildBookDetailUiState().copy(book = book)

            // ----- Act -----
            val result = state.heroReleaseYear

            // ----- Assert -----
            result shouldBe 1999
        }

        @Test
        fun `is null when there is no book and no edition`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                book = null,
                initialCover = null,
            )

            // ----- Act -----
            val result = state.heroReleaseYear

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class FailedHeroEditionId {
        @Test
        fun `returns the current edition id when it is in failedMutationEditionIds`() {
            // ----- Arrange -----
            val book = stubBook(
                userBook = null,
                currentEdition = stubEdition(id = 7),
            )
            val state = buildBookDetailUiState().copy(
                book = book,
                failedMutationEditionIds = persistentSetOf(
                    7,
                    8,
                ),
            )

            // ----- Act -----
            val result = state.failedHeroEditionId

            // ----- Assert -----
            result shouldBe 7
        }

        @Test
        fun `is null when the current edition id is not in failedMutationEditionIds`() {
            // ----- Arrange -----
            val book = stubBook(
                userBook = null,
                currentEdition = stubEdition(id = 7),
            )
            val state = buildBookDetailUiState().copy(
                book = book,
                failedMutationEditionIds = persistentSetOf(8),
            )

            // ----- Act -----
            val result = state.failedHeroEditionId

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `is null when there is no book`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(
                book = null,
                failedMutationEditionIds = persistentSetOf(7),
            )

            // ----- Act -----
            val result = state.failedHeroEditionId

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class WithBook {
        @Test
        fun `retires pendingShelfStatus when the incoming book has a user book`() {
            // ----- Arrange -----
            val book = stubBook(
                userBook = stubUserBook(),
                currentEdition = null,
            ).also { mock ->
                every {
                    mock.title
                } returns "Piranesi"
            }
            val state = buildBookDetailUiState().copy(pendingShelfStatus = BookStatus.Reading)

            // ----- Act -----
            val result = state.withBook(book = book)

            // ----- Assert -----
            result.pendingShelfStatus.shouldBeNull()
        }

        @Test
        fun `keeps pendingShelfStatus when the incoming book has no user book`() {
            // ----- Arrange -----
            val book = stubBook(
                userBook = null,
                currentEdition = null,
            ).also { mock ->
                every {
                    mock.title
                } returns "Piranesi"
            }
            val state = buildBookDetailUiState().copy(pendingShelfStatus = BookStatus.Reading)

            // ----- Act -----
            val result = state.withBook(book = book)

            // ----- Assert -----
            result.pendingShelfStatus shouldBe BookStatus.Reading
        }

        @Test
        fun `keeps pendingShelfStatus when the book goes away`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(pendingShelfStatus = BookStatus.Read)

            // ----- Act -----
            val result = state.withBook(book = null)

            // ----- Assert -----
            result.pendingShelfStatus shouldBe BookStatus.Read
        }

        @Test
        fun `writes the book and a top bar titled with it`() {
            // ----- Arrange -----
            val book = stubBook(
                userBook = null,
                currentEdition = null,
            ).also { mock ->
                every {
                    mock.title
                } returns "Piranesi"
            }

            // ----- Act -----
            val result = buildBookDetailUiState().withBook(book = book)

            // ----- Assert -----
            result.book shouldBe book
            result.topBar.title shouldBe "Piranesi"
            result.topBar.surface shouldBe TopBarSurface.Opaque
        }

        @Test
        fun `clears the top bar title when the book goes away`() {
            // ----- Arrange -----
            val book = stubBook(
                userBook = null,
                currentEdition = null,
            ).also { mock ->
                every {
                    mock.title
                } returns "Piranesi"
            }
            val state = buildBookDetailUiState().withBook(book = book)

            // ----- Act -----
            val result = state.withBook(book = null)

            // ----- Assert -----
            result.book.shouldBeNull()
            result.topBar.title.shouldBeNull()
            result.topBar.surface shouldBe TopBarSurface.Opaque
        }
    }

    @Nested
    inner class ShelfStatus {
        private fun statusBook(
            status: BookStatus,
            userBook: UserBook?,
        ): Book = mockk<Book>().also { mock ->
            every {
                mock.status
            } returns status
            every {
                mock.userBook
            } returns userBook
        }

        @Test
        fun `is null when book is null`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState().copy(pendingShelfStatus = BookStatus.Reading)

            // ----- Act -----
            val result = state.shelfStatus

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `uses pending status when book is off the shelf and a status is pending`() {
            // ----- Arrange -----
            val book = statusBook(
                status = BookStatus.None,
                userBook = null,
            )
            val state = buildBookDetailUiState()
                .copy(
                    book = book,
                    pendingShelfStatus = BookStatus.Reading,
                )

            // ----- Act -----
            val result = state.shelfStatus

            // ----- Assert -----
            result shouldBe BookStatus.Reading
        }

        @Test
        fun `uses book status when book is off the shelf and nothing is pending`() {
            // ----- Arrange -----
            val book = statusBook(
                status = BookStatus.None,
                userBook = null,
            )
            val state = buildBookDetailUiState().copy(book = book)

            // ----- Act -----
            val result = state.shelfStatus

            // ----- Assert -----
            result shouldBe BookStatus.None
        }

        @Test
        fun `ignores a stale pending status when book is on the shelf`() {
            // ----- Arrange -----
            val book = statusBook(
                status = BookStatus.Read,
                userBook = stubUserBook(),
            )
            val state = buildBookDetailUiState()
                .copy(
                    book = book,
                    pendingShelfStatus = BookStatus.Reading,
                )

            // ----- Act -----
            val result = state.shelfStatus

            // ----- Assert -----
            result shouldBe BookStatus.Read
        }
    }

    @Nested
    inner class YoursLensBlocks {
        private fun bookWithStatus(status: BookStatus): Book = mockk<Book>().also { mock ->
            every {
                mock.status
            } returns status
        }

        private fun stateWithStatus(status: BookStatus): BookDetailUiState = buildBookDetailUiState()
            .copy(book = bookWithStatus(status))

        private val callout = CalloutUiModel(
            variant = CalloutVariant.Neutral(),
            eyebrow = "Eyebrow",
            body = "Body",
        )

        @Test
        fun `shows the in progress section while reading`() {
            // ----- Arrange -----
            val state = stateWithStatus(BookStatus.Reading)

            // ----- Act -----
            val result = state.showsInProgressSection

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `shows the in progress section while paused`() {
            // ----- Arrange -----
            val state = stateWithStatus(BookStatus.Paused)

            // ----- Act -----
            val result = state.showsInProgressSection

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `hides the in progress section for every other status`() {
            // ----- Arrange -----
            val others = BookStatus.entries - setOf(BookStatus.Reading, BookStatus.Paused)

            // ----- Act -----
            val results = others.map { stateWithStatus(it).showsInProgressSection }

            // ----- Assert -----
            results.none { it } shouldBe true
        }

        @Test
        fun `hides the in progress section when no book is loaded`() {
            // ----- Arrange -----
            val state = buildBookDetailUiState()

            // ----- Act -----
            val result = state.showsInProgressSection

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `keeps the verdict block only when the book is read`() {
            // ----- Arrange -----
            val verdict = VerdictBlockUiModel.Empty
            val read = stateWithStatus(BookStatus.Read).copy(verdictBlock = verdict)
            val paused = stateWithStatus(BookStatus.Paused).copy(verdictBlock = verdict)

            // ----- Act -----
            val readResult = read.yoursVerdictBlock
            val pausedResult = paused.yoursVerdictBlock

            // ----- Assert -----
            readResult shouldBe verdict
            pausedResult.shouldBeNull()
        }

        @Test
        fun `has a leading block when the in progress section shows`() {
            // ----- Arrange -----
            val state = stateWithStatus(BookStatus.Paused)

            // ----- Act -----
            val result = state.hasYoursLeadingBlock

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `has a leading block when only a status callout is present`() {
            // ----- Arrange -----
            val state = stateWithStatus(BookStatus.Read).copy(statusCallout = callout)

            // ----- Act -----
            val result = state.hasYoursLeadingBlock

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `has no leading block without an in progress section or a status callout`() {
            // ----- Arrange -----
            val state = stateWithStatus(BookStatus.Read)

            // ----- Act -----
            val result = state.hasYoursLeadingBlock

            // ----- Assert -----
            result shouldBe false
        }
    }
}
