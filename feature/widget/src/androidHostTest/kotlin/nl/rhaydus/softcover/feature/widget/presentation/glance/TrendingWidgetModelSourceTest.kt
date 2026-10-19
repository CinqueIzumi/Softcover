package nl.rhaydus.softcover.feature.widget.presentation.glance

import android.net.Uri
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.widget.presentation.model.TrendingWidgetUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingWidgetModelSourceTest {
    private val source = TrendingWidgetModelSource(
        context = mockk(relaxed = true),
        environment = mockk(relaxed = true),
        observeTrendingFeed = mockk(relaxed = true),
        observeTrendingFeedFetching = mockk(relaxed = true),
        getTrendingPosition = mockk(relaxed = true),
        observeTrendingChanges = mockk(relaxed = true),
        observeTrendingSettingsChanges = mockk(relaxed = true),
    )

    private fun model(content: TrendingWidgetUiModel.Content) = TrendingWidgetUiModel(
        themeConfiguration = mockk<ThemeConfiguration>(),
        content = content,
    )

    private fun item(cover: TrendingWidgetUiModel.Cover) = TrendingWidgetUiModel.Item(
        bookId = 1,
        rank = "1",
        title = "Title",
        author = "Author",
        cover = cover,
    )

    private fun ranked(vararg covers: TrendingWidgetUiModel.Cover) = TrendingWidgetUiModel.Content.Ranked(
        eyebrow = "TRENDING",
        items = persistentListOf(*covers.map { item(it) }.toTypedArray()),
        pageLabel = null,
        showsArrows = false,
    )

    @Nested
    inner class IsSettled {
        @Test
        fun `switching is not settled`() {
            // ----- Arrange -----
            val model = model(TrendingWidgetUiModel.Content.Switching(eyebrow = "TRENDING"))

            // ----- Act -----
            val result = source.isSettled(model)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `a ranked list with any pending cover is not settled`() {
            // ----- Arrange -----
            val model = model(
                ranked(
                    TrendingWidgetUiModel.Cover.Loaded(mockk<Uri>()),
                    TrendingWidgetUiModel.Cover.Pending,
                ),
            )

            // ----- Act -----
            val result = source.isSettled(model)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `a ranked list with loaded and missing covers is settled`() {
            // ----- Arrange -----
            val model = model(
                ranked(
                    TrendingWidgetUiModel.Cover.Loaded(mockk<Uri>()),
                    TrendingWidgetUiModel.Cover.Missing,
                ),
            )

            // ----- Act -----
            val result = source.isSettled(model)

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `a ranked list without items is settled`() {
            // ----- Arrange -----
            val model = model(ranked())

            // ----- Act -----
            val result = source.isSettled(model)

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `terminal states are settled`() {
            // ----- Arrange -----
            val models = listOf(
                model(TrendingWidgetUiModel.Content.SignedOut),
                model(TrendingWidgetUiModel.Content.Empty(
                    eyebrow = "TRENDING",
                    headline = "Nothing",
                ),),
                model(TrendingWidgetUiModel.Content.FetchFailed(eyebrow = "TRENDING")),
            )

            // ----- Act -----
            val results = models.map { source.isSettled(it) }

            // ----- Assert -----
            results shouldBe listOf(true, true, true)
        }
    }
}
