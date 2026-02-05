package nl.rhaydus.softcover.core

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus

object PreviewData {
    val baseAuthor = Author(
        name = "Caitlin Starling",
        id = 1,
    )

    val baseEdition = BookEdition(
        id = 1,
        title = "Last to leave the Room",
        publisher = "",
        isbn10 = "",
        pages = 100,
        url = "",
        releaseYear = 2023,
        authors = listOf(baseAuthor)
    )

    val baseBook = Book(
        id = 1,
        title = "Last to leave the Room",
        rating = 3.7,
        releaseYear = 2023,
        description = "Last to Leave the Room is a new novel of genre-busting speculative horror from Caitlin Starling, the acclaimed author of The Death of Jane Lawrence. The city of San Siroco is sinking. The basement of Dr. Tamsin Rivers, the arrogant, selfish head of the research team assigned to find the source of the subsidence, is sinking faster. As Tamsin grows obsessed with the distorting dimensions of the room at the bottom of the stairs, she finds a door that didn’t exist before - and one night, it opens to reveal an exact physical copy of her. This doppelgänger is sweet and biddable where Tamsin is calculating and cruel. It appears fully, terribly human, passing every test Tamsin can devise. But the longer the double exists, the more Tamsin begins to forget pieces of her life, to lose track of time, to grow terrified of the outside world. As her employer grows increasingly suspicious, Tamsin must try to hold herself together long enough to figure out what her double wants from her, and just where the mysterious door leads...",
        editions = listOf(
            baseEdition,
        ),
        coverUrl = "",
        authors = listOf(baseAuthor),
        defaultEdition = baseEdition,
        userBook = UserBook(
            id = 1,
            status = BookStatus.None,
            dateAdded = "",
            privacySettingId = 1,
            reviewHasSpoilers = false,
            editionId = null,
            lastReadDate = null,
            rating = null,
            referrerUserId = null,
            reviewedAt = null,
            updatedAt = null,
        ),
        userBookRead = UserBookRead(
            id = 1,
            currentPage = null,
            progress = null,
            startedAt = null,
            finishedAt = null,
        ),
    )
}