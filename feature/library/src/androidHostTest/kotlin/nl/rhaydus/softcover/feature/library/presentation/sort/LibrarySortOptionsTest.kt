package nl.rhaydus.softcover.feature.library.presentation.sort

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

class LibrarySortOptionsTest {
    private fun buildUserBook(status: BookStatus) = UserBook(
        id = 1,
        status = status,
        dateAdded = "2024-01-01",
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
    )

    private fun buildBook(
        id: Int = 1,
        status: BookStatus = BookStatus.WantToRead,
    ) = Book(
        id = id,
        canonicalId = null,
        title = "A Book",
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        releaseDate = null,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = buildUserBook(status = status),
        userBookRead = null,
    )

    private fun buildDeadline(bookId: Int) = BookDeadline(
        bookId = bookId,
        deadlineDate = LocalDate(
            2026,
            1,
            1,
        ),
        setAt = LocalDate(
            2025,
            12,
            1,
        ),
        initialPerDay = 10f,
        unit = DeadlineUnit.PAGES,
    )

    private fun buildBookList(
        id: Int,
        ranked: Boolean,
    ) = BookList(
        id = id,
        name = "List $id",
        slug = "list-$id",
        ranked = ranked,
        books = emptyList(),
    )

    @Nested
    inner class CustomListTabs {
        @Test
        fun `non-ranked list offers the edition-sortable modes without ORDER or MANUAL`() {
            // ----- Arrange -----
            val tab = LibraryTab.CustomList(
                listId = 1,
                listName = "TBR",
            )
            val state = LibraryUiState(
                customLists = listOf(buildBookList(
                    id = 1,
                    ranked = false,
                ),),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result shouldBe listOf(
                LibrarySortMode.DATE_ADDED,
                LibrarySortMode.TITLE,
                LibrarySortMode.AUTHOR,
                LibrarySortMode.PAGE_COUNT,
                LibrarySortMode.RELEASE_DATE,
            )
        }

        @Test
        fun `ranked list prepends ORDER to the edition-sortable modes`() {
            // ----- Arrange -----
            val tab = LibraryTab.CustomList(
                listId = 1,
                listName = "Favorites",
            )
            val state = LibraryUiState(
                customLists = listOf(buildBookList(
                    id = 1,
                    ranked = true,
                ),),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result shouldBe listOf(
                LibrarySortMode.ORDER,
                LibrarySortMode.DATE_ADDED,
                LibrarySortMode.TITLE,
                LibrarySortMode.AUTHOR,
                LibrarySortMode.PAGE_COUNT,
                LibrarySortMode.RELEASE_DATE,
            )
        }

        @Test
        fun `list absent from customLists falls back to the non-ranked modes`() {
            // ----- Arrange -----
            val tab = LibraryTab.CustomList(
                listId = 99,
                listName = "Unknown",
            )
            val state = LibraryUiState(customLists = emptyList())

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result shouldBe listOf(
                LibrarySortMode.DATE_ADDED,
                LibrarySortMode.TITLE,
                LibrarySortMode.AUTHOR,
                LibrarySortMode.PAGE_COUNT,
                LibrarySortMode.RELEASE_DATE,
            )
        }

        @Test
        fun `custom list tabs never offer content-gated modes even with matching books`() {
            // ----- Arrange -----
            val tab = LibraryTab.CustomList(
                listId = 1,
                listName = "TBR",
            )
            val state = LibraryUiState(
                customLists = listOf(buildBookList(
                    id = 1,
                    ranked = false,
                ),),
                booksByTab = mapOf(tab.id to listOf(buildBook(
                    id = 1,
                    status = BookStatus.Reading,
                ),),),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.PROGRESS) shouldBe false
        }
    }

    @Nested
    inner class AllTab {
        @Test
        fun `excludes MANUAL and ORDER regardless of content`() {
            // ----- Arrange -----
            val state = LibraryUiState()

            // ----- Act -----
            val result = librarySortOptions(
                tab = LibraryTab.All,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.MANUAL) shouldBe false
            result.contains(LibrarySortMode.ORDER) shouldBe false
        }

        @Test
        fun `with no books excludes PROGRESS, DEADLINE_URGENCY, and DATE_FINISHED`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = mapOf(LibraryTab.All.id to emptyList()),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = LibraryTab.All,
                state = state,
            )

            // ----- Assert -----
            result shouldBe listOf(
                LibrarySortMode.DATE_ADDED,
                LibrarySortMode.TITLE,
                LibrarySortMode.AUTHOR,
                LibrarySortMode.RATING,
                LibrarySortMode.PAGE_COUNT,
                LibrarySortMode.RELEASE_DATE,
            )
        }

        @Test
        fun `with an in-progress book includes PROGRESS`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = mapOf(
                    LibraryTab.All.id to listOf(buildBook(
                        id = 1,
                        status = BookStatus.Reading,
                    ),),
                ),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = LibraryTab.All,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.PROGRESS) shouldBe true
        }

        @Test
        fun `with a finished book includes DATE_FINISHED`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = mapOf(
                    LibraryTab.All.id to listOf(buildBook(
                        id = 1,
                        status = BookStatus.Read,
                    ),),
                ),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = LibraryTab.All,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.DATE_FINISHED) shouldBe true
        }

        @Test
        fun `with a tracked deadline includes DEADLINE_URGENCY`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = mapOf(
                    LibraryTab.All.id to listOf(buildBook(
                        id = 1,
                        status = BookStatus.WantToRead,
                    ),),
                ),
                deadlines = mapOf(1 to buildDeadline(bookId = 1)),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = LibraryTab.All,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.DEADLINE_URGENCY) shouldBe true
        }

        @Test
        fun `with in-progress, finished, and deadline books includes all three content-gated modes but never MANUAL or ORDER`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = mapOf(
                    LibraryTab.All.id to listOf(
                        buildBook(
                            id = 1,
                            status = BookStatus.Reading,
                        ),
                        buildBook(
                            id = 2,
                            status = BookStatus.Read,
                        ),
                        buildBook(
                            id = 3,
                            status = BookStatus.WantToRead,
                        ),
                    ),
                ),
                deadlines = mapOf(3 to buildDeadline(bookId = 3)),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = LibraryTab.All,
                state = state,
            )

            // ----- Assert -----
            result shouldBe listOf(
                LibrarySortMode.DATE_ADDED,
                LibrarySortMode.DATE_FINISHED,
                LibrarySortMode.TITLE,
                LibrarySortMode.AUTHOR,
                LibrarySortMode.RATING,
                LibrarySortMode.PROGRESS,
                LibrarySortMode.DEADLINE_URGENCY,
                LibrarySortMode.PAGE_COUNT,
                LibrarySortMode.RELEASE_DATE,
            )
        }
    }

    @Nested
    inner class DidNotFinishTab {
        private val tab = LibraryTab.Status.of(UserBookStatus.DID_NOT_FINISH)

        @Test
        fun `excludes MANUAL and ORDER`() {
            // ----- Arrange -----
            val state = LibraryUiState()

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.MANUAL) shouldBe false
            result.contains(LibrarySortMode.ORDER) shouldBe false
        }

        @Test
        fun `with no books excludes the content-gated modes`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = mapOf(tab.id to emptyList()),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result shouldBe listOf(
                LibrarySortMode.DATE_ADDED,
                LibrarySortMode.TITLE,
                LibrarySortMode.AUTHOR,
                LibrarySortMode.RATING,
                LibrarySortMode.PAGE_COUNT,
                LibrarySortMode.RELEASE_DATE,
            )
        }

        @Test
        fun `content gating still applies off the tab's raw book list`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = mapOf(
                    tab.id to listOf(buildBook(
                        id = 1,
                        status = BookStatus.Read,
                    ),),
                ),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.DATE_FINISHED) shouldBe true
        }
    }

    @Nested
    inner class OrdinaryStatusTab {
        private val tab = LibraryTab.Status.of(UserBookStatus.CURRENTLY_READING)

        @Test
        fun `includes MANUAL but excludes ORDER`() {
            // ----- Arrange -----
            val state = LibraryUiState()

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.MANUAL) shouldBe true
            result.contains(LibrarySortMode.ORDER) shouldBe false
        }

        @Test
        fun `with no books excludes PROGRESS, DEADLINE_URGENCY, and DATE_FINISHED`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = mapOf(tab.id to emptyList()),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result shouldBe listOf(
                LibrarySortMode.DATE_ADDED,
                LibrarySortMode.TITLE,
                LibrarySortMode.AUTHOR,
                LibrarySortMode.RATING,
                LibrarySortMode.PAGE_COUNT,
                LibrarySortMode.RELEASE_DATE,
                LibrarySortMode.MANUAL,
            )
        }

        @Test
        fun `with an in-progress book includes PROGRESS alongside MANUAL`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = mapOf(
                    tab.id to listOf(buildBook(
                        id = 1,
                        status = BookStatus.Reading,
                    ),),
                ),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.PROGRESS) shouldBe true
            result.contains(LibrarySortMode.MANUAL) shouldBe true
        }

        @Test
        fun `with a tracked deadline includes DEADLINE_URGENCY`() {
            // ----- Arrange -----
            val state = LibraryUiState(
                booksByTab = mapOf(
                    tab.id to listOf(buildBook(
                        id = 5,
                        status = BookStatus.WantToRead,
                    ),),
                ),
                deadlines = mapOf(5 to buildDeadline(bookId = 5)),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.DEADLINE_URGENCY) shouldBe true
        }

        @Test
        fun `book on a different tab's list does not trigger content gating for this tab`() {
            // ----- Arrange -----
            val otherTab = LibraryTab.Status.of(UserBookStatus.READ)
            val state = LibraryUiState(
                booksByTab = mapOf(
                    tab.id to emptyList(),
                    otherTab.id to listOf(buildBook(
                        id = 1,
                        status = BookStatus.Reading,
                    ),),
                ),
            )

            // ----- Act -----
            val result = librarySortOptions(
                tab = tab,
                state = state,
            )

            // ----- Assert -----
            result.contains(LibrarySortMode.PROGRESS) shouldBe false
        }
    }
}
