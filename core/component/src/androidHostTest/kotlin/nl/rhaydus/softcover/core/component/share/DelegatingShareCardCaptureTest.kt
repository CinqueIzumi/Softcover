package nl.rhaydus.softcover.core.component.share

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.designsystem.share.SaveOutcome as FoundationSaveOutcome
import nl.rhaydus.designsystem.share.ShareCardCapture as FoundationShareCardCapture
import nl.rhaydus.designsystem.share.ShareOutcome as FoundationShareOutcome

class DelegatingShareCardCaptureTest {
    private val foundation = mockk<FoundationShareCardCapture>()

    private val capture = DelegatingShareCardCapture(foundation = foundation)

    @Nested
    inner class SaveToGallery {
        @Test
        fun `maps Saved to Saved carrying identifier and display path`() = runTest {
            // ----- Arrange -----
            coEvery {
                foundation.saveToGallery(any())
            } returns FoundationSaveOutcome.Saved(
                identifier = "content://1",
                displayPath = "Pictures/Softcover",
            )

            // ----- Act -----
            val outcome = capture.saveToGallery(displayName = "card")

            // ----- Assert -----
            outcome shouldBe SaveOutcome.Saved(
                identifier = "content://1",
                displayPath = "Pictures/Softcover",
            )
        }

        @Test
        fun `maps Cached to Cached carrying identifier`() = runTest {
            // ----- Arrange -----
            coEvery {
                foundation.saveToGallery(any())
            } returns FoundationSaveOutcome.Cached(identifier = "cache/a.png")

            // ----- Act -----
            val outcome = capture.saveToGallery(displayName = "card")

            // ----- Assert -----
            outcome shouldBe SaveOutcome.Cached(identifier = "cache/a.png")
        }

        @Test
        fun `maps Failure to Failure carrying reason`() = runTest {
            // ----- Arrange -----
            coEvery {
                foundation.saveToGallery(any())
            } returns FoundationSaveOutcome.Failure(reason = "disk full")

            // ----- Act -----
            val outcome = capture.saveToGallery(displayName = "card")

            // ----- Assert -----
            outcome shouldBe SaveOutcome.Failure(reason = "disk full")
        }
    }

    @Nested
    inner class SaveToCache {
        @Test
        fun `maps Saved to Saved carrying identifier and display path`() = runTest {
            // ----- Arrange -----
            coEvery {
                foundation.saveToCache(any())
            } returns FoundationSaveOutcome.Saved(
                identifier = "content://1",
                displayPath = "Pictures/Softcover",
            )

            // ----- Act -----
            val outcome = capture.saveToCache(displayName = "card")

            // ----- Assert -----
            outcome shouldBe SaveOutcome.Saved(
                identifier = "content://1",
                displayPath = "Pictures/Softcover",
            )
        }

        @Test
        fun `maps Cached to Cached carrying identifier`() = runTest {
            // ----- Arrange -----
            coEvery {
                foundation.saveToCache(any())
            } returns FoundationSaveOutcome.Cached(identifier = "cache/a.png")

            // ----- Act -----
            val outcome = capture.saveToCache(displayName = "card")

            // ----- Assert -----
            outcome shouldBe SaveOutcome.Cached(identifier = "cache/a.png")
        }

        @Test
        fun `maps Failure to Failure carrying reason`() = runTest {
            // ----- Arrange -----
            coEvery {
                foundation.saveToCache(any())
            } returns FoundationSaveOutcome.Failure(reason = "disk full")

            // ----- Act -----
            val outcome = capture.saveToCache(displayName = "card")

            // ----- Assert -----
            outcome shouldBe SaveOutcome.Failure(reason = "disk full")
        }
    }

    @Nested
    inner class Share {
        @Test
        fun `maps Shared to Shared`() = runTest {
            // ----- Arrange -----
            coEvery {
                foundation.share(any())
            } returns FoundationShareOutcome.Shared

            // ----- Act -----
            val outcome = capture.share(displayName = "card")

            // ----- Assert -----
            outcome shouldBe ShareOutcome.Shared
        }

        @Test
        fun `maps Cancelled to Cancelled`() = runTest {
            // ----- Arrange -----
            coEvery {
                foundation.share(any())
            } returns FoundationShareOutcome.Cancelled

            // ----- Act -----
            val outcome = capture.share(displayName = "card")

            // ----- Assert -----
            outcome shouldBe ShareOutcome.Cancelled
        }

        @Test
        fun `maps Failure to Failure carrying reason`() = runTest {
            // ----- Arrange -----
            coEvery {
                foundation.share(any())
            } returns FoundationShareOutcome.Failure(reason = "no handler")

            // ----- Act -----
            val outcome = capture.share(displayName = "card")

            // ----- Assert -----
            outcome shouldBe ShareOutcome.Failure(reason = "no handler")
        }
    }
}
