package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.net.Uri
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.presentation.model.BookArtworkUiModel
import nl.rhaydus.softcover.feature.widget.presentation.model.CurrentlyReadingWidgetUiModel
import nl.rhaydus.softcover.feature.widget.presentation.render.WavyProgressUris
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CurrentlyReadingWidgetModelSourceTest {
    private val source = CurrentlyReadingWidgetModelSource(
        context = mockk(relaxed = true),
        environment = mockk(relaxed = true),
        getCurrentlyReadingUserBooks = mockk(relaxed = true),
        getSelectedBookId = mockk(relaxed = true),
        observeSelectedBookChanges = mockk(relaxed = true),
    )

    private fun model(content: CurrentlyReadingWidgetUiModel.Content) = CurrentlyReadingWidgetUiModel(
        themeConfiguration = mockk<ThemeConfiguration>(),
        content = content,
    )

    private fun reading(
        progress: Map<Int, WavyProgressUris>,
        artwork: BookArtworkUiModel?,
        imagesPending: Boolean,
    ) = CurrentlyReadingWidgetUiModel.Content.Reading(
        title = "Title",
        author = "Author",
        bookId = 1,
        progressLabel = "10%",
        cover = null,
        progress = progress,
        eyebrow = "READING",
        canCycle = false,
        artwork = artwork,
        imagesPending = imagesPending,
    )

    private fun artwork() = BookArtworkUiModel(
        backdrop = null,
        veil = mockk<Uri>(),
        veilLift = mockk<Uri>(),
        washPrimary = mockk<Uri>(),
        washTertiary = mockk<Uri>(),
    )

    @Nested
    inner class IsSettled {
        @Test
        fun `a reading stage with its images pending is not settled even with artwork and progress`() {
            // ----- Arrange -----
            val model = model(
                reading(
                    progress = mapOf(120 to mockk<WavyProgressUris>()),
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
        fun `a reading stage whose image work finished is settled even without artwork or progress`() {
            // ----- Arrange -----
            val model = model(
                reading(
                    progress = emptyMap(),
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
        fun `signed out and empty are settled`() {
            // ----- Arrange -----
            val models = listOf(
                model(CurrentlyReadingWidgetUiModel.Content.SignedOut),
                model(CurrentlyReadingWidgetUiModel.Content.Empty),
            )

            // ----- Act -----
            val results = models.map { source.isSettled(it) }

            // ----- Assert -----
            results shouldBe listOf(true, true)
        }
    }
}
