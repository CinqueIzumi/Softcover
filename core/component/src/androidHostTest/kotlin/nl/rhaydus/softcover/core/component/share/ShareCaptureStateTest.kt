package nl.rhaydus.softcover.core.component.share

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShareCaptureStateTest {
    private val scaffold = SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Share",
                headline = "Piranesi",
            ),
        ),
    )

    @Nested
    inner class Reduce {
        @Test
        fun `ShareStarted marks the capture as sharing`() {
            // ----- Arrange -----
            val state = ShareCaptureState()

            // ----- Act -----
            val result = state.reduce(ShareCardSheetEvent.ShareStarted)

            // ----- Assert -----
            result shouldBe ShareCaptureState(progress = ShareCaptureProgress.Sharing)
        }

        @Test
        fun `SaveStarted marks the capture as saving`() {
            // ----- Arrange -----
            val state = ShareCaptureState()

            // ----- Act -----
            val result = state.reduce(ShareCardSheetEvent.SaveStarted)

            // ----- Assert -----
            result shouldBe ShareCaptureState(progress = ShareCaptureProgress.Saving)
        }

        @Test
        fun `CaptureFinished returns to idle from either capture`() {
            // ----- Arrange -----
            val sharing = ShareCaptureState(progress = ShareCaptureProgress.Sharing)
            val saving = ShareCaptureState(progress = ShareCaptureProgress.Saving)

            // ----- Act -----
            val afterShare = sharing.reduce(ShareCardSheetEvent.CaptureFinished)
            val afterSave = saving.reduce(ShareCardSheetEvent.CaptureFinished)

            // ----- Assert -----
            afterShare.progress shouldBe ShareCaptureProgress.Idle
            afterSave.progress shouldBe ShareCaptureProgress.Idle
        }

        @Test
        fun `Shared requests close and keeps the progress`() {
            // ----- Arrange -----
            val state = ShareCaptureState(progress = ShareCaptureProgress.Sharing)

            // ----- Act -----
            val result = state.reduce(ShareCardSheetEvent.Shared)

            // ----- Assert -----
            result shouldBe ShareCaptureState(
                progress = ShareCaptureProgress.Sharing,
                closeRequested = true,
            )
        }

        @Test
        fun `Dismissed changes nothing`() {
            // ----- Arrange -----
            val state = ShareCaptureState(
                progress = ShareCaptureProgress.Saving,
                closeRequested = true,
            )

            // ----- Act -----
            val result = state.reduce(ShareCardSheetEvent.Dismissed)

            // ----- Assert -----
            result shouldBe state
        }

        @Test
        fun `a full share flow ends idle with close requested`() {
            // ----- Arrange -----
            val events = listOf(
                ShareCardSheetEvent.ShareStarted,
                ShareCardSheetEvent.Shared,
                ShareCardSheetEvent.CaptureFinished,
            )

            // ----- Act -----
            val result = events.fold(ShareCaptureState()) { state, event -> state.reduce(event) }

            // ----- Assert -----
            result shouldBe ShareCaptureState(
                progress = ShareCaptureProgress.Idle,
                closeRequested = true,
            )
        }
    }

    @Nested
    inner class WithShareCapture {
        @Test
        fun `an idle capture shows the resting labels and is not busy`() {
            // ----- Arrange -----
            val capture = ShareCaptureState()

            // ----- Act -----
            val result = scaffold.withShareCapture(capture)

            // ----- Assert -----
            val footer = result.footer.shouldBeInstanceOf<SheetFooterUiModel.Inline>()
            footer.primary.label shouldBe "Share"
            footer.secondary.label shouldBe "Save to gallery"
            result.busy shouldBe false
            result.closeRequested shouldBe false
        }

        @Test
        fun `sharing relabels only the primary button and is busy`() {
            // ----- Arrange -----
            val capture = ShareCaptureState(progress = ShareCaptureProgress.Sharing)

            // ----- Act -----
            val result = scaffold.withShareCapture(capture)

            // ----- Assert -----
            val footer = result.footer.shouldBeInstanceOf<SheetFooterUiModel.Inline>()
            footer.primary.label shouldBe "Sharing…"
            footer.primary.running shouldBe true
            footer.secondary.label shouldBe "Save to gallery"
            result.busy shouldBe true
        }

        @Test
        fun `saving runs only the secondary button and is busy`() {
            // ----- Arrange -----
            val capture = ShareCaptureState(progress = ShareCaptureProgress.Saving)

            // ----- Act -----
            val result = scaffold.withShareCapture(capture)

            // ----- Assert -----
            val footer = result.footer.shouldBeInstanceOf<SheetFooterUiModel.Inline>()
            footer.primary.label shouldBe "Share"
            footer.secondary.label shouldBe "Save to gallery"
            footer.secondary.running shouldBe true
            result.busy shouldBe true
        }

        @Test
        fun `closeRequested is carried onto the scaffold`() {
            // ----- Arrange -----
            val capture = ShareCaptureState(closeRequested = true)

            // ----- Act -----
            val result = scaffold.withShareCapture(capture)

            // ----- Assert -----
            result.closeRequested shouldBe true
        }

        @Test
        fun `applying it twice gives the same scaffold`() {
            // ----- Arrange -----
            val capture = ShareCaptureState(progress = ShareCaptureProgress.Saving)

            // ----- Act -----
            val once = scaffold.withShareCapture(capture)
            val twice = once.withShareCapture(capture)

            // ----- Assert -----
            twice shouldBe once
        }
    }
}
