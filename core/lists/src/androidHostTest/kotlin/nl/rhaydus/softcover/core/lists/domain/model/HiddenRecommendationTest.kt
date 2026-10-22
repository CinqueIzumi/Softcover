package nl.rhaydus.softcover.core.lists.domain.model

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HiddenRecommendationTest {
    private fun book(authors: List<Author>): Book = PreviewData.baseBook.copy(
        id = 3,
        title = "The Tombs of Atuan",
        coverUrl = "https://covers.test/3.jpg",
        editions = emptyList(),
        defaultEdition = null,
        userBook = null,
        authors = authors,
    )

    @Nested
    inner class ToHiddenRecommendation {
        @Test
        fun `maps the id, title, cover and joined author names`() {
            // ----- Arrange -----
            val book = book(
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Ursula K. Le Guin",
                    ),
                    Author(
                        id = 2,
                        name = "Someone Else",
                    ),
                ),
            )

            // ----- Act -----
            val hidden = book.toHiddenRecommendation()

            // ----- Assert -----
            hidden shouldBe HiddenRecommendation(
                bookId = 3,
                title = "The Tombs of Atuan",
                coverUrl = "https://covers.test/3.jpg",
                authorText = "Ursula K. Le Guin, Someone Else",
            )
        }

        @Test
        fun `stores a blank author as null`() {
            // ----- Arrange -----
            val book = book(authors = emptyList())

            // ----- Act -----
            val hidden = book.toHiddenRecommendation()

            // ----- Assert -----
            hidden.authorText.shouldBeNull()
        }
    }
}
