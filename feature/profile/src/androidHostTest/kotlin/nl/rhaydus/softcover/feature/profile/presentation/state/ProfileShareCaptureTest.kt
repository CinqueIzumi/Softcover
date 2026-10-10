package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.share.ShareCaptureProgress
import nl.rhaydus.softcover.core.component.share.ShareCaptureState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProfileShareCaptureTest {
    @Nested
    inner class ProfileShareSheetFor {
        @Test
        fun `names the download after the reader`() {
            // ----- Arrange -----
            val card = buildShareCard(name = "Jane Doe")

            // ----- Act -----
            val sheet = profileShareSheetFor(
                card = card,
                progress = ShareCaptureProgress.Idle,
                closeRequested = false,
            )

            // ----- Assert -----
            sheet.displayName shouldBe "Jane Doe-reading-life"
            sheet.card shouldBe card
        }

        @Test
        fun `carries the progress and close request into the scaffold`() {
            // ----- Arrange -----
            val card = buildShareCard()

            // ----- Act -----
            val sheet = profileShareSheetFor(
                card = card,
                progress = ShareCaptureProgress.Saving,
                closeRequested = true,
            )

            // ----- Assert -----
            sheet.scaffold.busy shouldBe true
            sheet.scaffold.closeRequested shouldBe true
            sheet.scaffold.footer.shouldNotBeNull().secondary.shouldNotBeNull().running shouldBe true
        }
    }

    @Nested
    inner class WithShareCapture {
        @Test
        fun `returns the state with the capture recorded when no sheet is held`() {
            // ----- Arrange -----
            val capture = ShareCaptureState(progress = ShareCaptureProgress.Sharing)

            // ----- Act -----
            val result = ProfileUiState().withShareCapture(capture)

            // ----- Assert -----
            result.shareCapture shouldBe capture
            result.shareSheet shouldBe null
        }

        @Test
        fun `keeps the held card while rebuilding the scaffold`() {
            // ----- Arrange -----
            val card = buildShareCard()
            val state = ProfileUiState(
                shareSheet = profileShareSheetFor(
                    card = card,
                    progress = ShareCaptureProgress.Idle,
                    closeRequested = false,
                ),
            )

            // ----- Act -----
            val result = state.withShareCapture(ShareCaptureState(closeRequested = true))

            // ----- Assert -----
            val sheet = result.shareSheet.shouldNotBeNull()
            sheet.card shouldBe card
            sheet.scaffold.closeRequested shouldBe true
        }
    }
}
