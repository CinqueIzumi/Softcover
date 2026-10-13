package nl.rhaydus.softcover.feature.book_detail.presentation.component

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.editionCountHeaderFor
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toEditionCard
import nl.rhaydus.softcover.feature.book_detail.presentation.state.EditionSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.state.editionSheetScaffoldFor

/** Five editions across the formats, one selected and one tracked, mapped the way the collectors do. */
internal fun editionSheetPreviewModel(): EditionSheetUiModel {
    val baseEdition = PreviewData.baseEdition.copy(title = "King Sorrow")

    val editions = listOf(
        baseEdition.copy(
            id = 10,
            format = "Audiobook",
            readingFormat = ReadingFormat.Audio,
            publisher = "Harper Audio",
            releaseYear = 2025,
            pages = null,
            audioSeconds = 87120,
            isbn13 = "9780063339214",
            isbn10 = null,
            owned = true,
        ),
        baseEdition.copy(
            id = 20,
            format = "Hardcover",
            readingFormat = ReadingFormat.Physical,
            publisher = "William Morrow",
            releaseYear = 2025,
            pages = 896,
            audioSeconds = null,
            isbn13 = "9780062200600",
            isbn10 = null,
            owned = false,
        ),
        baseEdition.copy(
            id = 30,
            format = "Trade paperback",
            readingFormat = ReadingFormat.Physical,
            publisher = "William Morrow",
            releaseYear = 2026,
            pages = 912,
            audioSeconds = null,
            isbn13 = "9780063345123",
            isbn10 = null,
            owned = false,
        ),
        baseEdition.copy(
            id = 40,
            format = "E-book",
            readingFormat = ReadingFormat.Ebook,
            publisher = "William Morrow",
            releaseYear = 2025,
            pages = 900,
            audioSeconds = null,
            isbn13 = "9780062200624",
            isbn10 = null,
            owned = false,
        ),
        baseEdition.copy(
            id = 50,
            format = "Hardcover · UK",
            readingFormat = ReadingFormat.Physical,
            publisher = "Gollancz",
            releaseYear = 2025,
            pages = 880,
            audioSeconds = null,
            isbn13 = "9781473230129",
            isbn10 = null,
            owned = false,
        ),
    )

    val cards = editions.mapIndexed { index, edition ->
        edition.toEditionCard(
            fallbackEdition = null,
            selected = index == 2,
            tracked = index == 1,
        )
    }.toImmutableList()

    return EditionSheetUiModel(
        scaffold = editionSheetScaffoldFor(
            bookTitle = "King Sorrow",
            currentEdition = editions[1],
            selectedEdition = editions[2],
            cover = null,
            isLoading = false,
        ),
        cards = cards,
        countHeader = editionCountHeaderFor(count = cards.size),
        searchQuery = "",
        searchPlaceholder = "Search by ISBN or publisher",
        emptyMessage = "No editions match your search",
    )
}
