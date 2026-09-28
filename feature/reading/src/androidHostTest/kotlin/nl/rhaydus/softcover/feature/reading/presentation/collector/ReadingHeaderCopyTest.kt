package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingHeaderCopyTest {
    @Nested
    inner class MastheadEyebrowFor {
        @Test
        fun `reads Catching up on your reading while loading`() {
            // ----- Act -----
            val eyebrow = mastheadEyebrowFor(isLoading = true)

            // ----- Assert -----
            eyebrow shouldBe "Catching up on your reading…"
        }

        @Test
        fun `reads Now reading when not loading`() {
            // ----- Act -----
            val eyebrow = mastheadEyebrowFor(isLoading = false)

            // ----- Assert -----
            eyebrow shouldBe "Now reading"
        }
    }

    @Nested
    inner class MastheadRefreshFor {
        @Test
        fun `reads Refreshing and is disabled while loading`() {
            // ----- Act -----
            val refresh = mastheadRefreshFor(isLoading = true)

            // ----- Assert -----
            refresh shouldBe MastheadRefreshUiModel(
                label = "Refreshing…",
                enabled = false,
            )
        }

        @Test
        fun `reads Refresh and is enabled when not loading`() {
            // ----- Act -----
            val refresh = mastheadRefreshFor(isLoading = false)

            // ----- Assert -----
            refresh shouldBe MastheadRefreshUiModel(
                label = "Refresh",
                enabled = true,
            )
        }
    }
}
