package nl.rhaydus.softcover.feature.explore.data.mock

import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookSeries

internal object ExploreMockData {

    private fun mockEdition(
        id: Int,
        bookId: Int,
        title: String,
        author: String,
        releaseYear: Int,
    ): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        title = title,
        publisher = "",
        isbn10 = "",
        pages = 320,
        audioSeconds = null,
        url = "",
        localImagePath = null,
        releaseYear = releaseYear,
        authors = listOf(Author(id = id, name = author)),
        format = "Hardcover",
        bookId = bookId,
        owned = false,
    )

    private fun mockBook(
        id: Int,
        title: String,
        author: String,
        releaseYear: Int,
        rating: Double,
        usersCount: Int,
        ratingsCount: Int,
        series: BookSeries? = null,
        positionsInSeries: List<Double> = emptyList(),
    ): Book {
        val edition = mockEdition(
            id = id,
            bookId = id,
            title = title,
            author = author,
            releaseYear = releaseYear,
        )

        return Book(
            id = id,
            canonicalId = null,
            title = title,
            editions = listOf(edition),
            defaultEdition = edition,
            rating = rating,
            description = "",
            releaseYear = releaseYear,
            coverUrl = "",
            authors = listOf(Author(id = id, name = author)),
            usersCount = usersCount,
            ratingsCount = ratingsCount,
            bookSeries = series,
            positionsInSeries = positionsInSeries,
            isCompilation = false,
            userBook = null,
            userBookRead = null,
        )
    }

    val trending: List<Book> = listOf(
        mockBook(
            id = 9001,
            title = "The Familiar",
            author = "Leigh Bardugo",
            releaseYear = 2024,
            rating = 4.2,
            usersCount = 18420,
            ratingsCount = 7321,
        ),
        mockBook(
            id = 9002,
            title = "James",
            author = "Percival Everett",
            releaseYear = 2024,
            rating = 4.5,
            usersCount = 22041,
            ratingsCount = 9120,
        ),
        mockBook(
            id = 9003,
            title = "The Ministry of Time",
            author = "Kaliane Bradley",
            releaseYear = 2024,
            rating = 4.0,
            usersCount = 15730,
            ratingsCount = 6210,
        ),
        mockBook(
            id = 9004,
            title = "Funny Story",
            author = "Emily Henry",
            releaseYear = 2024,
            rating = 4.3,
            usersCount = 31022,
            ratingsCount = 12044,
        ),
        mockBook(
            id = 9005,
            title = "You Like It Darker",
            author = "Stephen King",
            releaseYear = 2024,
            rating = 4.1,
            usersCount = 24810,
            ratingsCount = 8421,
        ),
        mockBook(
            id = 9006,
            title = "Wandering Stars",
            author = "Tommy Orange",
            releaseYear = 2024,
            rating = 4.2,
            usersCount = 11023,
            ratingsCount = 4012,
        ),
    )

    val continueSeries: List<Book> = listOf(
        mockBook(
            id = 9101,
            title = "A Court of Wings and Ruin",
            author = "Sarah J. Maas",
            releaseYear = 2017,
            rating = 4.5,
            usersCount = 412000,
            ratingsCount = 198000,
            series = BookSeries(id = 9001, name = "A Court of Thorns and Roses", amountOfBooks = 5),
            positionsInSeries = listOf(3.0),
        ),
        mockBook(
            id = 9102,
            title = "The Wise Man's Fear",
            author = "Patrick Rothfuss",
            releaseYear = 2011,
            rating = 4.6,
            usersCount = 612000,
            ratingsCount = 332000,
            series = BookSeries(id = 9002, name = "The Kingkiller Chronicle", amountOfBooks = 3),
            positionsInSeries = listOf(2.0),
        ),
        mockBook(
            id = 9103,
            title = "Words of Radiance",
            author = "Brandon Sanderson",
            releaseYear = 2014,
            rating = 4.7,
            usersCount = 521000,
            ratingsCount = 287000,
            series = BookSeries(id = 9003, name = "The Stormlight Archive", amountOfBooks = 5),
            positionsInSeries = listOf(2.0),
        ),
        mockBook(
            id = 9104,
            title = "The Shadow Rising",
            author = "Robert Jordan",
            releaseYear = 1992,
            rating = 4.5,
            usersCount = 312000,
            ratingsCount = 168000,
            series = BookSeries(id = 9004, name = "The Wheel of Time", amountOfBooks = 14),
            positionsInSeries = listOf(4.0),
        ),
    )
}
