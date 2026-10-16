package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteCardMapperTest {
    private val savedOn: Instant = Instant.parse("2020-03-05T12:00:00Z")
    private val today: LocalDate = LocalDate(
        year = 2026,
        month = 10,
        day = 16,
    )

    private fun quote(
        position: QuotePosition? = null,
        privacy: PrivacySetting = PrivacySetting.PRIVATE,
        actionAt: Instant = savedOn,
        localId: Long = 4,
    ): Quote = Quote(
        localId = localId,
        remoteId = null,
        bookId = 7,
        editionId = null,
        text = "A line worth keeping",
        privacy = privacy,
        actionAt = actionAt,
        position = position,
        isPending = false,
    )

    @Nested
    inner class ToQuoteCardUiModel {
        @Test
        fun `labels a page position with its page number`() {
            // ----- Arrange -----
            val quote = quote(
                position = QuotePosition.pages(
                    page = 214,
                    totalPages = 437,
                ),
            )

            // ----- Act -----
            val card = quote.toQuoteCardUiModel(
                timeZone = TimeZone.UTC,
                today = today,
            )

            // ----- Assert -----
            card.byline shouldBe "p. 214 · 5 Mar 2020 · Private"
        }

        @Test
        fun `rounds a percent position to a whole percent`() {
            // ----- Arrange -----
            val quote = quote(position = QuotePosition.Percent(percent = 52.6))

            // ----- Act -----
            val card = quote.toQuoteCardUiModel(
                timeZone = TimeZone.UTC,
                today = today,
            )

            // ----- Assert -----
            card.byline shouldBe "53% · 5 Mar 2020 · Private"
        }

        @Test
        fun `labels a time position as a clock`() {
            // ----- Arrange -----
            val quote = quote(
                position = QuotePosition.seconds(
                    seconds = 3723,
                    totalSeconds = 36_000,
                ),
            )

            // ----- Act -----
            val card = quote.toQuoteCardUiModel(
                timeZone = TimeZone.UTC,
                today = today,
            )

            // ----- Assert -----
            card.byline shouldBe "1:02:03 · 5 Mar 2020 · Private"
        }

        @Test
        fun `starts with the date when the quote has no position`() {
            // ----- Arrange -----
            val quote = quote(
                position = null,
                privacy = PrivacySetting.FOLLOWERS,
            )

            // ----- Act -----
            val card = quote.toQuoteCardUiModel(
                timeZone = TimeZone.UTC,
                today = today,
            )

            // ----- Assert -----
            card.byline shouldBe "5 Mar 2020 · Followers"
        }

        @Test
        fun `ends with the public label for a public quote`() {
            // ----- Arrange -----
            val quote = quote(privacy = PrivacySetting.PUBLIC)

            // ----- Act -----
            val card = quote.toQuoteCardUiModel(
                timeZone = TimeZone.UTC,
                today = today,
            )

            // ----- Assert -----
            card.byline shouldBe "5 Mar 2020 · Public"
        }

        @Test
        fun `leaves the year off a date in the current year`() {
            // ----- Arrange -----
            val quote = quote(actionAt = Instant.parse("2026-03-05T12:00:00Z"))

            // ----- Act -----
            val card = quote.toQuoteCardUiModel(
                timeZone = TimeZone.UTC,
                today = today,
            )

            // ----- Assert -----
            card.byline shouldNotContain "2026"
        }

        @Test
        fun `puts the same instant on the next day in a far-east timezone`() {
            // ----- Arrange -----
            val quote = quote(actionAt = Instant.parse("2020-03-05T20:00:00Z"))

            // ----- Act -----
            val utcCard = quote.toQuoteCardUiModel(
                timeZone = TimeZone.UTC,
                today = today,
            )
            val farEastCard = quote.toQuoteCardUiModel(
                timeZone = TimeZone.of("Pacific/Kiritimati"),
                today = today,
            )

            // ----- Assert -----
            utcCard.byline shouldBe "5 Mar 2020 · Private"
            farEastCard.byline shouldBe "6 Mar 2020 · Private"
        }
    }

    @Nested
    inner class ToQuotesSection {
        @Test
        fun `shows the empty message while there are no quotes`() {
            // ----- Arrange -----
            val quotes = emptyList<Quote>()

            // ----- Act -----
            val section = quotes.toQuotesSection(
                timeZone = TimeZone.UTC,
                today = today,
            )

            // ----- Assert -----
            section.emptyMessage.shouldNotBeNull()
            section.quotes.size shouldBe 0
        }

        @Test
        fun `drops the empty message once there is a quote`() {
            // ----- Arrange -----
            val first = quote(localId = 1)
            val second = quote(localId = 2)
            val quotes = listOf(first, second)

            // ----- Act -----
            val section = quotes.toQuotesSection(
                timeZone = TimeZone.UTC,
                today = today,
            )

            // ----- Assert -----
            section.emptyMessage.shouldBeNull()
            section.quotes.map { it.key } shouldBe listOf(1L, 2L)
        }
    }
}
