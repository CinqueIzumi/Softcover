package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.net.Uri
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.domain.model.QuoteFetchTracker
import nl.rhaydus.softcover.feature.widget.presentation.model.QuoteWidgetUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteWidgetModelSourceTest {
    private val source = QuoteWidgetModelSource(
        context = mockk(relaxed = true),
        environment = mockk(relaxed = true),
        observeAllQuotes = mockk(relaxed = true),
        getQuoteSelection = mockk(relaxed = true),
        observeQuoteSelectionChanges = mockk(relaxed = true),
        fetchTracker = QuoteFetchTracker(),
    )

    private fun model(content: QuoteWidgetUiModel.Content) = QuoteWidgetUiModel(
        themeConfiguration = mockk<ThemeConfiguration>(),
        content = content,
    )

    private fun shown(
        artwork: QuoteWidgetUiModel.Artwork?,
        imagesPending: Boolean,
    ) = QuoteWidgetUiModel.Content.Shown(
        quoteText = "Quote",
        byline = null,
        bookId = 1,
        eyebrow = "QUOTE OF THE DAY",
        hasArrows = false,
        artwork = artwork,
        imagesPending = imagesPending,
    )

    private fun artwork() = QuoteWidgetUiModel.Artwork(
        backdrop = null,
        veil = mockk<Uri>(),
        veilLift = mockk<Uri>(),
        washPrimary = mockk<Uri>(),
        washTertiary = mockk<Uri>(),
    )

    @Nested
    inner class IsSettled {
        @Test
        fun `a shown quote with its images pending is not settled even with artwork`() {
            // ----- Arrange -----
            val model = model(
                shown(
                    artwork = artwork(),
                    imagesPending = true,
                ),
            )

            // ----- Act -----
            val result = source.isSettled(model)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `a shown quote whose image work finished is settled even without artwork`() {
            // ----- Arrange -----
            val model = model(
                shown(
                    artwork = null,
                    imagesPending = false,
                ),
            )

            // ----- Act -----
            val result = source.isSettled(model)

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `terminal states are settled`() {
            // ----- Arrange -----
            val models = listOf(
                model(QuoteWidgetUiModel.Content.SignedOut),
                model(QuoteWidgetUiModel.Content.Empty),
                model(QuoteWidgetUiModel.Content.FetchFailed),
            )

            // ----- Act -----
            val results = models.map { source.isSettled(it) }

            // ----- Assert -----
            results shouldBe listOf(true, true, true)
        }
    }
}
