package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.net.Uri
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.presentation.model.BookArtworkUiModel
import nl.rhaydus.softcover.feature.widget.presentation.model.RandomPickWidgetUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RandomPickWidgetModelSourceTest {
    private val source = RandomPickWidgetModelSource(
        context = mockk(relaxed = true),
        environment = mockk(relaxed = true),
        getWantToReadUserBooks = mockk(relaxed = true),
        getAllUserLists = mockk(relaxed = true),
        resolveRandomPick = mockk(relaxed = true),
        observeRandomPickChanges = mockk(relaxed = true),
        observeRandomPickSettingsChanges = mockk(relaxed = true),
    )

    private fun model(content: RandomPickWidgetUiModel.Content) = RandomPickWidgetUiModel(
        themeConfiguration = mockk<ThemeConfiguration>(),
        content = content,
    )

    private fun picked(
        cover: RandomPickWidgetUiModel.Cover,
        artwork: BookArtworkUiModel?,
        imagesPending: Boolean,
    ) = RandomPickWidgetUiModel.Content.Picked(
        eyebrow = "RANDOM PICK",
        bookId = 1,
        title = "Title",
        author = "Author",
        cover = cover,
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
        fun `switching is not settled`() {
            // ----- Arrange -----
            val model = model(RandomPickWidgetUiModel.Content.Switching(eyebrow = "RANDOM PICK"))

            // ----- Act -----
            val result = source.isSettled(model)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `a pick with its images pending is not settled whatever its cover and artwork`() {
            // ----- Arrange -----
            val pendingCover = model(
                picked(
                    cover = RandomPickWidgetUiModel.Cover.Pending,
                    artwork = null,
                    imagesPending = true,
                ),
            )
            val loadedCover = model(
                picked(
                    cover = RandomPickWidgetUiModel.Cover.Loaded(mockk<Uri>()),
                    artwork = artwork(),
                    imagesPending = true,
                ),
            )

            // ----- Act -----
            val results = listOf(pendingCover, loadedCover).map { source.isSettled(it) }

            // ----- Assert -----
            results shouldBe listOf(false, false)
        }

        @Test
        fun `a pick whose image work finished is settled whatever its cover and artwork`() {
            // ----- Arrange -----
            val loaded = model(
                picked(
                    cover = RandomPickWidgetUiModel.Cover.Loaded(mockk<Uri>()),
                    artwork = artwork(),
                    imagesPending = false,
                ),
            )
            val missing = model(
                picked(
                    cover = RandomPickWidgetUiModel.Cover.Missing,
                    artwork = null,
                    imagesPending = false,
                ),
            )

            // ----- Act -----
            val results = listOf(loaded, missing).map { source.isSettled(it) }

            // ----- Assert -----
            results shouldBe listOf(true, true)
        }

        @Test
        fun `terminal states are settled`() {
            // ----- Arrange -----
            val models = listOf(
                model(RandomPickWidgetUiModel.Content.SignedOut),
                model(RandomPickWidgetUiModel.Content.SourceUnavailable),
                model(RandomPickWidgetUiModel.Content.EmptyPool(eyebrow = "RANDOM PICK")),
            )

            // ----- Act -----
            val results = models.map { source.isSettled(it) }

            // ----- Assert -----
            results shouldBe listOf(true, true, true)
        }
    }
}
