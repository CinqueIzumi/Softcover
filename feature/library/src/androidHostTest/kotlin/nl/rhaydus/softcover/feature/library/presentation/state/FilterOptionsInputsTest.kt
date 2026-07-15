package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.UserBookStatus

class FilterOptionsInputsTest {
    // region Fixtures
    private val readTabId = LibraryTab.Status.of(UserBookStatus.READ).id

    private fun buildEdition(
        id: Int,
        bookId: Int = id,
        format: String = "",
        owned: Boolean = false,
    ) = BookEdition(
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

    private fun buildBook(
        id: Int,
        finishedYear: Int? = null,
    ) = Book(
        id = id,
        title = "Book $id",
        editions = listOf(buildEdition(
            id = id,
            bookId = id,
        ),),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = listOf(Author(
            id = id,
            name = "Author $id",
        ),),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        tags = emptyList(),
        userBook = null,
        userBookRead = finishedYear?.let {
            UserBookRead(
                id = id,
                currentPage = null,
                currentSeconds = null,
                progress = 1f,
                startedAt = null,
                finishedAt = "$it-06-15",
            )
        },
    )
    // endregion
    @Nested
    inner class Compute {
        @Test
        fun `readYears is populated for the Read tab id`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                finishedYear = 2021,
            )
            val inputs = FilterOptionsInputs(
                booksByTab = mapOf(readTabId to listOf(book)),
                editionsByTab = emptyMap(),
                bookByBookId = emptyMap(),
            )

            // ----- Act -----
            val result = inputs.compute()

            // ----- Assert -----
            result.getValue(readTabId).readYears shouldBe listOf(2021)
        }

        @Test
        fun `readYears stays empty for a non-Read tab id even when books have a finishedYear`() {
            // ----- Arrange -----
            val book = buildBook(
                id = 1,
                finishedYear = 2021,
            )
            val inputs = FilterOptionsInputs(
                booksByTab = mapOf("all" to listOf(book)),
                editionsByTab = emptyMap(),
                bookByBookId = emptyMap(),
            )

            // ----- Act -----
            val result = inputs.compute()

            // ----- Assert -----
            result.getValue("all").readYears shouldBe emptyList()
        }

        @Test
        fun `readYears is empty for edition-side tabs even when the tab id is the Read tab id`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 1,
                bookId = 1,
            )
            val book = buildBook(
                id = 1,
                finishedYear = 2021,
            )
            val inputs = FilterOptionsInputs(
                booksByTab = emptyMap(),
                editionsByTab = mapOf(readTabId to listOf(edition)),
                bookByBookId = mapOf(1 to book),
            )

            // ----- Act -----
            val result = inputs.compute()

            // ----- Assert -----
            result.getValue(readTabId).readYears shouldBe emptyList()
        }

        @Test
        fun `merges book-side and edition-side results keyed by tab id`() {
            // ----- Arrange -----
            val book = buildBook(id = 1)
            val editionBook = buildBook(id = 2)
            val edition = buildEdition(
                id = 2,
                bookId = 2,
            )
            val inputs = FilterOptionsInputs(
                booksByTab = mapOf("all" to listOf(book)),
                editionsByTab = mapOf("list-1" to listOf(edition)),
                bookByBookId = mapOf(2 to editionBook),
            )

            // ----- Act -----
            val result = inputs.compute()

            // ----- Assert -----
            result.keys shouldBe setOf("all", "list-1")
        }
    }
}
