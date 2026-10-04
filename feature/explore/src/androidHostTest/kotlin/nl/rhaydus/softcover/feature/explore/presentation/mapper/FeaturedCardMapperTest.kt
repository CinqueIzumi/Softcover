package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FeaturedCardMapperTest {
    private val cover = mockk<CoverUiModel>()

    private val header = SectionHeaderUiModel.Inline(eyebrow = "Most anticipated")

    private val badge = BadgeUiModel(
        label = "Arriving 12 Mar",
        tone = BadgeTone.Primary,
    )

    private val savedBook = UserBook(
        id = 1,
        status = BookStatus.WantToRead,
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

    private fun book(
        title: String = "Piranesi",
        headline: String = "",
        authors: List<Author> = emptyList(),
        usersCount: Int = 0,
        userBook: UserBook? = null,
    ): Book = Book(
        id = 7,
        title = title,
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        headline = headline,
        description = "",
        releaseYear = 2030,
        coverUrl = "",
        authors = authors,
        usersCount = usersCount,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = userBook,
        userBookRead = null,
    )

    @Nested
    inner class ToFeaturedCard {
        @Test
        fun `passes header, badge and cover through and titles the card with the book`() {
            // ----- Arrange -----
            val input = book(title = "Piranesi")

            // ----- Act -----
            val card = input.toFeaturedCard(
                header = header,
                badge = badge,
                cover = cover,
            )

            // ----- Assert -----
            card.header shouldBe header
            card.badge shouldBe badge
            card.cover shouldBe cover
            card.title shouldBe "Piranesi"
            card.isLoading shouldBe false
        }

        @Test
        fun `upper-cases the byline with the authors joined`() {
            // ----- Arrange -----
            val input = book(
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Susanna Clarke",
                    ),
                    Author(
                        id = 2,
                        name = "Ann Leckie",
                    ),
                ),
            )

            // ----- Act -----
            val card = input.toFeaturedCard(
                header = header,
                badge = null,
                cover = null,
            )

            // ----- Assert -----
            card.byline shouldBe "BY SUSANNA CLARKE, ANN LECKIE"
        }

        @Test
        fun `groups the readers count into the waiting label`() {
            // ----- Arrange -----
            val input = book(usersCount = 12_345)

            // ----- Act -----
            val card = input.toFeaturedCard(
                header = header,
                badge = null,
                cover = null,
            )

            // ----- Assert -----
            card.readersLabel shouldBe "12,345 readers waiting"
        }

        @Test
        fun `uses the headline as the description`() {
            // ----- Arrange -----
            val input = book(headline = "A house of endless halls")

            // ----- Act -----
            val card = input.toFeaturedCard(
                header = header,
                badge = null,
                cover = null,
            )

            // ----- Assert -----
            card.description shouldBe "A house of endless halls"
        }

        @Test
        fun `hides the description when the headline is blank`() {
            // ----- Arrange -----
            val input = book(headline = "   ")

            // ----- Act -----
            val card = input.toFeaturedCard(
                header = header,
                badge = null,
                cover = null,
            )

            // ----- Assert -----
            card.description.shouldBeNull()
        }

        @Test
        fun `is in the library only when the book has a user book`() {
            // ----- Arrange -----
            val saved = book(userBook = savedBook)
            val unsaved = book(userBook = null)

            // ----- Act -----
            val savedCard = saved.toFeaturedCard(
                header = header,
                badge = null,
                cover = null,
            )
            val unsavedCard = unsaved.toFeaturedCard(
                header = header,
                badge = null,
                cover = null,
            )

            // ----- Assert -----
            savedCard.inLibrary shouldBe true
            unsavedCard.inLibrary shouldBe false
        }
    }
}
