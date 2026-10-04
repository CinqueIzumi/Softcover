package nl.rhaydus.softcover.feature.reading.presentation.collector

import io.kotest.matchers.shouldBe
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
}
